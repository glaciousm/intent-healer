package io.github.glaciousm.playwright;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.glaciousm.core.model.ElementRect;
import io.github.glaciousm.core.model.ElementSnapshot;
import io.github.glaciousm.core.model.UiSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Captures UI snapshots from Playwright Page for healing analysis.
 * Extracts element attributes, positions, and page state for LLM evaluation.
 */
public class PlaywrightSnapshotBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PlaywrightSnapshotBuilder.class);

    private static final int DEFAULT_MAX_ELEMENTS = 100;
    private static final String[] INTERACTIVE_SELECTORS = {
            "button", "a", "input", "select", "textarea",
            "[role='button']", "[role='link']", "[role='textbox']",
            "[role='combobox']", "[role='checkbox']", "[role='radio']",
            "[onclick]", "[data-testid]", "[data-test]", "[data-cy]"
    };

    private final Page page;
    private int maxElements = DEFAULT_MAX_ELEMENTS;
    private boolean includeHidden = false;
    private boolean captureScreenshot = true;

    public PlaywrightSnapshotBuilder(Page page) {
        this.page = page;
    }

    /**
     * Set maximum number of elements to capture.
     */
    public PlaywrightSnapshotBuilder maxElements(int max) {
        this.maxElements = max;
        return this;
    }

    /**
     * Whether to include hidden elements.
     */
    public PlaywrightSnapshotBuilder includeHidden(boolean include) {
        this.includeHidden = include;
        return this;
    }

    /**
     * Whether to capture screenshot.
     */
    public PlaywrightSnapshotBuilder captureScreenshot(boolean capture) {
        this.captureScreenshot = capture;
        return this;
    }

    /**
     * Capture a complete UI snapshot of the current page.
     */
    public UiSnapshot captureAll() {
        List<ElementSnapshot> elements = new ArrayList<>();

        try {
            // Capture interactive elements
            for (String selector : INTERACTIVE_SELECTORS) {
                if (elements.size() >= maxElements) break;
                captureElements(selector, elements);
            }

            // Sort by position (top to bottom, left to right)
            elements.sort(Comparator
                    .comparingInt((ElementSnapshot e) -> e.getRect() != null ? e.getRect().getY() : 0)
                    .thenComparingInt((ElementSnapshot e) -> e.getRect() != null ? e.getRect().getX() : 0));

        } catch (Exception e) {
            logger.warn("Error capturing elements: {}", e.getMessage());
        }

        String screenshotBase64 = null;
        if (captureScreenshot) {
            screenshotBase64 = captureScreenshotBase64();
        }

        return UiSnapshot.builder()
                .url(page.url())
                .title(page.title())
                .interactiveElements(elements)
                .screenshotBase64(screenshotBase64)
                .build();
    }

    /**
     * Capture elements matching the given selector.
     */
    private void captureElements(String selector, List<ElementSnapshot> elements) {
        try {
            Locator locator = page.locator(selector);
            int count = locator.count();

            for (int i = 0; i < count && elements.size() < maxElements; i++) {
                try {
                    Locator element = locator.nth(i);

                    if (!includeHidden && !element.isVisible()) {
                        continue;
                    }

                    ElementSnapshot snapshot = captureElement(element, elements.size());
                    if (snapshot != null) {
                        elements.add(snapshot);
                    }
                } catch (Exception e) {
                    logger.trace("Skipping element {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.trace("Error with selector {}: {}", selector, e.getMessage());
        }
    }

    /**
     * Capture a single element's attributes.
     */
    private ElementSnapshot captureElement(Locator element, int index) {
        try {
            // Get bounding box
            var box = element.boundingBox();
            int x = box != null ? (int) box.x : 0;
            int y = box != null ? (int) box.y : 0;
            int width = box != null ? (int) box.width : 0;
            int height = box != null ? (int) box.height : 0;

            // Get element properties
            String tagName = element.evaluate("el => el.tagName.toLowerCase()").toString();
            String id = getAttributeSafe(element, "id");
            String name = getAttributeSafe(element, "name");
            String className = getAttributeSafe(element, "class");
            String type = getAttributeSafe(element, "type");
            String text = getTextSafe(element);
            String value = getAttributeSafe(element, "value");
            String placeholder = getAttributeSafe(element, "placeholder");
            String href = getAttributeSafe(element, "href");
            String ariaLabel = getAttributeSafe(element, "aria-label");
            String ariaRole = getAttributeSafe(element, "role");
            String title = getAttributeSafe(element, "title");

            // Gather data attributes
            Map<String, String> dataAttributes = new HashMap<>();
            String dataTestId = getAttributeSafe(element, "data-testid");
            if (dataTestId != null) {
                dataAttributes.put("testid", dataTestId);
            } else {
                dataTestId = getAttributeSafe(element, "data-test");
                if (dataTestId != null) {
                    dataAttributes.put("testid", dataTestId);
                } else {
                    dataTestId = getAttributeSafe(element, "data-cy");
                    if (dataTestId != null) {
                        dataAttributes.put("testid", dataTestId);
                    }
                }
            }
            if (href != null) {
                dataAttributes.put("href", href);
            }

            boolean isEnabled = element.isEnabled();
            boolean isVisible = element.isVisible();

            // Build classes list
            List<String> classes = className != null && !className.isEmpty()
                    ? Arrays.asList(className.split("\\s+"))
                    : Collections.emptyList();

            return ElementSnapshot.builder()
                    .index(index)
                    .tagName(tagName)
                    .id(id)
                    .name(name)
                    .classes(classes)
                    .type(type)
                    .text(text)
                    .value(value)
                    .placeholder(placeholder)
                    .ariaLabel(ariaLabel)
                    .ariaRole(ariaRole)
                    .title(title)
                    .rect(new ElementRect(x, y, width, height))
                    .enabled(isEnabled)
                    .visible(isVisible)
                    .dataAttributes(dataAttributes)
                    .build();

        } catch (Exception e) {
            logger.trace("Error capturing element: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Safely get an attribute value.
     */
    private String getAttributeSafe(Locator element, String attribute) {
        try {
            String value = element.getAttribute(attribute);
            return value != null && !value.isEmpty() ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely get element text content.
     */
    private String getTextSafe(Locator element) {
        try {
            String text = element.textContent();
            if (text != null) {
                text = text.trim();
                // Limit text length
                if (text.length() > 100) {
                    text = text.substring(0, 100) + "...";
                }
            }
            return text != null && !text.isEmpty() ? text : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Capture screenshot as Base64 string.
     */
    private String captureScreenshotBase64() {
        try {
            byte[] screenshot = page.screenshot();
            return Base64.getEncoder().encodeToString(screenshot);
        } catch (Exception e) {
            logger.debug("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Capture elements near a specific location (for context).
     */
    public List<ElementSnapshot> captureNearby(int centerX, int centerY, int radius) {
        List<ElementSnapshot> nearby = new ArrayList<>();

        try {
            UiSnapshot fullSnapshot = captureAll();
            for (ElementSnapshot element : fullSnapshot.getInteractiveElements()) {
                ElementRect rect = element.getRect();
                if (rect == null) continue;

                int elCenterX = rect.getX() + rect.getWidth() / 2;
                int elCenterY = rect.getY() + rect.getHeight() / 2;

                double distance = Math.sqrt(
                        Math.pow(elCenterX - centerX, 2) +
                        Math.pow(elCenterY - centerY, 2)
                );

                if (distance <= radius) {
                    nearby.add(element);
                }
            }
        } catch (Exception e) {
            logger.warn("Error capturing nearby elements: {}", e.getMessage());
        }

        return nearby;
    }
}

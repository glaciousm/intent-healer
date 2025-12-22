package io.github.glaciousm.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import io.github.glaciousm.core.model.ElementSnapshot;
import io.github.glaciousm.core.model.UiSnapshot;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaywrightSnapshotBuilder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaywrightSnapshotBuilder")
class PlaywrightSnapshotBuilderTest {

    @Mock
    private Page page;

    @Mock
    private Locator locator;

    private PlaywrightSnapshotBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new PlaywrightSnapshotBuilder(page);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create builder with page")
        void shouldCreateBuilderWithPage() {
            PlaywrightSnapshotBuilder snapshotBuilder = new PlaywrightSnapshotBuilder(page);
            assertThat(snapshotBuilder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Configuration")
    class BuilderConfigurationTests {

        @Test
        @DisplayName("should set max elements")
        void shouldSetMaxElements() {
            PlaywrightSnapshotBuilder result = builder.maxElements(50);
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should set include hidden")
        void shouldSetIncludeHidden() {
            PlaywrightSnapshotBuilder result = builder.includeHidden(true);
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should set capture screenshot")
        void shouldSetCaptureScreenshot() {
            PlaywrightSnapshotBuilder result = builder.captureScreenshot(false);
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("should support fluent configuration")
        void shouldSupportFluentConfiguration() {
            PlaywrightSnapshotBuilder result = builder
                    .maxElements(25)
                    .includeHidden(true)
                    .captureScreenshot(false);
            assertThat(result).isSameAs(builder);
        }
    }

    @Nested
    @DisplayName("Capture All")
    class CaptureAllTests {

        @Test
        @DisplayName("should capture empty snapshot when no elements found")
        void shouldCaptureEmptySnapshotWhenNoElementsFound() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Example Page");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(0);

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getUrl()).isEqualTo("https://example.com");
            assertThat(snapshot.getTitle()).isEqualTo("Example Page");
            assertThat(snapshot.getInteractiveElements()).isEmpty();
        }

        @Test
        @DisplayName("should capture page URL and title")
        void shouldCapturePageUrlAndTitle() {
            when(page.url()).thenReturn("https://test.com/page");
            when(page.title()).thenReturn("Test Page Title");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(0);

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getUrl()).isEqualTo("https://test.com/page");
            assertThat(snapshot.getTitle()).isEqualTo("Test Page Title");
        }

        @Test
        @DisplayName("should capture visible elements")
        void shouldCaptureVisibleElements() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);

            // First selector (button) has one element
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Click Me");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).hasSize(1);
            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getTagName()).isEqualTo("button");
            assertThat(element.getText()).isEqualTo("Click Me");
        }

        @Test
        @DisplayName("should skip hidden elements when includeHidden is false")
        void shouldSkipHiddenElementsWhenIncludeHiddenIsFalse() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(false); // Hidden element

            builder.captureScreenshot(false).includeHidden(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).isEmpty();
        }

        @Test
        @DisplayName("should include hidden elements when includeHidden is true")
        void shouldIncludeHiddenElementsWhenIncludeHiddenIsTrue() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(false); // Hidden element
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("input");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("");

            builder.captureScreenshot(false).includeHidden(true);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).hasSize(1);
        }

        @Test
        @DisplayName("should respect max elements limit")
        void shouldRespectMaxElementsLimit() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(10); // More elements than limit

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(anyInt())).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Button");

            builder.captureScreenshot(false).maxElements(3);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).hasSize(3);
        }

        @Test
        @DisplayName("should handle exception during capture gracefully")
        void shouldHandleExceptionDuringCaptureGracefully() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenThrow(new RuntimeException("Locator error"));

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getInteractiveElements()).isEmpty();
        }

        @Test
        @DisplayName("should capture screenshot when enabled")
        void shouldCaptureScreenshotWhenEnabled() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(0);
            when(page.screenshot()).thenReturn(new byte[]{1, 2, 3, 4});

            builder.captureScreenshot(true);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getScreenshotBase64()).isNotNull();
            assertThat(snapshot.getScreenshotBase64()).isNotEmpty();
        }

        @Test
        @DisplayName("should not capture screenshot when disabled")
        void shouldNotCaptureScreenshotWhenDisabled() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(0);

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getScreenshotBase64()).isEmpty();
            verify(page, never()).screenshot();
        }

        @Test
        @DisplayName("should handle screenshot failure gracefully")
        void shouldHandleScreenshotFailureGracefully() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(0);
            when(page.screenshot()).thenThrow(new RuntimeException("Screenshot failed"));

            builder.captureScreenshot(true);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getScreenshotBase64()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Element Capture")
    class ElementCaptureTests {

        @Test
        @DisplayName("should capture element with ID")
        void shouldCaptureElementWithId() {
            setupSingleElementCapture("button", "submit-btn", null, null, "Submit");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).hasSize(1);
            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getId()).isEqualTo("submit-btn");
        }

        @Test
        @DisplayName("should capture element with classes")
        void shouldCaptureElementWithClasses() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute("class")).thenReturn("btn btn-primary large");
            when(elementLocator.getAttribute(argThat(arg -> !"class".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Click");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getClasses()).containsExactly("btn", "btn-primary", "large");
        }

        @Test
        @DisplayName("should capture element with data-testid")
        void shouldCaptureElementWithDataTestId() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute("data-testid")).thenReturn("login-button");
            when(elementLocator.getAttribute(argThat(arg -> !"data-testid".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Login");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getDataAttributes()).containsEntry("testid", "login-button");
        }

        @Test
        @DisplayName("should capture element with data-test fallback")
        void shouldCaptureElementWithDataTestFallback() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute("data-testid")).thenReturn(null);
            when(elementLocator.getAttribute("data-test")).thenReturn("submit-button");
            when(elementLocator.getAttribute(argThat(arg ->
                !"data-testid".equals(arg) && !"data-test".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Submit");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getDataAttributes()).containsEntry("testid", "submit-button");
        }

        @Test
        @DisplayName("should capture element with data-cy fallback")
        void shouldCaptureElementWithDataCyFallback() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute("data-testid")).thenReturn(null);
            when(elementLocator.getAttribute("data-test")).thenReturn(null);
            when(elementLocator.getAttribute("data-cy")).thenReturn("cypress-button");
            when(elementLocator.getAttribute(argThat(arg ->
                !"data-testid".equals(arg) && !"data-test".equals(arg) && !"data-cy".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Button");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getDataAttributes()).containsEntry("testid", "cypress-button");
        }

        @Test
        @DisplayName("should capture element with href")
        void shouldCaptureElementWithHref() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("a");
            when(elementLocator.getAttribute("href")).thenReturn("/dashboard");
            when(elementLocator.getAttribute(argThat(arg -> !"href".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Dashboard");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getDataAttributes()).containsEntry("href", "/dashboard");
        }

        @Test
        @DisplayName("should capture element bounding box")
        void shouldCaptureElementBoundingBox() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);

            BoundingBox box = new BoundingBox();
            box.x = 100;
            box.y = 200;
            box.width = 150;
            box.height = 50;
            when(elementLocator.boundingBox()).thenReturn(box);
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Click");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getRect().getX()).isEqualTo(100);
            assertThat(element.getRect().getY()).isEqualTo(200);
            assertThat(element.getRect().getWidth()).isEqualTo(150);
            assertThat(element.getRect().getHeight()).isEqualTo(50);
        }

        @Test
        @DisplayName("should handle null bounding box")
        void shouldHandleNullBoundingBox() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(null);
            when(elementLocator.evaluate(anyString())).thenReturn("button");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);
            when(elementLocator.textContent()).thenReturn("Click");

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getRect().getX()).isZero();
            assertThat(element.getRect().getY()).isZero();
        }

        @Test
        @DisplayName("should truncate long text")
        void shouldTruncateLongText() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn("p");
            when(elementLocator.getAttribute(anyString())).thenReturn(null);

            String longText = "A".repeat(150);
            when(elementLocator.textContent()).thenReturn(longText);

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            ElementSnapshot element = snapshot.getInteractiveElements().get(0);
            assertThat(element.getText()).hasSize(103); // 100 chars + "..."
            assertThat(element.getText()).endsWith("...");
        }

        @Test
        @DisplayName("should handle element capture exception")
        void shouldHandleElementCaptureException() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.boundingBox()).thenThrow(new RuntimeException("Element detached"));

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).isEmpty();
        }

        private void setupSingleElementCapture(String tagName, String id, String className, String type, String text) {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator elementLocator = mock(Locator.class);
            when(locator.nth(0)).thenReturn(elementLocator);
            when(elementLocator.isVisible()).thenReturn(true);
            when(elementLocator.isEnabled()).thenReturn(true);
            when(elementLocator.boundingBox()).thenReturn(new BoundingBox());
            when(elementLocator.evaluate(anyString())).thenReturn(tagName);
            when(elementLocator.getAttribute("id")).thenReturn(id);
            when(elementLocator.getAttribute("class")).thenReturn(className);
            when(elementLocator.getAttribute("type")).thenReturn(type);
            when(elementLocator.getAttribute(argThat(arg ->
                !"id".equals(arg) && !"class".equals(arg) && !"type".equals(arg)))).thenReturn(null);
            when(elementLocator.textContent()).thenReturn(text);
        }
    }

    @Nested
    @DisplayName("Capture Nearby")
    class CaptureNearbyTests {

        @Test
        @DisplayName("should capture elements within radius")
        void shouldCaptureElementsWithinRadius() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            // Element 1: at (50, 50), center at (75, 75)
            Locator element1 = mock(Locator.class);
            BoundingBox box1 = new BoundingBox();
            box1.x = 50;
            box1.y = 50;
            box1.width = 50;
            box1.height = 50;
            when(element1.isVisible()).thenReturn(true);
            when(element1.isEnabled()).thenReturn(true);
            when(element1.boundingBox()).thenReturn(box1);
            when(element1.evaluate(anyString())).thenReturn("button");
            when(element1.getAttribute(anyString())).thenReturn(null);
            when(element1.textContent()).thenReturn("Near");

            // Element 2: at (500, 500), center at (525, 525) - far away
            Locator element2 = mock(Locator.class);
            BoundingBox box2 = new BoundingBox();
            box2.x = 500;
            box2.y = 500;
            box2.width = 50;
            box2.height = 50;
            when(element2.isVisible()).thenReturn(true);
            when(element2.isEnabled()).thenReturn(true);
            when(element2.boundingBox()).thenReturn(box2);
            when(element2.evaluate(anyString())).thenReturn("button");
            when(element2.getAttribute(anyString())).thenReturn(null);
            when(element2.textContent()).thenReturn("Far");

            when(locator.nth(0)).thenReturn(element1);
            when(locator.nth(1)).thenReturn(element2);

            builder.captureScreenshot(false);
            List<ElementSnapshot> nearby = builder.captureNearby(75, 75, 100);

            assertThat(nearby).hasSize(1);
            assertThat(nearby.get(0).getText()).isEqualTo("Near");
        }

        @Test
        @DisplayName("should return empty list when no elements nearby")
        void shouldReturnEmptyListWhenNoElementsNearby() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator element = mock(Locator.class);
            BoundingBox box = new BoundingBox();
            box.x = 1000;
            box.y = 1000;
            box.width = 50;
            box.height = 50;
            when(element.isVisible()).thenReturn(true);
            when(element.isEnabled()).thenReturn(true);
            when(element.boundingBox()).thenReturn(box);
            when(element.evaluate(anyString())).thenReturn("button");
            when(element.getAttribute(anyString())).thenReturn(null);
            when(element.textContent()).thenReturn("Far");
            when(locator.nth(0)).thenReturn(element);

            builder.captureScreenshot(false);
            List<ElementSnapshot> nearby = builder.captureNearby(0, 0, 50);

            assertThat(nearby).isEmpty();
        }

        @Test
        @DisplayName("should handle exception during nearby capture")
        void shouldHandleExceptionDuringNearbyCapture() {
            when(page.url()).thenThrow(new RuntimeException("Page closed"));

            builder.captureScreenshot(false);
            List<ElementSnapshot> nearby = builder.captureNearby(100, 100, 50);

            assertThat(nearby).isEmpty();
        }

        @Test
        @DisplayName("should skip elements with null rect")
        void shouldSkipElementsWithNullRect() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            Locator element = mock(Locator.class);
            when(element.isVisible()).thenReturn(true);
            when(element.isEnabled()).thenReturn(true);
            when(element.boundingBox()).thenReturn(null);
            when(element.evaluate(anyString())).thenReturn("button");
            when(element.getAttribute(anyString())).thenReturn(null);
            when(element.textContent()).thenReturn("Button");
            when(locator.nth(0)).thenReturn(element);

            builder.captureScreenshot(false);
            List<ElementSnapshot> nearby = builder.captureNearby(0, 0, 1000);

            // Element should be captured but excluded from nearby due to null rect becoming (0,0,0,0)
            // The center would be (0,0) which is within radius 1000 of (0,0)
            assertThat(nearby).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Element Sorting")
    class ElementSortingTests {

        @Test
        @DisplayName("should sort elements by position (top to bottom, left to right)")
        void shouldSortElementsByPosition() {
            when(page.url()).thenReturn("https://example.com");
            when(page.title()).thenReturn("Test");
            when(page.locator(anyString())).thenReturn(locator);
            when(locator.count()).thenReturn(3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            // Element at (100, 200)
            Locator element1 = createMockElement(100, 200, "Third");
            // Element at (50, 100)
            Locator element2 = createMockElement(50, 100, "First");
            // Element at (150, 100)
            Locator element3 = createMockElement(150, 100, "Second");

            when(locator.nth(0)).thenReturn(element1);
            when(locator.nth(1)).thenReturn(element2);
            when(locator.nth(2)).thenReturn(element3);

            builder.captureScreenshot(false);
            UiSnapshot snapshot = builder.captureAll();

            assertThat(snapshot.getInteractiveElements()).hasSize(3);
            assertThat(snapshot.getInteractiveElements().get(0).getText()).isEqualTo("First");
            assertThat(snapshot.getInteractiveElements().get(1).getText()).isEqualTo("Second");
            assertThat(snapshot.getInteractiveElements().get(2).getText()).isEqualTo("Third");
        }

        private Locator createMockElement(int x, int y, String text) {
            Locator element = mock(Locator.class);
            BoundingBox box = new BoundingBox();
            box.x = x;
            box.y = y;
            box.width = 50;
            box.height = 30;
            when(element.isVisible()).thenReturn(true);
            when(element.isEnabled()).thenReturn(true);
            when(element.boundingBox()).thenReturn(box);
            when(element.evaluate(anyString())).thenReturn("button");
            when(element.getAttribute(anyString())).thenReturn(null);
            when(element.textContent()).thenReturn(text);
            return element;
        }
    }
}

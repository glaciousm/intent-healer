package com.intenthealer.selenium.driver;

import com.intenthealer.core.config.HealerConfig;
import com.intenthealer.core.engine.HealingEngine;
import com.intenthealer.core.model.*;
import com.intenthealer.selenium.snapshot.SnapshotBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * WebDriver wrapper that provides automatic healing capabilities.
 * Intercepts element location failures and attempts to heal using LLM.
 */
public class HealingWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot, Interactive {

    private static final Logger logger = LoggerFactory.getLogger(HealingWebDriver.class);

    private final WebDriver delegate;
    private final HealingEngine healingEngine;
    private final SnapshotBuilder snapshotBuilder;
    private final HealerConfig config;

    // Current context for healing
    private IntentContract currentIntent;
    private String currentStepText;

    public HealingWebDriver(WebDriver delegate, HealingEngine healingEngine, HealerConfig config) {
        this.delegate = delegate;
        this.healingEngine = healingEngine;
        this.config = config;
        this.snapshotBuilder = new SnapshotBuilder(delegate);
    }

    /**
     * Set the current intent context for healing.
     */
    public void setCurrentIntent(IntentContract intent, String stepText) {
        this.currentIntent = intent;
        this.currentStepText = stepText;
    }

    /**
     * Clear the current intent context.
     */
    public void clearCurrentIntent() {
        this.currentIntent = null;
        this.currentStepText = null;
    }

    @Override
    public void get(String url) {
        delegate.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return delegate.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        try {
            return wrapElements(delegate.findElements(by), by);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("findElements failed for {}, attempting heal", by);
            return handleFindElementsFailure(by, e);
        }
    }

    @Override
    public WebElement findElement(By by) {
        try {
            return wrapElement(delegate.findElement(by), by);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("findElement failed for {}, attempting heal", by);
            return handleFindElementFailure(by, e);
        }
    }

    @Override
    public String getPageSource() {
        return delegate.getPageSource();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void quit() {
        delegate.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return delegate.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return delegate.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return delegate.switchTo();
    }

    @Override
    public Navigation navigate() {
        return delegate.navigate();
    }

    @Override
    public Options manage() {
        return delegate.manage();
    }

    // JavascriptExecutor
    @Override
    public Object executeScript(String script, Object... args) {
        if (delegate instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) delegate).executeScript(script, args);
        }
        throw new UnsupportedOperationException("Delegate does not support JavascriptExecutor");
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        if (delegate instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) delegate).executeAsyncScript(script, args);
        }
        throw new UnsupportedOperationException("Delegate does not support JavascriptExecutor");
    }

    // TakesScreenshot
    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        if (delegate instanceof TakesScreenshot) {
            return ((TakesScreenshot) delegate).getScreenshotAs(target);
        }
        throw new UnsupportedOperationException("Delegate does not support TakesScreenshot");
    }

    // Interactive
    @Override
    public void perform(Collection<Sequence> actions) {
        if (delegate instanceof Interactive) {
            ((Interactive) delegate).perform(actions);
        } else {
            throw new UnsupportedOperationException("Delegate does not support Interactive");
        }
    }

    @Override
    public void resetInputState() {
        if (delegate instanceof Interactive) {
            ((Interactive) delegate).resetInputState();
        }
    }

    /**
     * Get the underlying delegate driver.
     */
    public WebDriver getDelegate() {
        return delegate;
    }

    /**
     * Handle a failed findElement call by attempting to heal.
     */
    private WebElement handleFindElementFailure(By by, RuntimeException originalException) {
        if (!shouldAttemptHeal(originalException)) {
            throw originalException;
        }

        try {
            LocatorInfo originalLocator = byToLocatorInfo(by);
            UiSnapshot snapshot = snapshotBuilder.captureSnapshot();

            FailureContext failureContext = FailureContext.builder()
                    .exception(originalException)
                    .originalLocator(originalLocator)
                    .stepText(currentStepText)
                    .pageUrl(getCurrentUrl())
                    .pageTitle(getTitle())
                    .build();

            IntentContract intent = currentIntent != null
                    ? currentIntent
                    : IntentContract.defaultContract(currentStepText != null ? currentStepText : "find element");

            HealResult result = healingEngine.attemptHeal(failureContext, snapshot, intent);

            if (result.isSuccess() && result.getHealedLocator() != null) {
                By healedBy = locatorInfoToBy(result.getHealedLocator());
                logger.info("Healed locator: {} -> {}", by, healedBy);
                return wrapElement(delegate.findElement(healedBy), healedBy);
            }

        } catch (Exception healException) {
            logger.warn("Healing attempt failed: {}", healException.getMessage());
        }

        throw originalException;
    }

    /**
     * Handle a failed findElements call by attempting to heal.
     */
    private List<WebElement> handleFindElementsFailure(By by, RuntimeException originalException) {
        if (!shouldAttemptHeal(originalException)) {
            throw originalException;
        }

        try {
            LocatorInfo originalLocator = byToLocatorInfo(by);
            UiSnapshot snapshot = snapshotBuilder.captureSnapshot();

            FailureContext failureContext = FailureContext.builder()
                    .exception(originalException)
                    .originalLocator(originalLocator)
                    .stepText(currentStepText)
                    .pageUrl(getCurrentUrl())
                    .pageTitle(getTitle())
                    .build();

            IntentContract intent = currentIntent != null
                    ? currentIntent
                    : IntentContract.defaultContract(currentStepText != null ? currentStepText : "find elements");

            HealResult result = healingEngine.attemptHeal(failureContext, snapshot, intent);

            if (result.isSuccess() && result.getHealedLocator() != null) {
                By healedBy = locatorInfoToBy(result.getHealedLocator());
                logger.info("Healed locator: {} -> {}", by, healedBy);
                return wrapElements(delegate.findElements(healedBy), healedBy);
            }

        } catch (Exception healException) {
            logger.warn("Healing attempt failed: {}", healException.getMessage());
        }

        throw originalException;
    }

    /**
     * Check if we should attempt healing for this exception.
     */
    private boolean shouldAttemptHeal(RuntimeException e) {
        if (currentIntent != null && !currentIntent.isHealingAllowed()) {
            return false;
        }

        return e instanceof NoSuchElementException ||
               e instanceof StaleElementReferenceException;
    }

    /**
     * Convert a By to LocatorInfo.
     */
    private LocatorInfo byToLocatorInfo(By by) {
        String byString = by.toString();

        if (byString.startsWith("By.id:")) {
            return new LocatorInfo("id", byString.substring(7).trim());
        } else if (byString.startsWith("By.name:")) {
            return new LocatorInfo("name", byString.substring(8).trim());
        } else if (byString.startsWith("By.className:")) {
            return new LocatorInfo("className", byString.substring(13).trim());
        } else if (byString.startsWith("By.tagName:")) {
            return new LocatorInfo("tagName", byString.substring(11).trim());
        } else if (byString.startsWith("By.linkText:")) {
            return new LocatorInfo("linkText", byString.substring(12).trim());
        } else if (byString.startsWith("By.partialLinkText:")) {
            return new LocatorInfo("partialLinkText", byString.substring(19).trim());
        } else if (byString.startsWith("By.cssSelector:")) {
            return new LocatorInfo("css", byString.substring(15).trim());
        } else if (byString.startsWith("By.xpath:")) {
            return new LocatorInfo("xpath", byString.substring(9).trim());
        }

        return new LocatorInfo("unknown", byString);
    }

    /**
     * Convert a LocatorInfo back to By.
     */
    private By locatorInfoToBy(LocatorInfo locator) {
        return switch (locator.getStrategy().toLowerCase()) {
            case "id" -> By.id(locator.getValue());
            case "name" -> By.name(locator.getValue());
            case "classname", "class" -> By.className(locator.getValue());
            case "tagname", "tag" -> By.tagName(locator.getValue());
            case "linktext" -> By.linkText(locator.getValue());
            case "partiallinktext" -> By.partialLinkText(locator.getValue());
            case "css", "cssselector" -> By.cssSelector(locator.getValue());
            case "xpath" -> By.xpath(locator.getValue());
            default -> By.cssSelector(locator.getValue());
        };
    }

    /**
     * Wrap an element to provide healing capabilities.
     */
    private WebElement wrapElement(WebElement element, By by) {
        return new HealingWebElement(element, by, this);
    }

    /**
     * Wrap multiple elements to provide healing capabilities.
     */
    private List<WebElement> wrapElements(List<WebElement> elements, By by) {
        return elements.stream()
                .map(e -> wrapElement(e, by))
                .toList();
    }

    /**
     * Re-find an element after a stale element exception.
     */
    WebElement refindElement(By by) {
        return findElement(by);
    }
}

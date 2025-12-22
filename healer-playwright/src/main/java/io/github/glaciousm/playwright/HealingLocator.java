package io.github.glaciousm.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Playwright Locator wrapper that provides automatic healing capabilities.
 * Intercepts action failures and attempts to heal using LLM.
 */
public class HealingLocator {

    private static final Logger logger = LoggerFactory.getLogger(HealingLocator.class);
    private static final int MAX_HEAL_ATTEMPTS = 1;

    private Locator delegate;
    private final String originalSelector;
    private final HealingPage page;

    public HealingLocator(Locator delegate, String selector, HealingPage page) {
        this.delegate = delegate;
        this.originalSelector = selector;
        this.page = page;
    }

    /**
     * Get the underlying Locator.
     */
    public Locator getDelegate() {
        return delegate;
    }

    /**
     * Get the original selector used to create this locator.
     */
    public String getOriginalSelector() {
        return originalSelector;
    }

    // ==================== Click Actions ====================

    public void click() {
        executeWithHealing(() -> delegate.click());
    }

    public void click(Locator.ClickOptions options) {
        executeWithHealing(() -> delegate.click(options));
    }

    public void dblclick() {
        executeWithHealing(() -> delegate.dblclick());
    }

    public void dblclick(Locator.DblclickOptions options) {
        executeWithHealing(() -> delegate.dblclick(options));
    }

    // ==================== Input Actions ====================

    public void fill(String value) {
        executeWithHealing(() -> delegate.fill(value));
    }

    public void fill(String value, Locator.FillOptions options) {
        executeWithHealing(() -> delegate.fill(value, options));
    }

    public void type(String text) {
        executeWithHealing(() -> delegate.type(text));
    }

    public void type(String text, Locator.TypeOptions options) {
        executeWithHealing(() -> delegate.type(text, options));
    }

    public void press(String key) {
        executeWithHealing(() -> delegate.press(key));
    }

    public void pressSequentially(String text) {
        executeWithHealing(() -> delegate.pressSequentially(text));
    }

    public void clear() {
        executeWithHealing(() -> delegate.clear());
    }

    // ==================== Selection Actions ====================

    public List<String> selectOption(String value) {
        return executeWithHealingReturn(() -> delegate.selectOption(value));
    }

    public List<String> selectOption(String[] values) {
        return executeWithHealingReturn(() -> delegate.selectOption(values));
    }

    public List<String> selectOption(SelectOption value) {
        return executeWithHealingReturn(() -> delegate.selectOption(value));
    }

    public void check() {
        executeWithHealing(() -> delegate.check());
    }

    public void uncheck() {
        executeWithHealing(() -> delegate.uncheck());
    }

    public void setChecked(boolean checked) {
        executeWithHealing(() -> delegate.setChecked(checked));
    }

    // ==================== Hover & Focus ====================

    public void hover() {
        executeWithHealing(() -> delegate.hover());
    }

    public void hover(Locator.HoverOptions options) {
        executeWithHealing(() -> delegate.hover(options));
    }

    public void focus() {
        executeWithHealing(() -> delegate.focus());
    }

    public void blur() {
        executeWithHealing(() -> delegate.blur());
    }

    // ==================== File Upload ====================

    public void setInputFiles(Path file) {
        executeWithHealing(() -> delegate.setInputFiles(file));
    }

    public void setInputFiles(Path[] files) {
        executeWithHealing(() -> delegate.setInputFiles(files));
    }

    // ==================== Scrolling ====================

    public void scrollIntoViewIfNeeded() {
        executeWithHealing(() -> delegate.scrollIntoViewIfNeeded());
    }

    // ==================== State Queries ====================

    public boolean isVisible() {
        return executeWithHealingReturn(() -> delegate.isVisible());
    }

    public boolean isHidden() {
        return executeWithHealingReturn(() -> delegate.isHidden());
    }

    public boolean isEnabled() {
        return executeWithHealingReturn(() -> delegate.isEnabled());
    }

    public boolean isDisabled() {
        return executeWithHealingReturn(() -> delegate.isDisabled());
    }

    public boolean isChecked() {
        return executeWithHealingReturn(() -> delegate.isChecked());
    }

    public boolean isEditable() {
        return executeWithHealingReturn(() -> delegate.isEditable());
    }

    // ==================== Content Queries ====================

    public String textContent() {
        return executeWithHealingReturn(() -> delegate.textContent());
    }

    public String innerText() {
        return executeWithHealingReturn(() -> delegate.innerText());
    }

    public String innerHTML() {
        return executeWithHealingReturn(() -> delegate.innerHTML());
    }

    public String inputValue() {
        return executeWithHealingReturn(() -> delegate.inputValue());
    }

    public String getAttribute(String name) {
        return executeWithHealingReturn(() -> delegate.getAttribute(name));
    }

    // ==================== Count & Presence ====================

    public int count() {
        return delegate.count();
    }

    public Locator first() {
        return delegate.first();
    }

    public Locator last() {
        return delegate.last();
    }

    public Locator nth(int index) {
        return delegate.nth(index);
    }

    public List<Locator> all() {
        return delegate.all();
    }

    // ==================== Filtering ====================

    public HealingLocator filter(Locator.FilterOptions options) {
        return new HealingLocator(delegate.filter(options), originalSelector, page);
    }

    public HealingLocator locator(String selector) {
        return new HealingLocator(delegate.locator(selector), originalSelector + " >> " + selector, page);
    }

    public HealingLocator getByRole(AriaRole role) {
        return new HealingLocator(delegate.getByRole(role), originalSelector + " >> role=" + role, page);
    }

    public HealingLocator getByText(String text) {
        return new HealingLocator(delegate.getByText(text), originalSelector + " >> text=" + text, page);
    }

    public HealingLocator getByTestId(String testId) {
        return new HealingLocator(delegate.getByTestId(testId), originalSelector + " >> data-testid=" + testId, page);
    }

    // ==================== Waiting ====================

    public void waitFor() {
        executeWithHealing(() -> delegate.waitFor());
    }

    public void waitFor(Locator.WaitForOptions options) {
        executeWithHealing(() -> delegate.waitFor(options));
    }

    // ==================== Screenshot ====================

    public byte[] screenshot() {
        return executeWithHealingReturn(() -> delegate.screenshot());
    }

    public byte[] screenshot(Locator.ScreenshotOptions options) {
        return executeWithHealingReturn(() -> delegate.screenshot(options));
    }

    // ==================== Bounding Box ====================

    public BoundingBox boundingBox() {
        return executeWithHealingReturn(() -> delegate.boundingBox());
    }

    // ==================== JavaScript ====================

    public Object evaluate(String expression) {
        return executeWithHealingReturn(() -> delegate.evaluate(expression));
    }

    public Object evaluate(String expression, Object arg) {
        return executeWithHealingReturn(() -> delegate.evaluate(expression, arg));
    }

    public JSHandle evaluateHandle(String expression) {
        return executeWithHealingReturn(() -> delegate.evaluateHandle(expression));
    }

    // ==================== Healing Execution ====================

    /**
     * Execute an action with automatic healing on failure.
     */
    private void executeWithHealing(Runnable action) {
        try {
            action.run();
        } catch (PlaywrightException e) {
            Locator healed = attemptHeal(e);
            if (healed != null) {
                this.delegate = healed;
                action.run();
            } else {
                throw e;
            }
        }
    }

    /**
     * Execute an action that returns a value with automatic healing on failure.
     */
    private <T> T executeWithHealingReturn(java.util.function.Supplier<T> action) {
        try {
            return action.get();
        } catch (PlaywrightException e) {
            Locator healed = attemptHeal(e);
            if (healed != null) {
                this.delegate = healed;
                return action.get();
            } else {
                throw e;
            }
        }
    }

    /**
     * Attempt to heal the locator.
     */
    private Locator attemptHeal(RuntimeException e) {
        logger.debug("Locator failed: {}, attempting heal for: {}", e.getMessage(), originalSelector);
        return page.attemptHeal(originalSelector, e);
    }

    @Override
    public String toString() {
        return "HealingLocator{selector='" + originalSelector + "'}";
    }
}

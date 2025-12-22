package io.github.glaciousm.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import io.github.glaciousm.core.config.HealerConfig;
import io.github.glaciousm.core.engine.HealingEngine;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HealingLocator.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HealingLocator")
class HealingLocatorTest {

    @Mock
    private Page mockPage;

    @Mock
    private Locator mockLocator;

    @Mock
    private HealingEngine mockEngine;

    private HealerConfig config;
    private HealingPage healingPage;
    private HealingLocator healingLocator;

    @BeforeEach
    void setUp() {
        config = new HealerConfig();
        config.setEnabled(true);
        config.applyDefaults();
        healingPage = new HealingPage(mockPage, mockEngine, config);
        healingLocator = new HealingLocator(mockLocator, "#test-btn", healingPage);
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create instance with selector")
        void createsInstance() {
            assertThat(healingLocator).isNotNull();
            assertThat(healingLocator.getOriginalSelector()).isEqualTo("#test-btn");
            assertThat(healingLocator.getDelegate()).isSameAs(mockLocator);
        }

        @Test
        @DisplayName("should have proper toString")
        void hasProperToString() {
            assertThat(healingLocator.toString()).contains("#test-btn");
        }
    }

    @Nested
    @DisplayName("Click Actions")
    class ClickActionsTests {

        @Test
        @DisplayName("should delegate click")
        void delegatesClick() {
            healingLocator.click();
            verify(mockLocator).click();
        }

        @Test
        @DisplayName("should delegate dblclick")
        void delegatesDblclick() {
            healingLocator.dblclick();
            verify(mockLocator).dblclick();
        }
    }

    @Nested
    @DisplayName("Input Actions")
    class InputActionsTests {

        @Test
        @DisplayName("should delegate fill")
        void delegatesFill() {
            healingLocator.fill("test value");
            verify(mockLocator).fill("test value");
        }

        @Test
        @DisplayName("should delegate type")
        void delegatesType() {
            healingLocator.type("typing");
            verify(mockLocator).type("typing");
        }

        @Test
        @DisplayName("should delegate press")
        void delegatesPress() {
            healingLocator.press("Enter");
            verify(mockLocator).press("Enter");
        }

        @Test
        @DisplayName("should delegate clear")
        void delegatesClear() {
            healingLocator.clear();
            verify(mockLocator).clear();
        }
    }

    @Nested
    @DisplayName("Selection Actions")
    class SelectionActionsTests {

        @Test
        @DisplayName("should delegate selectOption")
        void delegatesSelectOption() {
            when(mockLocator.selectOption("value1")).thenReturn(List.of("value1"));

            List<String> result = healingLocator.selectOption("value1");

            assertThat(result).containsExactly("value1");
        }

        @Test
        @DisplayName("should delegate check")
        void delegatesCheck() {
            healingLocator.check();
            verify(mockLocator).check();
        }

        @Test
        @DisplayName("should delegate uncheck")
        void delegatesUncheck() {
            healingLocator.uncheck();
            verify(mockLocator).uncheck();
        }

        @Test
        @DisplayName("should delegate setChecked")
        void delegatesSetChecked() {
            healingLocator.setChecked(true);
            verify(mockLocator).setChecked(true);
        }
    }

    @Nested
    @DisplayName("Hover and Focus")
    class HoverFocusTests {

        @Test
        @DisplayName("should delegate hover")
        void delegatesHover() {
            healingLocator.hover();
            verify(mockLocator).hover();
        }

        @Test
        @DisplayName("should delegate focus")
        void delegatesFocus() {
            healingLocator.focus();
            verify(mockLocator).focus();
        }

        @Test
        @DisplayName("should delegate blur")
        void delegatesBlur() {
            healingLocator.blur();
            verify(mockLocator).blur();
        }
    }

    @Nested
    @DisplayName("State Queries")
    class StateQueriesTests {

        @Test
        @DisplayName("should return isVisible")
        void returnsIsVisible() {
            when(mockLocator.isVisible()).thenReturn(true);

            assertThat(healingLocator.isVisible()).isTrue();
        }

        @Test
        @DisplayName("should return isHidden")
        void returnsIsHidden() {
            when(mockLocator.isHidden()).thenReturn(false);

            assertThat(healingLocator.isHidden()).isFalse();
        }

        @Test
        @DisplayName("should return isEnabled")
        void returnsIsEnabled() {
            when(mockLocator.isEnabled()).thenReturn(true);

            assertThat(healingLocator.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should return isDisabled")
        void returnsIsDisabled() {
            when(mockLocator.isDisabled()).thenReturn(false);

            assertThat(healingLocator.isDisabled()).isFalse();
        }

        @Test
        @DisplayName("should return isChecked")
        void returnsIsChecked() {
            when(mockLocator.isChecked()).thenReturn(true);

            assertThat(healingLocator.isChecked()).isTrue();
        }

        @Test
        @DisplayName("should return isEditable")
        void returnsIsEditable() {
            when(mockLocator.isEditable()).thenReturn(true);

            assertThat(healingLocator.isEditable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Content Queries")
    class ContentQueriesTests {

        @Test
        @DisplayName("should return textContent")
        void returnsTextContent() {
            when(mockLocator.textContent()).thenReturn("Button Text");

            assertThat(healingLocator.textContent()).isEqualTo("Button Text");
        }

        @Test
        @DisplayName("should return innerText")
        void returnsInnerText() {
            when(mockLocator.innerText()).thenReturn("Inner Text");

            assertThat(healingLocator.innerText()).isEqualTo("Inner Text");
        }

        @Test
        @DisplayName("should return innerHTML")
        void returnsInnerHtml() {
            when(mockLocator.innerHTML()).thenReturn("<span>Content</span>");

            assertThat(healingLocator.innerHTML()).contains("<span>");
        }

        @Test
        @DisplayName("should return inputValue")
        void returnsInputValue() {
            when(mockLocator.inputValue()).thenReturn("input text");

            assertThat(healingLocator.inputValue()).isEqualTo("input text");
        }

        @Test
        @DisplayName("should return getAttribute")
        void returnsGetAttribute() {
            when(mockLocator.getAttribute("data-id")).thenReturn("123");

            assertThat(healingLocator.getAttribute("data-id")).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Count and Presence")
    class CountPresenceTests {

        @Test
        @DisplayName("should return count")
        void returnsCount() {
            when(mockLocator.count()).thenReturn(3);

            assertThat(healingLocator.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return first")
        void returnsFirst() {
            Locator firstLocator = mock(Locator.class);
            when(mockLocator.first()).thenReturn(firstLocator);

            assertThat(healingLocator.first()).isSameAs(firstLocator);
        }

        @Test
        @DisplayName("should return last")
        void returnsLast() {
            Locator lastLocator = mock(Locator.class);
            when(mockLocator.last()).thenReturn(lastLocator);

            assertThat(healingLocator.last()).isSameAs(lastLocator);
        }

        @Test
        @DisplayName("should return nth")
        void returnsNth() {
            Locator nthLocator = mock(Locator.class);
            when(mockLocator.nth(2)).thenReturn(nthLocator);

            assertThat(healingLocator.nth(2)).isSameAs(nthLocator);
        }
    }

    @Nested
    @DisplayName("Filtering")
    class FilteringTests {

        @Test
        @DisplayName("should return HealingLocator from locator")
        void returnsHealingLocatorFromLocator() {
            Locator childLocator = mock(Locator.class);
            when(mockLocator.locator(".child")).thenReturn(childLocator);

            HealingLocator child = healingLocator.locator(".child");

            assertThat(child).isNotNull();
            assertThat(child.getDelegate()).isSameAs(childLocator);
        }

        @Test
        @DisplayName("should return HealingLocator from getByRole")
        void returnsHealingLocatorFromGetByRole() {
            Locator roleLocator = mock(Locator.class);
            when(mockLocator.getByRole(AriaRole.BUTTON)).thenReturn(roleLocator);

            HealingLocator child = healingLocator.getByRole(AriaRole.BUTTON);

            assertThat(child).isNotNull();
        }

        @Test
        @DisplayName("should return HealingLocator from getByText")
        void returnsHealingLocatorFromGetByText() {
            Locator textLocator = mock(Locator.class);
            when(mockLocator.getByText("Submit")).thenReturn(textLocator);

            HealingLocator child = healingLocator.getByText("Submit");

            assertThat(child).isNotNull();
        }

        @Test
        @DisplayName("should return HealingLocator from getByTestId")
        void returnsHealingLocatorFromGetByTestId() {
            Locator testIdLocator = mock(Locator.class);
            when(mockLocator.getByTestId("my-id")).thenReturn(testIdLocator);

            HealingLocator child = healingLocator.getByTestId("my-id");

            assertThat(child).isNotNull();
        }
    }

    @Nested
    @DisplayName("Waiting")
    class WaitingTests {

        @Test
        @DisplayName("should delegate waitFor")
        void delegatesWaitFor() {
            healingLocator.waitFor();
            verify(mockLocator).waitFor();
        }
    }

    @Nested
    @DisplayName("Scrolling")
    class ScrollingTests {

        @Test
        @DisplayName("should delegate scrollIntoViewIfNeeded")
        void delegatesScrollIntoViewIfNeeded() {
            healingLocator.scrollIntoViewIfNeeded();
            verify(mockLocator).scrollIntoViewIfNeeded();
        }
    }

    @Nested
    @DisplayName("Bounding Box")
    class BoundingBoxTests {

        @Test
        @DisplayName("should return boundingBox")
        void returnsBoundingBox() {
            BoundingBox box = new BoundingBox();
            box.x = 10;
            box.y = 20;
            box.width = 100;
            box.height = 50;
            when(mockLocator.boundingBox()).thenReturn(box);

            BoundingBox result = healingLocator.boundingBox();

            assertThat(result.x).isEqualTo(10);
            assertThat(result.y).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("JavaScript")
    class JavaScriptTests {

        @Test
        @DisplayName("should delegate evaluate")
        void delegatesEvaluate() {
            when(mockLocator.evaluate("el => el.id")).thenReturn("my-id");

            Object result = healingLocator.evaluate("el => el.id");

            assertThat(result).isEqualTo("my-id");
        }
    }
}

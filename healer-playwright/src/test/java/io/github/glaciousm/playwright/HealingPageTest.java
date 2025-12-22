package io.github.glaciousm.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import io.github.glaciousm.core.config.HealerConfig;
import io.github.glaciousm.core.engine.HealingEngine;
import io.github.glaciousm.core.model.IntentContract;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HealingPage.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HealingPage")
class HealingPageTest {

    @Mock
    private Page mockPage;

    @Mock
    private Locator mockLocator;

    @Mock
    private HealingEngine mockEngine;

    private HealerConfig config;
    private HealingPage healingPage;

    @BeforeEach
    void setUp() {
        config = new HealerConfig();
        config.setEnabled(true);
        config.applyDefaults();
        healingPage = new HealingPage(mockPage, mockEngine, config);
    }

    @AfterEach
    void tearDown() {
        healingPage.cleanupThreadResources();
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create instance with valid parameters")
        void createsInstance() {
            assertThat(healingPage).isNotNull();
            assertThat(healingPage.getDelegate()).isSameAs(mockPage);
        }
    }

    @Nested
    @DisplayName("Intent Context")
    class IntentContextTests {

        @Test
        @DisplayName("should set and clear intent context")
        void setsAndClearsIntentContext() {
            IntentContract intent = IntentContract.defaultContract("click button");

            healingPage.setCurrentIntent(intent, "Click the submit button");
            // Intent is stored in ThreadLocal, so we just verify no exception
            healingPage.clearCurrentIntent();
            // After clear, intent should be null (tested implicitly by no exception)
        }

        @Test
        @DisplayName("should cleanup thread resources")
        void cleansUpThreadResources() {
            healingPage.setCurrentIntent(IntentContract.defaultContract("test"), "test step");
            healingPage.cleanupThreadResources();
            // No exception means success
        }
    }

    @Nested
    @DisplayName("Locator Creation")
    class LocatorCreationTests {

        @Test
        @DisplayName("should create HealingLocator from selector")
        void createsHealingLocator() {
            when(mockPage.locator("#submit-btn")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.locator("#submit-btn");

            assertThat(locator).isNotNull();
            assertThat(locator.getOriginalSelector()).isEqualTo("#submit-btn");
        }

        @Test
        @DisplayName("should create HealingLocator from getByRole")
        void createsHealingLocatorByRole() {
            when(mockPage.getByRole(AriaRole.BUTTON)).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByRole(AriaRole.BUTTON);

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByText")
        void createsHealingLocatorByText() {
            when(mockPage.getByText("Submit")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByText("Submit");

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByLabel")
        void createsHealingLocatorByLabel() {
            when(mockPage.getByLabel("Username")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByLabel("Username");

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByPlaceholder")
        void createsHealingLocatorByPlaceholder() {
            when(mockPage.getByPlaceholder("Enter email")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByPlaceholder("Enter email");

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByTestId")
        void createsHealingLocatorByTestId() {
            when(mockPage.getByTestId("submit-button")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByTestId("submit-button");

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByAltText")
        void createsHealingLocatorByAltText() {
            when(mockPage.getByAltText("Logo")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByAltText("Logo");

            assertThat(locator).isNotNull();
        }

        @Test
        @DisplayName("should create HealingLocator from getByTitle")
        void createsHealingLocatorByTitle() {
            when(mockPage.getByTitle("Help")).thenReturn(mockLocator);

            HealingLocator locator = healingPage.getByTitle("Help");

            assertThat(locator).isNotNull();
        }
    }

    @Nested
    @DisplayName("Page Navigation")
    class PageNavigationTests {

        @Test
        @DisplayName("should delegate navigate")
        void delegatesNavigate() {
            healingPage.navigate("https://example.com");
            verify(mockPage).navigate("https://example.com");
        }

        @Test
        @DisplayName("should delegate reload")
        void delegatesReload() {
            healingPage.reload();
            verify(mockPage).reload();
        }

        @Test
        @DisplayName("should delegate goBack")
        void delegatesGoBack() {
            healingPage.goBack();
            verify(mockPage).goBack();
        }

        @Test
        @DisplayName("should delegate goForward")
        void delegatesGoForward() {
            healingPage.goForward();
            verify(mockPage).goForward();
        }
    }

    @Nested
    @DisplayName("Page Properties")
    class PagePropertiesTests {

        @Test
        @DisplayName("should return page URL")
        void returnsUrl() {
            when(mockPage.url()).thenReturn("https://example.com");

            assertThat(healingPage.url()).isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("should return page title")
        void returnsTitle() {
            when(mockPage.title()).thenReturn("Example Page");

            assertThat(healingPage.title()).isEqualTo("Example Page");
        }

        @Test
        @DisplayName("should return page content")
        void returnsContent() {
            when(mockPage.content()).thenReturn("<html><body>Test</body></html>");

            assertThat(healingPage.content()).contains("<html>");
        }
    }

    @Nested
    @DisplayName("Waiting")
    class WaitingTests {

        @Test
        @DisplayName("should delegate waitForTimeout")
        void delegatesWaitForTimeout() {
            healingPage.waitForTimeout(1000);
            verify(mockPage).waitForTimeout(1000);
        }

        @Test
        @DisplayName("should delegate waitForLoadState")
        void delegatesWaitForLoadState() {
            healingPage.waitForLoadState();
            verify(mockPage).waitForLoadState();
        }

        @Test
        @DisplayName("should delegate waitForURL")
        void delegatesWaitForUrl() {
            healingPage.waitForURL("https://example.com");
            verify(mockPage).waitForURL("https://example.com");
        }
    }

    @Nested
    @DisplayName("Keyboard and Mouse")
    class KeyboardMouseTests {

        @Mock
        private Keyboard mockKeyboard;

        @Mock
        private Mouse mockMouse;

        @Test
        @DisplayName("should return keyboard")
        void returnsKeyboard() {
            when(mockPage.keyboard()).thenReturn(mockKeyboard);

            assertThat(healingPage.keyboard()).isSameAs(mockKeyboard);
        }

        @Test
        @DisplayName("should return mouse")
        void returnsMouse() {
            when(mockPage.mouse()).thenReturn(mockMouse);

            assertThat(healingPage.mouse()).isSameAs(mockMouse);
        }
    }

    @Nested
    @DisplayName("Page Lifecycle")
    class PageLifecycleTests {

        @Test
        @DisplayName("should delegate close")
        void delegatesClose() {
            healingPage.close();
            verify(mockPage).close();
        }

        @Test
        @DisplayName("should delegate isClosed")
        void delegatesIsClosed() {
            when(mockPage.isClosed()).thenReturn(false);

            assertThat(healingPage.isClosed()).isFalse();
        }

        @Test
        @DisplayName("should delegate bringToFront")
        void delegatesBringToFront() {
            healingPage.bringToFront();
            verify(mockPage).bringToFront();
        }
    }
}

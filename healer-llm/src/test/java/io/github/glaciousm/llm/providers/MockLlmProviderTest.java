package io.github.glaciousm.llm.providers;

import io.github.glaciousm.core.config.LlmConfig;
import io.github.glaciousm.core.model.*;
import io.github.glaciousm.llm.LlmRequest;
import io.github.glaciousm.llm.LlmResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MockLlmProvider.
 */
@DisplayName("MockLlmProvider")
@ExtendWith(MockitoExtension.class)
class MockLlmProviderTest {

    private MockLlmProvider provider;

    @Mock
    private LlmConfig config;

    @BeforeEach
    void setUp() {
        provider = new MockLlmProvider();
    }

    @Nested
    @DisplayName("Provider Configuration")
    class ProviderConfigTests {

        @Test
        @DisplayName("getProviderName should return 'mock'")
        void getProviderNameShouldReturnMock() {
            assertThat(provider.getProviderName()).isEqualTo("mock");
        }

        @Test
        @DisplayName("isAvailable should always return true")
        void isAvailableShouldAlwaysReturnTrue() {
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("should return successful response with mock content")
        void shouldReturnSuccessfulResponse() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Test prompt")
                    .build();

            LlmResponse response = provider.complete(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getContent()).contains("Mock response");
            assertThat(response.getPromptTokens()).isEqualTo(100);
            assertThat(response.getCompletionTokens()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("evaluateCandidates()")
    class EvaluateCandidatesTests {

        @Test
        @DisplayName("should return cannotHeal when no elements available")
        void shouldReturnCannotHealWhenNoElements() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Click login button")
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Test Page")
                    .interactiveElements(List.of())
                    .build();

            IntentContract intent = IntentContract.defaultContract("Click login button");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isFalse();
            assertThat(decision.getRefusalReason()).contains("No candidate elements found");
        }

        @Test
        @DisplayName("should select button element for click action")
        void shouldSelectButtonForClickAction() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Click login button")
                    .originalLocator(new LocatorInfo("id", "login-btn"))
                    .build();

            ElementSnapshot button = ElementSnapshot.builder()
                    .index(0)
                    .tagName("button")
                    .type("submit")
                    .text("Login")
                    .classes(List.of("btn", "btn-primary"))
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Login Page")
                    .interactiveElements(List.of(button))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Click login button");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
            assertThat(decision.getConfidence()).isGreaterThan(0.75);
        }

        @Test
        @DisplayName("should prefer visible elements over hidden")
        void shouldPreferVisibleElements() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Click submit")
                    .originalLocator(new LocatorInfo("css", ".submit-btn"))
                    .build();

            ElementSnapshot hiddenButton = ElementSnapshot.builder()
                    .index(0)
                    .tagName("button")
                    .text("Submit")
                    .visible(false)
                    .enabled(true)
                    .build();

            ElementSnapshot visibleButton = ElementSnapshot.builder()
                    .index(1)
                    .tagName("button")
                    .text("Submit")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Form Page")
                    .interactiveElements(List.of(hiddenButton, visibleButton))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Click submit");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("should select input for enter text action")
        void shouldSelectInputForEnterTextAction() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Enter username")
                    .originalLocator(new LocatorInfo("id", "user"))
                    .build();

            ElementSnapshot input = ElementSnapshot.builder()
                    .index(0)
                    .tagName("input")
                    .type("text")
                    .name("username")
                    .placeholder("Enter username")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Login Page")
                    .interactiveElements(List.of(input))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Enter username");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("should select checkbox for checkbox locator")
        void shouldSelectCheckboxForCheckboxLocator() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Check the checkbox")
                    .originalLocator(new LocatorInfo("id", "checkbox-1"))
                    .build();

            ElementSnapshot checkbox = ElementSnapshot.builder()
                    .index(0)
                    .tagName("input")
                    .type("checkbox")
                    .name("agree")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Form Page")
                    .interactiveElements(List.of(checkbox))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Check the checkbox");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
            assertThat(decision.getConfidence()).isGreaterThan(0.75);
        }

        @Test
        @DisplayName("should select dropdown for select action")
        void shouldSelectDropdownForSelectAction() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Select from dropdown")
                    .originalLocator(new LocatorInfo("id", "dropdown-menu"))
                    .build();

            ElementSnapshot select = ElementSnapshot.builder()
                    .index(0)
                    .tagName("select")
                    .name("country")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Form Page")
                    .interactiveElements(List.of(select))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Select from dropdown");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return cannotHeal when no suitable element found")
        void shouldReturnCannotHealWhenNoSuitableElement() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Click special element")
                    .originalLocator(new LocatorInfo("id", "special"))
                    .build();

            // Element with very low score
            ElementSnapshot div = ElementSnapshot.builder()
                    .index(0)
                    .tagName("div")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(div))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Click special element");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isFalse();
            assertThat(decision.getRefusalReason()).contains("No suitable element found");
        }

        @Test
        @DisplayName("should handle null step text")
        void shouldHandleNullStepText() {
            FailureContext failure = FailureContext.builder()
                    .stepText("")
                    .originalLocator(null)
                    .build();

            ElementSnapshot button = ElementSnapshot.builder()
                    .index(0)
                    .tagName("button")
                    .text("Submit")
                    .classes(List.of("btn"))
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Page")
                    .interactiveElements(List.of(button))
                    .build();

            IntentContract intent = IntentContract.defaultContract("");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            // Should still make a decision based on element attributes
            assertThat(decision).isNotNull();
        }

        @Test
        @DisplayName("should select link with action text")
        void shouldSelectLinkWithActionText() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Click Add Element")
                    .originalLocator(new LocatorInfo("linkText", "Add Element"))
                    .build();

            ElementSnapshot link = ElementSnapshot.builder()
                    .index(0)
                    .tagName("a")
                    .text("Add Element")
                    .classes(List.of("button", "added-manually"))
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Dynamic Page")
                    .interactiveElements(List.of(link))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Click Add Element");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("should select number input for number locator")
        void shouldSelectNumberInputForNumberLocator() {
            FailureContext failure = FailureContext.builder()
                    .stepText("Enter number")
                    .originalLocator(new LocatorInfo("id", "number-input"))
                    .build();

            ElementSnapshot numberInput = ElementSnapshot.builder()
                    .index(0)
                    .tagName("input")
                    .type("number")
                    .name("quantity")
                    .visible(true)
                    .enabled(true)
                    .build();

            UiSnapshot snapshot = UiSnapshot.builder()
                    .url("https://example.com")
                    .title("Order Form")
                    .interactiveElements(List.of(numberInput))
                    .build();

            IntentContract intent = IntentContract.defaultContract("Enter number");

            HealDecision decision = provider.evaluateCandidates(failure, snapshot, intent, config);

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("validateOutcome()")
    class ValidateOutcomeTests {

        @Test
        @DisplayName("should pass when URL changes for navigation outcome")
        void shouldPassWhenUrlChangesForNavigation() {
            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/login")
                    .title("Login")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/dashboard")
                    .title("Dashboard")
                    .interactiveElements(List.of())
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "User should navigate to dashboard page",
                    before, after, config);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getConfidence()).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("should pass when reaching secure area")
        void shouldPassWhenReachingSecureArea() {
            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/login")
                    .title("Login")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/secure/home")
                    .title("Secure Area")
                    .interactiveElements(List.of())
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "User should see secure dashboard",
                    before, after, config);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getReasoning()).contains("Secure area");
        }

        @Test
        @DisplayName("should pass when page state changes")
        void shouldPassWhenPageStateChanges() {
            ElementSnapshot element = ElementSnapshot.builder()
                    .index(0)
                    .tagName("button")
                    .build();

            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/page")
                    .title("Page")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/page")
                    .title("Page")
                    .interactiveElements(List.of(element))
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "Element should appear",
                    before, after, config);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("should fail when no expected change detected")
        void shouldFailWhenNoChangeDetected() {
            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/page")
                    .title("Page")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/page")
                    .title("Page")
                    .interactiveElements(List.of())
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "Something should change",
                    before, after, config);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getReasoning()).contains("Expected outcome not detected");
        }

        @Test
        @DisplayName("should detect success keywords in URL")
        void shouldDetectSuccessKeywordsInUrl() {
            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/checkout")
                    .title("Checkout")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/checkout/success")
                    .title("Order Complete")
                    .interactiveElements(List.of())
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "Order should complete successfully",
                    before, after, config);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("should pass for logged in outcome when on secure page")
        void shouldPassForLoggedInOutcome() {
            UiSnapshot before = UiSnapshot.builder()
                    .url("https://example.com/login")
                    .title("Login")
                    .interactiveElements(List.of())
                    .build();

            UiSnapshot after = UiSnapshot.builder()
                    .url("https://example.com/secure/dashboard")
                    .title("Secure Dashboard")
                    .interactiveElements(List.of())
                    .build();

            OutcomeResult result = provider.validateOutcome(
                    "User should be logged in",
                    before, after, config);

            assertThat(result.isPassed()).isTrue();
        }
    }
}

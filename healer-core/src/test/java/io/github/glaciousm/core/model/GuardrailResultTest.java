package io.github.glaciousm.core.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GuardrailResult model class.
 */
@DisplayName("GuardrailResult")
class GuardrailResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("proceed() should create proceeding result")
        void proceedShouldCreateProceedingResult() {
            GuardrailResult result = GuardrailResult.proceed();

            assertThat(result.canProceed()).isTrue();
            assertThat(result.isRefused()).isFalse();
            assertThat(result.getReason()).isNull();
            assertThat(result.getType()).isNull();
        }

        @Test
        @DisplayName("refuse(reason) should create refused result")
        void refuseShouldCreateRefusedResult() {
            GuardrailResult result = GuardrailResult.refuse("Forbidden URL pattern");

            assertThat(result.canProceed()).isFalse();
            assertThat(result.isRefused()).isTrue();
            assertThat(result.getReason()).isEqualTo("Forbidden URL pattern");
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.GENERAL);
        }

        @Test
        @DisplayName("refuse(format, args) should create formatted refused result")
        void refuseShouldCreateFormattedRefusedResult() {
            GuardrailResult result = GuardrailResult.refuse("Max heals reached: %d", 10);

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getReason()).isEqualTo("Max heals reached: 10");
        }

        @Test
        @DisplayName("refuse(type, reason) should create refused result with type")
        void refuseShouldCreateRefusedResultWithType() {
            GuardrailResult result = GuardrailResult.refuse(
                    GuardrailResult.GuardrailType.DESTRUCTIVE_ACTION,
                    "Delete action blocked");

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.DESTRUCTIVE_ACTION);
            assertThat(result.getReason()).isEqualTo("Delete action blocked");
        }

        @Test
        @DisplayName("lowConfidence should create low confidence refusal")
        void lowConfidenceShouldCreateLowConfidenceRefusal() {
            GuardrailResult result = GuardrailResult.lowConfidence(0.45, 0.6);

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.LOW_CONFIDENCE);
            assertThat(result.getReason()).containsIgnoringCase("confidence");
            assertThat(result.getReason()).containsIgnoringCase("threshold");
        }

        @Test
        @DisplayName("destructiveAction should create destructive action refusal")
        void destructiveActionShouldCreateDestructiveActionRefusal() {
            GuardrailResult result = GuardrailResult.destructiveAction("delete");

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.DESTRUCTIVE_ACTION);
            assertThat(result.getReason()).contains("delete");
        }

        @Test
        @DisplayName("forbiddenKeyword should create forbidden keyword refusal")
        void forbiddenKeywordShouldCreateForbiddenKeywordRefusal() {
            GuardrailResult result = GuardrailResult.forbiddenKeyword("admin");

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.FORBIDDEN_KEYWORD);
            assertThat(result.getReason()).contains("admin");
        }

        @Test
        @DisplayName("assertionStep should create assertion step refusal")
        void assertionStepShouldCreateAssertionStepRefusal() {
            GuardrailResult result = GuardrailResult.assertionStep();

            assertThat(result.isRefused()).isTrue();
            assertThat(result.getType()).isEqualTo(GuardrailResult.GuardrailType.ASSERTION_STEP);
            assertThat(result.getReason()).contains("Assertion");
        }
    }

    @Nested
    @DisplayName("GuardrailType enum")
    class GuardrailTypeTests {

        @Test
        @DisplayName("should have all required types")
        void shouldHaveAllRequiredTypes() {
            GuardrailResult.GuardrailType[] types = GuardrailResult.GuardrailType.values();

            assertThat(types).contains(GuardrailResult.GuardrailType.GENERAL);
            assertThat(types).contains(GuardrailResult.GuardrailType.LOW_CONFIDENCE);
            assertThat(types).contains(GuardrailResult.GuardrailType.DESTRUCTIVE_ACTION);
            assertThat(types).contains(GuardrailResult.GuardrailType.FORBIDDEN_KEYWORD);
            assertThat(types).contains(GuardrailResult.GuardrailType.ASSERTION_STEP);
            assertThat(types).contains(GuardrailResult.GuardrailType.POLICY_OFF);
            assertThat(types).contains(GuardrailResult.GuardrailType.NOT_INTERACTABLE);
            assertThat(types).contains(GuardrailResult.GuardrailType.COST_LIMIT);
            assertThat(types).contains(GuardrailResult.GuardrailType.CIRCUIT_BREAKER);
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("isRefused should be opposite of canProceed")
        void isRefusedShouldBeOppositeOfCanProceed() {
            GuardrailResult proceed = GuardrailResult.proceed();
            GuardrailResult refused = GuardrailResult.refuse("reason");

            assertThat(proceed.isRefused()).isNotEqualTo(proceed.canProceed());
            assertThat(refused.isRefused()).isNotEqualTo(refused.canProceed());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal results should be equal")
        void equalResultsShouldBeEqual() {
            GuardrailResult r1 = GuardrailResult.refuse("reason");
            GuardrailResult r2 = GuardrailResult.refuse("reason");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("different results should not be equal")
        void differentResultsShouldNotBeEqual() {
            GuardrailResult r1 = GuardrailResult.proceed();
            GuardrailResult r2 = GuardrailResult.refuse("blocked");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            GuardrailResult result = GuardrailResult.proceed();
            assertThat(result).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should format proceeding result correctly")
        void shouldFormatProceedingResult() {
            GuardrailResult result = GuardrailResult.proceed();

            String str = result.toString();

            assertThat(str).contains("proceed=true");
        }

        @Test
        @DisplayName("should format refused result correctly")
        void shouldFormatRefusedResult() {
            GuardrailResult result = GuardrailResult.refuse("Test reason");

            String str = result.toString();

            assertThat(str).contains("proceed=false");
            assertThat(str).contains("GENERAL");
            assertThat(str).contains("Test reason");
        }
    }
}

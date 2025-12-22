package io.github.glaciousm.core.model;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HealDecision model class.
 */
@DisplayName("HealDecision")
class HealDecisionTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("canHeal() should create healable decision")
        void canHealShouldCreateHealableDecision() {
            HealDecision decision = HealDecision.canHeal(5, 0.85, "Found matching button");

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getSelectedElementIndex()).isEqualTo(5);
            assertThat(decision.getConfidence()).isEqualTo(0.85);
            assertThat(decision.getReasoning()).isEqualTo("Found matching button");
            assertThat(decision.getRefusalReason()).isNull();
            assertThat(decision.getAlternativeIndices()).isEmpty();
            assertThat(decision.getWarnings()).isEmpty();
        }

        @Test
        @DisplayName("cannotHeal() should create non-healable decision with reason")
        void cannotHealShouldCreateNonHealableDecision() {
            HealDecision decision = HealDecision.cannotHeal("Element was removed from DOM");

            assertThat(decision.canHeal()).isFalse();
            assertThat(decision.getSelectedElementIndex()).isNull();
            assertThat(decision.getConfidence()).isZero();
            assertThat(decision.getReasoning()).isNull();
            assertThat(decision.getRefusalReason()).isEqualTo("Element was removed from DOM");
        }

        @Test
        @DisplayName("lowConfidence() should create decision with low confidence")
        void lowConfidenceShouldCreateDecisionWithLowConfidence() {
            HealDecision decision = HealDecision.lowConfidence(0.35, "Multiple similar elements found");

            assertThat(decision.canHeal()).isFalse();
            assertThat(decision.getConfidence()).isEqualTo(0.35);
            assertThat(decision.getReasoning()).isEqualTo("Multiple similar elements found");
            assertThat(decision.getRefusalReason()).contains("Confidence");
            assertThat(decision.getRefusalReason()).contains("below threshold");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should create decision with all fields")
        void shouldCreateDecisionWithAllFields() {
            HealDecision decision = HealDecision.builder()
                    .canHeal(true)
                    .confidence(0.92)
                    .selectedElementIndex(3)
                    .reasoning("Best match based on text content")
                    .alternativeIndices(List.of(5, 7))
                    .warnings(List.of("Element text is similar to another"))
                    .build();

            assertThat(decision.canHeal()).isTrue();
            assertThat(decision.getConfidence()).isEqualTo(0.92);
            assertThat(decision.getSelectedElementIndex()).isEqualTo(3);
            assertThat(decision.getReasoning()).isEqualTo("Best match based on text content");
            assertThat(decision.getAlternativeIndices()).containsExactly(5, 7);
            assertThat(decision.getWarnings()).containsExactly("Element text is similar to another");
        }

        @Test
        @DisplayName("should handle null alternative indices")
        void shouldHandleNullAlternativeIndices() {
            HealDecision decision = HealDecision.builder()
                    .canHeal(true)
                    .confidence(0.8)
                    .selectedElementIndex(0)
                    .alternativeIndices(null)
                    .build();

            assertThat(decision.getAlternativeIndices()).isEmpty();
        }

        @Test
        @DisplayName("should handle null warnings")
        void shouldHandleNullWarnings() {
            HealDecision decision = HealDecision.builder()
                    .canHeal(true)
                    .confidence(0.8)
                    .selectedElementIndex(0)
                    .warnings(null)
                    .build();

            assertThat(decision.getWarnings()).isEmpty();
        }

        @Test
        @DisplayName("should set refusal reason")
        void shouldSetRefusalReason() {
            HealDecision decision = HealDecision.builder()
                    .canHeal(false)
                    .refusalReason("Page changed unexpectedly")
                    .build();

            assertThat(decision.canHeal()).isFalse();
            assertThat(decision.getRefusalReason()).isEqualTo("Page changed unexpectedly");
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal decisions should be equal")
        void equalDecisionsShouldBeEqual() {
            HealDecision d1 = HealDecision.canHeal(1, 0.8, "reason");
            HealDecision d2 = HealDecision.canHeal(1, 0.8, "reason");

            assertThat(d1).isEqualTo(d2);
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }

        @Test
        @DisplayName("different decisions should not be equal")
        void differentDecisionsShouldNotBeEqual() {
            HealDecision d1 = HealDecision.canHeal(1, 0.8, "reason1");
            HealDecision d2 = HealDecision.canHeal(2, 0.9, "reason2");

            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            HealDecision decision = HealDecision.canHeal(1, 0.8, "reason");
            assertThat(decision).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            HealDecision decision = HealDecision.canHeal(1, 0.8, "reason");
            assertThat(decision).isNotEqualTo("not a decision");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should format healable decision correctly")
        void shouldFormatHealableDecision() {
            HealDecision decision = HealDecision.canHeal(5, 0.85, "Found element");

            String result = decision.toString();

            // Just verify toString returns meaningful content
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result).contains("HealDecision");
        }

        @Test
        @DisplayName("should format non-healable decision correctly")
        void shouldFormatNonHealableDecision() {
            HealDecision decision = HealDecision.cannotHeal("Cannot find element");

            String result = decision.toString();

            assertThat(result).contains("canHeal=false");
            assertThat(result).contains("Cannot find element");
        }
    }
}

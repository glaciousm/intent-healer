package io.github.glaciousm.core.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OutcomeResult model class.
 */
@DisplayName("OutcomeResult")
class OutcomeResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("passed(message) should create passed result with default confidence")
        void passedShouldCreatePassedResultWithDefaultConfidence() {
            OutcomeResult result = OutcomeResult.passed("Login successful");

            assertThat(result.isPassed()).isTrue();
            assertThat(result.isValid()).isTrue();
            assertThat(result.isFailed()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Login successful");
            assertThat(result.getReasoning()).isEqualTo("Login successful");
            assertThat(result.getConfidence()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("passed(message, confidence) should create passed result with custom confidence")
        void passedShouldCreatePassedResultWithCustomConfidence() {
            OutcomeResult result = OutcomeResult.passed("Page loaded", 0.85);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getConfidence()).isEqualTo(0.85);
        }

        @Test
        @DisplayName("failed(message) should create failed result")
        void failedShouldCreateFailedResult() {
            OutcomeResult result = OutcomeResult.failed("Element not found");

            assertThat(result.isPassed()).isFalse();
            assertThat(result.isValid()).isFalse();
            assertThat(result.isFailed()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Element not found");
            assertThat(result.getConfidence()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("failed(format, args) should create formatted failed result")
        void failedShouldCreateFormattedFailedResult() {
            OutcomeResult result = OutcomeResult.failed("Expected %d elements but found %d", 5, 3);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Expected 5 elements but found 3");
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("getReasoning should return same as getMessage")
        void getReasoningShouldReturnSameAsGetMessage() {
            OutcomeResult result = OutcomeResult.passed("Test message");

            assertThat(result.getReasoning()).isEqualTo(result.getMessage());
        }

        @Test
        @DisplayName("isValid should return same as isPassed")
        void isValidShouldReturnSameAsIsPassed() {
            OutcomeResult passedResult = OutcomeResult.passed("success");
            OutcomeResult failedResult = OutcomeResult.failed("failure");

            assertThat(passedResult.isValid()).isEqualTo(passedResult.isPassed());
            assertThat(failedResult.isValid()).isEqualTo(failedResult.isPassed());
        }

        @Test
        @DisplayName("isFailed should be opposite of isPassed")
        void isFailedShouldBeOppositeOfIsPassed() {
            OutcomeResult passedResult = OutcomeResult.passed("success");
            OutcomeResult failedResult = OutcomeResult.failed("failure");

            assertThat(passedResult.isFailed()).isNotEqualTo(passedResult.isPassed());
            assertThat(failedResult.isFailed()).isNotEqualTo(failedResult.isPassed());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal results should be equal")
        void equalResultsShouldBeEqual() {
            OutcomeResult r1 = OutcomeResult.passed("message");
            OutcomeResult r2 = OutcomeResult.passed("message");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("different results should not be equal")
        void differentResultsShouldNotBeEqual() {
            OutcomeResult r1 = OutcomeResult.passed("success");
            OutcomeResult r2 = OutcomeResult.failed("failure");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            OutcomeResult result = OutcomeResult.passed("message");
            assertThat(result).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should format passed result correctly")
        void shouldFormatPassedResult() {
            OutcomeResult result = OutcomeResult.passed("Operation successful");

            String str = result.toString();

            assertThat(str).contains("passed=true");
            assertThat(str).contains("Operation successful");
        }

        @Test
        @DisplayName("should format failed result correctly")
        void shouldFormatFailedResult() {
            OutcomeResult result = OutcomeResult.failed("Operation failed");

            String str = result.toString();

            assertThat(str).contains("passed=false");
            assertThat(str).contains("Operation failed");
        }
    }
}

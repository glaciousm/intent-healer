package io.github.glaciousm.core.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the exception hierarchy.
 */
@DisplayName("Exception Hierarchy")
class ExceptionHierarchyTest {

    @Nested
    @DisplayName("HealingException")
    class HealingExceptionTests {

        @Test
        @DisplayName("should create with message only")
        void shouldCreateWithMessageOnly() {
            HealingException ex = new HealingException("Test error");

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.UNKNOWN);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should create with message and reason")
        void shouldCreateWithMessageAndReason() {
            HealingException ex = new HealingException(
                    "Low confidence heal",
                    HealingException.HealingFailureReason.LOW_CONFIDENCE);

            assertThat(ex.getMessage()).isEqualTo("Low confidence heal");
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.LOW_CONFIDENCE);
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            HealingException ex = new HealingException("Test error", cause);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.UNKNOWN);
        }

        @Test
        @DisplayName("should create with message, cause and reason")
        void shouldCreateWithMessageCauseAndReason() {
            RuntimeException cause = new RuntimeException("Root cause");
            HealingException ex = new HealingException(
                    "Guardrail violation",
                    cause,
                    HealingException.HealingFailureReason.GUARDRAIL_VIOLATION);

            assertThat(ex.getMessage()).isEqualTo("Guardrail violation");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.GUARDRAIL_VIOLATION);
        }

        @Test
        @DisplayName("should have all failure reasons")
        void shouldHaveAllFailureReasons() {
            HealingException.HealingFailureReason[] reasons = HealingException.HealingFailureReason.values();

            assertThat(reasons).containsExactlyInAnyOrder(
                    HealingException.HealingFailureReason.LLM_UNAVAILABLE,
                    HealingException.HealingFailureReason.LLM_INVALID_RESPONSE,
                    HealingException.HealingFailureReason.GUARDRAIL_VIOLATION,
                    HealingException.HealingFailureReason.NO_CANDIDATES_FOUND,
                    HealingException.HealingFailureReason.LOW_CONFIDENCE,
                    HealingException.HealingFailureReason.OUTCOME_CHECK_FAILED,
                    HealingException.HealingFailureReason.INVARIANT_VIOLATED,
                    HealingException.HealingFailureReason.ACTION_EXECUTION_FAILED,
                    HealingException.HealingFailureReason.ELEMENT_NOT_REFINDABLE,
                    HealingException.HealingFailureReason.CONFIGURATION_ERROR,
                    HealingException.HealingFailureReason.CIRCUIT_BREAKER_OPEN,
                    HealingException.HealingFailureReason.COST_LIMIT_EXCEEDED,
                    HealingException.HealingFailureReason.UNKNOWN
            );
        }
    }

    @Nested
    @DisplayName("LlmException")
    class LlmExceptionTests {

        @Test
        @DisplayName("should create with basic info")
        void shouldCreateWithBasicInfo() {
            LlmException ex = new LlmException("API error", "openai", "gpt-4");

            assertThat(ex.getMessage()).isEqualTo("API error");
            assertThat(ex.getProvider()).isEqualTo("openai");
            assertThat(ex.getModel()).isEqualTo("gpt-4");
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.LLM_UNAVAILABLE);
        }

        @Test
        @DisplayName("should create with cause")
        void shouldCreateWithCause() {
            RuntimeException cause = new RuntimeException("Network failure");
            LlmException ex = new LlmException("Connection failed", cause, "anthropic", "claude-3");

            assertThat(ex.getMessage()).isEqualTo("Connection failed");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getProvider()).isEqualTo("anthropic");
            assertThat(ex.getModel()).isEqualTo("claude-3");
        }

        @Test
        @DisplayName("should create unavailable exception")
        void shouldCreateUnavailableException() {
            RuntimeException cause = new RuntimeException("Server down");
            LlmException ex = LlmException.unavailable("openai", "gpt-4", cause);

            assertThat(ex.getMessage()).contains("unavailable");
            assertThat(ex.getMessage()).contains("openai");
            assertThat(ex.getMessage()).contains("gpt-4");
            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.LLM_UNAVAILABLE);
        }

        @Test
        @DisplayName("should create invalid response exception")
        void shouldCreateInvalidResponseException() {
            LlmException ex = LlmException.invalidResponse("openai", "gpt-4", "JSON parsing failed");

            assertThat(ex.getMessage()).contains("Invalid");
            assertThat(ex.getMessage()).contains("JSON parsing failed");
            assertThat(ex.getProvider()).isEqualTo("openai");
        }

        @Test
        @DisplayName("should create timeout exception")
        void shouldCreateTimeoutException() {
            LlmException ex = LlmException.timeout("openai", "gpt-4", 30);

            assertThat(ex.getMessage()).contains("timed out");
            assertThat(ex.getMessage()).contains("30s");
        }

        @Test
        @DisplayName("should create connection error exception")
        void shouldCreateConnectionErrorException() {
            LlmException ex = LlmException.connectionError("openai", "Connection refused");

            assertThat(ex.getMessage()).contains("Connection error");
            assertThat(ex.getMessage()).contains("Connection refused");
        }

        @Test
        @DisplayName("should create connection error exception with cause")
        void shouldCreateConnectionErrorExceptionWithCause() {
            RuntimeException cause = new RuntimeException("SSL error");
            LlmException ex = LlmException.connectionError("anthropic", cause);

            assertThat(ex.getMessage()).contains("SSL error");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("should create crypto error exception")
        void shouldCreateCryptoErrorException() {
            RuntimeException cause = new RuntimeException("Invalid key");
            LlmException ex = LlmException.cryptoError("bedrock", "Signing request", cause);

            assertThat(ex.getMessage()).contains("Signing request");
            assertThat(ex.getMessage()).contains("bedrock");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("should create rate limited exception")
        void shouldCreateRateLimitedException() {
            LlmException ex = LlmException.rateLimited("openai", "gpt-4");

            assertThat(ex.getMessage()).contains("Rate limited");
            assertThat(ex.isRateLimited()).isTrue();
        }

        @Test
        @DisplayName("should create rate limited exception with retry hint")
        void shouldCreateRateLimitedExceptionWithRetryHint() {
            LlmException ex = LlmException.rateLimited("openai", "gpt-4", 60);

            assertThat(ex.getMessage()).contains("Rate limited");
            assertThat(ex.getMessage()).contains("60 seconds");
            assertThat(ex.isRateLimited()).isTrue();
        }

        @Test
        @DisplayName("should detect rate limiting")
        void shouldDetectRateLimiting() {
            LlmException rateLimited = LlmException.rateLimited("openai", "gpt-4");
            LlmException notRateLimited = new LlmException("Generic error", "openai", "gpt-4");

            assertThat(rateLimited.isRateLimited()).isTrue();
            assertThat(notRateLimited.isRateLimited()).isFalse();
        }

        @Test
        @DisplayName("should have proper toString")
        void shouldHaveProperToString() {
            LlmException ex = new LlmException("Test error", "openai", "gpt-4");

            String str = ex.toString();
            assertThat(str).contains("LlmException");
            assertThat(str).contains("openai");
            assertThat(str).contains("gpt-4");
            assertThat(str).contains("Test error");
        }
    }

    @Nested
    @DisplayName("ConfigurationException")
    class ConfigurationExceptionTests {

        @Test
        @DisplayName("should create with message")
        void shouldCreateWithMessage() {
            ConfigurationException ex = new ConfigurationException("Invalid config");

            assertThat(ex.getMessage()).isEqualTo("Invalid config");
            assertThat(ex).isInstanceOf(HealingException.class);
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Parse error");
            ConfigurationException ex = new ConfigurationException("Invalid YAML", cause);

            assertThat(ex.getMessage()).isEqualTo("Invalid YAML");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("should have CONFIGURATION_ERROR reason")
        void shouldHaveConfigurationErrorReason() {
            ConfigurationException ex = new ConfigurationException("Test");

            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.CONFIGURATION_ERROR);
        }
    }

    @Nested
    @DisplayName("GuardrailViolationException")
    class GuardrailViolationExceptionTests {

        @Test
        @DisplayName("should create with message")
        void shouldCreateWithMessage() {
            GuardrailViolationException ex = new GuardrailViolationException("Forbidden URL");

            assertThat(ex.getMessage()).isEqualTo("Forbidden URL");
            assertThat(ex).isInstanceOf(HealingException.class);
        }

        @Test
        @DisplayName("should have GUARDRAIL_VIOLATION reason")
        void shouldHaveGuardrailViolationReason() {
            GuardrailViolationException ex = new GuardrailViolationException("Test");

            assertThat(ex.getReason()).isEqualTo(HealingException.HealingFailureReason.GUARDRAIL_VIOLATION);
        }
    }

    @Nested
    @DisplayName("JsonSerializationException")
    class JsonSerializationExceptionTests {

        @Test
        @DisplayName("should create with message")
        void shouldCreateWithMessage() {
            JsonSerializationException ex = new JsonSerializationException("Invalid JSON");

            assertThat(ex.getMessage()).isEqualTo("Invalid JSON");
        }

        @Test
        @DisplayName("should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("Unexpected token");
            JsonSerializationException ex = new JsonSerializationException("Parse failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Parse failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy")
    class HierarchyTests {

        @Test
        @DisplayName("all exceptions should extend HealingException")
        void allExceptionsShouldExtendHealingException() {
            assertThat(LlmException.class.getSuperclass()).isEqualTo(HealingException.class);
            assertThat(ConfigurationException.class.getSuperclass()).isEqualTo(HealingException.class);
            assertThat(GuardrailViolationException.class.getSuperclass()).isEqualTo(HealingException.class);
        }

        @Test
        @DisplayName("HealingException should extend RuntimeException")
        void healingExceptionShouldExtendRuntimeException() {
            assertThat(HealingException.class.getSuperclass()).isEqualTo(RuntimeException.class);
        }

        @Test
        @DisplayName("all exceptions should be catchable as HealingException")
        void allExceptionsShouldBeCatchableAsHealingException() {
            try {
                throw new LlmException("Test", "provider", "model");
            } catch (HealingException ex) {
                assertThat(ex).isInstanceOf(LlmException.class);
            }

            try {
                throw new ConfigurationException("Test");
            } catch (HealingException ex) {
                assertThat(ex).isInstanceOf(ConfigurationException.class);
            }

            try {
                throw new GuardrailViolationException("Test");
            } catch (HealingException ex) {
                assertThat(ex).isInstanceOf(GuardrailViolationException.class);
            }
        }
    }
}

package io.github.glaciousm.llm;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LlmResponse.
 */
@DisplayName("LlmResponse")
class LlmResponseTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should create response with all fields")
        void shouldCreateResponseWithAllFields() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .content("Test content")
                    .errorMessage(null)
                    .promptTokens(100)
                    .completionTokens(50)
                    .latencyMs(500L)
                    .model("gpt-4")
                    .build();

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getContent()).contains("Test content");
            assertThat(response.getErrorMessage()).isEmpty();
            assertThat(response.getPromptTokens()).isEqualTo(100);
            assertThat(response.getCompletionTokens()).isEqualTo(50);
            assertThat(response.getLatencyMs()).isEqualTo(500L);
        }

        @Test
        @DisplayName("should create error response")
        void shouldCreateErrorResponse() {
            LlmResponse response = LlmResponse.builder()
                    .success(false)
                    .errorMessage("Connection failed")
                    .build();

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getErrorMessage()).contains("Connection failed");
        }

        @Test
        @DisplayName("should use default values for optional fields")
        void shouldUseDefaultValues() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .build();

            assertThat(response.getPromptTokens()).isZero();
            assertThat(response.getCompletionTokens()).isZero();
            assertThat(response.getLatencyMs()).isZero();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getErrorMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("error() should create failed response with error message")
        void errorShouldCreateFailedResponse() {
            LlmResponse response = LlmResponse.error("API timeout");

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getErrorMessage()).contains("API timeout");
            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("success() should create successful response with content")
        void successShouldCreateSuccessfulResponse() {
            LlmResponse response = LlmResponse.success("Generated content");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getContent()).contains("Generated content");
            assertThat(response.getErrorMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token Calculations")
    class TokenCalculationTests {

        @Test
        @DisplayName("getTotalTokens should return sum of prompt and completion tokens")
        void getTotalTokensShouldReturnSum() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .promptTokens(150)
                    .completionTokens(75)
                    .build();

            assertThat(response.getTotalTokens()).isEqualTo(225);
        }

        @Test
        @DisplayName("getTotalTokens should handle zero tokens")
        void getTotalTokensShouldHandleZeroTokens() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .build();

            assertThat(response.getTotalTokens()).isZero();
        }

        @Test
        @DisplayName("getTotalTokens should handle large token counts")
        void getTotalTokensShouldHandleLargeTokenCounts() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .promptTokens(100000)
                    .completionTokens(50000)
                    .build();

            assertThat(response.getTotalTokens()).isEqualTo(150000);
        }
    }

    @Nested
    @DisplayName("Optional Content")
    class OptionalContentTests {

        @Test
        @DisplayName("getContent should return Optional with value when content is set")
        void getContentShouldReturnOptionalWithValue() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .content("Some content")
                    .build();

            assertThat(response.getContent())
                    .isPresent()
                    .hasValue("Some content");
        }

        @Test
        @DisplayName("getContent should return empty Optional when content is null")
        void getContentShouldReturnEmptyOptional() {
            LlmResponse response = LlmResponse.builder()
                    .success(false)
                    .content(null)
                    .build();

            assertThat(response.getContent()).isEmpty();
        }

        @Test
        @DisplayName("getErrorMessage should return Optional with value when error is set")
        void getErrorMessageShouldReturnOptionalWithValue() {
            LlmResponse response = LlmResponse.builder()
                    .success(false)
                    .errorMessage("Error occurred")
                    .build();

            assertThat(response.getErrorMessage())
                    .isPresent()
                    .hasValue("Error occurred");
        }

        @Test
        @DisplayName("getErrorMessage should return empty Optional when error is null")
        void getErrorMessageShouldReturnEmptyOptional() {
            LlmResponse response = LlmResponse.builder()
                    .success(true)
                    .errorMessage(null)
                    .build();

            assertThat(response.getErrorMessage()).isEmpty();
        }
    }
}

package io.github.glaciousm.llm;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LlmRequest.
 */
@DisplayName("LlmRequest")
class LlmRequestTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should create request with all fields")
        void shouldCreateRequestWithAllFields() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Test prompt")
                    .systemMessage("System message")
                    .maxTokens(500)
                    .temperature(0.7)
                    .metadata(Map.of("key", "value"))
                    .build();

            assertThat(request.getPrompt()).isEqualTo("Test prompt");
            assertThat(request.getSystemMessage()).isEqualTo("System message");
            assertThat(request.getMaxTokens()).isEqualTo(500);
            assertThat(request.getTemperature()).isEqualTo(0.7);
            assertThat(request.getMetadata()).containsEntry("key", "value");
        }

        @Test
        @DisplayName("should use default values")
        void shouldUseDefaultValues() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Test")
                    .build();

            assertThat(request.getMaxTokens()).isEqualTo(1000);
            assertThat(request.getTemperature()).isEqualTo(0.0);
            assertThat(request.getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("should handle null metadata")
        void shouldHandleNullMetadata() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Test")
                    .metadata(null)
                    .build();

            assertThat(request.getMetadata()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("getUserPrompt should return same as getPrompt")
        void getUserPromptShouldReturnSameAsGetPrompt() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("My prompt")
                    .build();

            assertThat(request.getUserPrompt()).isEqualTo(request.getPrompt());
        }

        @Test
        @DisplayName("getSystemPrompt should return same as getSystemMessage")
        void getSystemPromptShouldReturnSameAsGetSystemMessage() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Prompt")
                    .systemMessage("System")
                    .build();

            assertThat(request.getSystemPrompt()).isEqualTo(request.getSystemMessage());
        }

        @Test
        @DisplayName("should return immutable metadata")
        void shouldReturnImmutableMetadata() {
            LlmRequest request = LlmRequest.builder()
                    .prompt("Test")
                    .metadata(Map.of("key", "value"))
                    .build();

            Map<String, Object> metadata = request.getMetadata();
            assertThat(metadata).isUnmodifiable();
        }
    }
}

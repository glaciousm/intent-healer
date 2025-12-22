package io.github.glaciousm.llm.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.glaciousm.core.config.LlmConfig;
import io.github.glaciousm.core.exception.LlmException;
import io.github.glaciousm.core.model.*;
import io.github.glaciousm.llm.LlmProvider;
import io.github.glaciousm.llm.PromptBuilder;
import io.github.glaciousm.llm.ResponseParser;
import io.github.glaciousm.llm.util.HttpClientFactory;
import io.github.glaciousm.llm.util.SecurityUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * OpenAI LLM provider implementation.
 */
public class OpenAiProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiProvider.class);
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final ResponseParser responseParser = new ResponseParser();

    @Override
    public HealDecision evaluateCandidates(
            FailureContext failure,
            UiSnapshot snapshot,
            IntentContract intent,
            LlmConfig config) {

        String apiKey = getApiKey(config);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LlmException(
                "OpenAI API key not configured. Set OPENAI_API_KEY environment variable or 'api_key_env' in healer-config.yml.",
                getProviderName(), config.getModel());
        }

        // Build prompt - use vision-enhanced prompt if vision is enabled
        String prompt;
        String screenshotBase64 = null;

        if (config.isVisionEnabled() && isVisionModel(config.getModel()) && snapshot.getScreenshotBase64().isPresent()) {
            prompt = promptBuilder.buildVisionHealingPrompt(failure, snapshot, intent);
            screenshotBase64 = snapshot.getScreenshotBase64().orElse(null);
            logger.debug("Using vision-enhanced healing with OpenAI model: {}", config.getModel());
        } else {
            prompt = promptBuilder.buildHealingPrompt(failure, snapshot, intent);
            logger.debug("Using text-only healing with OpenAI model: {}", config.getModel());
        }

        String response = callApi(prompt, screenshotBase64, config, apiKey);
        return responseParser.parseHealDecision(response, getProviderName(), config.getModel());
    }

    @Override
    public OutcomeResult validateOutcome(
            String expectedOutcome,
            UiSnapshot before,
            UiSnapshot after,
            LlmConfig config) {

        String apiKey = getApiKey(config);
        String prompt = promptBuilder.buildOutcomeValidationPrompt(expectedOutcome, before, after);

        String response = callApi(prompt, config, apiKey);
        return responseParser.parseOutcomeResult(response, getProviderName(), config.getModel());
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public boolean supportsVision() {
        return true;
    }

    @Override
    public boolean isVisionModel(String model) {
        if (model == null) return false;
        String lowerModel = model.toLowerCase();
        // GPT-4o and GPT-4 Turbo with vision support
        return lowerModel.contains("gpt-4o") ||
               lowerModel.contains("gpt-4-vision") ||
               lowerModel.contains("gpt-4-turbo");
    }

    private String callApi(String prompt, LlmConfig config, String apiKey) {
        return callApi(prompt, null, config, apiKey);
    }

    private String callApi(String prompt, String screenshotBase64, LlmConfig config, String apiKey) {
        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
        String url = baseUrl + "/chat/completions";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", config.getModel());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokensPerRequest());

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");

        // Check if we should use vision (multimodal) format
        boolean useVision = screenshotBase64 != null &&
                           !screenshotBase64.isEmpty() &&
                           config.isVisionEnabled() &&
                           isVisionModel(config.getModel());

        if (useVision) {
            // Multimodal message format with text and image
            ArrayNode contentArray = userMessage.putArray("content");

            // Add text content
            ObjectNode textContent = contentArray.addObject();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            // Add image content
            ObjectNode imageContent = contentArray.addObject();
            imageContent.put("type", "image_url");
            ObjectNode imageUrl = imageContent.putObject("image_url");
            imageUrl.put("url", "data:image/png;base64," + screenshotBase64);

            // Set image detail level based on config
            String detail = config.getVision() != null ? config.getVision().getImageQuality() : "auto";
            imageUrl.put("detail", detail);

            logger.debug("Using vision mode with screenshot for model: {}", config.getModel());
        } else {
            // Standard text-only message
            userMessage.put("content", prompt);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        int retries = 0;
        int maxRetries = config.getMaxRetries();
        Exception lastException = null;

        // Get a cached client with the configured timeout
        OkHttpClient client = HttpClientFactory.getClientWithReadTimeout(config.getTimeoutSeconds());

        while (retries <= maxRetries) {
            try {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "unknown";
                        int statusCode = response.code();
                        // Retry on server errors (5xx), throw immediately on client errors (4xx)
                        if (statusCode >= 500) {
                            throw new IOException("Server error: " + statusCode + " - " + errorBody);
                        }
                        throw new LlmException(SecurityUtils.sanitizeErrorMessage("OpenAI API error: " + statusCode + " - " + errorBody),
                                getProviderName(), config.getModel());
                    }

                    String responseBody = response.body().string();
                    return extractContentFromResponse(responseBody);
                }
            } catch (IOException e) {
                lastException = e;
                retries++;
                if (retries <= maxRetries) {
                    logger.warn("OpenAI request failed, retrying ({}/{}): {}", retries, maxRetries, SecurityUtils.sanitizeErrorMessage(e.getMessage()));
                    try {
                        Thread.sleep(1000L * retries);  // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw LlmException.unavailable(getProviderName(), config.getModel(), lastException);
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            JsonNode choices = json.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
            throw new LlmException("Invalid OpenAI response structure",
                    getProviderName(), "unknown");
        } catch (IOException e) {
            throw new LlmException("Failed to parse OpenAI response", e,
                    getProviderName(), "unknown");
        }
    }

    private String getApiKey(LlmConfig config) {
        String envVar = config.getApiKeyEnv() != null ? config.getApiKeyEnv() : "OPENAI_API_KEY";
        // Check environment variable first, then system property (for tests)
        String apiKey = System.getenv(envVar);
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty(envVar);
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LlmException("API key not found. Set " + envVar + " environment variable.",
                    getProviderName(), config.getModel());
        }
        return apiKey;
    }
}

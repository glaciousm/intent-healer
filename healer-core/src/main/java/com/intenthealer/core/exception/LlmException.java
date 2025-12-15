package com.intenthealer.core.exception;

/**
 * Exception for LLM-related errors.
 */
public class LlmException extends HealingException {

    private final String provider;
    private final String model;

    public LlmException(String message, String provider, String model) {
        super(message, HealingFailureReason.LLM_UNAVAILABLE);
        this.provider = provider;
        this.model = model;
    }

    public LlmException(String message, Throwable cause, String provider, String model) {
        super(message, cause, HealingFailureReason.LLM_UNAVAILABLE);
        this.provider = provider;
        this.model = model;
    }

    public LlmException(String message, Throwable cause, String provider, String model, HealingFailureReason reason) {
        super(message, cause, reason);
        this.provider = provider;
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    /**
     * Creates an exception for unavailable provider.
     */
    public static LlmException unavailable(String provider, String model, Throwable cause) {
        return new LlmException(
                "LLM provider unavailable: " + provider + "/" + model,
                cause, provider, model, HealingFailureReason.LLM_UNAVAILABLE);
    }

    /**
     * Creates an exception for invalid response.
     */
    public static LlmException invalidResponse(String provider, String model, String details) {
        return new LlmException(
                "Invalid LLM response from " + provider + "/" + model + ": " + details,
                provider, model);
    }

    /**
     * Creates an exception for timeout.
     */
    public static LlmException timeout(String provider, String model, int timeoutSeconds) {
        return new LlmException(
                "LLM request timed out after " + timeoutSeconds + "s: " + provider + "/" + model,
                provider, model);
    }

    @Override
    public String toString() {
        return "LlmException{provider='" + provider + "', model='" + model +
               "', message='" + getMessage() + "'}";
    }
}

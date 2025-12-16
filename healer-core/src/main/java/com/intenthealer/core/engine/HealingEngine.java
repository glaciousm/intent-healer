package com.intenthealer.core.engine;

import com.intenthealer.core.config.HealerConfig;
import com.intenthealer.core.engine.guardrails.GuardrailChecker;
import com.intenthealer.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Main healing engine that orchestrates the healing process.
 * This class is independent of Selenium/LLM implementations to allow different integrations.
 */
public class HealingEngine {

    private static final Logger logger = LoggerFactory.getLogger(HealingEngine.class);

    private final HealerConfig config;
    private final GuardrailChecker guardrails;

    // Pluggable components
    private Function<FailureContext, UiSnapshot> snapshotCapture;
    private BiFunction<FailureContext, UiSnapshot, HealDecision> llmEvaluator;
    private TriFunction<ActionType, ElementSnapshot, Object, Void> actionExecutor;
    private Function<ExecutionContext, OutcomeResult> outcomeValidator;

    public HealingEngine(HealerConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.guardrails = new GuardrailChecker(config.getGuardrails());
    }

    /**
     * Set the snapshot capture function.
     */
    public void setSnapshotCapture(Function<FailureContext, UiSnapshot> snapshotCapture) {
        this.snapshotCapture = snapshotCapture;
    }

    /**
     * Set the LLM evaluator function.
     */
    public void setLlmEvaluator(BiFunction<FailureContext, UiSnapshot, HealDecision> llmEvaluator) {
        this.llmEvaluator = llmEvaluator;
    }

    /**
     * Set the action executor function.
     */
    public void setActionExecutor(TriFunction<ActionType, ElementSnapshot, Object, Void> actionExecutor) {
        this.actionExecutor = actionExecutor;
    }

    /**
     * Set the outcome validator function.
     */
    public void setOutcomeValidator(Function<ExecutionContext, OutcomeResult> outcomeValidator) {
        this.outcomeValidator = outcomeValidator;
    }

    /**
     * Attempt to heal a test failure.
     */
    public HealResult attemptHeal(FailureContext failure, IntentContract intent) {
        Instant startTime = Instant.now();

        try {
            // Check if healing is enabled
            if (!config.isEnabled()) {
                return HealResult.refused("Healing is disabled");
            }

            // 1. Pre-LLM guardrail check
            GuardrailResult preCheck = guardrails.checkPreLlm(failure, intent);
            if (preCheck.isRefused()) {
                logger.info("Pre-LLM guardrail refused: {}", preCheck.getReason());
                return HealResult.refused(preCheck.getReason());
            }

            // 2. Capture UI snapshot
            if (snapshotCapture == null) {
                return HealResult.failed("Snapshot capture not configured");
            }
            UiSnapshot snapshot = snapshotCapture.apply(failure);

            if (snapshot == null || !snapshot.hasElements()) {
                return HealResult.failed("No interactive elements found on page");
            }

            // Check URL against forbidden patterns
            GuardrailResult urlCheck = guardrails.checkUrl(snapshot.getUrl());
            if (urlCheck.isRefused()) {
                return HealResult.refused(urlCheck.getReason());
            }

            // 3. Get LLM decision
            if (llmEvaluator == null) {
                return HealResult.failed("LLM evaluator not configured");
            }
            HealDecision decision = llmEvaluator.apply(failure, snapshot);

            // 4. Check if LLM decided not to heal
            if (!decision.canHeal()) {
                return HealResult.builder()
                        .outcome(HealOutcome.REFUSED)
                        .decision(decision)
                        .failureReason(decision.getRefusalReason())
                        .duration(Duration.between(startTime, Instant.now()))
                        .build();
            }

            // 5. Get chosen element
            Optional<ElementSnapshot> chosenOpt = snapshot.getElement(decision.getSelectedElementIndex());
            if (chosenOpt.isEmpty()) {
                return HealResult.failed("Selected element index not found in snapshot");
            }
            ElementSnapshot chosenElement = chosenOpt.get();

            // 6. Post-LLM guardrail check
            GuardrailResult postCheck = guardrails.checkPostLlm(decision, chosenElement, snapshot);
            if (postCheck.isRefused()) {
                logger.info("Post-LLM guardrail refused: {}", postCheck.getReason());
                return HealResult.builder()
                        .outcome(HealOutcome.REFUSED)
                        .decision(decision)
                        .failureReason(postCheck.getReason())
                        .duration(Duration.between(startTime, Instant.now()))
                        .build();
            }

            // 7. Handle SUGGEST mode (don't actually execute)
            if (intent.getPolicy() == HealPolicy.SUGGEST) {
                return HealResult.builder()
                        .outcome(HealOutcome.SUGGESTED)
                        .decision(decision)
                        .healedElementIndex(decision.getSelectedElementIndex())
                        .confidence(decision.getConfidence())
                        .reasoning(decision.getReasoning())
                        .duration(Duration.between(startTime, Instant.now()))
                        .build();
            }

            // 8. Execute the healed action
            if (actionExecutor != null) {
                try {
                    actionExecutor.apply(failure.getActionType(), chosenElement, failure.getActionData());
                } catch (Exception e) {
                    logger.error("Action execution failed: {}", e.getMessage());
                    return HealResult.builder()
                            .outcome(HealOutcome.FAILED)
                            .decision(decision)
                            .failureReason("Action execution failed: " + e.getMessage())
                            .duration(Duration.between(startTime, Instant.now()))
                            .build();
                }
            }

            // 9. Validate outcome (if validator configured)
            if (outcomeValidator != null) {
                ExecutionContext ctx = new ExecutionContext(null, snapshot);
                OutcomeResult outcomeResult = outcomeValidator.apply(ctx);
                if (outcomeResult.isFailed()) {
                    return HealResult.builder()
                            .outcome(HealOutcome.OUTCOME_FAILED)
                            .decision(decision)
                            .failureReason(outcomeResult.getMessage())
                            .duration(Duration.between(startTime, Instant.now()))
                            .build();
                }
            }

            // 10. Success! Generate healed locator from chosen element
            String healedLocator = generateLocatorFromElement(chosenElement);
            logger.info("Generated healed locator: {}", healedLocator);

            return HealResult.builder()
                    .outcome(HealOutcome.SUCCESS)
                    .decision(decision)
                    .healedElementIndex(decision.getSelectedElementIndex())
                    .healedLocator(healedLocator)
                    .confidence(decision.getConfidence())
                    .reasoning(decision.getReasoning())
                    .duration(Duration.between(startTime, Instant.now()))
                    .build();

        } catch (Exception e) {
            logger.error("Unexpected error during healing: {}", e.getMessage(), e);
            return HealResult.builder()
                    .outcome(HealOutcome.FAILED)
                    .failureReason("Unexpected error: " + e.getMessage())
                    .duration(Duration.between(startTime, Instant.now()))
                    .build();
        }
    }

    /**
     * Generate a locator string from an ElementSnapshot.
     * Format: "strategy=value" (e.g., "id=login-btn", "css=button.submit")
     * Prefers id > name > css selector with class.
     */
    private String generateLocatorFromElement(ElementSnapshot element) {
        // Prefer ID if available (but not UUIDs or dynamic IDs)
        String id = element.getId();
        if (id != null && !id.isEmpty() && !looksLikeDynamicId(id)) {
            return "id=" + id;
        }

        // Try name
        if (element.getName() != null && !element.getName().isEmpty()) {
            return "name=" + element.getName();
        }

        // Build CSS selector
        StringBuilder css = new StringBuilder();
        String tagName = element.getTagName() != null ? element.getTagName().toLowerCase() : "div";
        css.append(tagName);

        // Add classes if available (limit to first 2 to avoid overly specific selectors)
        List<String> classes = element.getClasses();
        if (classes != null && !classes.isEmpty()) {
            int classCount = 0;
            for (String className : classes) {
                // Skip dynamic-looking class names
                if (!looksLikeDynamicClass(className) && classCount < 2) {
                    css.append(".").append(className.replace(" ", ""));
                    classCount++;
                }
            }
        }

        // Add type attribute for inputs (only for specific types that are stable)
        String type = element.getType();
        if (type != null && !type.isEmpty() && isStableTypeAttribute(tagName, type)) {
            css.append("[type=\"").append(type).append("\"]");
        }

        // If we only have a tag name with no distinguishing features, try text content
        if (css.toString().equals(tagName) && element.getText() != null && !element.getText().isEmpty()) {
            // For buttons/links, use a more specific selector based on visible text
            String text = element.getText().trim();
            if (text.length() <= 30) {
                // XPath with text content for unique identification
                return "xpath=//" + tagName + "[contains(text(),'" + escapeXpathText(text) + "')]";
            }
        }

        return "css=" + css.toString();
    }

    /**
     * Check if an ID looks like a dynamically generated one.
     */
    private boolean looksLikeDynamicId(String id) {
        // UUID patterns, numbers only, or very long IDs
        return id.matches(".*[0-9a-f]{8}-[0-9a-f]{4}-.*") ||  // UUID pattern
               id.matches("\\d+") ||                          // Numbers only
               id.length() > 50 ||                            // Very long IDs
               id.contains("ember") ||                        // Ember.js generated
               id.contains("react") ||                        // React generated
               id.matches(".*\\d{5,}.*");                     // Contains 5+ digit numbers
    }

    /**
     * Check if a class name looks dynamically generated.
     */
    private boolean looksLikeDynamicClass(String className) {
        return className.matches(".*[0-9a-f]{6,}.*") ||       // Long hex strings
               className.matches(".*\\d{4,}.*") ||            // 4+ digit numbers
               className.contains("_");                        // Often dynamic separators
    }

    /**
     * Check if a type attribute is stable and worth including in selector.
     */
    private boolean isStableTypeAttribute(String tagName, String type) {
        if (!"input".equals(tagName)) {
            return false;
        }
        // Only include type for input elements with distinctive types
        return "checkbox".equals(type) || "radio".equals(type) ||
               "text".equals(type) || "password".equals(type) ||
               "email".equals(type) || "number".equals(type);
    }

    /**
     * Escape text for use in XPath.
     */
    private String escapeXpathText(String text) {
        return text.replace("'", "\\'");
    }

    /**
     * Functional interface for three-argument functions.
     */
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}

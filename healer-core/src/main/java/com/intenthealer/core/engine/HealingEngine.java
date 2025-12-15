package com.intenthealer.core.engine;

import com.intenthealer.core.config.HealerConfig;
import com.intenthealer.core.engine.guardrails.GuardrailChecker;
import com.intenthealer.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
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

            // 10. Success!
            return HealResult.builder()
                    .outcome(HealOutcome.SUCCESS)
                    .decision(decision)
                    .healedElementIndex(decision.getSelectedElementIndex())
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
     * Functional interface for three-argument functions.
     */
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}

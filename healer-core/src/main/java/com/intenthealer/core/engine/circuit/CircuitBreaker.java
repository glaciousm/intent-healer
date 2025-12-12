package com.intenthealer.core.engine.circuit;

import com.intenthealer.core.config.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker to automatically disable healing after repeated failures.
 * Prevents runaway LLM costs and flaky test suites from attempting heals.
 *
 * States:
 * - CLOSED: Normal operation, healing allowed
 * - OPEN: Too many failures, healing disabled
 * - HALF_OPEN: Testing if system recovered
 */
public class CircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);

    private final CircuitBreakerConfig config;
    private final AtomicReference<CircuitState> state;
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private final AtomicInteger halfOpenAttempts;
    private volatile Instant lastFailureTime;
    private volatile Instant openedAt;

    public CircuitBreaker(CircuitBreakerConfig config) {
        this.config = config != null ? config : new CircuitBreakerConfig();
        this.state = new AtomicReference<>(CircuitState.CLOSED);
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.halfOpenAttempts = new AtomicInteger(0);
    }

    public CircuitBreaker() {
        this(new CircuitBreakerConfig());
    }

    /**
     * Check if healing is currently allowed.
     */
    public boolean isHealingAllowed() {
        if (!config.isEnabled()) {
            return true; // Circuit breaker disabled
        }

        updateState();

        CircuitState currentState = state.get();
        switch (currentState) {
            case CLOSED:
                return true;
            case OPEN:
                logger.debug("Circuit is OPEN, healing not allowed");
                return false;
            case HALF_OPEN:
                // Allow limited attempts in half-open state
                int attempts = halfOpenAttempts.incrementAndGet();
                boolean allowed = attempts <= config.getHalfOpenMaxAttempts();
                if (!allowed) {
                    logger.debug("Circuit is HALF_OPEN but max attempts reached");
                }
                return allowed;
            default:
                return true;
        }
    }

    /**
     * Record a successful heal.
     */
    public void recordSuccess() {
        if (!config.isEnabled()) {
            return;
        }

        int successes = successCount.incrementAndGet();
        logger.debug("Recorded heal success (count: {})", successes);

        CircuitState currentState = state.get();
        if (currentState == CircuitState.HALF_OPEN) {
            // Successful heal in half-open state closes the circuit
            if (successes >= config.getSuccessThresholdToClose()) {
                closeCircuit();
            }
        } else if (currentState == CircuitState.CLOSED) {
            // Reset failure count on success in closed state
            if (successes >= config.getSuccessThresholdToClose()) {
                failureCount.set(0);
                successCount.set(0);
            }
        }
    }

    /**
     * Record a failed heal.
     */
    public void recordFailure() {
        if (!config.isEnabled()) {
            return;
        }

        int failures = failureCount.incrementAndGet();
        lastFailureTime = Instant.now();
        successCount.set(0); // Reset success count on failure
        logger.debug("Recorded heal failure (count: {})", failures);

        CircuitState currentState = state.get();
        if (currentState == CircuitState.HALF_OPEN) {
            // Any failure in half-open state opens the circuit again
            openCircuit();
        } else if (currentState == CircuitState.CLOSED) {
            // Check if we should open the circuit
            if (failures >= config.getFailureThreshold()) {
                openCircuit();
            }
        }
    }

    /**
     * Record that healing was refused (not a failure, but shouldn't reset counts).
     */
    public void recordRefusal() {
        // Refusals are not failures, but we don't want to reset failure counts either
        logger.debug("Recorded heal refusal");
    }

    /**
     * Get the current circuit state.
     */
    public CircuitState getState() {
        updateState();
        return state.get();
    }

    /**
     * Get the failure count.
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * Get time until circuit might close (if OPEN).
     */
    public Duration getTimeUntilClose() {
        if (state.get() != CircuitState.OPEN || openedAt == null) {
            return Duration.ZERO;
        }

        Duration elapsed = Duration.between(openedAt, Instant.now());
        Duration remaining = Duration.ofSeconds(config.getOpenDurationSeconds()).minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Manually reset the circuit breaker.
     */
    public void reset() {
        state.set(CircuitState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        halfOpenAttempts.set(0);
        openedAt = null;
        logger.info("Circuit breaker manually reset");
    }

    /**
     * Force the circuit open (for testing or manual intervention).
     */
    public void forceOpen() {
        openCircuit();
        logger.info("Circuit breaker forced OPEN");
    }

    private void updateState() {
        CircuitState currentState = state.get();

        if (currentState == CircuitState.OPEN && openedAt != null) {
            // Check if enough time has passed to try half-open
            Duration elapsed = Duration.between(openedAt, Instant.now());
            if (elapsed.getSeconds() >= config.getOpenDurationSeconds()) {
                if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                    halfOpenAttempts.set(0);
                    successCount.set(0);
                    logger.info("Circuit breaker transitioning to HALF_OPEN");
                }
            }
        }
    }

    private void openCircuit() {
        CircuitState previous = state.getAndSet(CircuitState.OPEN);
        openedAt = Instant.now();
        halfOpenAttempts.set(0);
        if (previous != CircuitState.OPEN) {
            logger.warn("Circuit breaker OPENED due to {} failures", failureCount.get());
        }
    }

    private void closeCircuit() {
        CircuitState previous = state.getAndSet(CircuitState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        halfOpenAttempts.set(0);
        openedAt = null;
        if (previous != CircuitState.CLOSED) {
            logger.info("Circuit breaker CLOSED after successful recovery");
        }
    }

    /**
     * Get circuit breaker statistics.
     */
    public CircuitStats getStats() {
        return new CircuitStats(
                state.get(),
                failureCount.get(),
                successCount.get(),
                lastFailureTime,
                openedAt,
                getTimeUntilClose()
        );
    }

    /**
     * Circuit breaker statistics.
     */
    public record CircuitStats(
            CircuitState state,
            int failureCount,
            int successCount,
            Instant lastFailureTime,
            Instant openedAt,
            Duration timeUntilClose
    ) {}
}

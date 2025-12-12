package com.intenthealer.core.engine.trust;

import com.intenthealer.core.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages trust level progression based on healing outcomes.
 *
 * Trust progression rules:
 * - Promote after N consecutive successes
 * - Demote after M failures within a time window
 * - Can be manually overridden
 */
public class TrustLevelManager {

    private static final Logger logger = LoggerFactory.getLogger(TrustLevelManager.class);

    private final AtomicReference<TrustLevel> currentLevel;
    private final AtomicInteger consecutiveSuccesses;
    private final AtomicInteger failuresInWindow;
    private volatile Instant windowStart;

    // Configuration
    private final int successesToPromote;
    private final int failuresToDemote;
    private final Duration failureWindow;
    private final TrustLevel maxLevel;
    private final TrustLevel minLevel;
    private final boolean autoPromote;
    private final boolean autoDemote;

    private TrustLevelManager(Builder builder) {
        this.currentLevel = new AtomicReference<>(builder.initialLevel);
        this.consecutiveSuccesses = new AtomicInteger(0);
        this.failuresInWindow = new AtomicInteger(0);
        this.windowStart = Instant.now();
        this.successesToPromote = builder.successesToPromote;
        this.failuresToDemote = builder.failuresToDemote;
        this.failureWindow = builder.failureWindow;
        this.maxLevel = builder.maxLevel;
        this.minLevel = builder.minLevel;
        this.autoPromote = builder.autoPromote;
        this.autoDemote = builder.autoDemote;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the current trust level.
     */
    public TrustLevel getCurrentLevel() {
        return currentLevel.get();
    }

    /**
     * Check if healing is allowed at the current trust level.
     */
    public boolean isHealingAllowed() {
        return currentLevel.get() != TrustLevel.L0_SHADOW;
    }

    /**
     * Check if auto-apply is allowed for a given action type.
     */
    public boolean canAutoApply(ActionType actionType) {
        TrustLevel level = currentLevel.get();
        if (!level.canAutoApply()) {
            return false;
        }

        String actionName = actionType != null ? actionType.name() : null;
        return level.canAutoApplyAction(actionName);
    }

    /**
     * Check if manual approval is required for a heal.
     */
    public boolean requiresApproval() {
        return currentLevel.get().requiresApproval();
    }

    /**
     * Check if heals can be auto-committed to source control.
     */
    public boolean canAutoCommit() {
        return currentLevel.get().canAutoCommit();
    }

    /**
     * Record a successful heal.
     */
    public void recordSuccess() {
        int successes = consecutiveSuccesses.incrementAndGet();
        logger.debug("Recorded success (consecutive: {})", successes);

        if (autoPromote && successes >= successesToPromote) {
            promote();
        }
    }

    /**
     * Record a failed heal.
     */
    public void recordFailure() {
        consecutiveSuccesses.set(0);

        // Check if we need to reset the failure window
        Instant now = Instant.now();
        if (Duration.between(windowStart, now).compareTo(failureWindow) > 0) {
            windowStart = now;
            failuresInWindow.set(0);
        }

        int failures = failuresInWindow.incrementAndGet();
        logger.debug("Recorded failure (in window: {})", failures);

        if (autoDemote && failures >= failuresToDemote) {
            demote();
            // Reset after demotion
            failuresInWindow.set(0);
            windowStart = now;
        }
    }

    /**
     * Record a refused heal (doesn't affect trust levels).
     */
    public void recordRefusal() {
        // Refusals are expected behavior, don't affect trust
        logger.debug("Recorded refusal (no trust impact)");
    }

    /**
     * Promote to the next trust level.
     */
    public TrustLevel promote() {
        while (true) {
            TrustLevel current = currentLevel.get();
            if (current.getLevel() >= maxLevel.getLevel()) {
                logger.debug("Already at max trust level: {}", current);
                return current;
            }

            TrustLevel next = current.promote();
            if (next.getLevel() > maxLevel.getLevel()) {
                next = maxLevel;
            }

            if (currentLevel.compareAndSet(current, next)) {
                consecutiveSuccesses.set(0);
                logger.info("Trust level promoted: {} -> {}", current, next);
                return next;
            }
        }
    }

    /**
     * Demote to the previous trust level.
     */
    public TrustLevel demote() {
        while (true) {
            TrustLevel current = currentLevel.get();
            if (current.getLevel() <= minLevel.getLevel()) {
                logger.debug("Already at min trust level: {}", current);
                return current;
            }

            TrustLevel prev = current.demote();
            if (prev.getLevel() < minLevel.getLevel()) {
                prev = minLevel;
            }

            if (currentLevel.compareAndSet(current, prev)) {
                logger.warn("Trust level demoted: {} -> {}", current, prev);
                return prev;
            }
        }
    }

    /**
     * Set trust level directly (manual override).
     */
    public void setLevel(TrustLevel level) {
        if (level.getLevel() < minLevel.getLevel()) {
            level = minLevel;
        } else if (level.getLevel() > maxLevel.getLevel()) {
            level = maxLevel;
        }

        TrustLevel previous = currentLevel.getAndSet(level);
        consecutiveSuccesses.set(0);
        failuresInWindow.set(0);
        windowStart = Instant.now();

        logger.info("Trust level manually set: {} -> {}", previous, level);
    }

    /**
     * Reset trust statistics without changing level.
     */
    public void resetStats() {
        consecutiveSuccesses.set(0);
        failuresInWindow.set(0);
        windowStart = Instant.now();
        logger.debug("Trust statistics reset");
    }

    /**
     * Get statistics about trust progression.
     */
    public TrustStats getStats() {
        return new TrustStats(
                currentLevel.get(),
                consecutiveSuccesses.get(),
                failuresInWindow.get(),
                successesToPromote - consecutiveSuccesses.get(),
                Duration.between(windowStart, Instant.now())
        );
    }

    /**
     * Trust level statistics.
     */
    public record TrustStats(
            TrustLevel currentLevel,
            int consecutiveSuccesses,
            int failuresInWindow,
            int successesUntilPromotion,
            Duration windowElapsed
    ) {}

    public static class Builder {
        private TrustLevel initialLevel = TrustLevel.L0_SHADOW;
        private int successesToPromote = 10;
        private int failuresToDemote = 3;
        private Duration failureWindow = Duration.ofHours(1);
        private TrustLevel maxLevel = TrustLevel.L4_SILENT;
        private TrustLevel minLevel = TrustLevel.L0_SHADOW;
        private boolean autoPromote = true;
        private boolean autoDemote = true;

        public Builder initialLevel(TrustLevel level) {
            this.initialLevel = level;
            return this;
        }

        public Builder successesToPromote(int count) {
            this.successesToPromote = count;
            return this;
        }

        public Builder failuresToDemote(int count) {
            this.failuresToDemote = count;
            return this;
        }

        public Builder failureWindow(Duration window) {
            this.failureWindow = window;
            return this;
        }

        public Builder maxLevel(TrustLevel level) {
            this.maxLevel = level;
            return this;
        }

        public Builder minLevel(TrustLevel level) {
            this.minLevel = level;
            return this;
        }

        public Builder autoPromote(boolean auto) {
            this.autoPromote = auto;
            return this;
        }

        public Builder autoDemote(boolean auto) {
            this.autoDemote = auto;
            return this;
        }

        public TrustLevelManager build() {
            return new TrustLevelManager(this);
        }
    }
}

package io.github.glaciousm.intellij.services;

import io.github.glaciousm.intellij.services.HealerProjectService.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HealerProjectService data structures.
 * Tests the record types without requiring IntelliJ runtime.
 */
class HealerProjectServiceTest {

    @Nested
    @DisplayName("HealHistoryEntry Record")
    class HealHistoryEntryTests {

        @Test
        @DisplayName("should create heal history entry with all fields")
        void createHealHistoryEntry() {
            Instant now = Instant.now();
            HealHistoryEntry entry = new HealHistoryEntry(
                    "heal-123",
                    now,
                    "Login Feature",
                    "User Login",
                    "When I click the login button",
                    "#login-btn",
                    "#submit-btn",
                    0.95,
                    "Button ID changed from login-btn to submit-btn",
                    HealStatus.PENDING
            );

            assertEquals("heal-123", entry.id());
            assertEquals(now, entry.timestamp());
            assertEquals("Login Feature", entry.featureName());
            assertEquals("User Login", entry.scenarioName());
            assertEquals("When I click the login button", entry.stepText());
            assertEquals("#login-btn", entry.originalLocator());
            assertEquals("#submit-btn", entry.healedLocator());
            assertEquals(0.95, entry.confidence());
            assertEquals("Button ID changed from login-btn to submit-btn", entry.reasoning());
            assertEquals(HealStatus.PENDING, entry.status());
        }

        @Test
        @DisplayName("should support all heal statuses")
        void allHealStatuses() {
            Instant now = Instant.now();

            HealHistoryEntry pending = new HealHistoryEntry(
                    "1", now, "", "", "", "", "", 0.9, "", HealStatus.PENDING);
            HealHistoryEntry accepted = new HealHistoryEntry(
                    "2", now, "", "", "", "", "", 0.9, "", HealStatus.ACCEPTED);
            HealHistoryEntry rejected = new HealHistoryEntry(
                    "3", now, "", "", "", "", "", 0.9, "", HealStatus.REJECTED);
            HealHistoryEntry blacklisted = new HealHistoryEntry(
                    "4", now, "", "", "", "", "", 0.9, "", HealStatus.BLACKLISTED);

            assertEquals(HealStatus.PENDING, pending.status());
            assertEquals(HealStatus.ACCEPTED, accepted.status());
            assertEquals(HealStatus.REJECTED, rejected.status());
            assertEquals(HealStatus.BLACKLISTED, blacklisted.status());
        }

        @Test
        @DisplayName("should be equal when all fields match")
        void entryEquality() {
            Instant now = Instant.now();
            HealHistoryEntry entry1 = new HealHistoryEntry(
                    "123", now, "Feature", "Scenario", "Step",
                    "#old", "#new", 0.9, "reason", HealStatus.PENDING);
            HealHistoryEntry entry2 = new HealHistoryEntry(
                    "123", now, "Feature", "Scenario", "Step",
                    "#old", "#new", 0.9, "reason", HealStatus.PENDING);

            assertEquals(entry1, entry2);
            assertEquals(entry1.hashCode(), entry2.hashCode());
        }
    }

    @Nested
    @DisplayName("TrustLevelInfo Record")
    class TrustLevelInfoTests {

        @Test
        @DisplayName("should create trust level info with all fields")
        void createTrustLevelInfo() {
            TrustLevelInfo info = new TrustLevelInfo(
                    "L2_AUTO_SAFE",
                    15,
                    2,
                    0.85
            );

            assertEquals("L2_AUTO_SAFE", info.level());
            assertEquals(15, info.consecutiveSuccesses());
            assertEquals(2, info.failuresInWindow());
            assertEquals(0.85, info.successRate());
        }

        @Test
        @DisplayName("should support L0_SHADOW level")
        void l0ShadowLevel() {
            TrustLevelInfo info = new TrustLevelInfo("L0_SHADOW", 0, 0, 0.0);
            assertEquals("L0_SHADOW", info.level());
        }

        @Test
        @DisplayName("should support L1_SUGGEST level")
        void l1SuggestLevel() {
            TrustLevelInfo info = new TrustLevelInfo("L1_SUGGEST", 5, 1, 0.83);
            assertEquals("L1_SUGGEST", info.level());
        }

        @Test
        @DisplayName("should support L3_AUTO_ALL level")
        void l3AutoAllLevel() {
            TrustLevelInfo info = new TrustLevelInfo("L3_AUTO_ALL", 50, 0, 1.0);
            assertEquals("L3_AUTO_ALL", info.level());
        }
    }

    @Nested
    @DisplayName("BlacklistEntry Record")
    class BlacklistEntryTests {

        @Test
        @DisplayName("should create blacklist entry with all fields")
        void createBlacklistEntry() {
            Instant now = Instant.now();
            String id = UUID.randomUUID().toString();

            BlacklistEntry entry = new BlacklistEntry(
                    id,
                    "#delete-btn",
                    "#remove-all-btn",
                    "Incorrectly healed to destructive action",
                    now
            );

            assertEquals(id, entry.id());
            assertEquals("#delete-btn", entry.originalLocator());
            assertEquals("#remove-all-btn", entry.healedLocator());
            assertEquals("Incorrectly healed to destructive action", entry.reason());
            assertEquals(now, entry.timestamp());
        }
    }

    @Nested
    @DisplayName("LocatorStabilityEntry Record")
    class LocatorStabilityEntryTests {

        @Test
        @DisplayName("should create stability entry with all fields")
        void createStabilityEntry() {
            LocatorStabilityEntry entry = new LocatorStabilityEntry(
                    "#submit-btn",
                    0.95,
                    "VERY_STABLE",
                    100,
                    5,
                    3
            );

            assertEquals("#submit-btn", entry.locator());
            assertEquals(0.95, entry.score());
            assertEquals("VERY_STABLE", entry.level());
            assertEquals(100, entry.successes());
            assertEquals(5, entry.failures());
            assertEquals(3, entry.heals());
        }

        @Test
        @DisplayName("should support all stability levels")
        void allStabilityLevels() {
            assertEquals("VERY_STABLE", new LocatorStabilityEntry("a", 0.95, "VERY_STABLE", 100, 0, 0).level());
            assertEquals("STABLE", new LocatorStabilityEntry("b", 0.85, "STABLE", 80, 10, 5).level());
            assertEquals("MODERATE", new LocatorStabilityEntry("c", 0.70, "MODERATE", 60, 20, 10).level());
            assertEquals("UNSTABLE", new LocatorStabilityEntry("d", 0.50, "UNSTABLE", 40, 30, 20).level());
            assertEquals("VERY_UNSTABLE", new LocatorStabilityEntry("e", 0.30, "VERY_UNSTABLE", 20, 40, 30).level());
        }
    }

    @Nested
    @DisplayName("StabilitySummary Record")
    class StabilitySummaryTests {

        @Test
        @DisplayName("should create stability summary with all fields")
        void createStabilitySummary() {
            StabilitySummary summary = new StabilitySummary(100, 60, 25, 15);

            assertEquals(100, summary.total());
            assertEquals(60, summary.stable());
            assertEquals(25, summary.moderate());
            assertEquals(15, summary.unstable());
        }

        @Test
        @DisplayName("should handle zero counts")
        void zeroCounts() {
            StabilitySummary summary = new StabilitySummary(0, 0, 0, 0);

            assertEquals(0, summary.total());
            assertEquals(0, summary.stable());
            assertEquals(0, summary.moderate());
            assertEquals(0, summary.unstable());
        }

        @Test
        @DisplayName("counts should sum to total")
        void countsAddUp() {
            StabilitySummary summary = new StabilitySummary(100, 60, 25, 15);

            assertEquals(summary.total(), summary.stable() + summary.moderate() + summary.unstable());
        }
    }

    @Nested
    @DisplayName("HealStatus Enum")
    class HealStatusTests {

        @Test
        @DisplayName("should have all expected values")
        void allValues() {
            HealStatus[] values = HealStatus.values();

            assertEquals(4, values.length);
            assertEquals(HealStatus.PENDING, HealStatus.valueOf("PENDING"));
            assertEquals(HealStatus.ACCEPTED, HealStatus.valueOf("ACCEPTED"));
            assertEquals(HealStatus.REJECTED, HealStatus.valueOf("REJECTED"));
            assertEquals(HealStatus.BLACKLISTED, HealStatus.valueOf("BLACKLISTED"));
        }

        @Test
        @DisplayName("should have correct ordinals")
        void ordinals() {
            assertEquals(0, HealStatus.PENDING.ordinal());
            assertEquals(1, HealStatus.ACCEPTED.ordinal());
            assertEquals(2, HealStatus.REJECTED.ordinal());
            assertEquals(3, HealStatus.BLACKLISTED.ordinal());
        }
    }
}

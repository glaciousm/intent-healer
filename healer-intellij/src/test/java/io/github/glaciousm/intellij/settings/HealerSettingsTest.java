package io.github.glaciousm.intellij.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HealerSettings.
 * Tests the state management without requiring IntelliJ runtime.
 */
class HealerSettingsTest {

    private HealerSettings settings;

    @BeforeEach
    void setUp() {
        settings = new HealerSettings();
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have notifications enabled by default")
        void notificationsEnabledByDefault() {
            assertTrue(settings.enableNotifications);
        }

        @Test
        @DisplayName("should have auto refresh dashboard enabled by default")
        void autoRefreshEnabledByDefault() {
            assertTrue(settings.autoRefreshDashboard);
        }

        @Test
        @DisplayName("should have 30 second refresh interval by default")
        void defaultRefreshInterval() {
            assertEquals(30, settings.refreshIntervalSeconds);
        }

        @Test
        @DisplayName("should have correct default heal cache directory")
        void defaultHealCacheDirectory() {
            assertEquals(".intent-healer/cache", settings.healCacheDirectory);
        }

        @Test
        @DisplayName("should have history persistence enabled by default")
        void historyPersistenceEnabledByDefault() {
            assertTrue(settings.persistHealHistory);
        }

        @Test
        @DisplayName("should have 1000 max history entries by default")
        void defaultMaxHistoryEntries() {
            assertEquals(1000, settings.maxHistoryEntries);
        }

        @Test
        @DisplayName("should have real-time watching enabled by default")
        void realTimeWatchingEnabledByDefault() {
            assertTrue(settings.enableRealTimeWatching);
        }

        @Test
        @DisplayName("should have 5 second watch polling interval by default")
        void defaultWatchPollingInterval() {
            assertEquals(5, settings.watchPollingIntervalSeconds);
        }

        @Test
        @DisplayName("should show trust level by default")
        void showTrustLevelByDefault() {
            assertTrue(settings.showTrustLevel);
        }

        @Test
        @DisplayName("should warn on low confidence by default")
        void warnOnLowConfidenceByDefault() {
            assertTrue(settings.warnOnLowConfidence);
        }

        @Test
        @DisplayName("should have 0.75 confidence warning threshold by default")
        void defaultConfidenceWarningThreshold() {
            assertEquals(0.75, settings.confidenceWarningThreshold);
        }

        @Test
        @DisplayName("should have openai as default LLM provider")
        void defaultLlmProvider() {
            assertEquals("openai", settings.llmProvider);
        }

        @Test
        @DisplayName("should have OPENAI_API_KEY as default env var")
        void defaultApiKeyEnvVar() {
            assertEquals("OPENAI_API_KEY", settings.apiKeyEnvVar);
        }

        @Test
        @DisplayName("should show line markers by default")
        void showLineMarkersByDefault() {
            assertTrue(settings.showLineMarkers);
        }

        @Test
        @DisplayName("should highlight healed locators by default")
        void highlightHealedLocatorsByDefault() {
            assertTrue(settings.highlightHealedLocators);
        }
    }

    @Nested
    @DisplayName("State Management")
    class StateManagementTests {

        @Test
        @DisplayName("getState should return this instance")
        void getStateReturnsThis() {
            assertSame(settings, settings.getState());
        }

        @Test
        @DisplayName("loadState should copy values from another instance")
        void loadStateCopiesValues() {
            HealerSettings other = new HealerSettings();
            other.enableNotifications = false;
            other.refreshIntervalSeconds = 60;
            other.llmProvider = "anthropic";
            other.confidenceWarningThreshold = 0.90;

            settings.loadState(other);

            assertFalse(settings.enableNotifications);
            assertEquals(60, settings.refreshIntervalSeconds);
            assertEquals("anthropic", settings.llmProvider);
            assertEquals(0.90, settings.confidenceWarningThreshold);
        }

        @Test
        @DisplayName("loadState should preserve other default values")
        void loadStatePreservesDefaults() {
            HealerSettings other = new HealerSettings();
            other.enableNotifications = false;

            settings.loadState(other);

            // Modified value
            assertFalse(settings.enableNotifications);
            // Unchanged values should still be defaults
            assertTrue(settings.autoRefreshDashboard);
            assertEquals(30, settings.refreshIntervalSeconds);
        }
    }

    @Nested
    @DisplayName("Field Modification")
    class FieldModificationTests {

        @Test
        @DisplayName("should allow modifying notification settings")
        void modifyNotificationSettings() {
            settings.enableNotifications = false;
            assertFalse(settings.enableNotifications);
        }

        @Test
        @DisplayName("should allow modifying refresh interval")
        void modifyRefreshInterval() {
            settings.refreshIntervalSeconds = 120;
            assertEquals(120, settings.refreshIntervalSeconds);
        }

        @Test
        @DisplayName("should allow modifying LLM provider")
        void modifyLlmProvider() {
            settings.llmProvider = "ollama";
            assertEquals("ollama", settings.llmProvider);
        }

        @Test
        @DisplayName("should allow modifying confidence threshold")
        void modifyConfidenceThreshold() {
            settings.confidenceWarningThreshold = 0.85;
            assertEquals(0.85, settings.confidenceWarningThreshold);
        }

        @Test
        @DisplayName("should allow modifying cache directory")
        void modifyCacheDirectory() {
            settings.healCacheDirectory = "/custom/cache/path";
            assertEquals("/custom/cache/path", settings.healCacheDirectory);
        }

        @Test
        @DisplayName("should allow modifying max history entries")
        void modifyMaxHistoryEntries() {
            settings.maxHistoryEntries = 5000;
            assertEquals(5000, settings.maxHistoryEntries);
        }
    }
}

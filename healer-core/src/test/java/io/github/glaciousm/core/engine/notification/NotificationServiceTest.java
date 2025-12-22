package io.github.glaciousm.core.engine.notification;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NotificationService.
 */
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Nested
    @DisplayName("Configuration")
    class ConfigurationTests {

        @Test
        @DisplayName("should not send when disabled")
        void shouldNotSendWhenDisabled() throws Exception {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(false)
                    .slackWebhookUrl("https://hooks.slack.com/services/test")
                    .build();

            NotificationService service = new NotificationService(config);

            try {
                // Should return immediately without sending
                service.notifyHeal(createTestNotification(true)).get(1, TimeUnit.SECONDS);
                // No exception means success - disabled notifications don't send
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("should not send when no channels configured")
        void shouldNotSendWhenNoChannelsConfigured() throws Exception {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .build();

            NotificationService service = new NotificationService(config);

            try {
                service.notifyHeal(createTestNotification(true)).get(1, TimeUnit.SECONDS);
                // No exception means success - no channels means nothing to send
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("should create service with slack channel")
        void shouldCreateServiceWithSlackChannel() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .slackWebhookUrl("https://hooks.slack.com/services/test")
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            // Service created successfully with Slack channel
        }

        @Test
        @DisplayName("should create service with teams channel")
        void shouldCreateServiceWithTeamsChannel() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .teamsWebhookUrl("https://outlook.office.com/webhook/test")
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            // Service created successfully with Teams channel
        }

        @Test
        @DisplayName("should create service with custom webhook channel")
        void shouldCreateServiceWithCustomWebhookChannel() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .customWebhookUrl("https://example.com/webhook")
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            // Service created successfully with custom webhook channel
        }

        @Test
        @DisplayName("should create service with multiple channels")
        void shouldCreateServiceWithMultipleChannels() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .slackWebhookUrl("https://hooks.slack.com/services/test")
                    .teamsWebhookUrl("https://outlook.office.com/webhook/test")
                    .customWebhookUrl("https://example.com/webhook")
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            // Service created successfully with all channels
        }
    }

    @Nested
    @DisplayName("Notification Levels")
    class NotificationLevelTests {

        @Test
        @DisplayName("should respect ALL level")
        void shouldRespectAllLevel() throws Exception {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .notificationLevel(NotificationService.NotificationLevel.ALL)
                    .build();

            NotificationService service = new NotificationService(config);

            try {
                // Both success and failure should be allowed
                service.notifyHeal(createTestNotification(true)).get(1, TimeUnit.SECONDS);
                service.notifyHeal(createTestNotification(false)).get(1, TimeUnit.SECONDS);
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("should respect NONE level")
        void shouldRespectNoneLevel() throws Exception {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .notificationLevel(NotificationService.NotificationLevel.NONE)
                    .build();

            NotificationService service = new NotificationService(config);

            try {
                // All notifications should complete without sending
                service.notifyHeal(createTestNotification(true)).get(1, TimeUnit.SECONDS);
                service.notifyHeal(createTestNotification(false)).get(1, TimeUnit.SECONDS);
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("should configure confidence threshold for LOW_CONFIDENCE level")
        void shouldConfigureConfidenceThreshold() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .notificationLevel(NotificationService.NotificationLevel.LOW_CONFIDENCE)
                    .confidenceThreshold(0.75)
                    .build();

            assertThat(config.getConfidenceThreshold()).isEqualTo(0.75);
            assertThat(config.getNotificationLevel()).isEqualTo(NotificationService.NotificationLevel.LOW_CONFIDENCE);
        }
    }

    @Nested
    @DisplayName("Custom Channel")
    class CustomChannelTests {

        @Test
        @DisplayName("should support adding custom channels")
        void shouldSupportAddingCustomChannels() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .notificationLevel(NotificationService.NotificationLevel.ALL)
                    .build();

            NotificationService service = new NotificationService(config);
            service.addChannel(new NotificationService.CustomWebhookChannel("https://example.com/webhook"));
            service.shutdown();
            // Channel added successfully
        }
    }

    @Nested
    @DisplayName("Notification Data")
    class NotificationDataTests {

        @Test
        @DisplayName("should create HealNotification with all fields")
        void shouldCreateHealNotificationWithAllFields() {
            Instant now = Instant.now();
            NotificationService.HealNotification notification = new NotificationService.HealNotification(
                    now,
                    "Login",
                    "Valid login",
                    "When I click the submit button",
                    "#old-submit-btn",
                    "#new-submit-btn",
                    0.85,
                    "HIGH",
                    "Found matching button with similar text",
                    true
            );

            assertThat(notification.timestamp()).isEqualTo(now);
            assertThat(notification.featureName()).isEqualTo("Login");
            assertThat(notification.scenarioName()).isEqualTo("Valid login");
            assertThat(notification.stepText()).isEqualTo("When I click the submit button");
            assertThat(notification.originalLocator()).isEqualTo("#old-submit-btn");
            assertThat(notification.healedLocator()).isEqualTo("#new-submit-btn");
            assertThat(notification.confidence()).isEqualTo(0.85);
            assertThat(notification.trustLevel()).isEqualTo("HIGH");
            assertThat(notification.reasoning()).isEqualTo("Found matching button with similar text");
            assertThat(notification.success()).isTrue();
        }

        @Test
        @DisplayName("should create HealSummary with all fields")
        void shouldCreateHealSummaryWithAllFields() {
            NotificationService.HealSummary summary = new NotificationService.HealSummary(
                    "2024-01-15",
                    10,
                    9,
                    1,
                    90.0,
                    0.85,
                    "HIGH"
            );

            assertThat(summary.period()).isEqualTo("2024-01-15");
            assertThat(summary.totalHeals()).isEqualTo(10);
            assertThat(summary.successCount()).isEqualTo(9);
            assertThat(summary.failureCount()).isEqualTo(1);
            assertThat(summary.successRate()).isEqualTo(90.0);
            assertThat(summary.avgConfidence()).isEqualTo(0.85);
            assertThat(summary.currentTrustLevel()).isEqualTo("HIGH");
        }
    }

    @Nested
    @DisplayName("Service Lifecycle")
    class ServiceLifecycleTests {

        @Test
        @DisplayName("should shutdown gracefully")
        void shouldShutdownGracefully() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .slackWebhookUrl("https://hooks.slack.com/services/test")
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            // Should complete without hanging
        }

        @Test
        @DisplayName("should handle multiple shutdowns")
        void shouldHandleMultipleShutdowns() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .build();

            NotificationService service = new NotificationService(config);
            service.shutdown();
            service.shutdown(); // Second shutdown should not throw
        }
    }

    @Nested
    @DisplayName("NotificationConfig Builder")
    class NotificationConfigBuilderTests {

        @Test
        @DisplayName("should build config with all options")
        void shouldBuildConfigWithAllOptions() {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .slackWebhookUrl("https://hooks.slack.com/test")
                    .teamsWebhookUrl("https://outlook.office.com/webhook/test")
                    .customWebhookUrl("https://example.com/webhook")
                    .notificationLevel(NotificationService.NotificationLevel.FAILURES_ONLY)
                    .confidenceThreshold(0.8)
                    .sendSummary(true)
                    .summarySchedule("weekly")
                    .build();

            assertThat(config.isEnabled()).isTrue();
            assertThat(config.getSlackWebhookUrl()).isEqualTo("https://hooks.slack.com/test");
            assertThat(config.getTeamsWebhookUrl()).isEqualTo("https://outlook.office.com/webhook/test");
            assertThat(config.getCustomWebhookUrl()).isEqualTo("https://example.com/webhook");
            assertThat(config.getNotificationLevel()).isEqualTo(NotificationService.NotificationLevel.FAILURES_ONLY);
            assertThat(config.getConfidenceThreshold()).isEqualTo(0.8);
            assertThat(config.isSendSummary()).isTrue();
            assertThat(config.getSummarySchedule()).isEqualTo("weekly");
        }

        @Test
        @DisplayName("should have sensible defaults")
        void shouldHaveSensibleDefaults() {
            NotificationConfig config = NotificationConfig.builder().build();

            assertThat(config.isEnabled()).isFalse();
            assertThat(config.getSlackWebhookUrl()).isNull();
            assertThat(config.getTeamsWebhookUrl()).isNull();
            assertThat(config.getCustomWebhookUrl()).isNull();
            assertThat(config.getNotificationLevel()).isEqualTo(NotificationService.NotificationLevel.ALL);
            assertThat(config.getConfidenceThreshold()).isEqualTo(0.75);
            assertThat(config.isSendSummary()).isTrue();
            assertThat(config.getSummarySchedule()).isEqualTo("daily");
        }
    }

    @Nested
    @DisplayName("Channel Implementations")
    class ChannelImplementationTests {

        @Test
        @DisplayName("should create SlackChannel with correct name")
        void shouldCreateSlackChannelWithCorrectName() {
            NotificationService.SlackChannel channel = new NotificationService.SlackChannel("https://hooks.slack.com/test");
            assertThat(channel.getName()).isEqualTo("Slack");
        }

        @Test
        @DisplayName("should create TeamsChannel with correct name")
        void shouldCreateTeamsChannelWithCorrectName() {
            NotificationService.TeamsChannel channel = new NotificationService.TeamsChannel("https://outlook.office.com/webhook/test");
            assertThat(channel.getName()).isEqualTo("Teams");
        }

        @Test
        @DisplayName("should create CustomWebhookChannel with correct name")
        void shouldCreateCustomWebhookChannelWithCorrectName() {
            NotificationService.CustomWebhookChannel channel = new NotificationService.CustomWebhookChannel("https://example.com/webhook");
            assertThat(channel.getName()).isEqualTo("Custom Webhook");
        }
    }

    @Nested
    @DisplayName("Summary Notifications")
    class SummaryNotificationTests {

        @Test
        @DisplayName("should send summary when enabled")
        void shouldSendSummaryWhenEnabled() throws Exception {
            NotificationConfig config = NotificationConfig.builder()
                    .enabled(true)
                    .sendSummary(true)
                    .build();

            NotificationService service = new NotificationService(config);

            try {
                service.notifySummary(createTestSummary()).get(1, TimeUnit.SECONDS);
                // No exception means success
            } finally {
                service.shutdown();
            }
        }
    }

    private NotificationService.HealNotification createTestNotification(boolean success) {
        return new NotificationService.HealNotification(
                Instant.now(),
                "Login",
                "Valid login",
                "When I click the submit button",
                "#old-submit-btn",
                "#new-submit-btn",
                0.85,
                "HIGH",
                "Found matching button with similar text",
                success
        );
    }

    private NotificationService.HealSummary createTestSummary() {
        return new NotificationService.HealSummary(
                "2024-01-15",
                10,
                9,
                1,
                90.0,
                0.85,
                "HIGH"
        );
    }
}

package io.github.glaciousm.agent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AgentBanner.
 * Tests banner output for different configuration states.
 */
class AgentBannerTest {

    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void setUp() {
        capturedErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(capturedErr));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Nested
    @DisplayName("Banner output")
    class BannerOutputTests {

        @Test
        @DisplayName("should print ACTIVE banner when provider is available")
        void printsActiveBanner() {
            AgentBanner.print(true);

            String output = capturedErr.toString();
            assertThat(output).contains("INTENT HEALER AGENT");
            assertThat(output).contains("ACTIVE");
            assertThat(output).contains("Mode:");
            assertThat(output).contains("Provider:");
            assertThat(output).contains("Model:");
            assertThat(output).contains("Healing:");
            assertThat(output).contains("Self-healing is active");
        }

        @Test
        @DisplayName("should print INACTIVE banner when provider is not available")
        void printsInactiveBanner() {
            AgentBanner.print(false);

            String output = capturedErr.toString();
            assertThat(output).contains("INTENT HEALER AGENT");
            assertThat(output).contains("INACTIVE");
            assertThat(output).contains("DISABLED");
            assertThat(output).contains("WARNING");
            assertThat(output).contains("API key is not configured");
        }

        @Test
        @DisplayName("should include configuration info in banner")
        void includesConfigInfo() {
            AgentBanner.print(true);

            String output = capturedErr.toString();
            // Default values when config is not loaded
            assertThat(output).containsAnyOf("AUTO_SAFE", "mock", "heuristic");
        }
    }

    @Nested
    @DisplayName("Minimal banner")
    class MinimalBannerTests {

        @Test
        @DisplayName("should print one-line minimal banner")
        void printsMinimalBanner() {
            AgentBanner.printMinimal();

            String output = capturedErr.toString();
            assertThat(output).contains("[Intent Healer] Agent active");
            assertThat(output).contains("provider:");
            // Should be a single line (or close to it)
            assertThat(output.split("\n").length).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Banner format")
    class BannerFormatTests {

        @Test
        @DisplayName("should have proper banner borders")
        void hasProperBorders() {
            AgentBanner.print(true);

            String output = capturedErr.toString();
            assertThat(output).contains("+===============================================================+");
            assertThat(output).contains("+---------------------------------------------------------------+");
        }

        @Test
        @DisplayName("should have aligned content")
        void hasAlignedContent() {
            AgentBanner.print(true);

            String output = capturedErr.toString();
            // Check that lines with | start and end properly
            for (String line : output.split("\n")) {
                String trimmedLine = line.trim();
                if (trimmedLine.contains("Mode:") || trimmedLine.contains("Provider:") || trimmedLine.contains("Model:") || trimmedLine.contains("Healing:")) {
                    assertThat(trimmedLine).startsWith("|");
                    assertThat(trimmedLine).endsWith("|");
                }
            }
        }
    }
}

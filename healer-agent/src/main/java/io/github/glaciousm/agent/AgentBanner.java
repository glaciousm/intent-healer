package io.github.glaciousm.agent;

import io.github.glaciousm.core.config.HealerConfig;
import io.github.glaciousm.core.config.LlmConfig;

/**
 * Displays a startup banner when the Intent Healer agent is loaded.
 *
 * <p>The banner shows key configuration information to help users
 * verify that the agent is active and properly configured.</p>
 */
public class AgentBanner {

    private static final String BANNER_TOP    = "+===============================================================+";
    private static final String BANNER_SEP    = "+---------------------------------------------------------------+";
    private static final String BANNER_BOTTOM = "+===============================================================+";

    /**
     * Print the startup banner to System.err (so it's visible in Surefire output).
     *
     * @param providerAvailable whether the configured provider is available (has API key, etc.)
     */
    public static void print(boolean providerAvailable) {
        HealerConfig config = AutoConfigurator.getConfig();

        String mode = "AUTO_SAFE";
        String provider = "mock";
        String model = "heuristic";
        String healing = providerAvailable ? "ENABLED" : "DISABLED (missing API key)";

        if (config != null) {
            if (config.getMode() != null) {
                mode = config.getMode().name();
            }
            LlmConfig llm = config.getLlm();
            if (llm != null) {
                if (llm.getProvider() != null) {
                    provider = llm.getProvider();
                }
                if (llm.getModel() != null) {
                    model = llm.getModel();
                }
            }
        }

        // Use System.err so the banner is visible in Surefire forked JVM output
        System.err.println();
        System.err.println(BANNER_TOP);
        if (providerAvailable) {
            System.err.println("|           INTENT HEALER AGENT - ACTIVE                        |");
        } else {
            System.err.println("|           INTENT HEALER AGENT - INACTIVE                      |");
        }
        System.err.println(BANNER_SEP);
        System.err.printf("|  Mode:       %-48s |%n", mode);
        System.err.printf("|  Provider:   %-48s |%n", provider);
        System.err.printf("|  Model:      %-48s |%n", model);
        System.err.printf("|  Healing:    %-48s |%n", healing);
        System.err.println(BANNER_BOTTOM);
        System.err.println();
        if (providerAvailable) {
            System.err.println("  Self-healing is active for all WebDriver instances.");
            System.err.println("  Broken locators will be automatically fixed at runtime.");
        } else {
            System.err.println("  WARNING: Healing is DISABLED because the API key is not configured.");
            System.err.println("  Set the required environment variable (e.g., AZURE_OPENAI_API_KEY)");
            System.err.println("  or switch to 'mock' provider in healer-config.yml");
        }
        System.err.println();
        System.err.flush();
    }

    /**
     * Print the startup banner (default - checks provider availability).
     */
    public static void print() {
        print(AutoConfigurator.isEnabled());
    }

    /**
     * Print a minimal banner (one line).
     */
    public static void printMinimal() {
        HealerConfig config = AutoConfigurator.getConfig();
        String provider = config != null && config.getLlm() != null
                ? config.getLlm().getProvider()
                : "mock";
        System.err.println("[Intent Healer] Agent active - provider: " + provider);
        System.err.flush();
    }
}

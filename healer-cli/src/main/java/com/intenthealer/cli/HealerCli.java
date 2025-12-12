package com.intenthealer.cli;

import com.intenthealer.core.config.ConfigLoader;
import com.intenthealer.core.config.HealerConfig;

/**
 * Command-line interface for Intent Healer utilities.
 */
public class HealerCli {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];

        switch (command) {
            case "config" -> showConfig();
            case "validate" -> validateConfig();
            case "version" -> printVersion();
            case "help" -> printUsage();
            default -> {
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
            }
        }
    }

    private static void showConfig() {
        try {
            HealerConfig config = new ConfigLoader().load();
            System.out.println("Current Configuration:");
            System.out.println("  Mode: " + config.getMode());
            System.out.println("  Enabled: " + config.isEnabled());
            System.out.println("  LLM Provider: " + config.getLlm().getProvider());
            System.out.println("  LLM Model: " + config.getLlm().getModel());
            System.out.println("  Confidence Threshold: " + config.getLlm().getConfidenceThreshold());
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void validateConfig() {
        try {
            HealerConfig config = new ConfigLoader().load();
            config.validate();
            System.out.println("Configuration is valid.");
        } catch (Exception e) {
            System.err.println("Configuration validation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printVersion() {
        System.out.println("Intent Healer v1.0.0-SNAPSHOT");
    }

    private static void printUsage() {
        System.out.println("""
            Intent Healer CLI

            Usage: healer <command>

            Commands:
              config    Show current configuration
              validate  Validate configuration file
              version   Show version information
              help      Show this help message

            For more information, see: https://github.com/intenthealer/intent-healer
            """);
    }
}

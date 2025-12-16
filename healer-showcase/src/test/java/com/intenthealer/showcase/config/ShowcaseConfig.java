package com.intenthealer.showcase.config;

import com.intenthealer.core.config.ConfigLoader;
import com.intenthealer.core.config.HealerConfig;
import com.intenthealer.core.engine.HealingEngine;
import com.intenthealer.core.model.IntentContract;
import com.intenthealer.llm.LlmOrchestrator;
import com.intenthealer.selenium.driver.HealingWebDriver;
import com.intenthealer.selenium.snapshot.SnapshotBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for Intent Healer Showcase.
 *
 * This class demonstrates how to integrate Intent Healer into any
 * Selenium-based test automation project.
 *
 * Key integration points:
 * 1. Load HealerConfig from YAML or programmatically
 * 2. Create HealingEngine with config
 * 3. Set up LLM provider (mock for demo, real for production)
 * 4. Wrap WebDriver with HealingWebDriver
 */
public class ShowcaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(ShowcaseConfig.class);
    private static ShowcaseConfig instance;

    private final HealerConfig config;
    private final HealingEngine healingEngine;
    private final LlmOrchestrator llmOrchestrator;
    private HealingWebDriver driver;

    private ShowcaseConfig() {
        // Load configuration from YAML file or use defaults
        this.config = new ConfigLoader().load();

        // Initialize LLM orchestrator
        this.llmOrchestrator = new LlmOrchestrator();

        // Initialize healing engine
        this.healingEngine = new HealingEngine(config);

        logger.info("Intent Healer Showcase initialized");
        logger.info("Mode: {}", config.getMode());
        logger.info("LLM Provider: {}", config.getLlm().getProvider());
    }

    public static synchronized ShowcaseConfig getInstance() {
        if (instance == null) {
            instance = new ShowcaseConfig();
        }
        return instance;
    }

    /**
     * Creates a HealingWebDriver that wraps a standard WebDriver.
     *
     * This is the key integration point - instead of using WebDriver directly,
     * you wrap it with HealingWebDriver to enable self-healing.
     */
    public HealingWebDriver createDriver() {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        WebDriver chromeDriver = new ChromeDriver(options);
        this.driver = new HealingWebDriver(chromeDriver, healingEngine, config);

        // Configure healing engine with snapshot capture and LLM evaluator
        // IMPORTANT: Use the base chromeDriver for snapshots, not the HealingWebDriver
        SnapshotBuilder snapshotBuilder = new SnapshotBuilder(chromeDriver);
        healingEngine.setSnapshotCapture(failure -> snapshotBuilder.captureAll());
        healingEngine.setLlmEvaluator((failure, snapshot) -> {
            IntentContract intent = IntentContract.defaultContract(failure.getStepText());
            return llmOrchestrator.evaluateCandidates(failure, snapshot, intent, config.getLlm());
        });

        printBanner();

        return driver;
    }

    private void printBanner() {
        System.out.println("============================================================");
        System.out.println("  Intent Healer - Self-Healing Selenium Framework");
        System.out.println("============================================================");
        System.out.println("  Mode:          " + config.getMode());
        System.out.println("  LLM Provider:  " + config.getLlm().getProvider());
        System.out.println("  Auto-Healing:  ENABLED");
        System.out.println("============================================================");
        System.out.println();
    }

    public HealingWebDriver getDriver() {
        if (driver == null) {
            return createDriver();
        }
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public HealerConfig getConfig() {
        return config;
    }

    public static void reset() {
        if (instance != null) {
            instance.quitDriver();
            instance = null;
        }
    }
}

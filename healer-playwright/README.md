# Intent Healer Playwright Integration

[![Playwright](https://img.shields.io/badge/Playwright-1.40.0+-brightgreen.svg)](https://playwright.dev/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/)

> **Self-healing locators for Playwright Java** - Automatic locator recovery when elements change in the DOM.

---

## Overview

The `healer-playwright` module provides self-healing capabilities for Playwright-based test automation. When a locator fails to find an element, Intent Healer automatically:

1. **Captures** the current page state (DOM snapshot)
2. **Analyzes** the failure context and element characteristics
3. **Finds** the correct element using AI-powered matching
4. **Heals** the locator transparently - tests continue without interruption

---

## Features

| Feature | Description |
|---------|-------------|
| **HealingPage** | Page wrapper with automatic healing on locator failures |
| **HealingLocator** | Locator wrapper with self-healing interactions |
| **PlaywrightSnapshotBuilder** | Captures UI state for LLM analysis |
| **Thread-safe Context** | ThreadLocal-based intent management for parallel execution |
| **Intent Support** | Full support for @Intent, @Invariant, @Outcome annotations |

---

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.glaciousm</groupId>
    <artifactId>healer-playwright</artifactId>
    <version>1.0.4</version>
</dependency>
```

---

## Quick Start

### Basic Usage

```java
import io.github.glaciousm.playwright.HealingPage;
import com.microsoft.playwright.*;

// Create Playwright browser and page
Playwright playwright = Playwright.create();
Browser browser = playwright.chromium().launch();
Page page = browser.newPage();

// Wrap with HealingPage
HealingPage healingPage = new HealingPage(page, healingEngine, config);

// Navigate and interact - healing happens automatically
healingPage.navigate("https://example.com");
healingPage.locator("#submit-btn").click();  // Auto-heals if locator fails
```

### With Intent Context

```java
// Set intent context for smarter healing
IntentContract intent = IntentContract.builder()
    .action("CLICK")
    .description("Submit the login form")
    .build();

healingPage.setIntent(intent);
healingPage.locator("#submit-btn").click();
healingPage.clearIntent();
```

---

## Key Classes

### HealingPage

Wraps Playwright's `Page` with healing capabilities:

```java
HealingPage healingPage = new HealingPage(page, healingEngine, config);

// All standard Page methods work normally
healingPage.navigate("https://example.com");
healingPage.waitForLoadState();

// Locators are automatically wrapped with healing
HealingLocator button = healingPage.locator("button.submit");
button.click();  // Self-heals if locator fails
```

### HealingLocator

Wraps Playwright's `Locator` with automatic healing:

```java
HealingLocator locator = healingPage.locator("#login-btn");

// These operations trigger healing on failure:
locator.click();
locator.fill("username");
locator.check();
locator.selectOption("value");
locator.textContent();
```

### PlaywrightSnapshotBuilder

Captures page state for LLM analysis:

```java
PlaywrightSnapshotBuilder builder = new PlaywrightSnapshotBuilder(page);
UiSnapshot snapshot = builder
    .maxElements(100)
    .includeHidden(false)
    .captureScreenshot(true)
    .captureAll();
```

---

## Configuration

Create `healer-config.yml`:

```yaml
healer:
  mode: AUTO_SAFE
  enabled: true

  llm:
    provider: ollama          # or openai, anthropic
    model: llama3.1
    base_url: http://localhost:11434

  guardrails:
    min_confidence: 0.75
    max_heals_per_scenario: 10

  snapshot:
    max_elements: 100
    include_hidden: false
    capture_screenshot: true
```

---

## Healing Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     Playwright Test                             │
├─────────────────────────────────────────────────────────────────┤
│  1. Test calls: healingPage.locator("#submit").click()         │
│                              │                                  │
│                              ▼                                  │
│  2. HealingLocator attempts click on delegate                   │
│                              │                                  │
│                              ▼                                  │
│  3. PlaywrightException thrown (element not found)              │
│                              │                                  │
│                              ▼                                  │
│  4. PlaywrightSnapshotBuilder captures DOM state                │
│                              │                                  │
│                              ▼                                  │
│  5. HealingEngine finds correct element via LLM                 │
│                              │                                  │
│                              ▼                                  │
│  6. New locator returned - click succeeds!                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Comparison with Selenium Integration

| Aspect | healer-selenium | healer-playwright |
|--------|-----------------|-------------------|
| Browser Engine | WebDriver protocol | CDP / WebSocket |
| Wrapper Class | HealingWebDriver | HealingPage |
| Element Type | HealingWebElement | HealingLocator |
| Auto-wait | Manual waits | Built-in auto-wait |
| Network Interception | Limited | Full support |
| Parallel Execution | ThreadLocal | ThreadLocal |

---

## Thread Safety

The module uses ThreadLocal for intent context management, making it safe for parallel test execution:

```java
// Each thread has its own intent context
ExecutorService executor = Executors.newFixedThreadPool(4);

executor.submit(() -> {
    HealingPage page1 = new HealingPage(createPage(), engine, config);
    page1.setIntent(intent1);  // Only affects this thread
    page1.locator("#btn").click();
});

executor.submit(() -> {
    HealingPage page2 = new HealingPage(createPage(), engine, config);
    page2.setIntent(intent2);  // Only affects this thread
    page2.locator("#btn").click();
});
```

---

## Requirements

- **Java** 21 or later
- **Playwright** 1.40.0 or later
- **Intent Healer Core** 1.0.4 or later
- **Intent Healer LLM** 1.0.4 or later

---

## License

This module is part of the Intent Healer framework and is licensed under AGPL-3.0.

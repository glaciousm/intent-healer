# Intent-Based Self-Healing for Selenium + Java + Cucumber
## LLM-Powered Semantic Test Recovery

---

## Executive Summary

Build an LLM-powered execution layer for Cucumber/Selenium that automatically recovers from UI changes by understanding test intent semantically. The system heals locator drift, text changes, and minor flow variations across any language or terminology—while refusing to mask true product regressions.

**Why LLM is required:** Deterministic approaches (synonym lists, string similarity) fail immediately when facing multiple languages, branded terminology, icon-only buttons, or any semantic complexity. Only an LLM can answer "do these two elements serve the same purpose?" reliably.

---

## Table of Contents

1. [Problem Statement](#1-problem-statement)
2. [Solution Overview](#2-solution-overview)
3. [Target Users and Value Proposition](#3-target-users-and-value-proposition)
4. [Core Concepts](#4-core-concepts)
5. [LLM Integration Architecture](#5-llm-integration-architecture)
6. [Healing Scope and Guardrails](#6-healing-scope-and-guardrails)
7. [System Architecture](#7-system-architecture)
8. [Detailed Technical Design](#8-detailed-technical-design)
9. [Prompt Engineering](#9-prompt-engineering)
10. [Public API](#10-public-api)
11. [Cost and Performance](#11-cost-and-performance)
12. [Security and Privacy](#12-security-and-privacy)
13. [Roadmap](#13-roadmap)
14. [Engineering Backlog](#14-engineering-backlog)
15. [Validation Strategy](#15-validation-strategy)
16. [Repository Structure](#16-repository-structure)
17. [Implementation Guidelines](#17-implementation-guidelines)

---

## 1. Problem Statement

### The Locator Maintenance Burden

Selenium test suites break constantly due to UI changes that don't affect actual functionality:

- Button IDs renamed during refactoring
- CSS classes changed by design system updates
- DOM structure reorganized
- Button text updated ("Login" → "Sign in")
- Localization changes across languages
- Component library migrations

**Impact:** Teams spend 10-30% of automation effort just maintaining locators. Releases get delayed by false failures. Trust in automation erodes.

### Why Deterministic Solutions Fail

Previous approaches tried hardcoded rules:

```java
// This approach is fundamentally broken
Map<String, List<String>> synonyms = Map.of(
    "login", List.of("sign in", "log in")
);
```

**Problems:**

| Scenario | Deterministic Approach | Result |
|----------|----------------------|--------|
| Japanese site: ログイン → サインイン | No Japanese synonyms | ❌ Fails |
| Icon-only button: 🔒 | No text to match | ❌ Fails |
| Branded text: "Yalla!" (submit) | Not in synonym list | ❌ Fails |
| "Add to Bag" vs "Add to Cart" | Requires retail domain knowledge | ❌ Fails |
| "Adjudicate" vs "Review" vs "Process" | Industry-specific | ❌ Fails |
| New terminology not in list | Requires constant maintenance | ❌ Fails |

**The only solution:** A system that actually understands meaning—an LLM.

---

## 2. Solution Overview

### Core Insight

Replace deterministic matching with semantic understanding:

```
Original: Click button with id="login-btn", text="Login"
Page now has: <button id="auth-submit">サインイン</button>

Deterministic: "Login" vs "サインイン" → Levenshtein distance 1.0 → No match

LLM: "Does 'サインイン' serve the same purpose as 'Login'?" → "Yes, both mean sign-in/authenticate"
```

### How It Works

1. **Test fails** with `NoSuchElementException` or similar
2. **Capture context:** step text, original locator, exception, page state
3. **Build UI snapshot:** all interactive elements with attributes, text, ARIA, position
4. **Ask LLM:** "Given what this test step intended to do, which element on the current page serves that purpose?"
5. **Validate LLM choice:** confidence threshold, safety checks, outcome validation
6. **Execute and verify:** try the healed action, confirm expected outcome
7. **Report:** full audit trail with LLM reasoning

### What Makes This Different

| Aspect | Deterministic Tools | This Solution |
|--------|-------------------|---------------|
| Language support | English only | Any language |
| Semantic understanding | None | Full |
| Novel terminology | Fails | Handles naturally |
| Icon-only elements | Blind | Understands from context |
| Explainability | Feature scores | Natural language reasoning |
| Maintenance | Constant rule updates | Zero |

---

## 3. Target Users and Value Proposition

### Primary Users

1. **QA Automation Engineers** — own large Selenium/Cucumber suites
2. **Release Managers** — frustrated by test failures blocking deployments
3. **QA Leads** — need governance and audit trails for test modifications

### Value Proposition

| Benefit | Measurable Outcome |
|---------|-------------------|
| Reduced maintenance | 50-70% fewer locator-fix commits |
| Faster releases | Eliminate hours of false-failure investigation |
| Global support | Test any language without extra configuration |
| Full auditability | Every heal explained in natural language |
| Preserved correctness | Guardrails prevent masking real bugs |

### Ideal Customer Profile

- 200+ automated scenarios
- Frequent UI updates (weekly+ deployments)
- Multi-language or international products
- Mature QA practice willing to review heal reports
- CI pipelines where test failures block releases

---

## 4. Core Concepts

### 4.1 Intent Contract

Each test step represents an **Intent**—what the user is trying to accomplish:

```java
@Intent(
    action = "authenticate_user",
    description = "Log into the application with provided credentials",
    outcomeCheck = DashboardVisible.class,
    invariants = { NoErrorBanner.class, NotOnLoginPage.class },
    healPolicy = HealPolicy.AUTO_SAFE
)
public void user_logs_in(String username, String password) {
    // implementation
}
```

**Components:**

| Component | Purpose | Example |
|-----------|---------|---------|
| Action | Semantic name for what's happening | `authenticate_user`, `add_to_cart`, `submit_payment` |
| Description | Natural language explanation (fed to LLM) | "Log into the application with provided credentials" |
| Outcome Check | What must be true after successful action | Dashboard visible, cart count increased |
| Invariants | What must never happen | Error banners, forbidden redirects |
| Heal Policy | Whether/how healing is allowed | OFF, SUGGEST, AUTO_SAFE, AUTO_ALL |

### 4.2 UI Snapshot

Structured representation of the current page state:

```json
{
  "url": "https://app.example.com/login",
  "title": "Sign In - Example App",
  "language_detected": "ja",
  "interactive_elements": [
    {
      "index": 0,
      "tag": "button",
      "type": "submit",
      "id": "auth-submit",
      "classes": ["btn", "btn-primary"],
      "text": "サインイン",
      "aria_label": null,
      "aria_role": "button",
      "placeholder": null,
      "name": "submit",
      "visible": true,
      "enabled": true,
      "rect": { "x": 200, "y": 400, "width": 120, "height": 40 },
      "container": "form#login-form",
      "nearby_labels": ["メールアドレス", "パスワード"],
      "data_attributes": { "testid": "login-submit" }
    }
  ]
}
```

**Collected Features:**

- Tag name and type
- All attributes (id, name, class, data-*)
- Visible text content (normalized)
- ARIA attributes (role, label, labelledby, describedby)
- Visibility and enabled state
- Bounding rectangle
- Container ancestry (form, modal, section)
- Nearby label text
- Placeholder text for inputs

### 4.3 Failure Context

Everything needed to understand what went wrong:

```json
{
  "step_text": "When the user clicks the login button",
  "step_keyword": "When",
  "scenario_name": "User logs in with valid credentials",
  "feature_name": "Authentication",
  "tags": ["@auth", "@smoke", "@intent:authenticate_user"],
  "exception_type": "NoSuchElementException",
  "exception_message": "Unable to locate element: #login-btn",
  "original_locator": {
    "strategy": "css",
    "value": "#login-btn"
  },
  "action_type": "click",
  "intent_metadata": {
    "action": "authenticate_user",
    "description": "Log into the application"
  }
}
```

### 4.4 Heal Decision

The LLM's output, structured for validation:

```json
{
  "can_heal": true,
  "confidence": 0.92,
  "selected_element_index": 0,
  "reasoning": "The step intends to click a login/sign-in button. Element 0 is a submit button with text 'サインイン' (Japanese for 'Sign in') inside the login form. This serves the same authentication purpose as the original 'login-btn' button.",
  "alternative_indices": [3],
  "warnings": [],
  "refusal_reason": null
}
```

---

## 5. LLM Integration Architecture

### 5.1 Supported Providers

| Provider | Model | Use Case | Latency | Cost |
|----------|-------|----------|---------|------|
| OpenAI | gpt-4o | Production, highest accuracy | ~2s | $$$ |
| OpenAI | gpt-4o-mini | Cost-sensitive production | ~1s | $ |
| Anthropic | claude-sonnet-4-20250514 | Production, strong reasoning | ~2s | $$ |
| Anthropic | claude-haiku | High-volume, cost-sensitive | ~0.8s | $ |
| Local | Llama 3.1 70B | Air-gapped environments | ~3s | Infrastructure |
| Local | Mistral 7B | Fast local inference | ~1s | Infrastructure |
| Azure OpenAI | gpt-4o | Enterprise compliance | ~2s | $$$ |
| AWS Bedrock | Claude/Titan | AWS-native deployments | ~2s | $$ |

### 5.2 Provider Abstraction

```java
public interface LlmProvider {
    
    HealDecision evaluateCandidates(
        FailureContext failure,
        UiSnapshot snapshot,
        LlmConfig config
    );
    
    String explainElement(
        ElementSnapshot element,
        String language
    );
    
    boolean validateOutcome(
        String expectedOutcome,
        UiSnapshot beforeSnapshot,
        UiSnapshot afterSnapshot
    );
}
```

### 5.3 Configuration

```yaml
# healer-config.yml

llm:
  provider: openai  # openai | anthropic | azure | bedrock | local
  model: gpt-4o-mini
  
  # API configuration
  api_key_env: OPENAI_API_KEY  # environment variable name
  base_url: null  # override for proxies or local deployment
  timeout_seconds: 30
  max_retries: 2
  
  # Cost controls
  max_tokens_per_request: 2000
  max_requests_per_test_run: 100
  max_cost_per_run_usd: 5.00
  
  # Quality controls  
  temperature: 0.1  # low temperature for consistency
  confidence_threshold: 0.80  # minimum confidence to heal
  require_reasoning: true  # LLM must explain its choice

# Fallback chain (if primary fails)
llm_fallback:
  - provider: anthropic
    model: claude-haiku
  - provider: local
    model: llama-3.1-8b
```

### 5.4 Request/Response Flow

```
┌─────────────────┐
│   Test Fails    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Build Failure   │
│ Context         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Capture UI      │
│ Snapshot        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Guardrails│──── Destructive action? ──► FAIL (no healing)
│ (Pre-LLM)       │
└────────┬────────┘
         │ Safe to attempt
         ▼
┌─────────────────┐
│ Build LLM       │
│ Prompt          │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Call LLM        │──── Timeout/Error ──► Try fallback provider
│ Provider        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Parse Response  │──── Invalid JSON ──► Retry with repair prompt
│ Validate Schema │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Confidence│──── Below threshold ──► FAIL (low confidence)
│ Threshold       │
└────────┬────────┘
         │ Above threshold
         ▼
┌─────────────────┐
│ Check Guardrails│──── Forbidden element ──► FAIL (guardrail)
│ (Post-LLM)      │
└────────┬────────┘
         │ Passed
         ▼
┌─────────────────┐
│ Execute Healed  │
│ Action          │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validate        │──── Invariant violated ──► FAIL (invariant)
│ Invariants      │──── Outcome not met ──► FAIL (outcome)
└────────┬────────┘
         │ All passed
         ▼
┌─────────────────┐
│ Continue Test   │
│ Record Evidence │
└─────────────────┘
```

### 5.5 Caching Strategy

To reduce costs and latency, implement intelligent caching:

```java
public class HealCache {
    
    // Cache key: hash of (step_text, original_locator, page_url_pattern, element_signatures)
    
    public Optional<CachedHeal> lookup(FailureContext failure, UiSnapshot snapshot) {
        String cacheKey = computeCacheKey(failure, snapshot);
        CachedHeal cached = cache.get(cacheKey);
        
        if (cached != null && !cached.isExpired() && cached.isStillValid(snapshot)) {
            return Optional.of(cached);
        }
        return Optional.empty();
    }
    
    // Cache successful heals for reuse
    public void store(FailureContext failure, UiSnapshot snapshot, HealDecision decision) {
        if (decision.canHeal() && decision.getConfidence() > 0.90) {
            String cacheKey = computeCacheKey(failure, snapshot);
            cache.put(cacheKey, new CachedHeal(decision, Instant.now(), TTL_HOURS));
        }
    }
}
```

**Cache invalidation:**

- TTL-based expiration (default: 24 hours)
- Invalidate when page structure changes significantly
- Invalidate on explicit cache clear command
- Never cache low-confidence heals

---

## 6. Healing Scope and Guardrails

### 6.1 What the System Heals

| Change Type | Example | Healing Approach |
|-------------|---------|------------------|
| Locator drift | `#login-btn` → `#signin-button` | LLM identifies equivalent element |
| Text changes | "Login" → "Sign in" → "サインイン" | LLM understands semantic equivalence |
| Structure changes | Button moved inside new wrapper div | LLM finds by purpose, not position |
| Attribute changes | `name="submit"` → `name="auth-submit"` | LLM matches by function |
| Icon-only buttons | Text removed, icon added | LLM infers from aria-label, context |
| Click interception | Overlay blocking click | Retry strategies + wait for overlay removal |
| Stale elements | SPA re-rendered component | Re-capture snapshot, re-identify element |
| Minor flow variations | New optional tooltip appeared | LLM determines it's not blocking |

### 6.2 What the System Must NOT Heal

| Scenario | Why | System Behavior |
|----------|-----|-----------------|
| Business assertion failures | "Price should be $99" failing is real bug | Never heal, always fail |
| Destructive actions | Delete, cancel, close account | Refuse unless explicitly allowlisted |
| Authentication changes | MFA now required | Fail—flow fundamentally changed |
| Missing required fields | New mandatory field in form | Fail—can't invent data |
| Permission errors | User lacks access | Fail—real issue |
| Validation errors | Form rejected input | Fail—may indicate real bug |
| Low confidence matches | LLM unsure which element | Fail—don't guess |

### 6.3 Guardrail Implementation

```java
public class GuardrailChecker {
    
    private final GuardrailConfig config;
    
    // Pre-LLM guardrails (before even asking LLM)
    public GuardrailResult checkPreLlm(FailureContext failure, IntentContract intent) {
        
        // Never heal assertion steps
        if (failure.getStepKeyword().equals("Then") && intent.isAssertion()) {
            return GuardrailResult.refuse("Assertion steps cannot be healed");
        }
        
        // Never heal if mode is OFF
        if (intent.getHealPolicy() == HealPolicy.OFF) {
            return GuardrailResult.refuse("Healing disabled for this intent");
        }
        
        // Check action against forbidden list
        if (config.isDestructiveAction(failure.getActionType())) {
            if (!intent.isDestructiveAllowed()) {
                return GuardrailResult.refuse("Destructive action not allowed");
            }
        }
        
        return GuardrailResult.proceed();
    }
    
    // Post-LLM guardrails (validate LLM's choice)
    public GuardrailResult checkPostLlm(HealDecision decision, ElementSnapshot chosen, UiSnapshot snapshot) {
        
        // Check confidence threshold
        if (decision.getConfidence() < config.getMinConfidence()) {
            return GuardrailResult.refuse(
                "Confidence %.2f below threshold %.2f",
                decision.getConfidence(), 
                config.getMinConfidence()
            );
        }
        
        // Check for forbidden keywords in chosen element
        String elementText = chosen.getNormalizedText();
        for (String forbidden : config.getForbiddenKeywords()) {
            if (containsKeyword(elementText, forbidden)) {
                return GuardrailResult.refuse(
                    "Chosen element contains forbidden keyword: %s", 
                    forbidden
                );
            }
        }
        
        // Check element is actually interactable
        if (!chosen.isVisible() || !chosen.isEnabled()) {
            return GuardrailResult.refuse("Chosen element is not interactable");
        }
        
        return GuardrailResult.proceed();
    }
}
```

### 6.4 Forbidden Keywords (Default)

Configurable list of terms that prevent automatic healing:

```yaml
guardrails:
  forbidden_keywords:
    # English
    - delete
    - remove
    - cancel
    - close account
    - unsubscribe
    - terminate
    - deactivate
    - permanently
    - irreversible
    
    # Common translations
    - 削除        # Japanese: delete
    - 取り消し    # Japanese: cancel
    - löschen     # German: delete
    - supprimer   # French: delete
    - eliminar    # Spanish: delete
    - удалить     # Russian: delete
    - מחק         # Hebrew: delete
    - حذف         # Arabic: delete
    
    # Note: LLM will also semantically detect destructive actions
    # These keywords are a fast pre-filter before LLM evaluation
```

### 6.5 Execution Modes

| Mode | Behavior | Use Case |
|------|----------|----------|
| `OFF` | No healing attempted | Critical flows, explicit disable |
| `SUGGEST` | Find candidates, report them, but fail the test | Building trust, review period |
| `AUTO_SAFE` | Heal non-destructive actions automatically | Default for most tests |
| `AUTO_ALL` | Heal all allowed actions including risky ones | Explicit opt-in only |

**Mode selection hierarchy:**

1. Step-level annotation (highest priority)
2. Scenario-level tag
3. Feature-level tag
4. Global configuration (lowest priority)

```gherkin
@heal:off
Feature: Payment Processing
  # All scenarios in this feature have healing disabled by default
  
  @heal:suggest
  Scenario: View payment history
    # This scenario will suggest heals but not apply them
    
  @heal:auto_safe
  Scenario: Update billing address
    # This scenario allows auto-healing of safe actions
```

---

## 7. System Architecture

### 7.1 Component Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Test Execution                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Cucumber   │  │   TestNG     │  │    JUnit     │          │
│  │   Runner     │  │   Runner     │  │    Runner    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                 │                 │                   │
│         └─────────────────┼─────────────────┘                   │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Healer Integration Layer                │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │   │
│  │  │  Cucumber   │  │   TestNG    │  │  JUnit          │  │   │
│  │  │  Hooks      │  │  Listeners  │  │  Extensions     │  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────────┘  │   │
│  └─────────────────────────┬───────────────────────────────┘   │
│                            │                                    │
└────────────────────────────┼────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Healer Core                               │
│                                                                 │
│  ┌──────────────────┐    ┌──────────────────┐                  │
│  │  Failure         │    │  Intent          │                  │
│  │  Classifier      │    │  Registry        │                  │
│  └────────┬─────────┘    └────────┬─────────┘                  │
│           │                       │                             │
│           ▼                       ▼                             │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Healing Engine                          │   │
│  │                                                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │   │
│  │  │  Guardrail  │  │  LLM        │  │  Action         │  │   │
│  │  │  Checker    │  │  Orchestrator│  │  Executor      │  │   │
│  │  └─────────────┘  └──────┬──────┘  └─────────────────┘  │   │
│  │                          │                               │   │
│  └──────────────────────────┼───────────────────────────────┘   │
│                             │                                   │
└─────────────────────────────┼───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      LLM Layer                                  │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Provider    │  │  Prompt      │  │  Response            │  │
│  │  Abstraction │  │  Builder     │  │  Parser              │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────────┘  │
│         │                                                       │
│         ▼                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   OpenAI     │  │  Anthropic   │  │  Local (Ollama)      │  │
│  │   Adapter    │  │  Adapter     │  │  Adapter             │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Selenium Layer                               │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │  UI Snapshot     │  │  Resilient       │                    │
│  │  Builder         │  │  Action Executor │                    │
│  └──────────────────┘  └──────────────────┘                    │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │  Element         │  │  Wait            │                    │
│  │  Fingerprinter   │  │  Strategies      │                    │
│  └──────────────────┘  └──────────────────┘                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Reporting Layer                              │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │  Evidence        │  │  Report          │                    │
│  │  Collector       │  │  Generator       │                    │
│  └──────────────────┘  └──────────────────┘                    │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │  JSON Schema     │  │  HTML Template   │                    │
│  │  Validator       │  │  Engine          │                    │
│  └──────────────────┘  └──────────────────┘                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 Package Structure

```
com.intenthealer/
├── core/
│   ├── config/
│   │   ├── HealerConfig.java
│   │   ├── LlmConfig.java
│   │   ├── GuardrailConfig.java
│   │   └── ConfigLoader.java
│   ├── model/
│   │   ├── FailureContext.java
│   │   ├── ExecutionContext.java
│   │   ├── IntentContract.java
│   │   ├── HealDecision.java
│   │   ├── HealResult.java
│   │   └── Evidence.java
│   ├── exception/
│   │   ├── HealingRefusedException.java
│   │   ├── LlmUnavailableException.java
│   │   └── GuardrailViolationException.java
│   └── util/
│       ├── TextNormalizer.java
│       └── JsonUtils.java
│
├── intent/
│   ├── IntentRegistry.java
│   ├── IntentInferrer.java
│   ├── annotations/
│   │   ├── Intent.java
│   │   ├── Outcome.java
│   │   ├── Invariant.java
│   │   └── HealPolicy.java
│   └── checks/
│       ├── OutcomeCheck.java
│       ├── InvariantCheck.java
│       └── builtin/
│           ├── UrlChangedCheck.java
│           ├── ElementVisibleCheck.java
│           ├── NoErrorBannerCheck.java
│           └── TitleContainsCheck.java
│
├── engine/
│   ├── HealingEngine.java
│   ├── HealingOrchestrator.java
│   ├── guardrails/
│   │   ├── GuardrailChecker.java
│   │   ├── PreLlmGuardrails.java
│   │   └── PostLlmGuardrails.java
│   └── execution/
│       ├── ActionExecutor.java
│       ├── OutcomeValidator.java
│       └── RollbackHandler.java
│
├── llm/
│   ├── LlmProvider.java
│   ├── LlmOrchestrator.java
│   ├── PromptBuilder.java
│   ├── ResponseParser.java
│   ├── providers/
│   │   ├── OpenAiProvider.java
│   │   ├── AnthropicProvider.java
│   │   ├── AzureOpenAiProvider.java
│   │   ├── BedrockProvider.java
│   │   └── OllamaProvider.java
│   └── cache/
│       ├── HealCache.java
│       └── CacheKeyBuilder.java
│
├── selenium/
│   ├── snapshot/
│   │   ├── UiSnapshot.java
│   │   ├── ElementSnapshot.java
│   │   ├── SnapshotBuilder.java
│   │   └── ElementFilter.java
│   ├── actions/
│   │   ├── ResilientClick.java
│   │   ├── ResilientType.java
│   │   ├── ResilientSelect.java
│   │   └── RetryStrategy.java
│   ├── fingerprint/
│   │   ├── ElementFingerprint.java
│   │   └── FingerprintMatcher.java
│   └── adapters/
│       ├── WebDriverAdapter.java
│       └── ElementAdapter.java
│
├── cucumber/
│   ├── HealerCucumberPlugin.java
│   ├── StepInterceptor.java
│   ├── ScenarioContext.java
│   └── TagParser.java
│
├── testng/
│   ├── HealerTestNgListener.java
│   └── TestNgContext.java
│
├── junit/
│   ├── HealerJunitExtension.java
│   └── JunitContext.java
│
├── report/
│   ├── ReportGenerator.java
│   ├── EvidenceCollector.java
│   ├── model/
│   │   ├── HealReport.java
│   │   ├── HealEvent.java
│   │   └── ArtifactReference.java
│   ├── json/
│   │   ├── JsonReportWriter.java
│   │   └── ReportSchema.java
│   └── html/
│       ├── HtmlReportWriter.java
│       └── templates/
│
└── cli/
    ├── HealerCli.java
    ├── commands/
    │   ├── ReportCommand.java
    │   ├── ConfigCommand.java
    │   └── CacheClearCommand.java
    └── output/
        └── ConsoleFormatter.java
```

---

## 8. Detailed Technical Design

### 8.1 Configuration Loading

```java
public class ConfigLoader {
    
    private static final String[] CONFIG_LOCATIONS = {
        "healer-config.yml",
        "healer-config.yaml",
        "src/test/resources/healer-config.yml",
        ".healer/config.yml"
    };
    
    public HealerConfig load() {
        HealerConfig config = new HealerConfig();
        
        // 1. Load defaults
        config.applyDefaults();
        
        // 2. Load from file (first found)
        for (String location : CONFIG_LOCATIONS) {
            File file = new File(location);
            if (file.exists()) {
                config.mergeFromFile(file);
                break;
            }
        }
        
        // 3. Override from environment variables
        config.mergeFromEnvironment(System.getenv());
        
        // 4. Override from system properties
        config.mergeFromSystemProperties(System.getProperties());
        
        // 5. Validate
        config.validate();
        
        return config;
    }
}
```

**Default Configuration:**

```yaml
healer:
  mode: AUTO_SAFE
  enabled: true

llm:
  provider: openai
  model: gpt-4o-mini
  temperature: 0.1
  confidence_threshold: 0.80
  timeout_seconds: 30
  max_retries: 2
  max_tokens_per_request: 2000

guardrails:
  min_confidence: 0.80
  forbidden_keywords:
    - delete
    - remove
    - cancel
    - unsubscribe
    - terminate
  max_heal_attempts_per_step: 2
  max_heals_per_scenario: 5

snapshot:
  max_elements: 500
  include_hidden: false
  include_disabled: true
  capture_screenshot: true
  capture_dom: true

cache:
  enabled: true
  ttl_hours: 24
  max_entries: 10000

report:
  output_dir: build/healer-reports
  json_enabled: true
  html_enabled: true
  include_screenshots: true
  include_llm_prompts: false  # Enable for debugging
```

### 8.2 Failure Classification

```java
public class FailureClassifier {
    
    public FailureKind classify(Throwable exception, String stepText) {
        
        if (exception instanceof NoSuchElementException) {
            return FailureKind.ELEMENT_NOT_FOUND;
        }
        
        if (exception instanceof StaleElementReferenceException) {
            return FailureKind.STALE_ELEMENT;
        }
        
        if (exception instanceof ElementClickInterceptedException) {
            return FailureKind.CLICK_INTERCEPTED;
        }
        
        if (exception instanceof ElementNotInteractableException) {
            return FailureKind.NOT_INTERACTABLE;
        }
        
        if (exception instanceof TimeoutException) {
            return FailureKind.TIMEOUT;
        }
        
        if (exception instanceof AssertionError) {
            return FailureKind.ASSERTION_FAILURE;  // Never heal
        }
        
        // Check if it's a wrapped Selenium exception
        if (exception.getCause() != null) {
            return classify(exception.getCause(), stepText);
        }
        
        return FailureKind.UNKNOWN;
    }
    
    public boolean isHealable(FailureKind kind) {
        return switch (kind) {
            case ELEMENT_NOT_FOUND, STALE_ELEMENT, 
                 CLICK_INTERCEPTED, NOT_INTERACTABLE -> true;
            case ASSERTION_FAILURE, UNKNOWN -> false;
            case TIMEOUT -> true;  // May be healable if element moved
        };
    }
}
```

### 8.3 UI Snapshot Builder

```java
public class SnapshotBuilder {
    
    private final WebDriver driver;
    private final SnapshotConfig config;
    
    public UiSnapshot capture(FailureContext failure) {
        UiSnapshot snapshot = new UiSnapshot();
        snapshot.setUrl(driver.getCurrentUrl());
        snapshot.setTitle(driver.getTitle());
        snapshot.setTimestamp(Instant.now());
        snapshot.setDetectedLanguage(detectLanguage());
        
        // Determine which elements to capture based on action type
        List<ElementSnapshot> elements = switch (failure.getActionType()) {
            case CLICK -> captureClickableElements();
            case TYPE -> captureInputElements();
            case SELECT -> captureSelectElements();
            default -> captureAllInteractiveElements();
        };
        
        snapshot.setInteractiveElements(elements);
        
        // Capture artifacts if configured
        if (config.isCaptureScreenshot()) {
            snapshot.setScreenshot(captureScreenshot());
        }
        if (config.isCaptureDom()) {
            snapshot.setDomSnapshot(captureDom());
        }
        
        return snapshot;
    }
    
    private List<ElementSnapshot> captureClickableElements() {
        List<ElementSnapshot> elements = new ArrayList<>();
        int index = 0;
        
        // Find all clickable elements
        String script = """
            return Array.from(document.querySelectorAll(
                'button, a, [role="button"], [role="link"], input[type="submit"], 
                input[type="button"], [onclick], [ng-click], [data-action]'
            )).filter(el => {
                const rect = el.getBoundingClientRect();
                const style = window.getComputedStyle(el);
                return rect.width > 0 && rect.height > 0 && 
                       style.visibility !== 'hidden' &&
                       style.display !== 'none';
            }).slice(0, %d);
            """.formatted(config.getMaxElements());
        
        List<WebElement> webElements = (List<WebElement>) 
            ((JavascriptExecutor) driver).executeScript(script);
        
        for (WebElement el : webElements) {
            elements.add(captureElement(el, index++));
        }
        
        return elements;
    }
    
    private ElementSnapshot captureElement(WebElement element, int index) {
        ElementSnapshot snapshot = new ElementSnapshot();
        snapshot.setIndex(index);
        
        // Basic properties
        snapshot.setTagName(element.getTagName());
        snapshot.setId(element.getAttribute("id"));
        snapshot.setName(element.getAttribute("name"));
        snapshot.setType(element.getAttribute("type"));
        snapshot.setClasses(parseClasses(element.getAttribute("class")));
        
        // Text content
        snapshot.setText(normalizeText(element.getText()));
        snapshot.setValue(element.getAttribute("value"));
        snapshot.setPlaceholder(element.getAttribute("placeholder"));
        
        // Accessibility
        snapshot.setAriaLabel(element.getAttribute("aria-label"));
        snapshot.setAriaLabelledBy(resolveAriaLabelledBy(element));
        snapshot.setAriaDescribedBy(element.getAttribute("aria-describedby"));
        snapshot.setAriaRole(element.getAttribute("role"));
        snapshot.setTitle(element.getAttribute("title"));
        
        // State
        snapshot.setVisible(element.isDisplayed());
        snapshot.setEnabled(element.isEnabled());
        snapshot.setSelected(element.isSelected());
        
        // Geometry
        Rectangle rect = element.getRect();
        snapshot.setRect(new ElementRect(rect.x, rect.y, rect.width, rect.height));
        
        // Context
        snapshot.setContainer(findContainer(element));
        snapshot.setNearbyLabels(findNearbyLabels(element));
        
        // Data attributes
        snapshot.setDataAttributes(captureDataAttributes(element));
        
        return snapshot;
    }
    
    private String findContainer(WebElement element) {
        String script = """
            let el = arguments[0];
            while (el.parentElement) {
                el = el.parentElement;
                if (el.tagName === 'FORM' || el.tagName === 'DIALOG' || 
                    el.tagName === 'SECTION' || el.tagName === 'NAV' ||
                    el.getAttribute('role') === 'dialog' ||
                    el.getAttribute('role') === 'form') {
                    return el.tagName + (el.id ? '#' + el.id : '') + 
                           (el.className ? '.' + el.className.split(' ')[0] : '');
                }
            }
            return 'body';
            """;
        return (String) ((JavascriptExecutor) driver).executeScript(script, element);
    }
    
    private List<String> findNearbyLabels(WebElement element) {
        String script = """
            const el = arguments[0];
            const labels = [];
            
            // Check for associated label
            if (el.id) {
                const label = document.querySelector(`label[for="${el.id}"]`);
                if (label) labels.push(label.textContent.trim());
            }
            
            // Check for wrapping label
            const parentLabel = el.closest('label');
            if (parentLabel) labels.push(parentLabel.textContent.trim());
            
            // Check aria-labelledby
            const labelledBy = el.getAttribute('aria-labelledby');
            if (labelledBy) {
                labelledBy.split(' ').forEach(id => {
                    const labelEl = document.getElementById(id);
                    if (labelEl) labels.push(labelEl.textContent.trim());
                });
            }
            
            // Check nearby text in same container
            const container = el.closest('div, fieldset, section') || el.parentElement;
            if (container) {
                const nearbyText = container.querySelector('h1, h2, h3, h4, legend, p');
                if (nearbyText) labels.push(nearbyText.textContent.trim());
            }
            
            return [...new Set(labels)].slice(0, 5);
            """;
        return (List<String>) ((JavascriptExecutor) driver).executeScript(script, element);
    }
}
```

### 8.4 Healing Engine

```java
public class HealingEngine {
    
    private final GuardrailChecker guardrails;
    private final LlmOrchestrator llm;
    private final ActionExecutor executor;
    private final OutcomeValidator outcomeValidator;
    private final EvidenceCollector evidence;
    private final HealCache cache;
    
    public HealResult attemptHeal(
            FailureContext failure,
            IntentContract intent,
            WebDriver driver) {
        
        // 1. Pre-LLM guardrail check
        GuardrailResult preCheck = guardrails.checkPreLlm(failure, intent);
        if (preCheck.isRefused()) {
            return HealResult.refused(preCheck.getReason());
        }
        
        // 2. Capture UI snapshot
        UiSnapshot snapshot = new SnapshotBuilder(driver, config).capture(failure);
        evidence.recordSnapshot(snapshot);
        
        // 3. Check cache for previous successful heal
        Optional<CachedHeal> cached = cache.lookup(failure, snapshot);
        if (cached.isPresent()) {
            HealDecision cachedDecision = cached.get().getDecision();
            return executeHeal(cachedDecision, snapshot, failure, intent, driver, true);
        }
        
        // 4. Ask LLM for heal decision
        HealDecision decision;
        try {
            decision = llm.evaluateCandidates(failure, snapshot, intent);
            evidence.recordLlmDecision(decision);
        } catch (LlmUnavailableException e) {
            return HealResult.failed("LLM unavailable: " + e.getMessage());
        }
        
        // 5. Check if LLM decided not to heal
        if (!decision.canHeal()) {
            return HealResult.refused(decision.getRefusalReason());
        }
        
        // 6. Execute the heal
        return executeHeal(decision, snapshot, failure, intent, driver, false);
    }
    
    private HealResult executeHeal(
            HealDecision decision,
            UiSnapshot snapshot,
            FailureContext failure,
            IntentContract intent,
            WebDriver driver,
            boolean fromCache) {
        
        // Post-LLM guardrail check
        ElementSnapshot chosenElement = snapshot.getElement(decision.getSelectedElementIndex());
        GuardrailResult postCheck = guardrails.checkPostLlm(decision, chosenElement, snapshot);
        if (postCheck.isRefused()) {
            return HealResult.refused(postCheck.getReason());
        }
        
        // Execute the healed action
        try {
            executor.execute(failure.getActionType(), chosenElement, failure.getActionData(), driver);
        } catch (Exception e) {
            evidence.recordExecutionFailure(e);
            return HealResult.failed("Healed action failed: " + e.getMessage());
        }
        
        // Validate invariants
        for (InvariantCheck invariant : intent.getInvariants()) {
            InvariantResult result = invariant.verify(new ExecutionContext(driver, snapshot));
            if (result.isViolated()) {
                evidence.recordInvariantViolation(result);
                return HealResult.failed("Invariant violated: " + result.getMessage());
            }
        }
        
        // Validate outcome
        if (intent.getOutcomeCheck() != null) {
            OutcomeResult outcomeResult = intent.getOutcomeCheck().verify(
                new ExecutionContext(driver, snapshot)
            );
            if (!outcomeResult.isPassed()) {
                evidence.recordOutcomeFailure(outcomeResult);
                return HealResult.failed("Outcome check failed: " + outcomeResult.getMessage());
            }
        }
        
        // Success! Cache this heal for future use
        if (!fromCache) {
            cache.store(failure, snapshot, decision);
        }
        
        evidence.recordSuccess(decision, chosenElement);
        
        return HealResult.success(
            decision.getSelectedElementIndex(),
            decision.getConfidence(),
            decision.getReasoning(),
            chosenElement
        );
    }
}
```

### 8.5 Action Executor

```java
public class ActionExecutor {
    
    private final WebDriver driver;
    private final RetryConfig retryConfig;
    
    public void execute(
            ActionType action,
            ElementSnapshot targetSnapshot,
            Object actionData,
            WebDriver driver) {
        
        // Re-find the element from snapshot
        WebElement element = refindElement(targetSnapshot, driver);
        
        switch (action) {
            case CLICK -> executeClick(element);
            case TYPE -> executeType(element, (String) actionData);
            case SELECT -> executeSelect(element, (String) actionData);
            case CLEAR -> executeClear(element);
            case HOVER -> executeHover(element);
        }
    }
    
    private void executeClick(WebElement element) {
        // Try strategies in order until one works
        List<ClickStrategy> strategies = List.of(
            this::standardClick,
            this::scrollThenClick,
            this::waitForOverlayThenClick,
            this::actionClick,
            this::jsClickLastResort  // Only if configured
        );
        
        Exception lastException = null;
        for (ClickStrategy strategy : strategies) {
            try {
                strategy.click(element);
                return;  // Success
            } catch (Exception e) {
                lastException = e;
                // Try next strategy
            }
        }
        
        throw new ActionExecutionException("All click strategies failed", lastException);
    }
    
    private void standardClick(WebElement element) {
        element.click();
    }
    
    private void scrollThenClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});",
            element
        );
        Thread.sleep(100);
        element.click();
    }
    
    private void waitForOverlayThenClick(WebElement element) {
        // Wait for any overlay to disappear
        new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(d -> {
                try {
                    element.click();
                    return true;
                } catch (ElementClickInterceptedException e) {
                    return false;
                }
            });
    }
    
    private void actionClick(WebElement element) {
        new Actions(driver).moveToElement(element).click().perform();
    }
    
    private void jsClickLastResort(WebElement element) {
        if (!retryConfig.isJsClickAllowed()) {
            throw new StrategyNotAllowedException("JS click is disabled");
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
    
    private WebElement refindElement(ElementSnapshot snapshot, WebDriver driver) {
        // Try to find by most reliable identifiers first
        
        // 1. Data-testid (most stable)
        if (snapshot.getDataTestId() != null) {
            try {
                return driver.findElement(By.cssSelector(
                    "[data-testid='" + snapshot.getDataTestId() + "']"
                ));
            } catch (NoSuchElementException ignored) {}
        }
        
        // 2. ID (if present and not dynamic-looking)
        if (snapshot.getId() != null && !looksGenerated(snapshot.getId())) {
            try {
                return driver.findElement(By.id(snapshot.getId()));
            } catch (NoSuchElementException ignored) {}
        }
        
        // 3. Unique aria-label
        if (snapshot.getAriaLabel() != null) {
            try {
                return driver.findElement(By.cssSelector(
                    "[aria-label='" + escapeForCss(snapshot.getAriaLabel()) + "']"
                ));
            } catch (NoSuchElementException ignored) {}
        }
        
        // 4. Text content + tag + container (composite)
        if (snapshot.getText() != null && !snapshot.getText().isEmpty()) {
            String xpath = String.format(
                "//%s[normalize-space(text())='%s']",
                snapshot.getTagName(),
                snapshot.getText()
            );
            try {
                return driver.findElement(By.xpath(xpath));
            } catch (NoSuchElementException ignored) {}
        }
        
        // 5. Position-based as last resort (brittle but may work)
        return findByPosition(snapshot, driver);
    }
}
```

---

## 9. Prompt Engineering

### 9.1 Core Healing Prompt

```java
public class PromptBuilder {
    
    public String buildHealingPrompt(FailureContext failure, UiSnapshot snapshot, IntentContract intent) {
        return """
            You are an expert test automation engineer analyzing a UI test failure.
            
            ## Test Context
            
            **Feature:** %s
            **Scenario:** %s
            **Step:** %s %s
            **Intent:** %s
            **Intent Description:** %s
            
            ## Failure Information
            
            **Exception:** %s
            **Original Locator:** %s (strategy: %s)
            **Action:** %s
            
            ## Current Page State
            
            **URL:** %s
            **Title:** %s
            **Detected Language:** %s
            
            ## Available Interactive Elements
            
            %s
            
            ## Your Task
            
            Analyze the test step's intent and the current page state. Determine if there is an element on the current page that serves the same purpose as the original target.
            
            **Important Guidelines:**
            - Focus on SEMANTIC PURPOSE, not exact text matching
            - Consider that the UI may be in any language
            - The element's purpose matters more than its appearance
            - If multiple candidates could work, choose the most likely based on context
            - If no element clearly matches the intent, respond that healing is not possible
            - NEVER suggest elements that could cause destructive actions (delete, remove, cancel) unless the original intent was destructive
            
            ## Response Format
            
            Respond with ONLY a JSON object in this exact format:
            
            ```json
            {
              "can_heal": true|false,
              "confidence": 0.0-1.0,
              "selected_element_index": <index>|null,
              "reasoning": "<2-3 sentences explaining your decision>",
              "alternative_indices": [<other possible indices>],
              "warnings": ["<any concerns about this heal>"],
              "refusal_reason": "<if can_heal is false, explain why>"|null
            }
            ```
            
            Confidence guide:
            - 0.95+: Nearly certain match (same text, clear purpose)
            - 0.85-0.94: High confidence (semantic match, clear context)
            - 0.75-0.84: Moderate confidence (likely match, some ambiguity)
            - Below 0.75: Do not heal, set can_heal to false
            """.formatted(
                failure.getFeatureName(),
                failure.getScenarioName(),
                failure.getStepKeyword(),
                failure.getStepText(),
                intent.getAction(),
                intent.getDescription(),
                failure.getExceptionType(),
                failure.getOriginalLocator().getValue(),
                failure.getOriginalLocator().getStrategy(),
                failure.getActionType(),
                snapshot.getUrl(),
                snapshot.getTitle(),
                snapshot.getDetectedLanguage(),
                formatElementsForPrompt(snapshot.getInteractiveElements())
            );
    }
    
    private String formatElementsForPrompt(List<ElementSnapshot> elements) {
        StringBuilder sb = new StringBuilder();
        for (ElementSnapshot el : elements) {
            sb.append(formatElement(el)).append("\n\n");
        }
        return sb.toString();
    }
    
    private String formatElement(ElementSnapshot el) {
        StringBuilder sb = new StringBuilder();
        sb.append("**[%d]** `<%s>`\n".formatted(el.getIndex(), el.getTagName()));
        
        if (el.getId() != null) sb.append("- id: %s\n".formatted(el.getId()));
        if (el.getName() != null) sb.append("- name: %s\n".formatted(el.getName()));
        if (el.getType() != null) sb.append("- type: %s\n".formatted(el.getType()));
        if (!el.getClasses().isEmpty()) sb.append("- classes: %s\n".formatted(String.join(", ", el.getClasses())));
        if (el.getText() != null && !el.getText().isEmpty()) sb.append("- text: \"%s\"\n".formatted(el.getText()));
        if (el.getAriaLabel() != null) sb.append("- aria-label: \"%s\"\n".formatted(el.getAriaLabel()));
        if (el.getAriaRole() != null) sb.append("- role: %s\n".formatted(el.getAriaRole()));
        if (el.getPlaceholder() != null) sb.append("- placeholder: \"%s\"\n".formatted(el.getPlaceholder()));
        if (el.getTitle() != null) sb.append("- title: \"%s\"\n".formatted(el.getTitle()));
        if (el.getContainer() != null) sb.append("- container: %s\n".formatted(el.getContainer()));
        if (!el.getNearbyLabels().isEmpty()) sb.append("- nearby labels: %s\n".formatted(String.join(", ", el.getNearbyLabels())));
        sb.append("- visible: %s, enabled: %s\n".formatted(el.isVisible(), el.isEnabled()));
        
        return sb.toString();
    }
}
```

### 9.2 Outcome Validation Prompt

For complex outcomes that can't be validated with simple checks:

```java
public String buildOutcomeValidationPrompt(
        String expectedOutcome,
        UiSnapshot beforeSnapshot,
        UiSnapshot afterSnapshot) {
    
    return """
        You are validating whether a test action achieved its expected outcome.
        
        ## Expected Outcome
        %s
        
        ## Before Action
        - URL: %s
        - Title: %s
        - Key elements: %s
        
        ## After Action
        - URL: %s
        - Title: %s
        - Key elements: %s
        
        ## Question
        Based on the before/after state, did the action achieve the expected outcome?
        
        Respond with ONLY:
        ```json
        {
          "outcome_achieved": true|false,
          "confidence": 0.0-1.0,
          "reasoning": "<brief explanation>"
        }
        ```
        """.formatted(
            expectedOutcome,
            beforeSnapshot.getUrl(),
            beforeSnapshot.getTitle(),
            summarizeElements(beforeSnapshot),
            afterSnapshot.getUrl(),
            afterSnapshot.getTitle(),
            summarizeElements(afterSnapshot)
        );
}
```

### 9.3 Prompt Optimization Notes

**Token efficiency:**

- Limit element list to top 50 candidates (pre-filter by relevance)
- Truncate long text content (max 100 chars)
- Omit empty/null fields
- Use compact formatting

**Consistency:**

- Temperature 0.1 for deterministic outputs
- Explicit JSON schema in prompt
- Examples of expected format in system prompt

**Safety:**

- Never include sensitive data (passwords, tokens) in prompts
- Sanitize user-generated content
- Log prompts only when explicitly enabled

---

## 10. Public API

### 10.1 Core Interfaces

```java
/**
 * Primary entry point for healing capabilities.
 */
public interface Healer {
    
    /**
     * Attempt to heal a test failure.
     */
    HealResult heal(FailureContext failure, IntentContract intent, WebDriver driver);
    
    /**
     * Check if a failure is potentially healable (fast pre-check).
     */
    boolean isHealable(FailureContext failure);
    
    /**
     * Get healing configuration.
     */
    HealerConfig getConfig();
}

/**
 * Outcome verification after action execution.
 */
public interface OutcomeCheck {
    
    /**
     * Verify the expected outcome was achieved.
     */
    OutcomeResult verify(ExecutionContext ctx);
    
    /**
     * Human-readable description of this check.
     */
    String getDescription();
}

/**
 * Invariant that must never be violated.
 */
public interface InvariantCheck {
    
    /**
     * Check if invariant is still valid.
     */
    InvariantResult verify(ExecutionContext ctx);
    
    /**
     * Human-readable description of this invariant.
     */
    String getDescription();
}

/**
 * LLM provider abstraction.
 */
public interface LlmProvider {
    
    /**
     * Evaluate candidates and return a heal decision.
     */
    HealDecision evaluateCandidates(
        FailureContext failure,
        UiSnapshot snapshot,
        IntentContract intent,
        LlmConfig config
    );
    
    /**
     * Validate outcome using LLM reasoning.
     */
    OutcomeResult validateOutcome(
        String expectedOutcome,
        UiSnapshot before,
        UiSnapshot after,
        LlmConfig config
    );
    
    /**
     * Provider name for logging/reporting.
     */
    String getProviderName();
}
```

### 10.2 Annotations

```java
/**
 * Declares the semantic intent of a step definition.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Intent {
    
    /**
     * Semantic action identifier (e.g., "authenticate_user", "add_to_cart").
     */
    String action();
    
    /**
     * Human-readable description for LLM context.
     */
    String description() default "";
    
    /**
     * Healing policy for this step.
     */
    HealPolicy policy() default HealPolicy.AUTO_SAFE;
    
    /**
     * Whether this is a destructive action requiring explicit allowlist.
     */
    boolean destructive() default false;
}

/**
 * Declares expected outcome checks for a step.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Outcome {
    
    /**
     * Outcome check classes to run after action.
     */
    Class<? extends OutcomeCheck>[] checks() default {};
    
    /**
     * Simple text description for LLM-based validation.
     */
    String description() default "";
}

/**
 * Declares invariants that must hold during/after a step.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Invariants.class)
public @interface Invariant {
    
    /**
     * Invariant check class.
     */
    Class<? extends InvariantCheck> value();
}

/**
 * Container for multiple @Invariant annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Invariants {
    Invariant[] value();
}
```

### 10.3 Built-in Checks

```java
// Outcome checks
public class UrlChangedCheck implements OutcomeCheck {
    private final Pattern expectedPattern;
    
    public UrlChangedCheck(String urlPattern) {
        this.expectedPattern = Pattern.compile(urlPattern);
    }
    
    @Override
    public OutcomeResult verify(ExecutionContext ctx) {
        String currentUrl = ctx.getDriver().getCurrentUrl();
        boolean matches = expectedPattern.matcher(currentUrl).matches();
        return matches 
            ? OutcomeResult.passed("URL matches expected pattern")
            : OutcomeResult.failed("URL '%s' does not match pattern '%s'", 
                currentUrl, expectedPattern.pattern());
    }
}

public class ElementVisibleCheck implements OutcomeCheck {
    private final By locator;
    
    public ElementVisibleCheck(By locator) {
        this.locator = locator;
    }
    
    @Override
    public OutcomeResult verify(ExecutionContext ctx) {
        try {
            WebElement element = ctx.getDriver().findElement(locator);
            return element.isDisplayed()
                ? OutcomeResult.passed("Element is visible")
                : OutcomeResult.failed("Element exists but is not visible");
        } catch (NoSuchElementException e) {
            return OutcomeResult.failed("Element not found: %s", locator);
        }
    }
}

// Invariant checks
public class NoErrorBannerCheck implements InvariantCheck {
    private final List<By> errorSelectors;
    
    public NoErrorBannerCheck(List<By> errorSelectors) {
        this.errorSelectors = errorSelectors;
    }
    
    @Override
    public InvariantResult verify(ExecutionContext ctx) {
        for (By selector : errorSelectors) {
            try {
                WebElement errorElement = ctx.getDriver().findElement(selector);
                if (errorElement.isDisplayed()) {
                    return InvariantResult.violated(
                        "Error banner detected: %s", 
                        errorElement.getText()
                    );
                }
            } catch (NoSuchElementException ignored) {
                // Good - no error element found
            }
        }
        return InvariantResult.satisfied();
    }
}

public class NoForbiddenUrlCheck implements InvariantCheck {
    private final List<Pattern> forbiddenPatterns;
    
    @Override
    public InvariantResult verify(ExecutionContext ctx) {
        String currentUrl = ctx.getDriver().getCurrentUrl();
        for (Pattern pattern : forbiddenPatterns) {
            if (pattern.matcher(currentUrl).matches()) {
                return InvariantResult.violated(
                    "Navigated to forbidden URL: %s", currentUrl
                );
            }
        }
        return InvariantResult.satisfied();
    }
}
```

### 10.4 Extension Points

```java
/**
 * Register custom components with the healer.
 */
public interface HealerExtension {
    
    /**
     * Called during healer initialization.
     */
    void initialize(HealerContext context);
}

public interface HealerContext {
    
    /**
     * Register a custom outcome check.
     */
    void registerOutcomeCheck(String name, OutcomeCheck check);
    
    /**
     * Register a custom invariant check.
     */
    void registerInvariantCheck(String name, InvariantCheck check);
    
    /**
     * Register a custom LLM provider.
     */
    void registerLlmProvider(String name, LlmProvider provider);
    
    /**
     * Add custom forbidden keywords.
     */
    void addForbiddenKeywords(Collection<String> keywords);
    
    /**
     * Register a page context provider for better element scoping.
     */
    void registerPageContext(String name, PageContextProvider provider);
}
```

---

## 11. Cost and Performance

### 11.1 Cost Model

| Provider | Model | Cost per 1K tokens (input) | Cost per 1K tokens (output) |
|----------|-------|---------------------------|----------------------------|
| OpenAI | gpt-4o | $0.0025 | $0.01 |
| OpenAI | gpt-4o-mini | $0.00015 | $0.0006 |
| Anthropic | claude-sonnet-4-20250514 | $0.003 | $0.015 |
| Anthropic | claude-haiku | $0.00025 | $0.00125 |

**Per-heal token estimate:**

- Input: ~1,500-2,500 tokens (context + element list)
- Output: ~200-400 tokens (JSON response)

**Cost per heal (gpt-4o-mini):**

- Input: 2,000 tokens × $0.00015 = $0.0003
- Output: 300 tokens × $0.0006 = $0.00018
- **Total: ~$0.0005 per heal attempt**

**Monthly cost projection:**

| Scenario | Heals/day | Cost/month |
|----------|-----------|------------|
| Small team (50 tests, 5% heal rate) | ~10 | ~$0.15 |
| Medium team (500 tests, 5% heal rate) | ~100 | ~$1.50 |
| Large team (5000 tests, 5% heal rate) | ~1,000 | ~$15.00 |
| Enterprise (50,000 tests, 5% heal rate) | ~10,000 | ~$150.00 |

### 11.2 Performance Impact

**Latency per heal:**

| Component | Time |
|-----------|------|
| Failure detection | 0ms (already failed) |
| UI snapshot capture | 200-500ms |
| LLM API call | 800-2000ms |
| Action execution | 100-500ms |
| Outcome validation | 100-300ms |
| **Total** | **1.2-3.3 seconds** |

**Mitigation strategies:**

1. **Cache hits eliminate LLM calls** - repeated failures use cached decisions
2. **Snapshot only on failure** - no overhead on passing tests
3. **Parallel provider calls** - try multiple providers simultaneously
4. **Local model fallback** - avoid network latency for low-risk heals

### 11.3 Cost Controls

```yaml
llm:
  # Hard limits
  max_cost_per_run_usd: 10.00
  max_requests_per_test_run: 500
  max_requests_per_hour: 1000
  
  # Soft controls
  prefer_cached: true
  cache_ttl_hours: 24
  
  # Fallback chain (try cheaper models first for simple cases)
  model_selection:
    simple_heals: gpt-4o-mini  # ID change, text change
    complex_heals: gpt-4o     # Structure change, ambiguous
    
  # Cost tracking
  cost_tracking:
    enabled: true
    alert_threshold_usd: 5.00
    report_daily: true
```

---

## 12. Security and Privacy

### 12.1 Data Handling

**What gets sent to LLM:**

- Page URL
- Element attributes (id, class, name, aria-*)
- Visible text content
- Step text from feature files

**What NEVER gets sent:**

- Passwords or credentials
- Session tokens or cookies
- Personal user data
- Full page HTML
- Screenshots (unless explicitly configured)

### 12.2 Sanitization

```java
public class PromptSanitizer {
    
    private static final Pattern PASSWORD_FIELD = Pattern.compile(
        "password|pwd|secret|token|key|auth",
        Pattern.CASE_INSENSITIVE
    );
    
    public ElementSnapshot sanitize(ElementSnapshot element) {
        ElementSnapshot sanitized = element.copy();
        
        // Never include values from password-like fields
        if (isPasswordField(element)) {
            sanitized.setValue("[REDACTED]");
            sanitized.setPlaceholder("[REDACTED]");
        }
        
        // Truncate very long text
        if (sanitized.getText() != null && sanitized.getText().length() > 200) {
            sanitized.setText(sanitized.getText().substring(0, 200) + "...");
        }
        
        // Remove data attributes that might contain sensitive info
        Map<String, String> safeDataAttrs = new HashMap<>();
        for (Map.Entry<String, String> entry : element.getDataAttributes().entrySet()) {
            if (!containsSensitiveKey(entry.getKey())) {
                safeDataAttrs.put(entry.getKey(), truncate(entry.getValue(), 50));
            }
        }
        sanitized.setDataAttributes(safeDataAttrs);
        
        return sanitized;
    }
    
    private boolean isPasswordField(ElementSnapshot element) {
        if ("password".equals(element.getType())) return true;
        if (element.getName() != null && PASSWORD_FIELD.matcher(element.getName()).find()) return true;
        if (element.getId() != null && PASSWORD_FIELD.matcher(element.getId()).find()) return true;
        return false;
    }
}
```

### 12.3 API Key Management

```java
public class LlmCredentialManager {
    
    /**
     * Resolve API key from secure sources only.
     * Never accept keys from config files or command line.
     */
    public String resolveApiKey(String providerName) {
        // 1. Environment variable (preferred)
        String envVar = System.getenv(providerName.toUpperCase() + "_API_KEY");
        if (envVar != null && !envVar.isEmpty()) {
            return envVar;
        }
        
        // 2. Secrets manager (enterprise)
        String secretsKey = secretsManager.get("healer/" + providerName + "/api-key");
        if (secretsKey != null) {
            return secretsKey;
        }
        
        // 3. Vault integration
        String vaultKey = vaultClient.read("secret/healer/" + providerName);
        if (vaultKey != null) {
            return vaultKey;
        }
        
        throw new ConfigurationException(
            "No API key found for provider '%s'. Set %s_API_KEY environment variable.",
            providerName, providerName.toUpperCase()
        );
    }
}
```

### 12.4 Audit Logging

```java
public class AuditLogger {
    
    public void logHealAttempt(HealAttemptEvent event) {
        AuditRecord record = new AuditRecord();
        record.setTimestamp(Instant.now());
        record.setTestId(event.getTestId());
        record.setStepText(event.getStepText());
        record.setOriginalLocator(event.getOriginalLocator());
        record.setHealDecision(event.getDecision().canHeal() ? "HEAL" : "REFUSE");
        record.setConfidence(event.getDecision().getConfidence());
        record.setSelectedElement(summarizeElement(event.getSelectedElement()));
        record.setReasoning(event.getDecision().getReasoning());
        record.setOutcome(event.getOutcome());
        
        // Never log: full prompts, API keys, sensitive data
        
        auditStore.append(record);
    }
}
```

### 12.5 Local/Air-Gapped Deployment

For environments where external API calls are not permitted:

```yaml
llm:
  provider: local
  local:
    type: ollama
    model: llama3.1:70b
    endpoint: http://localhost:11434
    
    # Or vLLM
    type: vllm
    model: /models/mistral-7b-instruct
    endpoint: http://localhost:8000
    
    # Or custom endpoint
    type: custom
    endpoint: http://internal-llm.company.com/v1/chat
    model: company-fine-tuned-model
```

---

## 13. Roadmap

### Phase 0: Foundation (Weeks 1-2)

**Deliverables:**

- Gradle multi-module project structure
- Core model classes (FailureContext, IntentContract, HealDecision)
- Configuration loading infrastructure
- Logging framework integration
- CI pipeline with unit tests

**Acceptance Criteria:**

- Project builds and tests pass
- Configuration can be loaded from file/env/props
- Models serialize/deserialize correctly

### Phase 1: LLM Integration (Weeks 3-5)

**Deliverables:**

- LLM provider abstraction
- OpenAI adapter implementation
- Anthropic adapter implementation
- Prompt builder with template system
- Response parser with validation
- Error handling and retry logic

**Acceptance Criteria:**

- Can make successful API calls to OpenAI and Anthropic
- Prompts are generated correctly from test context
- Responses parse into HealDecision objects
- Failures (timeout, invalid response) are handled gracefully

### Phase 2: Selenium Layer (Weeks 6-8)

**Deliverables:**

- UI snapshot builder
- Element feature extraction
- Action executor with retry strategies
- Element re-finding logic
- WebDriver adapter

**Acceptance Criteria:**

- Snapshot captures all interactive elements with features
- Actions execute reliably with fallback strategies
- Elements can be re-found from snapshot data

### Phase 3: Healing Engine (Weeks 9-11)

**Deliverables:**

- Healing orchestrator
- Pre-LLM guardrails
- Post-LLM guardrails
- Outcome validation framework
- Invariant checking framework
- Built-in checks (URL, element visible, no error banner)

**Acceptance Criteria:**

- End-to-end healing flow works
- Guardrails correctly block forbidden actions
- Outcome checks verify action results
- Invariant violations are detected

### Phase 4: Cucumber Integration (Weeks 12-14)

**Deliverables:**

- Cucumber plugin
- Step interceptor
- Annotation processor for @Intent, @Outcome, @Invariant
- Tag parser for feature-level configuration
- Scenario context management

**Acceptance Criteria:**

- Plugin integrates with standard Cucumber runner
- Annotations are processed and applied
- Healing triggers automatically on failure
- Context flows correctly through scenario

### Phase 5: Reporting (Weeks 15-17)

**Deliverables:**

- Evidence collector
- JSON report generator
- HTML report generator
- Artifact packaging (screenshots, DOM)
- CI integration examples

**Acceptance Criteria:**

- Reports generate after test run
- All heal events captured with evidence
- HTML report is readable and useful
- Reports integrate with common CI systems

### Phase 6: Caching and Optimization (Weeks 18-20)

**Deliverables:**

- Heal cache implementation
- Cache key builder
- Cost tracking and limits
- Performance optimizations
- Fallback provider chain

**Acceptance Criteria:**

- Repeated failures use cached decisions
- Cost limits are enforced
- Performance overhead is acceptable (<500ms for cache hit)

### Phase 7: Production Hardening (Weeks 21-24)

**Deliverables:**

- Edge case handling (iframes, shadow DOM, SPAs)
- Parallel execution support
- Local model support (Ollama)
- Security hardening
- Documentation
- Sample projects

**Acceptance Criteria:**

- Works reliably on 5+ diverse applications
- Handles parallel test execution
- Local deployment option functional
- Documentation complete

---

## 14. Engineering Backlog

### Must-Have (v1.0)

- [ ] Core models and configuration
- [ ] OpenAI provider implementation
- [ ] Anthropic provider implementation
- [ ] UI snapshot builder (clickable elements)
- [ ] Prompt builder with element formatting
- [ ] Response parser with JSON validation
- [ ] Healing orchestrator
- [ ] Pre-LLM guardrails (destructive action check)
- [ ] Post-LLM guardrails (confidence threshold, forbidden keywords)
- [ ] Action executor (click, type)
- [ ] Element re-finder
- [ ] Basic outcome checks (URL change, element visible)
- [ ] Basic invariant checks (no error banner)
- [ ] Cucumber plugin and hooks
- [ ] @Intent annotation
- [ ] JSON report generator
- [ ] HTML report generator
- [ ] Heal caching
- [ ] Cost tracking

### Should-Have (v1.1)

- [ ] Input field healing
- [ ] Select/dropdown healing
- [ ] @Outcome annotation with custom checks
- [ ] @Invariant annotation
- [ ] LLM-based outcome validation
- [ ] Ollama/local model support
- [ ] Azure OpenAI support
- [ ] AWS Bedrock support
- [ ] Iframe handling
- [ ] Shadow DOM handling
- [ ] Locator patch suggestion (generate fixed locators)
- [ ] TestNG listener
- [ ] JUnit extension

### Nice-to-Have (v1.2+)

- [ ] Visual comparison (screenshot diff)
- [ ] Multi-step flow healing
- [ ] Custom fine-tuned model support
- [ ] VS Code extension for viewing reports
- [ ] Slack/Teams notifications
- [ ] Trend analysis dashboard
- [ ] Auto-generated step definitions
- [ ] Locator stability scoring

---

## 15. Validation Strategy

### 15.1 Test Applications

Build or use 3+ diverse test applications:

1. **Login/Auth App**
   - Multiple languages (EN, JA, HE, AR)
   - Various button text changes
   - DOM restructuring

2. **E-commerce Checkout**
   - Dynamic element IDs
   - Form field movements
   - Price/quantity assertions (must NOT heal)

3. **Dashboard/SPA**
   - Async rendering
   - Stale elements
   - Dynamic content loading

### 15.2 Test Scenarios

**True Positives (should heal successfully):**

- Button ID changed
- Button text localized
- Element wrapped in new container
- CSS class renamed
- Data-testid removed
- Button text changed to icon
- Form restructured

**True Negatives (should refuse to heal):**

- Price assertion failed
- Permission denied error
- Required field missing
- Destructive action (delete button)
- MFA step introduced
- Low-confidence candidates only

### 15.3 Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Heal success rate | >85% | Healed and passed / heal attempts |
| False heal rate | <1% | Healed but wrong / healed and passed |
| Refusal accuracy | >95% | Correct refusals / total refusals |
| Latency (cache miss) | <3s | P95 time for heal attempt |
| Latency (cache hit) | <500ms | P95 time for cached heal |
| Cost per heal | <$0.001 | Avg LLM cost per successful heal |

### 15.4 Regression Tests for Healer

```java
@Test
void shouldHealButtonIdChange() {
    // Setup: button used to have id="login", now has id="signin"
    // Expected: healer finds button by text/aria, heals successfully
}

@Test
void shouldHealCrossLanguage() {
    // Setup: button text changed from "Login" to "サインイン"
    // Expected: LLM recognizes semantic equivalence, heals
}

@Test
void shouldRefuseDestructiveAction() {
    // Setup: original was "Submit", candidate is "Delete Account"
    // Expected: guardrail blocks, refuses to heal
}

@Test
void shouldRefuseLowConfidence() {
    // Setup: multiple ambiguous candidates, none clearly correct
    // Expected: LLM returns low confidence, refuses to heal
}

@Test
void shouldRespectAssertionSteps() {
    // Setup: Then step with assertion fails
    // Expected: never attempts to heal assertion steps
}
```

---

## 16. Repository Structure

```
intent-healer/
├── healer-core/
│   ├── src/main/java/com/intenthealer/core/
│   └── src/test/java/com/intenthealer/core/
│
├── healer-llm/
│   ├── src/main/java/com/intenthealer/llm/
│   └── src/test/java/com/intenthealer/llm/
│
├── healer-selenium/
│   ├── src/main/java/com/intenthealer/selenium/
│   └── src/test/java/com/intenthealer/selenium/
│
├── healer-cucumber/
│   ├── src/main/java/com/intenthealer/cucumber/
│   └── src/test/java/com/intenthealer/cucumber/
│
├── healer-report/
│   ├── src/main/java/com/intenthealer/report/
│   ├── src/main/resources/templates/
│   └── src/test/java/com/intenthealer/report/
│
├── healer-cli/
│   ├── src/main/java/com/intenthealer/cli/
│   └── src/test/java/com/intenthealer/cli/
│
├── samples/
│   ├── sample-login-app/
│   ├── sample-ecommerce/
│   └── sample-cucumber-tests/
│
├── docs/
│   ├── getting-started.md
│   ├── configuration.md
│   ├── annotations.md
│   ├── custom-checks.md
│   ├── llm-providers.md
│   └── troubleshooting.md
│
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── CHANGELOG.md
└── LICENSE
```

---

## 17. Implementation Guidelines

### 17.1 Code Standards

- Java 17+ (records, sealed classes, pattern matching)
- Null safety via `Optional<T>` and `@Nullable`/`@NonNull` annotations
- Immutable models where possible
- Builder pattern for complex objects
- Comprehensive Javadoc on public APIs

### 17.2 Testing Requirements

- Unit tests for all business logic
- Integration tests for LLM providers (with mocks for CI)
- End-to-end tests against sample applications
- Contract tests for prompt/response formats
- Performance tests for snapshot and scoring

### 17.3 Logging Standards

```java
// Use structured logging
logger.info("Heal attempt", Map.of(
    "step", failure.getStepText(),
    "original_locator", failure.getOriginalLocator(),
    "candidates_found", candidates.size()
));

// Log LLM interactions at DEBUG level
logger.debug("LLM request", Map.of(
    "provider", provider.getName(),
    "model", config.getModel(),
    "input_tokens", estimatedTokens
));

// Log decisions at INFO level
logger.info("Heal decision", Map.of(
    "can_heal", decision.canHeal(),
    "confidence", decision.getConfidence(),
    "element_index", decision.getSelectedElementIndex(),
    "reasoning", decision.getReasoning()
));
```

### 17.4 Error Handling

```java
// Specific exceptions for specific failures
public class HealingException extends RuntimeException {
    private final HealingFailureReason reason;
}

public enum HealingFailureReason {
    LLM_UNAVAILABLE,
    LLM_INVALID_RESPONSE,
    GUARDRAIL_VIOLATION,
    NO_CANDIDATES_FOUND,
    LOW_CONFIDENCE,
    OUTCOME_CHECK_FAILED,
    INVARIANT_VIOLATED,
    ACTION_EXECUTION_FAILED,
    ELEMENT_NOT_REFINDABLE
}
```

### 17.5 Performance Guidelines

- Snapshot capture: max 500ms
- LLM call: max 30s timeout
- Total heal attempt: max 45s
- Cache lookup: max 10ms
- Never block test execution if healing is disabled

---

## Appendix A: Example Step Definition

```java
public class LoginSteps {
    
    @Given("the user is on the login page")
    @Intent(action = "navigate_to_login", description = "Navigate to the login page")
    @Outcome(checks = LoginPageVisibleCheck.class)
    public void navigateToLogin() {
        driver.get(config.getLoginUrl());
    }
    
    @When("the user enters username {string}")
    @Intent(action = "enter_username", description = "Enter username in the login form")
    public void enterUsername(String username) {
        resilient.find(By.id("username")).type(username);
    }
    
    @When("the user enters password {string}")
    @Intent(action = "enter_password", description = "Enter password in the login form", destructive = false)
    public void enterPassword(String password) {
        resilient.find(By.id("password")).type(password);
    }
    
    @When("the user clicks the login button")
    @Intent(action = "submit_login", description = "Click the login/sign-in button to authenticate")
    @Outcome(checks = DashboardVisibleCheck.class)
    @Invariant(NoErrorBannerCheck.class)
    @Invariant(NotOnLoginPageCheck.class)
    public void clickLogin() {
        resilient.find(By.id("login-btn")).click();
    }
    
    @Then("the user should see the dashboard")
    @Intent(action = "verify_dashboard", policy = HealPolicy.OFF)  // Never heal assertions
    public void verifyDashboard() {
        assertThat(driver.findElement(By.id("dashboard"))).isDisplayed();
    }
}
```

---

## Appendix B: Example Configuration File

```yaml
# healer-config.yml

healer:
  mode: AUTO_SAFE
  enabled: true

llm:
  provider: openai
  model: gpt-4o-mini
  api_key_env: OPENAI_API_KEY
  temperature: 0.1
  confidence_threshold: 0.80
  timeout_seconds: 30
  max_retries: 2
  
  fallback:
    - provider: anthropic
      model: claude-haiku
    - provider: local
      model: llama3.1:8b

guardrails:
  min_confidence: 0.80
  max_heal_attempts_per_step: 2
  max_heals_per_scenario: 5
  forbidden_keywords:
    - delete
    - remove
    - cancel
    - terminate
    - unsubscribe
  
  # Custom error banner selectors
  error_selectors:
    - ".error-banner"
    - ".alert-danger"
    - "[role='alert']"

snapshot:
  max_elements: 500
  include_hidden: false
  capture_screenshot: true
  capture_dom: false

cache:
  enabled: true
  ttl_hours: 24
  storage: file  # file | redis | memory
  file_path: .healer/cache

report:
  output_dir: build/healer-reports
  json_enabled: true
  html_enabled: true
  include_screenshots: true
  include_llm_prompts: false

logging:
  level: INFO
  include_llm_interactions: false
```

---

## Appendix C: Report Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "run_id": { "type": "string" },
    "timestamp": { "type": "string", "format": "date-time" },
    "duration_ms": { "type": "integer" },
    "config": {
      "type": "object",
      "properties": {
        "mode": { "type": "string" },
        "llm_provider": { "type": "string" },
        "llm_model": { "type": "string" },
        "confidence_threshold": { "type": "number" }
      }
    },
    "summary": {
      "type": "object",
      "properties": {
        "total_steps": { "type": "integer" },
        "total_failures": { "type": "integer" },
        "heal_attempts": { "type": "integer" },
        "heal_successes": { "type": "integer" },
        "heal_refusals": { "type": "integer" },
        "heal_failures": { "type": "integer" },
        "total_llm_cost_usd": { "type": "number" }
      }
    },
    "events": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "event_id": { "type": "string" },
          "timestamp": { "type": "string", "format": "date-time" },
          "feature": { "type": "string" },
          "scenario": { "type": "string" },
          "step": { "type": "string" },
          "failure": {
            "type": "object",
            "properties": {
              "exception_type": { "type": "string" },
              "message": { "type": "string" },
              "original_locator": { "type": "string" }
            }
          },
          "decision": {
            "type": "object",
            "properties": {
              "can_heal": { "type": "boolean" },
              "confidence": { "type": "number" },
              "selected_element_index": { "type": ["integer", "null"] },
              "reasoning": { "type": "string" },
              "refusal_reason": { "type": ["string", "null"] }
            }
          },
          "result": {
            "type": "object",
            "properties": {
              "status": { "type": "string", "enum": ["SUCCESS", "REFUSED", "FAILED"] },
              "healed_locator": { "type": ["string", "null"] },
              "outcome_check_passed": { "type": "boolean" },
              "invariants_satisfied": { "type": "boolean" }
            }
          },
          "artifacts": {
            "type": "object",
            "properties": {
              "screenshot": { "type": ["string", "null"] },
              "dom_snapshot": { "type": ["string", "null"] }
            }
          },
          "cost": {
            "type": "object",
            "properties": {
              "input_tokens": { "type": "integer" },
              "output_tokens": { "type": "integer" },
              "cost_usd": { "type": "number" }
            }
          }
        }
      }
    }
  }
}
```

---

*End of Product Plan*

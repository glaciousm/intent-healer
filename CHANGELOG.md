# Changelog

All notable changes to Intent Healer will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2025-12-21

### Added
- SECURITY.md with LLM data flow, offline mode, and vulnerability reporting
- CONTRIBUTING.md with development setup and PR guidelines
- CHANGELOG.md in Keep a Changelog format
- GitHub issue templates (bug report, feature request)
- GitHub pull request template
- CI badge in README

### Documentation
- Added "What Intent Healer Does (and Doesn't)" section
- Added "When NOT to Use Intent Healer" section

## [1.0.2] - 2025-12-20

### Added
- HTML/JSON report generation at JVM shutdown via `HealingReportGenerator`
- Graceful handling when LLM provider is unavailable (missing API key)
- `isProviderAvailable()` method in `LlmOrchestrator`
- Agent banner shows INACTIVE state when provider not configured
- Gradle wrapper for `healer-intellij` module

### Fixed
- Better API key validation error messages for Azure, OpenAI, and Anthropic providers
- IntelliJ plugin compatibility extended to 2025.2 (build 252.*)
- Clear error when Azure `base_url` includes full endpoint path

### Documentation
- Clarified Azure OpenAI configuration (`base_url` should be base URL only)
- Fixed configuration examples (YAML nesting, `api_key_env` instead of `api_key`)
- Updated all version references from 1.0.1 to 1.0.2

## [1.0.1] - 2025-12-18

### Added
- Java Agent for zero-code self-healing integration (`healer-agent` module)
- ByteBuddy bytecode instrumentation for WebDriver interception
- Automatic interception of ChromeDriver, FirefoxDriver, EdgeDriver, SafariDriver, RemoteWebDriver
- Fat JAR packaging for easy distribution
- Startup banner showing configuration status

### Changed
- Improved documentation with complete integration guides
- Added zero-code integration section to README and USER_GUIDE

## [1.0.0] - 2025-12-17

### Added
- **Core Framework**
  - `HealingEngine` - Main orchestration with pluggable components
  - `HealingWebDriver` - WebDriver wrapper with automatic healing
  - `HealCache` - Caffeine-based caching for healing decisions
  - `CircuitBreaker` - Fail-fast mechanism for LLM failures
  - `GuardrailChecker` - Pre/post-LLM safety checks

- **LLM Providers**
  - OpenAI (GPT-4o, GPT-4o-mini)
  - Anthropic (Claude 3 Haiku, Sonnet, Opus)
  - Azure OpenAI
  - AWS Bedrock
  - Ollama (local LLM)
  - Mock provider (heuristic matching)

- **Test Framework Integrations**
  - Cucumber with `@Intent`, `@Invariant`, `@Outcome` annotations
  - TestNG listener
  - JUnit 5 extension

- **Reporting**
  - HTML reports with visual evidence
  - JSON export for CI integration
  - PDF report generation
  - Week-over-week trend charts
  - Locator stability scoring
  - Cost projection and budgeting

- **CLI**
  - Configuration management
  - Cache operations
  - Report generation
  - Real-time monitoring (`healer watch`)
  - Approval workflow (`healer approve`)

- **IntelliJ Plugin**
  - Dashboard for healing statistics
  - Heal history browser
  - Live event monitoring
  - Locator suggestions

- **Benchmark Suite**
  - 35 scenarios covering various DOM change types
  - Negative tests for false heal detection
  - Provider comparison reports

### Security
- API key sanitization via `SecurityUtils`
- Environment variable-based key management
- Configurable forbidden URL patterns
- Destructive action detection

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| 1.0.3 | 2025-12-21 | Community files, README improvements |
| 1.0.2 | 2025-12-20 | Report generation, graceful provider handling |
| 1.0.1 | 2025-12-18 | Java Agent for zero-code integration |
| 1.0.0 | 2025-12-17 | Initial release on Maven Central |

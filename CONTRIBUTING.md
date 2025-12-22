# Contributing to Intent Healer

Thank you for your interest in contributing to Intent Healer! This document provides guidelines for contributing.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/intent-healer.git
   cd intent-healer
   ```
3. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** and test them
5. **Submit a pull request**

## Development Setup

### Prerequisites

- Java 21+
- Maven 3.8+
- Chrome browser (for Selenium tests)
- Optional: Ollama (for local LLM testing)

### Building

```bash
# Build all modules
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests
```

### Running Tests

```bash
# All tests
mvn clean test

# Single module
mvn test -pl healer-core

# Single test class
mvn test -pl healer-core -Dtest=CircuitBreakerTest

# Single test method
mvn test -pl healer-core -Dtest=CircuitBreakerTest#testOpenCircuit
```

## Code Style

- **Follow existing patterns** in the codebase
- **No Lombok** - the project intentionally doesn't use it
- **Javadoc** for all public APIs
- **Tests required** for new features
- **Keep it simple** - avoid over-engineering

### Naming Conventions

- Classes: `PascalCase`
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`

## Pull Request Guidelines

### Before Submitting

- [ ] Code compiles: `mvn clean compile`
- [ ] Tests pass: `mvn test`
- [ ] No new warnings
- [ ] Documentation updated (if needed)

### PR Format

Use a clear, descriptive title:
- `feat: Add support for X`
- `fix: Resolve issue with Y`
- `docs: Update README`
- `refactor: Simplify Z logic`

### PR Description

Include:
- What the change does
- Why it's needed
- How to test it
- Related issues (if any)

## What We're Looking For

### High Priority

- Bug fixes with tests
- Documentation improvements
- Performance optimizations
- New LLM provider integrations

### Medium Priority

- New healing strategies
- Additional test framework integrations
- CLI enhancements
- Report improvements

### Lower Priority

- UI changes to IntelliJ plugin
- New languages/i18n

## Module Overview

| Module | Purpose |
|--------|---------|
| `healer-core` | Core engine, models, configuration, healing logic |
| `healer-llm` | LLM providers (OpenAI, Anthropic, Ollama, Azure, Bedrock) |
| `healer-selenium` | Selenium WebDriver wrapper with self-healing |
| `healer-playwright` | Playwright integration with self-healing |
| `healer-agent` | Java Agent for zero-code integration |
| `healer-cucumber` | Cucumber integration with @Intent annotations |
| `healer-testng` | TestNG listener integration |
| `healer-junit` | JUnit 5 extension integration |
| `healer-report` | HTML/JSON report generation |
| `healer-cli` | Command-line interface |
| `healer-intellij` | IntelliJ IDEA plugin |
| `healer-benchmark` | Benchmark suite (35 scenarios) |
| `healer-showcase` | Demo project with examples |

## Questions?

- Open a [GitHub issue](https://github.com/glaciousm/intent-healer/issues) for bugs or features
- Start a [GitHub Discussion](https://github.com/glaciousm/intent-healer/discussions) for questions

## License

By contributing, you agree that your contributions will be licensed under the AGPL-3.0 license.

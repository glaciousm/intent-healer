# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## LLM Data Flow

Intent Healer sends the following data to your configured LLM provider:

- **DOM snapshot**: Element attributes, text content, and structure (limited subset, not full page)
- **Failed locator**: The original selector that didn't find an element
- **Step context**: Action type (click, type, etc.) and step description

### What is NOT Sent

- Screenshots (unless you explicitly enable visual evidence reporting)
- Form field values or credentials
- Cookies or session data
- Full page HTML (only relevant DOM elements near the expected location)

## Running in Offline Mode

For air-gapped environments or when you don't want any external API calls:

```yaml
healer:
  llm:
    provider: mock    # Uses heuristic matching, no external calls
    model: heuristic
```

The mock provider uses attribute matching, text similarity, and DOM position analysis. It has ~70% accuracy vs ~90%+ for cloud LLMs, but requires no network access.

## API Key Handling

Intent Healer follows security best practices for API key management:

- **Environment variables only**: Keys are read from env vars, never stored in config files
- **Never logged**: All API keys are sanitized via `SecurityUtils` before logging
- **Not transmitted**: Keys stay local; only the LLM request/response crosses the network

### Supported Environment Variables

| Provider | Environment Variable |
|----------|---------------------|
| OpenAI | `OPENAI_API_KEY` |
| Anthropic | `ANTHROPIC_API_KEY` |
| Azure OpenAI | `AZURE_OPENAI_API_KEY` |
| AWS Bedrock | Uses AWS SDK default credential chain |

## Guardrails

Intent Healer includes built-in safety guardrails:

```yaml
healer:
  guardrails:
    min_confidence: 0.75          # Reject low-confidence heals
    max_heals_per_scenario: 10    # Prevent runaway healing
    forbidden_url_patterns:       # Never heal on these pages
      - /admin/
      - /payment/
      - /checkout/
    destructive_actions:          # Require higher confidence
      - delete
      - remove
      - cancel
```

## CONFIRM Mode

For maximum control, use CONFIRM mode to require human approval before any healing:

```yaml
healer:
  mode: CONFIRM
```

This prevents any automatic changes and creates a pending approval queue.

## Reporting Vulnerabilities

If you discover a security vulnerability in Intent Healer:

1. **Do NOT** open a public GitHub issue
2. Email: mmamouzellos@gmail.com
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact

We will respond within 48 hours and work with you on a fix before public disclosure.

## Security Audit

Intent Healer's CI pipeline includes:

- OWASP Dependency Check for vulnerable dependencies
- No secrets in code (enforced via pre-commit hooks)
- API key sanitization in all log outputs

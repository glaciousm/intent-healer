# Intent Healer IntelliJ Plugin

[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2023.2+-purple.svg)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)

> **IntelliJ IDEA plugin for Intent Healer** - View heal history, manage trust levels, monitor live healing events, and get locator suggestions directly in your IDE.

---

## Features

| Feature | Description | Access |
|---------|-------------|--------|
| **Dashboard** | Real-time healing statistics and activity | View → Tool Windows → Intent Healer |
| **Heal History** | Browse and manage past healing decisions | Tools → Intent Healer → View Heal History |
| **Live Events** | Monitor heal events as they happen | Intent Healer tool window → Live tab |
| **Locator Suggestions** | Get stable locator recommendations | Right-click in editor → Suggest Stable Locator |
| **Settings** | Configure healing behavior | Settings → Tools → Intent Healer |

---

## Installation

### Option 1: Build from Source

```bash
cd healer-intellij

# Windows
.\gradlew.bat buildPlugin

# macOS / Linux
./gradlew buildPlugin
```

Install the generated ZIP from `build/distributions/healer-intellij-1.0.4.zip`:

1. Open **IntelliJ IDEA**
2. Navigate to **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select the ZIP file
4. Restart IntelliJ IDEA

### Option 2: Run in Debug Mode

For development and testing:

```bash
cd healer-intellij

# Windows
.\gradlew.bat runIde

# macOS / Linux
./gradlew runIde
```

This launches a sandboxed IntelliJ instance with the plugin pre-installed.

---

## Configuration

Navigate to **Settings** → **Tools** → **Intent Healer** to configure:

| Setting | Description | Options |
|---------|-------------|---------|
| **Healer Mode** | Controls healing behavior | `OFF`, `SUGGEST`, `AUTO_SAFE`, `AUTO_ALL` |
| **Cache Directory** | Location for heal decision cache | File path |
| **Enable Notifications** | Show heal event notifications | `true` / `false` |
| **Max History Entries** | Number of history entries to retain | Integer (default: 1000) |

---

## Using the Dashboard

### Dashboard Tab
- View real-time healing statistics
- Monitor success rates and confidence scores
- Track healing activity trends

### History Tab
- Browse past healing decisions
- Double-click entries to view details
- Use **Accept**, **Reject**, or **Blacklist** buttons to manage heals
- Filter by scenario, locator type, or date range

### Live Tab
- Watch heal events in real-time during test execution
- Events appear as they occur
- Useful for debugging and monitoring active test runs

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Open Intent Healer Tool Window | `Alt+Shift+H` |
| Suggest Stable Locator | `Alt+Shift+L` (in editor) |

---

## Requirements

- **IntelliJ IDEA** 2023.2 or later (Ultimate or Community)
- **Java** 17 or later
- **Intent Healer** framework configured in your project

---

## Troubleshooting

### Plugin Not Loading
Ensure IntelliJ IDEA version is 2023.2 or later. Check **Help** → **About** to verify.

### No Heal Events Appearing
1. Verify Intent Healer is enabled in your project's `healer-config.yml`
2. Check that tests are running with the healer-agent or HealingWebDriver
3. Ensure the plugin settings are configured correctly

### Cache Directory Issues
Use an absolute path for the cache directory. Ensure the directory exists and is writable.

---

## License

This plugin is part of the Intent Healer framework and is licensed under AGPL-3.0.

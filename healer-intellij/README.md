# Intent Healer IntelliJ Plugin

IntelliJ IDEA plugin for Intent Healer - view heal history, manage trust levels, and get locator suggestions.

## Installation

### Option 1: Build from Source

```bash
cd healer-intellij
.\gradlew.bat buildPlugin   # Windows
./gradlew buildPlugin       # Mac/Linux
```

Then install the ZIP from `build/distributions/healer-intellij-1.0.3.zip`:

1. Open IntelliJ IDEA
2. Go to **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select the ZIP file
4. Restart IntelliJ

### Option 2: Run in Debug Mode

```bash
cd healer-intellij
.\gradlew.bat runIde   # Windows
./gradlew runIde       # Mac/Linux
```

This starts a sandboxed IntelliJ with the plugin installed for testing.

## Features

| Feature | Access |
|---------|--------|
| **Dashboard** | View → Tool Windows → Intent Healer |
| **Settings** | Settings → Tools → Intent Healer |
| **Heal History** | Tools → Intent Healer → View Heal History |
| **Live Events** | Real-time heal event monitoring |
| **Locator Suggestions** | Right-click in editor → Suggest Stable Locator |

## Configuration

1. Go to **Settings** → **Tools** → **Intent Healer**
2. Configure:
   - **Healer Mode**: OFF / SUGGEST / AUTO_SAFE / AUTO_ALL
   - **Cache Directory**: Location for heal cache
   - **Enable Notifications**: Show heal notifications
   - **Max History Entries**: Number of entries to keep

## Using the Dashboard

1. Open the **Intent Healer** tool window (right sidebar or View menu)
2. **Dashboard Tab**: View real-time healing activity and statistics
3. **History Tab**: Browse past heals
   - Double-click to view details
   - Use **Accept/Reject/Blacklist** buttons
4. **Live Tab**: Watch heal events as they happen

## Requirements

- IntelliJ IDEA 2023.2 or later
- Java 17+

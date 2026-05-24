# Claude Assistant — RuneLite Plugin

An AI-powered OSRS assistant that lives in your RuneLite sidebar. Powered by Claude (Anthropic), it reads your live game state and gives personalized advice.

---

## Features

- **Sidebar chat panel** — dark-themed, matches RuneLite's aesthetic
- **Live game context** — Claude sees your stats, inventory, gear, and location automatically
- **Conversation history** — maintains context across messages in the session
- **Configurable** — toggle which game data to share, set your API key, adjust response length

---

## Requirements

- Java 11+
- Gradle 7+
- A RuneLite development environment
- An [Anthropic API key](https://console.anthropic.com/)

---

## Setup & Build

### 1. Clone / place the project

```
runelite-claude-assistant/
├── build.gradle
├── settings.gradle
└── src/main/java/com/claudeassistant/
    ├── ClaudeAssistantPlugin.java
    ├── ClaudeAssistantConfig.java
    ├── ClaudeAssistantPanel.java
    ├── ClaudeApiClient.java
    ├── GameContextBuilder.java
    └── ChatMessage.java
```

### 2. Build the plugin jar

```bash
cd runelite-claude-assistant
gradle shadowJar
```

The jar will be at: `build/libs/claude-assistant-1.0.0.jar`

### 3. Load into RuneLite (Developer Mode)

Option A — **RuneLite Developer Setup** (recommended for development):

1. Clone the RuneLite repo: `git clone https://github.com/runelite/runelite`
2. Place this project inside `runelite/runelite-client/src/main/java/net/runelite/client/plugins/`
3. Or use the **external plugin** loading mechanism

Option B — **RuneLite External Plugin Loader**:

1. Build the shadow jar
2. In RuneLite, go to **Configuration → RuneLite → Developer tools → Load plugin from jar**
3. Select the built jar

Option C — **RuneLite Plugin Hub** (for distribution, requires review):

- Follow the [Plugin Hub submission guide](https://github.com/runelite/plugin-hub)

### 4. Configure the plugin

1. Open RuneLite → **Plugin Hub** (wrench icon)
2. Find **Claude Assistant** and click the gear icon
3. Enter your **Anthropic API key** (`sk-ant-...`)
4. Toggle which game data to share (stats, inventory, equipment, location)
5. Set max response tokens (default 1024)

---

## Usage

1. Click the **C** icon in the RuneLite sidebar (right-hand navigation)
2. Type your question and press **Enter** (or Shift+Enter for newlines)
3. Claude will respond with your live game state as context

### Example questions

- *"What slayer tasks should I block at my level?"*
- *"Is my gear good enough for Zulrah?"*
- *"Best way to train Agility at level 59?"*
- *"What should I spend my 2.8M GP on right now?"*
- *"Should I use Protect from Melee or Protect from Missiles here?"*

---

## Privacy Note

Your Anthropic API key is stored in RuneLite's config file (not encrypted at rest). Game state data (stats, inventory, location) is sent to Anthropic's API with each message. Do not share your API key.

---

## Development Notes

- Uses **OkHttp3** and **Gson** — both bundled with RuneLite, no extra dependencies
- Async API calls — never blocks the game client thread
- Game context is built on the client thread, API call fires off-thread
- Max 20 messages of history sent per request to manage token usage

---

## Roadmap / Future Ideas

- [ ] Quest tracker integration (show active quest and current step)
- [ ] XP tracker context (send current XP gains/hr)
- [ ] Bank value / wealth context
- [ ] Whisper mode (overlay on the game screen instead of sidebar)
- [ ] Pre-built prompts / quick-action buttons (e.g. "Analyze my gear")
- [ ] Screenshot → Claude vision (describe what's on screen)

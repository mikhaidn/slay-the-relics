#### 1. Mod Layer (`/mod/`)
- **Technology:** Java 15, BaseMod framework, Gradle build system
- **Main Class:** `str_exporter.SlayTheRelicsExporter`
- **Core Responsibilities:**
  - Subscribes to BaseMod lifecycle events (PostInitialize, PostRender, StartGame)
  - Polls game state via `GameState.poll()` method in render loop
  - Captures comprehensive game data: deck, relics, potions, map, combat piles, player/monster powers, orbs
  - Handles special cases: bottled cards (flame/lightning/tornado), card upgrades, relic counters, metascaling cards (Genetic Algorithm, Ritual Dagger, Searing Blow)
  - Integrates with third-party mods (Relic Stats mod for counter tracking)
  - Provides in-game configuration UI: delay slider (0-10s), Twitch OAuth button, connection status indicator
  - Serializes state to structured JSON and sends to backend via `EBSClient` HTTP client

**Authentication Flow:**
- Opens system browser for Twitch OAuth flow when user clicks "Connect with Twitch"
- Stores credentials locally in config file at `SlayTheSpire/preferences/str_config/config.properties`
- Visual status indicator (green = connected, red = disconnected/failed)
- Auto-reconnects on game restart if credentials valid

**Key Implementation Details:**
- Delay system allows sync with stream encoding latency (default 700ms recommended)
- Event-driven via BaseMod hooks (no background threads as of commit c2df5a6)
- JSON serialization using Gson library
- HTTP communication to backend API endpoint `/api/v2/game-state`


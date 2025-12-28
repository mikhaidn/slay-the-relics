#### 2. Backend Layer (`/backend/`)
- **Technology:** Go 1.23, Gin web framework, Redis 7, OpenTelemetry observability
- **Main Entry:** `main.go` (bootstraps HTTP server on port 8888)
- **Module Structure:**
  - `api/` - HTTP endpoint handlers and request validation
  - `slaytherelics/` - Core business logic (GameStateManager, Broadcaster, Users)
  - `client/` - External service clients (Twitch Helix API, Redis)
  - `models/` - Data transfer objects and game state models
  - `config/` - Configuration loading and validation
  - `o11y/` - OpenTelemetry instrumentation (traces, metrics, logging)

**API Endpoints:**
```
POST /api/v2/game-state              # Receive game state from mod
GET  /api/v2/game-state/:channel-id  # Fetch current state for extension
POST /api/v1/auth                    # Twitch OAuth callback
GET  /deck/:name                     # Legacy deck endpoint
POST /                               # Legacy message handler (deprecated)
POST /api/v1/message                 # Legacy message handler (deprecated)
POST /api/v1/login                   # User login (deprecated)
```

**State Management (`slaytherelics/game_state.go`):**
- Maintains `GameState` per channel in-memory (backed by Redis for persistence)
- Tracks `gameStateIndex` for versioning (increments on each state change)
- Computes diffs between old and new state to minimize bandwidth
- Only broadcasts to Twitch when state actually changes (stateful diffing)
- Supports partial updates via `GameStateUpdate` struct with nullable fields
- Compression pipeline: JSON → gzip → ASCII85 encoding (format: `<~...~>`)

**GameState Structure:**
```go
type GameState struct {
    Channel         string
    GameStateIndex  int
    Character       string
    Boss            string
    Relics          []string
    BaseRelicStats  map[int][]interface{}  // Relic counters, bottled cards
    RelicTips       []Tip                  // Dynamic relic tooltips
    Deck            []CardData             // Main deck
    DrawPile        []CardData             // Combat draw pile
    DiscardPile     []CardData             // Combat discard pile
    ExhaustPile     []CardData             // Combat exhaust pile
    Potions         []string
    PotionX         float64                // Horizontal position for potion bar
    AdditionalTips  []TipWithHitbox        // Dynamic tooltips (powers, orbs, stance)
    StaticTips      []TipWithHitbox        // Infrequent tooltips (shop, boss preview)
    MapNodes        [][]MapNode            // Dungeon map structure
    MapPath         [][]int                // Path taken through map
    Bottles         [3]int                 // Indices of bottled cards in deck
}
```

**Broadcasting (`slaytherelics/broadcaster.go`):**
- Uses Twitch Extensions PubSub API via Helix client
- Compresses messages to fit Twitch's 5KB message limit
- Compression: JSON stringify → gzip (pako) → ASCII85 encode → wrap in `<~...~>`
- Broadcasts only state changes (diff detection prevents spam)
- Handles authentication via JWT for Twitch API


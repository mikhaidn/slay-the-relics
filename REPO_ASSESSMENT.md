# Slay the Relics - Repository Assessment

## Claude TLDr (Technical Deep Dive)

### Architecture Overview

**Slay the Relics** is a three-tier distributed Twitch extension system for "Slay the Spire" that displays real-time game state overlays on stream. The architecture follows an event-driven pattern with state management at the backend tier.

**Data Flow:**
```
Mod (Java) → Backend (Go) → Twitch PubSub → Extension (React/TypeScript)
     ↓           ↓                              ↓
  Game State  State Mgmt                   UI Rendering
  Capture     + Diffing                    + Decompression
```

### Component Architecture

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

#### 3. Extension Layer (`/slay-the-relics-extension/`)
- **Technology:** React 19, TypeScript 5.8, Vite 5, Tailwind CSS 4
- **Main Entry:** `src/main.tsx` → `App.tsx` component
- **Build:** Vite bundler (migrated from Create React App)
- **Component Hierarchy:**
```
App (main orchestrator)
├── SpireMap (dungeon map visualization)
├── RelicBar (relic display with tooltips)
├── PotionBar (potion display with tooltips)
├── DeckView (main deck) ─── CardView (zoomed card detail)
├── DeckView (draw pile)
├── DeckView (discard pile)
├── DeckView (exhaust pile)
└── PowerTipBlock (dynamic tooltips for powers/orbs/stance)
```

**Positioning System:**
- **Critical Constraint:** Extension assumes game is fullscreen on stream (1920x1080)
- **Coordinate System:** Percentage-based positioning relative to viewport
- **Root Font Size:** `2vh` (based on 1080px height, so 1rem = 21.6px)
- **Hardcoded Positions:**
  - Deck button: `top: 0%, right: 4.322%` (Deck.tsx:411-412)
  - Draw pile button: `top: 89%, left: 2.322%` (Deck.tsx:415-416)
  - Discard pile button: `top: 89%, left: 94.322%` (Deck.tsx:419-420)
  - Exhaust pile button: `top: 78.5%, left: 94.56%` (Deck.tsx:423-424)
  - Relic hitboxes: `x: 1.458% + i*3.75%, y: 6.111%` (Relic.tsx:88-93)
  - Potion hitboxes: `x: potionX% (dynamic, ~33%), y: 0%` (Potion.tsx:44-48)
  - Map button: `top: 0%, right: 8.322%` (App.css:185-186)
  - Power/orb tooltips: Dynamic hitboxes sent from mod with x/y/w/h percentages

**Data Synchronization:**
1. On mount: Authenticate with Twitch Extensions API to get `channelId`
2. Fetch initial state: `GET /api/v2/game-state/:channelId` from backend
3. Subscribe to Twitch PubSub topic: `broadcast.${channelId}`
4. On message:
   - Decompress: ASCII85 decode (`base85` lib) → gzip inflate (`pako` lib) → JSON parse
   - Check `gameStateIndex`: if sequential, apply incremental update; if out of sync, refetch full state
   - Apply state update: Merge non-null fields from update into current state
   - Re-render UI components

**Card Image Loading:**
- Images fetched from GitHub repo: `https://raw.githubusercontent.com/Spireblight/slay-the-relics/refs/heads/master/assets/sts1/card-images/${cardName}.png`
- Card names normalized: lowercase, remove spaces/punctuation, append "plus1" for upgrades
- Uses Slaytabase format for card URLs

**Tooltip System:**
- Localization data loaded on mount from `public/localization/` JSON files
- Keyword detection in card/relic descriptions to show inline tooltips
- Power tips use react-tooltip library with HTML content rendered via ReactDOMServer
- Hitbox positioning via absolute positioning with percentage-based coordinates

### Key Features

1. **Real-time Synchronization:** Configurable delay (0-10s) to match stream encoding latency
2. **Bandwidth Optimization:** Incremental updates + gzip + ASCII85 compression (~5-10x reduction)
3. **Visual Accuracy:** Mimics game UI using extracted assets and color values
4. **Combat Visibility:** Shows draw/discard/exhaust piles during combat (cards clickable for zoom)
5. **Map Visualization:** Canvas-based dungeon map with path highlighting
6. **Relic Intelligence:** Bottled card indicators, relic counters, shop/boss preview tooltips
7. **Localization:** Supports game's native i18n for cards/relics/potions/keywords

### Repository Origin & Modifications

**Original Source:**
- Published by Spireblight organization (GitHub: https://github.com/Spireblight/slay-the-relics)
- Primary authors: Peijun Ma (MaT1g3R), Adam Volný
- License: GNU AGPL v3.0 (copyleft, network service provision triggers source distribution)
- Funding: Ko-fi (spireblight)

**This Repository Modifications:**
Based on git log analysis, this appears to be the original/canonical repository (not a fork). Recent commits show active development:

1. **Debugging & Polish (commits 67ef79f-85631fb):**
   - Added debug logging for game state updates
   - Steam Workshop release cleanup

2. **Potion Positioning (commit ae9b10e):**
   - Added dynamic `potionX` field to GameState (sent from mod)
   - Allows horizontal positioning of potion bar to match game (varies by relic count)

3. **Bottled Cards (commits a95d8a5-7ac8598):**
   - Force bottled cards to display as upgraded (yellow border)
   - Support for Bottled Flame/Lightning/Tornado relics
   - Indices tracked in `bottles: [3]int` array

4. **Tooltip Optimization (commit eefbd31):**
   - Split tooltips into `additionalTips` (frequent) and `staticTips` (infrequent)
   - Reduces bandwidth for tooltips that rarely change (shop items, boss relics)

5. **Special Card Handling (commits 1c8b587-c64fa8b):**
   - Searing Blow: Dynamic damage calculation based on upgrade count
   - Metascaling cards: Send counter values for Genetic Algorithm, Ritual Dagger
   - Stance tips for Watcher class

6. **Mod Architecture Rewrite (commit b947637):**
   - Removed background threading (now poll in PostRender hook)
   - Switched from timer-based to event-driven updates
   - Structured JSON instead of string manipulation

7. **Integration Features (commit f012e1e):**
   - Relic Stats mod integration for counter tracking
   - Boss identification and preview tooltips
   - Draw pile visibility during combat

**Notable Unreleased Features:**
- Map view (full dungeon map with path visualization)
- Combat pile visibility (draw/discard/exhaust)
- Boss relic preview in shop
- Stance indicator for Watcher

### Scaling for Non-Fullscreen Streams

**Current Limitation:**
README.md:32-33 explicitly states:
> "In order for the extension to be properly visually aligned with the game, the game capture has to perfectly fill the whole stream (as if you had the game fullscreen)"

**Root Cause Analysis:**

1. **Percentage-Based Positioning:** All UI elements positioned using viewport percentages
   - Assumes game fills entire 1920x1080 canvas
   - If game is windowed or scaled, percentages no longer align with game coordinates

2. **No Transform Matrix:** Extension lacks knowledge of:
   - Game window position in stream canvas
   - Game window size/scale
   - Letterboxing/pillarboxing offsets

3. **Hardcoded Coordinates:** Deck buttons, relics, potions all use absolute percentage positions

**Required Modifications for Scalable Steam Workshop Mod:**

To support non-fullscreen streams, you would need to:

1. **Capture Game Window Geometry from Mod:**
   - Detect actual game window position on screen
   - Calculate transform: `(gameX, gameY, gameWidth, gameHeight)` relative to stream canvas
   - Send transform matrix in GameState: `{offsetX: %, offsetY: %, scaleX: %, scaleY: %}`

2. **Apply Transform in Extension:**
   - Create coordinate transformation function:
     ```typescript
     function transformCoordinate(gameX: number, gameY: number, transform: Transform): {x: string, y: string} {
       return {
         x: `${transform.offsetX + gameX * transform.scaleX}%`,
         y: `${transform.offsetY + gameY * transform.scaleY}%`
       };
     }
     ```
   - Update all components to use transform: RelicBar, PotionBar, DeckButton, PowerTips

3. **Configuration UI in Mod:**
   - Allow streamer to set game window bounds
   - OR auto-detect via native window APIs (platform-dependent)
   - Visual alignment helper (show test overlay in-game to verify positioning)

4. **Backend Schema Update:**
   - Add `transform` field to GameState model
   - Update API documentation

5. **Extension Calibration Mode:**
   - Add UI for streamers to manually adjust alignment
   - Save calibration per channel in localStorage
   - Show alignment grid overlay during setup

**Example Transform Implementation:**
```typescript
// In App.tsx
interface Transform {
  offsetX: number;  // % from left edge of stream canvas
  offsetY: number;  // % from top edge
  scaleX: number;   // width scale factor (e.g., 0.75 for 3/4 width)
  scaleY: number;   // height scale factor
}

// In Relic.tsx
const hitbox = {
  x: transformX(RELIC_HITBOX_LEFT + i * RELIC_HITBOX_WIDTH, props.transform),
  y: transformY(6.111, props.transform),
  // ...
};
```

**Challenges:**
- Java mod cannot directly detect stream canvas size (only local game window)
- Streamer would need to manually configure stream layout dimensions
- Different streaming software (OBS, Streamlabs) handle window capture differently
- Dynamic repositioning (e.g., scene changes) would require recalibration

**Recommended Approach:**
1. Add manual configuration UI in mod settings
2. Streamer inputs game window position as % of stream canvas
3. Mod sends these offsets in every GameState update
4. Extension applies transform to all coordinates
5. Provide alignment helper overlay for visual verification

### Documentation Pointers

**For Creating Configurable Steam Workshop Mod:**

1. **Getting Started:**
   - Clone this repository
   - Read `/docs/README.md` for architecture overview
   - Read main `/README.md` for user-facing setup

2. **Mod Development:**
   - Mod source: `/mod/src/main/java/str_exporter/`
   - Main class: `SlayTheRelicsExporter.java`
   - Build: `cd mod && ./gradlew build`
   - Output: `mod/build/libs/SlayTheRelicsExporter.jar`
   - Install: Copy JAR to `SlayTheSpire/mods/`
   - Required dependencies: ModTheSpire, BaseMod

3. **Adding Transform Configuration:**
   - Modify `Config.java` to add transform parameters
   - Update `SlayTheRelicsExporter.receivePostInitialize()` to add sliders for offsetX/Y, scaleX/Y
   - Update `GameState.java` to include transform in serialized JSON
   - Update `GameStateManager.java` to send new fields to backend

4. **Backend Schema Update:**
   - Update `backend/models/game_state.go` to add Transform struct
   - No API changes needed (existing POST endpoint handles arbitrary JSON)
   - Rebuild: `cd backend && go build`

5. **Extension Transform Logic:**
   - Add Transform interface in `src/components/App/App.tsx`
   - Create helper functions: `transformX()`, `transformY()`, `transformHitbox()`
   - Update `Relic.tsx`, `Potion.tsx`, `Deck.tsx` to apply transforms
   - Add calibration UI component (optional but recommended)
   - Build: `cd slay-the-relics-extension && npm run build`

6. **Testing:**
   - Use Twitch Developer Rig for local testing
   - Set up non-fullscreen game window in OBS
   - Configure transform values in mod UI
   - Verify alignment in extension preview

7. **Steam Workshop Publishing:**
   - Assets in `/mod/steam/` (icon, description, README)
   - Upload via Steam Workshop Upload Tool
   - Reference existing workshop item: https://steamcommunity.com/sharedfiles/filedetails/?id=3048891690

8. **Extension Deployment:**
   - Twitch extension dashboard: https://dashboard.twitch.tv/extensions/ebkycs9lir8pbic2r0b7wa6bg6n7ua
   - Upload new extension version with transform support
   - Update configuration service documentation

**Key Files to Modify:**
```
mod/src/main/java/str_exporter/
├── SlayTheRelicsExporter.java      # Add UI sliders for transform
├── config/Config.java               # Add transform config persistence
├── game_state/GameState.java        # Add transform to JSON output
└── game_state/GameStateManager.java # Send transform to backend

backend/models/
└── game_state.go                    # Add Transform struct

slay-the-relics-extension/src/
├── components/App/App.tsx           # Receive transform from backend
├── components/Relic/Relic.tsx       # Apply transform to positions
├── components/Potion/Potion.tsx     # Apply transform to positions
├── components/Deck/Deck.tsx         # Apply transform to button positions
└── components/Tip/Tip.tsx           # Apply transform to hitboxes
```

**Additional Resources:**
- BaseMod wiki: https://github.com/daviscook477/BaseMod/wiki
- Twitch Extensions docs: https://dev.twitch.tv/docs/extensions
- Slay the Spire modding Discord: (link in README)
- Project Discord: https://discord.gg/744R7j74

---

## Human TLDr (Practical Guide)

### What is Slay the Relics?

Slay the Relics is a Twitch extension that shows your Slay the Spire game info overlaid on your stream. When you're streaming the game, viewers can see:
- Your full deck with card art
- Your relics and potions with descriptions
- Your current combat piles (draw, discard, exhaust)
- The dungeon map and your path
- Powers, orbs, and other buffs/debuffs

It looks just like the in-game UI, perfectly positioned over the actual game.

### How Does It Work?

**Three Parts Working Together:**

1. **The Mod (runs on your PC):**
   - Installed via Steam Workshop
   - Watches your game and captures what's happening
   - Sends updates to the cloud server
   - Has a config menu to set sync delay with your stream

2. **The Server (runs on baalorlord.tv):**
   - Receives updates from your mod
   - Keeps track of your current game state
   - Pushes updates to Twitch when things change
   - Makes sure viewers see the right info

3. **The Extension (runs in viewers' browsers):**
   - Shows the overlay on top of your stream
   - Gets updates from Twitch in real-time
   - Displays cards, relics, tooltips exactly where they are in-game
   - Viewers can click cards to zoom in and see details

### Important Current Limitation

**The extension ONLY works if your game fills the entire stream canvas** (like you're playing fullscreen). If you play windowed or have other elements on screen, the overlay positions won't match the actual game.

This is because the extension assumes:
- Your stream is 1920x1080
- The game fills 100% of the stream
- Game elements are always in the exact same spot

### What's Been Modified From the Original?

This appears to be the original repository, not a fork. Recent changes include:

1. **Potion Positioning Fix:** The mod now tells the extension exactly where potions are (position changes based on how many relics you have)

2. **Bottled Card Support:** Cards in bottles now show correctly with the bottle icon

3. **Special Card Handling:** Cards like Searing Blow and Genetic Algorithm show correct values based on upgrades

4. **Map View:** Added a full dungeon map viewer (press Map button to see your path)

5. **Combat Piles:** Can now see your draw, discard, and exhaust piles during combat

6. **Performance Improvements:** Moved from constant polling to event-based updates, uses less bandwidth

7. **Better Tooltips:** Split into frequent and rare updates to save bandwidth

### How to Make a Scalable Version for Non-Fullscreen Streams

**The Problem:**
Right now, if your game window only takes up part of your stream (e.g., 75% width, positioned in center), the overlay won't align. The deck button will be in the wrong place, relics won't line up, tooltips will be offset.

**What You'd Need to Build:**

**Step 1: Capture Window Position (Mod Changes)**
- Add configuration UI in the mod to set where your game is on stream
- Sliders for: "Game X Position", "Game Y Position", "Game Width %", "Game Height %"
- Example: Game is centered, 75% width → X=12.5%, Y=0%, Width=75%, Height=100%
- Save these settings in mod config
- Send them to the server with every game update

**Step 2: Apply Transform (Extension Changes)**
- Extension receives the position info
- Before placing anything on screen, apply math:
  ```
  Real Position = Stream Offset + (Game Position × Game Scale)
  ```
- Example: Deck button is at 95% in game
  - Game starts at 12.5% on stream and is 75% wide
  - Real position: 12.5% + (95% × 75%) = 83.75%

**Step 3: Add Calibration Helper**
- Show test overlay while setting up
- Grid or markers that match key game elements
- Adjust sliders until overlay aligns perfectly
- Save the calibration for that stream layout

**Files You'd Modify:**

In the **Mod** (`/mod/src/main/java/str_exporter/`):
- `SlayTheRelicsExporter.java` - Add UI sliders for position/scale
- `Config.java` - Save/load the position settings
- `GameState.java` - Include position in data sent to server

In the **Extension** (`/slay-the-relics-extension/src/components/`):
- `App.tsx` - Receive and store position settings
- `Relic.tsx`, `Potion.tsx`, `Deck.tsx` - Apply position transform to all elements
- Create new `Calibration.tsx` - Helper UI for streamers to align

In the **Backend** (`/backend/models/`):
- `game_state.go` - Add fields for position/scale info

**Testing Your Changes:**
1. Build the mod: `cd mod && ./gradlew build`
2. Copy JAR to your Slay the Spire mods folder
3. Build the extension: `cd slay-the-relics-extension && npm run build`
4. Use Twitch Developer Rig to test locally
5. Set up a windowed game capture in OBS
6. Adjust the position sliders until everything lines up
7. Play a run and verify alignment stays correct

**Challenges You'll Face:**
- Java can't detect your stream canvas size, only the game window
- You need to manually measure/configure stream layout
- Different streaming software works differently
- If you change scenes/layouts, need to recalibrate
- Need good UI/UX for the calibration process (very important!)

**Recommended Features for Your Version:**
1. **Presets:** Save multiple configurations for different stream layouts
2. **Auto-detect common sizes:** 1920x1080 full, 1440x1080 4:3, etc.
3. **Visual guides:** Show alignment grid in-game while configuring
4. **Quick toggle:** Hotkey to switch between presets mid-stream
5. **Stream canvas input:** Ask streamer for their base resolution
6. **Position wizard:** Step-by-step guide for first-time setup

### Getting Started Guide

**To Explore the Code:**
1. Backend (Go server): `/backend/main.go`
2. Mod (Java): `/mod/src/main/java/str_exporter/SlayTheRelicsExporter.java`
3. Extension (React): `/slay-the-relics-extension/src/components/App/App.tsx`

**To Build Locally:**
```bash
# Build the mod
cd mod
./gradlew build
# Output: mod/build/libs/SlayTheRelicsExporter.jar

# Build the backend
cd backend
go build
# Output: backend/slaytherelics

# Build the extension
cd slay-the-relics-extension
npm install
npm run build
# Output: slay-the-relics-extension/dist/
```

**To Test:**
1. Install mod JAR in `SlayTheSpire/mods/`
2. Run backend: `cd backend && ./slaytherelics`
3. Run extension dev server: `cd slay-the-relics-extension && npm run dev`
4. Use Twitch Developer Rig to load extension
5. Start Slay the Spire with mods enabled
6. Configure Twitch connection in mod settings
7. Start a run and watch extension update

**Helpful Documentation:**
- Architecture overview: `/docs/README.md`
- User setup guide: `/README.md`
- Beta testing notes: `/docs/BETA.md`
- Slay the Spire modding: BaseMod wiki (https://github.com/daviscook477/BaseMod/wiki)

**Getting Help:**
- Project Discord: https://discord.gg/744R7j74
- Issues: https://github.com/Spireblight/slay-the-relics/issues
- Modding community has resources for working with game's coordinate system

### Key Takeaway

This is a well-architected, production-quality Twitch extension with ~50 commits of active development. To make it work for non-fullscreen layouts, you need to add coordinate transformation logic that accounts for where the game window actually is on the stream canvas, not just assume it fills the whole screen. The main work is in the mod configuration UI and extension positioning math.

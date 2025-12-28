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


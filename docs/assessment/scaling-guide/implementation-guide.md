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


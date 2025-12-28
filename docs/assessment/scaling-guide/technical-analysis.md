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


# Transform Feature - Non-Fullscreen Layout Support

## Overview

The Transform feature enables Slay the Relics extension to work with non-fullscreen game layouts. Previously, the extension required the game to fill the entire 1920x1080 stream canvas. With this feature, streamers can position the game window anywhere on their stream and configure the extension to align correctly.

## How It Works

### Architecture

The transform system uses a simple coordinate transformation approach:

1. **Mod Layer (Java)** - Captures transform configuration from streamer
2. **Backend Layer (Go)** - Passes transform data through to extension
3. **Extension Layer (TypeScript)** - Applies transforms to all UI element positions

### Transform Parameters

Four parameters define the transformation:

- `offsetX` (%) - Horizontal offset of game window from left edge of stream
- `offsetY` (%) - Vertical offset of game window from top edge of stream
- `scaleX` (%) - Width of game window as percentage of stream width (100 = full width)
- `scaleY` (%) - Height of game window as percentage of stream height (100 = full height)

### Coordinate Transformation

Original game coordinates are transformed to stream coordinates using:

```
streamX = offsetX + (gameX × scaleX / 100)
streamY = offsetY + (gameY × scaleY / 100)
```

## Configuration

### In-Game Mod Settings

1. Open Slay the Spire with mods enabled
2. Main Menu → Mods → Slay the Relics Exporter → Config
3. Adjust Transform sliders:
   - **X Offset** (0-100%): Move game window left/right
   - **Y Offset** (0-100%): Move game window up/down
   - **Width Scale** (0-200%): Adjust game window width
   - **Height Scale** (0-200%): Adjust game window height
4. Click **Save** to apply settings

### Default Values

- **Fullscreen (no transform)**:
  - X Offset: 0%
  - Y Offset: 0%
  - Width Scale: 100%
  - Height Scale: 100%

- **Example: Game centered at 75% width**:
  - X Offset: 12.5%
  - Y Offset: 0%
  - Width Scale: 75%
  - Height Scale: 100%

## Implementation Details

### Modified Components

#### Mod (Java)

**Files Changed:**
- `mod/src/main/java/str_exporter/config/Config.java` - Added transform getters/setters
- `mod/src/main/java/str_exporter/game_state/Transform.java` - New transform data class
- `mod/src/main/java/str_exporter/game_state/GameState.java` - Added transform field
- `mod/src/main/java/str_exporter/game_state/GameStateManager.java` - Updates transform from config
- `mod/src/main/java/str_exporter/SlayTheRelicsExporter.java` - UI sliders for configuration

#### Backend (Go)

**Files Changed:**
- `backend/slaytherelics/game_state.go` - Added Transform struct to GameState and GameStateUpdate

#### Extension (TypeScript/React)

**Files Changed:**
- `slay-the-relics-extension/src/utils/transform.ts` - Transform utilities (NEW)
- `slay-the-relics-extension/src/components/App/App.tsx` - Receives and distributes transform
- `slay-the-relics-extension/src/components/Relic/Relic.tsx` - Applies transform to relic positions
- `slay-the-relics-extension/src/components/Potion/Potion.tsx` - Applies transform to potion positions
- `slay-the-relics-extension/src/components/Deck/Deck.tsx` - Applies transform to deck button positions
- `slay-the-relics-extension/src/components/Tip/Tip.tsx` - Applies transform to tooltip hitboxes

### Transform Utilities

**Location**: `slay-the-relics-extension/src/utils/transform.ts`

Key functions:
- `transformX(x, transform)` - Transform X coordinate
- `transformY(y, transform)` - Transform Y coordinate
- `transformHitBox(hitbox, transform)` - Transform entire hitbox
- `DEFAULT_TRANSFORM` - Constant for fullscreen (no transformation)

### Data Flow

1. Streamer configures transform in mod UI
2. Config values stored in `slayTheRelicsExporterConfig`
3. `GameStateManager` reads config and creates `Transform` object
4. `Transform` included in JSON sent to backend
5. Backend forwards `Transform` to Twitch PubSub
6. Extension receives `Transform` in game state updates
7. All positioning components apply transform before rendering

## Testing

### Manual Testing

1. **Fullscreen Test**:
   - Set all transforms to default (0, 0, 100, 100)
   - Verify overlay aligns perfectly with fullscreen game

2. **Windowed Test**:
   - Play game in windowed mode (e.g., 75% width, centered)
   - Configure: X Offset = 12.5%, Width Scale = 75%
   - Verify deck buttons, relics, potions align with game

3. **Edge Cases**:
   - Test with very small scale (50%)
   - Test with offset at edges (90%+)
   - Verify tooltips remain correctly positioned

### Automated Testing

Build tests:
```bash
# Backend
cd backend
go build

# Mod
cd mod
./gradlew build

# Extension
cd slay-the-relics-extension
npm install
npm run build
```

## Future Enhancements

Potential improvements for the transform feature:

1. **Presets** - Save/load common layout configurations
2. **Auto-calibration** - Visual alignment helper overlay
3. **Stream canvas input** - Specify stream resolution for better defaults
4. **Real-time preview** - See transform changes without saving
5. **Per-scene transforms** - Different settings for different OBS scenes

## Troubleshooting

### Overlay misaligned

- Double-check transform values match your stream layout
- Verify game window position hasn't changed
- Try resetting to defaults (0, 0, 100, 100) and reconfiguring

### UI elements missing

- Ensure scale values aren't too small (< 20%)
- Check offset values aren't pushing UI off-screen (> 90%)

### Performance issues

- Transform calculations are lightweight
- If experiencing lag, check network/stream encoding settings

## Related Documentation

- [Architecture Overview](assessment/architecture/overview.md)
- [Extension Layer Details](assessment/architecture/extension-layer.md)
- [Scaling Guide](assessment/scaling-guide/README.md)

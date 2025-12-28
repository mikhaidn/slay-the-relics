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


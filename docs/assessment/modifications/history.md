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


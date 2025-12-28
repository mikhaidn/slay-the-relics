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


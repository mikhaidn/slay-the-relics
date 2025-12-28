### Architecture Overview

**Slay the Relics** is a three-tier distributed Twitch extension system for "Slay the Spire" that displays real-time game state overlays on stream. The architecture follows an event-driven pattern with state management at the backend tier.

**Data Flow:**
```
Mod (Java) → Backend (Go) → Twitch PubSub → Extension (React/TypeScript)
     ↓           ↓                              ↓
  Game State  State Mgmt                   UI Rendering
  Capture     + Diffing                    + Decompression
```


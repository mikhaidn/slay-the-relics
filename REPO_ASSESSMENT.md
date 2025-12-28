# Slay the Relics - Repository Assessment

> **Note:** This assessment has been split into organized documentation for easier navigation.
> See [docs/assessment/README.md](docs/assessment/README.md) for the full structure.

## Quick Links

### Technical Deep Dive (Claude TLDr)
- **Architecture:** [Overview](docs/assessment/architecture/overview.md) | [Mod Layer](docs/assessment/architecture/mod-layer.md) | [Backend](docs/assessment/architecture/backend-layer.md) | [Extension](docs/assessment/architecture/extension-layer.md)
- **Project Info:** [Features](docs/assessment/architecture/key-features.md) | [History](docs/assessment/modifications/history.md)
- **Scaling:** [Technical Analysis](docs/assessment/scaling-guide/technical-analysis.md) | [Documentation](docs/assessment/getting-started/documentation-pointers.md)

### Practical Guide (Human TLDr)
- **Understanding:** [What is it?](docs/assessment/getting-started/what-is-it.md) | [How does it work?](docs/assessment/getting-started/how-it-works.md)
- **Modifications:** [Recent Changes](docs/assessment/modifications/recent-changes.md)
- **Building:** [Scaling Guide](docs/assessment/scaling-guide/README.md) | [Building Locally](docs/assessment/getting-started/building-locally.md)

## Summary

**Slay the Relics** is a production-quality Twitch extension that overlays Slay the Spire game state on streams in real-time. It consists of three components:

1. **Java Mod** - Captures game state via BaseMod framework
2. **Go Backend** - Manages state and broadcasts via Twitch PubSub
3. **React Extension** - Displays overlay in viewers' browsers

**Current Limitation:** Requires fullscreen game capture (1920x1080). To support non-fullscreen layouts, you need to implement coordinate transformation. See [Scaling Guide](docs/assessment/scaling-guide/README.md) for details.

**Key Recent Changes:**
- Dynamic potion positioning
- Bottled card support
- Map visualization
- Combat pile visibility
- Architecture rewrite (event-driven)

## Repository Structure

```
docs/assessment/
├── README.md                          # Main navigation index
├── architecture/                       # Technical architecture details
│   ├── overview.md                    # High-level system design
│   ├── mod-layer.md                   # Java mod implementation
│   ├── backend-layer.md               # Go server architecture
│   ├── extension-layer.md             # React frontend details
│   └── key-features.md                # Notable capabilities
├── modifications/                      # Change history
│   ├── history.md                     # Origin and modifications
│   └── recent-changes.md              # Recent commit summary
├── scaling-guide/                      # Non-fullscreen support
│   ├── README.md                      # Scaling overview
│   ├── current-limitation.md          # Why fullscreen is required
│   ├── technical-analysis.md          # Root cause analysis
│   └── implementation-guide.md        # Step-by-step guide
└── getting-started/                    # Developer guides
    ├── what-is-it.md                  # Plain English overview
    ├── how-it-works.md                # System explanation
    ├── building-locally.md            # Build instructions
    ├── documentation-pointers.md      # File references
    └── summary.md                     # Key takeaways
```

## For LLM Context Preservation

This documentation is organized into small, focused files to:
- Preserve context efficiently in future conversations
- Allow selective loading of relevant sections
- Maintain clear separation of concerns
- Enable easy updates to specific components

Each file is self-contained with clear headings and can be read independently or as part of the whole.

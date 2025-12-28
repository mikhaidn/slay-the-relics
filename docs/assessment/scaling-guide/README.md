# Scaling Guide - Non-Fullscreen Stream Support

This guide explains how to modify Slay the Relics to support non-fullscreen game layouts.

## Understanding the Problem

- [Current Limitation](current-limitation.md) - Why fullscreen is required
- [Technical Analysis](technical-analysis.md) - Root cause and architecture constraints

## Building the Solution

- [Implementation Guide](implementation-guide.md) - Step-by-step instructions for creating a scalable version

## Overview

The current extension assumes the game fills 100% of the stream canvas (1920x1080 fullscreen). To support windowed or scaled layouts, you need to:

1. **Capture game window geometry** - Where is the game positioned on the stream canvas?
2. **Send transform matrix** - Offset and scale values from mod to backend
3. **Apply transformations** - Convert game coordinates to stream coordinates in extension
4. **Add calibration UI** - Let streamers align the overlay visually

All components (mod, backend, extension) need modifications to pass and apply the transform.

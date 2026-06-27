# CoordsDisplay — Fabric Mod for Minecraft 1.21.1

Shows your XYZ coordinates as a HUD overlay with full customisation via ModMenu.

---

## Features

| Feature | Details |
|---|---|
| **Keybind mode** | **HOLD** (default) — coords visible while key held · **TOGGLE** — press once to show, press again to hide |
| **Keybind** | Default: `Tab` — changeable in **Options → Controls → CoordsDisplay** |
| **Coordinate format** | Numbers only · XYZ labels · Horizontal (H/V) · Vertical (stacked) |
| **Per-axis colours** | Independent RGB colour picker for X, Y, and Z values |
| **Position presets** | Top-Left · Top-Right · Bottom-Left · Bottom-Right · Custom (pixel X/Y) |
| **ModMenu config screen** | Full settings UI — no manual file editing needed |

### Defaults
- Mode: **HOLD**
- Key: **Tab**
- Format: **Numbers only** (`-312  64  128`)
- Colour: **White** for all axes
- Position: **Top-Left**

---

## Required Dependencies (put all JARs in `mods/`)

1. **Fabric Loader** ≥ 0.16.0 — https://fabricmc.net/use/
2. **Fabric API** 0.102.0+1.21.1 — https://modrinth.com/mod/fabric-api
3. **ModMenu** 11.x — https://modrinth.com/mod/modmenu
4. **Cloth Config** 15.x (Fabric) — https://modrinth.com/mod/cloth-config

---

## Building from Source

### Prerequisites
- JDK 21 — https://adoptium.net/
- Internet connection (Gradle downloads Minecraft mappings on first build)

### Steps

```bash
# 1. Clone / unzip this project
cd coordsmod

# 2. Build
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows

# 3. Grab the JAR
#    build/libs/coordsmod-1.0.0.jar
```

Copy `coordsmod-1.0.0.jar` plus all four dependency JARs into your `.minecraft/mods/` folder.

---

## File Structure

```
src/main/java/com/coordsmod/
  CoordsMod.java      ← entrypoint, keybind registration, HUD hook, tick handler
  CoordsConfig.java   ← all settings (GSON serialised), ModMenu + Cloth Config screen
  CoordsHud.java      ← HUD rendering (all 4 formats, position logic, colour rendering)

src/main/resources/
  fabric.mod.json                    ← mod metadata & entrypoints
  assets/coordsmod/lang/en_us.json  ← keybind display name
```

---

## Config File

Saved automatically to `.minecraft/config/coordsmod.json`:

```json
{
  "displayMode": "HOLD",
  "toggleVisible": false,
  "coordFormat": "NUMBERS_ONLY",
  "colorX": -1,
  "colorY": -1,
  "colorZ": -1,
  "positionPreset": "TOP_LEFT",
  "customX": 2,
  "customY": 2
}
```

Colors are stored as signed ARGB integers (`-1` = `0xFFFFFFFF` = opaque white).

---

## Changing the Keybind

**In-game:** `Options → Controls → CoordsDisplay → Show Coordinates`

The keybind is registered with Fabric's standard keybind API so it appears in the vanilla Controls screen and supports conflict detection.

---

## Coordinate Formats

| Format | Output example |
|---|---|
| `NUMBERS_ONLY` | `-312  64  128` |
| `XYZ_LABELS` | `X: -312  Y: 64  Z: 128` |
| `HORIZONTAL` | `H: -312  V: 64` (horizontal=X, vertical=Y) |
| `VERTICAL` | Three stacked lines: X / Y / Z |

Labels ("X:", "Y:", "Z:") always render in white; only the number values use the configured per-axis colour.

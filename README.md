# PolyCrosshair

[![CurseForge downloads](https://img.shields.io/curseforge/dt/1147783?style=flat&logo=curseforge&label=CurseForge&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/polycrosshair)

PolyCrosshair replaces the vanilla crosshair with one you draw yourself in a pixel editor. It does nothing until you turn on "Enabled" in its settings.

## Features

### Pixel editor

- Draw on a square canvas of 7, 15, 31 or 63 pixels per side. The grid size also sets how large the crosshair is on screen.
- Left click draws with the pen color you pick, and right click erases.
- Switch to the circle tool to drag a circle out from its center, with a thickness you pick. Right click a circle to remove it.
- Mirror each stroke horizontally, vertically or into all four quadrants.
- Undo and redo up to 128 steps.
- Save a crosshair with the Save button. Saved crosshairs appear next to the canvas, and a click loads one back.

| Input | Action |
|-------|--------|
| Left click | Draw a pixel |
| Right click | Erase a pixel |
| `Delete` | Clear the canvas |
| `S` | Show or hide the guide lines |
| Hold `Shift` | Lock the stroke to one direction |
| `Z` | Undo |
| `Y` | Redo |
| `W` | Switch to the next mirror mode |
| `C` | Switch between the pen and the circle tool |

### Friendly and enemy crosshairs

You can draw two more crosshairs that replace the normal one while you look at a friendly or an enemy target. Both are off by default.

- If you and the target are both on a scoreboard team, a teammate is friendly and anyone else is an enemy.
- Otherwise monsters and other players are enemies, and animals are friendly.
- A button copies every setting from the normal crosshair onto the friendly or enemy one.

### Elytra indicator

On Minecraft 1.21.1 and newer, PolyCrosshair draws a small indicator below the crosshair while you wear an elytra. It is on by default. You can redraw it in the pixel editor or turn it off.

### Options for each crosshair

The normal, friendly and enemy crosshairs each have their own copy of these options.

- Outline draws a border around the crosshair. You set its color and a thickness from 0 to 1 crosshair pixels. It is off by default.
- Vanilla blending inverts the colors behind the crosshair, the way the vanilla crosshair does. It is on by default.
- Health color shift recolors the crosshair when your health is at or below a threshold from 1 to 20, where 20 is full health. It is off by default.
- Dynamic bow spreads the crosshair apart when you start to draw a bow and closes it as the shot charges. It is off by default.
- Dynamic attack spreads the crosshair apart after you attack and closes it as the cooldown recovers. It is off by default, and Minecraft 1.8.9 does not have it.
- Custom scale draws the crosshair at a scale from 0.5 to 6, whatever your GUI scale is. It is off by default.
- "Show in Third Person" is off by default, except on 1.8.9.
- "Always Visible in Spectator" keeps the crosshair on screen the whole time you spectate. It is off by default.

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2 and 26.3 on Fabric.

PolyCrosshair needs [OneConfig](https://modrinth.com/mod/oneconfig) and Fabric Language Kotlin.

## Support

- Report bugs and ask questions on the [Polyfrost Discord](https://polyfrost.org/discord).
- Check [Polyfrost Status](https://status.polyfrost.org/) if a Polyfrost service is down.

## Building

```sh
./gradlew ":26.3:build"
```

Replace `26.3` with any supported Minecraft version. The build needs JDK 21 and JDK 25 installed.

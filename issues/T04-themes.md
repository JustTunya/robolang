# T04 — Space and Jungle themes

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T04-themes` · **Owner reviews before merge**

## Goal

Implement the Abstract Factory pattern: two concrete factories, each producing a consistent family of five products (§6).

## Owns

- `src/main/java/robolang/theme/ThemeRegistry.java` (method bodies)
- `src/main/java/robolang/theme/space/**`, `src/main/java/robolang/theme/jungle/**`
- matching tests; `src/test/java/robolang/theme/ThemePreview.java`

## Contracts used

`ThemeFactory`, `Paintable`, `RobotView`, `WallTile`, `FloorTile`, `Collectible`, `GoalTile`, `Direction`.

## Requirements

1. `SpaceThemeFactory` and `JungleThemeFactory`: public, stateless, one instance each (D7). Product classes are **package-private**.
2. Products per §6.1:

   | Product | Space | Jungle |
   |---|---|---|
   | RobotView | Rover | Monkey |
   | WallTile | Asteroid | Tree |
   | FloorTile | Metal plate | Grass |
   | Collectible | Energy crystal | Banana |
   | GoalTile | Landing pad | Hut |

3. Painting: Java2D shapes only (no image files), antialiased, stays inside the cell rectangle, readable from 16 px to 96 px cells. The two themes must be clearly distinct at a glance (palette and shapes).
4. `RobotView.paint(g, cell, facing, progress)` rotates the robot to face `facing`; `progress` (0.0–1.0) drives a small motion cue (e.g. wheel/arm offset). Moving between cells is the grid's job, not the product's.
5. Messages: `pickupMessage()` and `crashMessage()` return theme-specific text (e.g. "Crystal charged", "Banana collected", "Rover hit an asteroid").
6. `ThemeRegistry`: `all()` returns Space then Jungle; `byName` is case-insensitive and falls back to `defaultTheme()` (Space) for unknown names.
7. `ThemePreview` (test sources, has a `main`): renders every product of every registered theme at 64 px in all four facings into `target/theme-preview/*.png`. Reviewers use it to look at the art.

## Acceptance criteria

- [ ] Each factory returns a non-null product of every type; repeated calls are consistent (same family).
- [ ] Every product paints into a 16 px, 48 px and 96 px off-screen `BufferedImage` without exception and changes at least some pixels; nothing is drawn outside the cell (check a border of pixels).
- [ ] Robot rendering differs between facings (pixel comparison).
- [ ] `ThemeRegistry` behavior tested (order, case-insensitive lookup, fallback).
- [ ] `ArchitectureTest` passes (products package-private, no `javax.swing`).
- [ ] PR description includes the `ThemePreview` PNGs or a note on where to find them.
- [ ] `mvn -B verify` green.

## Out of scope

Retro theme (T10). Grid rendering and animation (T05).

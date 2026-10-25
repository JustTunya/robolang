# T10 — Retro theme

**Wave:** 2 · **Depends on:** T04 · **Branch:** `feat/T10-retro-theme` · **Priority:** Could (FR-41)

## Goal

Add a third product family **without changing any existing code except one registration line**. The diff of this PR is presentation material: it proves the Open/Closed side of Abstract Factory (§10.7, NFR-06).

## Owns

- `src/main/java/robolang/theme/retro/**`
- matching tests

## Allowed shared edits

`theme/ThemeRegistry.java`: one line registering `RetroThemeFactory`. Nothing else.

## Requirements

1. `RetroThemeFactory` (public, stateless, one instance) and five package-private products: Pixel bot, Brick, Dark tile, Coin, Flag (§6.1).
2. Pixel-art style: each product drawn on an 8×8 virtual pixel grid with `fillRect`, scaled to the cell; nearest-neighbor look (no antialiasing).
3. Theme-specific messages ("Coin collected", "Bot bumped a brick").
4. Follow every T04 product rule (stay in the cell, 16–96 px, facing rotation).

## Acceptance criteria

- [ ] Same product tests as T04 pass for Retro (reuse or parameterize T04's test helpers if they are in your test package; otherwise copy them).
- [ ] `git diff --stat main` in the PR description shows changes only under `theme/retro/` and one line in `ThemeRegistry`.
- [ ] Retro appears in the theme selector and renders level 01 (screenshot).
- [ ] `mvn -B verify` green.

# T01 — Level loading

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T01-level-loading`

## Goal

Parse and validate level files (§5.1) and load the campaign from the classpath.

## Owns

- `src/main/java/robolang/model/LevelLoader.java` (method bodies)
- `src/main/resources/levels/index.txt`
- `src/main/resources/levels/01-first-steps.level` and `01-first-steps.solution.rl` (one sample level for development; T09 takes over this folder later)
- `src/test/java/robolang/model/LevelLoaderTest.java`, `src/test/resources/levels-test/**`

## Contracts used

`Level`, `Variant`, `Grid`, `Position`, `Direction`, `CellType`, `LevelFormatException` (all from T00).

## Requirements

1. `LevelLoader.parse(String id, String text)`:
   - Header lines `key: value` for `title`, `briefing`, `keywords`, `par-instructions`, `par-actions`. All required. Unknown keys are an error.
   - One or more `grid:` blocks; each block's rows run until the next `grid:` or end of file. Blank lines end a block.
   - Symbols per §5.1: `#` wall, `.` floor, `*` floor with item, `G` goal, `^ > v <` robot start (the cell is floor).
   - Keywords are split on whitespace, upper-cased, and must be valid RoboLang keywords from §4.1. Keep the valid set as a constant in `model` (it cannot import `lang`); a test compares it with `TokenType` keywords.
   - Validation per §5.1: equal row lengths, size 3×3 to 12×12, exactly one robot and one goal per grid, par values positive integers.
   - Every error is a `LevelFormatException` naming the level id and the 1-based line number, e.g. `05-staircase line 12: row length 9, expected 11`.
2. `LevelLoader.loadAll()`:
   - Reads `levels/index.txt` from the classpath: one base name per line, `#` comments and blank lines ignored. (Listing a classpath directory does not work inside a JAR, so the index is required.)
   - For each name, loads `levels/<name>.level` and, if present, `levels/<name>.solution.rl` as `referenceSolution`. Level id = base name.
   - An invalid level is logged with `System.Logger` at WARNING and skipped. The method never throws for a bad level file.
   - Returns levels in index order.
3. Ship one valid sample level `01-first-steps` (straight corridor, keywords `MOVE`) and its solution, listed in `index.txt`.

## Acceptance criteria

- [ ] Valid single-variant and multi-variant files parse to the expected `Level` (robot start, facing, goal, items, pars).
- [ ] Each validation rule in §5.1 has a failing-input test with the expected message and line number.
- [ ] Unknown header key and missing header key are rejected.
- [ ] Keyword constant equals the keyword set of `TokenType` (test).
- [ ] `loadAll()` returns `01-first-steps` with its reference solution; a broken file in a test index is skipped, not thrown.
- [ ] `mvn -B verify` green.

## Out of scope

Campaign content beyond level 01 (T09). Running solutions (T09).

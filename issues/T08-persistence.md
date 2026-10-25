# T08 — Persistence and settings

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T08-persistence`

## Goal

Store progress, last programs and settings locally (FR-11, FR-43, FR-51, FR-52). Pure Java, no UI.

## Owns

- `src/main/java/robolang/app/ProgressStore.java`, `src/main/java/robolang/app/Settings.java` (method bodies)
- matching tests

## Contracts used

`ProgressStore`, `Settings`, `BestResult`, `Outcome`.

## Requirements

1. Storage directory passed in the constructor (`Main` will pass `~/.robolang`). Files: `progress.properties`, `settings.properties`, `programs/<levelId>.rl`.
2. `record(levelId, outcome)`: ignored unless `outcome.success()`. Keeps the best result: more stars wins; on a tie, fewer instructions, then fewer actions.
3. `isSolved` true once any successful outcome was recorded.
4. `saveProgram` / `lastProgram`: program text stored verbatim (UTF-8) in its own file.
5. `resetAll()`: deletes progress and programs; keeps settings.
6. Settings defaults: theme `Space`, demo mode off.
7. Writes are atomic (write a temp file, then `Files.move` with `ATOMIC_MOVE`, falling back to `REPLACE_EXISTING`).
8. A missing or corrupt file is treated as empty and logged (`System.Logger`, WARNING). Never throws on read.

## Acceptance criteria

- [ ] Best-result rules tested (better stars, tie-breaks, failure ignored).
- [ ] Round-trip across new instances on the same directory (`@TempDir`).
- [ ] Program text with newlines, `#` and non-ASCII characters round-trips exactly.
- [ ] `resetAll` keeps settings.
- [ ] Corrupt `progress.properties` → empty progress, no exception.
- [ ] `mvn -B verify` green.

## Out of scope

UI wiring (T11).

# T05 — UI shell

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T05-ui-shell`

## Goal

Build the main window and its panels (§7.1, §7.2) with a stable public API that T06, T07 and T11 plug into. No execution logic.

## Owns

- `src/main/java/robolang/ui/**` **except** `ExecutionController.java`, `ui/execution/**`, `AstTreePanel.java`, `ui/ast/**`, `ui/briefing/**`, `ui/console/**`, `ui/highlight/**`, `ui/menu/**`
- `src/main/java/robolang/app/Main.java`
- matching tests

You own `MainFrame` and `CodeEditorPanel`; later tasks may add wiring lines only.

## Contracts used

`ThemeFactory`, `ThemeRegistry`, `WorldSnapshot`, `Level`, `LevelLoader`, `SourceRange`, `AstNode`, `Program`, `Outcome`.
`LevelLoader` and `ThemeRegistry` may still be stubs (T01, T04 run in parallel): handle `UnsupportedOperationException` and empty lists gracefully.

## Requirements — public API (frozen once merged)

- `enum ExecState { EDITING, RUNNING, PAUSED, SUCCEEDED, FAILED }`
- `interface ExecutionObserver` (default no-op methods, always called on the EDT):
  `programParsed(Program)`, `nodeEntered(AstNode)`, `runFinished(Outcome)`, `reset()`
- `interface ControlListener { onRun(); onStep(); onPause(); onStop(); onReset(); }`
- `MainFrame`: layout per §7.1 (editor | grid | right slot, control bar, status bar; menu bar Game / View / Help with placeholder items), minimum size 1280×800. Accessors `editor()`, `grid()`, `controls()`, `statusBar()`, `levelSelector()`, `themeSelector()`, and `setRightPanel(JComponent)` (placeholder panel until T07).
- `CodeEditorPanel`: `JTextPane` with line-number gutter, monospace font. `getProgram()`, `setProgram(String)`, `setReadOnly(boolean)`, `highlightRange(SourceRange, HighlightKind)` with `enum HighlightKind { EXECUTING, ERROR }`, `selectRange(SourceRange)`, `clearHighlights()`, `addEditListener(Runnable)`.
- `GridPanel`: `setTheme(ThemeFactory)` (repaints at once), `showSnapshot(WorldSnapshot)` (animates the robot from the previous position/facing over `setAnimationMillis(int)` with a `javax.swing.Timer`, ≥ 30 fps; 0 ms = jump), `setVariantCount(int)`, `selectVariant(int)` (variant tabs), cell size scales to the panel. Without a theme, paint plain colored squares (fallback).
- `ControlBar`: Run, Step, Pause, Stop, Reset buttons → `ControlListener`; `applyState(ExecState)` implements the enablement table in §7.3; speed slider 1–20 actions/s, default 4, last position = Instant; `int speed()` returns 0 for Instant; `setCounters(int actions, long ticks)`.
- `StatusBar`: `setMessage(String)`, `setStars(int)` (0–3).
- Level selector: combo of `Level` titles; `onLevelSelected(Consumer<Level>)`. Theme selector: combo of `ThemeRegistry.all()` names wired to `GridPanel.setTheme`.
- `Main`: sets up the frame on the EDT with levels from `LevelLoader.loadAll()` and the default theme. Shows level 1 in the grid if available.

## Acceptance criteria

- [ ] `ControlBar.applyState` matches every cell of the §7.3 table (pure mapping, unit-tested).
- [ ] Grid geometry (cell size, offsets for any panel size and grid size) is a pure class with tests.
- [ ] `GridPanel` paints a snapshot into an off-screen image headless, with and without a theme.
- [ ] `CodeEditorPanel` highlight and read-only behavior tested on the component (no window).
- [ ] App starts with stubs present (no level, no theme) without exceptions — manual check described in PR.
- [ ] Screenshot of the running window in the PR description.
- [ ] `mvn -B verify` green.

## Out of scope

Running programs (T06), AST tree (T07), persistence and dialogs (T11).

# T11 — UX and teaching features

**Wave:** 3 · **Depends on:** T06, T07, T08 · **Branch:** `feat/T11-ux`

## Goal

Finish the player experience and the teaching aids that make both patterns visible during the presentation.

## Owns

- `src/main/java/robolang/ui/briefing/**`, `src/main/java/robolang/ui/console/**`, `src/main/java/robolang/ui/highlight/**`, `src/main/java/robolang/ui/menu/**`
- matching tests

## Allowed shared edits

- `ui/MainFrame.java`: wiring (menus, console panel, dialogs, shortcuts).
- `ui/CodeEditorPanel.java`: attach the syntax highlighter behind the existing API; do not change public methods.
- `app/Main.java`: create `ProgressStore` / `Settings` on `~/.robolang`, apply FlatLaf.
- `pom.xml`: add FlatLaf (current stable) as the only new runtime dependency.

## Requirements

| Requirement | Behavior |
|---|---|
| FR-02, FR-03 | Level selector shows title, lock state and best stars; level n+1 unlocked when n solved. |
| FR-04 | Briefing dialog on level open: title, briefing, available keywords, both pars. |
| FR-05 | View → Demo mode (persisted): unlocks all levels; Game → Load reference solution. |
| FR-11 | Last program per level restored on open; saved on edit (debounced 1 s) and on level switch. |
| FR-13 | Syntax highlighting from `Lexer` tokens (keywords, conditions, numbers, comments, identifiers), debounced 150 ms; lexer errors leave the text unstyled. |
| FR-43 | Theme choice persisted and restored. |
| FR-50, FR-51 | Successful outcomes recorded via `ProgressStore`; status bar shows best stars. |
| FR-52 | Game → Reset progress, with a confirmation dialog. |
| FR-60 | Help → Pattern map: table of pattern role → class, read from code (`Statement.class.getPermittedSubclasses()`, `BoolExpression.class.getPermittedSubclasses()`, `ThemeRegistry.all()`). |
| FR-61 | Collapsible trace console. Factory trace: wrap the selected theme in a logging `TracingThemeFactory` decorator (in `ui/console`) that logs each `create*()` call and delegates. Interpret trace: an `ExecutionObserver` logging `label()` indented by tree depth; off by default (View → Trace mode). |
| §7.4 | Shortcuts F5, F10, F6, Shift+F5, Ctrl+R, Ctrl+T, Ctrl+L via `InputMap`/`ActionMap`. |
| Look | FlatLaf light; fall back to the system look-and-feel on failure. |

## Acceptance criteria

- [ ] Unlock logic and selector labels are pure functions with tests (solved sets, demo mode).
- [ ] Debounced save tested with an injectable clock or scheduler.
- [ ] Highlighter maps a token list to style runs (pure, tested).
- [ ] `TracingThemeFactory` logs one line per `create*()` call and returns the delegate's products (test).
- [ ] Pattern map lists all permitted node subclasses (test).
- [ ] Manual checklist in PR: every shortcut, briefing, demo mode, reset progress, trace console during a run.
- [ ] `mvn -B verify` green.

## Out of scope

Sound, level editor, localization (§13.4).

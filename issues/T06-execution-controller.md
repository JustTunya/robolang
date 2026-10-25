# T06 — Execution controller

**Wave:** 2 · **Depends on:** T01, T02, T03, T05 · **Branch:** `feat/T06-execution`

## Goal

Connect editor → parser → checker → runtime → grid, with Run / Step / Pause / Stop / Reset on a worker thread (§7.3, §10.5).

## Owns

- `src/main/java/robolang/ui/ExecutionController.java`, `src/main/java/robolang/ui/execution/**`
- matching tests

## Allowed shared edits

`ui/MainFrame.java`: wiring only (create the controller, register it as `ControlListener`, hook level selection) — about 20 lines.

## Contracts used

`Parser`, `StaticChecker`, `ProgramRunner`, `ExecutionListener`, `Outcome`, `ExecutionStoppedException`, and the T05 UI API (`ExecState`, `ExecutionObserver`, `ControlListener`, panels).

## Requirements

1. **State machine** as a pure class `ExecutionStateMachine`: states and transitions exactly as §7.3 (diagram + text). Every state change calls `ControlBar.applyState`.
2. **Run / Step from Editing:** parse and check against the selected level's keywords. On error: highlight the range as `ERROR`, show `formatted()` in the status bar, stay in Editing. On success: notify observers `programParsed`, start the run.
3. **Worker:** `SwingWorker` calls `ProgramRunner.run` with a pacing `ExecutionListener`:
   - `variantStarted` → publish (grid selects the variant and shows the initial snapshot).
   - `nodeEntered` → publish (observers `nodeEntered`, editor highlights the range as `EXECUTING`); coalesce so the EDT is never flooded.
   - `actionPerformed` → publish snapshot + counters, then wait: Running → `1000 / speed` ms; Paused → block on a `Semaphore`; Step → switch to Paused after this action.
   - **Instant speed:** no waits, no per-action publishing; show only the final state.
   - **Stop:** set a flag, release the semaphore; the listener throws `ExecutionStoppedException` on its next call.
4. **Finish:** on the EDT, notify `runFinished(Outcome)`; success → Succeeded with status `Solved! ★★☆ · 5 instructions · 14 actions`; failure → Failed, highlight `location` as `ERROR`, show the message.
5. Editor read-only in Running and Paused. An edit in Succeeded or Failed resets to Editing.
6. **Reset:** stop if running, show the level's first variant start state, clear highlights, notify `reset()`.
7. Selecting another level stops and resets.
8. Grid animation duration follows speed (`min(250, 1000/speed * 0.8)` ms).
9. Theme switching during a run must work without exceptions (grid owns rendering; the controller does nothing special).

## Acceptance criteria

- [ ] Every transition of the §7.3 state machine unit-tested on `ExecutionStateMachine`, including invalid events being ignored.
- [ ] Pacing listener tested without Swing: Step releases exactly one action; Stop while paused ends the run as `STOPPED`; Instant performs no sleeps.
- [ ] Parse/check errors keep the state in Editing and report the message (test with a fake view).
- [ ] Manual check in PR: run level 01 at speeds 1, 20, Instant; Step through; Stop while paused; switch theme mid-run.
- [ ] `mvn -B verify` green.

## Out of scope

AST tree (T07), persistence, briefing, shortcuts (T11).

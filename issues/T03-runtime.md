# T03 — Interpreter runtime

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T03-runtime` · **Owner reviews before merge**

## Goal

Implement the Interpreter pattern: every AST node interprets or evaluates itself against a `Context`.
Add a headless `ProgramRunner` that runs a program over all variants of a level and scores it.

## Owns

- `src/main/java/robolang/lang/ast/**` — **method bodies only** (no new components, no signature changes)
- `src/main/java/robolang/lang/runtime/**`
- matching tests

## Contracts used

All of `lang.ast`, `lang.runtime`, `lang.error`; `World`, `Level`, `Variant` from `model`.
The parser (T02) runs in parallel: **build ASTs by hand in tests**, do not use `Parser`.

## Requirements

**Nodes (§4.3, §4.4)** — each node calls `ctx.tick(this)` on entry, with these costs:

| Node | Ticks | Behavior |
|---|---|---|
| `MoveStmt` | 1 | `ctx.move(this)` |
| `TurnStmt` | 1 | `ctx.turn(this, direction)` |
| `PickStmt` | 1 | `ctx.pick(this)` |
| `BlockStmt` | 0 | runs statements in order |
| `RepeatStmt` | 1 per iteration | body `count` times |
| `WhileStmt` | 1 per check | evaluate condition, run body while true |
| `IfStmt` | 1 | evaluate once, run one branch |
| `CallStmt` | 1 | `ctx.enterCall(this)`, run `target().body()`, `ctx.exitCall()` in `finally` |
| conditions | 1 each | `AND`/`OR` short-circuit; no side effects |
| `Program.execute` | 0 | runs `main` |

- **FR-30:** no `switch`/`instanceof` over node types anywhere in `lang.runtime`.
- `instructionCount()` per §3.5: `MOVE`, `TURN`, `PICK`, `REPEAT`, `WHILE`, `IF`, `DEF`, `CALL` count 1 each; blocks sum children; `ProcDef` = 1 + body; `Program` = procedures + main; conditions 0; `ELSE` 0 (a nested `IF` in an `ELSE IF` counts its own 1).
- `label()`: `MOVE`, `TURN LEFT`, `PICK`, `REPEAT 4`, `WHILE NOT WALL_AHEAD`, `IF ON_ITEM`, `CALL zigzag`, `DEF zigzag`, `Block`, `Program`. Conditions render their expression, parenthesizing nested `AND`/`OR`: `NOT (ON_ITEM AND AT_GOAL)`.

**Context**
- `tick`: count; notify `listener.nodeEntered`; if stop was requested throw `ExecutionStoppedException`; if ticks exceed `maxTicks` throw `TickLimitException` (`Tick limit of 100 000 reached, possible infinite loop`) at the node.
- `move`: if `world.wallAhead()` throw `CrashException` (`robot crashed into a wall at (r, c)`, target cell); else move, count the action, notify `actionPerformed` with a fresh snapshot.
- `turn`, `pick` likewise (`EmptyPickException`: `nothing to pick up at (r, c)`).
- `enterCall` beyond `maxCallDepth` throws `RecursionLimitException` (`Call depth limit of 64 exceeded`).
- `requestStop` is thread-safe (`volatile`).

**ProgramRunner.run**
- For each variant (index 0-based internally, 1-based in messages): new `World`, new `Context`, `listener.variantStarted`, `program.execute(ctx)`.
- After a variant terminates normally: success iff robot on goal **and** no items left; else `INCOMPLETE` with `program ended without reaching the goal` or `program ended with N item(s) left`.
- Failure messages are prefixed `Variant <n>, ` (§11); stop at the first failing variant.
- `ExecutionStoppedException` → `STOPPED`, also when thrown by the listener (the UI stops a paused run this way).
- `Outcome` totals actions and ticks across executed variants; `instructionCount` from the program; `stars` from `Scoring`.

**Scoring:** 0 when unsolved, else 1 + (instructions ≤ par) + (actions ≤ par).

## Acceptance criteria

- [ ] Each node: behavior and exact tick/action counts on a hand-built AST and a small `World`.
- [ ] Short-circuit of `AND`/`OR` verified via tick counts.
- [ ] Each failure kind (`CRASH`, `EMPTY_PICK`, `TICK_LIMIT`, `RECURSION_LIMIT`, `INCOMPLETE`, `STOPPED`) produces the expected `Outcome`, message and location.
- [ ] `WHILE NOT AT_GOAL { }` hits the tick limit.
- [ ] Multi-variant run stops at the first failing variant with the right index.
- [ ] Listener receives `variantStarted`, `nodeEntered`, `actionPerformed` in order.
- [ ] `instructionCount` and `label` match the rules above (Appendix A.3 program = 4).
- [ ] `Scoring` truth table tested.
- [ ] `mvn -B verify` green, `ArchitectureTest` included.

## Out of scope

Threading and pacing (T06 does it through the listener). Parsing (T02).

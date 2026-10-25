# T09 — Campaign levels

**Wave:** 2 · **Depends on:** T01, T02, T03 · **Branch:** `feat/T09-campaign`

## Goal

Design the 10 campaign levels of §5.2 with reference solutions, and prove every level is solvable at par.

## Owns

- `src/main/resources/levels/**` (including the T01 sample, which you may rewrite)
- `src/test/java/robolang/levels/LevelSolutionsTest.java`, `src/test/java/robolang/levels/FailureCasesTest.java`

## Contracts used

`LevelLoader`, `Parser`, `StaticChecker`, `ProgramRunner`, `ExecutionLimits`.

## Requirements

1. Ten levels, file names `NN-slug.level`, listed in `index.txt` in order, matching the §5.2 table: titles, introduced constructs, variant counts, design intent.
2. Keywords are cumulative: each level allows everything introduced so far plus its new constructs.
3. Every level has a `.solution.rl`. `par-instructions` and `par-actions` equal the reference solution's counts.
4. Pars must be meaningful:
   - Level 3: the instruction par is reachable only with `REPEAT`.
   - Levels 6–8: multi-variant, so hard-coded move sequences fail at least one variant.
   - Level 9: `DEF`/`CALL` gives a lower instruction count than the best `REPEAT`-only solution (state both counts in the PR).
   - Level 10: three mazes solved by the SPEC Appendix A.2 wall-follower; every item lies on the follower's path before the goal.
5. Grids within 12×12. Briefings: one or two plain sentences saying the objective and the new construct.

## Acceptance criteria

- [ ] `LevelSolutionsTest` (parameterized over `loadAll()`): exactly 10 levels load; each solution parses, passes `StaticChecker` with the level's keywords, succeeds on all variants with 3 stars, and its counts equal both pars.
- [ ] `FailureCasesTest`: one small inline level + program per failure kind in §3.4 except `STOPPED` (`CRASH`, `EMPTY_PICK`, `TICK_LIMIT`, `RECURSION_LIMIT`, `INCOMPLETE`), asserting kind, variant and location.
- [ ] Levels 6–8: a test shows a hard-coded solution for variant 1 fails another variant.
- [ ] PR description: an ASCII picture of each level's first variant and its solution.
- [ ] `mvn -B verify` green.

## Out of scope

Level selector, unlocking (T11).

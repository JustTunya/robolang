# T12 — Quality gates

**Wave:** 2 · **Depends on:** T02, T03 · **Branch:** `feat/T12-quality`

## Goal

Enforce robustness, performance and coverage (NFR-02, NFR-04, NFR-07) in CI.

## Owns

- `src/test/java/robolang/quality/**`

## Allowed shared edits

`pom.xml`: a JaCoCo `check` execution with a line-coverage rule of 0.80 on `robolang.lang.*` packages. Nothing else.

## Requirements

1. **Fuzz test (NFR-04):** 10 000 random programs from a fixed seed (print the seed on failure): half random token sequences (valid tokens, random order, nested braces), half random printable strings. Pipeline: `Parser.parse` → `StaticChecker.check` (all keywords) → `ProgramRunner.run` on a fixed 5×5 level with `new ExecutionLimits(10_000, 64)`. Only `RoboLangException` subtypes may escape parse/check; `run` must return an `Outcome`. Whole test under 60 s in CI.
2. **Performance (NFR-02):** a generated 10 000-character valid program parses in under 100 ms (median of 5 runs after 3 warm-ups); a program that loops to 100 000 ticks finishes in under 1 s with a no-op listener. Tag these `@Tag("perf")` and keep them in the default build.
3. **Coverage (NFR-07):** JaCoCo check fails `mvn verify` when `lang` line coverage is below 80%. If current coverage is below 80%, **do not write filler tests**: report the uncovered classes in the PR and stop for a decision.

## Acceptance criteria

- [ ] Fuzz test passes and is deterministic for a given seed.
- [ ] Both performance tests pass on CI.
- [ ] Deliberately removing a `lang` test drops coverage and fails the build (describe the experiment in the PR; do not commit it).
- [ ] `mvn -B verify` green.

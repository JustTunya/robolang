# AGENTS.md — rules for every worker

RoboLang is a Java 21 / Swing desktop puzzle game that demonstrates two GoF patterns:
**Interpreter** (`robolang.lang`) and **Abstract Factory** (`robolang.theme`).
The full specification is `docs/SPEC.md`. Cite it by section number (e.g. "§4.3").

You are one of several agents working in parallel. Each of you works on one GitHub
issue (`T01`…`T13`) on its own branch. Read your issue completely before writing code.

## 1. Scope

- Change **only** the paths your issue lists under "Owns". Test paths mirror main paths.
- Shared files (`MainFrame`, `CodeEditorPanel`, `ThemeRegistry`, `pom.xml`) may be
  touched only as your issue's "Allowed shared edits" section says.
- Do not implement another task's stubs, even if it would be quick.
- Do not refactor, rename or reformat code you do not own.

## 2. Contracts are frozen

T00 created the shared types. Their **signatures never change in a feature PR**:
method names, parameters, return types, record components, sealed `permits` lists,
public constants.

- You may replace a body that throws `UnsupportedOperationException` **only if your
  issue assigns that body to you**.
- If you need a contract change: stop, comment on your issue with the exact change and
  why, add the `blocked` label, and wait. The orchestrator decides.

## 3. Code rules

- Java 21. Records, sealed interfaces, `switch` expressions are fine.
- **No new dependencies.** No Spring, Lombok, Guava, Apache Commons, or similar.
- **FR-30:** the runtime never dispatches on AST node types. No `switch` / `instanceof`
  chains over node classes in `lang.runtime`; each node implements its own
  `interpret` / `evaluate`.
- `lang`, `model` must not import `java.awt` or `javax.swing`. `theme` may use
  `java.awt` (Graphics2D) but not `javax.swing`. `ArchitectureTest` enforces this.
- Concrete theme products are package-private. Only `ThemeRegistry` names a concrete
  factory.
- Every public type and public method has Javadoc.
- Messages, limits and numbers come from `docs/SPEC.md` verbatim. Do not invent your own.

## 4. Tests

- Every behavior in your acceptance criteria has a JUnit 5 test.
- Tests must run headless (CI has no display). Never create a `JFrame` or show a window
  in a test. Test logic classes; render to an off-screen `BufferedImage` when needed.
- Never delete, weaken or `@Disabled` an existing test.
- Run `mvn -B verify` locally before opening or updating a PR. It must pass.

## 5. Git and PR

- Branch: `feat/<ID>-<slug>`, e.g. `feat/T02-parser`.
- Commits: `T02: short imperative summary`.
- One PR per issue, titled `T02: Lexer, parser, static checker`, body starts with
  `Closes #<issue number>`.
- PR description must contain:
  1. **Acceptance criteria** — copy the checklist from the issue, tick each item, and
     name the test that proves it.
  2. **Files outside owned paths** — list them with a reason, or write "none".
  3. **Open questions** — anything ambiguous in the spec, or "none".
- When `main` moves and your branch conflicts, rebase onto `main` and resolve conflicts
  in your own files. Do not resolve conflicts by deleting someone else's code.

## 6. When stuck

Comment on your issue, add `blocked`, and stop. Do not guess at contract changes,
spec changes or another task's internals.

# T00 — Foundation and contracts

**Wave:** 0 · **Depends on:** — · **Branch:** `feat/T00-foundation` · **Done by:** the owner, with one agent

## Goal

Create the Maven project, CI, and every shared type with its **final signature**. After this
merges, signatures are frozen (AGENTS.md §2). Bodies owned by later tasks throw
`UnsupportedOperationException("T0X")`, naming the task that implements them.

Spec: §3.1, §10.1–§10.4, §11.

## Owns

`pom.xml`, `.github/`, `AGENTS.md`, `CLAUDE.md`, `ORCHESTRATOR.md`, `docs/SPEC.md`,
every file listed under Contracts below, `src/test/java/robolang/ArchitectureTest.java`,
`package-info.java` in every package.

## Build

- Maven, `groupId` of your choice, `artifactId` `robolang`, Java 21 (`maven.compiler.release=21`).
- Test dependencies only: JUnit Jupiter 5 (current stable), ArchUnit JUnit 5 (current stable).
- JaCoCo plugin bound to `verify` (report only; T12 adds the threshold).
- Executable JAR: `maven-jar-plugin` manifest `Main-Class: robolang.app.Main`.
- `-Xlint:all` with no warnings.
- `robolang.app.Main` opens an empty `JFrame` titled "RoboLang" (T05 replaces the content).

## Contracts

Root package: `robolang`. **C** = complete implementation in T00. **S** = signature + Javadoc,
body throws `UnsupportedOperationException("<task>")`.

### `model` (C unless marked)

| Type | Shape |
|---|---|
| `Direction` | `enum { NORTH, EAST, SOUTH, WEST }` + `left()`, `right()`, `dRow()`, `dCol()` |
| `Position` | `record(int row, int col)` + `step(Direction)` |
| `CellType` | `enum { FLOOR, WALL, GOAL }` |
| `Grid` | immutable: `rows()`, `cols()`, `cellAt(Position)` (outside the grid → `WALL`), `goal()`, `initialItems(): Set<Position>` |
| `World` | mutable: `World(Grid, Position start, Direction facing)`, `robot()`, `facing()`, `wallAhead()`, `onItem()`, `atGoal()`, `itemsLeft()`, `moveForward()`, `turnLeft()`, `turnRight()`, `pick()`, `snapshot()`. `moveForward()` / `pick()` throw `IllegalStateException` when their precondition fails (the runtime checks first and raises the spec's exception). |
| `WorldSnapshot` | `record(Grid grid, Position robot, Direction facing, Set<Position> items)` (immutable copy) |
| `Variant` | `record(Grid grid, Position start, Direction facing)` + `newWorld()` |
| `Level` | `record(String id, String title, String briefing, Set<String> keywords, int parInstructions, int parActions, List<Variant> variants, String referenceSolution)` — keywords upper-case; `referenceSolution` may be null |
| `LevelFormatException` | `extends RuntimeException`, carries file id and line number |
| `LevelLoader` | **S (T01):** `static Level parse(String id, String text)`, `static List<Level> loadAll()` |

`model` imports nothing from `lang`, `theme`, `ui`, `app`, `java.awt`, `javax.swing`.

### `lang.ast` (records: C for components; method bodies S unless noted)

- `record SourceRange(int startLine, int startCol, int endLine, int endCol)` — C
- `sealed interface AstNode permits Statement, BoolExpression, ProcDef, Program` —
  `SourceRange range()`, `String label()` **S (T03)**, `int instructionCount()` **S (T03)**
- `sealed interface Statement extends AstNode` — `void interpret(Context ctx)` **S (T03)**;
  permits `MoveStmt, TurnStmt, PickStmt, BlockStmt, RepeatStmt, WhileStmt, IfStmt, CallStmt`
- `sealed interface BoolExpression extends AstNode` — `boolean evaluate(Context ctx)` **S (T03)**;
  permits `WallAheadCond, OnItemCond, AtGoalCond, NotCond, AndCond, OrCond`
- `enum TurnDirection { LEFT, RIGHT }` — C
- Records (components final):
  - `MoveStmt(SourceRange range)`, `PickStmt(SourceRange range)`
  - `TurnStmt(TurnDirection direction, SourceRange range)`
  - `BlockStmt(List<Statement> statements, SourceRange range)`
  - `RepeatStmt(int count, BlockStmt body, SourceRange range)`
  - `WhileStmt(BoolExpression condition, BlockStmt body, SourceRange range)`
  - `IfStmt(BoolExpression condition, BlockStmt thenBlock, Statement elseBranch, SourceRange range)` — `elseBranch` is a `BlockStmt`, an `IfStmt`, or null
  - `WallAheadCond(SourceRange range)`, `OnItemCond(SourceRange range)`, `AtGoalCond(SourceRange range)`
  - `NotCond(BoolExpression operand, SourceRange range)`
  - `AndCond(BoolExpression left, BoolExpression right, SourceRange range)`, `OrCond(…)` same shape
  - `ProcDef(String name, BlockStmt body, SourceRange range)`
  - `Program(List<ProcDef> procedures, BlockStmt main, SourceRange range)` + `void execute(Context ctx)` **S (T03)**
- `final class CallStmt implements Statement` (not a record; it is linked after parsing, D6):
  `CallStmt(String name, SourceRange range)`, `name()`, `range()`, `void bind(ProcDef target)` (C: once only, else `IllegalStateException`), `ProcDef target()` (C)

### `lang.runtime`

- `record ExecutionLimits(int maxTicks, int maxCallDepth)` + `static final ExecutionLimits DEFAULT = new ExecutionLimits(100_000, 64)` — C
- `interface ExecutionListener` — C, all methods `default` no-ops; implementations **may block** (that is how the UI paces execution):
  - `void variantStarted(int variantIndex, WorldSnapshot initial)`
  - `void nodeEntered(AstNode node)`
  - `void actionPerformed(AstNode node, WorldSnapshot after)`
- `final class Context` — **S (T03)**:
  `Context(World world, ExecutionLimits limits, ExecutionListener listener, int variantIndex)`,
  `World world()`, `void tick(AstNode node)`, `void move(AstNode node)`,
  `void turn(AstNode node, TurnDirection d)`, `void pick(AstNode node)`,
  `void enterCall(CallStmt call)`, `void exitCall()`, `long ticks()`, `int actions()`,
  `void requestStop()` (thread-safe)
- `enum FailureKind { CRASH, EMPTY_PICK, TICK_LIMIT, RECURSION_LIMIT, INCOMPLETE, STOPPED }` — C
- `record Outcome(boolean success, FailureKind failure, String message, SourceRange location, int failedVariant, int instructionCount, int totalActions, long totalTicks, int stars)` — C; `failure`, `message`, `location` null on success; `failedVariant` −1 on success
- `final class ProgramRunner` — **S (T03):** `static Outcome run(Program program, Level level, ExecutionLimits limits, ExecutionListener listener)`
- `final class Scoring` — **S (T03):** `static int stars(boolean solved, int instructionCount, int totalActions, Level level)`

### `lang.lexer` / `lang.parser`

- `enum TokenType` — C: one constant per keyword in §4.1, plus `IDENT, INT, LBRACE, RBRACE, LPAREN, RPAREN, EOF`; `boolean isKeyword()`
- `record Token(TokenType type, String text, int line, int col)` — C
- `final class Lexer` — **S (T02):** `Lexer(String source)`, `List<Token> tokenize()`
- `final class Parser` — **S (T02):** `static Program parse(String source)`
- `final class StaticChecker` — **S (T02):** `static void check(Program program, Set<String> allowedKeywords)`

### `lang.error` (all C, all unchecked)

`abstract RoboLangException(String message, SourceRange range)` with `range()` and
`formatted()` → `"<line>:<col> <message>"` (just the message when `range` is null).
Subclasses: `LexException`, `ParseException`, `SemanticException`,
`abstract ExecutionFailure` (+ `int variantIndex()`) → `CrashException`, `EmptyPickException`,
`TickLimitException`, `RecursionLimitException`; and `ExecutionStoppedException`.

### `theme`

- `interface Paintable { void paint(Graphics2D g, Rectangle cell); }` — C
- `interface WallTile extends Paintable`, `interface FloorTile extends Paintable`, `interface GoalTile extends Paintable` — C
- `interface Collectible extends Paintable { String pickupMessage(); }` — C
- `interface RobotView extends Paintable { void paint(Graphics2D g, Rectangle cell, Direction facing, double progress); String crashMessage(); }` — C (the plain `paint` defaults to facing EAST, progress 1.0)
- `interface ThemeFactory { String name(); RobotView createRobot(); WallTile createWall(); FloorTile createFloor(); Collectible createCollectible(); GoalTile createGoal(); }` — C
- `final class ThemeRegistry` — **S (T04):** `static List<ThemeFactory> all()`, `static ThemeFactory byName(String name)`, `static ThemeFactory defaultTheme()`

### `app`

- `record BestResult(int stars, int instructions, int actions)` — C
- `final class ProgressStore` — **S (T08):** `ProgressStore(Path dir)`, `Optional<BestResult> best(String levelId)`, `boolean isSolved(String levelId)`, `void record(String levelId, Outcome outcome)`, `Optional<String> lastProgram(String levelId)`, `void saveProgram(String levelId, String source)`, `void resetAll()`
- `final class Settings` — **S (T08):** `Settings(Path dir)`, `String themeName()`, `void setThemeName(String)`, `boolean demoMode()`, `void setDemoMode(boolean)`

## `ArchitectureTest` (ArchUnit)

1. `robolang.lang..` does not depend on `robolang.theme..`, `robolang.ui..`, `robolang.app..`.
2. `robolang.theme..` does not depend on `robolang.lang..`, `robolang.ui..`, `robolang.app..`.
3. `robolang.model..` does not depend on `robolang.lang..`, `robolang.theme..`, `robolang.ui..`, `robolang.app..`.
4. `robolang.lang..` and `robolang.model..` do not depend on `java.awt..` or `javax.swing..`.
5. `robolang.theme..` does not depend on `javax.swing..`.
6. Classes in `robolang.theme.*` sub-packages (`space`, `jungle`, `retro`) are accessed only from their own package or from `robolang.theme.ThemeRegistry`.
7. No cycles between top-level packages.

Rule 6 must pass with empty sub-packages (`allowEmptyShould(true)`). Import production classes only
(`ImportOption.DoNotIncludeTests`), so tests may cross package boundaries (e.g. a `model` test comparing with `TokenType`).

## Acceptance criteria

- [ ] `mvn -B verify` passes locally and in CI.
- [ ] Every contract above exists with exactly the listed signature and Javadoc naming the implementing task.
- [ ] `model` types marked C are fully implemented, with unit tests: turning, stepping, wall/out-of-bounds checks, item pickup, snapshot immutability.
- [ ] `lang.error` hierarchy complete; `formatted()` tested.
- [ ] `CallStmt.bind` tested (second bind throws).
- [ ] `ArchitectureTest` contains rules 1–7 and passes.
- [ ] `java -jar target/robolang-*.jar` opens a window titled "RoboLang".
- [ ] `docs/SPEC.md` committed.

## Out of scope

Any logic marked S.

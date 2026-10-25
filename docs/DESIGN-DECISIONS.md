# Design decisions

Decisions D1–D7 from [SPEC §10.6](SPEC.md#106-design-decisions-and-deviations), each with the GoF baseline,
the decision, its justification and a code excerpt from `main`. D1–D5 depart from or add to the GoF
Interpreter structure; D6 is an addition on top of it; D7 is an implementation choice inside Abstract Factory.
Diagrams: [`uml/02-interpreter-robolang.puml`](uml/02-interpreter-robolang.puml) and
[`uml/04-abstract-factory-robolang.puml`](uml/04-abstract-factory-robolang.puml).

## D1. Two abstract expression types: `Statement` and `BoolExpression`

- **GoF baseline:** one `AbstractExpression` with a single `Interpret(Context)` operation.
- **Decision:** two sealed interfaces under a common `AstNode`. Statements `interpret` (return `void`),
  conditions `evaluate` (return `boolean`).
- **Justification:** type safety. A condition cannot appear where a statement is expected, and the compiler
  knows each node's return type. The parser enforces the split. Sealing gives the checker a closed set of
  node types.

`src/main/java/robolang/lang/ast/Statement.java`

```java
public sealed interface Statement extends AstNode
        permits MoveStmt, TurnStmt, PickStmt, BlockStmt, RepeatStmt, WhileStmt, IfStmt, CallStmt {
    void interpret(Context ctx);
}
```

`src/main/java/robolang/lang/ast/BoolExpression.java`

```java
public sealed interface BoolExpression extends AstNode
        permits WallAheadCond, OnItemCond, AtGoalCond, NotCond, AndCond, OrCond {
    boolean evaluate(Context ctx);
}
```

## D2. Lexer, parser and static checker are part of the system

- **GoF baseline:** building the AST is explicitly outside the pattern's scope.
- **Decision:** `Lexer`, `Parser` and `StaticChecker` are implemented, in their own packages
  (`lang.lexer`, `lang.parser`).
- **Justification:** players type programs as text, so something must build the tree. Keeping this code out
  of `lang.ast` and `lang.runtime` leaves the pattern classes clean. The Client is whoever builds the tree
  and calls `interpret`.

`src/main/java/robolang/lang/parser/Parser.java`

```java
public static Program parse(String source) {
    return new Parser(new Lexer(source).tokenize()).program();
}
```

`src/main/java/robolang/ui/ExecutionController.java` (the client)

```java
program = Parser.parse(view.source());
StaticChecker.check(program, level.keywords());
```

## D3. `Context` notifies an `ExecutionListener`

- **GoF baseline:** the Context is passive global information that the expressions read and write.
- **Decision:** `Context` reports every node entry and every action to an `ExecutionListener`.
  An Observer is added to the pattern without changing its roles.
- **Justification:** the UI needs node and world updates to animate the robot and highlight the executing
  AST node. The listener may block, which is also how the UI paces a run (speed, Step, Pause).

`src/main/java/robolang/lang/runtime/ExecutionListener.java`

```java
public interface ExecutionListener {
    default void variantStarted(int variantIndex, WorldSnapshot initial) { }
    default void nodeEntered(AstNode node) { }
    default void actionPerformed(AstNode node, WorldSnapshot after) { }
}
```

`src/main/java/robolang/lang/runtime/Context.java`

```java
public void tick(AstNode node) {
    ticks++;
    listener.nodeEntered(node);
    ...
}

private void acted(AstNode node) {
    actions++;
    listener.actionPerformed(node, world.snapshot());
}
```

## D4. Execution limits are enforced in `Context`

- **GoF baseline:** not covered. The pattern assumes the interpreted program terminates.
- **Decision:** `Context.tick` counts ticks and stops the run when the tick limit is exceeded or a stop was
  requested; `Context.enterCall` bounds the call depth. Defaults: 100 000 ticks, depth 64.
- **Justification:** player code can loop forever (`WHILE NOT AT_GOAL { }`) or recurse without bound. Because
  every node calls `ctx.tick(this)`, one check covers the whole tree. The same hook delivers Stop.

`src/main/java/robolang/lang/runtime/Context.java`

```java
if (stopRequested) {
    throw new ExecutionStoppedException(node.range());
}
if (ticks > limits.maxTicks()) {
    ...
    throw new TickLimitException("Tick limit of " + max + " reached, possible infinite loop",
            node.range(), variantIndex);
}
```

```java
public void enterCall(CallStmt call) {
    if (depth + 1 > limits.maxCallDepth()) {
        throw new RecursionLimitException("Call depth limit of " + limits.maxCallDepth() + " exceeded",
                call.range(), variantIndex);
    }
    depth++;
}
```

`src/main/java/robolang/lang/runtime/ExecutionLimits.java`

```java
public static final ExecutionLimits DEFAULT = new ExecutionLimits(100_000, 64);
```

## D5. Extra operations are methods on the nodes (no Visitor)

- **GoF baseline:** the GoF text notes that adding operations to the expression hierarchy is costly and
  points to Visitor.
- **Decision:** `label()` and `instructionCount()` are declared on `AstNode` and implemented by each node.
- **Justification:** with two extra operations a Visitor adds more classes than it saves. The trade-off is
  discussed in the presentation (SPEC §10.7). Note that `StaticChecker` is the one place that switches over
  node types, using a pattern-matching `switch` over the sealed hierarchy; the runtime never does (FR-30).

`src/main/java/robolang/lang/ast/AstNode.java`

```java
public sealed interface AstNode permits Statement, BoolExpression, ProcDef, Program {
    SourceRange range();
    String label();
    int instructionCount();
}
```

`src/main/java/robolang/lang/ast/RepeatStmt.java`

```java
@Override
public String label() {
    return "REPEAT " + count;
}

@Override
public int instructionCount() {
    return 1 + body.instructionCount();
}
```

## D6. `CallStmt` is linked to its `ProcDef` by the static checker

- **GoF baseline:** not covered.
- **Decision:** the checker collects all `DEF`s first, then binds each `CallStmt` to its target once.
  `CallStmt` is therefore a small mutable class, not a record.
- **Justification:** undefined procedures fail before execution, forward calls work, and the runtime needs no
  name lookup: `CallStmt.interpret` goes straight to `target.body()`.

`src/main/java/robolang/lang/parser/StaticChecker.java`

```java
case CallStmt s -> {
    use("CALL", s.range());
    ProcDef target = procs.get(s.name());
    if (target == null) {
        throw new SemanticException("Unknown procedure '" + s.name() + "'", s.range());
    }
    if (s.target() == null) {
        s.bind(target);
    }
}
```

`src/main/java/robolang/lang/ast/CallStmt.java`

```java
@Override
public void interpret(Context ctx) {
    ctx.tick(this);
    ctx.enterCall(this);
    try {
        target.body().interpret(ctx);
    } finally {
        ctx.exitCall();
    }
}
```

## D7. Concrete factories are stateless singletons in a `ThemeRegistry`

- **GoF baseline:** the GoF text suggests an application normally needs only one instance of a concrete
  factory.
- **Decision:** each concrete factory has a `public static final INSTANCE`, a private constructor and
  returns shared, stateless product instances. `ThemeRegistry` holds the list; it is the only class that
  names concrete factories.
- **Justification:** this follows the GoF recommendation. The registry lets the theme combo box list
  and look up themes without naming concrete classes, and a new theme is one new factory plus
  one registry entry (see "Adding a theme" in the README). Sharing product instances across cells is a
  Flyweight-style optimisation that is safe because products hold no state.

`src/main/java/robolang/theme/ThemeRegistry.java`

```java
private static final List<ThemeFactory> ALL = List.of(SpaceThemeFactory.INSTANCE, JungleThemeFactory.INSTANCE,
        RetroThemeFactory.INSTANCE);
```

`src/main/java/robolang/theme/retro/RetroThemeFactory.java`

```java
public static final RetroThemeFactory INSTANCE = new RetroThemeFactory();
private static final RobotView ROBOT = new PixelBot();
...
private RetroThemeFactory() {
}

@Override
public RobotView createRobot() {
    return ROBOT;
}
```

`src/main/java/robolang/ui/GridPanel.java` (the client holds only `ThemeFactory`)

```java
robot = theme.createRobot();
wall = theme.createWall();
floor = theme.createFloor();
item = theme.createCollectible();
goal = theme.createGoal();
```

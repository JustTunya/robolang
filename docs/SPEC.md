# RoboLang — Software Requirements Specification

Oct 25, 2026 · @Neumorphis

## 1. Introduction

RoboLang is a single-player desktop puzzle game. The player writes programs in a small custom language to guide a robot through grid levels. It demonstrates the Abstract Factory and Interpreter design patterns for the Design Patterns course at Babeș-Bolyai University.

### 1.1 Purpose

This document defines the game rules, the RoboLang language, the requirements and the architecture. It is the reference for implementation, testing, the UML diagrams and the in-class presentation.

### 1.2 Scope

In scope:

- a campaign of 10 levels
- a code editor
- an interpreter with step-by-step execution
- live AST visualization
- visual themes that can be switched at runtime
- locally saved progress

Section 13.4 lists what is out of scope.

### 1.3 Course requirements

| ID | Course requirement | Addressed in |
| --- | --- | --- |
| CR-1 | Desktop application that demonstrates the patterns; one program may cover both | §2, §10 |
| CR-2 | Original class hierarchy based on an own idea, not the textbook structure | §10.3, §10.4 |
| CR-3 | Java and Swing recommended | §10.1 |
| CR-4 | Any deviation from the base pattern needs a detailed justification | §10.6 |
| CR-5 | UML for both the theoretical pattern and the specific example | §13.1 |
| CR-6 | About 20 minutes of code presentation in the lecture | §13.2 |
| CR-7 | Grading on code complexity and discussion of pattern specifics | §10.7 |

### 1.4 Definitions

| Term | Meaning |
| --- | --- |
| Level | A grid puzzle with a start, a goal, items and par values |
| Variant | One of several grids in a level; a solution must pass all of them |
| Action | One execution of `MOVE`, `TURN` or `PICK` |
| Tick | One evaluation of any AST node; counted for the safety limit |
| Instruction count | Number of statement keywords in a program (§3.5) |
| Par | The target value a player must meet to earn a star |
| AST | Abstract syntax tree built by the parser |
| Theme | A family of visual objects produced by one concrete factory |

### 1.5 References

- Gamma, Helm, Johnson, Vlissides: *Design Patterns: Elements of Reusable Object-Oriented Software*, Addison-Wesley, 1994. Chapters on Abstract Factory and Interpreter.
- ISO/IEC/IEEE 29148:2018, *Requirements engineering*. Used as the structural reference for this document.
- Oracle, *Java SE 21 API documentation*, packages `javax.swing` and `java.util.concurrent`.

### 1.6 Conventions

- Requirement priority uses MoSCoW: **Must**, **Should**, **Could**.
- Language keywords and class names are written in `monospace`.
- “Shall” marks a mandatory requirement.

## 2. Overall description

RoboLang is a standalone, offline desktop application distributed as one executable JAR. It has no server, network access or accounts.

### 2.1 Product functions

- Select a level from the campaign and read its briefing
- Write a program in the editor
- Parse the program and inspect its AST
- Run, step, pause, stop and reset execution with animation
- Switch the visual theme at any time, including during execution
- See the outcome, stars and error locations
- Save progress and the last program for each level locally

### 2.2 Users

| User | Needs |
| --- | --- |
| Player | Solves levels. Needs no programming experience: levels introduce one construct at a time. |
| Presenter / evaluator | Demonstrates the patterns. Uses demo mode, which unlocks all levels and loads reference solutions. |

### 2.3 Constraints

- Java 21 and Swing. No runtime dependency beyond the JDK, except the optional look-and-feel library (§10.1).
- Runs on Windows, macOS and Linux.
- All code is written by the author (course rule CR-2).
- Both patterns must be visible at runtime, not only present in the code.

### 2.4 Assumptions

- Screen resolution is at least 1280×800.
- Levels ship as resources inside the JAR. There is no in-game level editor.
- The UI language is English. The presentation is given in Hungarian.

## 3. Gameplay and rules

The player solves a level by writing one program that collects every item and ends with the robot on the goal, in every variant of the level. Shorter and more efficient programs earn more stars.

### 3.1 World model

| Element | Rule |
| --- | --- |
| Grid | Rectangular, 3×3 to 12×12 cells, walls included. Coordinates are (row, column) with origin (0, 0) at the top left. |
| Cell types | Floor, Wall, Goal. Positions outside the grid count as walls. |
| Items | Placed on floor cells, at most one per cell. |
| Robot | Exactly one. State is a position and a facing (`NORTH`, `EAST`, `SOUTH`, `WEST`). Starts on a floor cell. |
| Goal | Exactly one per variant. The robot may pass over it. |

### 3.2 Core loop

1. Select a level and read its briefing: objective, available keywords, par values.
2. Write a program.
3. Press Run or Step. The robot executes the program with animation.
4. On success, the game shows stars and unlocks the next level. On failure, it highlights the failing AST node and source location and shows the reason.
5. Edit and retry. Reset returns the world to its start state.

### 3.3 Success condition

A run succeeds when the program terminates normally and, in every variant, both of these hold:

- the robot stands on the goal cell
- no items remain on the grid

Reaching the goal mid-program does not end the run. The program continues until it terminates. This makes `AT_GOAL` useful as a loop condition and keeps program termination as the only point of evaluation.

### 3.4 Failure conditions

| Failure | Trigger | Timing |
| --- | --- | --- |
| Crash | `MOVE` into a wall or off the grid | Immediate |
| Empty pick | `PICK` on a cell without an item | Immediate |
| Tick limit | More than 100 000 ticks in one variant (infinite-loop guard) | Immediate |
| Recursion limit | Procedure call depth exceeds 64 | Immediate |
| Incomplete | Program terminates, but the success condition is not met | At termination |
| Stopped | Player presses Stop | Immediate |

On an immediate failure, the robot stays at its last valid position. The failing node is highlighted in red in the AST view and the editor.

### 3.5 Scoring

Each level awards up to three stars.

| Star | Condition |
| --- | --- |
| 1 | Level solved |
| 2 | Instruction count ≤ the level’s `par-instructions` |
| 3 | Total action count across all variants ≤ the level’s `par-actions` |

- **Instruction count** is static and computed from the AST. Each occurrence of `MOVE`, `TURN`, `PICK`, `REPEAT`, `WHILE`, `IF`, `DEF` and `CALL` counts 1. `ELSE`, conditions, numbers, braces and comments count 0.
- **Action count** is dynamic: the number of `MOVE`, `TURN` and `PICK` executions at runtime.
- The two stars pull against each other. Loops lower the instruction count, but a careless loop can raise the action count.
- The best result per level is stored: stars, instruction count and action count.

### 3.6 Progression

- Levels unlock in order. Level n+1 unlocks when level n is solved.
- Each level lists the keywords it allows. Using any other keyword is a semantic error, so constructs are introduced one at a time.
- Demo mode, a settings toggle, unlocks all levels and enables a “Load reference solution” action.

### 3.7 Variants

From level 6 on, a level may contain 2–3 grid variants, for example the same corridor at different lengths. The program runs on each variant in order, from a fresh start state, and must succeed on all of them. Execution stops at the first failing variant.

Variants make hard-coded move sequences fail and force general solutions with `WHILE` and `IF`. The grid view shows which variant is executing.

## 4. The RoboLang language

RoboLang is a small imperative language with no variables or arithmetic. Control flow comes from counted loops, conditional loops, conditionals on sensor predicates, and parameterless procedures.

### 4.1 Lexical rules

- **Keywords** are case-insensitive: `MOVE TURN LEFT RIGHT PICK REPEAT WHILE IF ELSE DEF CALL NOT AND OR WALL_AHEAD ON_ITEM AT_GOAL`.
- **Identifiers** match `[A-Za-z_][A-Za-z0-9_]*`, are at most 32 characters, are case-sensitive, and may not equal a keyword in any letter case.
- **Integer literals** are decimal, 1–999.
- **Punctuation**: `{ } ( )`.
- **Comments** run from `#` to the end of the line.
- Whitespace and line breaks carry no meaning. Statements need no separator.
- Each token records its line and column, both 1-based, for error reporting.

### 4.2 Grammar (EBNF)

```
program    = { topLevel } ;
topLevel   = procDef | statement ;
procDef    = "DEF" IDENT block ;
block      = "{" { statement } "}" ;
statement  = "MOVE"
           | "TURN" ( "LEFT" | "RIGHT" )
           | "PICK"
           | "REPEAT" INT block
           | "WHILE" cond block
           | ifStmt
           | "CALL" IDENT ;
ifStmt     = "IF" cond block [ "ELSE" ( block | ifStmt ) ] ;
cond       = andCond { "OR" andCond } ;
andCond    = notCond { "AND" notCond } ;
notCond    = "NOT" notCond | atom ;
atom       = "WALL_AHEAD" | "ON_ITEM" | "AT_GOAL" | "(" cond ")" ;
```

The grammar is LL(1), so a hand-written recursive-descent parser handles it with one token of lookahead. Operator precedence, highest first: `NOT`, `AND`, `OR`. `ELSE IF` chains are supported through the `ifStmt` alternative.

### 4.3 Statement semantics

| Statement | Effect | Ticks | Actions |
| --- | --- | --- | --- |
| `MOVE` | Moves the robot one cell in its facing direction. Crash if the target is a wall or off the grid. | 1 | 1 |
| `TURN LEFT` / `TURN RIGHT` | Rotates the robot 90° counter-clockwise / clockwise. | 1 | 1 |
| `PICK` | Removes the item from the current cell. Empty-pick failure if there is none. | 1 | 1 |
| `REPEAT n { … }` | Executes the block n times. | 1 per iteration + body | body |
| `WHILE c { … }` | Evaluates c. If true, executes the block and repeats. | 1 per check + c + body | body |
| `IF c { … } ELSE { … }` | Evaluates c once and executes one branch. | 1 + c + branch | branch |
| `DEF name { … }` | Declares a procedure. No effect when reached during execution. | 0 | 0 |
| `CALL name` | Executes the procedure body. Call depth +1 for its duration. | 1 + body | body |

### 4.4 Condition semantics

| Condition | True when |
| --- | --- |
| `WALL_AHEAD` | The cell in front of the robot is a wall or off the grid |
| `ON_ITEM` | The robot’s cell holds an item |
| `AT_GOAL` | The robot stands on the goal cell |
| `NOT c` | c is false |
| `a AND b` | Both are true. `b` is not evaluated if `a` is false. |
| `a OR b` | At least one is true. `b` is not evaluated if `a` is true. |

Each condition node evaluation costs 1 tick. Conditions have no side effects.

### 4.5 Static rules

The static checker runs after parsing and before execution. A program that breaks any rule does not run.

- Procedure names are unique.
- Every `CALL` refers to a defined procedure. The checker links each `CALL` node to its definition.
- A procedure may be called before its definition, because definitions are collected in a first pass.
- Recursion, direct and mutual, is allowed. Its depth is bounded at runtime.
- `DEF` appears only at top level. The grammar enforces this.
- Only keywords listed for the current level may appear.
- An empty program or empty block is valid. An empty program fails as Incomplete.

### 4.6 Limits

| Limit | Value |
| --- | --- |
| Source length | 10 000 characters |
| `REPEAT` count | 1–999 |
| Ticks per variant | 100 000 |
| Call depth | 64 |
| Procedures per program | 20 |
| Identifier length | 32 characters |

Ticks and actions are separate counters on purpose. `WHILE NOT AT_GOAL { }` performs no actions but loops forever. Only a tick limit catches it.

## 5. Levels

Levels are plain-text resources, so adding or rebalancing a level needs no code change. Every level ships with a reference solution that an automated test runs.

### 5.1 Level file format

Location: `src/main/resources/levels/NN-slug.level` (UTF-8). The reference solution sits next to it as `NN-slug.solution.rl`. The file has header lines (`key: value`) followed by one or more `grid:` blocks, one per variant.

```
title: Unknown Corridor
briefing: The corridor length changes. Walk to its end without counting steps.
keywords: MOVE WHILE NOT WALL_AHEAD
par-instructions: 2
par-actions: 14
grid:
#########
#>.....G#
#########
grid:
###########
#>.......G#
###########
```

The reference solution `WHILE NOT WALL_AHEAD { MOVE }` uses 2 instructions and 6 + 8 = 14 actions.

| Symbol | Meaning |
| --- | --- |
| `#` | Wall |
| `.` | Floor |
| `*` | Floor with an item |
| `G` | Goal |
| `^` `>` `v` `<` | Robot start on floor, facing north / east / south / west |

The loader rejects a level that breaks any of these rules:

- all rows of a grid have equal length
- grid size is within 3×3 and 12×12
- exactly one robot and one goal per grid
- every header key is present, and every keyword is valid
- par values are positive integers

A rejected level is logged and skipped. The game does not crash.

### 5.2 Campaign

| # | Title | Introduces | Variants | Design intent |
| --- | --- | --- | --- | --- |
| 1 | First Steps | `MOVE` | 1 | Straight line to the goal |
| 2 | Corners | `TURN` | 1 | One L-shaped turn |
| 3 | Long Hall | `REPEAT` | 1 | 9 cells straight. The instruction par is reachable only with `REPEAT`. |
| 4 | Harvest | `PICK` | 1 | Items on the path. Teaches the “all items” rule. |
| 5 | Staircase | Nested `REPEAT` | 1 | A repeated move-turn-move-turn pattern |
| 6 | Unknown Corridor | `WHILE`, `NOT`, `WALL_AHEAD` | 2 | Different corridor lengths |
| 7 | Scattered Harvest | `IF`, `ON_ITEM` | 2 | Items on different cells of the same path |
| 8 | Fork in the Road | `ELSE`, `AND`, `OR` | 3 | The open direction changes per variant |
| 9 | Patterns | `DEF`, `CALL` | 1 | A repeated sub-path that `REPEAT` alone can’t express compactly |
| 10 | The Maze | `AT_GOAL`, all constructs | 3 | A wall-following solution: `WHILE NOT AT_GOAL { … }` |

Par values are set when each level is built. They equal the reference solution’s counts.

## 6. Themes

A theme changes only appearance and flavor text, never the rules. The same program produces the same result in every theme. Each theme is one concrete factory that produces a consistent family of five products.

### 6.1 Product families

| Product interface | Space | Jungle | Retro (Could) |
| --- | --- | --- | --- |
| `RobotView` | Rover | Monkey | Pixel bot |
| `WallTile` | Asteroid | Tree | Brick |
| `FloorTile` | Metal plate | Grass | Dark tile |
| `Collectible` | Energy crystal | Banana | Coin |
| `GoalTile` | Landing pad | Hut | Flag |

### 6.2 Product behavior

Products carry behavior, not only images:

- Every product implements `paint(Graphics2D g, Rectangle cell)` and draws itself with Java2D shapes. The game needs no image assets. PNG sprites are a Could.
- `RobotView` also takes the facing and an animation progress from 0.0 to 1.0, to interpolate moves and turns.
- `Collectible.pickupMessage()` and `RobotView.crashMessage()` return theme-specific status text, for example “Crystal charged” versus “Banana collected”.

### 6.3 Rules

- The player selects the theme from a combo box. The choice is saved.
- Switching is allowed at any time, including during execution. The grid repaints with the new factory’s products, and execution continues unaffected.
- Level files never reference a theme, and the interpreter never references a theme.

## 7. User interface

The game uses one window, at least 1280×800, with three side-by-side panels that put both patterns on screen at once. The editor and AST tree show the Interpreter. The grid and theme selector show the Abstract Factory.

### 7.1 Layout

```
+------------------------------------------------------------------------------+
| Game  View  Help        Level: [06 Unknown Corridor v]   Theme: [Space v]    |
+-------------------------+----------------------------+-----------------------+
| CODE EDITOR             | GRID VIEW                  | AST TREE              |
|  1  # walk to the end   | Variant: [1] [2]           | Program               |
|  2  WHILE NOT WALL_AHEAD|                            | +- WHILE              |
|  3  {                   |   # # # # # # # # #        |    +- NOT             |
|  4      MOVE            |   # > . . . . . G #        |    |  +- WALL_AHEAD   |
|  5  }                   |   # # # # # # # # #        |    +- Block           |
|                         |                            |       +- MOVE  (now)  |
+-------------------------+----------------------------+-----------------------+
| [Run] [Step] [Pause] [Stop] [Reset]   Speed [===o----]   Actions 3  Ticks 9  |
+------------------------------------------------------------------------------+
| Variant 1 running: MOVE at line 4                            Best: 2/3 stars |
+------------------------------------------------------------------------------+
```

### 7.2 Panels

| Panel | Swing component | Behavior |
| --- | --- | --- |
| Code editor | `JTextPane` + line-number gutter | Monospace font. Syntax highlighting from the lexer’s tokens (Could). Underlines the error range. Highlights the source range of the executing node. Read-only during execution. |
| Grid view | Custom `JPanel` | Paints cells with the current theme’s products. Cell size scales to the panel. Animates moves and turns. Variant tabs show which variant runs. |
| AST tree | `JTree` | Built from the parsed program. Node labels such as `REPEAT 4` and `IF ON_ITEM`. Highlights the executing node: blue while running, red on failure. Selecting a node selects its source range in the editor. |
| Control bar | `JToolBar` | Run, Step, Pause, Stop, Reset. Speed slider from 1 to 20 actions per second, default 4. The last slider position is Instant: no animation, only the final state. Live action and tick counters. |
| Status bar | `JLabel` | Current state, failure reason with line:column, best stars for the level. |
| Briefing dialog | `JDialog` | Opens with each level: objective, available keywords, par values. |
| Console (Should) | `JTextArea` in a collapsible panel | Trace log of factory calls on theme switch, and of `interpret()` calls in trace mode (§8, FR-61). |

### 7.3 Execution states

Execution is a five-state machine: Editing, Running, Paused, Succeeded, Failed. The editor is editable only in Editing, Succeeded and Failed. Any text edit in Succeeded or Failed resets the world and returns to Editing.

&#91;embedded content: execution state machine · 5 states\]

Run and Step parse the program first. Reset from Running or Paused stops execution and also returns to Editing.

| Control | Editing | Running | Paused | Succeeded / Failed |
| --- | --- | --- | --- | --- |
| Run | enabled | disabled | enabled (resume) | disabled |
| Step | enabled | disabled | enabled | disabled |
| Pause | disabled | enabled | disabled | disabled |
| Stop | disabled | enabled | enabled | disabled |
| Reset | disabled | enabled | enabled | enabled |

### 7.4 Keyboard shortcuts

| Action | Shortcut |
| --- | --- |
| Run / Resume | F5 |
| Step | F10 |
| Pause | F6 |
| Stop | Shift+F5 |
| Reset | Ctrl+R |
| Next theme | Ctrl+T |
| Level selector | Ctrl+L |

## 8. Functional requirements

There are 33 requirements: 20 Must, 9 Should and 4 Could. The Must set alone is a complete, defensible submission. The Should and Could sets raise the complexity score.

| ID | Area | Requirement | Priority |
| --- | --- | --- | --- |
| FR-01 | Levels | The system shall load and validate all levels from resources at startup (§5.1), skipping invalid ones. | Must |
| FR-02 | Levels | The level selector shall show each level’s title, lock state and best stars. | Must |
| FR-03 | Levels | Levels shall unlock in order (§3.6). | Should |
| FR-04 | Levels | Opening a level shall show its briefing. | Must |
| FR-05 | Levels | Demo mode shall unlock all levels and allow loading the reference solution. | Should |
| FR-10 | Editor | The player shall be able to edit the program text freely in the Editing state. | Must |
| FR-11 | Editor | The last program per level shall be restored when the level is reopened. | Should |
| FR-12 | Editor | The editor shall show line numbers. | Should |
| FR-13 | Editor | The editor shall highlight syntax using the lexer’s tokens. | Could |
| FR-14 | Editor | The editor shall mark the error range and show the message with line:column. | Must |
| FR-20 | Language | The lexer shall produce tokens with line and column positions (§4.1). | Must |
| FR-21 | Language | A recursive-descent parser shall build the AST according to §4.2. | Must |
| FR-22 | Language | The static checker shall enforce §4.5 before execution. | Must |
| FR-23 | Language | The parser shall report the first error and stop. | Must |
| FR-24 | Language | The system shall compute the instruction count from the AST. | Must |
| FR-30 | Execution | Execution shall call `interpret()` and `evaluate()` on the AST node classes, with no central switch over node types. | Must |
| FR-31 | Execution | Run shall execute with animation at the selected speed. | Must |
| FR-32 | Execution | Step shall execute until the next action completes, then pause. | Must |
| FR-33 | Execution | Pause, Resume, Stop and Reset shall behave as in §7.3. | Must |
| FR-34 | Execution | The system shall enforce the limits in §4.6. | Must |
| FR-35 | Execution | The system shall run all variants in order and stop at the first failure. | Must |
| FR-36 | Execution | The AST tree shall highlight the executing node. | Must |
| FR-37 | Execution | The editor shall highlight the source range of the executing node. | Should |
| FR-38 | Execution | Action and tick counters shall update live. | Should |
| FR-40 | Themes | The system shall provide a `ThemeFactory` with at least two concrete factories: Space and Jungle. | Must |
| FR-41 | Themes | The system shall provide a third theme, Retro. | Could |
| FR-42 | Themes | The theme shall be switchable at any time, including during execution. | Must |
| FR-43 | Themes | The selected theme shall persist between sessions. | Should |
| FR-50 | Results | The system shall show the outcome, stars, instruction count and action count after each run. | Must |
| FR-51 | Results | The system shall persist the best result per level in `~/.robolang/progress.properties`. | Should |
| FR-52 | Results | A menu action shall reset all progress after confirmation. | Could |
| FR-60 | Teaching | Help → Pattern map shall show which classes play which pattern role, read from the code structure. | Could |
| FR-61 | Teaching | Trace mode shall log each factory call and each `interpret()` entry, indented by tree depth, to the console. | Should |

## 9. Non-functional requirements

The binding constraints are responsiveness during execution and robustness against arbitrary player input. Every value below is measurable.

| ID | Category | Requirement | Verification |
| --- | --- | --- | --- |
| NFR-01 | Responsiveness | The UI thread never runs interpretation. The UI reacts to input within 100 ms during execution. | Manual test at Instant speed |
| NFR-02 | Performance | A 10 000-character program parses in under 100 ms. 100 000 ticks execute in under 1 s at Instant speed. | Timed unit test |
| NFR-03 | Rendering | Animation runs at 30 fps or more on a 12×12 grid. | Manual test |
| NFR-04 | Robustness | No input text causes an unhandled exception. Every failure becomes a message. | Fuzz test, §12 |
| NFR-05 | Portability | Runs on Java 21 on Windows, macOS and Linux as one executable JAR. | Manual run on two OSes |
| NFR-06 | Extensibility | A new statement needs one AST class and one parser rule. A new theme needs one factory and its products, with no change elsewhere. | Code review, presentation demo |
| NFR-07 | Testability | 80% or more line coverage of the `lang` package. | JaCoCo report |
| NFR-08 | Usability | Every error message names line:column and what was expected, for example “Expected '{' after REPEAT 4”. | Unit tests on messages |
| NFR-09 | Startup | The main window appears within 2 s. | Manual test |
| NFR-10 | Code quality | Public types have Javadoc. The build has no compiler warnings with `-Xlint:all`. | Build log |

## 10. Architecture and tech stack

The application has four layers. `lang` and `theme` both depend on `model`, but never on each other, so the two patterns stay fully decoupled. Only `ui` knows about both.

&#91;embedded content: package dependencies · 4 layers\]

Arrows point from a package to the packages it uses. `ui` depends on `model` directly because the grid view renders `WorldSnapshot` records.

### 10.1 Tech stack

| Concern | Choice | Reason |
| --- | --- | --- |
| Language | Java 21 (LTS) | Records, sealed interfaces, pattern matching for `switch` |
| UI | Swing | Course recommendation; part of the JDK |
| Look and feel | FlatLaf 3.x (optional) | Modern flat look. Falls back to the system look and feel if removed. |
| Build | Maven 3.9 | Standard layout; builds an executable JAR |
| Tests | JUnit 5 | Standard; parameterized tests suit the level-solution checks |
| Coverage | JaCoCo | Measures NFR-07 |
| UML | PlantUML | Text-based diagrams that live in the repository and render in the IDE |
| Version control | Git + GitHub | History doubles as a progress log |
| IDE | IntelliJ IDEA | Maven, PlantUML and coverage integration |

### 10.2 Package structure

```
robolang
├── app/          Main, Settings, ProgressStore
├── model/        Direction, Position, CellType, Grid, World, WorldSnapshot,
│                 Level, Variant, LevelLoader
├── lang/
│   ├── lexer/    Token, TokenType, Lexer
│   ├── parser/   Parser, StaticChecker
│   ├── ast/      AstNode, Statement, BoolExpression, Program, ProcDef,
│   │             SourceRange + concrete node records
│   ├── runtime/  Context, ExecutionListener, ExecutionLimits, Outcome
│   └── error/    RoboLangException hierarchy (§11)
├── theme/        ThemeFactory, ThemeRegistry, product interfaces
│   ├── space/    SpaceThemeFactory + 5 products
│   ├── jungle/   JungleThemeFactory + 5 products
│   └── retro/    RetroThemeFactory + 5 products (Could)
└── ui/           MainFrame, CodeEditorPanel, GridPanel, AstTreePanel,
                  ControlBar, StatusBar, ExecutionController
```

### 10.3 Interpreter mapping

| GoF role | RoboLang class | Notes |
| --- | --- | --- |
| AbstractExpression | `Statement` (`void interpret(Context)`), `BoolExpression` (`boolean evaluate(Context)`) | Both are sealed and extend `AstNode` (`range()`, `label()`, `instructionCount()`). See D1. |
| TerminalExpression | `MoveStmt`, `TurnStmt`, `PickStmt`, `WallAheadCond`, `OnItemCond`, `AtGoalCond` | Leaves. Statements call `ctx.act(…)`; conditions query `ctx.world()`. |
| NonterminalExpression | `BlockStmt`, `RepeatStmt`, `WhileStmt`, `IfStmt`, `CallStmt`, `NotCond`, `AndCond`, `OrCond` | Hold child nodes and recurse into them. |
| Context | `Context` | World state, procedure call depth, tick and action counters, limits, listener, pacing gate. |
| Client | `Parser` builds the tree; `ExecutionController` calls `program.interpret(ctx)` per variant |  |

A `Program` record holds the procedure definitions and the main block. Each `ProcDef` holds a name and a body. A typical nonterminal:

```java
public record RepeatStmt(int count, BlockStmt body, SourceRange range)
        implements Statement {
    @Override
    public void interpret(Context ctx) {
        for (int i = 0; i < count; i++) {
            ctx.tick(this);          // counts, checks limits, notifies UI
            body.interpret(ctx);
        }
    }
    @Override
    public int instructionCount() { return 1 + body.instructionCount(); }
}
```

### 10.4 Abstract Factory mapping

| GoF role | RoboLang class | Notes |
| --- | --- | --- |
| AbstractFactory | `ThemeFactory` | `createRobot()`, `createWall()`, `createFloor()`, `createCollectible()`, `createGoal()`, `name()` |
| ConcreteFactory | `SpaceThemeFactory`, `JungleThemeFactory`, `RetroThemeFactory` | Stateless singletons registered in `ThemeRegistry` |
| AbstractProduct | `RobotView`, `WallTile`, `FloorTile`, `Collectible`, `GoalTile` | All extend `Paintable` (`paint(Graphics2D, Rectangle)`) |
| ConcreteProduct | `Rover`, `AsteroidWall`, `MetalFloor`, `EnergyCrystal`, `LandingPad`, and the Jungle and Retro equivalents | Package-private: only their factory can create them |
| Client | `GridPanel` | Holds a `ThemeFactory` reference only. Never names a concrete class. |

On a theme switch, `GridPanel` asks the new factory for one instance of each product and reuses them for every cell. This works because products are stateless, and it is a Flyweight-style sharing worth a remark in the presentation.

### 10.5 Threading model

1. All Swing work runs on the Event Dispatch Thread (EDT).
2. `ExecutionController` starts interpretation in a `SwingWorker<Outcome, StepEvent>`.
3. Every node entry calls `ctx.tick(node)`, which checks the stop flag and the limits and reports the current node.
4. Every action calls `ctx.act(…)`, which mutates the `World`, publishes a `StepEvent` with an immutable `WorldSnapshot`, then waits. In Running it sleeps 1000 / speed ms. In Paused it blocks on a `Semaphore` that Step and Resume release.
5. Stop sets a `volatile` flag. The next `tick` throws `ExecutionStoppedException`, which unwinds the recursion.
6. The UI renders only snapshots received in `process()`. Only the worker mutates the `World`, so there are no data races.

### 10.6 Design decisions and deviations

CR-4 asks for a justification of every deviation from the base pattern. The first five decisions below depart from or add to the GoF structure. The last two are implementation choices inside the pattern.

| # | Decision | GoF baseline | Justification |
| --- | --- | --- | --- |
| D1 | Two abstract expression types: `Statement` and `BoolExpression` | One `AbstractExpression` with `Interpret(Context)` | Type safety: a condition can’t appear where a statement is expected. Different return types (`void` vs `boolean`). The parser enforces the split. |
| D2 | Lexer, parser and static checker included | Building the AST is out of the pattern’s scope | Players type programs as text. These classes live in separate packages so the pattern classes stay clean. |
| D3 | `Context` notifies an `ExecutionListener` | Context is passive global information | The UI needs node and state updates for animation. This adds an Observer to the pattern without changing its roles. |
| D4 | Execution limits enforced in `Context` | Not covered | Player code can loop forever or recurse without bound. |
| D5 | Extra operations (`instructionCount()`, `label()`) are node methods | GoF notes that adding operations to the hierarchy is costly and points to Visitor | With only two extra operations, a Visitor adds more classes than it saves. The trade-off is discussed in the presentation. |
| D6 | `CallStmt` is linked to its `ProcDef` by the static checker | Not covered | Undefined procedures fail before execution, and calls need no name lookup at runtime. |
| D7 | Concrete factories are stateless singletons in a `ThemeRegistry` | GoF suggests one instance per concrete factory | Follows the GoF recommendation. The registry lets the theme combo box list factories without naming concrete classes. |

### 10.7 Pattern discussion points

**Interpreter**

- The AST is a Composite. Interpreter is Composite plus an `interpret` operation per node class.
- Adding a grammar rule means one new class. Adding an operation means changing every class. This is the reason Visitor often accompanies Interpreter (D5).
- GoF limits the pattern to simple grammars. Real languages use parser generators such as ANTLR and compile to bytecode. RoboLang sits within the pattern’s intended range.
- A tree-walking interpreter is slow compared with compiled code, but that cost doesn’t matter here.
- The pattern assumes interpretation runs to completion. Stepping and pausing needed the worker thread and gate in §10.5, which the GoF description doesn’t address.
- Modern Java alternative: a sealed hierarchy with pattern-matching `switch` can move operations outside the nodes without a Visitor. It is worth showing as a contrast.

**Abstract Factory**

- It guarantees family consistency: a Space robot never appears next to a Jungle tree.
- Its extensibility is asymmetric. A new family (theme) is added without touching existing code, which follows the Open/Closed principle. A new product type, such as `createEnemy()`, forces a change in every factory. Show this with a code diff.
- Comparison with Factory Method: one creator and one product versus one factory and a family of products.
- Concrete factories are naturally singletons (D7).

**Combined**

- The interpreter knows nothing about themes, and themes know nothing about the language. Switching the theme mid-execution is the live proof.

## 11. Error handling

Every error carries a category, a `SourceRange` and a message that says what was expected. Errors found before execution prevent the run. Errors found during execution end the current variant.

| Category | Detected by | Phase | Example message |
| --- | --- | --- | --- |
| Lexical | `Lexer` | Parse | `1:7 Unexpected character '$'` |
| Syntax | `Parser` | Parse | `3:12 Expected '{' after REPEAT 4` |
| Semantic | `StaticChecker` | After parse | `5:3 Unknown procedure 'zigzag'` · `2:1 WHILE is not available in this level` · `1:8 REPEAT count must be 1–999` |
| Runtime | AST nodes via `Context` | Execution | `Variant 2, 4:5: robot crashed into a wall at (3, 7)` |
| Limit | `Context` | Execution | `Tick limit of 100 000 reached, possible infinite loop at 2:1` |
| Internal | Any | Any | `Internal error, details written to ~/.robolang/error.log` |

- The parser reports the first error and stops. Error recovery is out of scope.
- On runtime and limit errors, the robot stays at its last valid position, and the failing node turns red in the AST tree and the editor.
- Internal errors are caught at the `ExecutionController` and `MainFrame` boundaries. The stack trace goes to the log file, and the application keeps running.

Exception hierarchy, all unchecked:

```
RoboLangException (abstract; holds SourceRange)
├── LexException
├── ParseException
├── SemanticException
├── ExecutionFailure (abstract; holds variant index)
│   ├── CrashException
│   ├── EmptyPickException
│   ├── TickLimitException
│   └── RecursionLimitException
└── ExecutionStoppedException
```

## 12. Testing strategy

Automated tests cover the language and the levels. A manual checklist covers the UI before the presentation.

| Level | Scope | Method | Pass criterion |
| --- | --- | --- | --- |
| Unit: lexer | Token types, positions, comments, case-insensitivity, invalid characters | JUnit 5 | All token streams match expected |
| Unit: parser | Every grammar rule, operator precedence, `ELSE IF` chains, each error message | JUnit 5, parameterized | Expected AST or expected message with line:column |
| Unit: static checker | Duplicate and unknown procedures, forward calls, disallowed keywords | JUnit 5 | Expected `SemanticException` or none |
| Unit: AST nodes | `interpret()` / `evaluate()` on hand-built trees and a fixed `World` | JUnit 5 | Expected world state, tick and action counts |
| Unit: themes | Each factory returns a complete, non-null family, and every product paints without exception | JUnit 5 with an off-screen `BufferedImage` | No null, no exception |
| Integration: levels | Every reference solution solves all variants and meets both pars | JUnit 5, one test per level file | Success with 3 stars |
| Integration: failures | One wrong program per failure condition in §3.4 | JUnit 5 | Expected failure type and location |
| Robustness | 10 000 random token sequences through parse, check and execute | Seeded random generator | Only `RoboLangException` subtypes escape |
| Coverage | `lang` package | JaCoCo | 80% or more line coverage (NFR-07) |
| UI | Run, Step, Pause, Stop, Reset; theme switch during a run; error marking; 1280×800 layout; shortcuts | Manual checklist | All items pass on Windows and one other OS |

The level integration test also guards the campaign. A level whose par can’t be reached fails the build.

## 13. Deliverables, milestones and scope

The submission consists of the source code, an executable JAR, six UML diagrams, a README and a 20-minute presentation.

### 13.1 Deliverables

| Deliverable | Content | Priority |
| --- | --- | --- |
| Source code | GitHub repository, Maven project, tests | Must |
| Executable | `robolang.jar`, runnable with `java -jar` | Must |
| UML 1 | Interpreter: GoF class diagram | Must |
| UML 2 | Interpreter: RoboLang class diagram (§10.3) | Must |
| UML 3 | Abstract Factory: GoF class diagram | Must |
| UML 4 | Abstract Factory: RoboLang class diagram (§10.4) | Must |
| UML 5 | Object diagram of the AST for a sample program (Appendix A.3) | Must |
| UML 6 | Sequence diagram of one `REPEAT` execution through `Context` and the UI | Should |
| README | Build and run steps, language reference, controls | Must |
| Design decisions | §10.6 expanded with code excerpts | Must |

### 13.2 Presentation plan (20 minutes)

| Minutes | Content |
| --- | --- |
| 0–2 | The game idea and a quick solve of level 6 |
| 2–5 | Abstract Factory: GoF UML versus ours, live theme switch during a run |
| 5–12 | Interpreter: grammar, GoF UML versus ours, AST object diagram, Step mode with tree highlighting |
| 12–16 | Code: `RepeatStmt.interpret()`, `Context.tick()` / `act()`, `ThemeFactory` and one concrete factory |
| 16–19 | Deviations D1–D7 and discussion points from §10.7 |
| 19–20 | Questions |

### 13.3 Milestones

Effort is estimated in focused working days. Calendar dates are set once the deadline is known.

1. **Model and console runner** (0.5 day): grid, world, robot, `MOVE`/`TURN`/`PICK` on a hand-built AST.
2. **Language** (1 day): lexer, parser, static checker, full grammar, unit tests.
3. **Themes** (0.5–1 day): `ThemeFactory`, Space and Jungle, `GridPanel` rendering.
4. **Execution UI** (1 day): editor, control bar, worker thread, Step and Pause.
5. **Teaching features** (0.5–1 day): AST tree with highlighting, error marking, the 10 levels with reference solutions.
6. **Documentation** (0.5 day): UML diagrams, README, design decisions, presentation rehearsal.

Milestones 1–3 already form a defensible project. Milestones 4–5 deliver the runtime demonstration that CR-7 rewards.

### 13.4 Out of scope

- Level editor
- Variables, arithmetic, or procedures with parameters
- Parser error recovery (reporting more than one error)
- Sound and music
- Multiplayer, online leaderboards, accounts
- Localization (the UI is English only)
- Web or mobile versions

### 13.5 Open questions

- What are the submission and presentation dates?
- Do the lab machines run Java 21? If not, records and sealed types need a Java 17 check (both are available from Java 17).
- Does the professor expect the UML diagrams on paper or slides, or is the PlantUML output enough?

## Appendix A. Sample programs

These programs illustrate the language. The final level grids decide the exact reference solutions.

### A.1 One program per construct

```
# Level 3, Long Hall: counted loop
REPEAT 9 { MOVE }

# Level 5, Staircase: nested loops
REPEAT 3 {
    REPEAT 2 { MOVE }
    TURN LEFT
    REPEAT 2 { MOVE }
    TURN RIGHT
}

# Level 7, Scattered Harvest: conditional inside a loop
WHILE NOT WALL_AHEAD {
    IF ON_ITEM { PICK }
    MOVE
}
```

### A.2 Level 10, The Maze: right-hand wall follower

```
DEF step {
    TURN RIGHT
    WHILE WALL_AHEAD { TURN LEFT }
    MOVE
}

WHILE NOT AT_GOAL {
    IF ON_ITEM { PICK }
    CALL step
}
```

This solves any maze whose walls form one connected piece, provided every item lies on the path the robot follows before reaching the goal. All three variants of level 10 are designed to meet both conditions. A robot boxed in on all four sides turns forever and hits the tick limit.

### A.3 AST for UML diagram 5

Source: `REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }`. Instruction count: 4.

```
Program
├── procedures: none
└── main: BlockStmt
    └── RepeatStmt (count = 2)
        └── body: BlockStmt
            ├── MoveStmt
            └── IfStmt
                ├── condition: WallAheadCond
                ├── then: BlockStmt
                │   └── TurnStmt (LEFT)
                └── else: none
```

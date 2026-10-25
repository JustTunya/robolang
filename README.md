# RoboLang

RoboLang is a Java 21 / Swing puzzle game. You write a small program in the RoboLang language to guide a
robot through a grid: collect every item and finish on the goal cell. Ten levels introduce one language
construct at a time, and the stars you earn reward short and efficient programs.

The project exists to demonstrate two GoF design patterns on screen at the same time:

- **Interpreter** (`robolang.lang`): the editor and the AST tree panel. Your program is parsed into an AST
  whose nodes each implement `interpret` / `evaluate`.
- **Abstract Factory** (`robolang.theme`): the grid view and the theme selector. One factory per theme
  (Space, Jungle, Retro) produces a consistent family of five products. Themes can be switched mid-run.

The full specification is [`docs/SPEC.md`](docs/SPEC.md). UML diagrams are in [`docs/uml/`](docs/uml), and
the justification of every deviation from the GoF baseline is in
[`docs/DESIGN-DECISIONS.md`](docs/DESIGN-DECISIONS.md).

## Build and run

Requirements: JDK 21 and Maven 3.9 (`JAVA_HOME` must point to the JDK 21).

```
mvn package
java -jar target/robolang-0.1.0-SNAPSHOT.jar
```

`mvn package` also runs the tests. `mvn -B verify` additionally enforces the coverage gates.

## Controls

| Control | Effect |
| --- | --- |
| Run | Parse and run the program with animation. Resumes a paused run. |
| Step | Execute one action, then pause. |
| Pause | Pause a running program. |
| Stop | Abort the run (reported as "Stopped"). |
| Reset | Return the world to its start state and go back to editing. |
| Speed slider | 1 to 20 actions per second. The last position is Instant (no animation). |
| Level selector | Choose a level. Levels unlock in order. |
| Theme selector | Switch theme at any time, also during a run. |
| Variant tabs | Show which grid variant is displayed or running (levels 6 and up). |

Keyboard shortcuts (SPEC §7.4): Run/Resume `F5`, Step `F10`, Pause `F6`, Stop `Shift+F5`, Reset `Ctrl+R`,
next theme `Ctrl+T`, level selector `Ctrl+L`.
The shortcuts are wired by T11 (PR #29), which is not merged into `main` at the time of writing.

## Language reference

Full details: [SPEC §4](docs/SPEC.md#4-the-robolang-language). Keywords are case-insensitive, comments run
from `#` to the end of the line, whitespace carries no meaning.

| Construct | Example | Notes (SPEC) |
| --- | --- | --- |
| `MOVE` | `MOVE` | One cell forward. Crashes into walls (§4.3). |
| `TURN LEFT` / `TURN RIGHT` | `TURN LEFT` | Rotate 90° (§4.3). |
| `PICK` | `PICK` | Fails if the cell holds no item (§4.3). |
| `REPEAT n { … }` | `REPEAT 9 { MOVE }` | `n` is 1–999 (§4.3, §4.6). |
| `WHILE cond { … }` | `WHILE NOT WALL_AHEAD { MOVE }` | Checks the condition before every iteration (§4.3). |
| `IF cond { … } [ELSE …]` | `IF ON_ITEM { PICK } ELSE { MOVE }` | `ELSE IF` chains are allowed (§4.2). |
| `DEF name { … }` | `DEF step { MOVE TURN LEFT }` | Top level only, no parameters (§4.5). |
| `CALL name` | `CALL step` | May be used before the `DEF`. Recursion depth is limited to 64 (§4.5, §4.6). |
| Conditions | `WALL_AHEAD`, `ON_ITEM`, `AT_GOAL` | Combine with `NOT`, `AND`, `OR` and parentheses (§4.4). |

Example (SPEC Appendix A.2, right-hand wall follower for level 10):

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

Scoring (§3.5): 1 star for solving, 2 stars if the instruction count is within the level's par, 3 stars if
the total action count is within the level's par. Programs are stopped after 100 000 ticks per variant.

## Project structure

```
src/main/java/robolang
├── app/        Main, Settings, ProgressStore, BestResult
├── model/      Grid, World, WorldSnapshot, Level, Variant, LevelLoader, Direction, Position, CellType
├── lang/
│   ├── lexer/    Lexer, Token, TokenType
│   ├── parser/   Parser, StaticChecker
│   ├── ast/      AstNode, Statement, BoolExpression, Program, ProcDef and the node records
│   ├── runtime/  Context, ProgramRunner, ExecutionListener, ExecutionLimits, Outcome, Scoring
│   └── error/    RoboLangException hierarchy
├── theme/      ThemeFactory, ThemeRegistry, product interfaces
│   ├── space/    SpaceThemeFactory
│   ├── jungle/   JungleThemeFactory
│   └── retro/    RetroThemeFactory
└── ui/         MainFrame, CodeEditorPanel, GridPanel, AstTreePanel, ControlBar, StatusBar,
                ExecutionController (+ execution/, ast/ helpers)
src/main/resources/levels   The ten campaign levels with reference solutions
docs/                       SPEC.md, DESIGN-DECISIONS.md, uml/
```

`lang` and `theme` both depend on `model` but never on each other; `ArchitectureTest` enforces the layering.

## Adding a theme

A theme is one new package with one factory. The Retro theme (T10, PR #28, commit `614194e`) is the
reference: `git show 614194e` lists exactly what a new theme touches.

1. Create `robolang/theme/<name>/<Name>ThemeFactory.java`: a `final` class implementing `ThemeFactory`
   with a `public static final ... INSTANCE`, a private constructor and `name()`.
2. Implement the five products (`RobotView`, `WallTile`, `FloorTile`, `Collectible`, `GoalTile`) as
   private nested classes that draw with Java2D. `createX()` returns the shared instance.
3. Register the factory in the `ALL` list in `ThemeRegistry` (the only place a concrete factory is named).
4. Add a test next to the existing theme tests (`RetroThemeTest`) and update `ThemeRegistryTest`.

No other class changes: `GridPanel` only ever sees `ThemeFactory`.

## Diagrams

| File | Content |
| --- | --- |
| `docs/uml/01-interpreter-gof.puml` | Interpreter, GoF structure |
| `docs/uml/02-interpreter-robolang.puml` | Interpreter, RoboLang classes |
| `docs/uml/03-abstract-factory-gof.puml` | Abstract Factory, GoF structure |
| `docs/uml/04-abstract-factory-robolang.puml` | Abstract Factory, RoboLang classes |
| `docs/uml/05-ast-object-diagram.puml` | AST object diagram of `REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }` |
| `docs/uml/06-repeat-sequence.puml` | Sequence of one `REPEAT 2 { MOVE }` run |

Each has a committed PNG next to it. To re-render: `java -jar plantuml.jar -tpng docs/uml`
(PlantUML is not a Maven dependency; download the jar from <https://plantuml.com/download>).

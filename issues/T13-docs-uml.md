# T13 — UML and documentation

**Wave:** 3 · **Depends on:** T00–T09 merged · **Branch:** `feat/T13-docs`

## Goal

Draft the UML diagrams, README and design-decisions document (§13.1). The owner reworks these for the presentation, so accuracy matters more than polish.

## Owns

- `docs/uml/**`, `README.md`, `docs/DESIGN-DECISIONS.md`

## Requirements

1. PlantUML sources in `docs/uml/`, one per diagram, plus a PNG export of each committed next to it (render locally with PlantUML; do not add a Maven dependency):
   1. `01-interpreter-gof.puml` — GoF roles: Client, Context, AbstractExpression, TerminalExpression, NonterminalExpression.
   2. `02-interpreter-robolang.puml` — actual classes from `main`, stereotyped with their GoF role (`<<AbstractExpression>>` etc.); show `Statement`, `BoolExpression`, all node classes, `Context`, `Program`, `ProcDef`, `Parser`, `ProgramRunner`.
   3. `03-abstract-factory-gof.puml` — AbstractFactory, ConcreteFactory1/2, AbstractProductA/B, ProductA1/A2/B1/B2, Client.
   4. `04-abstract-factory-robolang.puml` — `ThemeFactory`, every concrete factory, the five product interfaces, every concrete product, `GridPanel` as client, `ThemeRegistry`.
   5. `05-ast-object-diagram.puml` — object diagram for `REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }` (SPEC Appendix A.3).
   6. `06-repeat-sequence.puml` — sequence of one `REPEAT 2 { MOVE }` run: `ProgramRunner` → `Program` → `RepeatStmt` → `Context.tick` → `MoveStmt` → `Context.move` → `World` → `ExecutionListener`.
2. Class names, methods and relationships must match the code on `main` exactly. List anything in the spec that the code does differently under "Divergences" in the PR.
3. `README.md`: what the project is, build (`mvn package`), run (`java -jar`), controls and shortcuts, a short language reference (keywords and one example per construct, linking SPEC §4), project structure, how to add a theme (pointing to the T10 diff).
4. `docs/DESIGN-DECISIONS.md`: D1–D7 from SPEC §10.6, each with the GoF baseline, the decision, the justification and a short code excerpt with its file path.

## Acceptance criteria

- [ ] All six diagrams render (PNG committed) and match the code.
- [ ] README build and run instructions verified on a clean clone.
- [ ] Every D1–D7 entry has a real code excerpt.
- [ ] "Divergences" section in the PR (or "none").

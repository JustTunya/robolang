# T02 — Lexer, parser, static checker

**Wave:** 1 · **Depends on:** T00 · **Branch:** `feat/T02-parser`

## Goal

Turn RoboLang source text into a checked `Program` AST, with precise error messages.

## Owns

- `src/main/java/robolang/lang/lexer/**` (method bodies; `Token`/`TokenType` are frozen)
- `src/main/java/robolang/lang/parser/**`
- matching tests under `src/test/java/robolang/lang/lexer/` and `.../lang/parser/`

## Contracts used

AST records and `CallStmt` (`lang.ast`), `LexException`, `ParseException`, `SemanticException`, `SourceRange`.
Build AST nodes with their canonical constructors. Do **not** implement `interpret`, `evaluate`, `label` or `instructionCount` (T03).

## Requirements

**Lexer (§4.1)**
- Keywords case-insensitive; identifiers `[A-Za-z_][A-Za-z0-9_]*`, case-sensitive, max 32 characters.
- `#` comments to end of line; whitespace insignificant.
- Line and column 1-based on every token; `EOF` token at the end.
- Errors (`LexException`): unexpected character; identifier over 32 characters; source over 10 000 characters.

**Parser (§4.2)**
- Hand-written recursive descent, one token of lookahead. Grammar exactly as §4.2.
- Precedence `NOT` > `AND` > `OR`, left-associative `AND`/`OR`, parentheses.
- `ELSE IF` → `IfStmt.elseBranch` holds the nested `IfStmt`.
- `DEF` only at top level (grammar). Top-level `DEF`s go to `Program.procedures`, other top-level statements to `Program.main`, in source order.
- `SourceRange` of a node spans its first to its last token.
- First error stops parsing. Messages name what was expected, e.g. `Expected '{' after REPEAT 4`, `Expected procedure name after DEF`, `Unexpected '}'`.
- `REPEAT` counts outside 1–999 raise `SemanticException` with `REPEAT count must be 1–999` at the number's position. (Decided here because the count must fit in `RepeatStmt.count`.)

**StaticChecker (§4.5)**
- Procedure names unique; at most 20 procedures.
- Every `CallStmt` refers to a defined procedure; bind it with `CallStmt.bind(ProcDef)`. Calls before the definition are allowed.
- Keyword allow-list: the node types used imply keywords — `MoveStmt`→`MOVE`, `TurnStmt`→`TURN`, `PickStmt`→`PICK`, `RepeatStmt`→`REPEAT`, `WhileStmt`→`WHILE`, `IfStmt`→`IF` (+`ELSE` when `elseBranch` ≠ null), `ProcDef`→`DEF`, `CallStmt`→`CALL`, `NotCond`→`NOT`, `AndCond`→`AND`, `OrCond`→`OR`, `WallAheadCond`→`WALL_AHEAD`, `OnItemCond`→`ON_ITEM`, `AtGoalCond`→`AT_GOAL`. The first disallowed one raises `<KEYWORD> is not available in this level` at that node.
- All messages as in §11.

**Performance:** linear time; a 10 000-character program parses in under 100 ms (T12 measures it).

## Acceptance criteria

- [ ] Lexer: every token type, comments, mixed case, positions, each lexical error.
- [ ] Parser: every grammar rule; precedence verified by AST shape (`NOT a AND b OR c`); `ELSE IF` chain; empty program; empty block; nested `REPEAT`; procedures before and after use.
- [ ] Every syntax error message tested with line:column.
- [ ] Checker: duplicate procedure, unknown procedure, 21 procedures, forward call bound correctly, each disallowed keyword, `ELSE` without allowance.
- [ ] Sample programs from SPEC Appendix A parse and check with all keywords allowed.
- [ ] `mvn -B verify` green.

## Out of scope

Execution, instruction count, labels (T03). Error recovery (§13.4).

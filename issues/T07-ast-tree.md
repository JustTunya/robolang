# T07 — AST tree panel

**Wave:** 2 · **Depends on:** T02, T05 · **Branch:** `feat/T07-ast-tree`

## Goal

Show the parsed program as a live tree and highlight the executing node (FR-36). This is the main on-screen demonstration of the Interpreter pattern.

## Owns

- `src/main/java/robolang/ui/AstTreePanel.java`, `src/main/java/robolang/ui/ast/**`
- matching tests

## Allowed shared edits

`ui/MainFrame.java`: create the panel, `setRightPanel(...)`, register it as an `ExecutionObserver` — a few lines.

## Contracts used

`lang.ast` (all node types), `Parser` (tests only), T05 API (`ExecutionObserver`, `CodeEditorPanel.selectRange`).

## Requirements

1. `AstTreeBuilder` (pure, no Swing types in its API — returns a simple `TreeItem(String label, AstNode node, List<TreeItem> children)` structure): `Program` → `Procedures` (one `DEF name` per procedure) and `Main`; statements nest their bodies; `IF` has children `condition`, `then`, `else`; `WHILE` has `condition` and `body`; conditions show their subtree (`NOT` → operand, `AND`/`OR` → two operands). Labels come from `AstNode.label()`.
   - Use an exhaustive pattern-matching `switch` over the sealed node types. This is allowed here (FR-30 applies to the runtime only) and is a presentation talking point (§10.7: sealed switch vs Visitor).
2. `AstTreePanel` (`JTree`) implements `ExecutionObserver`:
   - `programParsed` → rebuild the tree, expand all.
   - `nodeEntered` → highlight that node (blue), expand its path, scroll it into view. Identity map `AstNode → tree node`.
   - `runFinished` with a failure → highlight the node at `location` (red).
   - `reset` → clear highlights.
3. Selecting a tree node calls `editor.selectRange(node.range())`.
4. Custom cell renderer: monospace label, highlight colors work in light and dark look-and-feels.

## Acceptance criteria

- [ ] Builder output for the SPEC Appendix A.3 program matches the expected tree exactly (labels and structure).
- [ ] Builder covers every node type (`ELSE IF` chain, procedures, nested conditions).
- [ ] Panel: `nodeEntered` selects the right tree node; failure highlight; reset clears (component test, no window).
- [ ] Screenshot of the panel during a Step run in the PR (requires T06 if merged; otherwise a screenshot with a parsed program).
- [ ] `mvn -B verify` green.

## Out of scope

Editor highlighting itself (T05/T06), trace console (T11).

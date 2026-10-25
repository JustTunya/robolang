package robolang.lang.ast;

/**
 * Procedure definition {@code DEF name { ... }} (SPEC §4.3).
 */
public record ProcDef(String name, BlockStmt body, SourceRange range) implements AstNode {

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "DEF " + name;
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1 + body.instructionCount();
    }
}

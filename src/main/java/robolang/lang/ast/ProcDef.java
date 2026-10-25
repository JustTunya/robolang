package robolang.lang.ast;

/**
 * Procedure definition {@code DEF name { ... }} (SPEC §4.3).
 */
public record ProcDef(String name, BlockStmt body, SourceRange range) implements AstNode {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public String label() {
        throw new UnsupportedOperationException("T03");
    }

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public int instructionCount() {
        throw new UnsupportedOperationException("T03");
    }
}

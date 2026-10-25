package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Counted loop {@code REPEAT n { ... }} (SPEC §4.3).
 */
public record RepeatStmt(int count, BlockStmt body, SourceRange range) implements Statement {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public void interpret(Context ctx) {
        throw new UnsupportedOperationException("T03");
    }

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

package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Conditional {@code IF c { ... } [ELSE ...]} (SPEC §4.3). {@code elseBranch} is a {@link BlockStmt}, an {@link IfStmt}, or null.
 */
public record IfStmt(BoolExpression condition, BlockStmt thenBlock, Statement elseBranch, SourceRange range) implements Statement {

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

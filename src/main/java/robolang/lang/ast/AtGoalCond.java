package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Condition {@code AT_GOAL} (SPEC §4.4).
 */
public record AtGoalCond(SourceRange range) implements BoolExpression {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public boolean evaluate(Context ctx) {
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

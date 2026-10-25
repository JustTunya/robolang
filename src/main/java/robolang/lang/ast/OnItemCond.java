package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Condition {@code ON_ITEM} (SPEC §4.4).
 */
public record OnItemCond(SourceRange range) implements BoolExpression {

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

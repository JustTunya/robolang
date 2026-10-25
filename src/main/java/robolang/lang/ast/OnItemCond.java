package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Condition {@code ON_ITEM} (SPEC §4.4).
 */
public record OnItemCond(SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return ctx.world().onItem();
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "ON_ITEM";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }
}

package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Condition {@code WALL_AHEAD} (SPEC §4.4).
 */
public record WallAheadCond(SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return ctx.world().wallAhead();
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "WALL_AHEAD";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }
}

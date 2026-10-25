package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Condition {@code AT_GOAL} (SPEC §4.4).
 */
public record AtGoalCond(SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return ctx.world().atGoal();
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "AT_GOAL";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }
}

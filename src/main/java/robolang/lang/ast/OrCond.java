package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Short-circuit {@code a OR b} (SPEC §4.4).
 */
public record OrCond(BoolExpression left, BoolExpression right, SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return left.evaluate(ctx) || right.evaluate(ctx);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return AndCond.operandLabel(left) + " OR " + AndCond.operandLabel(right);
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }
}

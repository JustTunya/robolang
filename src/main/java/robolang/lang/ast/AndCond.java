package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Short-circuit {@code a AND b} (SPEC §4.4).
 */
public record AndCond(BoolExpression left, BoolExpression right, SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return left.evaluate(ctx) && right.evaluate(ctx);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return operandLabel(left) + " AND " + operandLabel(right);
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }

    /** Renders an operand of NOT, AND or OR, parenthesizing nested AND/OR. */
    static String operandLabel(BoolExpression e) {
        return e instanceof AndCond || e instanceof OrCond ? "(" + e.label() + ")" : e.label();
    }
}

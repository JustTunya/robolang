package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Negation {@code NOT c} (SPEC §4.4).
 */
public record NotCond(BoolExpression operand, SourceRange range) implements BoolExpression {

    /** {@inheritDoc} */
    @Override
    public boolean evaluate(Context ctx) {
        ctx.tick(this);
        return !operand.evaluate(ctx);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "NOT " + AndCond.operandLabel(operand);
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 0;
    }
}

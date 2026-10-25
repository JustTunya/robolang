package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Conditional {@code IF c { ... } [ELSE ...]} (SPEC §4.3). {@code elseBranch} is a {@link BlockStmt}, an {@link IfStmt}, or null.
 */
public record IfStmt(BoolExpression condition, BlockStmt thenBlock, Statement elseBranch, SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        ctx.tick(this);
        if (condition.evaluate(ctx)) {
            thenBlock.interpret(ctx);
        } else if (elseBranch != null) {
            elseBranch.interpret(ctx);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "IF " + condition.label();
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1 + thenBlock.instructionCount() + (elseBranch == null ? 0 : elseBranch.instructionCount());
    }
}

package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Conditional loop {@code WHILE c { ... }} (SPEC §4.3).
 */
public record WhileStmt(BoolExpression condition, BlockStmt body, SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        while (true) {
            ctx.tick(this);
            if (!condition.evaluate(ctx)) {
                return;
            }
            body.interpret(ctx);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "WHILE " + condition.label();
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1 + body.instructionCount();
    }
}

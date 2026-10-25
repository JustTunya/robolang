package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Counted loop {@code REPEAT n { ... }} (SPEC §4.3).
 */
public record RepeatStmt(int count, BlockStmt body, SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        for (int i = 0; i < count; i++) {
            ctx.tick(this);
            body.interpret(ctx);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "REPEAT " + count;
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1 + body.instructionCount();
    }
}

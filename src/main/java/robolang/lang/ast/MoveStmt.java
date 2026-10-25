package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Moves the robot one cell forward (SPEC §4.3).
 */
public record MoveStmt(SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        ctx.tick(this);
        ctx.move(this);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "MOVE";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1;
    }
}

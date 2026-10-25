package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Picks up the item on the robot's cell (SPEC §4.3).
 */
public record PickStmt(SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        ctx.tick(this);
        ctx.pick(this);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "PICK";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1;
    }
}

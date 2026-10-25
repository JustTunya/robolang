package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Turns the robot 90 degrees (SPEC §4.3).
 */
public record TurnStmt(TurnDirection direction, SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        ctx.tick(this);
        ctx.turn(this, direction);
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "TURN " + direction;
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        return 1;
    }
}

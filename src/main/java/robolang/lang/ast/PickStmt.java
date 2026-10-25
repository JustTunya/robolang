package robolang.lang.ast;

import robolang.lang.runtime.Context;

/**
 * Picks up the item on the robot's cell (SPEC §4.3).
 */
public record PickStmt(SourceRange range) implements Statement {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public void interpret(Context ctx) {
        throw new UnsupportedOperationException("T03");
    }

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public String label() {
        throw new UnsupportedOperationException("T03");
    }

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public int instructionCount() {
        throw new UnsupportedOperationException("T03");
    }
}

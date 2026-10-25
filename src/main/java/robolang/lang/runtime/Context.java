package robolang.lang.runtime;

import robolang.lang.ast.AstNode;
import robolang.lang.ast.CallStmt;
import robolang.lang.ast.TurnDirection;
import robolang.model.World;

/** Execution state shared by all nodes (Interpreter: Context). All bodies are implemented by T03. */
public final class Context {

    /**
     * Creates a context for one variant. Implemented by T03.
     *
     * @param world the world to act on
     * @param limits tick and call-depth limits
     * @param listener observer notified of progress
     * @param variantIndex zero-based index of the variant being run
     */
    public Context(World world, ExecutionLimits limits, ExecutionListener listener, int variantIndex) {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Returns the world being acted on. Implemented by T03.
     *
     * @return the world
     */
    public World world() {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Counts one tick, checks stop flag and limits, and reports the node. Implemented by T03.
     *
     * @param node the node being entered
     */
    public void tick(AstNode node) {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Moves the robot forward. Implemented by T03.
     *
     * @param node the acting node
     */
    public void move(AstNode node) {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Turns the robot. Implemented by T03.
     *
     * @param node the acting node
     * @param d turn direction
     */
    public void turn(AstNode node, TurnDirection d) {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Picks up the item under the robot. Implemented by T03.
     *
     * @param node the acting node
     */
    public void pick(AstNode node) {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Enters a procedure call, checking the depth limit. Implemented by T03.
     *
     * @param call the call node
     */
    public void enterCall(CallStmt call) {
        throw new UnsupportedOperationException("T03");
    }

    /** Leaves the current procedure call. Implemented by T03. */
    public void exitCall() {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Returns the ticks used so far. Implemented by T03.
     *
     * @return tick count
     */
    public long ticks() {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Returns the actions performed so far. Implemented by T03.
     *
     * @return action count
     */
    public int actions() {
        throw new UnsupportedOperationException("T03");
    }

    /** Asks the run to stop at the next tick. Thread-safe. Implemented by T03. */
    public void requestStop() {
        throw new UnsupportedOperationException("T03");
    }
}

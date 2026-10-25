package robolang.lang.runtime;

import robolang.lang.ast.AstNode;
import robolang.lang.ast.CallStmt;
import robolang.lang.ast.TurnDirection;
import java.util.Locale;
import robolang.lang.error.CrashException;
import robolang.lang.error.EmptyPickException;
import robolang.lang.error.ExecutionStoppedException;
import robolang.lang.error.RecursionLimitException;
import robolang.lang.error.TickLimitException;
import robolang.model.Position;
import robolang.model.World;

/** Execution state shared by all nodes (Interpreter: Context). */
public final class Context {
    private final World world;
    private final ExecutionLimits limits;
    private final ExecutionListener listener;
    private final int variantIndex;
    private long ticks;
    private int actions;
    private int depth;
    private volatile boolean stopRequested;

    /**
     * Creates a context for one variant.
     *
     * @param world the world to act on
     * @param limits tick and call-depth limits
     * @param listener observer notified of progress
     * @param variantIndex zero-based index of the variant being run
     */
    public Context(World world, ExecutionLimits limits, ExecutionListener listener, int variantIndex) {
        this.world = world;
        this.limits = limits;
        this.listener = listener;
        this.variantIndex = variantIndex;
    }

    /**
     * Returns the world being acted on.
     *
     * @return the world
     */
    public World world() {
        return world;
    }

    /**
     * Counts one tick, checks stop flag and limits, and reports the node.
     *
     * @param node the node being entered
     */
    public void tick(AstNode node) {
        ticks++;
        listener.nodeEntered(node);
        if (stopRequested) {
            throw new ExecutionStoppedException(node.range());
        }
        if (ticks > limits.maxTicks()) {
            String max = String.format(Locale.ROOT, "%,d", limits.maxTicks()).replace(',', ' ');
            throw new TickLimitException("Tick limit of " + max + " reached, possible infinite loop",
                    node.range(), variantIndex);
        }
    }

    /**
     * Moves the robot forward.
     *
     * @param node the acting node
     */
    public void move(AstNode node) {
        if (world.wallAhead()) {
            Position target = world.robot().step(world.facing());
            throw new CrashException("robot crashed into a wall at " + cell(target), node.range(), variantIndex);
        }
        world.moveForward();
        acted(node);
    }

    /**
     * Turns the robot.
     *
     * @param node the acting node
     * @param d turn direction
     */
    public void turn(AstNode node, TurnDirection d) {
        if (d == TurnDirection.LEFT) {
            world.turnLeft();
        } else {
            world.turnRight();
        }
        acted(node);
    }

    /**
     * Picks up the item under the robot.
     *
     * @param node the acting node
     */
    public void pick(AstNode node) {
        if (!world.onItem()) {
            throw new EmptyPickException("nothing to pick up at " + cell(world.robot()), node.range(), variantIndex);
        }
        world.pick();
        acted(node);
    }

    /**
     * Enters a procedure call, checking the depth limit.
     *
     * @param call the call node
     */
    public void enterCall(CallStmt call) {
        if (depth + 1 > limits.maxCallDepth()) {
            throw new RecursionLimitException("Call depth limit of " + limits.maxCallDepth() + " exceeded",
                    call.range(), variantIndex);
        }
        depth++;
    }

    /** Leaves the current procedure call. */
    public void exitCall() {
        depth--;
    }

    /**
     * Returns the ticks used so far.
     *
     * @return tick count
     */
    public long ticks() {
        return ticks;
    }

    /**
     * Returns the actions performed so far.
     *
     * @return action count
     */
    public int actions() {
        return actions;
    }

    /** Asks the run to stop at the next tick. Thread-safe. */
    public void requestStop() {
        stopRequested = true;
    }

    private void acted(AstNode node) {
        actions++;
        listener.actionPerformed(node, world.snapshot());
    }

    private static String cell(Position p) {
        return "(" + p.row() + ", " + p.col() + ")";
    }
}

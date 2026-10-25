package robolang.ui.execution;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import robolang.lang.ast.AstNode;
import robolang.lang.error.ExecutionStoppedException;
import robolang.lang.runtime.ExecutionListener;
import robolang.model.WorldSnapshot;

/**
 * Paces a run on the worker thread (SPEC §10.5): sleeps between actions, blocks while paused and turns Stop into
 * {@link ExecutionStoppedException}. Free of Swing; the controller reacts through a {@link Sink}.
 */
public final class PacingListener implements ExecutionListener {

    /** Receives progress on the worker thread; the controller forwards it to the EDT. */
    public interface Sink {
        /**
         * A variant starts; not called at Instant speed.
         *
         * @param variant zero-based index
         * @param initial the start state
         */
        void variantStarted(int variant, WorldSnapshot initial);

        /**
         * A node is entered; not called at Instant speed.
         *
         * @param node the node
         */
        void nodeEntered(AstNode node);

        /**
         * An action was performed; not called at Instant speed.
         *
         * @param after the world after the action
         * @param actions actions so far
         * @param ticks ticks so far
         */
        void actionPerformed(WorldSnapshot after, int actions, long ticks);

        /** The worker is about to block until resumed or stepped. */
        void paused();
    }

    /** Waits for a number of milliseconds. */
    @FunctionalInterface
    public interface Sleeper {
        /**
         * Waits.
         *
         * @param millis how long
         * @throws InterruptedException if interrupted
         */
        void sleep(long millis) throws InterruptedException;
    }

    private final Sink sink;
    private final IntSupplier speed;
    private final Semaphore permits = new Semaphore(0);
    private final Sleeper sleeper;
    private volatile boolean stopRequested;
    private volatile boolean pauseRequested;
    private volatile boolean stepRequested;
    private volatile int variant;
    private volatile WorldSnapshot last;
    private volatile int actions;
    private volatile long ticks;

    /**
     * Creates a listener whose waits end early on Stop.
     *
     * @param sink receives progress
     * @param speed actions per second, 0 for Instant; read at every call
     * @param stepFirst true to pause after the first action (Step from Editing)
     */
    public PacingListener(Sink sink, IntSupplier speed, boolean stepFirst) {
        this.sink = sink;
        this.speed = speed;
        this.stepRequested = stepFirst;
        this.sleeper = ms -> permits.tryAcquire(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a listener with its own sleeper, for tests.
     *
     * @param sink receives progress
     * @param speed actions per second, 0 for Instant
     * @param stepFirst true to pause after the first action
     * @param sleeper replaces the real wait
     */
    public PacingListener(Sink sink, IntSupplier speed, boolean stepFirst, Sleeper sleeper) {
        this.sink = sink;
        this.speed = speed;
        this.stepRequested = stepFirst;
        this.sleeper = sleeper;
    }

    /**
     * Grid animation length for a speed (SPEC §7.2).
     *
     * @param speed actions per second, 0 for Instant
     * @return {@code min(250, 1000 / speed * 0.8)} ms, 0 for Instant
     */
    public static int animationMillis(int speed) {
        return speed <= 0 ? 0 : (int) Math.min(250, 1000.0 / speed * 0.8);
    }

    /** Asks the run to pause at its next action. */
    public void pause() {
        pauseRequested = true;
    }

    /** Continues a paused run until it is paused or ends. */
    public void resume() {
        pauseRequested = false;
        stepRequested = false;
        permits.release();
    }

    /** Lets a paused run perform exactly one more action. */
    public void step() {
        stepRequested = true;
        permits.release();
    }

    /** Ends the run: the next listener call throws {@link ExecutionStoppedException}. */
    public void stop() {
        stopRequested = true;
        permits.release();
    }

    /**
     * Returns the world after the latest action.
     *
     * @return the snapshot, or the initial one before any action
     */
    public WorldSnapshot lastSnapshot() {
        return last;
    }

    /**
     * Returns the variant that ran last.
     *
     * @return zero-based index
     */
    public int variant() {
        return variant;
    }

    /**
     * Returns the actions performed so far, over all variants.
     *
     * @return the count
     */
    public int actions() {
        return actions;
    }

    /**
     * Returns the nodes entered so far, over all variants.
     *
     * @return the count
     */
    public long ticks() {
        return ticks;
    }

    @Override
    public void variantStarted(int index, WorldSnapshot initial) {
        checkStop(null);
        variant = index;
        last = initial;
        if (speed.getAsInt() > 0) {
            sink.variantStarted(index, initial);
        }
    }

    @Override
    public void nodeEntered(AstNode node) {
        ticks++;
        checkStop(node);
        if (speed.getAsInt() > 0) {
            sink.nodeEntered(node);
        }
    }

    @Override
    public void actionPerformed(AstNode node, WorldSnapshot after) {
        checkStop(node);
        actions++;
        last = after;
        int perSecond = speed.getAsInt();
        if (perSecond > 0) {
            sink.actionPerformed(after, actions, ticks);
        }
        if (stepRequested || pauseRequested) {
            stepRequested = false;
            sink.paused();
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                stopRequested = true;
            }
        } else if (perSecond > 0) {
            try {
                sleeper.sleep(1000L / perSecond);
            } catch (InterruptedException e) {
                stopRequested = true;
            }
        }
        checkStop(node);
    }

    private void checkStop(AstNode node) {
        if (stopRequested) {
            throw new ExecutionStoppedException(node == null ? null : node.range());
        }
    }
}

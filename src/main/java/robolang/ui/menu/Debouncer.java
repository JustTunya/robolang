package robolang.ui.menu;

import javax.swing.Timer;

/** Runs an action once after a quiet period; every {@link #trigger()} restarts the wait. */
public final class Debouncer {

    /** Runs a task later; injectable so tests need no real time. */
    public interface Scheduler {
        /**
         * Schedules a task once.
         *
         * @param task what to run
         * @param delayMillis how long to wait
         * @return a handle that cancels the task
         */
        Runnable schedule(Runnable task, long delayMillis);
    }

    private final Scheduler scheduler;
    private final long delayMillis;
    private final Runnable action;
    private Runnable cancelPending;

    /**
     * Creates a debouncer.
     *
     * @param scheduler runs the delayed task
     * @param delayMillis quiet period before the action runs
     * @param action the action
     */
    public Debouncer(Scheduler scheduler, long delayMillis, Runnable action) {
        this.scheduler = scheduler;
        this.delayMillis = delayMillis;
        this.action = action;
    }

    /**
     * Returns a scheduler backed by a Swing timer, so the task runs on the event dispatch thread.
     *
     * @return the scheduler
     */
    public static Scheduler swing() {
        return (task, ms) -> {
            Timer timer = new Timer((int) ms, e -> task.run());
            timer.setRepeats(false);
            timer.start();
            return timer::stop;
        };
    }

    /** Restarts the wait; the action runs once the quiet period passes without another trigger. */
    public void trigger() {
        cancel();
        cancelPending = scheduler.schedule(() -> {
            cancelPending = null;
            action.run();
        }, delayMillis);
    }

    /** Runs the action now if one is pending. */
    public void flush() {
        if (cancelPending != null) {
            cancel();
            action.run();
        }
    }

    /** Drops the pending action, if any. */
    public void cancel() {
        if (cancelPending != null) {
            cancelPending.run();
            cancelPending = null;
        }
    }
}

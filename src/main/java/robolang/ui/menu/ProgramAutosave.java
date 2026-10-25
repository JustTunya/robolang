package robolang.ui.menu;

import java.util.function.Supplier;

/**
 * Saves the editor text of the open level shortly after the last edit and when the level changes (FR-11).
 * Restoring is {@link ProgressPort#lastProgram}.
 */
public final class ProgramAutosave {
    private static final System.Logger LOG = System.getLogger(ProgramAutosave.class.getName());
    /** Quiet period after the last edit before the program is saved. */
    public static final long DELAY_MILLIS = 1000;

    private final ProgressPort store;
    private final Supplier<String> text;
    private final Debouncer debouncer;
    private String levelId;

    /**
     * Creates the autosave.
     *
     * @param store where programs are saved
     * @param scheduler runs the delayed save
     * @param text supplies the current editor text
     */
    public ProgramAutosave(ProgressPort store, Debouncer.Scheduler scheduler, Supplier<String> text) {
        this.store = store;
        this.text = text;
        this.debouncer = new Debouncer(scheduler, DELAY_MILLIS, this::save);
    }

    /**
     * Switches to another level. A pending save is written first, so call this before the editor text changes.
     *
     * @param newLevelId the level now open, or null for none
     */
    public void switchTo(String newLevelId) {
        debouncer.flush();
        levelId = newLevelId;
    }

    /** Tells the autosave that the text changed. */
    public void edited() {
        if (levelId != null) {
            debouncer.trigger();
        }
    }

    /** Drops a pending save, e.g. after the stored programs were deleted. */
    public void cancel() {
        debouncer.cancel();
    }

    private void save() {
        try {
            store.saveProgram(levelId, text.get());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING, "Cannot save program of " + levelId, e);
        }
    }
}

package robolang.ui.menu;

import java.util.Optional;
import java.util.OptionalInt;
import robolang.lang.runtime.Outcome;

/**
 * What the UI needs from the progress store. {@code ui} cannot depend on {@code app}, so {@code Main} adapts its
 * {@code ProgressStore} to this port.
 */
public interface ProgressPort {

    /**
     * Returns the best stars of a level.
     *
     * @param levelId level id
     * @return the stars, empty if the level was never solved
     */
    OptionalInt bestStars(String levelId);

    /**
     * Records a run; the store keeps the better result and ignores failures.
     *
     * @param levelId level id
     * @param outcome the run outcome
     */
    void record(String levelId, Outcome outcome);

    /**
     * Returns the last saved program of a level.
     *
     * @param levelId level id
     * @return the source, if any
     */
    Optional<String> lastProgram(String levelId);

    /**
     * Saves the current program of a level.
     *
     * @param levelId level id
     * @param source program text
     */
    void saveProgram(String levelId, String source);

    /** Deletes all stored results and programs. */
    void resetAll();
}

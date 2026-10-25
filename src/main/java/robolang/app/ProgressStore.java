package robolang.app;

import java.nio.file.Path;
import java.util.Optional;
import robolang.lang.runtime.Outcome;

/** Persists best results and last programs per level. */
public final class ProgressStore {
    /**
     * Opens the store. Implemented by T08.
     *
     * @param dir directory holding the data files
     */
    public ProgressStore(Path dir) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Returns the best result of a level. Implemented by T08.
     *
     * @param levelId level id
     * @return the best result, if any
     */
    public Optional<BestResult> best(String levelId) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Tells whether a level has been solved. Implemented by T08.
     *
     * @param levelId level id
     * @return true if solved
     */
    public boolean isSolved(String levelId) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Records a run, keeping the better result. Implemented by T08.
     *
     * @param levelId level id
     * @param outcome the run outcome
     */
    public void record(String levelId, Outcome outcome) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Returns the last saved program of a level. Implemented by T08.
     *
     * @param levelId level id
     * @return the source, if any
     */
    public Optional<String> lastProgram(String levelId) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Saves the current program of a level. Implemented by T08.
     *
     * @param levelId level id
     * @param source program text
     */
    public void saveProgram(String levelId, String source) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Deletes all stored progress. Implemented by T08.
     */
    public void resetAll() {
        throw new UnsupportedOperationException("T08");
    }
}

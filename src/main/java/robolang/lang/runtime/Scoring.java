package robolang.lang.runtime;

import robolang.model.Level;

/** Star calculation (SPEC §3.5). */
public final class Scoring {
    private Scoring() {
    }

    /**
     * Computes the stars for a run.
     *
     * @param solved whether the level was solved
     * @param instructionCount static instruction count
     * @param totalActions total actions over all variants
     * @param level the level, for its par values
     * @return 0 to 3 stars
     */
    public static int stars(boolean solved, int instructionCount, int totalActions, Level level) {
        if (!solved) {
            return 0;
        }
        return 1 + (instructionCount <= level.parInstructions() ? 1 : 0)
                + (totalActions <= level.parActions() ? 1 : 0);
    }
}

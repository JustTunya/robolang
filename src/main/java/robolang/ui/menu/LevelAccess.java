package robolang.ui.menu;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import robolang.model.Level;

/** Pure rules for the level selector: which levels are open (FR-03, FR-05) and how they are labelled (FR-02). */
public final class LevelAccess {
    private LevelAccess() {
    }

    /**
     * Tells whether a level can be opened: the first level and, in demo mode, every level; otherwise level n+1
     * once level n is solved.
     *
     * @param levels the campaign in order
     * @param index zero-based index of the level to test
     * @param solved tells whether a level id is solved
     * @param demo true in demo mode
     * @return true if unlocked
     */
    public static boolean isUnlocked(List<Level> levels, int index, Predicate<String> solved, boolean demo) {
        return demo || index <= 0 || solved.test(levels.get(index - 1).id());
    }

    /**
     * Builds the selector text of a level: number, title and either the best stars or a lock marker.
     *
     * @param number one-based position in the campaign
     * @param title level title
     * @param locked true if the level is locked
     * @param bestStars best stars so far, empty if unsolved
     * @return e.g. {@code 06  Unknown Corridor  ★★☆} or {@code 07  Scattered Harvest  (locked)}
     */
    public static String label(int number, String title, boolean locked, OptionalInt bestStars) {
        String prefix = String.format("%02d  %s  ", number, title);
        if (locked) {
            return prefix + "(locked)";
        }
        int stars = bestStars.orElse(0);
        return prefix + "★".repeat(stars) + "☆".repeat(3 - stars);
    }
}

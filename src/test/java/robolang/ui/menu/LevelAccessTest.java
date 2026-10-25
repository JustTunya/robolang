package robolang.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;
import robolang.model.Level;

class LevelAccessTest {
    private static Level level(String id) {
        return new Level(id, id, "", Set.of("MOVE"), 1, 1, List.of(), null);
    }

    private final List<Level> levels = List.of(level("a"), level("b"), level("c"));

    @Test
    void firstLevelIsAlwaysOpen() {
        assertTrue(LevelAccess.isUnlocked(levels, 0, id -> false, false));
    }

    @Test
    void nextLevelOpensWhenPreviousIsSolved() {
        assertFalse(LevelAccess.isUnlocked(levels, 1, id -> false, false));
        assertTrue(LevelAccess.isUnlocked(levels, 1, id -> id.equals("a"), false));
        assertFalse(LevelAccess.isUnlocked(levels, 2, id -> id.equals("a"), false));
        assertTrue(LevelAccess.isUnlocked(levels, 2, id -> id.equals("b"), false));
    }

    @Test
    void demoModeOpensEverything() {
        assertTrue(LevelAccess.isUnlocked(levels, 2, id -> false, true));
    }

    @Test
    void labelShowsNumberTitleAndStars() {
        assertEquals("06  Unknown Corridor  ★★☆", LevelAccess.label(6, "Unknown Corridor", false, OptionalInt.of(2)));
        assertEquals("01  First Steps  ☆☆☆", LevelAccess.label(1, "First Steps", false, OptionalInt.empty()));
        assertEquals("10  The Maze  ★★★", LevelAccess.label(10, "The Maze", false, OptionalInt.of(3)));
    }

    @Test
    void lockedLabelHidesStars() {
        assertEquals("07  Scattered Harvest  (locked)",
                LevelAccess.label(7, "Scattered Harvest", true, OptionalInt.of(3)));
    }
}

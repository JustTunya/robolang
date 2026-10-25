package robolang.levels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import robolang.lang.ast.Program;
import robolang.lang.parser.Parser;
import robolang.lang.parser.StaticChecker;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.ExecutionListener;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.LevelLoader;

/** Every campaign level is solvable at par by its reference solution (SPEC §5.2, §12). */
class LevelSolutionsTest {
    private static final List<Level> LEVELS = LevelLoader.loadAll();

    static Stream<Level> levels() {
        return LEVELS.stream();
    }

    private static Outcome run(String source, Level level, Set<String> keywords) {
        Program program = Parser.parse(source);
        StaticChecker.check(program, keywords);
        return ProgramRunner.run(program, level, ExecutionLimits.DEFAULT, new ExecutionListener() { });
    }

    private static Outcome run(String source, Level level) {
        return run(source, level, level.keywords());
    }

    private static Level level(int number) {
        return LEVELS.get(number - 1);
    }

    @Test
    void campaignHasTenLevelsWithSpecTitlesAndVariantCounts() {
        assertEquals(List.of("First Steps", "Corners", "Long Hall", "Harvest", "Staircase", "Unknown Corridor",
                "Scattered Harvest", "Fork in the Road", "Patterns", "The Maze"),
                LEVELS.stream().map(Level::title).toList());
        assertEquals(List.of(1, 1, 1, 1, 1, 2, 2, 3, 1, 3), LEVELS.stream().map(l -> l.variants().size()).toList());
    }

    @Test
    void keywordsAreCumulative() {
        for (int i = 1; i < LEVELS.size(); i++) {
            assertTrue(LEVELS.get(i).keywords().containsAll(LEVELS.get(i - 1).keywords()),
                    "level " + (i + 1) + " must allow everything level " + i + " allows");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("levels")
    void referenceSolutionSolvesAtPar(Level level) {
        assertNotNull(level.referenceSolution(), "missing .solution.rl");
        Outcome o = run(level.referenceSolution(), level);
        assertTrue(o.success(), o.message());
        assertEquals(level.parInstructions(), o.instructionCount(), "par-instructions");
        assertEquals(level.parActions(), o.totalActions(), "par-actions");
        assertEquals(3, o.stars());
    }

    @Test
    void longHallParNeedsRepeat() {
        Level l = level(3);
        Outcome flat = run("MOVE ".repeat(9), l);
        assertTrue(flat.success());
        assertTrue(flat.instructionCount() > l.parInstructions());
    }

    /** A program hard-coded for variant 1 solves variant 1 alone but fails the full level. */
    private static void assertHardCodedFails(int number, String hardCoded) {
        Level full = level(number);
        Level first = new Level(full.id(), full.title(), full.briefing(), full.keywords(), full.parInstructions(),
                full.parActions(), full.variants().subList(0, 1), full.referenceSolution());
        assertTrue(run(hardCoded, first).success(), "hard-coded program must solve variant 1");
        Outcome o = run(hardCoded, full);
        assertFalse(o.success());
        assertTrue(o.failedVariant() > 0, "must fail on a later variant");
    }

    @Test
    void unknownCorridorHardCodedWalkFails() {
        assertHardCodedFails(6, "MOVE ".repeat(6));
    }

    @Test
    void scatteredHarvestHardCodedWalkFails() {
        assertHardCodedFails(7, "MOVE MOVE PICK MOVE MOVE MOVE PICK MOVE MOVE");
    }

    @Test
    void forkInTheRoadHardCodedWalkFails() {
        assertHardCodedFails(8, "MOVE MOVE MOVE TURN LEFT MOVE MOVE MOVE TURN RIGHT MOVE MOVE MOVE");
    }

    @Test
    void patternsCallBeatsRepeatOnly() {
        Level l = level(9);
        String grab = "PICK TURN RIGHT MOVE PICK TURN LEFT MOVE PICK TURN LEFT MOVE TURN RIGHT ";
        String repeatOnly = "MOVE " + grab + "REPEAT 3 { MOVE } " + grab + "REPEAT 2 { MOVE } " + grab;
        Set<String> noProcedures = new HashSet<>(l.keywords());
        noProcedures.removeAll(Set.of("DEF", "CALL"));
        Outcome o = run(repeatOnly, l, noProcedures);
        assertTrue(o.success(), o.message());
        assertEquals(35, o.instructionCount());
        assertTrue(o.instructionCount() > l.parInstructions());
    }
}

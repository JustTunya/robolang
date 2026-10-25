package robolang.quality;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.Program;
import robolang.lang.error.RoboLangException;
import robolang.lang.parser.Parser;
import robolang.lang.parser.StaticChecker;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.ExecutionListener;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.LevelLoader;

/** NFR-04: random input never escapes as anything but a {@link RoboLangException}. */
class FuzzTest {
    static final long SEED = 20_240_611L;
    private static final int PROGRAMS = 10_000;
    private static final List<String> TOKENS = List.of("MOVE", "TURN", "LEFT", "RIGHT", "PICK", "REPEAT", "WHILE",
            "IF", "ELSE", "DEF", "CALL", "NOT", "AND", "OR", "WALL_AHEAD", "ON_ITEM", "AT_GOAL", "{", "}", "{", "}",
            "1", "3", "0", "999", "1000", "foo", "bar", "# c\n");
    private static final Level LEVEL = LevelLoader.parse("fuzz", """
            title: Fuzz
            briefing: Fuzz level.
            keywords: MOVE
            par-instructions: 1
            par-actions: 1
            grid:
            #####
            #>..#
            #...#
            #..G#
            #####
            """);

    @Test
    void randomProgramsNeverEscape() {
        Random rnd = new Random(SEED);
        for (int i = 0; i < PROGRAMS; i++) {
            String src = i % 2 == 0 ? tokens(rnd) : printable(rnd);
            try {
                runPipeline(src);
            } catch (Throwable t) {
                fail("seed=" + SEED + " program #" + i + " source=<" + src + ">", t);
            }
        }
    }

    @Test
    void sameSeedSameSources() {
        Random a = new Random(SEED);
        Random b = new Random(SEED);
        for (int i = 0; i < 100; i++) {
            org.junit.jupiter.api.Assertions.assertEquals(tokens(a), tokens(b));
            org.junit.jupiter.api.Assertions.assertEquals(printable(a), printable(b));
        }
    }

    private static void runPipeline(String src) {
        Program program;
        try {
            program = Parser.parse(src);
            StaticChecker.check(program, LevelLoader.KEYWORDS);
        } catch (RoboLangException expected) {
            return;
        }
        Outcome outcome = ProgramRunner.run(program, LEVEL, new ExecutionLimits(10_000, 64),
                new ExecutionListener() { });
        assertNotNull(outcome);
    }

    private static String tokens(Random rnd) {
        StringBuilder sb = new StringBuilder();
        for (int n = rnd.nextInt(40); n >= 0; n--) {
            sb.append(TOKENS.get(rnd.nextInt(TOKENS.size()))).append(' ');
        }
        return sb.toString();
    }

    private static String printable(Random rnd) {
        StringBuilder sb = new StringBuilder();
        for (int n = rnd.nextInt(80); n >= 0; n--) {
            sb.append((char) (32 + rnd.nextInt(95)));
        }
        return sb.toString();
    }
}

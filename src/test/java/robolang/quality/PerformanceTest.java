package robolang.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.Program;
import robolang.lang.parser.Parser;
import robolang.lang.parser.StaticChecker;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.ExecutionListener;
import robolang.lang.runtime.FailureKind;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.LevelLoader;

/** NFR-02: parse and run speed. */
@Tag("perf")
class PerformanceTest {

    @Test
    void tenThousandCharactersParseUnder100ms() {
        String line = "REPEAT 2 { IF NOT WALL_AHEAD { MOVE } ELSE { TURN LEFT } }\n";
        String src = line.repeat(10_000 / line.length() + 1).substring(0, 10_000 / line.length() * line.length());
        assertTrue(src.length() >= 9_900);
        for (int i = 0; i < 3; i++) {
            Parser.parse(src);
        }
        long[] ns = new long[5];
        for (int i = 0; i < ns.length; i++) {
            long t = System.nanoTime();
            Parser.parse(src);
            ns[i] = System.nanoTime() - t;
        }
        Arrays.sort(ns);
        assertTrue(ns[2] < 100_000_000L, "median parse ms: " + ns[2] / 1e6);
    }

    @Test
    void hundredThousandTicksRunUnderOneSecond() {
        Level level = LevelLoader.parse("perf", """
                title: Perf
                briefing: Perf level.
                keywords: WHILE
                par-instructions: 1
                par-actions: 1
                grid:
                #####
                #>..#
                #..G#
                #####
                """);
        Program p = Parser.parse("WHILE NOT AT_GOAL { TURN LEFT }");
        StaticChecker.check(p, LevelLoader.KEYWORDS);
        long t = System.nanoTime();
        Outcome o = ProgramRunner.run(p, level, ExecutionLimits.DEFAULT, new ExecutionListener() { });
        long ms = (System.nanoTime() - t) / 1_000_000;
        assertEquals(FailureKind.TICK_LIMIT, o.failure());
        assertTrue(ms < 1_000, "run ms: " + ms);
    }
}

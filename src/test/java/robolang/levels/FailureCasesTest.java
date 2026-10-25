package robolang.levels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import robolang.lang.ast.Program;
import robolang.lang.ast.SourceRange;
import robolang.lang.parser.Parser;
import robolang.lang.parser.StaticChecker;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.ExecutionListener;
import robolang.lang.runtime.FailureKind;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.LevelLoader;

/** One wrong program per failure kind of SPEC §3.4 (except Stopped), on small inline levels. */
class FailureCasesTest {
    private static final String HEADER = "title: t\nbriefing: b\nkeywords: MOVE PICK WHILE NOT AT_GOAL DEF CALL\n"
            + "par-instructions: 1\npar-actions: 1\n";

    private static Outcome run(String grids, String source) {
        Level level = LevelLoader.parse("inline", HEADER + grids);
        Program program = Parser.parse(source);
        StaticChecker.check(program, level.keywords());
        return ProgramRunner.run(program, level, ExecutionLimits.DEFAULT, new ExecutionListener() { });
    }

    private static void assertFailure(Outcome o, FailureKind kind, int variant, SourceRange where) {
        assertFalse(o.success());
        assertEquals(kind, o.failure());
        assertEquals(variant, o.failedVariant());
        assertEquals(where, o.location());
    }

    @Test
    void crashIntoWallInSecondVariant() {
        Outcome o = run("grid:\n#####\n#>.G#\n#####\ngrid:\n####\n#>G#\n####\n", "MOVE\nMOVE\n");
        assertFailure(o, FailureKind.CRASH, 1, new SourceRange(2, 1, 2, 4));
    }

    @Test
    void emptyPick() {
        Outcome o = run("grid:\n#####\n#>*G#\n#####\n", "MOVE\nPICK\nPICK\n");
        assertFailure(o, FailureKind.EMPTY_PICK, 0, new SourceRange(3, 1, 3, 4));
    }

    @Test
    void tickLimit() {
        Outcome o = run("grid:\n#####\n#>.G#\n#####\n", "WHILE NOT AT_GOAL { }\n");
        assertFailure(o, FailureKind.TICK_LIMIT, 0, new SourceRange(1, 7, 1, 17));
    }

    @Test
    void recursionLimit() {
        Outcome o = run("grid:\n#####\n#>.G#\n#####\n", "DEF f { CALL f }\nCALL f\n");
        assertFailure(o, FailureKind.RECURSION_LIMIT, 0, new SourceRange(1, 9, 1, 14));
    }

    @Test
    void incomplete() {
        Outcome o = run("grid:\n#####\n#>.G#\n#####\n", "MOVE\n");
        assertFailure(o, FailureKind.INCOMPLETE, 0, new SourceRange(1, 1, 1, 4));
    }
}

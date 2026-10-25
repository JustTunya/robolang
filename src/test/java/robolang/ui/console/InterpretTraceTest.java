package robolang.ui.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.IfStmt;
import robolang.lang.ast.Program;
import robolang.lang.ast.RepeatStmt;
import robolang.lang.parser.Parser;

class InterpretTraceTest {
    private final List<String> lines = new ArrayList<>();
    private boolean on = true;
    private final InterpretTrace trace = new InterpretTrace(lines::add, () -> on);

    private final Program program = Parser.parse("REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }");
    private final RepeatStmt repeat = (RepeatStmt) program.main().statements().get(0);
    private final IfStmt ifStmt = (IfStmt) repeat.body().statements().get(1);

    @Test
    void indentsLabelsByTreeDepth() {
        trace.programParsed(program);
        trace.nodeEntered(repeat);
        trace.nodeEntered(repeat.body().statements().get(0));
        trace.nodeEntered(ifStmt);
        trace.nodeEntered(ifStmt.condition());

        int repeatDepth = indent(lines.get(1));
        int moveDepth = indent(lines.get(2));
        int ifDepth = indent(lines.get(3));
        int condDepth = indent(lines.get(4));
        assertTrue(lines.get(1).trim().startsWith("REPEAT"), lines.get(1));
        assertTrue(moveDepth > repeatDepth, "MOVE is nested in REPEAT");
        assertEquals(moveDepth, ifDepth, "MOVE and IF are siblings");
        assertTrue(condDepth > ifDepth, "the condition is nested in IF");
    }

    @Test
    void logsNothingWhileTraceModeIsOff() {
        on = false;
        trace.programParsed(program);
        trace.nodeEntered(repeat);
        assertTrue(lines.isEmpty());
    }

    private static int indent(String line) {
        return line.length() - line.stripLeading().length();
    }
}

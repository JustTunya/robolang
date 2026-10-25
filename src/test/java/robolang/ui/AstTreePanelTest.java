package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import robolang.lang.ast.IfStmt;
import robolang.lang.ast.Program;
import robolang.lang.ast.RepeatStmt;
import robolang.lang.ast.SourceRange;
import robolang.lang.parser.Parser;
import robolang.lang.runtime.FailureKind;
import robolang.lang.runtime.Outcome;

class AstTreePanelTest {
    private static final String SRC = "REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }";
    private final CodeEditorPanel editor = new CodeEditorPanel();
    private final AstTreePanel panel = new AstTreePanel(editor);
    private final Program program = Parser.parse(SRC);

    AstTreePanelTest() {
        editor.setProgram(SRC);
        panel.programParsed(program);
    }

    @Test
    void parsedExpandsAll() {
        assertEquals(9, panel.tree().getRowCount());
    }

    @Test
    void nodeEnteredHighlightsBlue() {
        IfStmt ifStmt = (IfStmt) ((RepeatStmt) program.main().statements().get(0)).body().statements().get(1);
        panel.nodeEntered(ifStmt);
        assertSame(ifStmt, panel.highlightedItem().node());
        assertEquals(new Color(0x1F6FEB), panel.highlightColor());
        panel.nodeEntered(ifStmt.condition());
        assertSame(ifStmt.condition(), panel.highlightedItem().node());
    }

    @Test
    void failureHighlightsRedAndResetClears() {
        RepeatStmt repeat = (RepeatStmt) program.main().statements().get(0);
        SourceRange where = repeat.body().statements().get(0).range();
        panel.runFinished(new Outcome(false, FailureKind.CRASH, "x", where, 0, 4, 0, 0, 0));
        assertSame(repeat.body().statements().get(0), panel.highlightedItem().node());
        assertEquals(new Color(0xD1242F), panel.highlightColor());
        panel.reset();
        assertNull(panel.highlightedItem());
        assertNull(panel.highlightColor());
    }

    @Test
    void selectingRowSelectsSourceRange() {
        panel.tree().setSelectionRow(2); // REPEAT 2
        assertEquals(SRC, editor.textPane().getSelectedText());
    }

    @Test
    void failureOnSoleStatementHighlightsStatementNotProgram() {
        Program single = Parser.parse("MOVE");
        panel.programParsed(single);
        SourceRange where = single.main().statements().get(0).range();
        panel.runFinished(new Outcome(false, FailureKind.CRASH, "x", where, 0, 1, 0, 0, 0));
        assertSame(single.main().statements().get(0), panel.highlightedItem().node());
    }
}

package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.Highlight;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.SourceRange;
import robolang.ui.CodeEditorPanel.HighlightKind;

class CodeEditorPanelTest {
    private static final String SRC = "REPEAT 4 {\n    MOVE\n}\n";

    private static String text(CodeEditorPanel editor, Highlight h) throws BadLocationException {
        return editor.textPane().getDocument().getText(h.getStartOffset(), h.getEndOffset() - h.getStartOffset());
    }

    @Test
    void programRoundTrips() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        assertEquals(SRC, editor.getProgram());
    }

    @Test
    void highlightCoversRangeWithInclusiveEnd() throws BadLocationException {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(2, 5, 2, 8), HighlightKind.EXECUTING);
        Highlight[] hs = editor.highlights();
        assertEquals(1, hs.length);
        assertEquals("MOVE", text(editor, hs[0]));
    }

    @Test
    void multiLineRange() throws BadLocationException {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(1, 1, 3, 1), HighlightKind.ERROR);
        assertEquals(SRC.substring(0, SRC.length() - 1), text(editor, editor.highlights()[0]));
    }

    @Test
    void newRangeOfSameKindReplacesOldOne() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(1, 1, 1, 6), HighlightKind.EXECUTING);
        editor.highlightRange(new SourceRange(2, 5, 2, 8), HighlightKind.EXECUTING);
        editor.highlightRange(new SourceRange(3, 1, 3, 1), HighlightKind.ERROR);
        assertEquals(2, editor.highlights().length);
    }

    @Test
    void clearHighlightsRemovesAll() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(1, 1, 1, 6), HighlightKind.EXECUTING);
        editor.highlightRange(new SourceRange(2, 5, 2, 8), HighlightKind.ERROR);
        editor.clearHighlights();
        assertEquals(0, editor.highlights().length);
    }

    @Test
    void outOfRangeIsClamped() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(2, 5, 99, 99), HighlightKind.ERROR);
        assertEquals(1, editor.highlights().length);
        editor.setProgram("");
        editor.highlightRange(new SourceRange(1, 1, 1, 5), HighlightKind.ERROR);
        assertEquals(0, editor.highlights().length);
    }

    @Test
    void selectRangeSelectsText() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.selectRange(new SourceRange(1, 1, 1, 6));
        assertEquals("REPEAT", editor.textPane().getSelectedText());
    }

    @Test
    void readOnlyBlocksEditing() {
        CodeEditorPanel editor = new CodeEditorPanel();
        assertFalse(editor.isReadOnly());
        editor.setReadOnly(true);
        assertTrue(editor.isReadOnly());
        assertFalse(editor.textPane().isEditable());
        editor.setReadOnly(false);
        assertTrue(editor.textPane().isEditable());
    }

    @Test
    void editListenerSeesUserEditsButNotSetProgram() throws BadLocationException {
        CodeEditorPanel editor = new CodeEditorPanel();
        AtomicInteger edits = new AtomicInteger();
        editor.addEditListener(edits::incrementAndGet);
        editor.setProgram(SRC);
        assertEquals(0, edits.get());
        editor.textPane().getDocument().insertString(0, "# hi\n", null);
        assertEquals(1, edits.get());
        editor.textPane().getDocument().remove(0, 5);
        assertEquals(2, edits.get());
    }

    @Test
    void setProgramClearsHighlights() {
        CodeEditorPanel editor = new CodeEditorPanel();
        editor.setProgram(SRC);
        editor.highlightRange(new SourceRange(1, 1, 1, 6), HighlightKind.EXECUTING);
        editor.setProgram(SRC);
        assertEquals(0, editor.highlights().length);
    }
}

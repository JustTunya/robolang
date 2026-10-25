package robolang.ui.highlight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JTextPane;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.junit.jupiter.api.Test;

class SyntaxHighlighterTest {
    private final JTextPane pane = new JTextPane();
    private final StyledDocument doc = pane.getStyledDocument();

    @Test
    void appliesRunsToTheDocument() throws Exception {
        doc.insertString(0, "MOVE 5 # c", null);
        SyntaxHighlighter.apply(doc);

        assertTrue(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes()));
        assertFalse(StyleConstants.isBold(doc.getCharacterElement(5).getAttributes()));
        assertTrue(StyleConstants.isItalic(doc.getCharacterElement(7).getAttributes()));
        assertNotEquals(StyleConstants.getForeground(doc.getCharacterElement(0).getAttributes()),
                StyleConstants.getForeground(doc.getCharacterElement(5).getAttributes()));
    }

    @Test
    void lexerErrorClearsEarlierStyling() throws Exception {
        doc.insertString(0, "MOVE", null);
        SyntaxHighlighter.apply(doc);
        assertTrue(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes()));

        doc.insertString(4, " $", null);
        SyntaxHighlighter.apply(doc);
        assertFalse(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes()));
        assertEquals(java.awt.Color.BLACK, StyleConstants.getForeground(doc.getCharacterElement(0).getAttributes()));
    }

    @Test
    void installRestylesAfterTheDebounce() throws Exception {
        SyntaxHighlighter.install(pane);
        doc.insertString(0, "MOVE", null);
        long deadline = System.currentTimeMillis() + 3000;
        while (!StyleConstants.isBold(doc.getCharacterElement(0).getAttributes())
                && System.currentTimeMillis() < deadline) {
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
            Thread.sleep(20);
        }
        assertTrue(StyleConstants.isBold(doc.getCharacterElement(0).getAttributes()));
    }
}

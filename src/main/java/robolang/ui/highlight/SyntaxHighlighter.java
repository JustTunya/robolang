package robolang.ui.highlight;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import robolang.ui.highlight.SyntaxRuns.Run;
import robolang.ui.highlight.SyntaxRuns.Style;

/** Colors the text of a styled document from {@link SyntaxRuns}, 150 ms after the last change (FR-13). */
public final class SyntaxHighlighter {
    /** Quiet period after the last edit before the text is restyled. */
    public static final int DELAY_MILLIS = 150;

    private static final AttributeSet PLAIN = attrs(Color.BLACK, false, false);
    private static final Map<Style, AttributeSet> STYLES = new EnumMap<>(Style.class);

    static {
        STYLES.put(Style.KEYWORD, attrs(new Color(0x0033b3), true, false));
        STYLES.put(Style.CONDITION, attrs(new Color(0x871094), true, false));
        STYLES.put(Style.NUMBER, attrs(new Color(0x1750eb), false, false));
        STYLES.put(Style.COMMENT, attrs(new Color(0x8c8c8c), false, true));
        STYLES.put(Style.IDENTIFIER, attrs(new Color(0x00627a), false, false));
    }

    private SyntaxHighlighter() {
    }

    /**
     * Keeps a text component highlighted; its document must be a {@link StyledDocument}.
     *
     * @param pane the component, e.g. a {@code JTextPane}
     */
    public static void install(JTextComponent pane) {
        StyledDocument doc = (StyledDocument) pane.getDocument();
        Timer timer = new Timer(DELAY_MILLIS, e -> apply(doc));
        timer.setRepeats(false);
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // attribute changes only, including our own
            }
        });
    }

    /** Restyles the whole document now. */
    static void apply(StyledDocument doc) {
        try {
            String text = doc.getText(0, doc.getLength());
            doc.setCharacterAttributes(0, text.length(), PLAIN, true);
            for (Run run : SyntaxRuns.of(text)) {
                doc.setCharacterAttributes(run.start(), run.length(), STYLES.get(run.style()), false);
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AttributeSet attrs(Color color, boolean bold, boolean italic) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, color);
        StyleConstants.setBold(a, bold);
        StyleConstants.setItalic(a, italic);
        return a;
    }
}

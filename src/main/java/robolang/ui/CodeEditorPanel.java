package robolang.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import robolang.lang.ast.SourceRange;
import robolang.ui.highlight.SyntaxHighlighter;

/** The program editor: a monospace {@link JTextPane} with a line-number gutter (SPEC §7.2). */
@SuppressWarnings("serial")
public final class CodeEditorPanel extends JPanel {

    /** What a highlighted range means. */
    public enum HighlightKind {
        /** The node that is executing now. */
        EXECUTING,
        /** The place of an error or failure. */
        ERROR
    }

    private static final Map<HighlightKind, Color> COLORS = Map.of(
            HighlightKind.EXECUTING, new Color(0x9ec5ff),
            HighlightKind.ERROR, new Color(0xff9e9e));

    private final JTextPane pane = new JTextPane() {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            // no line wrapping (one document line is one gutter number), but fill a wider viewport
            return getParent() instanceof JViewport vp && vp.getWidth() > getUI().getPreferredSize(this).width;
        }
    };
    private final Map<HighlightKind, Object> tags = new EnumMap<>(HighlightKind.class);
    private boolean programmatic;

    /** Creates an empty, editable editor. */
    public CodeEditorPanel() {
        super(new java.awt.BorderLayout());
        pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setRowHeaderView(new Gutter());
        add(scroll);
        SyntaxHighlighter.install(pane);
    }

    /**
     * Returns the program text.
     *
     * @return the full editor content
     */
    public String getProgram() {
        Document doc = pane.getDocument();
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Replaces the program text and clears all highlights. Edit listeners are not notified.
     *
     * @param source the new content
     */
    public void setProgram(String source) {
        programmatic = true;
        try {
            pane.setText(source);
        } finally {
            programmatic = false;
        }
        clearHighlights();
        pane.setCaretPosition(0);
    }

    /**
     * Makes the editor editable or not.
     *
     * @param readOnly true to block typing
     */
    public void setReadOnly(boolean readOnly) {
        pane.setEditable(!readOnly);
    }

    /**
     * Returns whether typing is blocked.
     *
     * @return true when read-only
     */
    public boolean isReadOnly() {
        return !pane.isEditable();
    }

    /**
     * Highlights a source range. A new range replaces the previous range of the same kind.
     *
     * @param range the range, lines and columns 1-based, end column inclusive; clamped to the text
     * @param kind what the range means
     */
    public void highlightRange(SourceRange range, HighlightKind kind) {
        removeHighlight(kind);
        int[] span = offsets(range);
        if (span[0] == span[1]) {
            return;
        }
        try {
            tags.put(kind, pane.getHighlighter().addHighlight(span[0], span[1],
                    new DefaultHighlighter.DefaultHighlightPainter(COLORS.get(kind))));
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Selects a source range and scrolls to it.
     *
     * @param range the range, as for {@link #highlightRange}
     */
    public void selectRange(SourceRange range) {
        int[] span = offsets(range);
        pane.select(span[0], span[1]);
    }

    /** Removes every highlight. */
    public void clearHighlights() {
        for (HighlightKind kind : HighlightKind.values()) {
            removeHighlight(kind);
        }
    }

    /**
     * Registers a listener for user edits; changes made by {@link #setProgram} are not reported.
     *
     * @param listener called after each insertion or removal
     */
    public void addEditListener(Runnable listener) {
        pane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // attribute changes only
            }

            private void fire() {
                if (!programmatic) {
                    listener.run();
                }
            }
        });
    }

    /**
     * Returns the text component, for tests and key bindings.
     *
     * @return the text pane
     */
    public JTextPane textPane() {
        return pane;
    }

    /**
     * Returns the currently highlighted spans.
     *
     * @return the highlights of the text pane
     */
    public Highlighter.Highlight[] highlights() {
        return pane.getHighlighter().getHighlights();
    }

    private void removeHighlight(HighlightKind kind) {
        Object tag = tags.remove(kind);
        if (tag != null) {
            pane.getHighlighter().removeHighlight(tag);
        }
    }

    /** Converts a 1-based range (end column inclusive) to document offsets {start, end}, clamped. */
    private int[] offsets(SourceRange r) {
        int length = pane.getDocument().getLength();
        int start = Math.min(offset(r.startLine(), r.startCol()), length);
        int end = Math.min(Math.max(offset(r.endLine(), r.endCol()) + 1, start), length);
        return new int[] {start, end};
    }

    private int offset(int line, int col) {
        Element root = pane.getDocument().getDefaultRootElement();
        Element el = root.getElement(Math.max(0, Math.min(line - 1, root.getElementCount() - 1)));
        return Math.min(el.getStartOffset() + Math.max(0, col - 1), el.getEndOffset() - 1);
    }

    /** Line numbers beside the text. */
    @SuppressWarnings("serial")
    private final class Gutter extends JComponent {
        Gutter() {
            setOpaque(true);
            setBackground(new Color(0xeceff4));
            setForeground(new Color(0x7a808c));
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            pane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    refresh();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    refresh();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    // attribute changes only
                }
            });
        }

        private void refresh() {
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            int lines = pane.getDocument().getDefaultRootElement().getElementCount();
            int digits = Math.max(2, String.valueOf(lines).length());
            FontMetrics fm = getFontMetrics(pane.getFont());
            return new Dimension(fm.charWidth('0') * digits + 12, pane.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
            g.setFont(pane.getFont());
            FontMetrics fm = g.getFontMetrics();
            Element root = pane.getDocument().getDefaultRootElement();
            for (int i = 0; i < root.getElementCount(); i++) {
                try {
                    Rectangle2D line = pane.modelToView2D(root.getElement(i).getStartOffset());
                    if (line == null) {
                        continue;
                    }
                    String number = String.valueOf(i + 1);
                    g.drawString(number, getWidth() - 6 - fm.stringWidth(number),
                            (int) line.getY() + fm.getAscent());
                } catch (BadLocationException e) {
                    return;
                }
            }
        }
    }
}

package robolang.ui.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/** A collapsible log panel for the factory and interpreter traces (SPEC §7.2, FR-61). */
@SuppressWarnings("serial")
public final class TraceConsole extends JPanel {
    /** Oldest lines are dropped beyond this many, so a long trace cannot exhaust memory. */
    static final int MAX_LINES = 2000;

    private final JTextArea area = new JTextArea();
    private final JScrollPane body = new JScrollPane(area);
    private final JButton header = new JButton();
    private Consumer<Boolean> toggleListener = expanded -> { };

    /** Creates a collapsed console. */
    public TraceConsole() {
        super(new BorderLayout());
        area.setEditable(false);
        area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        body.setPreferredSize(new Dimension(100, 140));
        header.setHorizontalAlignment(JButton.LEFT);
        header.addActionListener(e -> setExpanded(!isExpanded()));
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        setExpanded(false);
    }

    /**
     * Appends a line.
     *
     * @param line the text without line break
     */
    public void log(String line) {
        area.append(line + "\n");
        int extra = area.getLineCount() - 1 - MAX_LINES; // the text ends with a line break, hence the extra line
        if (extra > 0) {
            try {
                area.replaceRange("", 0, area.getLineEndOffset(extra - 1));
            } catch (BadLocationException e) {
                throw new IllegalStateException(e);
            }
        }
        area.setCaretPosition(area.getDocument().getLength());
    }

    /** Removes all lines. */
    public void clear() {
        area.setText("");
    }

    /**
     * Returns the logged text.
     *
     * @return all lines
     */
    public String text() {
        return area.getText();
    }

    /**
     * Shows or hides the log.
     *
     * @param expanded true to show it
     */
    public void setExpanded(boolean expanded) {
        body.setVisible(expanded);
        header.setText((expanded ? "▾ " : "▸ ") + "Console");
        revalidate();
        toggleListener.accept(expanded);
    }

    /**
     * Tells whether the log is shown.
     *
     * @return true when expanded
     */
    public boolean isExpanded() {
        return body.isVisible();
    }

    /**
     * Registers the listener told whenever the console is expanded or collapsed.
     *
     * @param listener receives the new state
     */
    public void onToggle(Consumer<Boolean> listener) {
        toggleListener = listener;
    }
}

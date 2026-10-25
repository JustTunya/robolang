package robolang.ui.briefing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import robolang.model.Level;

/** The dialog shown when a level opens (FR-04). */
@SuppressWarnings("serial")
public final class BriefingDialog extends JDialog {

    private BriefingDialog(Frame owner, Level level) {
        super(owner, level.title(), true);
        JTextArea text = new JTextArea(BriefingText.of(level));
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        JButton start = new JButton("Start");
        start.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(start);
        JPanel buttons = new JPanel();
        buttons.add(start);
        add(text, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(420, 240));
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the briefing and waits until it is closed.
     *
     * @param owner the parent window
     * @param level the level that was opened
     */
    public static void show(Frame owner, Level level) {
        new BriefingDialog(owner, level).setVisible(true);
    }
}

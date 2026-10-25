package robolang.app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Application entry point. T05 replaces the empty window with the real UI. */
public final class Main {
    private Main() {
    }

    /**
     * Opens the main window on the event dispatch thread.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RoboLang");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(960, 640);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

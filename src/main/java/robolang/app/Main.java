package robolang.app;

import java.util.List;
import javax.swing.SwingUtilities;
import robolang.model.Level;
import robolang.model.LevelLoader;
import robolang.theme.ThemeFactory;
import robolang.theme.ThemeRegistry;
import robolang.ui.MainFrame;

/** Application entry point. */
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
            MainFrame frame = new MainFrame();
            frame.setThemes(themes(), defaultTheme());
            frame.setLevels(levels());
            frame.setVisible(true);
        });
    }

    // The loaders may still be stubs; the window then starts without levels or themes.
    private static List<Level> levels() {
        try {
            return LevelLoader.loadAll();
        } catch (UnsupportedOperationException e) {
            return List.of();
        }
    }

    private static List<ThemeFactory> themes() {
        try {
            return ThemeRegistry.all();
        } catch (UnsupportedOperationException e) {
            return List.of();
        }
    }

    private static ThemeFactory defaultTheme() {
        try {
            return ThemeRegistry.defaultTheme();
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }
}

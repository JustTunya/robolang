package robolang.app;

import com.formdev.flatlaf.FlatLightLaf;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import robolang.lang.runtime.Outcome;
import robolang.model.Level;
import robolang.model.LevelLoader;
import robolang.theme.ThemeFactory;
import robolang.theme.ThemeRegistry;
import robolang.ui.MainFrame;
import robolang.ui.menu.ProgressPort;
import robolang.ui.menu.SettingsPort;

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
        Path dir = Path.of(System.getProperty("user.home"), ".robolang");
        ProgressStore store = new ProgressStore(dir);
        Settings settings = new Settings(dir);
        SwingUtilities.invokeLater(() -> {
            applyLookAndFeel();
            MainFrame frame = new MainFrame(progressPort(store), settingsPort(settings));
            frame.setThemes(themes(), ThemeRegistry.byName(settings.themeName()));
            frame.setLevels(levels());
            frame.setVisible(true);
            SwingUtilities.invokeLater(frame::showBriefing);
        });
    }

    // FlatLaf light; the system look and feel if FlatLaf is missing or fails.
    private static void applyLookAndFeel() {
        try {
            if (FlatLightLaf.setup()) {
                return;
            }
        } catch (LinkageError e) {
            // FlatLaf is not on the class path
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // keep the default look and feel
        }
    }

    private static ProgressPort progressPort(ProgressStore store) {
        return new ProgressPort() {
            @Override
            public OptionalInt bestStars(String levelId) {
                return store.best(levelId).map(b -> OptionalInt.of(b.stars())).orElse(OptionalInt.empty());
            }

            @Override
            public void record(String levelId, Outcome outcome) {
                store.record(levelId, outcome);
            }

            @Override
            public Optional<String> lastProgram(String levelId) {
                return store.lastProgram(levelId);
            }

            @Override
            public void saveProgram(String levelId, String source) {
                store.saveProgram(levelId, source);
            }

            @Override
            public void resetAll() {
                store.resetAll();
            }
        };
    }

    private static SettingsPort settingsPort(Settings settings) {
        return new SettingsPort() {
            @Override
            public boolean demoMode() {
                return settings.demoMode();
            }

            @Override
            public void setDemoMode(boolean on) {
                settings.setDemoMode(on);
            }

            @Override
            public void setThemeName(String name) {
                settings.setThemeName(name);
            }
        };
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
}

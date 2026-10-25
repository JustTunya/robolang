package robolang.app;

import java.nio.file.Path;
import java.util.Properties;

/** User settings: theme and demo mode. */
public final class Settings {
    private final Path file;
    private final Properties props;

    /**
     * Opens the settings.
     *
     * @param dir directory holding the settings file
     */
    public Settings(Path dir) {
        file = dir.resolve("settings.properties");
        props = ProgressStore.load(file);
    }

    /**
     * Returns the chosen theme name.
     *
     * @return theme name
     */
    public String themeName() {
        return props.getProperty("theme", "Space");
    }

    /**
     * Stores the chosen theme name.
     *
     * @param name theme name
     */
    public void setThemeName(String name) {
        props.setProperty("theme", name);
        save();
    }

    /**
     * Tells whether demo mode is on.
     *
     * @return true if demo mode is on
     */
    public boolean demoMode() {
        return Boolean.parseBoolean(props.getProperty("demo", "false"));
    }

    /**
     * Switches demo mode.
     *
     * @param on true to enable
     */
    public void setDemoMode(boolean on) {
        props.setProperty("demo", Boolean.toString(on));
        save();
    }

    private void save() {
        ProgressStore.write(file, w -> props.store(w, null));
    }
}

package robolang.app;

import java.nio.file.Path;

/** User settings: theme and demo mode. */
public final class Settings {
    /**
     * Opens the settings. Implemented by T08.
     *
     * @param dir directory holding the settings file
     */
    public Settings(Path dir) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Returns the chosen theme name. Implemented by T08.
     *
     * @return theme name
     */
    public String themeName() {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Stores the chosen theme name. Implemented by T08.
     *
     * @param name theme name
     */
    public void setThemeName(String name) {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Tells whether demo mode is on. Implemented by T08.
     *
     * @return true if demo mode is on
     */
    public boolean demoMode() {
        throw new UnsupportedOperationException("T08");
    }

    /**
     * Switches demo mode. Implemented by T08.
     *
     * @param on true to enable
     */
    public void setDemoMode(boolean on) {
        throw new UnsupportedOperationException("T08");
    }
}

package robolang.ui.menu;

/** What the UI needs from the user settings; adapted from {@code app.Settings} by {@code Main}. */
public interface SettingsPort {

    /**
     * Tells whether demo mode is on.
     *
     * @return true if on
     */
    boolean demoMode();

    /**
     * Stores demo mode.
     *
     * @param on true to enable
     */
    void setDemoMode(boolean on);

    /**
     * Stores the chosen theme.
     *
     * @param name theme name
     */
    void setThemeName(String name);
}

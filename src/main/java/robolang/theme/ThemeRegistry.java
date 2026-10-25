package robolang.theme;

import java.util.List;

/** The only place that names concrete theme factories (D7). */
public final class ThemeRegistry {
    private ThemeRegistry() {
    }

    /**
     * Lists the available themes. Implemented by T04.
     *
     * @return all factories
     */
    public static List<ThemeFactory> all() {
        throw new UnsupportedOperationException("T04");
    }

    /**
     * Finds a theme by name. Implemented by T04.
     *
     * @param name theme name
     * @return the matching factory
     */
    public static ThemeFactory byName(String name) {
        throw new UnsupportedOperationException("T04");
    }

    /**
     * Returns the default theme. Implemented by T04.
     *
     * @return the default factory
     */
    public static ThemeFactory defaultTheme() {
        throw new UnsupportedOperationException("T04");
    }
}

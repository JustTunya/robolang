package robolang.theme;

import java.util.List;
import robolang.theme.jungle.JungleThemeFactory;
import robolang.theme.retro.RetroThemeFactory;
import robolang.theme.space.SpaceThemeFactory;

/** The only place that names concrete theme factories (D7). */
public final class ThemeRegistry {
    private static final List<ThemeFactory> ALL = List.of(SpaceThemeFactory.INSTANCE, JungleThemeFactory.INSTANCE,
            RetroThemeFactory.INSTANCE);

    private ThemeRegistry() {
    }

    /**
     * Lists the available themes, Space first.
     *
     * @return all factories
     */
    public static List<ThemeFactory> all() {
        return ALL;
    }

    /**
     * Finds a theme by name, ignoring case. Unknown names fall back to {@link #defaultTheme()}.
     *
     * @param name theme name
     * @return the matching factory
     */
    public static ThemeFactory byName(String name) {
        return ALL.stream().filter(f -> f.name().equalsIgnoreCase(name)).findFirst().orElse(defaultTheme());
    }

    /**
     * Returns the default theme (Space).
     *
     * @return the default factory
     */
    public static ThemeFactory defaultTheme() {
        return ALL.get(0);
    }
}

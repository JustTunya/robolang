package robolang.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;

class ThemeRegistryTest {
    @Test
    void allListsSpaceJungleThenRetro() {
        assertEquals(List.of("Space", "Jungle", "Retro"), ThemeRegistry.all().stream().map(ThemeFactory::name).toList());
    }

    @Test
    void byNameIsCaseInsensitive() {
        assertSame(ThemeRegistry.all().get(1), ThemeRegistry.byName("jUnGlE"));
        assertSame(ThemeRegistry.all().get(0), ThemeRegistry.byName("SPACE"));
    }

    @Test
    void unknownOrNullNameFallsBackToDefault() {
        assertSame(ThemeRegistry.defaultTheme(), ThemeRegistry.byName("nope"));
        assertSame(ThemeRegistry.defaultTheme(), ThemeRegistry.byName(null));
        assertEquals("Space", ThemeRegistry.defaultTheme().name());
    }
}

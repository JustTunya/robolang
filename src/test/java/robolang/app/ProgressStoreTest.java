package robolang.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import robolang.lang.runtime.Outcome;

class ProgressStoreTest {
    @TempDir Path dir;

    private static Outcome out(boolean ok, int stars, int instr, int actions) {
        return new Outcome(ok, null, "", null, 0, instr, actions, 0L, stars);
    }

    @Test
    void bestResultRules() {
        ProgressStore s = new ProgressStore(dir);
        s.record("L1", out(false, 3, 1, 1));
        assertFalse(s.isSolved("L1"));
        s.record("L1", out(true, 2, 5, 9));
        s.record("L1", out(true, 1, 1, 1));
        assertEquals(new BestResult(2, 5, 9), s.best("L1").orElseThrow());
        s.record("L1", out(true, 2, 4, 20));
        assertEquals(new BestResult(2, 4, 20), s.best("L1").orElseThrow());
        s.record("L1", out(true, 2, 4, 10));
        assertEquals(new BestResult(2, 4, 10), s.best("L1").orElseThrow());
        s.record("L1", out(true, 3, 9, 99));
        assertEquals(new BestResult(3, 9, 99), s.best("L1").orElseThrow());
        assertTrue(s.isSolved("L1"));
    }

    @Test
    void roundTripAndProgramText() {
        String text = "move # not a comment\r\nturn\né中文 🚀\n";
        new ProgressStore(dir).record("L1", out(true, 1, 2, 3));
        new ProgressStore(dir).saveProgram("L1", text);
        ProgressStore s = new ProgressStore(dir);
        assertEquals(new BestResult(1, 2, 3), s.best("L1").orElseThrow());
        assertEquals(Optional.of(text), s.lastProgram("L1"));
        assertEquals(Optional.empty(), s.lastProgram("L2"));
    }

    @Test
    void resetAllKeepsSettings() {
        ProgressStore s = new ProgressStore(dir);
        Settings st = new Settings(dir);
        st.setThemeName("Jungle");
        st.setDemoMode(true);
        s.record("L1", out(true, 1, 2, 3));
        s.saveProgram("L1", "x");
        s.resetAll();
        assertFalse(s.isSolved("L1"));
        assertEquals(Optional.empty(), s.lastProgram("L1"));
        Settings again = new Settings(dir);
        assertEquals("Jungle", again.themeName());
        assertTrue(again.demoMode());
    }

    @Test
    void corruptProgressIsEmpty() throws Exception {
        Files.write(dir.resolve("progress.properties"), "\\uzzzz=1".getBytes(StandardCharsets.UTF_8));
        ProgressStore s = new ProgressStore(dir);
        assertEquals(Optional.empty(), s.best("L1"));
        Files.writeString(dir.resolve("progress.properties"), "L1.stars=abc\n", StandardCharsets.UTF_8);
        assertEquals(Optional.empty(), s.best("L1"));
    }

    @Test
    void settingsDefaults() {
        Settings st = new Settings(dir);
        assertEquals("Space", st.themeName());
        assertFalse(st.demoMode());
    }
}

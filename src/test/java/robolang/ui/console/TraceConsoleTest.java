package robolang.ui.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraceConsoleTest {
    private final TraceConsole console = new TraceConsole();

    @Test
    void startsCollapsedAndToggles() {
        List<Boolean> seen = new ArrayList<>();
        console.onToggle(seen::add);
        assertFalse(console.isExpanded());
        console.setExpanded(true);
        assertTrue(console.isExpanded());
        assertEquals(List.of(true), seen);
    }

    @Test
    void keepsOnlyTheNewestLines() {
        for (int i = 0; i < TraceConsole.MAX_LINES + 50; i++) {
            console.log("line " + i);
        }
        String[] lines = console.text().split("\n");
        assertEquals(TraceConsole.MAX_LINES, lines.length);
        assertEquals("line " + (TraceConsole.MAX_LINES + 49), lines[lines.length - 1]);
        assertEquals("line 50", lines[0]);
    }
}

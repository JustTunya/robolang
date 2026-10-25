package robolang.ui.highlight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import robolang.ui.highlight.SyntaxRuns.Run;
import robolang.ui.highlight.SyntaxRuns.Style;

class SyntaxRunsTest {

    @Test
    void mapsEveryTokenKindToAStyle() {
        String src = "DEF go { move }\nREPEAT 12 { WHILE NOT AT_GOAL { CALL go } } # done";
        List<Run> runs = SyntaxRuns.of(src);

        assertEquals(new Run(0, 3, Style.KEYWORD), runs.get(0));
        assertEquals(new Run(4, 2, Style.IDENTIFIER), runs.get(1));
        assertEquals(new Run(9, 4, Style.KEYWORD), runs.get(2), "keywords are case-insensitive");
        assertTrue(runs.contains(new Run(16 + 7, 2, Style.NUMBER)));
        assertTrue(runs.contains(new Run(16 + 22, 7, Style.CONDITION)));
        assertEquals(new Run(src.indexOf('#'), "# done".length(), Style.COMMENT), runs.get(runs.size() - 1));
    }

    @Test
    void runsAreOrderedAndDoNotOverlap() {
        List<Run> runs = SyntaxRuns.of("# top\nMOVE # trailing\n\nTURN LEFT");
        for (int i = 1; i < runs.size(); i++) {
            assertTrue(runs.get(i - 1).start() + runs.get(i - 1).length() <= runs.get(i).start(), runs.toString());
        }
        assertEquals(Style.COMMENT, runs.get(0).style());
        assertEquals(2, runs.stream().filter(r -> r.style() == Style.COMMENT).count());
        assertEquals(3, runs.stream().filter(r -> r.style() == Style.KEYWORD).count(), "MOVE, TURN, LEFT");
    }

    @Test
    void lexerErrorLeavesTheTextUnstyled() {
        assertTrue(SyntaxRuns.of("MOVE $ MOVE").isEmpty());
    }

    @Test
    void emptyTextHasNoRuns() {
        assertTrue(SyntaxRuns.of("").isEmpty());
    }
}

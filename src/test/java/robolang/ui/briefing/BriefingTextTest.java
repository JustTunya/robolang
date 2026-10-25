package robolang.ui.briefing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import robolang.model.Level;

class BriefingTextTest {

    @Test
    void containsBriefingKeywordsAndBothPars() {
        Level level = new Level("06-x", "Unknown Corridor", "Walk to the end.",
                Set.of("WHILE", "MOVE", "WALL_AHEAD", "NOT"), 2, 14, List.of(), null);

        String text = BriefingText.of(level);

        assertTrue(text.contains("Walk to the end."));
        assertTrue(text.contains("Keywords: MOVE, WHILE, NOT, WALL_AHEAD"), text);
        assertTrue(text.contains("2 instructions"), text);
        assertTrue(text.contains("14 actions"), text);
    }
}

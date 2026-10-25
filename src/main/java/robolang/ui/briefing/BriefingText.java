package robolang.ui.briefing;

import java.util.Comparator;
import robolang.lang.lexer.TokenType;
import robolang.model.Level;

/** Builds the text of the level briefing (FR-04). */
public final class BriefingText {
    private BriefingText() {
    }

    /**
     * Describes a level: the briefing, the keywords it allows and both pars.
     *
     * @param level the level
     * @return multi-line plain text
     */
    public static String of(Level level) {
        String keywords = String.join(", ", level.keywords().stream()
                .sorted(Comparator.comparingInt(k -> TokenType.valueOf(k).ordinal()))
                .toList());
        return level.briefing() + "\n\n"
                + "Keywords: " + keywords + "\n"
                + "Par: " + level.parInstructions() + " instructions (2nd star), "
                + level.parActions() + " actions (3rd star)";
    }
}

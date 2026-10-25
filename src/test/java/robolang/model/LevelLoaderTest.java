package robolang.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import robolang.lang.lexer.TokenType;

class LevelLoaderTest {
    private static final String HEADER = "title: T\nbriefing: B\nkeywords: move turn\npar-instructions: 2\n"
            + "par-actions: 14\n";

    private static void assertFails(String text, int line, String messagePart) {
        LevelFormatException e = assertThrows(LevelFormatException.class, () -> LevelLoader.parse("05-x", text));
        assertEquals("05-x", e.fileId());
        assertEquals(line, e.line(), e.getMessage());
        assertTrue(e.getMessage().contains(messagePart), e.getMessage());
    }

    @Test
    void parsesSingleVariant() {
        Level l = LevelLoader.parse("a", HEADER + "grid:\n#####\n#>*.G\n#####\n");
        assertEquals("T", l.title());
        assertEquals("B", l.briefing());
        assertEquals(Set.of("MOVE", "TURN"), l.keywords());
        assertEquals(2, l.parInstructions());
        assertEquals(14, l.parActions());
        assertEquals(1, l.variants().size());
        Variant v = l.variants().get(0);
        assertEquals(new Position(1, 1), v.start());
        assertEquals(Direction.EAST, v.facing());
        assertEquals(new Position(1, 4), v.grid().goal());
        assertEquals(Set.of(new Position(1, 2)), v.grid().initialItems());
        assertEquals(CellType.FLOOR, v.grid().cellAt(v.start()));
    }

    @Test
    void parsesMultipleVariantsAndAllFacings() {
        Level l = LevelLoader.parse("a", HEADER + "grid:\n###\n#^G\n###\n\ngrid:\n###\n#vG\n###\ngrid:\n###\n<G#\n###");
        assertEquals(3, l.variants().size());
        assertEquals(Direction.NORTH, l.variants().get(0).facing());
        assertEquals(Direction.SOUTH, l.variants().get(1).facing());
        assertEquals(Direction.WEST, l.variants().get(2).facing());
    }

    @Test
    void rejectsUnequalRowLength() {
        assertFails(HEADER + "grid:\n#####\n#>.G\n#####\n", 8, "row length 4, expected 5");
    }

    @Test
    void rejectsSizeOutOfRange() {
        assertFails(HEADER + "grid:\n##\n>G\n##\n", 6, "3x3 and 12x12");
        String wide = "#".repeat(13);
        assertFails(HEADER + "grid:\n" + wide + "\n#>" + ".".repeat(9) + "G#\n" + wide + "\n", 6, "3x13");
    }

    @Test
    void rejectsWrongRobotOrGoalCount() {
        assertFails(HEADER + "grid:\n####\n#.G#\n####\n", 6, "exactly one robot, found 0");
        assertFails(HEADER + "grid:\n#####\n#>>G#\n#####\n", 6, "exactly one robot, found 2");
        assertFails(HEADER + "grid:\n####\n#>.#\n####\n", 6, "exactly one goal, found 0");
        assertFails(HEADER + "grid:\n#####\n#>GG#\n#####\n", 6, "exactly one goal, found 2");
    }

    @Test
    void rejectsInvalidSymbolAndStrayLine() {
        assertFails(HEADER + "grid:\n####\n#>X#\n####\n", 8, "invalid symbol 'X'");
        assertFails(HEADER + "grid:\n####\n#>G#\n####\n\nextra\n", 11, "unexpected line");
    }

    @Test
    void rejectsHeaderProblems() {
        assertFails("bogus: 1\n" + HEADER + "grid:\n###\n>G#\n###\n", 1, "unknown header key 'bogus'");
        assertFails(HEADER.replace("title: T\n", "") + "grid:\n###\n>G#\n###\n", 1, "missing header key 'title'");
        assertFails(HEADER.replace("move turn", "move fly") + "grid:\n###\n>G#\n###\n", 1, "invalid keyword 'fly'");
        assertFails(HEADER.replace("par-actions: 14", "par-actions: 0") + "grid:\n###\n>G#\n###\n", 1, "par-actions");
        assertFails(HEADER.replace("par-instructions: 2", "par-instructions: x") + "grid:\n###\n>G#\n###\n", 1,
                "par-instructions");
        assertFails(HEADER, 6, "no grid");
    }

    @Test
    void keywordConstantMatchesTokenType() {
        Set<String> lexer = Arrays.stream(TokenType.values()).filter(TokenType::isKeyword).map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(lexer, LevelLoader.KEYWORDS);
    }

    @Test
    void loadAllReturnsShippedCampaign() {
        List<Level> levels = LevelLoader.loadAll();
        assertEquals("01-first-steps", levels.get(0).id());
        assertEquals("MOVE\nMOVE\nMOVE\nMOVE\nMOVE", levels.get(0).referenceSolution().strip().replace("\r", ""));
    }

    @Test
    void loadAllSkipsBrokenAndMissingLevels() {
        List<Level> levels = LevelLoader.loadAll("levels-test");
        assertEquals(1, levels.size());
        assertEquals("good", levels.get(0).id());
        assertEquals("MOVE", levels.get(0).referenceSolution().strip());
    }
}

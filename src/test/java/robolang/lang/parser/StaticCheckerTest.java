package robolang.lang.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.CallStmt;
import robolang.lang.ast.Program;
import robolang.lang.error.SemanticException;

class StaticCheckerTest {
    private static final Set<String> ALL = Set.of("MOVE", "TURN", "PICK", "REPEAT", "WHILE", "IF", "ELSE", "DEF",
            "CALL", "NOT", "AND", "OR", "WALL_AHEAD", "ON_ITEM", "AT_GOAL");

    private static Program check(String src, Set<String> allowed) {
        Program p = Parser.parse(src);
        StaticChecker.check(p, allowed);
        return p;
    }

    private static void fails(String src, Set<String> allowed, String formatted) {
        assertEquals(formatted, assertThrows(SemanticException.class, () -> check(src, allowed)).formatted());
    }

    private static Set<String> without(String... kw) {
        Set<String> s = new HashSet<>(ALL);
        s.removeAll(Set.of(kw));
        return s;
    }

    @Test
    void duplicateProcedure() {
        fails("DEF a { }\nDEF a { }", ALL, "2:1 Duplicate procedure 'a'");
    }

    @Test
    void procedureNamesAreCaseSensitive() {
        assertDoesNotThrow(() -> check("DEF a { } DEF A { }", ALL));
    }

    @Test
    void unknownProcedure() {
        fails("MOVE\n  CALL zigzag", ALL, "2:3 Unknown procedure 'zigzag'");
    }

    @Test
    void twentyProceduresOkTwentyOneRejected() {
        String defs = IntStream.range(0, 20).mapToObj(i -> "DEF p" + i + " { }\n").collect(Collectors.joining());
        assertDoesNotThrow(() -> check(defs, ALL));
        fails(defs + "DEF p20 { }", ALL, "21:1 Too many procedures (maximum 20)");
    }

    @Test
    void forwardAndBackwardCallsAreBound() {
        Program p = check("CALL a DEF a { MOVE } CALL a DEF b { CALL a CALL b }", ALL);
        CallStmt before = (CallStmt) p.main().statements().get(0);
        CallStmt after = (CallStmt) p.main().statements().get(1);
        assertSame(p.procedures().get(0), before.target());
        assertSame(p.procedures().get(0), after.target());
        CallStmt inside = (CallStmt) p.procedures().get(1).body().statements().get(1);
        assertSame(p.procedures().get(1), inside.target());
    }

    @Test
    void recursionIsAllowed() {
        assertDoesNotThrow(() -> check("DEF a { CALL a }", ALL));
    }

    @Test
    void checkingTwiceIsHarmless() {
        Program p = check("DEF a { } CALL a", ALL);
        assertDoesNotThrow(() -> StaticChecker.check(p, ALL));
    }

    @Test
    void eachDisallowedKeyword() {
        String[][] cases = {
            {"MOVE", "MOVE"}, {"TURN", "TURN LEFT"}, {"PICK", "PICK"}, {"REPEAT", "REPEAT 2 { }"},
            {"WHILE", "WHILE ON_ITEM { }"}, {"IF", "IF ON_ITEM { }"}, {"DEF", "DEF a { }"},
            {"CALL", "DEF a { } CALL a"}, {"NOT", "IF NOT ON_ITEM { }"}, {"AND", "IF ON_ITEM AND ON_ITEM { }"},
            {"OR", "IF ON_ITEM OR ON_ITEM { }"}, {"WALL_AHEAD", "IF WALL_AHEAD { }"},
            {"ON_ITEM", "IF ON_ITEM { }"}, {"AT_GOAL", "IF AT_GOAL { }"},
        };
        for (String[] c : cases) {
            Set<String> allowed = without(c[0]);
            SemanticException e = assertThrows(SemanticException.class, () -> check(c[1], allowed), c[0]);
            assertEquals(c[0] + " is not available in this level", e.getMessage());
        }
    }

    @Test
    void elseNeedsItsOwnAllowance() {
        fails("IF ON_ITEM { }\nELSE { }", without("ELSE"), "1:1 ELSE is not available in this level");
        assertDoesNotThrow(() -> check("IF ON_ITEM { }", without("ELSE")));
    }

    @Test
    void reportsPositionOfOffendingNode() {
        fails("MOVE\nREPEAT 3 {\n  PICK\n}", without("PICK"), "3:3 PICK is not available in this level");
        fails("WHILE NOT\n WALL_AHEAD { }", without("WALL_AHEAD"), "2:2 WALL_AHEAD is not available in this level");
    }

    @Test
    void firstInSourceOrderWins() {
        fails("PICK\nDEF a { MOVE }", without("PICK", "MOVE"), "1:1 PICK is not available in this level");
        fails("DEF a { MOVE }\nPICK", without("PICK", "MOVE"), "1:9 MOVE is not available in this level");
        fails("IF ON_ITEM AND WALL_AHEAD { }", without("AND", "WALL_AHEAD"),
                "1:4 AND is not available in this level");
    }

    @Test
    void appendixASamplesParseAndCheckWithAllKeywords() {
        String[] samples = {
            "REPEAT 9 { MOVE }",
            "REPEAT 3 {\n REPEAT 2 { MOVE }\n TURN LEFT\n REPEAT 2 { MOVE }\n TURN RIGHT\n}",
            "WHILE NOT WALL_AHEAD {\n IF ON_ITEM { PICK }\n MOVE\n}",
            "DEF step {\n TURN RIGHT\n WHILE WALL_AHEAD { TURN LEFT }\n MOVE\n}\n"
                    + "WHILE NOT AT_GOAL {\n IF ON_ITEM { PICK }\n CALL step\n}",
            "REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }",
        };
        for (String s : samples) {
            assertDoesNotThrow(() -> check(s, ALL), s);
        }
    }
}

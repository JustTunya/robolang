package robolang.ui.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import robolang.lang.parser.Parser;

class AstTreeBuilderTest {
    static String dump(TreeItem item, String indent, StringBuilder sb) {
        sb.append(indent).append(item.label()).append('\n');
        item.children().forEach(c -> dump(c, indent + "  ", sb));
        return sb.toString();
    }

    private static String tree(String src) {
        return dump(AstTreeBuilder.build(Parser.parse(src)), "", new StringBuilder());
    }

    @Test
    void appendixA3() {
        assertEquals("""
                Program
                  Main
                    REPEAT 2
                      MOVE
                      IF WALL_AHEAD
                        condition
                          WALL_AHEAD
                        then
                          TURN LEFT
                """, tree("REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }"));
    }

    @Test
    void procedures() {
        assertEquals("""
                Program
                  Procedures
                    DEF go
                      MOVE
                      PICK
                  Main
                    CALL go
                """, tree("DEF go { MOVE PICK } CALL go"));
    }

    @Test
    void elseIfChainWhileAndNestedConditions() {
        assertEquals("""
                Program
                  Main
                    IF WALL_AHEAD
                      condition
                        WALL_AHEAD
                      then
                        MOVE
                      else
                        IF AT_GOAL
                          condition
                            AT_GOAL
                          then
                            PICK
                          else
                            TURN RIGHT
                    WHILE NOT (WALL_AHEAD OR ON_ITEM) AND AT_GOAL
                      condition
                        NOT (WALL_AHEAD OR ON_ITEM) AND AT_GOAL
                          NOT (WALL_AHEAD OR ON_ITEM)
                            WALL_AHEAD OR ON_ITEM
                              WALL_AHEAD
                              ON_ITEM
                          AT_GOAL
                      body
                        MOVE
                """, tree("""
                IF WALL_AHEAD { MOVE } ELSE IF AT_GOAL { PICK } ELSE { TURN RIGHT }
                WHILE NOT (WALL_AHEAD OR ON_ITEM) AND AT_GOAL { MOVE }
                """));
    }
}

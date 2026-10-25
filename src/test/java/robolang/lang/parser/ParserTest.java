package robolang.lang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import robolang.lang.ast.AndCond;
import robolang.lang.ast.AtGoalCond;
import robolang.lang.ast.BlockStmt;
import robolang.lang.ast.BoolExpression;
import robolang.lang.ast.CallStmt;
import robolang.lang.ast.IfStmt;
import robolang.lang.ast.MoveStmt;
import robolang.lang.ast.NotCond;
import robolang.lang.ast.OnItemCond;
import robolang.lang.ast.OrCond;
import robolang.lang.ast.PickStmt;
import robolang.lang.ast.Program;
import robolang.lang.ast.RepeatStmt;
import robolang.lang.ast.SourceRange;
import robolang.lang.ast.TurnDirection;
import robolang.lang.ast.TurnStmt;
import robolang.lang.ast.WallAheadCond;
import robolang.lang.ast.WhileStmt;
import robolang.lang.error.LexException;
import robolang.lang.error.ParseException;
import robolang.lang.error.RoboLangException;
import robolang.lang.error.SemanticException;

class ParserTest {
    private static Program parse(String s) {
        return Parser.parse(s);
    }

    private static <T> T first(Program p, Class<T> type) {
        return assertInstanceOf(type, p.main().statements().get(0));
    }

    private static BoolExpression cond(String c) {
        return first(parse("WHILE " + c + " { }"), WhileStmt.class).condition();
    }

    private static void fails(Class<? extends RoboLangException> type, String src, String formatted) {
        assertEquals(formatted, assertThrows(type, () -> parse(src)).formatted());
    }

    @Test
    void emptyProgramAndEmptyBlock() {
        Program p = parse("");
        assertTrue(p.procedures().isEmpty());
        assertTrue(p.main().statements().isEmpty());
        assertTrue(first(parse("REPEAT 2 { }"), RepeatStmt.class).body().statements().isEmpty());
    }

    @Test
    void simpleStatements() {
        Program p = parse("MOVE TURN left TURN RIGHT PICK CALL go");
        assertInstanceOf(MoveStmt.class, p.main().statements().get(0));
        assertEquals(TurnDirection.LEFT, assertInstanceOf(TurnStmt.class, p.main().statements().get(1)).direction());
        assertEquals(TurnDirection.RIGHT, assertInstanceOf(TurnStmt.class, p.main().statements().get(2)).direction());
        assertInstanceOf(PickStmt.class, p.main().statements().get(3));
        assertEquals("go", assertInstanceOf(CallStmt.class, p.main().statements().get(4)).name());
    }

    @Test
    void nestedRepeat() {
        RepeatStmt outer = first(parse("REPEAT 3 { REPEAT 2 { MOVE } TURN LEFT }"), RepeatStmt.class);
        assertEquals(3, outer.count());
        assertEquals(2, assertInstanceOf(RepeatStmt.class, outer.body().statements().get(0)).count());
        assertInstanceOf(TurnStmt.class, outer.body().statements().get(1));
    }

    @Test
    void proceduresGoToProgramProceduresInSourceOrder() {
        Program p = parse("MOVE DEF a { MOVE } CALL a DEF b { } CALL b");
        assertEquals(2, p.procedures().size());
        assertEquals("a", p.procedures().get(0).name());
        assertEquals("b", p.procedures().get(1).name());
        assertEquals(3, p.main().statements().size());
    }

    @Test
    void procedureUsedBeforeAndAfterDefinitionParses() {
        assertEquals(2, parse("CALL a DEF a { } CALL a").main().statements().size());
    }

    @Test
    void precedenceNotAndOr() {
        // NOT a AND b OR c  ==  ((NOT a) AND b) OR c
        OrCond or = assertInstanceOf(OrCond.class, cond("NOT WALL_AHEAD AND ON_ITEM OR AT_GOAL"));
        AndCond and = assertInstanceOf(AndCond.class, or.left());
        NotCond not = assertInstanceOf(NotCond.class, and.left());
        assertInstanceOf(WallAheadCond.class, not.operand());
        assertInstanceOf(OnItemCond.class, and.right());
        assertInstanceOf(AtGoalCond.class, or.right());
    }

    @Test
    void andBindsTighterThanOrOnTheRight() {
        OrCond or = assertInstanceOf(OrCond.class, cond("WALL_AHEAD OR ON_ITEM AND AT_GOAL"));
        assertInstanceOf(WallAheadCond.class, or.left());
        assertInstanceOf(AndCond.class, or.right());
    }

    @Test
    void andOrAreLeftAssociative() {
        AndCond and = assertInstanceOf(AndCond.class, cond("WALL_AHEAD AND ON_ITEM AND AT_GOAL"));
        assertInstanceOf(AndCond.class, and.left());
        assertInstanceOf(AtGoalCond.class, and.right());
        OrCond or = assertInstanceOf(OrCond.class, cond("WALL_AHEAD OR ON_ITEM OR AT_GOAL"));
        assertInstanceOf(OrCond.class, or.left());
    }

    @Test
    void parenthesesOverridePrecedence() {
        AndCond and = assertInstanceOf(AndCond.class, cond("(WALL_AHEAD OR ON_ITEM) AND AT_GOAL"));
        assertInstanceOf(OrCond.class, and.left());
        NotCond not = assertInstanceOf(NotCond.class, cond("NOT (WALL_AHEAD OR ON_ITEM)"));
        assertInstanceOf(OrCond.class, not.operand());
        assertInstanceOf(NotCond.class, assertInstanceOf(NotCond.class, cond("NOT NOT ON_ITEM")).operand());
    }

    @Test
    void ifWithoutElse() {
        IfStmt s = first(parse("IF ON_ITEM { PICK }"), IfStmt.class);
        assertNull(s.elseBranch());
        assertEquals(1, s.thenBlock().statements().size());
    }

    @Test
    void ifElseBlock() {
        IfStmt s = first(parse("IF ON_ITEM { PICK } ELSE { MOVE }"), IfStmt.class);
        assertInstanceOf(BlockStmt.class, s.elseBranch());
    }

    @Test
    void elseIfChainNestsIfStmtInElseBranch() {
        IfStmt a = first(parse("IF WALL_AHEAD { } ELSE IF ON_ITEM { } ELSE IF AT_GOAL { } ELSE { MOVE }"), IfStmt.class);
        IfStmt b = assertInstanceOf(IfStmt.class, a.elseBranch());
        assertInstanceOf(OnItemCond.class, b.condition());
        IfStmt c = assertInstanceOf(IfStmt.class, b.elseBranch());
        assertInstanceOf(BlockStmt.class, c.elseBranch());
    }

    @Test
    void rangesSpanFirstToLastToken() {
        Program p = parse("REPEAT 4 {\n  MOVE\n}");
        RepeatStmt r = first(p, RepeatStmt.class);
        assertEquals(new SourceRange(1, 1, 3, 1), r.range());
        assertEquals(new SourceRange(1, 10, 3, 1), r.body().range());
        assertEquals(new SourceRange(2, 3, 2, 6), r.body().statements().get(0).range());
        assertEquals(new SourceRange(1, 1, 3, 1), p.range());
        BoolExpression c = cond("NOT WALL_AHEAD AND ON_ITEM");
        assertEquals(new SourceRange(1, 7, 1, 32), c.range());
        assertEquals(new SourceRange(1, 1, 1, 8), parse("CALL abc").main().statements().get(0).range());
    }

    @Test
    void repeatCountBounds() {
        assertEquals(1, first(parse("REPEAT 1 { }"), RepeatStmt.class).count());
        assertEquals(999, first(parse("REPEAT 999 { }"), RepeatStmt.class).count());
        assertEquals(7, first(parse("REPEAT 007 { }"), RepeatStmt.class).count());
        fails(SemanticException.class, "MOVE REPEAT 0 { }", "1:13 REPEAT count must be 1–999");
        fails(SemanticException.class, "REPEAT 1000 { }", "1:8 REPEAT count must be 1–999");
        fails(SemanticException.class, "REPEAT 99999999999999999999 { }", "1:8 REPEAT count must be 1–999");
    }

    @Test
    void syntaxErrors() {
        fails(ParseException.class, "REPEAT 4 MOVE", "1:10 Expected '{' after REPEAT 4");
        fails(ParseException.class, "REPEAT { }", "1:8 Expected number after REPEAT");
        fails(ParseException.class, "DEF { }", "1:5 Expected procedure name after DEF");
        fails(ParseException.class, "DEF MOVE { }", "1:5 Expected procedure name after DEF");
        fails(ParseException.class, "DEF go MOVE", "1:8 Expected '{' after DEF go");
        fails(ParseException.class, "MOVE }", "1:6 Unexpected '}'");
        fails(ParseException.class, "\n\nELSE { }", "3:1 Unexpected 'ELSE'");
        fails(ParseException.class, "TURN UP", "1:6 Expected LEFT or RIGHT after TURN");
        fails(ParseException.class, "TURN", "1:5 Expected LEFT or RIGHT after TURN");
        fails(ParseException.class, "CALL 3", "1:6 Expected procedure name after CALL");
        fails(ParseException.class, "WHILE { }", "1:7 Expected condition after WHILE");
        fails(ParseException.class, "IF ON_ITEM PICK", "1:12 Expected '{' after IF condition");
        fails(ParseException.class, "WHILE ON_ITEM MOVE", "1:15 Expected '{' after WHILE condition");
        fails(ParseException.class, "IF ON_ITEM { } ELSE MOVE", "1:21 Expected '{' or IF after ELSE");
        fails(ParseException.class, "IF ON_ITEM AND { }", "1:16 Expected condition after AND");
        fails(ParseException.class, "IF NOT { }", "1:8 Expected condition after NOT");
        fails(ParseException.class, "IF ON_ITEM OR { }", "1:15 Expected condition after OR");
        fails(ParseException.class, "IF (ON_ITEM { }", "1:13 Expected ')'");
        fails(ParseException.class, "IF () { }", "1:5 Expected condition after '('");
        fails(ParseException.class, "REPEAT 2 { MOVE", "1:16 Expected '}' to close block opened at 1:10");
        fails(ParseException.class, "{ }", "1:1 Unexpected '{'");
        fails(ParseException.class, "REPEAT 2 { DEF a { } }", "1:12 Unexpected 'DEF'");
    }

    @Test
    void firstErrorWins() {
        fails(ParseException.class, "MOVE } TURN UP", "1:6 Unexpected '}'");
    }

    @Test
    void lexErrorsSurfaceFromParse() {
        fails(LexException.class, "MOVE ;", "1:6 Unexpected character ';'");
    }

    @Test
    void pathologicalNestingIsAParseErrorNotAStackOverflow() {
        assertThrows(ParseException.class,
                () -> parse("WHILE " + "(".repeat(5000) + "ON_ITEM" + ")".repeat(4000) + " { }"));
        assertThrows(ParseException.class, () -> parse("REPEAT 1 {".repeat(900)));
        assertThrows(ParseException.class, () -> parse("WHILE " + "NOT ".repeat(2000) + "ON_ITEM { }"));
    }

    @Test
    void largeProgramParsesQuickly() {
        String src = "REPEAT 2 { MOVE IF WALL_AHEAD { TURN LEFT } }\n".repeat(200);
        long t0 = System.nanoTime();
        Program p = parse(src);
        assertEquals(200, p.main().statements().size());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 1000);
    }
}

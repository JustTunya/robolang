package robolang.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.AndCond;
import robolang.lang.ast.AstNode;
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
import robolang.lang.ast.ProcDef;
import robolang.lang.ast.Program;
import robolang.lang.ast.RepeatStmt;
import robolang.lang.ast.SourceRange;
import robolang.lang.ast.Statement;
import robolang.lang.ast.TurnDirection;
import robolang.lang.ast.TurnStmt;
import robolang.lang.ast.WallAheadCond;
import robolang.lang.ast.WhileStmt;
import robolang.lang.error.EmptyPickException;
import robolang.lang.error.ExecutionStoppedException;
import robolang.model.CellType;
import robolang.model.Direction;
import robolang.model.Grid;
import robolang.model.Level;
import robolang.model.Position;
import robolang.model.Variant;
import robolang.model.WorldSnapshot;

class RuntimeTest {
    private static final SourceRange R = new SourceRange(1, 1, 1, 5);
    private static final CellType F = CellType.FLOOR;
    private static final CellType W = CellType.WALL;
    private static final CellType G = CellType.GOAL;
    private static final ExecutionListener NONE = new ExecutionListener() { };

    /** Floor at (1,0)..(1,3), goal at (1,4); the robot starts at {@code startCol} facing east. */
    private static Variant corridor(int startCol, Set<Position> items) {
        Grid g = new Grid(new CellType[][] {{W, W, W, W, W}, {F, F, F, F, G}, {W, W, W, W, W}}, items);
        return new Variant(g, new Position(1, startCol), Direction.EAST);
    }

    private static Level level(Variant... variants) {
        return new Level("t", "t", "", Set.of(), 2, 5, List.of(variants), "");
    }

    private static Context ctx(Variant v) {
        return new Context(v.newWorld(), ExecutionLimits.DEFAULT, NONE, 0);
    }

    private static BlockStmt block(Statement... s) {
        return new BlockStmt(List.of(s), R);
    }

    private static MoveStmt move() {
        return new MoveStmt(R);
    }

    private static Program program(Statement... s) {
        return new Program(List.of(), block(s), R);
    }

    private static Outcome run(Program p, Variant... variants) {
        return ProgramRunner.run(p, level(variants), ExecutionLimits.DEFAULT, NONE);
    }

    // ---- nodes ----

    @Test
    void actionsCostOneTickAndOneAction() {
        Context c = ctx(corridor(0, Set.of(new Position(1, 1))));
        move().interpret(c);
        new TurnStmt(TurnDirection.LEFT, R).interpret(c);
        new TurnStmt(TurnDirection.RIGHT, R).interpret(c);
        new PickStmt(R).interpret(c);
        assertEquals(4, c.ticks());
        assertEquals(4, c.actions());
        assertEquals(new Position(1, 1), c.world().robot());
        assertEquals(Direction.EAST, c.world().facing());
        assertEquals(0, c.world().itemsLeft());
    }

    @Test
    void blockIsFreeAndRunsInOrder() {
        Context c = ctx(corridor(0, Set.of()));
        block(move(), move()).interpret(c);
        assertEquals(2, c.ticks());
        assertEquals(new Position(1, 2), c.world().robot());
    }

    @Test
    void repeatCostsOneTickPerIteration() {
        Context c = ctx(corridor(0, Set.of()));
        new RepeatStmt(3, block(move()), R).interpret(c);
        assertEquals(6, c.ticks());
        assertEquals(3, c.actions());
    }

    @Test
    void whileCostsOnePerCheckPlusCondition() {
        Context c = ctx(corridor(0, Set.of()));
        new WhileStmt(new NotCond(new AtGoalCond(R), R), block(move()), R).interpret(c);
        // 5 checks x (WHILE + NOT + AT_GOAL) + 4 moves
        assertEquals(19, c.ticks());
        assertEquals(4, c.actions());
        assertTrue(c.world().atGoal());
    }

    @Test
    void ifEvaluatesOnceAndRunsOneBranch() {
        Context c = ctx(corridor(0, Set.of()));
        new IfStmt(new WallAheadCond(R), block(new TurnStmt(TurnDirection.LEFT, R)), block(move()), R).interpret(c);
        assertEquals(3, c.ticks()); // IF + WALL_AHEAD + MOVE
        assertEquals(new Position(1, 1), c.world().robot());

        Context c2 = ctx(corridor(0, Set.of()));
        new IfStmt(new OnItemCond(R), block(move()), null, R).interpret(c2);
        assertEquals(2, c2.ticks());
        assertEquals(0, c2.actions());
    }

    @Test
    void elseIfChainEvaluatesNestedIf() {
        Context c = ctx(corridor(0, Set.of()));
        IfStmt inner = new IfStmt(new AtGoalCond(R), block(), block(move()), R);
        new IfStmt(new OnItemCond(R), block(), inner, R).interpret(c);
        assertEquals(5, c.ticks()); // IF, ON_ITEM, IF, AT_GOAL, MOVE
        assertEquals(1, c.actions());
    }

    @Test
    void andOrShortCircuitViaTickCounts() {
        Variant v = corridor(0, Set.of());
        Context c = ctx(v);
        assertFalse(new AndCond(new OnItemCond(R), new AtGoalCond(R), R).evaluate(c));
        assertEquals(2, c.ticks()); // AND + ON_ITEM, right skipped

        c = ctx(v);
        assertFalse(new AndCond(new NotCond(new OnItemCond(R), R), new OnItemCond(R), R).evaluate(c));
        assertEquals(4, c.ticks()); // AND + NOT + ON_ITEM + right

        c = ctx(v);
        assertTrue(new OrCond(new NotCond(new OnItemCond(R), R), new AtGoalCond(R), R).evaluate(c));
        assertEquals(3, c.ticks()); // OR + NOT + ON_ITEM, right skipped

        c = ctx(v);
        assertFalse(new OrCond(new OnItemCond(R), new AtGoalCond(R), R).evaluate(c));
        assertEquals(3, c.ticks()); // OR + both
    }

    @Test
    void callRunsTargetBodyAndRestoresDepth() {
        CallStmt call = new CallStmt("go", R);
        call.bind(new ProcDef("go", block(move()), R));
        Context c = new Context(corridor(0, Set.of()).newWorld(), new ExecutionLimits(100, 1), NONE, 0);
        call.interpret(c);
        call.interpret(c); // would exceed depth 1 if the first call had not exited
        assertEquals(4, c.ticks());
        assertEquals(new Position(1, 2), c.world().robot());
    }

    @Test
    void depthIsRestoredWhenBodyFails() {
        CallStmt call = new CallStmt("go", R);
        call.bind(new ProcDef("go", block(new PickStmt(R)), R));
        Context c = new Context(corridor(0, Set.of()).newWorld(), new ExecutionLimits(100, 1), NONE, 0);
        assertThrows(EmptyPickException.class, () -> call.interpret(c));
        assertThrows(EmptyPickException.class, () -> call.interpret(c)); // not RecursionLimitException
    }

    // ---- failures ----

    @Test
    void crash() {
        Outcome o = run(program(new TurnStmt(TurnDirection.LEFT, R), move()), corridor(0, Set.of()));
        assertFalse(o.success());
        assertEquals(FailureKind.CRASH, o.failure());
        assertEquals("Variant 1, robot crashed into a wall at (0, 0)", o.message());
        assertEquals(R, o.location());
        assertEquals(0, o.failedVariant());
        assertEquals(1, o.totalActions());
        assertEquals(0, o.stars());
    }

    @Test
    void emptyPick() {
        Outcome o = run(program(new PickStmt(R)), corridor(2, Set.of()));
        assertEquals(FailureKind.EMPTY_PICK, o.failure());
        assertEquals("Variant 1, nothing to pick up at (1, 2)", o.message());
        assertEquals(0, o.totalActions());
    }

    @Test
    void emptyLoopHitsTickLimit() {
        Program p = program(new WhileStmt(new NotCond(new AtGoalCond(R), R), block(), R));
        Outcome o = run(p, corridor(0, Set.of()));
        assertEquals(FailureKind.TICK_LIMIT, o.failure());
        assertEquals("Variant 1, Tick limit of 100 000 reached, possible infinite loop", o.message());
        assertEquals(100_001, o.totalTicks());
        assertEquals(0, o.totalActions());
    }

    @Test
    void unboundedRecursionHitsDepthLimit() {
        CallStmt call = new CallStmt("f", R);
        ProcDef f = new ProcDef("f", block(call), R);
        call.bind(f);
        Outcome o = run(new Program(List.of(f), block(call), R), corridor(0, Set.of()));
        assertEquals(FailureKind.RECURSION_LIMIT, o.failure());
        assertEquals("Variant 1, Call depth limit of 64 exceeded", o.message());
        assertEquals(65, o.totalTicks());
    }

    @Test
    void incompleteExplainsWhy() {
        Outcome away = run(program(), corridor(0, Set.of()));
        assertEquals(FailureKind.INCOMPLETE, away.failure());
        assertEquals("Variant 1, program ended without reaching the goal", away.message());

        Outcome items = run(program(new RepeatStmt(4, block(move()), R)),
                corridor(0, Set.of(new Position(1, 1), new Position(1, 2))));
        assertEquals(FailureKind.INCOMPLETE, items.failure());
        assertEquals("Variant 1, program ended with 2 item(s) left", items.message());
        assertEquals(4, items.totalActions());
    }

    @Test
    void listenerCanStopTheRun() {
        ExecutionListener stopper = new ExecutionListener() {
            @Override
            public void actionPerformed(AstNode node, WorldSnapshot after) {
                throw new ExecutionStoppedException(node.range());
            }
        };
        Outcome o = ProgramRunner.run(program(move(), move()), level(corridor(0, Set.of())),
                ExecutionLimits.DEFAULT, stopper);
        assertEquals(FailureKind.STOPPED, o.failure());
        assertEquals(R, o.location());
        assertEquals(0, o.failedVariant());
        assertEquals(1, o.totalActions());
    }

    @Test
    void requestStopThrowsAtNextTick() {
        Context c = ctx(corridor(0, Set.of()));
        c.requestStop();
        assertThrows(ExecutionStoppedException.class, () -> move().interpret(c));
        assertEquals(0, c.actions());
    }

    // ---- runner ----

    @Test
    void successAndStars() {
        Outcome o = run(program(new RepeatStmt(4, block(move()), R)), corridor(0, Set.of()));
        assertTrue(o.success());
        assertNull(o.failure());
        assertNull(o.message());
        assertNull(o.location());
        assertEquals(-1, o.failedVariant());
        assertEquals(2, o.instructionCount());
        assertEquals(4, o.totalActions());
        assertEquals(8, o.totalTicks());
        assertEquals(3, o.stars());
    }

    @Test
    void multiVariantRunStopsAtFirstFailure() {
        Program p = program(new RepeatStmt(4, block(move()), R));
        Outcome o = run(p, corridor(0, Set.of()), corridor(1, Set.of()), corridor(0, Set.of()));
        assertEquals(FailureKind.CRASH, o.failure());
        assertEquals(1, o.failedVariant());
        assertEquals("Variant 2, robot crashed into a wall at (1, 5)", o.message());
        assertEquals(4 + 3, o.totalActions()); // all of variant 1, three moves of variant 2, variant 3 never ran
    }

    @Test
    void listenerSeesEventsInOrder() {
        List<String> log = new ArrayList<>();
        ExecutionListener l = new ExecutionListener() {
            @Override
            public void variantStarted(int i, WorldSnapshot s) {
                log.add("variant " + i);
            }

            @Override
            public void nodeEntered(AstNode n) {
                log.add("node " + n.label());
            }

            @Override
            public void actionPerformed(AstNode n, WorldSnapshot s) {
                log.add("action " + s.robot().col());
            }
        };
        ProgramRunner.run(program(new RepeatStmt(2, block(move()), R)), level(corridor(0, Set.of())),
                ExecutionLimits.DEFAULT, l);
        assertEquals(List.of("variant 0", "node REPEAT 2", "node MOVE", "action 1", "node REPEAT 2", "node MOVE",
                "action 2"), log);
    }

    // ---- static ----

    @Test
    void instructionCountAndLabelsForAppendixA3() {
        IfStmt iff = new IfStmt(new WallAheadCond(R), block(new TurnStmt(TurnDirection.LEFT, R)), null, R);
        Program p = program(new RepeatStmt(2, block(move(), iff), R));
        assertEquals(4, p.instructionCount());
        assertEquals("Program", p.label());
        assertEquals("Block", p.main().label());
        assertEquals("REPEAT 2", p.main().statements().get(0).label());
        assertEquals("IF WALL_AHEAD", iff.label());
        assertEquals("TURN LEFT", new TurnStmt(TurnDirection.LEFT, R).label());
        assertEquals("MOVE", move().label());
        assertEquals("PICK", new PickStmt(R).label());
    }

    @Test
    void instructionCountCoversDefCallElseAndConditions() {
        ProcDef def = new ProcDef("zigzag", block(move()), R);
        CallStmt call = new CallStmt("zigzag", R);
        IfStmt chain = new IfStmt(new OnItemCond(R), block(new PickStmt(R)),
                new IfStmt(new AtGoalCond(R), block(move()), block(move()), R), R);
        Program p = new Program(List.of(def), block(call, chain, new WhileStmt(new AtGoalCond(R), block(), R)), R);
        // DEF 1 + MOVE 1; CALL 1; IF 1 + PICK 1 + (IF 1 + MOVE 1 + MOVE 1); WHILE 1
        assertEquals(2 + 1 + 5 + 1, p.instructionCount());
        assertEquals("DEF zigzag", def.label());
        assertEquals("CALL zigzag", call.label());
        assertEquals("WHILE AT_GOAL", new WhileStmt(new AtGoalCond(R), block(), R).label());
        assertEquals(0, new AndCond(new OnItemCond(R), new AtGoalCond(R), R).instructionCount());
    }

    @Test
    void conditionLabelsParenthesizeNestedBinaryOperators() {
        BoolExpression both = new AndCond(new OnItemCond(R), new AtGoalCond(R), R);
        assertEquals("NOT (ON_ITEM AND AT_GOAL)", new NotCond(both, R).label());
        assertEquals("NOT WALL_AHEAD", new NotCond(new WallAheadCond(R), R).label());
        assertEquals("(ON_ITEM AND AT_GOAL) OR WALL_AHEAD", new OrCond(both, new WallAheadCond(R), R).label());
        assertEquals("WHILE NOT WALL_AHEAD",
                new WhileStmt(new NotCond(new WallAheadCond(R), R), block(), R).label());
    }

    @Test
    void scoringTruthTable() {
        Level l = level(corridor(0, Set.of())); // par 2 instructions, 5 actions
        assertEquals(0, Scoring.stars(false, 1, 1, l));
        assertEquals(3, Scoring.stars(true, 2, 5, l));
        assertEquals(2, Scoring.stars(true, 2, 6, l));
        assertEquals(2, Scoring.stars(true, 3, 5, l));
        assertEquals(1, Scoring.stars(true, 3, 6, l));
    }
}

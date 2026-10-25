package robolang.lang.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import robolang.lang.ast.TurnStmt;
import robolang.lang.ast.WallAheadCond;
import robolang.lang.ast.WhileStmt;
import robolang.lang.error.SemanticException;

/** Static rules of SPEC §4.5; links each {@code CALL} to its definition. */
public final class StaticChecker {
    private static final int MAX_PROCEDURES = 20;

    private final Set<String> allowed;
    private final Map<String, ProcDef> procs = new HashMap<>();

    private StaticChecker(Set<String> allowed) {
        this.allowed = allowed;
    }

    /**
     * Checks a program and binds its calls. Nodes are visited in source order, so the first
     * problem in the text is the one reported.
     *
     * @param program the parsed program
     * @param allowedKeywords upper-case keywords the level allows
     * @throws SemanticException if a rule is broken
     */
    public static void check(Program program, Set<String> allowedKeywords) {
        new StaticChecker(allowedKeywords).run(program);
    }

    private void run(Program program) {
        for (ProcDef p : program.procedures()) {
            if (procs.put(p.name(), p) != null) {
                throw new SemanticException("Duplicate procedure '" + p.name() + "'", p.range());
            }
            if (procs.size() > MAX_PROCEDURES) {
                throw new SemanticException("Too many procedures (maximum " + MAX_PROCEDURES + ")", p.range());
            }
        }
        List<AstNode> topLevel = new ArrayList<>(program.procedures());
        topLevel.addAll(program.main().statements());
        topLevel.sort(Comparator.comparingInt((AstNode n) -> n.range().startLine())
                .thenComparingInt(n -> n.range().startCol()));
        for (AstNode n : topLevel) {
            visit(n);
        }
    }

    private void visit(AstNode node) {
        switch (node) {
            case ProcDef d -> {
                use("DEF", d.range());
                visit(d.body());
            }
            case BlockStmt b -> b.statements().forEach(this::visit);
            case MoveStmt s -> use("MOVE", s.range());
            case TurnStmt s -> use("TURN", s.range());
            case PickStmt s -> use("PICK", s.range());
            case RepeatStmt s -> {
                use("REPEAT", s.range());
                visit(s.body());
            }
            case WhileStmt s -> {
                use("WHILE", s.range());
                visit(s.condition());
                visit(s.body());
            }
            case IfStmt s -> {
                use("IF", s.range());
                visit(s.condition());
                visit(s.thenBlock());
                if (s.elseBranch() != null) {
                    use("ELSE", s.range());
                    visit(s.elseBranch());
                }
            }
            case CallStmt s -> {
                use("CALL", s.range());
                ProcDef target = procs.get(s.name());
                if (target == null) {
                    throw new SemanticException("Unknown procedure '" + s.name() + "'", s.range());
                }
                if (s.target() == null) {
                    s.bind(target);
                }
            }
            case NotCond c -> {
                use("NOT", c.range());
                visit(c.operand());
            }
            case AndCond c -> {
                visit(c.left());
                use("AND", c.range());
                visit(c.right());
            }
            case OrCond c -> {
                visit(c.left());
                use("OR", c.range());
                visit(c.right());
            }
            case WallAheadCond c -> use("WALL_AHEAD", c.range());
            case OnItemCond c -> use("ON_ITEM", c.range());
            case AtGoalCond c -> use("AT_GOAL", c.range());
            case Program p -> throw new IllegalArgumentException("nested program");
        }
    }

    private void use(String keyword, SourceRange at) {
        if (!allowed.contains(keyword)) {
            throw new SemanticException(keyword + " is not available in this level", at);
        }
    }
}

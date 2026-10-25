package robolang.ui.ast;

import java.util.ArrayList;
import java.util.List;

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
import robolang.lang.ast.Statement;
import robolang.lang.ast.TurnStmt;
import robolang.lang.ast.WallAheadCond;
import robolang.lang.ast.WhileStmt;

/**
 * Turns a {@link Program} into a {@link TreeItem} structure for display (SPEC §7.2).
 *
 * <p>The exhaustive {@code switch} over the sealed node types is deliberate: FR-30 only forbids it in the runtime.
 * It is the "sealed switch versus Visitor" talking point of SPEC §10.7.
 */
public final class AstTreeBuilder {
    private AstTreeBuilder() {
    }

    /**
     * Builds the tree for a program: {@code Program} with an optional {@code Procedures} row (omitted when there
     * are no procedures) and a {@code Main} row.
     *
     * @param program the parsed program
     * @return the root row
     */
    public static TreeItem build(Program program) {
        List<TreeItem> top = new ArrayList<>();
        if (!program.procedures().isEmpty()) {
            List<TreeItem> procs = new ArrayList<>();
            for (ProcDef p : program.procedures()) {
                procs.add(item(p));
            }
            top.add(new TreeItem("Procedures", null, procs));
        }
        top.add(new TreeItem("Main", program.main(), statements(program.main())));
        return new TreeItem(program.label(), program, top);
    }

    /**
     * Builds the row of one node and its subtree.
     *
     * @param node any AST node
     * @return the row
     */
    public static TreeItem item(AstNode node) {
        List<TreeItem> kids = switch (node) {
            case Program p -> List.of(build(p));
            case ProcDef d -> statements(d.body());
            case BlockStmt b -> statements(b);
            case RepeatStmt r -> statements(r.body());
            case WhileStmt w -> List.of(group("condition", w.condition()), group("body", w.body()));
            case IfStmt i -> ifChildren(i);
            case NotCond n -> List.of(item(n.operand()));
            case AndCond a -> List.of(item(a.left()), item(a.right()));
            case OrCond o -> List.of(item(o.left()), item(o.right()));
            case MoveStmt m -> List.of();
            case TurnStmt t -> List.of();
            case PickStmt p -> List.of();
            case CallStmt c -> List.of();
            case WallAheadCond w -> List.of();
            case OnItemCond o -> List.of();
            case AtGoalCond g -> List.of();
        };
        return new TreeItem(node.label(), node, kids);
    }

    private static List<TreeItem> ifChildren(IfStmt i) {
        List<TreeItem> kids = new ArrayList<>();
        kids.add(group("condition", i.condition()));
        kids.add(group("then", i.thenBlock()));
        if (i.elseBranch() != null) {
            // An ELSE IF chain nests the next IfStmt directly under "else".
            kids.add(new TreeItem("else", null, i.elseBranch() instanceof BlockStmt b
                    ? statements(b) : List.of(item(i.elseBranch()))));
        }
        return kids;
    }

    private static TreeItem group(String label, AstNode child) {
        return new TreeItem(label, null, child instanceof BlockStmt b ? statements(b) : List.of(item(child)));
    }

    private static List<TreeItem> statements(BlockStmt block) {
        List<TreeItem> out = new ArrayList<>();
        for (Statement s : block.statements()) {
            out.add(item(s));
        }
        return out;
    }
}

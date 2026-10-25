package robolang.ui.console;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import robolang.lang.ast.AstNode;
import robolang.lang.ast.Program;
import robolang.ui.ExecutionObserver;
import robolang.ui.ast.AstTreeBuilder;
import robolang.ui.ast.TreeItem;

/**
 * Logs each node the interpreter enters, indented by its depth in the AST (FR-61). The controller coalesces
 * node events under load, so at high speed some nodes are skipped.
 */
public final class InterpretTrace implements ExecutionObserver {
    private final Consumer<String> log;
    private final BooleanSupplier enabled;
    private final Map<AstNode, Integer> depths = new IdentityHashMap<>();

    /**
     * Creates the trace.
     *
     * @param log receives one line per node
     * @param enabled tells whether trace mode is on
     */
    public InterpretTrace(Consumer<String> log, BooleanSupplier enabled) {
        this.log = log;
        this.enabled = enabled;
    }

    @Override
    public void programParsed(Program program) {
        depths.clear();
        collect(AstTreeBuilder.build(program), 0);
        if (enabled.getAsBoolean()) {
            log.accept("--- run ---");
        }
    }

    @Override
    public void nodeEntered(AstNode node) {
        if (enabled.getAsBoolean()) {
            log.accept("  ".repeat(depths.getOrDefault(node, 0)) + node.label());
        }
    }

    // Grouping rows of the tree panel (condition, body, ...) have no node and add no depth.
    private void collect(TreeItem item, int depth) {
        int next = depth;
        if (item.node() != null) {
            depths.putIfAbsent(item.node(), depth);
            next = depth + 1;
        }
        for (TreeItem child : item.children()) {
            collect(child, next);
        }
    }
}

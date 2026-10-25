package robolang.lang.runtime;

import robolang.lang.ast.AstNode;
import robolang.model.WorldSnapshot;

/** Observes execution (D3). Implementations may block; that is how the UI paces a run. */
public interface ExecutionListener {

    /**
     * A variant is about to run.
     *
     * @param variantIndex zero-based variant index
     * @param initial the start state
     */
    default void variantStarted(int variantIndex, WorldSnapshot initial) {
    }

    /**
     * A node is being entered.
     *
     * @param node the node
     */
    default void nodeEntered(AstNode node) {
    }

    /**
     * A move, turn or pick has been performed.
     *
     * @param node the node that acted
     * @param after the world after the action
     */
    default void actionPerformed(AstNode node, WorldSnapshot after) {
    }
}

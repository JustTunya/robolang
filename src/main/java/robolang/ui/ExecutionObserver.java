package robolang.ui;

import robolang.lang.ast.AstNode;
import robolang.lang.ast.Program;
import robolang.lang.runtime.Outcome;

/** Receives execution events. All methods are called on the event dispatch thread and do nothing by default. */
public interface ExecutionObserver {

    /**
     * Called after a program has been parsed successfully.
     *
     * @param program the parsed program
     */
    default void programParsed(Program program) {
    }

    /**
     * Called when the interpreter enters a node.
     *
     * @param node the node being executed
     */
    default void nodeEntered(AstNode node) {
    }

    /**
     * Called when a run has ended.
     *
     * @param outcome the result of the run
     */
    default void runFinished(Outcome outcome) {
    }

    /** Called when the world is reset and the UI returns to editing. */
    default void reset() {
    }
}

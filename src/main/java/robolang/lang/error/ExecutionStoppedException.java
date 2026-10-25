package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Thrown by the context when the player presses Stop (SPEC §3.4, §10.5).
 */
public final class ExecutionStoppedException extends RoboLangException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param range location of the node being executed, may be null
     */
    public ExecutionStoppedException(SourceRange range) {
        super("Execution stopped", range);
    }
}

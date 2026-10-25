package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * The robot tried to pick up an item where there is none (SPEC §3.4).
 */
public final class EmptyPickException extends ExecutionFailure {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location of the failing node, may be null
     * @param variantIndex zero-based index of the variant that failed
     */
    public EmptyPickException(String message, SourceRange range, int variantIndex) {
        super(message, range, variantIndex);
    }
}

package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * The procedure call depth exceeded its limit (SPEC §3.4).
 */
public final class RecursionLimitException extends ExecutionFailure {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location of the failing node, may be null
     * @param variantIndex zero-based index of the variant that failed
     */
    public RecursionLimitException(String message, SourceRange range, int variantIndex) {
        super(message, range, variantIndex);
    }
}

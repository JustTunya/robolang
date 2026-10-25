package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * More than the allowed number of ticks ran in one variant (SPEC §3.4).
 */
public final class TickLimitException extends ExecutionFailure {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location of the failing node, may be null
     * @param variantIndex zero-based index of the variant that failed
     */
    public TickLimitException(String message, SourceRange range, int variantIndex) {
        super(message, range, variantIndex);
    }
}

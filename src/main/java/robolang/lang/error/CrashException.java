package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * The robot moved into a wall or off the grid (SPEC §3.4).
 */
public final class CrashException extends ExecutionFailure {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location of the failing node, may be null
     * @param variantIndex zero-based index of the variant that failed
     */
    public CrashException(String message, SourceRange range, int variantIndex) {
        super(message, range, variantIndex);
    }
}

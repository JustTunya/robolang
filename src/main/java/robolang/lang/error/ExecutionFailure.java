package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Base class of failures that end a variant during execution (SPEC §11).
 */
public abstract class ExecutionFailure extends RoboLangException {
    private static final long serialVersionUID = 1L;

    private final int variantIndex;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location of the failing node, may be null
     * @param variantIndex zero-based index of the variant that failed
     */
    protected ExecutionFailure(String message, SourceRange range, int variantIndex) {
        super(message, range);
        this.variantIndex = variantIndex;
    }

    /**
     * Returns the variant in which the failure happened.
     *
     * @return zero-based variant index
     */
    public int variantIndex() {
        return variantIndex;
    }
}

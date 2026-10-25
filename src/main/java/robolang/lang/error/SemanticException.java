package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Semantic error found by the static checker (SPEC §11).
 */
public final class SemanticException extends RoboLangException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location, may be null
     */
    public SemanticException(String message, SourceRange range) {
        super(message, range);
    }
}

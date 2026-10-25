package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Lexical error found by the lexer (SPEC §11).
 */
public final class LexException extends RoboLangException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location, may be null
     */
    public LexException(String message, SourceRange range) {
        super(message, range);
    }
}

package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Syntax error found by the parser (SPEC §11).
 */
public final class ParseException extends RoboLangException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location, may be null
     */
    public ParseException(String message, SourceRange range) {
        super(message, range);
    }
}

package robolang.lang.error;

import robolang.lang.ast.SourceRange;

/**
 * Base class of every RoboLang error (SPEC §11). Unchecked.
 */
public abstract class RoboLangException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final transient SourceRange range;

    /**
     * Creates the exception.
     *
     * @param message error message
     * @param range location, may be null
     */
    protected RoboLangException(String message, SourceRange range) {
        super(message);
        this.range = range;
    }

    /**
     * Returns where the error happened.
     *
     * @return the source range, or null when unknown
     */
    public SourceRange range() {
        return range;
    }

    /**
     * Formats the error for display.
     *
     * @return {@code "<line>:<col> <message>"}, or just the message when there is no range
     */
    public String formatted() {
        return range == null ? getMessage() : range.startLine() + ":" + range.startCol() + " " + getMessage();
    }
}

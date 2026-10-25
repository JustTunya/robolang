package robolang.model;

/** A level file is malformed. Carries the file id and the line number. */
public class LevelFormatException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String fileId;
    private final int line;

    /**
     * Creates the exception.
     *
     * @param fileId id of the offending level file
     * @param line 1-based line number
     * @param message what is wrong
     */
    public LevelFormatException(String fileId, int line, String message) {
        super(fileId + ":" + line + " " + message);
        this.fileId = fileId;
        this.line = line;
    }

    /**
     * Returns the id of the offending level file.
     *
     * @return file id
     */
    public String fileId() {
        return fileId;
    }

    /**
     * Returns the line of the problem.
     *
     * @return 1-based line number
     */
    public int line() {
        return line;
    }
}

package robolang.model;

import java.util.List;

/** Reads level files (SPEC §5.1). */
public final class LevelLoader {
    private LevelLoader() {
    }

    /**
     * Parses one level file. Implemented by T01.
     *
     * @param id file id
     * @param text file contents
     * @return the level
     * @throws LevelFormatException if the text is malformed
     */
    public static Level parse(String id, String text) {
        throw new UnsupportedOperationException("T01");
    }

    /**
     * Loads every level of the campaign, in order. Implemented by T01.
     *
     * @return all levels
     */
    public static List<Level> loadAll() {
        throw new UnsupportedOperationException("T01");
    }
}

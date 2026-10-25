package robolang.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads level files (SPEC §5.1). */
public final class LevelLoader {
    /** The RoboLang keywords of SPEC §4.1; a level may only list these. */
    public static final Set<String> KEYWORDS = Set.of("MOVE", "TURN", "LEFT", "RIGHT", "PICK", "REPEAT", "WHILE",
            "IF", "ELSE", "DEF", "CALL", "NOT", "AND", "OR", "WALL_AHEAD", "ON_ITEM", "AT_GOAL");

    private static final System.Logger LOG = System.getLogger(LevelLoader.class.getName());
    private static final Set<String> HEADER_KEYS = Set.of("title", "briefing", "keywords", "par-instructions",
            "par-actions");
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 12;

    private LevelLoader() {
    }

    /**
     * Parses one level file.
     *
     * @param id file id
     * @param text file contents
     * @return the level
     * @throws LevelFormatException if the text is malformed
     */
    public static Level parse(String id, String text) {
        String[] lines = text.split("\\R", -1);
        Map<String, String> header = new HashMap<>();
        List<Variant> variants = new ArrayList<>();
        int i = 0;
        for (; i < lines.length && !lines[i].strip().equals("grid:"); i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon < 0) {
                throw new LevelFormatException(id, i + 1, "expected 'key: value'");
            }
            String key = line.substring(0, colon).strip();
            if (!HEADER_KEYS.contains(key)) {
                throw new LevelFormatException(id, i + 1, "unknown header key '" + key + "'");
            }
            if (header.put(key, line.substring(colon + 1).strip()) != null) {
                throw new LevelFormatException(id, i + 1, "duplicate header key '" + key + "'");
            }
        }
        for (String key : HEADER_KEYS) {
            if (!header.containsKey(key)) {
                throw new LevelFormatException(id, 1, "missing header key '" + key + "'");
            }
        }
        while (i < lines.length) {
            int gridLine = i + 1;
            List<String> rows = new ArrayList<>();
            boolean ended = false;
            for (i++; i < lines.length && !lines[i].strip().equals("grid:"); i++) {
                if (lines[i].isBlank()) {
                    ended = true;
                } else if (ended) {
                    throw new LevelFormatException(id, i + 1, "unexpected line after the end of the grid");
                } else {
                    rows.add(lines[i]);
                }
            }
            variants.add(parseGrid(id, gridLine, rows));
        }
        if (variants.isEmpty()) {
            throw new LevelFormatException(id, lines.length, "no grid: block");
        }
        Set<String> keywords = new LinkedHashSet<>();
        for (String k : header.get("keywords").split("\\s+")) {
            if (k.isEmpty()) {
                continue;
            }
            String upper = k.toUpperCase();
            if (!KEYWORDS.contains(upper)) {
                throw new LevelFormatException(id, 1, "invalid keyword '" + k + "'");
            }
            keywords.add(upper);
        }
        if (keywords.isEmpty()) {
            throw new LevelFormatException(id, 1, "keywords must not be empty");
        }
        return new Level(id, header.get("title"), header.get("briefing"), keywords,
                par(id, header, "par-instructions"), par(id, header, "par-actions"), variants, null);
    }

    private static int par(String id, Map<String, String> header, String key) {
        try {
            int v = Integer.parseInt(header.get(key));
            if (v > 0) {
                return v;
            }
        } catch (NumberFormatException e) {
            // falls through to the error below
        }
        throw new LevelFormatException(id, 1, key + " must be a positive integer, got '" + header.get(key) + "'");
    }

    /** Parses the rows of one grid block; the rows start on the line after {@code gridLine}. */
    private static Variant parseGrid(String id, int gridLine, List<String> rows) {
        if (rows.isEmpty()) {
            throw new LevelFormatException(id, gridLine, "empty grid");
        }
        int width = rows.get(0).length();
        CellType[][] cells = new CellType[rows.size()][width];
        Set<Position> items = new HashSet<>();
        Position start = null;
        Direction facing = null;
        int robots = 0;
        int goals = 0;
        for (int r = 0; r < rows.size(); r++) {
            String row = rows.get(r);
            int line = gridLine + 1 + r;
            if (row.length() != width) {
                throw new LevelFormatException(id, line, "row length " + row.length() + ", expected " + width);
            }
            for (int c = 0; c < width; c++) {
                char ch = row.charAt(c);
                Position p = new Position(r, c);
                CellType type = CellType.FLOOR;
                switch (ch) {
                    case '#' -> type = CellType.WALL;
                    case '.' -> { }
                    case '*' -> items.add(p);
                    case 'G' -> {
                        type = CellType.GOAL;
                        goals++;
                    }
                    case '^', '>', 'v', '<' -> {
                        robots++;
                        start = p;
                        facing = switch (ch) {
                            case '^' -> Direction.NORTH;
                            case '>' -> Direction.EAST;
                            case 'v' -> Direction.SOUTH;
                            default -> Direction.WEST;
                        };
                    }
                    default -> throw new LevelFormatException(id, line,
                            "invalid symbol '" + ch + "' at column " + (c + 1));
                }
                cells[r][c] = type;
            }
        }
        int height = rows.size();
        if (height < MIN_SIZE || height > MAX_SIZE || width < MIN_SIZE || width > MAX_SIZE) {
            throw new LevelFormatException(id, gridLine,
                    "grid size " + height + "x" + width + ", must be between 3x3 and 12x12");
        }
        if (robots != 1) {
            throw new LevelFormatException(id, gridLine, "expected exactly one robot, found " + robots);
        }
        if (goals != 1) {
            throw new LevelFormatException(id, gridLine, "expected exactly one goal, found " + goals);
        }
        return new Variant(new Grid(cells, items), start, facing);
    }

    /**
     * Loads every level of the campaign, in order. Invalid levels are logged and skipped.
     *
     * @return all levels
     */
    public static List<Level> loadAll() {
        return loadAll("levels");
    }

    /** Loads the campaign listed in {@code <dir>/index.txt} on the classpath, skipping invalid levels. */
    static List<Level> loadAll(String dir) {
        List<Level> levels = new ArrayList<>();
        String index = read(dir + "/index.txt");
        if (index == null) {
            LOG.log(System.Logger.Level.WARNING, "Level index {0}/index.txt not found", dir);
            return levels;
        }
        for (String raw : index.split("\\R")) {
            String name = raw.strip();
            if (name.isEmpty() || name.startsWith("#")) {
                continue;
            }
            try {
                String text = read(dir + "/" + name + ".level");
                if (text == null) {
                    throw new IllegalStateException("file not found");
                }
                Level p = parse(name, text);
                levels.add(new Level(p.id(), p.title(), p.briefing(), p.keywords(), p.parInstructions(),
                        p.parActions(), p.variants(), read(dir + "/" + name + ".solution.rl")));
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.WARNING, "Skipping level " + name + ": " + e.getMessage());
            }
        }
        return levels;
    }

    /** Returns the classpath resource as UTF-8 text, or null when absent. */
    private static String read(String path) {
        try (InputStream in = LevelLoader.class.getResourceAsStream("/" + path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

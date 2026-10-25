package robolang.model;

import java.util.Arrays;
import java.util.Set;

/** An immutable rectangular map of cells, with the initial item placement. */
public final class Grid {
    private final CellType[][] cells;
    private final Position goal;
    private final Set<Position> items;

    /**
     * Creates a grid. The arrays are copied.
     *
     * @param cells rectangular, non-empty cell matrix indexed [row][col], with exactly one {@link CellType#GOAL}
     * @param items initial item positions; each must be on a {@link CellType#FLOOR} cell
     * @throws IllegalArgumentException if the matrix is empty, ragged, has no or several goals, or an item is misplaced
     */
    public Grid(CellType[][] cells, Set<Position> items) {
        if (cells.length == 0 || cells[0].length == 0) {
            throw new IllegalArgumentException("Grid must not be empty");
        }
        this.cells = new CellType[cells.length][];
        Position found = null;
        for (int r = 0; r < cells.length; r++) {
            if (cells[r].length != cells[0].length) {
                throw new IllegalArgumentException("Grid must be rectangular");
            }
            this.cells[r] = cells[r].clone();
            for (int c = 0; c < cells[r].length; c++) {
                if (cells[r][c] == CellType.GOAL) {
                    if (found != null) {
                        throw new IllegalArgumentException("Grid must have exactly one goal");
                    }
                    found = new Position(r, c);
                }
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Grid must have exactly one goal");
        }
        this.goal = found;
        this.items = Set.copyOf(items);
        for (Position p : this.items) {
            if (cellAt(p) != CellType.FLOOR) {
                throw new IllegalArgumentException("Item at " + p + " is not on a floor cell");
            }
        }
    }

    /**
     * Returns the number of rows.
     *
     * @return row count
     */
    public int rows() {
        return cells.length;
    }

    /**
     * Returns the number of columns.
     *
     * @return column count
     */
    public int cols() {
        return cells[0].length;
    }

    /**
     * Returns the cell type at a position.
     *
     * @param p position to look up
     * @return the cell type; {@link CellType#WALL} for positions outside the grid
     */
    public CellType cellAt(Position p) {
        if (p.row() < 0 || p.row() >= rows() || p.col() < 0 || p.col() >= cols()) {
            return CellType.WALL;
        }
        return cells[p.row()][p.col()];
    }

    /**
     * Returns the goal position.
     *
     * @return the single goal cell
     */
    public Position goal() {
        return goal;
    }

    /**
     * Returns the initial item placement.
     *
     * @return an unmodifiable set of positions
     */
    public Set<Position> initialItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Grid g && Arrays.deepEquals(cells, g.cells) && items.equals(g.items);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.deepHashCode(cells) + items.hashCode();
    }
}

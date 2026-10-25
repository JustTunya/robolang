package robolang.model;

/** A compass direction the robot can face. Row 0 is the top of the grid. */
public enum Direction {
    /** Towards row 0. */
    NORTH(-1, 0),
    /** Towards higher columns. */
    EAST(0, 1),
    /** Towards higher rows. */
    SOUTH(1, 0),
    /** Towards column 0. */
    WEST(0, -1);

    private final int dRow;
    private final int dCol;

    Direction(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    /**
     * Returns the direction after a 90 degree counter-clockwise turn.
     *
     * @return the direction to the left
     */
    public Direction left() {
        return values()[(ordinal() + 3) % 4];
    }

    /**
     * Returns the direction after a 90 degree clockwise turn.
     *
     * @return the direction to the right
     */
    public Direction right() {
        return values()[(ordinal() + 1) % 4];
    }

    /**
     * Returns the row offset of one step in this direction.
     *
     * @return -1, 0 or 1
     */
    public int dRow() {
        return dRow;
    }

    /**
     * Returns the column offset of one step in this direction.
     *
     * @return -1, 0 or 1
     */
    public int dCol() {
        return dCol;
    }
}

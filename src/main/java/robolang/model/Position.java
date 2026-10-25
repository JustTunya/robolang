package robolang.model;

/**
 * A grid coordinate; the origin (0, 0) is the top left cell.
 *
 * @param row zero-based row
 * @param col zero-based column
 */
public record Position(int row, int col) {

    /**
     * Returns the neighbouring position in the given direction. No bounds check.
     *
     * @param direction direction of the step
     * @return the position one cell away
     */
    public Position step(Direction direction) {
        return new Position(row + direction.dRow(), col + direction.dCol());
    }
}

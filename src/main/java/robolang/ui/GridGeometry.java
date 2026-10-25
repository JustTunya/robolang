package robolang.ui;

import java.awt.Rectangle;

/**
 * Where a grid is drawn inside a panel: the largest square cells that fit, centred.
 *
 * @param cellSize side of one cell in pixels
 * @param offsetX x of the left edge of the grid
 * @param offsetY y of the top edge of the grid
 */
public record GridGeometry(int cellSize, int offsetX, int offsetY) {

    /**
     * Fits a grid into a panel.
     *
     * @param width panel width in pixels
     * @param height panel height in pixels
     * @param rows grid rows, at least 1
     * @param cols grid columns, at least 1
     * @return the geometry; cell size 0 if the panel is too small
     */
    public static GridGeometry fit(int width, int height, int rows, int cols) {
        int size = Math.max(0, Math.min(width / cols, height / rows));
        return new GridGeometry(size, (width - size * cols) / 2, (height - size * rows) / 2);
    }

    /**
     * Returns the bounds of a cell.
     *
     * @param row zero-based row
     * @param col zero-based column
     * @return the cell rectangle
     */
    public Rectangle cell(int row, int col) {
        return new Rectangle(offsetX + col * cellSize, offsetY + row * cellSize, cellSize, cellSize);
    }
}

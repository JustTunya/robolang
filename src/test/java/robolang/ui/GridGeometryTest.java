package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

class GridGeometryTest {

    @Test
    void widePanelIsLimitedByHeightAndCentredHorizontally() {
        GridGeometry g = GridGeometry.fit(900, 300, 3, 9);
        assertEquals(100, g.cellSize());
        assertEquals(0, g.offsetX());
        assertEquals(0, g.offsetY());
        g = GridGeometry.fit(1000, 300, 3, 9);
        assertEquals(100, g.cellSize());
        assertEquals(50, g.offsetX());
    }

    @Test
    void tallPanelIsLimitedByWidthAndCentredVertically() {
        GridGeometry g = GridGeometry.fit(400, 1000, 4, 4);
        assertEquals(100, g.cellSize());
        assertEquals(0, g.offsetX());
        assertEquals(300, g.offsetY());
    }

    @Test
    void cellBoundsFollowGeometry() {
        GridGeometry g = GridGeometry.fit(1000, 300, 3, 9);
        assertEquals(new Rectangle(250, 100, 100, 100), g.cell(1, 2));
    }

    @Test
    void tinyPanelGivesZeroCells() {
        assertEquals(0, GridGeometry.fit(5, 5, 10, 10).cellSize());
        assertEquals(0, GridGeometry.fit(0, 0, 3, 3).cellSize());
    }

    @Test
    void singleCellGrid() {
        GridGeometry g = GridGeometry.fit(200, 100, 1, 1);
        assertEquals(100, g.cellSize());
        assertEquals(50, g.offsetX());
    }
}

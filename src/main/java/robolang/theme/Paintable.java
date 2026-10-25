package robolang.theme;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Something that draws itself into a grid cell. */
public interface Paintable {

    /**
     * Paints into the given cell.
     *
     * @param g graphics context
     * @param cell the cell bounds in device pixels
     */
    void paint(Graphics2D g, Rectangle cell);
}

package robolang.theme;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import robolang.model.Direction;

/** Abstract product: the robot. */
public interface RobotView extends Paintable {

    /**
     * Paints the robot facing a direction, part-way through an animation.
     *
     * @param g graphics context
     * @param cell the cell bounds
     * @param facing direction the robot faces
     * @param progress animation progress from 0.0 to 1.0
     */
    void paint(Graphics2D g, Rectangle cell, Direction facing, double progress);

    /**
     * Paints the robot facing east with a finished animation.
     *
     * @param g graphics context
     * @param cell the cell bounds
     */
    @Override
    default void paint(Graphics2D g, Rectangle cell) {
        paint(g, cell, Direction.EAST, 1.0);
    }

    /**
     * Returns the message shown when the robot crashes.
     *
     * @return themed crash text
     */
    String crashMessage();
}

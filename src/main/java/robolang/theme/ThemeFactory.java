package robolang.theme;

/** Abstract factory creating one consistent family of visual products. */
public interface ThemeFactory {

    /**
     * Returns the theme name.
     *
     * @return display name
     */
    String name();

    /**
     * Creates the robot product.
     *
     * @return a robot view
     */
    RobotView createRobot();

    /**
     * Creates the wall product.
     *
     * @return a wall tile
     */
    WallTile createWall();

    /**
     * Creates the floor product.
     *
     * @return a floor tile
     */
    FloorTile createFloor();

    /**
     * Creates the item product.
     *
     * @return a collectible
     */
    Collectible createCollectible();

    /**
     * Creates the goal product.
     *
     * @return a goal tile
     */
    GoalTile createGoal();
}

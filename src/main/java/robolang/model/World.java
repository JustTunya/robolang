package robolang.model;

import java.util.HashSet;
import java.util.Set;

/** The mutable game state: a robot on a grid with the items not yet picked up. */
public final class World {
    private final Grid grid;
    private final Set<Position> items;
    private Position robot;
    private Direction facing;

    /**
     * Creates a world with all initial items present.
     *
     * @param grid the map
     * @param start robot start position; must not be a wall
     * @param facing robot start direction
     * @throws IllegalArgumentException if the start cell is a wall
     */
    public World(Grid grid, Position start, Direction facing) {
        if (grid.cellAt(start) == CellType.WALL) {
            throw new IllegalArgumentException("Start " + start + " is a wall");
        }
        this.grid = grid;
        this.items = new HashSet<>(grid.initialItems());
        this.robot = start;
        this.facing = facing;
    }

    /**
     * Returns the robot position.
     *
     * @return current position
     */
    public Position robot() {
        return robot;
    }

    /**
     * Returns the robot direction.
     *
     * @return current facing
     */
    public Direction facing() {
        return facing;
    }

    /**
     * Tells whether the cell in front of the robot is a wall or off the grid.
     *
     * @return true if a move would crash
     */
    public boolean wallAhead() {
        return grid.cellAt(robot.step(facing)) == CellType.WALL;
    }

    /**
     * Tells whether the robot's cell holds an item.
     *
     * @return true if there is an item to pick
     */
    public boolean onItem() {
        return items.contains(robot);
    }

    /**
     * Tells whether the robot stands on the goal.
     *
     * @return true at the goal
     */
    public boolean atGoal() {
        return grid.cellAt(robot) == CellType.GOAL;
    }

    /**
     * Returns how many items are still on the grid.
     *
     * @return remaining item count
     */
    public int itemsLeft() {
        return items.size();
    }

    /**
     * Moves the robot one cell forward.
     *
     * @throws IllegalStateException if {@link #wallAhead()} is true
     */
    public void moveForward() {
        if (wallAhead()) {
            throw new IllegalStateException("Wall ahead of " + robot);
        }
        robot = robot.step(facing);
    }

    /** Turns the robot 90 degrees counter-clockwise. */
    public void turnLeft() {
        facing = facing.left();
    }

    /** Turns the robot 90 degrees clockwise. */
    public void turnRight() {
        facing = facing.right();
    }

    /**
     * Removes the item on the robot's cell.
     *
     * @throws IllegalStateException if {@link #onItem()} is false
     */
    public void pick() {
        if (!items.remove(robot)) {
            throw new IllegalStateException("No item at " + robot);
        }
    }

    /**
     * Captures the current state.
     *
     * @return an immutable copy
     */
    public WorldSnapshot snapshot() {
        return new WorldSnapshot(grid, robot, facing, items);
    }
}

package robolang.model;

/**
 * One grid layout of a level together with the robot start state.
 *
 * @param grid the map
 * @param start robot start position
 * @param facing robot start direction
 */
public record Variant(Grid grid, Position start, Direction facing) {

    /**
     * Creates a fresh world in the start state.
     *
     * @return a new mutable world
     */
    public World newWorld() {
        return new World(grid, start, facing);
    }
}

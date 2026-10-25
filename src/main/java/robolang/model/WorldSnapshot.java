package robolang.model;

import java.util.Set;

/**
 * An immutable copy of a {@link World} state.
 *
 * @param grid the map
 * @param robot robot position
 * @param facing robot direction
 * @param items positions of the items still on the grid (copied on construction)
 */
public record WorldSnapshot(Grid grid, Position robot, Direction facing, Set<Position> items) {

    /** Copies {@code items} so later changes to the source do not leak in. */
    public WorldSnapshot {
        items = Set.copyOf(items);
    }
}

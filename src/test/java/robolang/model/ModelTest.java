package robolang.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModelTest {
    private static final CellType F = CellType.FLOOR;
    private static final CellType W = CellType.WALL;
    private static final CellType G = CellType.GOAL;

    /** 3x4: a floor corridor in row 1 with the goal at (1,3). */
    private static Grid corridor(Set<Position> items) {
        return new Grid(new CellType[][] {{W, W, W, W}, {F, F, F, G}, {W, W, W, W}}, items);
    }

    @Test
    void turningWrapsAroundBothWays() {
        assertEquals(Direction.WEST, Direction.NORTH.left());
        assertEquals(Direction.NORTH, Direction.WEST.right());
        for (Direction d : Direction.values()) {
            assertEquals(d, d.left().right());
            assertEquals(d, d.right().right().right().right());
        }
    }

    @Test
    void steppingMovesByTheDirectionOffsets() {
        Position p = new Position(2, 2);
        assertEquals(new Position(1, 2), p.step(Direction.NORTH));
        assertEquals(new Position(2, 3), p.step(Direction.EAST));
        assertEquals(new Position(3, 2), p.step(Direction.SOUTH));
        assertEquals(new Position(2, 1), p.step(Direction.WEST));
    }

    @Test
    void outsideTheGridIsWall() {
        Grid g = corridor(Set.of());
        assertEquals(3, g.rows());
        assertEquals(4, g.cols());
        assertEquals(new Position(1, 3), g.goal());
        assertEquals(CellType.WALL, g.cellAt(new Position(-1, 0)));
        assertEquals(CellType.WALL, g.cellAt(new Position(0, 4)));
        assertEquals(CellType.FLOOR, g.cellAt(new Position(1, 0)));
    }

    @Test
    void gridRejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(new CellType[][] {{F, F}}, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new Grid(new CellType[][] {{G, G}}, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new Grid(new CellType[][] {{F, G}, {F}}, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Grid(new CellType[][] {{W, G}}, Set.of(new Position(0, 0))));
    }

    @Test
    void gridDoesNotShareItsInputs() {
        CellType[][] cells = {{F, G}};
        Set<Position> items = new HashSet<>(Set.of(new Position(0, 0)));
        Grid g = new Grid(cells, items);
        cells[0][0] = W;
        items.clear();
        assertEquals(CellType.FLOOR, g.cellAt(new Position(0, 0)));
        assertEquals(Set.of(new Position(0, 0)), g.initialItems());
    }

    @Test
    void moveAndWallDetection() {
        World w = new World(corridor(Set.of()), new Position(1, 2), Direction.EAST);
        assertFalse(w.wallAhead());
        w.moveForward();
        assertTrue(w.atGoal());
        assertTrue(w.wallAhead());
        assertThrows(IllegalStateException.class, w::moveForward);
        assertEquals(new Position(1, 3), w.robot());
    }

    @Test
    void movingOffTheGridIsBlocked() {
        World w = new World(corridor(Set.of()), new Position(1, 0), Direction.WEST);
        assertTrue(w.wallAhead());
        assertThrows(IllegalStateException.class, w::moveForward);
    }

    @Test
    void turningChangesFacing() {
        World w = new World(corridor(Set.of()), new Position(1, 0), Direction.EAST);
        w.turnLeft();
        assertEquals(Direction.NORTH, w.facing());
        w.turnRight();
        w.turnRight();
        assertEquals(Direction.SOUTH, w.facing());
    }

    @Test
    void pickRemovesTheItem() {
        World w = new World(corridor(Set.of(new Position(1, 1))), new Position(1, 0), Direction.EAST);
        assertEquals(1, w.itemsLeft());
        assertFalse(w.onItem());
        assertThrows(IllegalStateException.class, w::pick);
        w.moveForward();
        assertTrue(w.onItem());
        w.pick();
        assertEquals(0, w.itemsLeft());
        assertFalse(w.onItem());
        assertThrows(IllegalStateException.class, w::pick);
    }

    @Test
    void worldStartingOnAWallIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new World(corridor(Set.of()), new Position(0, 0), Direction.EAST));
    }

    @Test
    void snapshotIsImmutableAndDetached() {
        World w = new World(corridor(Set.of(new Position(1, 1))), new Position(1, 1), Direction.EAST);
        WorldSnapshot before = w.snapshot();
        w.pick();
        w.moveForward();
        assertEquals(Set.of(new Position(1, 1)), before.items());
        assertEquals(new Position(1, 1), before.robot());
        assertEquals(Set.of(), w.snapshot().items());
        assertThrows(UnsupportedOperationException.class, () -> before.items().clear());
    }

    @Test
    void variantCreatesFreshWorldsEachTime() {
        Variant v = new Variant(corridor(Set.of(new Position(1, 1))), new Position(1, 1), Direction.EAST);
        World a = v.newWorld();
        a.pick();
        assertEquals(1, v.newWorld().itemsLeft());
    }
}

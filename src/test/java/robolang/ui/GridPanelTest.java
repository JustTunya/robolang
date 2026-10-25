package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Set;
import org.junit.jupiter.api.Test;
import robolang.model.CellType;
import robolang.model.Direction;
import robolang.model.Grid;
import robolang.model.Position;
import robolang.model.WorldSnapshot;
import robolang.theme.Collectible;
import robolang.theme.FloorTile;
import robolang.theme.GoalTile;
import robolang.theme.RobotView;
import robolang.theme.ThemeFactory;
import robolang.theme.WallTile;

class GridPanelTest {
    private static final int W = 300;
    private static final int H = 200;

    private static final Grid GRID = new Grid(new CellType[][] {
        {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL},
        {CellType.WALL, CellType.FLOOR, CellType.FLOOR, CellType.GOAL},
        {CellType.WALL, CellType.WALL, CellType.WALL, CellType.WALL}},
            Set.of());

    private static WorldSnapshot snap(int col, Direction facing) {
        return new WorldSnapshot(GRID, new Position(1, col), facing, Set.of(new Position(1, 2)));
    }

    private static BufferedImage paint(GridPanel panel) {
        panel.setSize(W, H);
        panel.doLayout();
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        return img;
    }

    /** Colour at (dx, dy) from the top left of grid cell (row, col); the canvas is the second child. */
    private static int pixel(BufferedImage img, GridPanel panel, int row, int col, boolean centre) {
        Rectangle canvas = panel.getComponent(1).getBounds();
        Rectangle c = GridGeometry.fit(canvas.width, canvas.height, 3, 4).cell(row, col);
        int off = centre ? c.width / 2 : 2;
        return img.getRGB(canvas.x + c.x + off, canvas.y + c.y + off);
    }

    @Test
    void paintsFallbackWithoutTheme() {
        GridPanel panel = new GridPanel();
        panel.showSnapshot(snap(1, Direction.EAST));
        BufferedImage img = paint(panel);
        assertEquals(new Color(0x3b3f4a).getRGB(), pixel(img, panel, 0, 0, true));
        assertEquals(new Color(0x3fa34d).getRGB(), pixel(img, panel, 1, 3, true));
        assertEquals(new Color(0xc9ced6).getRGB(), pixel(img, panel, 1, 2, false));
        assertEquals(Color.YELLOW.getRGB(), pixel(img, panel, 1, 2, true));
        assertEquals(Color.WHITE.getRGB(), pixel(img, panel, 1, 1, true));
    }

    @Test
    void paintsThemeProducts() {
        GridPanel panel = new GridPanel();
        panel.setTheme(new SolidTheme());
        panel.showSnapshot(snap(1, Direction.EAST));
        BufferedImage img = paint(panel);
        assertEquals(Color.RED.getRGB(), pixel(img, panel, 0, 0, true));
        assertEquals(Color.GREEN.getRGB(), pixel(img, panel, 1, 3, true));
        assertEquals(Color.MAGENTA.getRGB(), pixel(img, panel, 1, 1, true));
        assertEquals(Color.ORANGE.getRGB(), pixel(img, panel, 1, 2, true));
        panel.showSnapshot(new WorldSnapshot(GRID, new Position(1, 1), Direction.EAST, Set.of()));
        assertEquals(Color.BLUE.getRGB(), pixel(paint(panel), panel, 1, 2, true));
    }

    @Test
    void setThemeRepaintsImmediately() {
        GridPanel panel = new GridPanel();
        panel.showSnapshot(snap(1, Direction.EAST));
        int before = pixel(paint(panel), panel, 0, 0, true);
        panel.setTheme(new SolidTheme());
        assertNotEquals(before, pixel(paint(panel), panel, 0, 0, true));
        panel.setTheme(null);
        assertEquals(before, pixel(paint(panel), panel, 0, 0, true));
    }

    @Test
    void emptyPanelPaintsWithoutSnapshot() {
        paint(new GridPanel());
    }

    @Test
    void zeroMillisJumps() {
        GridPanel panel = new GridPanel();
        panel.setAnimationMillis(0);
        panel.showSnapshot(snap(1, Direction.EAST));
        panel.showSnapshot(snap(2, Direction.EAST));
        assertFalse(panel.isAnimating());
    }

    @Test
    void animatesBetweenSnapshotsAndEnds() throws Exception {
        GridPanel panel = new GridPanel();
        panel.setAnimationMillis(80);
        EventQueue.invokeAndWait(() -> {
            panel.showSnapshot(snap(1, Direction.EAST));
            panel.showSnapshot(snap(2, Direction.EAST));
            assertTrue(panel.isAnimating());
        });
        long deadline = System.currentTimeMillis() + 3000;
        while (panel.isAnimating() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(panel.isAnimating());
    }

    @Test
    void variantTabs() {
        GridPanel panel = new GridPanel();
        assertEquals(-1, panel.selectedVariant());
        panel.setVariantCount(3);
        assertEquals(0, panel.selectedVariant());
        panel.selectVariant(2);
        assertEquals(2, panel.selectedVariant());
        panel.selectVariant(7);
        assertEquals(2, panel.selectedVariant());
        panel.setVariantCount(1);
        assertEquals(-1, panel.selectedVariant());
    }

    /** A theme whose products fill the cell with one colour each. */
    private static final class SolidTheme implements ThemeFactory {
        private static robolang.theme.Paintable solid(Color color) {
            return (g, cell) -> {
                g.setColor(color);
                g.fillRect(cell.x, cell.y, cell.width, cell.height);
            };
        }

        @Override
        public String name() {
            return "Solid";
        }

        @Override
        public RobotView createRobot() {
            return new RobotView() {
                @Override
                public void paint(Graphics2D g, Rectangle cell, Direction facing, double progress) {
                    solid(Color.MAGENTA).paint(g, cell);
                }

                @Override
                public String crashMessage() {
                    return "crash";
                }
            };
        }

        @Override
        public WallTile createWall() {
            return solid(Color.RED)::paint;
        }

        @Override
        public FloorTile createFloor() {
            return solid(Color.BLUE)::paint;
        }

        @Override
        public Collectible createCollectible() {
            return new Collectible() {
                @Override
                public void paint(Graphics2D g, Rectangle cell) {
                    solid(Color.ORANGE).paint(g, cell);
                }

                @Override
                public String pickupMessage() {
                    return "picked";
                }
            };
        }

        @Override
        public GoalTile createGoal() {
            return solid(Color.GREEN)::paint;
        }
    }
}

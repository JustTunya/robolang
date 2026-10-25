package robolang.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import robolang.model.Direction;

class ThemeProductsTest {
    private static final int[] SIZES = {16, 48, 96};
    private static final int MARGIN = 4;

    private static List<Paintable> products(ThemeFactory f) {
        return List.of(f.createRobot(), f.createWall(), f.createFloor(), f.createCollectible(), f.createGoal());
    }

    /** Paints into the middle of a larger transparent image so overdraw shows up in the margin. */
    private static BufferedImage render(int size, BiConsumer<Graphics2D, Rectangle> painter) {
        BufferedImage img = new BufferedImage(size + 2 * MARGIN, size + 2 * MARGIN, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        painter.accept(g, new Rectangle(MARGIN, MARGIN, size, size));
        g.dispose();
        return img;
    }

    private static boolean inCell(int x, int y, int size) {
        return x >= MARGIN && y >= MARGIN && x < MARGIN + size && y < MARGIN + size;
    }

    @Test
    void factoriesReturnConsistentFamilies() {
        for (ThemeFactory f : ThemeRegistry.all()) {
            assertNotNull(f.name());
            List<Paintable> first = products(f);
            List<Paintable> second = products(f);
            for (int i = 0; i < first.size(); i++) {
                assertNotNull(first.get(i));
                assertSame(first.get(i).getClass(), second.get(i).getClass());
            }
        }
    }

    @Test
    void themesProduceDistinctClasses() {
        List<Paintable> a = products(ThemeRegistry.all().get(0));
        List<Paintable> b = products(ThemeRegistry.all().get(1));
        for (int i = 0; i < a.size(); i++) {
            assertNotEquals(a.get(i).getClass(), b.get(i).getClass());
        }
    }

    @Test
    void everyProductPaintsInsideItsCell() {
        for (ThemeFactory f : ThemeRegistry.all()) {
            for (Paintable p : products(f)) {
                for (int size : SIZES) {
                    BufferedImage img = render(size, p::paint);
                    String who = f.name() + " " + p.getClass().getSimpleName() + " size " + size;
                    boolean drew = false;
                    for (int y = 0; y < img.getHeight(); y++) {
                        for (int x = 0; x < img.getWidth(); x++) {
                            int alpha = img.getRGB(x, y) >>> 24;
                            if (inCell(x, y, size)) {
                                drew |= alpha != 0;
                            } else {
                                assertEquals(0, alpha, who + " drew outside the cell at " + x + "," + y);
                            }
                        }
                    }
                    assertTrue(drew, who + " drew nothing");
                }
            }
        }
    }

    @Test
    void robotRenderingDiffersBetweenFacings() {
        for (ThemeFactory f : ThemeRegistry.all()) {
            RobotView robot = f.createRobot();
            Direction[] dirs = Direction.values();
            for (int i = 0; i < dirs.length; i++) {
                for (int j = i + 1; j < dirs.length; j++) {
                    Direction a = dirs[i];
                    Direction b = dirs[j];
                    assertFalse(sameImage(render(48, (g, c) -> robot.paint(g, c, a, 1.0)),
                            render(48, (g, c) -> robot.paint(g, c, b, 1.0))), f.name() + " " + a + " vs " + b);
                }
            }
        }
    }

    @Test
    void robotProgressDrivesMotionCue() {
        for (ThemeFactory f : ThemeRegistry.all()) {
            RobotView robot = f.createRobot();
            assertFalse(sameImage(render(96, (g, c) -> robot.paint(g, c, Direction.EAST, 0.0)),
                    render(96, (g, c) -> robot.paint(g, c, Direction.EAST, 0.25))), f.name());
        }
    }

    @Test
    void messagesAreThemed() {
        ThemeFactory space = ThemeRegistry.byName("Space");
        ThemeFactory jungle = ThemeRegistry.byName("Jungle");
        assertEquals("Crystal charged", space.createCollectible().pickupMessage());
        assertEquals("Rover hit an asteroid", space.createRobot().crashMessage());
        assertEquals("Banana collected", jungle.createCollectible().pickupMessage());
        assertEquals("Monkey hit a tree", jungle.createRobot().crashMessage());
    }

    private static boolean sameImage(BufferedImage a, BufferedImage b) {
        return Arrays.equals(a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth()),
                b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0, b.getWidth()));
    }
}

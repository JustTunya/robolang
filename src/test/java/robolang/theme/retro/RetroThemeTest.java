package robolang.theme.retro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import robolang.model.Direction;
import robolang.theme.ThemeRegistry;

class RetroThemeTest {
    private static final RetroThemeFactory F = RetroThemeFactory.INSTANCE;

    @Test
    void registeredAndSingleInstance() {
        assertSame(F, ThemeRegistry.byName("retro"));
        assertSame(F.createRobot(), F.createRobot());
    }

    @Test
    void messagesAreThemed() {
        assertEquals("Coin collected", F.createCollectible().pickupMessage());
        assertEquals("Bot bumped a brick", F.createRobot().crashMessage());
    }

    @Test
    void robotIsDrawnWithoutAntialiasingOrBlending() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        F.createRobot().paint(g, new Rectangle(0, 0, 50, 50), Direction.NORTH, 0.0);
        g.dispose();
        Set<Integer> alphas = new HashSet<>();
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
                alphas.add(img.getRGB(x, y) >>> 24);
            }
        }
        assertTrue(alphas.stream().allMatch(a -> a == 0 || a == 255), "partial alpha: " + alphas);
    }

    @Test
    void floorAndWallFillTheWholeCellAtAwkwardSizes() {
        for (int size : new int[] {17, 50, 95}) {
            for (var p : java.util.List.of(F.createFloor(), F.createWall())) {
                BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                p.paint(g, new Rectangle(0, 0, size, size));
                g.dispose();
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        assertEquals(255, img.getRGB(x, y) >>> 24, size + " at " + x + "," + y);
                    }
                }
            }
        }
    }
}

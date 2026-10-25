package robolang.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import robolang.model.Direction;

/** Renders every product of every registered theme at 64 px into {@code target/theme-preview/}. */
public final class ThemePreview {
    private static final int SIZE = 64;

    private ThemePreview() {
    }

    /**
     * Writes one PNG per theme: the robot in all four facings on top, wall, floor, collectible and goal below.
     *
     * @param args unused
     * @throws IOException if a PNG cannot be written
     */
    public static void main(String[] args) throws IOException {
        File dir = new File("target/theme-preview");
        dir.mkdirs();
        for (ThemeFactory f : ThemeRegistry.all()) {
            List<Paintable> row2 = List.of(f.createWall(), f.createFloor(), f.createCollectible(), f.createGoal());
            BufferedImage img = new BufferedImage(SIZE * 4, SIZE * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            Direction[] dirs = Direction.values();
            for (int i = 0; i < 4; i++) {
                Rectangle top = new Rectangle(i * SIZE, 0, SIZE, SIZE);
                f.createFloor().paint(g, top);
                f.createRobot().paint(g, top, dirs[i], 0.25);
                Rectangle bottom = new Rectangle(i * SIZE, SIZE, SIZE, SIZE);
                if (i != 1) {
                    f.createFloor().paint(g, bottom);
                }
                row2.get(i).paint(g, bottom);
            }
            g.dispose();
            ImageIO.write(img, "png", new File(dir, f.name().toLowerCase() + ".png"));
        }
    }
}

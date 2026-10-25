package robolang.theme.jungle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import robolang.model.Direction;
import robolang.theme.Collectible;
import robolang.theme.FloorTile;
import robolang.theme.GoalTile;
import robolang.theme.RobotView;
import robolang.theme.ThemeFactory;
import robolang.theme.WallTile;

/** Concrete factory for the Jungle theme: monkey, trees, grass, bananas, hut. */
public final class JungleThemeFactory implements ThemeFactory {
    /** The single stateless instance (D7). */
    public static final JungleThemeFactory INSTANCE = new JungleThemeFactory();

    private static final RobotView ROBOT = new Monkey();
    private static final WallTile WALL = new Tree();
    private static final FloorTile FLOOR = new Grass();
    private static final Collectible ITEM = new Banana();
    private static final GoalTile GOAL = new Hut();

    private JungleThemeFactory() {
    }

    @Override
    public String name() {
        return "Jungle";
    }

    @Override
    public RobotView createRobot() {
        return ROBOT;
    }

    @Override
    public WallTile createWall() {
        return WALL;
    }

    @Override
    public FloorTile createFloor() {
        return FLOOR;
    }

    @Override
    public Collectible createCollectible() {
        return ITEM;
    }

    @Override
    public GoalTile createGoal() {
        return GOAL;
    }

    /** Returns a clipped, antialiased copy of {@code g} whose coordinates span the cell as the unit square. */
    private static Graphics2D open(Graphics2D g, Rectangle cell) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.clip(cell);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(cell.x, cell.y);
        g2.scale(cell.width, cell.height);
        return g2;
    }

    private static void fill(Graphics2D g, Shape s, Color c) {
        g.setColor(c);
        g.fill(s);
    }

    private static void line(Graphics2D g, Shape s, Color c, float w) {
        g.setColor(c);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    private static final class Monkey implements RobotView {
        @Override
        public void paint(Graphics2D g, Rectangle cell, Direction facing, double progress) {
            Graphics2D g2 = open(g, cell);
            g2.rotate(Math.toRadians(90.0 * (facing.ordinal() - 1)), 0.5, 0.5);
            double swing = 0.07 * Math.sin(2 * Math.PI * progress);
            Color fur = new Color(0x7B4A22);
            Path2D tail = new Path2D.Double();
            tail.moveTo(0.3, 0.5);
            tail.curveTo(0.12, 0.5, 0.12, 0.28, 0.24, 0.24);
            line(g2, tail, fur, 0.05f);
            line(g2, new Line2D.Double(0.45, 0.3, 0.62 + swing, 0.16), fur, 0.07f);
            line(g2, new Line2D.Double(0.45, 0.7, 0.62 - swing, 0.84), fur, 0.07f);
            fill(g2, new Ellipse2D.Double(0.22, 0.27, 0.46, 0.46), fur);
            fill(g2, new Ellipse2D.Double(0.52, 0.32, 0.32, 0.36), fur);
            fill(g2, new Ellipse2D.Double(0.62, 0.38, 0.2, 0.24), new Color(0xF0CFA0));
            fill(g2, new Ellipse2D.Double(0.58, 0.22, 0.12, 0.12), fur);
            fill(g2, new Ellipse2D.Double(0.58, 0.66, 0.12, 0.12), fur);
            fill(g2, new Ellipse2D.Double(0.74, 0.42, 0.05, 0.05), Color.BLACK);
            fill(g2, new Ellipse2D.Double(0.74, 0.53, 0.05, 0.05), Color.BLACK);
            g2.dispose();
        }

        @Override
        public String crashMessage() {
            return "Monkey hit a tree";
        }
    }

    private static final class Tree implements WallTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            fill(g2, new Ellipse2D.Double(0.04, 0.04, 0.92, 0.92), new Color(0x1B5E20));
            fill(g2, new Ellipse2D.Double(0.18, 0.16, 0.5, 0.5), new Color(0x2E7D32));
            fill(g2, new Ellipse2D.Double(0.4, 0.34, 0.42, 0.42), new Color(0x388E3C));
            fill(g2, new Ellipse2D.Double(0.42, 0.42, 0.16, 0.16), new Color(0x5D4037));
            g2.dispose();
        }
    }

    private static final class Grass implements FloorTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            fill(g2, new Rectangle2D.Double(0, 0, 1, 1), new Color(0x7CB342));
            double[][] tufts = {{0.2, 0.3}, {0.65, 0.2}, {0.4, 0.72}, {0.82, 0.68}};
            for (double[] t : tufts) {
                for (int i = -1; i <= 1; i++) {
                    line(g2, new Line2D.Double(t[0], t[1], t[0] + 0.05 * i, t[1] - 0.1), new Color(0x558B2F), 0.025f);
                }
            }
            g2.dispose();
        }
    }

    private static final class Banana implements Collectible {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            Path2D b = new Path2D.Double();
            b.moveTo(0.24, 0.3);
            b.curveTo(0.3, 0.8, 0.7, 0.88, 0.82, 0.5);
            b.curveTo(0.66, 0.72, 0.38, 0.68, 0.24, 0.3);
            b.closePath();
            fill(g2, b, new Color(0xFFE135));
            line(g2, b, new Color(0xB8860B), 0.03f);
            line(g2, new Line2D.Double(0.24, 0.3, 0.2, 0.2), new Color(0x5D4037), 0.045f);
            g2.dispose();
        }

        @Override
        public String pickupMessage() {
            return "Banana collected";
        }
    }

    private static final class Hut implements GoalTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            fill(g2, new RoundRectangle2D.Double(0.14, 0.4, 0.72, 0.5, 0.06, 0.06), new Color(0x8D6E63));
            Path2D roof = new Path2D.Double();
            roof.moveTo(0.5, 0.08);
            roof.lineTo(0.94, 0.44);
            roof.lineTo(0.06, 0.44);
            roof.closePath();
            fill(g2, roof, new Color(0xD4A24C));
            line(g2, roof, new Color(0x8A6420), 0.03f);
            fill(g2, new Rectangle2D.Double(0.42, 0.6, 0.16, 0.3), new Color(0x3E2723));
            g2.dispose();
        }
    }
}

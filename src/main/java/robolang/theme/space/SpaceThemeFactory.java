package robolang.theme.space;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
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

/** Concrete factory for the Space theme: rover, asteroids, metal plates, energy crystals, landing pad. */
public final class SpaceThemeFactory implements ThemeFactory {
    /** The single stateless instance (D7). */
    public static final SpaceThemeFactory INSTANCE = new SpaceThemeFactory();

    private static final RobotView ROBOT = new Rover();
    private static final WallTile WALL = new Asteroid();
    private static final FloorTile FLOOR = new MetalPlate();
    private static final Collectible ITEM = new EnergyCrystal();
    private static final GoalTile GOAL = new LandingPad();

    private SpaceThemeFactory() {
    }

    @Override
    public String name() {
        return "Space";
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

    private static void fill(Graphics2D g, java.awt.Shape s, Color c) {
        g.setColor(c);
        g.fill(s);
    }

    private static void line(Graphics2D g, java.awt.Shape s, Color c, float w) {
        g.setColor(c);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    private static final class Rover implements RobotView {
        @Override
        public void paint(Graphics2D g, Rectangle cell, Direction facing, double progress) {
            Graphics2D g2 = open(g, cell);
            g2.rotate(Math.toRadians(90.0 * (facing.ordinal() - 1)), 0.5, 0.5);
            double shift = 0.08 * Math.max(0, Math.min(1, progress));
            for (double y : new double[] {0.17, 0.69}) {
                for (double x : new double[] {0.2, 0.55}) {
                    fill(g2, new RoundRectangle2D.Double(x, y, 0.25, 0.14, 0.05, 0.05), new Color(0x2B2F3A));
                    line(g2, new java.awt.geom.Line2D.Double(x + 0.03 + shift, y + 0.02, x + 0.03 + shift, y + 0.12),
                            new Color(0x9AA3B5), 0.025f);
                }
            }
            fill(g2, new RoundRectangle2D.Double(0.2, 0.3, 0.6, 0.4, 0.1, 0.1), new Color(0xD8DEE9));
            line(g2, new RoundRectangle2D.Double(0.2, 0.3, 0.6, 0.4, 0.1, 0.1), new Color(0x5E6B85), 0.03f);
            fill(g2, new Rectangle2D.Double(0.28, 0.38, 0.22, 0.24), new Color(0x3B5BA5));
            fill(g2, new Ellipse2D.Double(0.62, 0.4, 0.14, 0.2), new Color(0xFFD23F));
            g2.dispose();
        }

        @Override
        public String crashMessage() {
            return "Rover hit an asteroid";
        }
    }

    private static final class Asteroid implements WallTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            Path2D rock = new Path2D.Double();
            double[][] p = {{0.5, 0.06}, {0.78, 0.12}, {0.94, 0.4}, {0.88, 0.74}, {0.62, 0.94},
                {0.3, 0.9}, {0.08, 0.66}, {0.12, 0.3}, {0.3, 0.12}};
            rock.moveTo(p[0][0], p[0][1]);
            for (int i = 1; i < p.length; i++) {
                rock.lineTo(p[i][0], p[i][1]);
            }
            rock.closePath();
            fill(g2, rock, new Color(0x7A7468));
            line(g2, rock, new Color(0x3E3A33), 0.04f);
            fill(g2, new Ellipse2D.Double(0.3, 0.25, 0.22, 0.2), new Color(0x57524A));
            fill(g2, new Ellipse2D.Double(0.58, 0.55, 0.18, 0.16), new Color(0x57524A));
            fill(g2, new Ellipse2D.Double(0.28, 0.62, 0.12, 0.1), new Color(0x57524A));
            g2.dispose();
        }
    }

    private static final class MetalPlate implements FloorTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            fill(g2, new Rectangle2D.Double(0, 0, 1, 1), new Color(0x4A5163));
            line(g2, new Rectangle2D.Double(0.03, 0.03, 0.94, 0.94), new Color(0x2C3140), 0.04f);
            for (double x : new double[] {0.14, 0.86}) {
                for (double y : new double[] {0.14, 0.86}) {
                    fill(g2, new Ellipse2D.Double(x - 0.04, y - 0.04, 0.08, 0.08), new Color(0x8C95A8));
                }
            }
            g2.dispose();
        }
    }

    private static final class EnergyCrystal implements Collectible {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            Path2D gem = new Path2D.Double();
            gem.moveTo(0.5, 0.15);
            gem.lineTo(0.72, 0.5);
            gem.lineTo(0.5, 0.85);
            gem.lineTo(0.28, 0.5);
            gem.closePath();
            fill(g2, gem, new Color(0x2EE6D6));
            Path2D facet = new Path2D.Double();
            facet.moveTo(0.5, 0.15);
            facet.lineTo(0.5, 0.85);
            facet.lineTo(0.28, 0.5);
            facet.closePath();
            fill(g2, facet, new Color(0x1AA8B8));
            line(g2, gem, new Color(0xE8FFFF), 0.035f);
            g2.dispose();
        }

        @Override
        public String pickupMessage() {
            return "Crystal charged";
        }
    }

    private static final class LandingPad implements GoalTile {
        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            Graphics2D g2 = open(g, cell);
            fill(g2, new Ellipse2D.Double(0.08, 0.08, 0.84, 0.84), new Color(0x20242F));
            line(g2, new Ellipse2D.Double(0.14, 0.14, 0.72, 0.72), new Color(0xFFD23F), 0.05f);
            line(g2, new java.awt.geom.Line2D.Double(0.36, 0.34, 0.36, 0.66), new Color(0xFFD23F), 0.06f);
            line(g2, new java.awt.geom.Line2D.Double(0.64, 0.34, 0.64, 0.66), new Color(0xFFD23F), 0.06f);
            line(g2, new java.awt.geom.Line2D.Double(0.36, 0.5, 0.64, 0.5), new Color(0xFFD23F), 0.06f);
            g2.dispose();
        }
    }
}

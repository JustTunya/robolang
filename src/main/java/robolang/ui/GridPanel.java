package robolang.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Collections;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;
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

/**
 * Paints the world with the products of a {@link ThemeFactory} (Abstract Factory client). Holds only the
 * factory interface; without a theme it paints plain coloured squares.
 */
@SuppressWarnings("serial")
public final class GridPanel extends JPanel {
    private static final int FRAME_MILLIS = 15;
    private static final Color BACKGROUND = new Color(0x20232a);

    private final Canvas canvas = new Canvas();
    private final JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    private final ButtonGroup tabGroup = new ButtonGroup();
    private final Timer timer = new Timer(FRAME_MILLIS, e -> tick());

    private RobotView robot;
    private WallTile wall;
    private FloorTile floor;
    private Collectible item;
    private GoalTile goal;

    private WorldSnapshot current;
    private WorldSnapshot previous;
    private int animationMillis = 250;
    private long animationStart;
    private double progress = 1.0;
    private int variantCount;

    /** Creates an empty grid view. */
    public GridPanel() {
        super(new BorderLayout());
        add(tabs, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        setVariantCount(0);
    }

    /**
     * Switches theme: asks the factory for one instance of each product and repaints at once.
     *
     * @param theme the new theme, or null for the plain fallback
     */
    public void setTheme(ThemeFactory theme) {
        if (theme == null) {
            robot = null;
            wall = null;
            floor = null;
            item = null;
            goal = null;
        } else {
            robot = theme.createRobot();
            wall = theme.createWall();
            floor = theme.createFloor();
            item = theme.createCollectible();
            goal = theme.createGoal();
        }
        canvas.repaint();
    }

    /**
     * Shows a world state. The robot animates from its previous position and facing.
     *
     * @param snapshot the state to show; null clears the view
     */
    public void showSnapshot(WorldSnapshot snapshot) {
        timer.stop();
        boolean animate = snapshot != null && current != null && animationMillis > 0
                && snapshot.grid().equals(current.grid());
        previous = animate ? current : null;
        current = snapshot;
        progress = animate ? 0.0 : 1.0;
        if (animate) {
            animationStart = System.nanoTime();
            timer.start();
        }
        canvas.repaint();
    }

    /**
     * Sets the duration of one move or turn animation.
     *
     * @param millis duration in milliseconds; 0 makes the robot jump
     */
    public void setAnimationMillis(int millis) {
        animationMillis = Math.max(0, millis);
    }

    /**
     * Returns whether a robot animation is running.
     *
     * @return true while animating
     */
    public boolean isAnimating() {
        return timer.isRunning();
    }

    /**
     * Sets the number of variant tabs; fewer than two variants show no tabs. Selects the first tab.
     *
     * @param count number of variants
     */
    public void setVariantCount(int count) {
        variantCount = count;
        tabs.removeAll();
        for (var tab : Collections.list(tabGroup.getElements())) {
            tabGroup.remove(tab);
        }
        if (count > 1) {
            tabs.add(new JLabel("Variant:"));
            for (int i = 0; i < count; i++) {
                JToggleButton tab = new JToggleButton(String.valueOf(i + 1));
                tab.setMargin(new Insets(1, 6, 1, 6));
                tabGroup.add(tab);
                tabs.add(tab);
            }
            selectVariant(0);
        }
        tabs.revalidate();
        tabs.repaint();
    }

    /**
     * Highlights a variant tab.
     *
     * @param index zero-based variant index; ignored if out of range
     */
    public void selectVariant(int index) {
        if (variantCount < 2 || index < 0 || index >= variantCount) {
            return;
        }
        var buttons = Collections.list(tabGroup.getElements());
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setSelected(i == index);
        }
    }

    /**
     * Returns the selected variant tab.
     *
     * @return zero-based index, or -1 when there are no tabs
     */
    public int selectedVariant() {
        var buttons = Collections.list(tabGroup.getElements());
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void tick() {
        progress = Math.min(1.0, (System.nanoTime() - animationStart) / (animationMillis * 1_000_000.0));
        if (progress >= 1.0) {
            timer.stop();
            previous = null;
        }
        canvas.repaint();
    }

    /** The drawing surface below the variant tabs. */
    @SuppressWarnings("serial")
    private final class Canvas extends JPanel {
        Canvas() {
            setBackground(BACKGROUND);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            WorldSnapshot snap = current;
            if (snap == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Grid grid = snap.grid();
            GridGeometry geo = GridGeometry.fit(getWidth(), getHeight(), grid.rows(), grid.cols());
            for (int r = 0; r < grid.rows(); r++) {
                for (int c = 0; c < grid.cols(); c++) {
                    paintCell(g, grid.cellAt(new Position(r, c)), geo.cell(r, c));
                }
            }
            for (Position p : snap.items()) {
                Rectangle cell = geo.cell(p.row(), p.col());
                if (item != null) {
                    item.paint(g, cell);
                } else {
                    g.setColor(Color.YELLOW);
                    g.fillOval(cell.x + cell.width / 3, cell.y + cell.height / 3, cell.width / 3, cell.height / 3);
                }
            }
            paintRobot(g, snap, geo);
            g.dispose();
        }

        private void paintCell(Graphics2D g, CellType type, Rectangle cell) {
            switch (type) {
                case WALL -> {
                    if (wall != null) {
                        wall.paint(g, cell);
                    } else {
                        fallback(g, cell, new Color(0x3b3f4a));
                    }
                }
                case GOAL -> {
                    if (goal != null) {
                        goal.paint(g, cell);
                    } else {
                        fallback(g, cell, new Color(0x3fa34d));
                    }
                }
                case FLOOR -> {
                    if (floor != null) {
                        floor.paint(g, cell);
                    } else {
                        fallback(g, cell, new Color(0xc9ced6));
                    }
                }
            }
        }

        private void fallback(Graphics2D g, Rectangle cell, Color color) {
            g.setColor(color);
            g.fillRect(cell.x, cell.y, cell.width - 1, cell.height - 1);
        }

        private void paintRobot(Graphics2D g, WorldSnapshot snap, GridGeometry geo) {
            Position to = snap.robot();
            Position from = previous != null ? previous.robot() : to;
            Rectangle cell = geo.cell(to.row(), to.col());
            cell.x += (int) Math.round((from.col() - to.col()) * (1 - progress) * geo.cellSize());
            cell.y += (int) Math.round((from.row() - to.row()) * (1 - progress) * geo.cellSize());
            if (robot != null) {
                robot.paint(g, cell, snap.facing(), progress);
                return;
            }
            g.setColor(new Color(0x2f6fdc));
            g.fillOval(cell.x + 3, cell.y + 3, cell.width - 6, cell.height - 6);
            g.setColor(Color.WHITE);
            g.fill(arrow(cell, snap.facing(), previous != null ? previous.facing() : snap.facing()));
        }

        /** A white arrowhead pointing the facing direction, turned part-way from the old direction. */
        private Polygon arrow(Rectangle cell, Direction to, Direction from) {
            // EAST = 0 degrees; the angle grows clockwise because y grows downwards. NORTH is 270 degrees.
            double angle = Math.toRadians(90 * (to.ordinal() + 3));
            int diff = Math.floorMod(to.ordinal() - from.ordinal() + 2, 4) - 2;
            angle -= Math.toRadians(90 * diff) * (1 - progress);
            double cx = cell.getCenterX();
            double cy = cell.getCenterY();
            double s = cell.width / 4.0;
            double[][] pts = {{s, 0}, {-s, -s}, {-s, s}};
            Polygon poly = new Polygon();
            for (double[] p : pts) {
                poly.addPoint((int) Math.round(cx + p[0] * Math.cos(angle) - p[1] * Math.sin(angle)),
                        (int) Math.round(cy + p[0] * Math.sin(angle) + p[1] * Math.cos(angle)));
            }
            return poly;
        }
    }
}

package robolang.theme.retro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import robolang.model.Direction;
import robolang.theme.Collectible;
import robolang.theme.FloorTile;
import robolang.theme.GoalTile;
import robolang.theme.RobotView;
import robolang.theme.ThemeFactory;
import robolang.theme.WallTile;

/** Concrete factory for the Retro theme: pixel bot, brick, dark tile, coin, flag. */
public final class RetroThemeFactory implements ThemeFactory {
    /** The single stateless instance (D7). */
    public static final RetroThemeFactory INSTANCE = new RetroThemeFactory();

    private static final int GRID = 8;
    private static final RobotView ROBOT = new PixelBot();
    private static final WallTile WALL = new Brick();
    private static final FloorTile FLOOR = new DarkTile();
    private static final Collectible ITEM = new Coin();
    private static final GoalTile GOAL = new Flag();

    private RetroThemeFactory() {
    }

    @Override
    public String name() {
        return "Retro";
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

    /** Colour of a sprite character; anything else (".") is transparent. */
    private static Color color(char c) {
        return switch (c) {
            case 'B' -> new Color(0x5BC0EB);
            case 'D' -> new Color(0x2B7A9E);
            case 'W' -> Color.WHITE;
            case 'K' -> new Color(0x111111);
            case 'R' -> new Color(0xE63946);
            case 'M' -> new Color(0x9E9E9E);
            case 'O' -> new Color(0xB5472E);
            case 'F' -> new Color(0x1B1B2F);
            case 'L' -> new Color(0x2C2C47);
            case 'Y' -> new Color(0xFFD23F);
            case 'G' -> new Color(0xC99700);
            case 'P' -> new Color(0xCFCFCF);
            default -> null;
        };
    }

    /**
     * Draws an 8x8 sprite with {@code fillRect}, rotated clockwise by {@code quarterTurns}. Each virtual pixel's
     * edges are rounded to device pixels, so neighbours abut without gaps or overlap and nothing leaves the cell.
     */
    private static void sprite(Graphics2D g, Rectangle cell, String[] rows, int quarterTurns) {
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int sx = x;
                int sy = y;
                for (int t = 0; t < Math.floorMod(quarterTurns, 4); t++) {
                    // inverse of one clockwise quarter turn: destination (x, y) reads source (y, GRID-1-x)
                    int nx = sy;
                    sy = GRID - 1 - sx;
                    sx = nx;
                }
                Color c = color(rows[sy].charAt(sx));
                if (c == null) {
                    continue;
                }
                int x0 = cell.x + x * cell.width / GRID;
                int y0 = cell.y + y * cell.height / GRID;
                int x1 = cell.x + (x + 1) * cell.width / GRID;
                int y1 = cell.y + (y + 1) * cell.height / GRID;
                g.setColor(c);
                g.fillRect(x0, y0, x1 - x0, y1 - y0);
            }
        }
    }

    private static final class PixelBot implements RobotView {
        private static final String[] BODY = {
            "....R...",
            ".DDDDDD.",
            ".BBBBWK.",
            ".BBBBBB.",
            ".BBBBBB.",
            ".DDDDDD.",
        };
        private static final String[] LEGS_A = {".M.M.M..", ".M.M.M.."};
        private static final String[] LEGS_B = {"..M.M.M.", "..M.M.M."};

        @Override
        public void paint(Graphics2D g, Rectangle cell, Direction facing, double progress) {
            String[] legs = (int) (progress * 4) % 2 == 0 ? LEGS_A : LEGS_B;
            String[] rows = new String[GRID];
            System.arraycopy(BODY, 0, rows, 0, BODY.length);
            System.arraycopy(legs, 0, rows, BODY.length, legs.length);
            sprite(g, cell, rows, facing.ordinal() - 1);
        }

        @Override
        public String crashMessage() {
            return "Bot bumped a brick";
        }
    }

    private static final class Brick implements WallTile {
        private static final String[] ROWS = {
            "OOOMOOOO",
            "OOOMOOOO",
            "MMMMMMMM",
            "OMOOOMOO",
            "OMOOOMOO",
            "MMMMMMMM",
            "OOOMOOOO",
            "OOOMOOOO",
        };

        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            sprite(g, cell, ROWS, 0);
        }
    }

    private static final class DarkTile implements FloorTile {
        private static final String[] ROWS = {
            "FFFFFFFF",
            "FFFFFFFF",
            "FFLFFFFF",
            "FFFFFFFF",
            "FFFFFFLF",
            "FFFFFFFF",
            "FLFFFFFF",
            "FFFFFFFF",
        };

        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            sprite(g, cell, ROWS, 0);
        }
    }

    private static final class Coin implements Collectible {
        private static final String[] ROWS = {
            "........",
            "..GGGG..",
            ".GYYYYG.",
            ".GYGGYG.",
            ".GYGGYG.",
            ".GYYYYG.",
            "..GGGG..",
            "........",
        };

        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            sprite(g, cell, ROWS, 0);
        }

        @Override
        public String pickupMessage() {
            return "Coin collected";
        }
    }

    private static final class Flag implements GoalTile {
        private static final String[] ROWS = {
            "..PRRRR.",
            "..PRRRR.",
            "..PRRR..",
            "..P.....",
            "..P.....",
            "..P.....",
            "..P.....",
            ".MMMM...",
        };

        @Override
        public void paint(Graphics2D g, Rectangle cell) {
            sprite(g, cell, ROWS, 0);
        }
    }
}

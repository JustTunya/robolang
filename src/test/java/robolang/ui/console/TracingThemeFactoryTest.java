package robolang.ui.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import robolang.theme.Collectible;
import robolang.theme.FloorTile;
import robolang.theme.GoalTile;
import robolang.theme.RobotView;
import robolang.theme.ThemeFactory;
import robolang.theme.WallTile;

class TracingThemeFactoryTest {

    private static <T> T stub(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (p, m, a) -> null));
    }

    private final RobotView robot = stub(RobotView.class);
    private final WallTile wall = stub(WallTile.class);
    private final FloorTile floor = stub(FloorTile.class);
    private final Collectible item = stub(Collectible.class);
    private final GoalTile goal = stub(GoalTile.class);

    private final ThemeFactory delegate = new ThemeFactory() {
        @Override
        public String name() {
            return "Stub";
        }

        @Override
        public RobotView createRobot() {
            return robot;
        }

        @Override
        public WallTile createWall() {
            return wall;
        }

        @Override
        public FloorTile createFloor() {
            return floor;
        }

        @Override
        public Collectible createCollectible() {
            return item;
        }

        @Override
        public GoalTile createGoal() {
            return goal;
        }
    };

    private final List<String> lines = new ArrayList<>();
    private final ThemeFactory traced = new TracingThemeFactory(delegate, lines::add);

    @Test
    void returnsTheDelegatesProducts() {
        assertSame(robot, traced.createRobot());
        assertSame(wall, traced.createWall());
        assertSame(floor, traced.createFloor());
        assertSame(item, traced.createCollectible());
        assertSame(goal, traced.createGoal());
    }

    @Test
    void logsOneLinePerCreateCall() {
        traced.createRobot();
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("createRobot()"), lines.get(0));

        traced.createWall();
        traced.createFloor();
        traced.createCollectible();
        traced.createGoal();
        assertEquals(5, lines.size());
        assertTrue(lines.get(4).contains("createGoal()"), lines.get(4));
    }

    @Test
    void nameDelegatesWithoutLogging() {
        assertEquals("Stub", traced.name());
        assertTrue(lines.isEmpty());
    }
}

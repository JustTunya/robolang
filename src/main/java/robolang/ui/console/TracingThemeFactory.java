package robolang.ui.console;

import java.util.function.Consumer;
import robolang.theme.Collectible;
import robolang.theme.FloorTile;
import robolang.theme.GoalTile;
import robolang.theme.RobotView;
import robolang.theme.ThemeFactory;
import robolang.theme.WallTile;

/** Decorator that logs every {@code create*()} call and returns the delegate's product (FR-61). */
public final class TracingThemeFactory implements ThemeFactory {
    private final ThemeFactory delegate;
    private final Consumer<String> log;

    /**
     * Wraps a factory.
     *
     * @param delegate the factory that really creates the products
     * @param log receives one line per call
     */
    public TracingThemeFactory(ThemeFactory delegate, Consumer<String> log) {
        this.delegate = delegate;
        this.log = log;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public RobotView createRobot() {
        return traced("createRobot", delegate.createRobot());
    }

    @Override
    public WallTile createWall() {
        return traced("createWall", delegate.createWall());
    }

    @Override
    public FloorTile createFloor() {
        return traced("createFloor", delegate.createFloor());
    }

    @Override
    public Collectible createCollectible() {
        return traced("createCollectible", delegate.createCollectible());
    }

    @Override
    public GoalTile createGoal() {
        return traced("createGoal", delegate.createGoal());
    }

    private <T> T traced(String method, T product) {
        log.accept(delegate.getClass().getSimpleName() + "." + method + "() -> "
                + product.getClass().getSimpleName());
        return product;
    }
}

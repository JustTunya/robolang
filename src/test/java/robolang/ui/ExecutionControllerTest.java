package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.SourceRange;
import robolang.lang.runtime.FailureKind;
import robolang.lang.runtime.Outcome;
import robolang.model.Level;
import robolang.model.LevelLoader;
import robolang.model.WorldSnapshot;
import robolang.ui.CodeEditorPanel.HighlightKind;
import robolang.ui.execution.ExecutionView;

class ExecutionControllerTest {
    /** Level 01 allows only MOVE; five MOVEs solve it with three stars. */
    private static final Level LEVEL = LevelLoader.loadAll().get(0);

    private static final class FakeView implements ExecutionView {
        volatile String source = "";
        volatile int speed;
        volatile String status = "";
        volatile ExecState state;
        volatile int snapshots;
        volatile int starts;
        final List<String> highlights = new CopyOnWriteArrayList<>();

        @Override
        public String source() {
            return source;
        }

        @Override
        public int speed() {
            return speed;
        }

        @Override
        public void applyState(ExecState newState) {
            state = newState;
        }

        @Override
        public void setStatus(String text) {
            status = text;
        }

        @Override
        public void highlight(SourceRange range, HighlightKind kind) {
            highlights.add(kind + "@" + range.startLine() + ":" + range.startCol());
        }

        @Override
        public void clearHighlights() {
            highlights.clear();
        }

        @Override
        public void showSnapshot(WorldSnapshot snapshot, int animationMillis) {
            snapshots++;
        }

        @Override
        public void showLevelStart(Level level) {
            starts++;
        }

        @Override
        public void selectVariant(int index) {
        }

        @Override
        public void setCounters(int actions, long ticks) {
        }
    }

    private FakeView view;
    private ExecutionController controller;
    private final List<String> events = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        view = new FakeView();
        onEdt(() -> {
            controller = new ExecutionController(view);
            controller.addObserver(new ExecutionObserver() {
                @Override
                public void runFinished(Outcome outcome) {
                    events.add("finished " + outcome.failure());
                }

                @Override
                public void reset() {
                    events.add("reset");
                }
            });
            controller.setLevel(LEVEL);
        });
        events.clear();
    }

    private static void onEdt(Runnable r) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(r);
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + 5_000_000_000L;
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < end, "timed out");
            Thread.sleep(10);
        }
    }

    @Test
    void parseErrorStaysInEditingAndReportsMessage() throws Exception {
        view.source = "MOVE\nMOVE $";
        onEdt(controller::onRun);

        assertEquals(ExecState.EDITING, controller.state());
        assertEquals(ExecState.EDITING, view.state);
        assertTrue(view.status.startsWith("2:"), view.status);
        assertEquals(1, view.highlights.size());
        assertTrue(view.highlights.get(0).startsWith("ERROR@2:"), view.highlights.get(0));
    }

    @Test
    void checkErrorStaysInEditing() throws Exception {
        view.source = "TURN LEFT";
        onEdt(controller::onStep);

        assertEquals(ExecState.EDITING, controller.state());
        assertNotNull(view.status);
        assertTrue(view.status.contains("not available in this level"), view.status);
        assertEquals(1, view.highlights.size());
    }

    @Test
    void instantRunSucceedsAndEditResets() throws Exception {
        view.source = "MOVE\nMOVE\nMOVE\nMOVE\nMOVE";
        view.speed = 0;
        onEdt(controller::onRun);
        await(() -> controller.state() == ExecState.SUCCEEDED);

        assertEquals("Solved! ★★★ · 5 instructions · 5 actions", view.status);
        assertEquals(List.of("finished null"), events);
        assertEquals(ExecState.SUCCEEDED, view.state);

        onEdt(controller::onEdit);
        assertEquals(ExecState.EDITING, controller.state());
        assertEquals(List.of("finished null", "reset"), events);
    }

    @Test
    void failureHighlightsLocationAndResetReturnsToEditing() throws Exception {
        view.source = "MOVE\nMOVE\nMOVE\nMOVE\nMOVE\nMOVE";
        view.speed = 0;
        onEdt(controller::onRun);
        await(() -> controller.state() == ExecState.FAILED);

        assertTrue(view.status.contains("crashed"), view.status);
        assertEquals(List.of("ERROR@6:1"), view.highlights);
        assertEquals(List.of("finished " + FailureKind.CRASH), events);

        onEdt(controller::onReset);
        assertEquals(ExecState.EDITING, controller.state());
        assertTrue(view.highlights.isEmpty());
    }

    @Test
    void stepPausesAfterOneActionThenStopEndsRun() throws Exception {
        view.source = "MOVE\nMOVE\nMOVE\nMOVE\nMOVE";
        view.speed = 20;
        onEdt(controller::onStep);
        await(() -> controller.state() == ExecState.PAUSED);
        assertEquals(ExecState.PAUSED, view.state);

        onEdt(controller::onStop);
        await(() -> controller.state() == ExecState.FAILED);
        assertEquals(List.of("finished " + FailureKind.STOPPED), events);
    }

    @Test
    void selectingAnotherLevelStopsAndResets() throws Exception {
        view.source = "MOVE\nMOVE\nMOVE\nMOVE\nMOVE";
        view.speed = 20;
        onEdt(controller::onStep);
        await(() -> controller.state() == ExecState.PAUSED);

        onEdt(() -> controller.setLevel(LEVEL));

        assertEquals(ExecState.EDITING, controller.state());
        assertEquals(List.of("reset"), events);
    }
}

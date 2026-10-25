package robolang.ui.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.AstNode;
import robolang.lang.ast.Program;
import robolang.lang.parser.Parser;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.FailureKind;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.LevelLoader;
import robolang.model.WorldSnapshot;

class PacingListenerTest {
    /** Level 01: five MOVEs reach the goal. */
    private static final Level LEVEL = LevelLoader.loadAll().get(0);
    private static final Program FIVE_MOVES = Parser.parse("MOVE\nMOVE\nMOVE\nMOVE\nMOVE");

    private static final class Recorder implements PacingListener.Sink {
        final Semaphore pauses = new Semaphore(0);
        final List<Integer> actions = Collections.synchronizedList(new ArrayList<>());
        int variants;

        @Override
        public void variantStarted(int variant, WorldSnapshot initial) {
            variants++;
        }

        @Override
        public void nodeEntered(AstNode node) {
        }

        @Override
        public void actionPerformed(WorldSnapshot after, int count, long ticks) {
            actions.add(count);
        }

        @Override
        public void paused() {
            pauses.release();
        }

        void awaitPause() throws InterruptedException {
            assertTrue(pauses.tryAcquire(5, TimeUnit.SECONDS), "worker did not pause");
        }
    }

    private static CompletableFuture<Outcome> runAsync(PacingListener pacer) {
        return CompletableFuture.supplyAsync(
                () -> ProgramRunner.run(FIVE_MOVES, LEVEL, ExecutionLimits.DEFAULT, pacer));
    }

    @Test
    void stepReleasesExactlyOneAction() throws Exception {
        Recorder rec = new Recorder();
        PacingListener pacer = new PacingListener(rec, () -> 20, true, ms -> { });
        CompletableFuture<Outcome> run = runAsync(pacer);

        rec.awaitPause();
        assertEquals(1, pacer.actions());
        pacer.step();
        rec.awaitPause();
        assertEquals(2, pacer.actions());
        assertEquals(List.of(1, 2), rec.actions);

        pacer.stop();
        assertEquals(FailureKind.STOPPED, run.get(5, TimeUnit.SECONDS).failure());
        assertEquals(2, pacer.actions());
    }

    @Test
    void stopWhilePausedEndsAsStopped() throws Exception {
        Recorder rec = new Recorder();
        PacingListener pacer = new PacingListener(rec, () -> 20, true, ms -> { });
        CompletableFuture<Outcome> run = runAsync(pacer);
        rec.awaitPause();

        pacer.stop();
        Outcome outcome = run.get(5, TimeUnit.SECONDS);
        assertEquals(FailureKind.STOPPED, outcome.failure());
        assertEquals(1, outcome.totalActions());
    }

    @Test
    void pauseThenResumeFinishesTheRun() throws Exception {
        Recorder rec = new Recorder();
        PacingListener pacer = new PacingListener(rec, () -> 20, false, ms -> { });
        pacer.pause();
        CompletableFuture<Outcome> run = runAsync(pacer);
        rec.awaitPause();
        assertEquals(1, pacer.actions());

        pacer.resume();
        Outcome outcome = run.get(5, TimeUnit.SECONDS);
        assertTrue(outcome.success());
        assertEquals(5, outcome.totalActions());
    }

    @Test
    void instantNeverSleepsAndPublishesNoActions() {
        Recorder rec = new Recorder();
        List<Long> sleeps = new ArrayList<>();
        PacingListener pacer = new PacingListener(rec, () -> 0, false, sleeps::add);

        Outcome outcome = ProgramRunner.run(FIVE_MOVES, LEVEL, ExecutionLimits.DEFAULT, pacer);

        assertTrue(outcome.success());
        assertTrue(sleeps.isEmpty());
        assertTrue(rec.actions.isEmpty());
        assertEquals(0, rec.variants);
        assertEquals(5, pacer.actions());
        assertEquals(outcome.totalTicks(), pacer.ticks());
    }

    @Test
    void sleepsOneOverSpeedPerAction() {
        List<Long> sleeps = new ArrayList<>();
        PacingListener pacer = new PacingListener(new Recorder(), () -> 4, false, sleeps::add);

        ProgramRunner.run(FIVE_MOVES, LEVEL, ExecutionLimits.DEFAULT, pacer);

        assertEquals(List.of(250L, 250L, 250L, 250L, 250L), sleeps);
    }

    @Test
    void animationFollowsSpeed() {
        assertEquals(0, PacingListener.animationMillis(0));
        assertEquals(250, PacingListener.animationMillis(1));
        assertEquals(200, PacingListener.animationMillis(4));
        assertEquals(40, PacingListener.animationMillis(20));
    }
}

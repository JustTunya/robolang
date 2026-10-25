package robolang.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import robolang.lang.runtime.Outcome;

class ProgramAutosaveTest {

    /** Collects scheduled tasks; the test fires them by hand instead of waiting. */
    private static final class FakeScheduler implements Debouncer.Scheduler {
        final List<Runnable> pending = new ArrayList<>();
        long lastDelay;

        @Override
        public Runnable schedule(Runnable task, long delayMillis) {
            lastDelay = delayMillis;
            pending.add(task);
            return () -> pending.remove(task);
        }

        void elapse() {
            new ArrayList<>(pending).forEach(Runnable::run);
            pending.clear();
        }
    }

    private static final class FakeStore implements ProgressPort {
        final Map<String, String> programs = new HashMap<>();
        final List<String> saves = new ArrayList<>();

        @Override
        public OptionalInt bestStars(String levelId) {
            return OptionalInt.empty();
        }

        @Override
        public void record(String levelId, Outcome outcome) {
        }

        @Override
        public Optional<String> lastProgram(String levelId) {
            return Optional.ofNullable(programs.get(levelId));
        }

        @Override
        public void saveProgram(String levelId, String source) {
            saves.add(levelId + "=" + source);
            programs.put(levelId, source);
        }

        @Override
        public void resetAll() {
            programs.clear();
        }
    }

    private final FakeScheduler scheduler = new FakeScheduler();
    private final FakeStore store = new FakeStore();
    private String text = "";
    private final ProgramAutosave autosave = new ProgramAutosave(store, scheduler, () -> text);

    @Test
    void savesOnceAfterTheQuietSecond() {
        autosave.switchTo("L1");
        text = "M";
        autosave.edited();
        text = "MO";
        autosave.edited();
        text = "MOVE";
        autosave.edited();

        assertEquals(1, scheduler.pending.size(), "each edit restarts the single pending save");
        assertEquals(1000, scheduler.lastDelay);
        assertTrue(store.saves.isEmpty());
        scheduler.elapse();
        assertEquals(List.of("L1=MOVE"), store.saves);
    }

    @Test
    void levelSwitchSavesPendingTextUnderTheOldLevel() {
        autosave.switchTo("L1");
        text = "MOVE";
        autosave.edited();

        autosave.switchTo("L2");

        assertEquals(List.of("L1=MOVE"), store.saves);
        assertTrue(scheduler.pending.isEmpty(), "flushed save is not saved again");
    }

    @Test
    void levelSwitchWithoutEditsSavesNothing() {
        autosave.switchTo("L1");
        autosave.switchTo("L2");
        assertTrue(store.saves.isEmpty());
    }

    @Test
    void cancelDropsThePendingSave() {
        autosave.switchTo("L1");
        autosave.edited();
        autosave.cancel();
        scheduler.elapse();
        assertTrue(store.saves.isEmpty());
    }

    @Test
    void editsWithoutAnOpenLevelAreIgnored() {
        autosave.edited();
        assertTrue(scheduler.pending.isEmpty());
    }
}

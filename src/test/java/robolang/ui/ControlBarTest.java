package robolang.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import robolang.ui.ControlBar.Enablement;

class ControlBarTest {

    /** SPEC §7.3, columns run, step, pause, stop, reset. */
    @Test
    void enablementMatchesSpecTable() {
        assertEquals(new Enablement(true, true, false, false, false), ControlBar.enablementFor(ExecState.EDITING));
        assertEquals(new Enablement(false, false, true, true, true), ControlBar.enablementFor(ExecState.RUNNING));
        assertEquals(new Enablement(true, true, false, true, true), ControlBar.enablementFor(ExecState.PAUSED));
        assertEquals(new Enablement(false, false, false, false, true), ControlBar.enablementFor(ExecState.SUCCEEDED));
        assertEquals(new Enablement(false, false, false, false, true), ControlBar.enablementFor(ExecState.FAILED));
    }

    @ParameterizedTest
    @EnumSource(ExecState.class)
    void applyStateSetsButtons(ExecState state) {
        ControlBar bar = new ControlBar();
        bar.applyState(state);
        Enablement e = ControlBar.enablementFor(state);
        assertEquals(e.run(), bar.runButton().isEnabled());
        assertEquals(e.step(), bar.stepButton().isEnabled());
        assertEquals(e.pause(), bar.pauseButton().isEnabled());
        assertEquals(e.stop(), bar.stopButton().isEnabled());
        assertEquals(e.reset(), bar.resetButton().isEnabled());
    }

    @Test
    void speedDefaultsToFourAndLastPositionIsInstant() {
        ControlBar bar = new ControlBar();
        assertEquals(1, bar.speedSlider().getMinimum());
        assertEquals(4, bar.speed());
        bar.speedSlider().setValue(20);
        assertEquals(20, bar.speed());
        bar.speedSlider().setValue(bar.speedSlider().getMaximum());
        assertEquals(0, bar.speed());
    }

    @Test
    void buttonsCallListener() {
        ControlBar bar = new ControlBar();
        List<String> calls = new ArrayList<>();
        bar.addControlListener(new ControlListener() {
            @Override
            public void onRun() {
                calls.add("run");
            }

            @Override
            public void onStep() {
                calls.add("step");
            }

            @Override
            public void onPause() {
                calls.add("pause");
            }

            @Override
            public void onStop() {
                calls.add("stop");
            }

            @Override
            public void onReset() {
                calls.add("reset");
            }
        });
        bar.runButton().doClick();
        bar.stepButton().setEnabled(true);
        bar.stepButton().doClick();
        bar.pauseButton().setEnabled(true);
        bar.pauseButton().doClick();
        bar.stopButton().setEnabled(true);
        bar.stopButton().doClick();
        bar.resetButton().setEnabled(true);
        bar.resetButton().doClick();
        assertEquals(List.of("run", "step", "pause", "stop", "reset"), calls);
    }

    @Test
    void countersAreShown() {
        ControlBar bar = new ControlBar();
        bar.setCounters(3, 9);
        assertTrue(bar.countersText().contains("3"));
        assertTrue(bar.countersText().contains("9"));
    }
}

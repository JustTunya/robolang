package robolang.ui.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import robolang.ui.ExecState;
import robolang.ui.execution.ExecutionStateMachine.Event;

class ExecutionStateMachineTest {
    private static final ExecState E = ExecState.EDITING;
    private static final ExecState R = ExecState.RUNNING;
    private static final ExecState P = ExecState.PAUSED;
    private static final ExecState S = ExecState.SUCCEEDED;
    private static final ExecState F = ExecState.FAILED;

    /** SPEC §7.3: every legal transition; all other (state, event) pairs must be ignored. */
    private static final Map<ExecState, Map<Event, ExecState>> LEGAL = Map.of(
            E, Map.of(Event.RUN, R, Event.STEP, R),
            R, Map.of(Event.PAUSE, P, Event.SUCCEED, S, Event.FAIL, F, Event.RESET, E),
            P, Map.of(Event.RUN, R, Event.STEP, R, Event.FAIL, F, Event.RESET, E),
            S, Map.of(Event.EDIT, E, Event.RESET, E),
            F, Map.of(Event.EDIT, E, Event.RESET, E));

    private static ExecutionStateMachine in(ExecState target, List<ExecState> changes) {
        ExecutionStateMachine m = new ExecutionStateMachine(changes::add);
        switch (target) {
            case EDITING -> { }
            case RUNNING -> m.fire(Event.RUN);
            case PAUSED -> {
                m.fire(Event.RUN);
                m.fire(Event.PAUSE);
            }
            case SUCCEEDED -> {
                m.fire(Event.RUN);
                m.fire(Event.SUCCEED);
            }
            case FAILED -> {
                m.fire(Event.RUN);
                m.fire(Event.FAIL);
            }
        }
        changes.clear();
        assertEquals(target, m.state());
        return m;
    }

    @ParameterizedTest
    @EnumSource(ExecState.class)
    void everyEventFollowsTheTableOrIsIgnored(ExecState from) {
        for (Event event : Event.values()) {
            List<ExecState> changes = new ArrayList<>();
            ExecutionStateMachine m = in(from, changes);
            ExecState expected = LEGAL.get(from).get(event);
            boolean accepted = m.fire(event);
            if (expected == null) {
                assertFalse(accepted, from + " + " + event + " must be ignored");
                assertEquals(from, m.state());
                assertTrue(changes.isEmpty(), "no notification for an ignored event");
            } else {
                assertTrue(accepted, from + " + " + event);
                assertEquals(expected, m.state());
                assertEquals(List.of(expected), changes);
            }
        }
    }

    @Test
    void editableOnlyOutsideRunningAndPaused() {
        assertTrue(ExecutionStateMachine.editable(E));
        assertFalse(ExecutionStateMachine.editable(R));
        assertFalse(ExecutionStateMachine.editable(P));
        assertTrue(ExecutionStateMachine.editable(S));
        assertTrue(ExecutionStateMachine.editable(F));
    }
}

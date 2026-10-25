package robolang.ui.execution;

import java.util.Map;
import java.util.function.Consumer;
import robolang.ui.ExecState;

/** The execution state machine of SPEC §7.3. Pure logic: events that do not apply to a state are ignored. */
public final class ExecutionStateMachine {

    /** Things that can happen to the machine. */
    public enum Event {
        /** Run or resume. */
        RUN,
        /** Single step. */
        STEP,
        /** The worker reached a pause point. */
        PAUSE,
        /** The run ended with success. */
        SUCCEED,
        /** The run ended with a failure, including Stop. */
        FAIL,
        /** Reset, or another level was selected. */
        RESET,
        /** The text was edited. */
        EDIT
    }

    private static final Map<ExecState, Map<Event, ExecState>> TRANSITIONS = Map.of(
            ExecState.EDITING, Map.of(Event.RUN, ExecState.RUNNING, Event.STEP, ExecState.RUNNING),
            ExecState.RUNNING, Map.of(Event.PAUSE, ExecState.PAUSED, Event.SUCCEED, ExecState.SUCCEEDED,
                    Event.FAIL, ExecState.FAILED, Event.RESET, ExecState.EDITING),
            ExecState.PAUSED, Map.of(Event.RUN, ExecState.RUNNING, Event.STEP, ExecState.RUNNING,
                    Event.FAIL, ExecState.FAILED, Event.RESET, ExecState.EDITING),
            ExecState.SUCCEEDED, Map.of(Event.EDIT, ExecState.EDITING, Event.RESET, ExecState.EDITING),
            ExecState.FAILED, Map.of(Event.EDIT, ExecState.EDITING, Event.RESET, ExecState.EDITING));

    private final Consumer<ExecState> onChange;
    private ExecState state = ExecState.EDITING;

    /**
     * Creates a machine in {@link ExecState#EDITING}.
     *
     * @param onChange called with the new state after every accepted event
     */
    public ExecutionStateMachine(Consumer<ExecState> onChange) {
        this.onChange = onChange;
    }

    /**
     * Returns the current state.
     *
     * @return the state
     */
    public ExecState state() {
        return state;
    }

    /**
     * Applies an event.
     *
     * @param event what happened
     * @return true if the state changed, false if the event does not apply to the current state
     */
    public boolean fire(Event event) {
        ExecState next = TRANSITIONS.get(state).get(event);
        if (next == null) {
            return false;
        }
        state = next;
        onChange.accept(next);
        return true;
    }

    /**
     * Tells whether the editor may be edited (SPEC §7.3).
     *
     * @param state a state
     * @return false in Running and Paused
     */
    public static boolean editable(ExecState state) {
        return state != ExecState.RUNNING && state != ExecState.PAUSED;
    }
}

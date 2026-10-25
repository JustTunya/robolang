package robolang.ui;

/** The five execution states of the UI (SPEC §7.3). */
public enum ExecState {
    /** The program is being edited; nothing runs. */
    EDITING,
    /** The program is running. */
    RUNNING,
    /** The run is paused and can be resumed or stepped. */
    PAUSED,
    /** The last run solved every variant. */
    SUCCEEDED,
    /** The last run failed. */
    FAILED
}

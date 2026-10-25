package robolang.ui;

/** Receives the clicks of the control bar buttons. */
public interface ControlListener {

    /** Run or resume was requested. */
    void onRun();

    /** A single step was requested. */
    void onStep();

    /** Pause was requested. */
    void onPause();

    /** Stop was requested. */
    void onStop();

    /** Reset was requested. */
    void onReset();
}

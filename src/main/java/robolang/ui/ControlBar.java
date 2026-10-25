package robolang.ui;

import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

/** Run, Step, Pause, Stop and Reset buttons, the speed slider and the live counters (SPEC §7.2). */
@SuppressWarnings("serial")
public final class ControlBar extends JToolBar {
    private static final int MAX_SPEED = 20;
    private static final int INSTANT = MAX_SPEED + 1;
    private static final int DEFAULT_SPEED = 4;

    /**
     * Which buttons are enabled in one execution state.
     *
     * @param run Run / Resume
     * @param step Step
     * @param pause Pause
     * @param stop Stop
     * @param reset Reset
     */
    public record Enablement(boolean run, boolean step, boolean pause, boolean stop, boolean reset) {
    }

    private final JButton run = new JButton("Run");
    private final JButton step = new JButton("Step");
    private final JButton pause = new JButton("Pause");
    private final JButton stop = new JButton("Stop");
    private final JButton reset = new JButton("Reset");
    private final JSlider speed = new JSlider(1, INSTANT, DEFAULT_SPEED);
    private final JLabel speedLabel = new JLabel();
    private final JLabel counters = new JLabel();

    /** Creates the bar in the {@link ExecState#EDITING} state. */
    public ControlBar() {
        setFloatable(false);
        add(run);
        add(step);
        add(pause);
        add(stop);
        add(reset);
        addSeparator();
        add(new JLabel("Speed "));
        speed.setPaintLabels(true);
        speed.setMajorTickSpacing(5);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(1, new JLabel("1"));
        labels.put(INSTANT, new JLabel("Instant"));
        speed.setLabelTable(labels);
        speed.setMaximumSize(speed.getPreferredSize());
        speed.addChangeListener(e -> updateSpeedLabel());
        add(speed);
        add(speedLabel);
        addSeparator();
        add(counters);
        updateSpeedLabel();
        setCounters(0, 0);
        applyState(ExecState.EDITING);
    }

    /**
     * The enablement table of SPEC §7.3.
     *
     * @param state an execution state
     * @return which buttons are enabled in that state
     */
    public static Enablement enablementFor(ExecState state) {
        return switch (state) {
            case EDITING -> new Enablement(true, true, false, false, false);
            case RUNNING -> new Enablement(false, false, true, true, true);
            case PAUSED -> new Enablement(true, true, false, true, true);
            case SUCCEEDED, FAILED -> new Enablement(false, false, false, false, true);
        };
    }

    /**
     * Enables and disables the buttons for a state.
     *
     * @param state the new execution state
     */
    public void applyState(ExecState state) {
        Enablement e = enablementFor(state);
        run.setEnabled(e.run());
        step.setEnabled(e.step());
        pause.setEnabled(e.pause());
        stop.setEnabled(e.stop());
        reset.setEnabled(e.reset());
    }

    /**
     * Connects the buttons to a listener.
     *
     * @param listener receives the button clicks
     */
    public void addControlListener(ControlListener listener) {
        run.addActionListener(e -> listener.onRun());
        step.addActionListener(e -> listener.onStep());
        pause.addActionListener(e -> listener.onPause());
        stop.addActionListener(e -> listener.onStop());
        reset.addActionListener(e -> listener.onReset());
    }

    /**
     * Returns the chosen speed.
     *
     * @return actions per second from 1 to 20, or 0 for Instant
     */
    public int speed() {
        return speed.getValue() == INSTANT ? 0 : speed.getValue();
    }

    /**
     * Shows the live counters.
     *
     * @param actions actions performed so far
     * @param ticks ticks consumed so far
     */
    public void setCounters(int actions, long ticks) {
        counters.setText("Actions " + actions + "   Ticks " + ticks);
    }

    /**
     * Returns the Run button, for tests and key bindings.
     *
     * @return the button
     */
    public JButton runButton() {
        return run;
    }

    /**
     * Returns the Step button.
     *
     * @return the button
     */
    public JButton stepButton() {
        return step;
    }

    /**
     * Returns the Pause button.
     *
     * @return the button
     */
    public JButton pauseButton() {
        return pause;
    }

    /**
     * Returns the Stop button.
     *
     * @return the button
     */
    public JButton stopButton() {
        return stop;
    }

    /**
     * Returns the Reset button.
     *
     * @return the button
     */
    public JButton resetButton() {
        return reset;
    }

    /**
     * Returns the speed slider.
     *
     * @return the slider; its last position is Instant
     */
    public JSlider speedSlider() {
        return speed;
    }

    /**
     * Returns the counters text.
     *
     * @return e.g. {@code Actions 3   Ticks 9}
     */
    public String countersText() {
        return counters.getText();
    }

    private void updateSpeedLabel() {
        speedLabel.setText(speed() == 0 ? " Instant" : " " + speed() + "/s");
    }
}

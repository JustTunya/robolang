package robolang.ui.execution;

import robolang.lang.ast.SourceRange;
import robolang.model.Level;
import robolang.model.WorldSnapshot;
import robolang.ui.CodeEditorPanel.HighlightKind;
import robolang.ui.ExecState;

/** What the execution controller needs from the UI. All methods are called on the event dispatch thread. */
public interface ExecutionView {

    /**
     * Returns the program text.
     *
     * @return the editor content
     */
    String source();

    /**
     * Returns the chosen speed.
     *
     * @return actions per second, 0 for Instant
     */
    int speed();

    /**
     * Shows the state: control bar buttons and the editor's read-only flag.
     *
     * @param state the new state
     */
    void applyState(ExecState state);

    /**
     * Shows a message in the status bar.
     *
     * @param text the message
     */
    void setStatus(String text);

    /**
     * Highlights a source range in the editor.
     *
     * @param range the range
     * @param kind what it means
     */
    void highlight(SourceRange range, HighlightKind kind);

    /** Removes all editor highlights. */
    void clearHighlights();

    /**
     * Shows a world state in the grid.
     *
     * @param snapshot the state
     * @param animationMillis length of the robot animation, 0 to jump
     */
    void showSnapshot(WorldSnapshot snapshot, int animationMillis);

    /**
     * Shows the start state of a level's first variant without animation.
     *
     * @param level the level, or null to clear the grid
     */
    void showLevelStart(Level level);

    /**
     * Selects a variant tab in the grid.
     *
     * @param index zero-based variant index
     */
    void selectVariant(int index);

    /**
     * Shows the live counters.
     *
     * @param actions actions so far
     * @param ticks ticks so far
     */
    void setCounters(int actions, long ticks);
}

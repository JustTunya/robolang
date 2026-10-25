package robolang.ui.execution;

import robolang.lang.ast.SourceRange;
import robolang.model.Level;
import robolang.model.Variant;
import robolang.model.WorldSnapshot;
import robolang.ui.CodeEditorPanel;
import robolang.ui.CodeEditorPanel.HighlightKind;
import robolang.ui.ControlBar;
import robolang.ui.ExecState;
import robolang.ui.GridPanel;
import robolang.ui.StatusBar;

/** The {@link ExecutionView} backed by the real panels. */
public final class SwingExecutionView implements ExecutionView {
    private final CodeEditorPanel editor;
    private final GridPanel grid;
    private final ControlBar controls;
    private final StatusBar statusBar;

    /**
     * Creates the view.
     *
     * @param editor the code editor
     * @param grid the grid view
     * @param controls the control bar
     * @param statusBar the status bar
     */
    public SwingExecutionView(CodeEditorPanel editor, GridPanel grid, ControlBar controls, StatusBar statusBar) {
        this.editor = editor;
        this.grid = grid;
        this.controls = controls;
        this.statusBar = statusBar;
    }

    @Override
    public String source() {
        return editor.getProgram();
    }

    @Override
    public int speed() {
        return controls.speed();
    }

    @Override
    public void applyState(ExecState state) {
        controls.applyState(state);
        editor.setReadOnly(!ExecutionStateMachine.editable(state));
    }

    @Override
    public void setStatus(String text) {
        statusBar.setMessage(text);
    }

    @Override
    public void highlight(SourceRange range, HighlightKind kind) {
        editor.highlightRange(range, kind);
    }

    @Override
    public void clearHighlights() {
        editor.clearHighlights();
    }

    @Override
    public void showSnapshot(WorldSnapshot snapshot, int animationMillis) {
        grid.setAnimationMillis(animationMillis);
        grid.showSnapshot(snapshot);
    }

    @Override
    public void showLevelStart(Level level) {
        if (level == null || level.variants().isEmpty()) {
            grid.setVariantCount(0);
            grid.showSnapshot(null);
            return;
        }
        Variant v = level.variants().get(0);
        grid.setVariantCount(level.variants().size());
        showSnapshot(new WorldSnapshot(v.grid(), v.start(), v.facing(), v.grid().initialItems()), 0);
    }

    @Override
    public void selectVariant(int index) {
        grid.selectVariant(index);
    }

    @Override
    public void setCounters(int actions, long ticks) {
        controls.setCounters(actions, ticks);
    }
}

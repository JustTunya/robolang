package robolang.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingWorker;
import robolang.lang.ast.AstNode;
import robolang.lang.ast.Program;
import robolang.lang.error.RoboLangException;
import robolang.lang.parser.Parser;
import robolang.lang.parser.StaticChecker;
import robolang.lang.runtime.ExecutionLimits;
import robolang.lang.runtime.Outcome;
import robolang.lang.runtime.ProgramRunner;
import robolang.model.Level;
import robolang.model.WorldSnapshot;
import robolang.ui.CodeEditorPanel.HighlightKind;
import robolang.ui.execution.ExecutionStateMachine;
import robolang.ui.execution.ExecutionStateMachine.Event;
import robolang.ui.execution.ExecutionView;
import robolang.ui.execution.PacingListener;

/**
 * Connects editor, parser, checker, runtime and grid (SPEC §7.3, §10.5). All methods are called on the event
 * dispatch thread; the program runs on a {@link SwingWorker}.
 */
public final class ExecutionController implements ControlListener {

    private sealed interface Chunk {
    }

    private record VariantChunk(int index, WorldSnapshot initial) implements Chunk {
    }

    private record NodeChunk() implements Chunk {
    }

    private record ActionChunk(WorldSnapshot after, int actions, long ticks) implements Chunk {
    }

    private record PausedChunk() implements Chunk {
    }

    private final ExecutionView view;
    private final List<ExecutionObserver> observers = new ArrayList<>();
    private final ExecutionStateMachine machine;
    private Level level;
    private Worker current;

    /**
     * Creates a controller in the Editing state.
     *
     * @param view the UI to drive
     */
    public ExecutionController(ExecutionView view) {
        this.view = view;
        this.machine = new ExecutionStateMachine(view::applyState);
        view.applyState(ExecState.EDITING);
    }

    /**
     * Registers an observer.
     *
     * @param observer receives execution events on the event dispatch thread
     */
    public void addObserver(ExecutionObserver observer) {
        observers.add(observer);
    }

    /**
     * Returns the current state.
     *
     * @return the state
     */
    public ExecState state() {
        return machine.state();
    }

    /**
     * Selects the level to run on: stops a running program and resets.
     *
     * @param newLevel the level, or null for none
     */
    public void setLevel(Level newLevel) {
        level = newLevel;
        stopCurrent();
        machine.fire(Event.RESET);
        resetWorld();
    }

    /** Tells the controller that the user edited the program text. */
    public void onEdit() {
        if (machine.fire(Event.EDIT)) {
            resetWorld();
        } else if (machine.state() == ExecState.EDITING) {
            view.clearHighlights();
        }
    }

    @Override
    public void onRun() {
        if (machine.state() == ExecState.EDITING) {
            start(false);
        } else if (machine.state() == ExecState.PAUSED && machine.fire(Event.RUN)) {
            current.pacer.resume();
        }
    }

    @Override
    public void onStep() {
        if (machine.state() == ExecState.EDITING) {
            start(true);
        } else if (machine.state() == ExecState.PAUSED && machine.fire(Event.STEP)) {
            current.pacer.step();
        }
    }

    @Override
    public void onPause() {
        if (machine.state() == ExecState.RUNNING) {
            current.pacer.pause();
        }
    }

    @Override
    public void onStop() {
        if (current != null && (machine.state() == ExecState.RUNNING || machine.state() == ExecState.PAUSED)) {
            current.pacer.stop();
        }
    }

    @Override
    public void onReset() {
        stopCurrent();
        if (machine.fire(Event.RESET)) {
            resetWorld();
        }
    }

    private void start(boolean step) {
        if (level == null) {
            view.setStatus("No level selected");
            return;
        }
        Program program;
        try {
            program = Parser.parse(view.source());
            StaticChecker.check(program, level.keywords());
        } catch (RoboLangException e) {
            view.clearHighlights();
            if (e.range() != null) {
                view.highlight(e.range(), HighlightKind.ERROR);
            }
            view.setStatus(e.formatted());
            return;
        } catch (RuntimeException e) {
            internalError(e);
            return;
        }
        view.clearHighlights();
        view.setCounters(0, 0);
        view.setStatus("Running");
        machine.fire(step ? Event.STEP : Event.RUN);
        observers.forEach(o -> o.programParsed(program));
        current = new Worker(program, level, step);
        current.execute();
    }

    private void stopCurrent() {
        if (current != null) {
            current.pacer.stop();
            current = null;
        }
    }

    private void resetWorld() {
        view.clearHighlights();
        view.showLevelStart(level);
        view.setCounters(0, 0);
        view.setStatus(level == null ? " " : level.title());
        observers.forEach(ExecutionObserver::reset);
    }

    private void finish(Outcome outcome) {
        view.clearHighlights();
        view.selectVariant(current.pacer.variant());
        view.setCounters(current.pacer.actions(), current.pacer.ticks());
        if (current.shownActions != current.pacer.actions()) {
            view.showSnapshot(current.pacer.lastSnapshot(), 0);
        }
        current = null;
        observers.forEach(o -> o.runFinished(outcome));
        if (outcome.success()) {
            machine.fire(Event.SUCCEED);
            view.setStatus("Solved! " + "★".repeat(outcome.stars()) + "☆".repeat(3 - outcome.stars()) + " · "
                    + outcome.instructionCount() + " instructions · " + outcome.totalActions() + " actions");
        } else {
            machine.fire(Event.FAIL);
            if (outcome.location() != null) {
                view.highlight(outcome.location(), HighlightKind.ERROR);
            }
            view.setStatus(outcome.message());
        }
    }

    // SPEC §11: the stack trace goes to the log file and the application keeps running.
    private void internalError(Throwable t) {
        current = null;
        machine.fire(Event.RESET);
        resetWorld();
        try {
            StringWriter trace = new StringWriter();
            t.printStackTrace(new PrintWriter(trace));
            Path log = Path.of(System.getProperty("user.home"), ".robolang", "error.log");
            Files.createDirectories(log.getParent());
            Files.writeString(log, trace.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException e) {
            // nothing more can be done
        }
        view.setStatus("Internal error, details written to ~/.robolang/error.log");
    }

    private final class Worker extends SwingWorker<Outcome, Chunk> implements PacingListener.Sink {
        private final Program program;
        private final Level runLevel;
        private final PacingListener pacer;
        private final AtomicReference<AstNode> pendingNode = new AtomicReference<>();
        private int shownActions;

        Worker(Program program, Level runLevel, boolean step) {
            this.program = program;
            this.runLevel = runLevel;
            this.pacer = new PacingListener(this, view::speed, step);
        }

        @Override
        protected Outcome doInBackground() {
            return ProgramRunner.run(program, runLevel, ExecutionLimits.DEFAULT, pacer);
        }

        @Override
        public void variantStarted(int variant, WorldSnapshot initial) {
            publish(new VariantChunk(variant, initial));
        }

        // Coalesced: only the latest node is delivered, so the EDT is never flooded.
        @Override
        public void nodeEntered(AstNode node) {
            if (pendingNode.getAndSet(node) == null) {
                publish(new NodeChunk());
            }
        }

        @Override
        public void actionPerformed(WorldSnapshot after, int actions, long ticks) {
            publish(new ActionChunk(after, actions, ticks));
        }

        @Override
        public void paused() {
            publish(new PausedChunk());
        }

        @Override
        protected void process(List<Chunk> chunks) {
            if (current != this) {
                return;
            }
            for (Chunk chunk : chunks) {
                flushNode();
                switch (chunk) {
                    case VariantChunk v -> {
                        view.selectVariant(v.index());
                        view.showSnapshot(v.initial(), 0);
                        view.setStatus("Variant " + (v.index() + 1) + " running");
                    }
                    case NodeChunk n -> {
                    }
                    case ActionChunk a -> {
                        shownActions = a.actions();
                        view.showSnapshot(a.after(), PacingListener.animationMillis(view.speed()));
                        view.setCounters(a.actions(), a.ticks());
                    }
                    case PausedChunk p -> {
                        machine.fire(Event.PAUSE);
                        view.showSnapshot(pacer.lastSnapshot(), PacingListener.animationMillis(view.speed()));
                        view.setCounters(pacer.actions(), pacer.ticks());
                        shownActions = pacer.actions();
                    }
                }
            }
        }

        @Override
        protected void done() {
            if (current != this) {
                return;
            }
            try {
                finish(get());
            } catch (InterruptedException | ExecutionException | RuntimeException e) {
                internalError(e);
            }
        }

        private void flushNode() {
            AstNode node = pendingNode.getAndSet(null);
            if (node != null) {
                view.highlight(node.range(), HighlightKind.EXECUTING);
                observers.forEach(o -> o.nodeEntered(node));
            }
        }
    }
}

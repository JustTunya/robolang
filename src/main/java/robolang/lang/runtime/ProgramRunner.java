package robolang.lang.runtime;

import robolang.lang.ast.Program;
import robolang.model.Level;

/** Runs a program on every variant of a level. */
public final class ProgramRunner {
    private ProgramRunner() {
    }

    /**
     * Runs the program, stopping at the first failing variant. Implemented by T03.
     *
     * @param program the checked program
     * @param level the level to run on
     * @param limits execution limits
     * @param listener observer, may be a no-op
     * @return the outcome
     */
    public static Outcome run(Program program, Level level, ExecutionLimits limits, ExecutionListener listener) {
        throw new UnsupportedOperationException("T03");
    }
}

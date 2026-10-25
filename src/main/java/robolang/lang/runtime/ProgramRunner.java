package robolang.lang.runtime;

import java.util.List;
import robolang.lang.ast.Program;
import robolang.lang.ast.SourceRange;
import robolang.lang.error.ExecutionFailure;
import robolang.lang.error.ExecutionStoppedException;
import robolang.lang.error.RecursionLimitException;
import robolang.lang.error.CrashException;
import robolang.lang.error.EmptyPickException;
import robolang.lang.error.TickLimitException;
import robolang.model.Variant;
import robolang.model.World;
import robolang.model.Level;

/** Runs a program on every variant of a level. */
public final class ProgramRunner {
    private ProgramRunner() {
    }

    /**
     * Runs the program, stopping at the first failing variant.
     *
     * @param program the checked program
     * @param level the level to run on
     * @param limits execution limits
     * @param listener observer, may be a no-op
     * @return the outcome
     */
    public static Outcome run(Program program, Level level, ExecutionLimits limits, ExecutionListener listener) {
        int instructions = program.instructionCount();
        List<Variant> variants = level.variants();
        int actions = 0;
        long ticks = 0;
        for (int i = 0; i < variants.size(); i++) {
            World world = variants.get(i).newWorld();
            Context ctx = new Context(world, limits, listener, i);
            FailureKind kind;
            String message;
            SourceRange where = program.range();
            try {
                listener.variantStarted(i, world.snapshot());
                program.execute(ctx);
                if (world.atGoal() && world.itemsLeft() == 0) {
                    actions += ctx.actions();
                    ticks += ctx.ticks();
                    continue;
                }
                kind = FailureKind.INCOMPLETE;
                message = world.atGoal() ? "program ended with " + world.itemsLeft() + " item(s) left"
                        : "program ended without reaching the goal";
            } catch (ExecutionStoppedException e) {
                kind = FailureKind.STOPPED;
                message = e.getMessage();
                where = e.range();
            } catch (ExecutionFailure e) {
                kind = kindOf(e);
                message = e.getMessage();
                where = e.range();
            }
            actions += ctx.actions();
            ticks += ctx.ticks();
            return new Outcome(false, kind, "Variant " + (i + 1) + ", " + message, where, i, instructions, actions,
                    ticks, 0);
        }
        return new Outcome(true, null, null, null, -1, instructions, actions, ticks,
                Scoring.stars(true, instructions, actions, level));
    }

    // Exceptions, not AST nodes, so FR-30 does not apply.
    private static FailureKind kindOf(ExecutionFailure e) {
        if (e instanceof CrashException) {
            return FailureKind.CRASH;
        } else if (e instanceof EmptyPickException) {
            return FailureKind.EMPTY_PICK;
        } else if (e instanceof TickLimitException) {
            return FailureKind.TICK_LIMIT;
        } else if (e instanceof RecursionLimitException) {
            return FailureKind.RECURSION_LIMIT;
        }
        throw e;
    }
}

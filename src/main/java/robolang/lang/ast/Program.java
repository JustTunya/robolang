package robolang.lang.ast;

import java.util.List;
import robolang.lang.runtime.Context;

/**
 * A whole program: its procedure definitions and the main block (SPEC §10.3).
 */
public record Program(List<ProcDef> procedures, BlockStmt main, SourceRange range) implements AstNode {

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "Program";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        int n = main.instructionCount();
        for (ProcDef p : procedures) {
            n += p.instructionCount();
        }
        return n;
    }

    /**
     * Executes the main block of this program against the context.
     *
     * @param ctx execution context
     */
    public void execute(Context ctx) {
        main.interpret(ctx);
    }
}

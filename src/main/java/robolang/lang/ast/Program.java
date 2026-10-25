package robolang.lang.ast;

import java.util.List;
import robolang.lang.runtime.Context;

/**
 * A whole program: its procedure definitions and the main block (SPEC §10.3).
 */
public record Program(List<ProcDef> procedures, BlockStmt main, SourceRange range) implements AstNode {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public String label() {
        throw new UnsupportedOperationException("T03");
    }

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public int instructionCount() {
        throw new UnsupportedOperationException("T03");
    }

    /**
     * Executes the main block of this program against the context. Implemented by T03.
     *
     * @param ctx execution context
     */
    public void execute(Context ctx) {
        throw new UnsupportedOperationException("T03");
    }
}

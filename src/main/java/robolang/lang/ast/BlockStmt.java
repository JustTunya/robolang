package robolang.lang.ast;

import java.util.List;
import robolang.lang.runtime.Context;

/**
 * A braced sequence of statements (SPEC §4.2).
 */
public record BlockStmt(List<Statement> statements, SourceRange range) implements Statement {

    /** {@inheritDoc} Implemented by T03. */
    @Override
    public void interpret(Context ctx) {
        throw new UnsupportedOperationException("T03");
    }

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
}

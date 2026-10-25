package robolang.lang.ast;

import java.util.List;
import robolang.lang.runtime.Context;

/**
 * A braced sequence of statements (SPEC §4.2).
 */
public record BlockStmt(List<Statement> statements, SourceRange range) implements Statement {

    /** {@inheritDoc} */
    @Override
    public void interpret(Context ctx) {
        for (Statement s : statements) {
            s.interpret(ctx);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String label() {
        return "Block";
    }

    /** {@inheritDoc} */
    @Override
    public int instructionCount() {
        int n = 0;
        for (Statement s : statements) {
            n += s.instructionCount();
        }
        return n;
    }
}

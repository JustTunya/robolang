package robolang.lang.ast;

import robolang.lang.runtime.Context;

/** Procedure call {@code CALL name}. Linked to its definition after parsing (D6). */
public final class CallStmt implements Statement {
    private final String name;
    private final SourceRange range;
    private ProcDef target;

    /**
     * Creates an unbound call.
     *
     * @param name called procedure name
     * @param range source location
     */
    public CallStmt(String name, SourceRange range) {
        this.name = name;
        this.range = range;
    }

    /**
     * Returns the called procedure name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    @Override
    public SourceRange range() {
        return range;
    }

    /**
     * Links this call to its definition.
     *
     * @param target the procedure to call
     * @throws IllegalStateException if already bound
     */
    public void bind(ProcDef target) {
        if (this.target != null) {
            throw new IllegalStateException("CALL " + name + " is already bound");
        }
        this.target = target;
    }

    /**
     * Returns the linked procedure.
     *
     * @return the target, or null before {@link #bind}
     */
    public ProcDef target() {
        return target;
    }

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

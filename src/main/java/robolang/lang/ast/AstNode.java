package robolang.lang.ast;

/** Common type of every AST node (Interpreter: AbstractExpression). */
public sealed interface AstNode permits Statement, BoolExpression, ProcDef, Program {

    /**
     * Returns where this node appears in the source.
     *
     * @return the source range
     */
    SourceRange range();

    /**
     * Returns the text shown for this node in the AST tree. Implemented by T03.
     *
     * @return the label
     */
    String label();

    /**
     * Returns the static instruction count of this subtree (SPEC §3.5). Implemented by T03.
     *
     * @return the number of counted constructs
     */
    int instructionCount();
}

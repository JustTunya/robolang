package robolang.lang.ast;

import robolang.lang.runtime.Context;

/** A node that performs actions (Interpreter: AbstractExpression for statements, D1). */
public sealed interface Statement extends AstNode
        permits MoveStmt, TurnStmt, PickStmt, BlockStmt, RepeatStmt, WhileStmt, IfStmt, CallStmt {

    /**
     * Executes this statement. Implemented by T03.
     *
     * @param ctx execution context
     */
    void interpret(Context ctx);
}

package robolang.lang.ast;

import robolang.lang.runtime.Context;

/** A node that evaluates to a boolean (Interpreter: AbstractExpression for conditions, D1). */
public sealed interface BoolExpression extends AstNode
        permits WallAheadCond, OnItemCond, AtGoalCond, NotCond, AndCond, OrCond {

    /**
     * Evaluates this condition. Implemented by T03.
     *
     * @param ctx execution context
     * @return the truth value
     */
    boolean evaluate(Context ctx);
}

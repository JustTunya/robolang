package robolang.lang.parser;

import java.util.Set;
import robolang.lang.ast.Program;

/** Static rules of SPEC §4.5; links each {@code CALL} to its definition. */
public final class StaticChecker {
    private StaticChecker() {
    }

    /**
     * Checks a program and binds its calls. Implemented by T02.
     *
     * @param program the parsed program
     * @param allowedKeywords upper-case keywords the level allows
     * @throws robolang.lang.error.SemanticException if a rule is broken
     */
    public static void check(Program program, Set<String> allowedKeywords) {
        throw new UnsupportedOperationException("T02");
    }
}

package robolang.lang.parser;

import robolang.lang.ast.Program;

/** Recursive-descent parser for the grammar of SPEC §4.2. */
public final class Parser {
    private Parser() {
    }

    /**
     * Parses source text into an AST. Implemented by T02.
     *
     * @param source program text
     * @return the program
     */
    public static Program parse(String source) {
        throw new UnsupportedOperationException("T02");
    }
}

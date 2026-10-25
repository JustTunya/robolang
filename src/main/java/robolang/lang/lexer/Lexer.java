package robolang.lang.lexer;

import java.util.List;

/** Splits source text into tokens (SPEC §4.1). */
public final class Lexer {

    /**
     * Creates a lexer. Implemented by T02.
     *
     * @param source program text
     */
    public Lexer(String source) {
        throw new UnsupportedOperationException("T02");
    }

    /**
     * Tokenizes the whole source, ending with an {@link TokenType#EOF} token. Implemented by T02.
     *
     * @return the tokens
     */
    public List<Token> tokenize() {
        throw new UnsupportedOperationException("T02");
    }
}

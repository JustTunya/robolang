package robolang.lang.lexer;

/**
 * A lexical token.
 *
 * @param type token kind
 * @param text source text of the token
 * @param line 1-based line
 * @param col 1-based column
 */
public record Token(TokenType type, String text, int line, int col) {
}

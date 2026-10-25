package robolang.lang.lexer;

import java.util.ArrayList;
import java.util.List;
import robolang.lang.ast.SourceRange;
import robolang.lang.error.LexException;

/** Splits source text into tokens (SPEC §4.1). */
public final class Lexer {
    private static final int MAX_SOURCE = 10_000;
    private static final int MAX_IDENT = 32;

    private final String src;
    private int pos;
    private int line = 1;
    private int col = 1;

    /**
     * Creates a lexer.
     *
     * @param source program text
     */
    public Lexer(String source) {
        this.src = source;
    }

    /**
     * Tokenizes the whole source, ending with an {@link TokenType#EOF} token.
     *
     * @return the tokens
     * @throws LexException on an unexpected character, an over-long identifier or over-long source
     */
    public List<Token> tokenize() {
        if (src.length() > MAX_SOURCE) {
            throw new LexException("Source is longer than " + MAX_SOURCE + " characters", new SourceRange(1, 1, 1, 1));
        }
        List<Token> out = new ArrayList<>();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == '\n') {
                advance();
                line++;
                col = 1;
            } else if (Character.isWhitespace(c)) {
                advance();
            } else if (c == '#') {
                while (pos < src.length() && src.charAt(pos) != '\n') {
                    advance();
                }
            } else if (c == '{') {
                out.add(single(TokenType.LBRACE));
            } else if (c == '}') {
                out.add(single(TokenType.RBRACE));
            } else if (c == '(') {
                out.add(single(TokenType.LPAREN));
            } else if (c == ')') {
                out.add(single(TokenType.RPAREN));
            } else if (c >= '0' && c <= '9') {
                out.add(word(TokenType.INT, c1 -> c1 >= '0' && c1 <= '9'));
            } else if (isIdentStart(c)) {
                out.add(identOrKeyword());
            } else {
                throw new LexException("Unexpected character '" + c + "'", new SourceRange(line, col, line, col));
            }
        }
        out.add(new Token(TokenType.EOF, "", line, col));
        return out;
    }

    private void advance() {
        pos++;
        col++;
    }

    private Token single(TokenType type) {
        Token t = new Token(type, String.valueOf(src.charAt(pos)), line, col);
        advance();
        return t;
    }

    private Token word(TokenType type, java.util.function.IntPredicate part) {
        int start = pos;
        int startCol = col;
        while (pos < src.length() && part.test(src.charAt(pos))) {
            advance();
        }
        return new Token(type, src.substring(start, pos), line, startCol);
    }

    private Token identOrKeyword() {
        Token w = word(TokenType.IDENT, Lexer::isIdentPart);
        if (w.text().length() > MAX_IDENT) {
            throw new LexException("Identifier is longer than " + MAX_IDENT + " characters",
                    new SourceRange(w.line(), w.col(), w.line(), w.col() + w.text().length() - 1));
        }
        try {
            TokenType kw = TokenType.valueOf(w.text().toUpperCase(java.util.Locale.ROOT));
            if (kw.isKeyword()) {
                return new Token(kw, w.text(), w.line(), w.col());
            }
        } catch (IllegalArgumentException e) {
            // not a keyword
        }
        return w;
    }

    private static boolean isIdentStart(int c) {
        return c == '_' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isIdentPart(int c) {
        return isIdentStart(c) || (c >= '0' && c <= '9');
    }
}

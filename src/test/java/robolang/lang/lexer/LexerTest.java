package robolang.lang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import robolang.lang.error.LexException;

class LexerTest {
    private static List<Token> lex(String s) {
        return new Lexer(s).tokenize();
    }

    private static List<TokenType> types(String s) {
        return lex(s).stream().map(Token::type).toList();
    }

    @Test
    void everyTokenType() {
        String src = "MOVE TURN LEFT RIGHT PICK REPEAT WHILE IF ELSE DEF CALL NOT AND OR WALL_AHEAD ON_ITEM AT_GOAL"
                + " name 42 { } ( )";
        List<TokenType> expected = List.of(TokenType.MOVE, TokenType.TURN, TokenType.LEFT, TokenType.RIGHT,
                TokenType.PICK, TokenType.REPEAT, TokenType.WHILE, TokenType.IF, TokenType.ELSE, TokenType.DEF,
                TokenType.CALL, TokenType.NOT, TokenType.AND, TokenType.OR, TokenType.WALL_AHEAD, TokenType.ON_ITEM,
                TokenType.AT_GOAL, TokenType.IDENT, TokenType.INT, TokenType.LBRACE, TokenType.RBRACE,
                TokenType.LPAREN, TokenType.RPAREN, TokenType.EOF);
        assertEquals(expected, types(src));
    }

    @Test
    void keywordsAreCaseInsensitiveIdentifiersAreNot() {
        List<Token> t = lex("mOvE Move foo Foo");
        assertEquals(List.of(TokenType.MOVE, TokenType.MOVE, TokenType.IDENT, TokenType.IDENT, TokenType.EOF),
                t.stream().map(Token::type).toList());
        assertEquals("foo", t.get(2).text());
        assertEquals("Foo", t.get(3).text());
    }

    @Test
    void identifierShapes() {
        assertEquals(List.of(TokenType.IDENT, TokenType.IDENT, TokenType.IDENT, TokenType.EOF),
                types("_a1 move_2 WALL_AHEADX"));
    }

    @Test
    void identifierNamedLikeEofOrIdentIsStillAnIdentifier() {
        assertEquals(List.of(TokenType.IDENT, TokenType.IDENT, TokenType.EOF), types("eof ident"));
    }

    @Test
    void commentsAndWhitespaceAreSkipped() {
        assertEquals(List.of(TokenType.MOVE, TokenType.PICK, TokenType.EOF),
                types("MOVE # TURN LEFT\n\t PICK # end"));
    }

    @Test
    void positionsAreOneBased() {
        List<Token> t = lex("MOVE\n  # c\n  TURN LEFT");
        assertEquals(new Token(TokenType.MOVE, "MOVE", 1, 1), t.get(0));
        assertEquals(new Token(TokenType.TURN, "TURN", 3, 3), t.get(1));
        assertEquals(new Token(TokenType.LEFT, "LEFT", 3, 8), t.get(2));
        assertEquals(new Token(TokenType.EOF, "", 3, 12), t.get(3));
    }

    @Test
    void emptySourceYieldsOnlyEof() {
        assertEquals(List.of(new Token(TokenType.EOF, "", 1, 1)), lex(""));
    }

    @Test
    void unexpectedCharacter() {
        LexException e = assertThrows(LexException.class, () -> lex("MOVE $"));
        assertEquals("1:6 Unexpected character '$'", e.formatted());
    }

    @Test
    void identifierLimit() {
        assertEquals(TokenType.IDENT, lex("a".repeat(32)).get(0).type());
        LexException e = assertThrows(LexException.class, () -> lex("MOVE " + "a".repeat(33)));
        assertEquals(1, e.range().startLine());
        assertEquals(6, e.range().startCol());
    }

    @Test
    void sourceLimit() {
        assertEquals(1, lex("#".repeat(10_000)).size());
        assertThrows(LexException.class, () -> lex("#".repeat(10_001)));
    }
}

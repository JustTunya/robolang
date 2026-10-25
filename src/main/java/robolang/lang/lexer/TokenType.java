package robolang.lang.lexer;

/** Token kinds (SPEC §4.1). */
public enum TokenType {
    /** Keyword {@code MOVE}. */
    MOVE(true),
    /** Keyword {@code TURN}. */
    TURN(true),
    /** Keyword {@code LEFT}. */
    LEFT(true),
    /** Keyword {@code RIGHT}. */
    RIGHT(true),
    /** Keyword {@code PICK}. */
    PICK(true),
    /** Keyword {@code REPEAT}. */
    REPEAT(true),
    /** Keyword {@code WHILE}. */
    WHILE(true),
    /** Keyword {@code IF}. */
    IF(true),
    /** Keyword {@code ELSE}. */
    ELSE(true),
    /** Keyword {@code DEF}. */
    DEF(true),
    /** Keyword {@code CALL}. */
    CALL(true),
    /** Keyword {@code NOT}. */
    NOT(true),
    /** Keyword {@code AND}. */
    AND(true),
    /** Keyword {@code OR}. */
    OR(true),
    /** Keyword {@code WALL_AHEAD}. */
    WALL_AHEAD(true),
    /** Keyword {@code ON_ITEM}. */
    ON_ITEM(true),
    /** Keyword {@code AT_GOAL}. */
    AT_GOAL(true),
    /** Identifier. */
    IDENT(false),
    /** Integer literal. */
    INT(false),
    /** Left brace. */
    LBRACE(false),
    /** Right brace. */
    RBRACE(false),
    /** Left parenthesis. */
    LPAREN(false),
    /** Right parenthesis. */
    RPAREN(false),
    /** End of input. */
    EOF(false);

    private final boolean keyword;

    TokenType(boolean keyword) {
        this.keyword = keyword;
    }

    /**
     * Tells whether this type is a language keyword.
     *
     * @return true for the keywords of SPEC §4.1
     */
    public boolean isKeyword() {
        return keyword;
    }
}

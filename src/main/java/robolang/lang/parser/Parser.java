package robolang.lang.parser;

import java.util.ArrayList;
import java.util.List;
import robolang.lang.ast.AndCond;
import robolang.lang.ast.AtGoalCond;
import robolang.lang.ast.BlockStmt;
import robolang.lang.ast.BoolExpression;
import robolang.lang.ast.CallStmt;
import robolang.lang.ast.IfStmt;
import robolang.lang.ast.MoveStmt;
import robolang.lang.ast.NotCond;
import robolang.lang.ast.OnItemCond;
import robolang.lang.ast.OrCond;
import robolang.lang.ast.PickStmt;
import robolang.lang.ast.ProcDef;
import robolang.lang.ast.Program;
import robolang.lang.ast.RepeatStmt;
import robolang.lang.ast.SourceRange;
import robolang.lang.ast.Statement;
import robolang.lang.ast.TurnDirection;
import robolang.lang.ast.TurnStmt;
import robolang.lang.ast.WallAheadCond;
import robolang.lang.ast.WhileStmt;
import robolang.lang.error.ParseException;
import robolang.lang.error.SemanticException;
import robolang.lang.lexer.Lexer;
import robolang.lang.lexer.Token;
import robolang.lang.lexer.TokenType;

/** Recursive-descent parser for the grammar of SPEC §4.2. */
public final class Parser {
    /** Guards the Java stack against pathological nesting such as thousands of '('. */
    private static final int MAX_DEPTH = 200;

    private final List<Token> tokens;
    private int pos;
    private int depth;

    private Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses source text into an AST.
     *
     * @param source program text
     * @return the program
     * @throws robolang.lang.error.LexException on a lexical error
     * @throws ParseException on the first syntax error
     * @throws SemanticException if a {@code REPEAT} count is outside 1–999
     */
    public static Program parse(String source) {
        return new Parser(new Lexer(source).tokenize()).program();
    }

    private Program program() {
        List<ProcDef> procs = new ArrayList<>();
        List<Statement> main = new ArrayList<>();
        while (peek().type() != TokenType.EOF) {
            if (peek().type() == TokenType.DEF) {
                procs.add(procDef());
            } else {
                main.add(statement());
            }
        }
        SourceRange all = pos == 0 ? new SourceRange(1, 1, 1, 1) : span(tokens.get(0), prev());
        return new Program(procs, new BlockStmt(main, all), all);
    }

    private ProcDef procDef() {
        Token def = next();
        if (peek().type() != TokenType.IDENT) {
            throw error("Expected procedure name after DEF");
        }
        Token name = next();
        BlockStmt body = block("Expected '{' after DEF " + name.text());
        return new ProcDef(name.text(), body, span(def, prev()));
    }

    private BlockStmt block(String missingBrace) {
        Token open = expect(TokenType.LBRACE, missingBrace);
        List<Statement> stmts = new ArrayList<>();
        while (peek().type() != TokenType.RBRACE) {
            if (peek().type() == TokenType.EOF) {
                throw error("Expected '}' to close block opened at " + open.line() + ":" + open.col());
            }
            stmts.add(statement());
        }
        next();
        return new BlockStmt(stmts, span(open, prev()));
    }

    private Statement statement() {
        if (++depth > MAX_DEPTH) {
            throw error("Nesting is too deep");
        }
        try {
            Token t = peek();
            return switch (t.type()) {
                case MOVE -> { next(); yield new MoveStmt(span(t, t)); }
                case PICK -> { next(); yield new PickStmt(span(t, t)); }
                case TURN -> turn();
                case REPEAT -> repeat();
                case WHILE -> {
                    next();
                    BoolExpression c = cond("WHILE");
                    BlockStmt body = block("Expected '{' after WHILE condition");
                    yield new WhileStmt(c, body, span(t, prev()));
                }
                case IF -> ifStmt();
                case CALL -> {
                    next();
                    if (peek().type() != TokenType.IDENT) {
                        throw error("Expected procedure name after CALL");
                    }
                    Token name = next();
                    yield new CallStmt(name.text(), span(t, name));
                }
                default -> throw error("Unexpected " + describe(t));
            };
        } finally {
            depth--;
        }
    }

    private Statement turn() {
        Token t = next();
        TurnDirection dir;
        if (peek().type() == TokenType.LEFT) {
            dir = TurnDirection.LEFT;
        } else if (peek().type() == TokenType.RIGHT) {
            dir = TurnDirection.RIGHT;
        } else {
            throw error("Expected LEFT or RIGHT after TURN");
        }
        return new TurnStmt(dir, span(t, next()));
    }

    private Statement repeat() {
        Token t = next();
        if (peek().type() != TokenType.INT) {
            throw error("Expected number after REPEAT");
        }
        Token n = next();
        String digits = n.text().replaceFirst("^0+(?=.)", "");
        if (digits.length() > 3 || Integer.parseInt(digits) < 1 || Integer.parseInt(digits) > 999) {
            throw new SemanticException("REPEAT count must be 1–999", span(n, n));
        }
        BlockStmt body = block("Expected '{' after REPEAT " + n.text());
        return new RepeatStmt(Integer.parseInt(digits), body, span(t, prev()));
    }

    private IfStmt ifStmt() {
        Token t = next();
        BoolExpression c = cond("IF");
        BlockStmt then = block("Expected '{' after IF condition");
        Statement otherwise = null;
        if (peek().type() == TokenType.ELSE) {
            next();
            if (peek().type() == TokenType.IF) {
                if (++depth > MAX_DEPTH) {
                    throw error("Nesting is too deep");
                }
                try {
                    otherwise = ifStmt();
                } finally {
                    depth--;
                }
            } else {
                otherwise = block("Expected '{' or IF after ELSE");
            }
        }
        return new IfStmt(c, then, otherwise, span(t, prev()));
    }

    private BoolExpression cond(String after) {
        BoolExpression left = andCond(after);
        while (peek().type() == TokenType.OR) {
            next();
            BoolExpression right = andCond("OR");
            left = new OrCond(left, right, join(left.range(), right.range()));
        }
        return left;
    }

    private BoolExpression andCond(String after) {
        BoolExpression left = notCond(after);
        while (peek().type() == TokenType.AND) {
            next();
            BoolExpression right = notCond("AND");
            left = new AndCond(left, right, join(left.range(), right.range()));
        }
        return left;
    }

    private BoolExpression notCond(String after) {
        if (++depth > MAX_DEPTH) {
            throw error("Nesting is too deep");
        }
        try {
            Token t = peek();
            if (t.type() == TokenType.NOT) {
                next();
                BoolExpression operand = notCond("NOT");
                return new NotCond(operand, join(span(t, t), operand.range()));
            }
            return atom(after);
        } finally {
            depth--;
        }
    }

    private BoolExpression atom(String after) {
        Token t = peek();
        switch (t.type()) {
            case WALL_AHEAD:
                next();
                return new WallAheadCond(span(t, t));
            case ON_ITEM:
                next();
                return new OnItemCond(span(t, t));
            case AT_GOAL:
                next();
                return new AtGoalCond(span(t, t));
            case LPAREN:
                next();
                BoolExpression inner = cond("'('");
                expect(TokenType.RPAREN, "Expected ')'");
                return inner;
            default:
                throw error("Expected condition after " + after);
        }
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token prev() {
        return tokens.get(pos - 1);
    }

    private Token next() {
        return tokens.get(pos++);
    }

    private Token expect(TokenType type, String message) {
        if (peek().type() != type) {
            throw error(message);
        }
        return next();
    }

    private ParseException error(String message) {
        Token t = peek();
        return new ParseException(message, new SourceRange(t.line(), t.col(), t.line(), t.col()));
    }

    private static String describe(Token t) {
        return t.type() == TokenType.EOF ? "end of input" : "'" + t.text() + "'";
    }

    private static SourceRange span(Token first, Token last) {
        return new SourceRange(first.line(), first.col(), last.line(), last.col() + Math.max(last.text().length(), 1) - 1);
    }

    private static SourceRange join(SourceRange a, SourceRange b) {
        return new SourceRange(a.startLine(), a.startCol(), b.endLine(), b.endCol());
    }
}

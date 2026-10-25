package robolang.ui.highlight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import robolang.lang.error.RoboLangException;
import robolang.lang.lexer.Lexer;
import robolang.lang.lexer.Token;
import robolang.lang.lexer.TokenType;

/** Pure mapping from program text to style runs, from the lexer's tokens (FR-13). */
public final class SyntaxRuns {

    /** What a run of text is. */
    public enum Style {
        /** A statement or operator keyword. */
        KEYWORD,
        /** A sensor condition: {@code WALL_AHEAD}, {@code ON_ITEM}, {@code AT_GOAL}. */
        CONDITION,
        /** An integer literal. */
        NUMBER,
        /** A {@code #} comment. */
        COMMENT,
        /** A procedure name. */
        IDENTIFIER
    }

    /**
     * A styled stretch of the text.
     *
     * @param start document offset of the first character
     * @param length number of characters
     * @param style the style
     */
    public record Run(int start, int length, Style style) {
    }

    private SyntaxRuns() {
    }

    /**
     * Styles a program. Text the lexer rejects gets no runs at all, so it stays unstyled.
     *
     * @param source the program text
     * @return the runs ordered by start
     */
    public static List<Run> of(String source) {
        List<Token> tokens;
        try {
            tokens = new Lexer(source).tokenize();
        } catch (RoboLangException e) {
            return List.of();
        }
        List<Integer> lineStarts = new ArrayList<>(List.of(0));
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                lineStarts.add(i + 1);
            }
        }
        List<Run> runs = new ArrayList<>();
        for (Token t : tokens) {
            Style style = style(t.type());
            if (style != null) {
                runs.add(new Run(lineStarts.get(t.line() - 1) + t.col() - 1, t.text().length(), style));
            }
        }
        // The lexer drops comments; a '#' always starts one because the language has no strings.
        for (int line = 0; line < lineStarts.size(); line++) {
            int start = lineStarts.get(line);
            int end = line + 1 < lineStarts.size() ? lineStarts.get(line + 1) - 1 : source.length();
            int hash = source.indexOf('#', start);
            if (hash >= 0 && hash < end) {
                runs.add(new Run(hash, end - hash, Style.COMMENT));
            }
        }
        runs.sort(Comparator.comparingInt(Run::start));
        return runs;
    }

    private static Style style(TokenType type) {
        return switch (type) {
            case WALL_AHEAD, ON_ITEM, AT_GOAL -> Style.CONDITION;
            case INT -> Style.NUMBER;
            case IDENT -> Style.IDENTIFIER;
            default -> type.isKeyword() ? Style.KEYWORD : null;
        };
    }
}

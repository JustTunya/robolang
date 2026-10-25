package robolang.lang.ast;

/**
 * A span in the program text; lines and columns are 1-based.
 *
 * @param startLine first line
 * @param startCol first column
 * @param endLine last line
 * @param endCol last column
 */
public record SourceRange(int startLine, int startCol, int endLine, int endCol) {
}

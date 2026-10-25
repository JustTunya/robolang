package robolang.ui.ast;

import java.util.List;

import robolang.lang.ast.AstNode;

/**
 * One row of the AST tree, free of Swing types.
 *
 * @param label the text shown for the row
 * @param node the AST node the row stands for; null for pure grouping rows such as {@code condition}
 * @param children the nested rows
 */
public record TreeItem(String label, AstNode node, List<TreeItem> children) {
}

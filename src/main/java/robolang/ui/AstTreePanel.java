package robolang.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import robolang.lang.ast.AstNode;
import robolang.lang.ast.BlockStmt;
import robolang.lang.ast.Program;
import robolang.lang.runtime.Outcome;
import robolang.ui.ast.AstTreeBuilder;
import robolang.ui.ast.TreeItem;

/**
 * Shows the parsed program as a tree and highlights the executing node (SPEC §7.2, FR-36).
 */
@SuppressWarnings("serial")
public final class AstTreePanel extends JPanel implements ExecutionObserver {
    private static final Color RUNNING = new Color(0x1F6FEB);
    private static final Color FAILED = new Color(0xD1242F);

    private final CodeEditorPanel editor;
    private final JTree tree = new JTree(new DefaultTreeModel(null));
    private final Map<AstNode, DefaultMutableTreeNode> nodes = new IdentityHashMap<>();
    private DefaultMutableTreeNode highlighted;
    private Color highlightColor;

    /**
     * Creates an empty panel.
     *
     * @param editor the editor whose selection follows the tree selection
     */
    public AstTreePanel(CodeEditorPanel editor) {
        super(new BorderLayout());
        this.editor = editor;
        tree.setRootVisible(true);
        tree.setCellRenderer(new Renderer());
        tree.addTreeSelectionListener(e -> {
            if (e.getNewLeadSelectionPath() != null
                    && ((DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject()
                            instanceof TreeItem item && item.node() != null) {
                editor.selectRange(item.node().range());
            }
        });
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    /**
     * Returns the underlying tree, for tests and styling.
     *
     * @return the tree
     */
    public JTree tree() {
        return tree;
    }

    /**
     * Returns the tree row currently highlighted.
     *
     * @return the row, or null if none
     */
    public TreeItem highlightedItem() {
        return highlighted == null ? null : (TreeItem) highlighted.getUserObject();
    }

    /**
     * Returns the colour of the current highlight.
     *
     * @return {@code RUNNING} blue or {@code FAILED} red; null if nothing is highlighted
     */
    public Color highlightColor() {
        return highlighted == null ? null : highlightColor;
    }

    /** {@inheritDoc} */
    @Override
    public void programParsed(Program program) {
        nodes.clear();
        highlighted = null;
        ((DefaultTreeModel) tree.getModel()).setRoot(toTreeNode(AstTreeBuilder.build(program)));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void nodeEntered(AstNode node) {
        highlight(nodes.get(node), RUNNING);
    }

    /** {@inheritDoc} */
    @Override
    public void runFinished(Outcome outcome) {
        if (outcome.success() || outcome.location() == null) {
            return;
        }
        DefaultMutableTreeNode failed = null;
        for (Map.Entry<AstNode, DefaultMutableTreeNode> e : nodes.entrySet()) {
            // Blocks share their range with the statement or row that owns them; the owner is what failed.
            if (!(e.getKey() instanceof BlockStmt) && outcome.location().equals(e.getKey().range())) {
                failed = e.getValue();
                break;
            }
        }
        highlight(failed, FAILED);
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        highlight(null, null);
    }

    private void highlight(DefaultMutableTreeNode node, Color color) {
        highlighted = node;
        highlightColor = color;
        if (node != null) {
            tree.scrollPathToVisible(new TreePath(node.getPath()));
        }
        tree.repaint();
    }

    private DefaultMutableTreeNode toTreeNode(TreeItem item) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(item);
        if (item.node() != null) {
            nodes.putIfAbsent(item.node(), n);
        }
        for (TreeItem child : item.children()) {
            n.add(toTreeNode(child));
        }
        return n;
    }

    private final class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree t, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(t, value, sel, expanded, leaf, row, hasFocus);
            setText(value instanceof DefaultMutableTreeNode n && n.getUserObject() instanceof TreeItem item
                    ? item.label() : String.valueOf(value));
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
            if (value == highlighted) {
                // White on a saturated fill reads in both light and dark look-and-feels.
                setOpaque(true);
                setBackground(highlightColor);
                setForeground(Color.WHITE);
            } else {
                setOpaque(false);
            }
            return this;
        }
    }
}

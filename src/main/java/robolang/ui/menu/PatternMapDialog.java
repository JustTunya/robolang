package robolang.ui.menu;

import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Shows {@link PatternMap} as a table. */
@SuppressWarnings("serial")
public final class PatternMapDialog extends JDialog {

    /**
     * Creates the dialog.
     *
     * @param owner the parent window
     */
    public PatternMapDialog(Frame owner) {
        super(owner, "Pattern map", true);
        DefaultTableModel model = new DefaultTableModel(new Object[] {"Pattern", "Role", "Class"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (PatternMap.Row r : PatternMap.rows()) {
            model.addRow(new Object[] {r.pattern(), r.role(), r.className()});
        }
        add(new JScrollPane(new JTable(model)));
        setPreferredSize(new Dimension(640, 520));
        pack();
        setLocationRelativeTo(owner);
    }
}

package robolang.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import robolang.model.Level;
import robolang.model.Variant;
import robolang.model.WorldSnapshot;
import robolang.theme.ThemeFactory;
import robolang.ui.execution.SwingExecutionView;

/** The main window (SPEC §7.1): editor | grid | right slot, control bar and status bar. */
@SuppressWarnings("serial")
public final class MainFrame extends JFrame {
    private final CodeEditorPanel editor = new CodeEditorPanel();
    private final GridPanel grid = new GridPanel();
    private final ControlBar controls = new ControlBar();
    private final StatusBar statusBar = new StatusBar();
    private final JComboBox<Level> levelSelector = new JComboBox<>();
    private final JComboBox<ThemeFactory> themeSelector = new JComboBox<>();
    private final JPanel rightSlot = new JPanel(new BorderLayout());
    private final ExecutionController execution = new ExecutionController(
            new SwingExecutionView(editor, grid, controls, statusBar));

    /** Creates the window; call {@link #setLevels} and {@link #setThemes} to fill the selectors. */
    public MainFrame() {
        super("RoboLang");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));

        levelSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list,
                        value instanceof Level l ? l.title() : "(no levels)", index, selected, focus);
            }
        });
        themeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list,
                        value instanceof ThemeFactory t ? t.name() : "(no themes)", index, selected, focus);
            }
        });
        themeSelector.addActionListener(e -> grid.setTheme((ThemeFactory) themeSelector.getSelectedItem()));
        levelSelector.addActionListener(e -> showLevel((Level) levelSelector.getSelectedItem()));
        controls.addControlListener(execution);
        editor.addEditListener(execution::onEdit);
        onLevelSelected(execution::setLevel);

        setJMenuBar(menuBar());
        setRightPanel(placeholder("AST tree"));

        JPanel center = new JPanel(new GridBagLayout());
        addColumn(center, editor, 0, 0.28);
        addColumn(center, grid, 1, 0.44);
        addColumn(center, rightSlot, 2, 0.28);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(controls, BorderLayout.NORTH);
        bottom.add(statusBar, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setSize(Math.max(getWidth(), 1280), Math.max(getHeight(), 800));
        setLocationRelativeTo(null);
    }

    /**
     * Returns the code editor.
     *
     * @return the editor panel
     */
    public CodeEditorPanel editor() {
        return editor;
    }

    /**
     * Returns the grid view.
     *
     * @return the grid panel
     */
    public GridPanel grid() {
        return grid;
    }

    /**
     * Returns the control bar.
     *
     * @return the control bar
     */
    public ControlBar controls() {
        return controls;
    }

    /**
     * Returns the status bar.
     *
     * @return the status bar
     */
    public StatusBar statusBar() {
        return statusBar;
    }

    /**
     * Returns the level selector. Its items are {@link Level}s shown by title.
     *
     * @return the combo box
     */
    public JComboBox<Level> levelSelector() {
        return levelSelector;
    }

    /**
     * Returns the theme selector. Its items are {@link ThemeFactory}s shown by name; choosing one calls
     * {@link GridPanel#setTheme}.
     *
     * @return the combo box
     */
    public JComboBox<ThemeFactory> themeSelector() {
        return themeSelector;
    }

    /**
     * Registers a listener for levels the user picks in the selector.
     *
     * @param listener receives the chosen level
     */
    public void onLevelSelected(Consumer<Level> listener) {
        levelSelector.addActionListener(e -> {
            if (levelSelector.getSelectedItem() instanceof Level level) {
                listener.accept(level);
            }
        });
    }

    /**
     * Fills the level selector and selects the first level, which shows it in the grid.
     *
     * @param levels the campaign levels; may be empty
     */
    public void setLevels(List<Level> levels) {
        levelSelector.setModel(new DefaultComboBoxModel<>(levels.toArray(new Level[0])));
        showLevel(levels.isEmpty() ? null : levels.get(0));
        execution.setLevel(levels.isEmpty() ? null : levels.get(0));
    }

    /**
     * Fills the theme selector and applies the selected theme to the grid.
     *
     * @param themes the available themes; may be empty
     * @param selected the theme to select; the first one if null or unknown
     */
    public void setThemes(List<ThemeFactory> themes, ThemeFactory selected) {
        themeSelector.setModel(new DefaultComboBoxModel<>(themes.toArray(new ThemeFactory[0])));
        if (selected != null && themes.contains(selected)) {
            themeSelector.setSelectedItem(selected);
        }
        grid.setTheme((ThemeFactory) themeSelector.getSelectedItem());
    }

    /**
     * Returns the execution controller, e.g. to register an {@link ExecutionObserver}.
     *
     * @return the controller
     */
    public ExecutionController execution() {
        return execution;
    }

    /**
     * Puts a component into the right-hand slot, replacing the previous one.
     *
     * @param panel the new content
     */
    public void setRightPanel(JComponent panel) {
        rightSlot.removeAll();
        rightSlot.add(panel, BorderLayout.CENTER);
        rightSlot.revalidate();
        rightSlot.repaint();
    }

    private void showLevel(Level level) {
        if (level == null || level.variants().isEmpty()) {
            grid.setVariantCount(0);
            grid.showSnapshot(null);
            return;
        }
        Variant v = level.variants().get(0);
        grid.setVariantCount(level.variants().size());
        grid.setAnimationMillis(0);
        grid.showSnapshot(new WorldSnapshot(v.grid(), v.start(), v.facing(), v.grid().initialItems()));
        grid.setAnimationMillis(250);
        statusBar.setMessage(level.title());
    }

    private static void addColumn(JPanel parent, JComponent child, int x, double weight) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.weightx = weight;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        parent.add(child, c);
    }

    private static JPanel placeholder(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(text, SwingConstants.CENTER));
        return panel;
    }

    private JMenuBar menuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(menu("Game", "New game", "Reset progress"));
        bar.add(menu("View", "Console", "Trace mode"));
        bar.add(menu("Help", "Language reference", "About"));
        bar.add(Box.createHorizontalGlue());
        bar.add(new JLabel("Level: "));
        bar.add(levelSelector);
        bar.add(new JLabel("   Theme: "));
        bar.add(themeSelector);
        return bar;
    }

    private static JMenu menu(String title, String... items) {
        JMenu menu = new JMenu(title);
        for (String item : items) {
            JMenuItem mi = new JMenuItem(item);
            mi.setEnabled(false);
            menu.add(mi);
        }
        return menu;
    }
}

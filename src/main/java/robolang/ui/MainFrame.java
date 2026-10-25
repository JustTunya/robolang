package robolang.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import robolang.lang.runtime.Outcome;
import robolang.model.Level;
import robolang.model.Variant;
import robolang.model.WorldSnapshot;
import robolang.theme.ThemeFactory;
import robolang.ui.briefing.BriefingDialog;
import robolang.ui.console.InterpretTrace;
import robolang.ui.console.TraceConsole;
import robolang.ui.console.TracingThemeFactory;
import robolang.ui.execution.SwingExecutionView;
import robolang.ui.menu.Debouncer;
import robolang.ui.menu.LevelAccess;
import robolang.ui.menu.PatternMapDialog;
import robolang.ui.menu.ProgramAutosave;
import robolang.ui.menu.ProgressPort;
import robolang.ui.menu.SettingsPort;

/** The main window (SPEC §7.1): editor | grid | right slot, control bar and status bar. */
@SuppressWarnings("serial")
public final class MainFrame extends JFrame {
    private final CodeEditorPanel editor = new CodeEditorPanel();
    private final GridPanel grid = new GridPanel();
    private final ControlBar controls = new ControlBar();
    private final StatusBar statusBar = new StatusBar();
    private final JComboBox<Level> levelSelector = new JComboBox<>();
    private final JComboBox<ThemeFactory> themeSelector = new JComboBox<>();
    private final AstTreePanel astTree = new AstTreePanel(editor);
    private final JPanel rightSlot = new JPanel(new BorderLayout());
    private final ExecutionController execution = new ExecutionController(
            new SwingExecutionView(editor, grid, controls, statusBar));
    private final TraceConsole console = new TraceConsole();
    private final JCheckBoxMenuItem consoleItem = new JCheckBoxMenuItem("Console");
    private final JCheckBoxMenuItem traceItem = new JCheckBoxMenuItem("Trace mode");
    private final JCheckBoxMenuItem demoItem = new JCheckBoxMenuItem("Demo mode");
    private final JMenuItem loadSolutionItem = new JMenuItem("Load reference solution");
    private final ProgressPort progress;
    private final SettingsPort settings;
    private final ProgramAutosave autosave;
    private List<Level> levels = List.of();
    private Level currentLevel;
    private boolean loadingSelectors;

    /**
     * Creates the window; call {@link #setLevels} and {@link #setThemes} to fill the selectors.
     *
     * @param progress where results and programs are kept
     * @param settings where demo mode and the theme are kept
     */
    public MainFrame(ProgressPort progress, SettingsPort settings) {
        super("RoboLang");
        this.progress = progress;
        this.settings = settings;
        this.autosave = new ProgramAutosave(progress, Debouncer.swing(), editor::getProgram);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));

        levelSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                java.awt.Component c = super.getListCellRendererComponent(list,
                        value instanceof Level l ? levelLabel(l) : "(no levels)", index, selected, focus);
                c.setEnabled(!(value instanceof Level l) || !isLocked(l));
                return c;
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
        themeSelector.addActionListener(e -> {
            if (!loadingSelectors) {
                applyTheme();
                if (themeSelector.getSelectedItem() instanceof ThemeFactory t) {
                    settings.setThemeName(t.name());
                }
            }
        });
        levelSelector.addActionListener(e -> showLevel((Level) levelSelector.getSelectedItem()));
        controls.addControlListener(execution);
        editor.addEditListener(execution::onEdit);
        editor.addEditListener(autosave::edited);
        onLevelSelected(execution::setLevel);
        onLevelSelected(level -> {
            if (!loadingSelectors) {
                openLevel(level);
                SwingUtilities.invokeLater(this::showBriefing);
            }
        });
        execution.addObserver(new ExecutionObserver() {
            @Override
            public void runFinished(Outcome outcome) {
                recordResult(outcome);
            }
        });
        execution.addObserver(new InterpretTrace(console::log, traceItem::isSelected));
        console.onToggle(consoleItem::setSelected);

        setJMenuBar(menuBar());
        setRightPanel(astTree);
        bindShortcuts();

        JPanel center = new JPanel(new GridBagLayout());
        addColumn(center, editor, 0, 0.28);
        addColumn(center, grid, 1, 0.44);
        addColumn(center, rightSlot, 2, 0.28);

        JPanel main = new JPanel(new BorderLayout());
        main.add(center, BorderLayout.CENTER);
        main.add(console, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(controls, BorderLayout.NORTH);
        bottom.add(statusBar, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
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
     * Returns the AST tree panel; the run controller registers it as an {@link ExecutionObserver}.
     *
     * @return the AST tree panel
     */
    public AstTreePanel astTree() {
        return astTree;
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
        this.levels = List.copyOf(levels);
        // a locked level cannot be selected
        levelSelector.setModel(new DefaultComboBoxModel<>(levels.toArray(new Level[0])) {
            @Override
            public void setSelectedItem(Object item) {
                if (item instanceof Level l && isLocked(l)) {
                    statusBar.setMessage(l.title() + " is locked: solve the previous level first");
                } else {
                    super.setSelectedItem(item);
                }
            }
        });
        showLevel(levels.isEmpty() ? null : levels.get(0));
        if (levels.isEmpty()) {
            execution.setLevel(null);
        } else {
            openLevel(levels.get(0));
        }
    }

    /** Shows the briefing of the open level; call once the window is visible to greet the first level. */
    public void showBriefing() {
        if (currentLevel != null) {
            BriefingDialog.show(this, currentLevel);
        }
    }

    /**
     * Fills the theme selector and applies the selected theme to the grid.
     *
     * @param themes the available themes; may be empty
     * @param selected the theme to select; the first one if null or unknown
     */
    public void setThemes(List<ThemeFactory> themes, ThemeFactory selected) {
        loadingSelectors = true;
        try {
            themeSelector.setModel(new DefaultComboBoxModel<>(themes.toArray(new ThemeFactory[0])));
            if (selected != null && themes.contains(selected)) {
                themeSelector.setSelectedItem(selected);
            }
        } finally {
            loadingSelectors = false;
        }
        applyTheme();
    }

    // The grid gets the factory wrapped in a logging decorator, so every create*() call reaches the console.
    private void applyTheme() {
        ThemeFactory theme = (ThemeFactory) themeSelector.getSelectedItem();
        grid.setTheme(theme == null ? null : new TracingThemeFactory(theme, console::log));
    }

    private boolean isLocked(Level level) {
        int index = levels.indexOf(level);
        return index >= 0 && !LevelAccess.isUnlocked(levels, index,
                id -> progress.bestStars(id).isPresent(), settings.demoMode());
    }

    private String levelLabel(Level level) {
        return LevelAccess.label(levels.indexOf(level) + 1, level.title(), isLocked(level),
                progress.bestStars(level.id()));
    }

    // Saves the program of the level being left, then restores the program of the new one (FR-11).
    private void openLevel(Level level) {
        autosave.switchTo(level.id());
        currentLevel = level;
        editor.setProgram(progress.lastProgram(level.id()).orElse(""));
        execution.setLevel(level);
        statusBar.setStars(progress.bestStars(level.id()).orElse(0));
        refreshAccess();
    }

    private void refreshAccess() {
        loadSolutionItem.setEnabled(settings.demoMode() && currentLevel != null
                && currentLevel.referenceSolution() != null);
        levelSelector.repaint();
    }

    private void recordResult(Outcome outcome) {
        if (!outcome.success() || currentLevel == null) {
            return;
        }
        try {
            progress.record(currentLevel.id(), outcome);
        } catch (RuntimeException e) {
            System.getLogger(MainFrame.class.getName()).log(System.Logger.Level.WARNING, "Cannot record result", e);
        }
        statusBar.setStars(progress.bestStars(currentLevel.id()).orElse(0));
        refreshAccess();
    }

    private void loadReferenceSolution() {
        if (currentLevel == null || currentLevel.referenceSolution() == null || editor.isReadOnly()) {
            return;
        }
        editor.setProgram(currentLevel.referenceSolution());
        execution.onEdit();
        autosave.edited();
    }

    private void resetProgress() {
        int answer = JOptionPane.showConfirmDialog(this, "Erase all results and saved programs?", "Reset progress",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        autosave.cancel();
        try {
            progress.resetAll();
        } catch (RuntimeException e) {
            statusBar.setMessage("Cannot reset progress: " + e.getMessage());
            return;
        }
        if (levels.isEmpty()) {
            return;
        }
        loadingSelectors = true;
        try {
            levelSelector.setSelectedItem(levels.get(0));
        } finally {
            loadingSelectors = false;
        }
        openLevel(levels.get(0));
    }

    private void bindShortcuts() {
        bind("run", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), execution::onRun);
        bind("step", KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), execution::onStep);
        bind("pause", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), execution::onPause);
        bind("stop", KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK), execution::onStop);
        bind("reset", KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), execution::onReset);
        bind("nextTheme", KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), this::nextTheme);
        bind("levelSelector", KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), () -> {
            levelSelector.requestFocusInWindow();
            levelSelector.showPopup();
        });
        // The look and feel binds F10 to the menu bar; Step needs it.
        getJMenuBar().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0),
                "none");
    }

    private void bind(String name, KeyStroke key, Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void nextTheme() {
        int size = themeSelector.getItemCount();
        if (size > 0) {
            themeSelector.setSelectedIndex((themeSelector.getSelectedIndex() + 1) % size);
        }
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

    private JMenuBar menuBar() {
        loadSolutionItem.addActionListener(e -> loadReferenceSolution());
        JMenuItem reset = new JMenuItem("Reset progress...");
        reset.addActionListener(e -> resetProgress());
        consoleItem.addActionListener(e -> console.setExpanded(consoleItem.isSelected()));
        demoItem.setSelected(settings.demoMode());
        demoItem.addActionListener(e -> {
            settings.setDemoMode(demoItem.isSelected());
            refreshAccess();
        });
        JMenuItem patterns = new JMenuItem("Pattern map");
        patterns.addActionListener(e -> new PatternMapDialog(this).setVisible(true));
        loadSolutionItem.setEnabled(false);

        JMenuBar bar = new JMenuBar();
        bar.add(menu("Game", loadSolutionItem, reset));
        bar.add(menu("View", consoleItem, traceItem, demoItem));
        bar.add(menu("Help", patterns));
        bar.add(Box.createHorizontalGlue());
        bar.add(new JLabel("Level: "));
        bar.add(levelSelector);
        bar.add(new JLabel("   Theme: "));
        bar.add(themeSelector);
        return bar;
    }

    private static JMenu menu(String title, JMenuItem... items) {
        JMenu menu = new JMenu(title);
        for (JMenuItem item : items) {
            menu.add(item);
        }
        return menu;
    }
}

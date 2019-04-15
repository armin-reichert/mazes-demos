package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.ui.control.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.ui.control.action.FloodFill;
import de.amr.demos.maze.swingapp.ui.control.action.SaveImage;
import de.amr.demos.maze.swingapp.ui.control.action.SolveMaze;
import de.amr.demos.maze.swingapp.ui.control.menu.GeneratorMenu;
import de.amr.demos.maze.swingapp.ui.control.menu.OptionMenu;
import de.amr.demos.maze.swingapp.ui.control.menu.SolverMenu;
import de.amr.graph.core.api.TraversalState;

/**
 * View controller for control UI.
 * 
 * @author Armin Reichert
 */
public class ControlViewController {

	private static final String ICON_ZOOM_IN = "/zoom_in.png";
	private static final String ICON_ZOOM_OUT = "/zoom_out.png";
	private static final int COLLAPSED_WINDOW_HEIGHT = 160;

	private final MazeDemoModel model;
	private ControlView view;
	private JFrame window;
	private GeneratorMenu generatorMenu;
	private JMenu canvasMenu;
	private SolverMenu solverMenu;
	private OptionMenu optionMenu;
	private boolean hidingWindowWhenBusy;

	private Icon icon(String path) {
		return new ImageIcon(getClass().getResource(path));
	}

	private final Action actionCollapseWindow = new AbstractAction("Hide Details", icon(ICON_ZOOM_OUT)) {

		@Override
		public void actionPerformed(ActionEvent e) {
			collapseWindow();
		}
	};

	private final Action actionExpandWindow = new AbstractAction("Show Details", icon(ICON_ZOOM_IN)) {

		@Override
		public void actionPerformed(ActionEvent e) {
			expandWindow();
		}
	};

	private final Action actionChangeGridResolution = new AbstractAction("Change Resolution") {

		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> combo = (JComboBox<?>) e.getSource();
			app().changeSelectedGridCellSize(combo.getSelectedIndex());
			combo.requestFocusInWindow();
		}
	};

	private final Action actionCreateEmptyGrid = new AbstractAction("Create Empty Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), false, TraversalState.COMPLETED);
		}
	};

	private final Action actionCreateFullGrid = new AbstractAction("Create Full Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), true, TraversalState.COMPLETED);
		}
	};

	private final Action actionStopBackgroundThread = new AbstractAction("Stop") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().stopBackgroundThread();
		}
	};

	private final Action actionClearCanvas = new AbstractAction("Clear Canvas") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().getGridViewController().clearView();
			app().getGridViewController().drawGrid();
		}
	};

	private final Action actionCreateSingleMaze = new CreateSingleMaze();
	private final Action actionCreateAllMazes = new CreateAllMazes();
	private final Action actionSolveMaze = new SolveMaze();
	private final Action actionFloodFill = new FloodFill();
	private final Action actionSaveImage = new SaveImage();

	public ControlViewController(MazeDemoModel model) {
		this.model = model;
		createView();
		createWindow();
	}

	private void createView() {
		view = new ControlView();

		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = app().getInitialSize().width / cellSize;
			int numRows = app().getInitialSize().height / cellSize;
			return String.format("%d cells (%d cols x %d rows, cell size %d)", numCols * numRows, numCols, numRows,
					cellSize);
		}).toArray(String[]::new);
		view.getComboGridResolution().setModel(new DefaultComboBoxModel<>(entries));
		view.getComboGridResolution().setSelectedIndex(model.getGridCellSizeIndex());
		view.getComboGridResolution().setAction(actionChangeGridResolution);

		view.getSliderPassageWidth().setValue(model.getPassageWidthPercentage());
		view.getSliderPassageWidth().addChangeListener(e -> {
			if (!view.getSliderPassageWidth().getValueIsAdjusting()) {
				model.setPassageWidthPercentage(view.getSliderPassageWidth().getValue());
			}
		});

		view.getSliderDelay().setMinimum(0);
		view.getSliderDelay().setMaximum(100);
		view.getSliderDelay().setValue(model.getDelay());
		view.getSliderDelay().setMinorTickSpacing(10);
		view.getSliderDelay().setMajorTickSpacing(50);
		view.getSliderDelay().addChangeListener(e -> {
			if (!view.getSliderDelay().getValueIsAdjusting()) {
				model.setDelay(view.getSliderDelay().getValue());
			}
		});

		view.getBtnCreateMaze().setAction(actionCreateSingleMaze);
		view.getBtnCreateAllMazes().setAction(actionCreateAllMazes);
		view.getBtnSolve().setAction(actionSolveMaze);
		view.getBtnStop().setAction(actionStopBackgroundThread);
	}

	private void createWindow() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Control View");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setContentPane(view);

		// Menus
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);

		generatorMenu = new GeneratorMenu(this);
		menuBar.add(generatorMenu);

		solverMenu = new SolverMenu(this);
		menuBar.add(solverMenu);

		canvasMenu = new JMenu("Canvas");
		canvasMenu.add(actionClearCanvas);
		canvasMenu.add(actionFloodFill);
		canvasMenu.addSeparator();
		canvasMenu.add(actionCreateEmptyGrid);
		canvasMenu.add(actionCreateFullGrid);
		canvasMenu.addSeparator();
		canvasMenu.add(actionSaveImage);
		menuBar.add(canvasMenu);

		optionMenu = new OptionMenu(this);
		menuBar.add(optionMenu);
	}

	public boolean isHidingWindowWhenBusy() {
		return hidingWindowWhenBusy;
	}

	public void setHidingWindowWhenBusy(boolean hidingWindowWhenBusy) {
		this.hidingWindowWhenBusy = hidingWindowWhenBusy;
		optionMenu.updateState();
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public void placeWindow() {
		window.setLocation((app().getInitialSize().width - window.getWidth()) / 2, 42);
	}

	public void showWindow() {
		window.setVisible(true);
		window.requestFocusInWindow();
	}

	public void collapseWindow() {
		view.getCollapsibleArea().setVisible(false);
		view.getBtnShowHideDetails().setAction(actionExpandWindow);
		window.pack();
		window.setSize(window.getWidth(), COLLAPSED_WINDOW_HEIGHT);
	}

	public void expandWindow() {
		view.getCollapsibleArea().setVisible(true);
		view.getBtnShowHideDetails().setAction(actionCollapseWindow);
		window.pack();
	}

	public void setBusy(boolean busy) {
		if (busy) {
			if (hidingWindowWhenBusy) {
				window.setVisible(false);
			}
			setEnabled(false, generatorMenu, solverMenu, canvasMenu, optionMenu);
			setEnabled(false, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setWaitCursor(view);
			setNormalCursor(view.getBtnStop(), view.getBtnShowHideDetails(), view.getSliderDelay());
		}
		else {
			window.setVisible(true);
			setEnabled(true, generatorMenu, solverMenu, canvasMenu, optionMenu);
			setEnabled(true, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setNormalCursor(view);
		}
	}

	private void setEnabled(boolean b, Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setEnabled(b));
	}

	private void setEnabled(boolean b, Action... actions) {
		Arrays.stream(actions).forEach(action -> action.setEnabled(b));
	}

	private void setWaitCursor(Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
	}

	private void setNormalCursor(Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setCursor(Cursor.getDefaultCursor()));
	}

	public void showMessage(String msg) {
		view.getTextArea().append(msg);
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<AlgorithmInfo> getSelectedGenerator() {
		return generatorMenu.getSelectedAlgorithm();
	}

	public void selectSolver(AlgorithmInfo solverInfo) {
		solverMenu.selectAlgorithm(solverInfo);
		updateSolverText(solverInfo);
	}

	public Optional<AlgorithmInfo> getSelectedSolver() {
		return solverMenu.getSelectedAlgorithm();
	}

	public void selectGenerator(AlgorithmInfo generatorInfo) {
		generatorMenu.selectAlgorithm(generatorInfo);
		updateGeneratorText(generatorInfo);
	}

	private void updateSolverText(AlgorithmInfo solverInfo) {
		String text = solverInfo.getDescription();
		if (solverInfo.isTagged(SolverTag.INFORMED)) {
			String metric = model.getMetric().toString();
			metric = metric.substring(0, 1) + metric.substring(1).toLowerCase();
			text += " (" + metric + ")";
		}
		view.getLblSolverName().setText(text);
	}

	private void updateGeneratorText(AlgorithmInfo generatorInfo) {
		view.getLblGeneratorName().setText(generatorInfo.getDescription());
	}
}
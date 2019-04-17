package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static de.amr.demos.maze.swingapp.ui.common.MenuBuilder.updateMenuSelection;
import static de.amr.demos.maze.swingapp.ui.common.SwingGoodies.action;
import static de.amr.demos.maze.swingapp.ui.common.SwingGoodies.icon;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildCanvasMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildGeneratorMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildOptionMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildSolverMenu;

import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.common.MenuBuilder;
import de.amr.demos.maze.swingapp.ui.common.SwingGoodies;
import de.amr.demos.maze.swingapp.ui.control.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.ui.control.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.ui.control.action.FloodFill;
import de.amr.demos.maze.swingapp.ui.control.action.SaveImage;
import de.amr.demos.maze.swingapp.ui.control.action.SolveMaze;
import de.amr.graph.core.api.TraversalState;

/**
 * View controller for control UI.
 * 
 * @author Armin Reichert
 */
public class ControlViewController implements PropertyChangeListener {

	private static final int COLLAPSED_WINDOW_HEIGHT = 160;

	private final MazeDemoModel model;

	private final JFrame window;
	private final JMenu generatorMenu;
	private final JMenu canvasMenu;
	private final JMenu solverMenu;
	private final JMenu optionMenu;
	private final ControlView view;

	private boolean hidingWindowWhenBusy;

	// Actions

	final Action actionCollapseWindow = action("Hide Details", icon("/zoom_out.png"), e -> collapseWindow());

	final Action actionExpandWindow = action("Show Details", icon("/zoom_in.png"), e -> expandWindow());

	final Action actionChangeGridResolution = action("Change Resolution", e -> {
		JComboBox<?> combo = (JComboBox<?>) e.getSource();
		theApp.changeSelectedGridCellSize(combo.getSelectedIndex());
		combo.requestFocusInWindow();
	});

	final Action actionCreateEmptyGrid = action("Create Empty Grid", e -> {
		getModel().createGrid(getModel().getGrid().numCols(), getModel().getGrid().numRows(), false,
				TraversalState.COMPLETED);
	});

	final Action actionCreateFullGrid = action("Create Full Grid", e -> {
		getModel().createGrid(getModel().getGrid().numCols(), getModel().getGrid().numRows(), true,
				TraversalState.COMPLETED);
	});

	final Action actionStopBackgroundThread = action("Stop", e -> theApp.stopBackgroundThread());

	final Action actionClearCanvas = action("Clear Canvas", e -> {
		theApp.getGridViewController().clearView();
		theApp.getGridViewController().drawGrid();
	});

	final Action actionCreateSingleMaze = new CreateSingleMaze("New Maze");

	final Action actionCreateAllMazes = new CreateAllMazes("All Mazes");

	final Action actionSolveMaze = new SolveMaze("Solve");

	final Action actionFloodFill = new FloodFill("Flood-fill");

	final Action actionSaveImage = new SaveImage("Save Image...", this);

	public ControlViewController(MazeDemoModel model, Dimension gridWindowSize) {

		// connect controller with model

		this.model = model;
		model.changeHandler.addPropertyChangeListener(this);

		// create UI

		view = new ControlView();

		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = gridWindowSize.width / cellSize;
			int numRows = gridWindowSize.height / cellSize;
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

		window = new JFrame();
		window.setTitle("Maze Demo App - Control View");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setContentPane(view);

		window.setJMenuBar(new JMenuBar());
		window.getJMenuBar().add(generatorMenu = buildGeneratorMenu(this));
		window.getJMenuBar().add(solverMenu = buildSolverMenu(this));
		window.getJMenuBar().add(canvasMenu = buildCanvasMenu(this));
		window.getJMenuBar().add(optionMenu = buildOptionMenu(this));

		updateMenuSelection(generatorMenu);
		updateMenuSelection(solverMenu);
		updateMenuSelection(canvasMenu);
		updateMenuSelection(optionMenu);
	}

	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "metric":
			onMetricChanged((Metric) change.getNewValue());
			break;
		default:
			break;
		}
	}

	public boolean isHidingWindowWhenBusy() {
		return hidingWindowWhenBusy;
	}

	public void setHidingWindowWhenBusy(boolean hidingWindowWhenBusy) {
		this.hidingWindowWhenBusy = hidingWindowWhenBusy;
		MenuBuilder.updateMenuSelection(optionMenu);
	}

	public JFrame getWindow() {
		return window;
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public void placeWindowRelativeTo(Window parentWindow) {
		window.setLocation((parentWindow.getWidth() - window.getWidth()) / 2, 42);
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
			SwingGoodies.setEnabled(false, generatorMenu, solverMenu, canvasMenu, optionMenu);
			SwingGoodies.setEnabled(false, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			SwingGoodies.setWaitCursor(view);
			SwingGoodies.setNormalCursor(view.getBtnStop(), view.getBtnShowHideDetails(), view.getSliderDelay());
		}
		else {
			window.setVisible(true);
			SwingGoodies.setEnabled(true, generatorMenu, solverMenu, canvasMenu, optionMenu);
			SwingGoodies.setEnabled(true, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			SwingGoodies.setNormalCursor(view);
		}
	}

	public void showMessage(String msg) {
		view.getTextArea().append(msg);
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<AlgorithmInfo> getSelectedGenerator() {
		return ControlWindowMenus.getSelectedAlgorithm(generatorMenu);
	}

	public void selectGenerator(AlgorithmInfo generatorInfo) {
		ControlWindowMenus.selectAlgorithm(generatorMenu, generatorInfo);
		updateGeneratorText(generatorInfo);
	}

	public Optional<AlgorithmInfo> getSelectedSolver() {
		return ControlWindowMenus.getSelectedAlgorithm(solverMenu);
	}

	public void selectSolver(AlgorithmInfo solverInfo) {
		ControlWindowMenus.selectAlgorithm(solverMenu, solverInfo);
		updateSolverText(solverInfo);
	}

	private void onMetricChanged(Metric metric) {
		getSelectedSolver().ifPresent(this::updateSolverText);
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
package de.amr.demos.maze.swingapp;

import static de.amr.demos.maze.swingapp.model.MazeDemoModel.GENERATOR_ALGORITHMS;
import static de.amr.demos.maze.swingapp.model.MazeDemoModel.PATHFINDER_ALGORITHMS;
import static de.amr.graph.grid.api.GridPosition.BOTTOM_RIGHT;
import static de.amr.graph.grid.api.GridPosition.CENTER;
import static de.amr.graph.grid.api.GridPosition.TOP_LEFT;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.demos.maze.swingapp.action.CancelTaskAction;
import de.amr.demos.maze.swingapp.action.ChangeGridResolutionAction;
import de.amr.demos.maze.swingapp.action.ClearCanvasAction;
import de.amr.demos.maze.swingapp.action.CreateAllMazesAction;
import de.amr.demos.maze.swingapp.action.CreateEmptyGridAction;
import de.amr.demos.maze.swingapp.action.CreateFullGridAction;
import de.amr.demos.maze.swingapp.action.CreateMazeAction;
import de.amr.demos.maze.swingapp.action.FloodFillAction;
import de.amr.demos.maze.swingapp.action.RedrawGridAction;
import de.amr.demos.maze.swingapp.action.SaveImageAction;
import de.amr.demos.maze.swingapp.action.ShowControlWindowAction;
import de.amr.demos.maze.swingapp.action.SolveMazeAction;
import de.amr.demos.maze.swingapp.action.ToggleControlPanelAction;
import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Style;
import de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag;
import de.amr.demos.maze.swingapp.model.PathFinderTag;
import de.amr.demos.maze.swingapp.view.ControlPanel;
import de.amr.demos.maze.swingapp.view.ControlWindow;
import de.amr.demos.maze.swingapp.view.GridDisplay;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.maze.alg.traversal.IterativeDFS;

/**
 * This application visualizes different maze generation algorithms and path finders. The grid size
 * and display style can be changed interactively.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(MazeDemoApp::new);
	}

	private static MazeDemoApp APP;

	public static MazeDemoApp app() {
		return APP;
	}

	public static MazeDemoModel model() {
		return APP.model;
	}

	public static ControlWindow controlWindow() {
		return APP.wndControl;
	}

	public static GridDisplay canvas() {
		return APP.canvas;
	}

	public static ControlPanel controlPanel() {
		return APP.wndControl.controlPanel;
	}

	public static final DisplayMode DISPLAY_MODE = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice().getDisplayMode();

	private final MazeDemoModel model;
	private final ControlWindow wndControl;
	private final JFrame wndDisplayArea;
	private GridDisplay canvas;

	public final Action actionCreateMaze = new CreateMazeAction();
	public final Action actionCreateAllMazes = new CreateAllMazesAction();
	public final Action actionRunMazeSolver = new SolveMazeAction();
	public final Action actionCancelTask = new CancelTaskAction();
	public final Action actionClearCanvas = new ClearCanvasAction();
	public final Action actionRedrawGrid = new RedrawGridAction();
	public final Action actionFloodFill = new FloodFillAction();
	public final Action actionSaveImage = new SaveImageAction();
	public final Action actionEmptyGrid = new CreateEmptyGridAction();
	public final Action actionFullGrid = new CreateFullGridAction();
	public final Action actionChangeGridResolution = new ChangeGridResolutionAction();
	public final Action actionShowControls = new ShowControlWindowAction();
	public final ToggleControlPanelAction actionToggleControlPanel = new ToggleControlPanelAction();

	private Thread workerThread;

	public MazeDemoApp() {

		APP = this;

		// initialize data
		model = new MazeDemoModel();
		model.setGridCellSizes(256, 128, 64, 32, 16, 8, 4, 2);
		model.setPassageWidthPercentage(100);
		model.setDelay(0);
		model.setGenerationStart(CENTER);
		model.setPathFinderStart(TOP_LEFT);
		model.setPathFinderTarget(BOTTOM_RIGHT);
		model.setMetric(Metric.EUCLIDEAN);
		model.setGenerationAnimated(true);
		model.setFloodFillAfterGeneration(false);
		model.setDistancesVisible(false);
		model.setHidingControlsWhenRunning(false);

		// create initial grid
		model.setGridCellSize(32);
		model.setGridWidth(DISPLAY_MODE.getWidth() / model.getGridCellSize());
		model.setGridHeight(DISPLAY_MODE.getHeight() / model.getGridCellSize());
		model.createGrid(false, TraversalState.UNVISITED);

		// create grid display
		createCanvas();
		wndDisplayArea = new JFrame("Maze Display Window");
		wndDisplayArea.setExtendedState(JFrame.MAXIMIZED_BOTH);
		wndDisplayArea.setUndecorated(true);
		wndDisplayArea.setContentPane(canvas);

		// create control window
		wndControl = new ControlWindow();
		wndControl.setAlwaysOnTop(true);

		// initialize generator and path finder
		MazeDemoModel.find(GENERATOR_ALGORITHMS, IterativeDFS.class).ifPresent(generatorInfo -> {
			wndControl.generatorMenu.selectAlgorithm(generatorInfo);
			changeGenerator(generatorInfo);
		});

		MazeDemoModel
				.find(PATHFINDER_ALGORITHMS,
						pathfinderInfo -> pathfinderInfo.getAlgorithmClass() == BidiBreadthFirstSearch.class)
				.ifPresent(alg -> {
					wndControl.solverMenu.selectAlgorithm(alg);
					changeSolver(alg);
				});

		// hide details initially
		actionToggleControlPanel.setMinimized(true);

		// show windows
		wndDisplayArea.setVisible(true);
		wndControl.setLocation((DISPLAY_MODE.getWidth() - wndControl.getWidth()) / 2, 42);
		wndControl.setVisible(true);
	}

	private void createCanvas() {
		canvas = new GridDisplay(model);
		canvas.setCompletedCellColor(Color.WHITE);
		canvas.setVisitedCellColor(Color.BLUE);
		canvas.setUnvisitedCellColor(Color.BLACK);
		canvas.setPathColor(Color.RED);
		canvas.setStyle(Style.WALL_PASSAGES);
	}

	public void resetDisplay() {
		model.changeHandler.removePropertyChangeListener(canvas);
		model.createGrid(false, TraversalState.UNVISITED);
		createCanvas();
		wndDisplayArea.setContentPane(canvas);
		wndDisplayArea.validate();
	}

	public void changeGenerator(AlgorithmInfo generatorInfo) {
		boolean full = generatorInfo.isTagged(MazeGenerationAlgorithmTag.FullGridRequired);
		model.createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		canvas.clear();
		controlPanel().getLblGenerationAlgorithm().setText(generatorInfo.getDescription());
	}

	public void changeSolver(AlgorithmInfo solverInfo) {
		String label = solverInfo.getDescription();
		if (solverInfo.isTagged(PathFinderTag.INFORMED)) {
			String metricName = model.getMetric().toString();
			String text = metricName.substring(0, 1) + metricName.substring(1).toLowerCase();
			label += " (" + text + ")";
		}
		controlPanel().getLblSolver().setText(label);
	}

	public void showMessage(String msg) {
		controlPanel().showMessage(msg + "\n");
	}

	public void enableUI(boolean enabled) {
		wndControl.setVisible(enabled || !model.isHidingControlsWhenRunning());
		wndControl.generatorMenu.setEnabled(enabled);
		wndControl.solverMenu.setEnabled(enabled);
		wndControl.canvasMenu.setEnabled(enabled);
		wndControl.optionMenu.setEnabled(enabled);
		actionChangeGridResolution.setEnabled(enabled);
		actionCreateMaze.setEnabled(enabled);
		actionCreateAllMazes.setEnabled(enabled);
		actionRunMazeSolver.setEnabled(enabled);
		controlPanel().getSliderPassageWidth().setEnabled(enabled);
	}

	public void startWorkerThread(Runnable work) {
		workerThread = new Thread(work);
		workerThread.start();
	}

	public void stopWorkerThread() {
		workerThread.interrupt();
	}
}
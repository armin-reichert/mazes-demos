package de.amr.demos.maze.swingapp;

import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag;
import de.amr.demos.maze.swingapp.view.ControlWindow;
import de.amr.demos.maze.swingapp.view.GridDisplay;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
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

	public static final DisplayMode DISPLAY_MODE = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice().getDisplayMode();

	private static MazeDemoApp IT;

	public static MazeDemoApp app() {
		return IT;
	}

	public static MazeDemoModel model() {
		return IT.model;
	}

	public static GridDisplay canvas() {
		return IT.canvas;
	}

	public static ControlWindow controlWindow() {
		return IT.wndControl;
	}

	private final MazeDemoModel model;
	private final ControlWindow wndControl;
	private final JFrame wndDisplayArea;
	private GridDisplay canvas;
	private Thread bgThread;

	public MazeDemoApp() {
		IT = this;

		model = new MazeDemoModel();

		// create initial grid
		model.setGridCellSize(32);
		model.setGridWidth(DISPLAY_MODE.getWidth() / 32);
		model.setGridHeight(DISPLAY_MODE.getHeight() / 32);
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
		wndControl.minimize();

		// initialize generator and path finder
		model.findGenerator(IterativeDFS.class).ifPresent(this::changeGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(this::changeSolver);

		// show windows
		wndDisplayArea.setVisible(true);
		wndControl.setLocation((DISPLAY_MODE.getWidth() - wndControl.getWidth()) / 2, 42);
		wndControl.setVisible(true);
	}

	public Optional<AlgorithmInfo> currentGenerator() {
		return wndControl.generatorMenu.getSelectedAlgorithm();
	}

	public void changeGenerator(AlgorithmInfo generatorInfo) {
		boolean full = generatorInfo.isTagged(MazeGenerationAlgorithmTag.FullGridRequired);
		model.createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		canvas.clear();
		wndControl.generatorMenu.selectAlgorithm(generatorInfo);
		wndControl.controlPanel.updateGeneratorText(generatorInfo);
	}

	public Optional<AlgorithmInfo> currentSolver() {
		return wndControl.solverMenu.getSelectedAlgorithm();
	}

	public void changeSolver(AlgorithmInfo solverInfo) {
		wndControl.solverMenu.selectAlgorithm(solverInfo);
		wndControl.controlPanel.updateSolverText(solverInfo);
	}

	private void createCanvas() {
		GridDisplay oldCanvas = canvas;
		canvas = new GridDisplay(model);
		if (oldCanvas != null) {
			canvas.setGridBackgroundColor(oldCanvas.getGridBackgroundColor());
			canvas.setCompletedCellColor(oldCanvas.getCompletedCellColor());
			canvas.setVisitedCellColor(oldCanvas.getVisitedCellColor());
			canvas.setUnvisitedCellColor(oldCanvas.getUnvisitedCellColor());
			canvas.setPathColor(oldCanvas.getPathColor());
			canvas.setStyle(oldCanvas.getStyle());
		}
	}

	public void resetDisplay() {
		model.changeHandler.removePropertyChangeListener(canvas);
		boolean wasFull = model.getGrid().isFull();
		model.createGrid(wasFull, wasFull ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		createCanvas();
		canvas.clear();
		canvas.drawGrid();
		wndDisplayArea.setContentPane(canvas);
		wndDisplayArea.validate();
	}

	public void showMessage(String msg) {
		wndControl.controlPanel.showMessage(msg + "\n");
	}

	public void setBusy(boolean busy) {
		wndControl.setVisible(!busy || !model.isHidingControlsWhenRunning());
		wndControl.setEnabled(!busy);
	}

	public void startBackgroundThread(Runnable code, Consumer<AnimationInterruptedException> onInterruption,
			Consumer<Throwable> onFailure) {
		bgThread = new Thread(() -> {
			setBusy(true);
			code.run();
			setBusy(false);
		});
		bgThread.setUncaughtExceptionHandler((thread, e) -> {
			if (e.getClass() == AnimationInterruptedException.class) {
				onInterruption.accept((AnimationInterruptedException) e);
			}
			else {
				onFailure.accept(e);
			}
			setBusy(false);
		});
		bgThread.start();
	}

	public void stopBackgroundThread() {
		bgThread.interrupt();
	}
}
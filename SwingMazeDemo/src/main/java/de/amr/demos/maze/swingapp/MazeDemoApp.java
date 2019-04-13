package de.amr.demos.maze.swingapp;

import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag;
import de.amr.demos.maze.swingapp.view.ControlWindow;
import de.amr.demos.maze.swingapp.view.GridView;
import de.amr.demos.maze.swingapp.view.GridWindow;
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

	private static MazeDemoApp it;

	public static MazeDemoApp app() {
		return it;
	}

	public static MazeDemoModel model() {
		return it.model;
	}

	public static GridView canvas() {
		return it.wndGrid.getGridView();
	}

	public static ControlWindow controlWindow() {
		return it.wndControl;
	}

	private final MazeDemoModel model;
	private final ControlWindow wndControl;
	private final GridWindow wndGrid;
	private Thread bgThread;

	public MazeDemoApp() {
		it = this;

		model = new MazeDemoModel();

		// create initial grid
		model.setGridWidth(DISPLAY_MODE.getWidth() / model.getGridCellSize());
		model.setGridHeight(DISPLAY_MODE.getHeight() / model.getGridCellSize());
		model.createGrid(false, TraversalState.UNVISITED);

		// create grid window
		wndGrid = new GridWindow(model);

		// create control window
		wndControl = new ControlWindow(model);
		wndControl.setAlwaysOnTop(true);
		wndControl.minimize();
		wndControl.setBusy(false);

		// initialize generator and path finder
		model.findGenerator(IterativeDFS.class).ifPresent(this::changeGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(this::changeSolver);

		// show windows
		wndGrid.setVisible(true);
		wndControl.setLocation((DISPLAY_MODE.getWidth() - wndControl.getWidth()) / 2, 42);
		wndControl.setVisible(true);
	}

	public Optional<AlgorithmInfo> currentGenerator() {
		return wndControl.getSelectedGenerator();
	}

	public void changeGenerator(AlgorithmInfo generatorInfo) {
		boolean full = generatorInfo.isTagged(MazeGenerationAlgorithmTag.FullGridRequired);
		model.createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		wndGrid.clear();
		wndControl.selectGenerator(generatorInfo);
	}

	public Optional<AlgorithmInfo> currentSolver() {
		return wndControl.getSelectedSolver();
	}

	public void changeSolver(AlgorithmInfo solverInfo) {
		wndControl.selectSolver(solverInfo);
	}

	public void resetDisplay() {
		boolean wasFull = model.getGrid().isFull();
		model.createGrid(wasFull, wasFull ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		wndGrid.replaceGridView(model);
	}

	public void showMessage(String msg) {
		wndControl.showMessage(msg + "\n");
	}

	public void setBusy(boolean busy) {
		wndControl.setBusy(busy);
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
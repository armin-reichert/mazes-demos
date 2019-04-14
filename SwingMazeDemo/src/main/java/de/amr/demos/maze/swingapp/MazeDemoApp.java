package de.amr.demos.maze.swingapp;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.function.Consumer;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.view.ControlViewController;
import de.amr.demos.maze.swingapp.view.GridViewController;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.maze.alg.traversal.IterativeDFS;

/**
 * This application visualizes different maze generation algorithms and path finders.
 * <p>
 * The application provides an undecorated full-screen preview area where the maze generation and
 * path finding algorithms are displayed as animations. Using a control window one can change the
 * maze generation algorithm and the path finder algorithm. The size/resolution of the grid can also
 * be changed interactively.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	private static final MazeDemoApp theApp = new MazeDemoApp();

	public static MazeDemoApp app() {
		return theApp;
	}

	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(theApp::createAndShowUI);
	}

	private final Dimension initialGridWindowSize;
	private final MazeDemoModel model;
	private ControlViewController controlViewController;
	private GridViewController gridViewController;
	private Thread bgThread;

	public MazeDemoApp() {
		initialGridWindowSize = getDisplaySize();
		model = new MazeDemoModel();
		model.setGridWidth(getInitialGridWindowSize().width / model.getGridCellSize());
		model.setGridHeight(getInitialGridWindowSize().height / model.getGridCellSize());
		model.createGrid(false, TraversalState.UNVISITED);
	}

	private void createAndShowUI() {
		gridViewController = new GridViewController(model);

		controlViewController = new ControlViewController(model);
		controlViewController.collapseWindow();
		controlViewController.setBusy(false);

		model.findGenerator(IterativeDFS.class).ifPresent(this::changeGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(controlViewController::selectSolver);

		gridViewController.showWindow();
		controlViewController.placeWindow();
		controlViewController.showWindow();
	}

	public Dimension getInitialGridWindowSize() {
		return initialGridWindowSize != null ? initialGridWindowSize : getDisplaySize();
	}

	private Dimension getDisplaySize() {
		DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDisplayMode();
		return new Dimension(displayMode.getWidth(), displayMode.getHeight());
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public ControlViewController getControlViewController() {
		return controlViewController;
	}

	public GridViewController getGridViewController() {
		return gridViewController;
	}

	public void changeGenerator(AlgorithmInfo generatorInfo) {
		boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
		model.createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		gridViewController.clear();
		controlViewController.selectGenerator(generatorInfo);
	}

	public void reset() {
		controlViewController.setBusy(true);
		gridViewController.stopListening();
		boolean full = model.getGrid().isFull();
		model.createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		gridViewController.replaceView();
		gridViewController.startListening();
		controlViewController.setBusy(false);
	}

	public void resizeGrid(int cellSize) {
		model.setGridCellSize(cellSize);
		int w = getGridViewController().getWindow() != null ? getGridViewController().getWindow().getWidth()
				: getInitialGridWindowSize().width;
		int h = getGridViewController().getWindow() != null ? getGridViewController().getWindow().getHeight()
				: getInitialGridWindowSize().height;
		model.setGridWidth(w / cellSize);
		model.setGridHeight(h / cellSize);
		reset();
	}

	public void showMessage(String msg) {
		controlViewController.showMessage(msg + "\n");
	}

	public void startBackgroundThread(Runnable code, Consumer<AnimationInterruptedException> onInterruption,
			Consumer<Throwable> onFailure) {
		bgThread = new Thread(() -> {
			controlViewController.setBusy(true);
			code.run();
			controlViewController.setBusy(false);
		});
		bgThread.setUncaughtExceptionHandler((thread, e) -> {
			if (e.getClass() == AnimationInterruptedException.class) {
				onInterruption.accept((AnimationInterruptedException) e);
			}
			else {
				onFailure.accept(e);
			}
			controlViewController.setBusy(false);
		});
		bgThread.start();
	}

	public void stopBackgroundThread() {
		bgThread.interrupt();
	}
}
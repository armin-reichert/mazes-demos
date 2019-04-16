package de.amr.demos.maze.swingapp;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.function.Consumer;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.common.ThemeConverter;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.maze.alg.Armin;

/**
 * This application visualizes different maze generation algorithms and path finders.
 * <p>
 * The application provides an undecorated full-screen preview area where the maze generation and
 * path finding algorithms are displayed as animations. Using a control window one can change the
 * maze generation path finder algorithm. the size/resolution of the grid, the rendering style and
 * other settings.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	public static final MazeDemoApp theApp = new MazeDemoApp();

	public static void main(String[] args) {
		JCommander.newBuilder().addObject(theApp).build().parse(args);
		EventQueue.invokeLater(theApp::createAndShowUI);
	}

	private final MazeDemoModel model;

	private ControlViewController controlViewController;

	private GridViewController gridViewController;

	private Thread bgThread;

	@Parameter(names = { "-theme" }, description = "Theme", converter = ThemeConverter.class)
	private String theme;

	private MazeDemoApp() {
		model = new MazeDemoModel();
		theme = NimbusLookAndFeel.class.getName();
	}

	private void createAndShowUI() {
		try {
			UIManager.setLookAndFeel(theme);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		gridViewController = new GridViewController(model, getDisplaySize());

		controlViewController = new ControlViewController(model);
		controlViewController.setHidingWindowWhenBusy(false);
		controlViewController.expandWindow();
		controlViewController.setBusy(false);

		model.findGenerator(Armin.class).ifPresent(this::changeGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(controlViewController::selectSolver);

		gridViewController.showWindow();
		controlViewController.placeWindowRelativeTo(gridViewController.getWindow());
		controlViewController.showWindow();
	}

	public Dimension getDisplaySize() {
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
		model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), full,
				full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		gridViewController.clearView();
		controlViewController.selectGenerator(generatorInfo);
	}

	public void reset() {
		controlViewController.setBusy(true);
		gridViewController.stopModelChangeListening();
		int numCols = getGridViewController().getWindow().getWidth() / model.getGridCellSize();
		int numRows = getGridViewController().getWindow().getHeight() / model.getGridCellSize();
		boolean full = model.getGrid().isFull();
		model.createGrid(numCols, numRows, full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		gridViewController.resetView();
		gridViewController.startModelChangeListening();
		controlViewController.setBusy(false);
	}

	public void changeSelectedGridCellSize(int selectedIndex) {
		model.setGridCellSizeIndex(selectedIndex);
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
		}, "MazeDemoWorker");
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
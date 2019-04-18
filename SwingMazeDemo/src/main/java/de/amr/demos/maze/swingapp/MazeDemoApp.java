package de.amr.demos.maze.swingapp;

import static de.amr.swing.Swing.getDisplaySize;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.function.Consumer;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

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
		EventQueue.invokeLater(() -> theApp.createAndShowUI(getDisplaySize()));
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

	private void createAndShowUI(Dimension gridWindowSize) {
		try {
			UIManager.setLookAndFeel(theme);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		gridViewController = new GridViewController(model, gridWindowSize);

		controlViewController = new ControlViewController(model, gridWindowSize, gridViewController);
		controlViewController.setHiddenWhenBusy(false);
		controlViewController.expandWindow();
		controlViewController.setBusy(false);

		model.findGenerator(Armin.class).ifPresent(controlViewController::selectGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(controlViewController::selectSolver);

		gridViewController.showWindow();
		controlViewController.placeWindowRelativeTo(gridViewController.getWindow());
		controlViewController.showWindow();
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

	public void showMessage(String msg) {
		controlViewController.showMessage(msg);
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
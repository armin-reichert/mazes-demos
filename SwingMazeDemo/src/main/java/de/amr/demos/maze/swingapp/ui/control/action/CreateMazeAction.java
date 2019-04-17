package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.util.StopWatch;

public abstract class CreateMazeAction extends AbstractAction {

	protected static void pause(float seconds) {
		try {
			Thread.sleep(Math.round(seconds * 1000));
		} catch (InterruptedException e) {
			throw new AnimationInterruptedException();
		}
	}

	protected final GridViewController gridViewController;
	protected final ControlViewController controlViewController;
	protected final MazeDemoModel model;

	public CreateMazeAction(String name, GridViewController gridViewController, ControlViewController controlViewController) {
		super(name);
		this.gridViewController = gridViewController;
		this.controlViewController = controlViewController;
		model = gridViewController.getModel();
	}

	protected void floodFill() {
		int startCell = model.getGrid().cell(model.getGenerationStart());
		gridViewController.floodFill(startCell, false);
	}

	protected void createMaze(AlgorithmInfo generatorInfo, GridPosition startPosition) {
		ObservableGridGraph<TraversalState, Integer> grid = model.getGrid();
		MazeGenerator generator = null;
		try {
			generator = (MazeGenerator) generatorInfo.getAlgorithmClass().getConstructor(GridGraph2D.class)
					.newInstance(model.getGrid());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new RuntimeException(e);
		}
		int startCell = grid.cell(startPosition);
		int x = grid.col(startCell), y = grid.row(startCell);
		theApp.showMessage(format("\n%s (%d cells)", generatorInfo.getDescription(), grid.numVertices()));
		if (model.isGenerationAnimated()) {
			generator.createMaze(x, y);
		}
		else {
			gridViewController.getAnimation().setEnabled(false);
			gridViewController.clearView();
			StopWatch watch = new StopWatch();
			watch.start();
			generator.createMaze(x, y);
			watch.stop();
			theApp.showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> gridViewController.drawGrid());
			theApp.showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			gridViewController.getAnimation().setEnabled(true);
		}
	}
}
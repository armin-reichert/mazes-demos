package de.amr.demos.maze.swingapp.ui.control.action;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.Algorithm;
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

	protected final ControlViewController controlViewController;
	protected final GridViewController gridViewController;
	protected final MazeDemoModel model;

	public CreateMazeAction(String name, ControlViewController controlViewController,
			GridViewController gridViewController) {
		super(name);
		this.gridViewController = gridViewController;
		this.controlViewController = controlViewController;
		model = gridViewController.getModel();
	}

	protected void floodFill() {
		int startCell = model.getGrid().cell(model.getGenerationStart());
		gridViewController.floodFill(startCell, false);
	}

	protected void createMaze(Algorithm generator, GridPosition startPosition) {
		ObservableGridGraph<TraversalState, Integer> grid = model.getGrid();
		MazeGenerator generatorInstance = null;
		try {
			generatorInstance = (MazeGenerator) generator.getAlgorithmClass().getConstructor(GridGraph2D.class)
					.newInstance(model.getGrid());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new RuntimeException(e);
		}
		int startCell = grid.cell(startPosition);
		int x = grid.col(startCell), y = grid.row(startCell);
		controlViewController
				.showMessage(format("\n%s (%d cells)", generator.getDescription(), grid.numVertices()));
		if (model.isGenerationAnimated()) {
			generatorInstance.createMaze(x, y);
		}
		else {
			gridViewController.getAnimation().setEnabled(false);
			gridViewController.clearView();
			StopWatch watch = new StopWatch();
			watch.start();
			generatorInstance.createMaze(x, y);
			watch.stop();
			controlViewController.showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> gridViewController.drawGrid());
			controlViewController.showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			gridViewController.getAnimation().setEnabled(true);
		}
	}
}
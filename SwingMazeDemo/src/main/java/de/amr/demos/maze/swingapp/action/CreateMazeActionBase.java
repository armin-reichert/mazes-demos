package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static java.lang.String.format;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.api.ObservableGridGraph2D;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.util.StopWatch;

public abstract class CreateMazeActionBase extends AbstractAction {

	protected void pause(float seconds) {
		try {
			Thread.sleep(Math.round(seconds * 1000));
		} catch (InterruptedException e) {
			throw new AnimationInterruptedException();
		}
	}

	protected void floodFill() {
		BFSAnimation.builder().canvas(canvas()).distanceVisible(false).build()
				.floodFill(model().getGrid().cell(model().getGenerationStart()));
	}

	protected ObservableGridGraph2D<TraversalState, Integer> createMaze(AlgorithmInfo algo,
			GridPosition startPosition) throws Exception, StackOverflowError {
		MazeGenerator generator = app().createMazeGenerator(algo);
		model().setGrid((ObservableGridGraph2D<TraversalState, Integer>) generator.getGrid());
		canvas().setGrid((GridGraph<?, ?>) generator.getGrid());
		int startCell = generator.getGrid().cell(startPosition);
		int x = generator.getGrid().col(startCell), y = generator.getGrid().row(startCell);
		app().showMessage(format("\n%s (%d cells)", algo.getDescription(), generator.getGrid().numVertices()));
		if (model().isGenerationAnimated()) {
			generator.createMaze(x, y);
		}
		else {
			canvas().enableAnimation(false);
			StopWatch watch = new StopWatch();
			watch.measure(() -> generator.createMaze(x, y));
			app().showMessage(format("Maze generation: %.2f seconds.", watch.getSeconds()));
			watch.measure(() -> canvas().drawGrid());
			app().showMessage(format("Grid rendering:  %.2f seconds.", watch.getSeconds()));
			canvas().enableAnimation(true);
		}
		return (ObservableGridGraph2D<TraversalState, Integer>) generator.getGrid();
	}
}
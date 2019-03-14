package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static java.lang.String.format;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.OrthogonalGrid;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.Floodfill;
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
		Floodfill.builder().canvas(canvas()).source(model().getGenerationStart()).distanceVisible(false).build()
				.run();
	}

	@SuppressWarnings("unchecked")
	private MazeGenerator<OrthogonalGrid> createGenerator(AlgorithmInfo algo) throws Exception {
		return (MazeGenerator<OrthogonalGrid>) algo.getAlgorithmClass().getConstructor(Integer.TYPE, Integer.TYPE)
				.newInstance(model().getGridWidth(), model().getGridHeight());
	}

	protected OrthogonalGrid createMaze(AlgorithmInfo algo, GridPosition startPosition)
			throws Exception, StackOverflowError {
		MazeGenerator<OrthogonalGrid> generator = createGenerator(algo);
		canvas().setGrid(generator.getGrid());
		canvas().clear();
		canvas().drawGrid();
		int startCell = generator.getGrid().cell(startPosition);
		int x = generator.getGrid().col(startCell), y = generator.getGrid().row(startCell);
		app().showMessage(format("\n%s (%d cells)", algo.getDescription(), generator.getGrid().numVertices()));
		if (model().isGenerationAnimated()) {
			generator.createMaze(x, y);
		} else {
			canvas().enableAnimation(false);
			StopWatch watch = new StopWatch();
			watch.measure(() -> generator.createMaze(x, y));
			app().showMessage(format("Maze generation: %.2f seconds.", watch.getSeconds()));
			canvas().clear();
			watch.measure(() -> canvas().drawGrid());
			app().showMessage(format("Grid rendering:  %.2f seconds.", watch.getSeconds()));
			canvas().enableAnimation(true);
		}
		return generator.getGrid();
	}
}

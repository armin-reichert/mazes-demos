package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static java.lang.String.format;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.graph.grid.api.GridPosition;
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

	protected void createMaze(AlgorithmInfo generatorInfo, GridPosition startPosition)
			throws Exception, StackOverflowError {
		MazeGenerator generator = app().createMazeGenerator(generatorInfo);
		int startCell = model().getGrid().cell(startPosition);
		int x = model().getGrid().col(startCell), y = model().getGrid().row(startCell);
		app().showMessage(
				format("\n%s (%d cells)", generatorInfo.getDescription(), model().getGrid().numVertices()));
		if (model().isGenerationAnimated()) {
			generator.createMaze(x, y);
		}
		else {
			canvas().enableAnimation(false);
			canvas().clear();
			StopWatch watch = new StopWatch();
			watch.measure(() -> generator.createMaze(x, y));
			app().showMessage(format("Maze generation: %.2f seconds.", watch.getSeconds()));
			watch.measure(() -> canvas().drawGrid());
			app().showMessage(format("Grid rendering:  %.2f seconds.", watch.getSeconds()));
			canvas().enableAnimation(true);
		}
	}
}
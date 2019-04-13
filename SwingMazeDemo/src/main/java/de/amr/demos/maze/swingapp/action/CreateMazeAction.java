package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.util.StopWatch;

public abstract class CreateMazeAction extends AbstractAction {

	protected void pause(float seconds) {
		try {
			Thread.sleep(Math.round(seconds * 1000));
		} catch (InterruptedException e) {
			throw new AnimationInterruptedException();
		}
	}

	protected void floodFill() {
		app().getGridWindow().floodFill(app().getModel().getGrid().cell(app().getModel().getGenerationStart()), false);
	}

	protected void createMaze(AlgorithmInfo generatorInfo, GridPosition startPosition) {
		MazeGenerator generator = null;
		try {
			generator = (MazeGenerator) generatorInfo.getAlgorithmClass().getConstructor(GridGraph2D.class)
					.newInstance(app().getModel().getGrid());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new RuntimeException(e);
		}
		int startCell = app().getModel().getGrid().cell(startPosition);
		int x = app().getModel().getGrid().col(startCell), y = app().getModel().getGrid().row(startCell);
		app().showMessage(
				format("\n%s (%d cells)", generatorInfo.getDescription(), app().getModel().getGrid().numVertices()));
		if (app().getModel().isGenerationAnimated()) {
			generator.createMaze(x, y);
		}
		else {
			app().getGridWindow().enableGridAnimation(false);
			app().getGridWindow().clear();
			StopWatch watch = new StopWatch();
			watch.start();
			generator.createMaze(x, y);
			watch.stop();
			app().showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> app().getGridWindow().drawGrid());
			app().showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			app().getGridWindow().enableGridAnimation(true);
		}
	}
}
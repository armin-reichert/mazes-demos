package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
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
		theApp.getGridViewController()
				.floodFill(theApp.getModel().getGrid().cell(theApp.getModel().getGenerationStart()), false);
	}

	protected void createMaze(AlgorithmInfo generatorInfo, GridPosition startPosition) {
		MazeGenerator generator = null;
		try {
			generator = (MazeGenerator) generatorInfo.getAlgorithmClass().getConstructor(GridGraph2D.class)
					.newInstance(theApp.getModel().getGrid());
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new RuntimeException(e);
		}
		int startCell = theApp.getModel().getGrid().cell(startPosition);
		int x = theApp.getModel().getGrid().col(startCell), y = theApp.getModel().getGrid().row(startCell);
		theApp.showMessage(
				format("\n%s (%d cells)", generatorInfo.getDescription(), theApp.getModel().getGrid().numVertices()));
		if (theApp.getModel().isGenerationAnimated()) {
			generator.createMaze(x, y);
		}
		else {
			theApp.getGridViewController().enableGridAnimation(false);
			theApp.getGridViewController().clearView();
			StopWatch watch = new StopWatch();
			watch.start();
			generator.createMaze(x, y);
			watch.stop();
			theApp.showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> theApp.getGridViewController().drawGrid());
			theApp.showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			theApp.getGridViewController().enableGridAnimation(true);
		}
	}
}
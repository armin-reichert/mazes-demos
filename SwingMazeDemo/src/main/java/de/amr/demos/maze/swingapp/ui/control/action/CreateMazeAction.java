package de.amr.demos.maze.swingapp.ui.control.action;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.util.StopWatch;

public abstract class CreateMazeAction extends AbstractAction {

	protected final ControlUI controlUI;
	protected final GridUI gridUI;
	protected final MazeDemoModel model;

	public CreateMazeAction(String name, ControlUI controlUI, GridUI gridUI) {
		super(name);
		this.gridUI = gridUI;
		this.controlUI = controlUI;
		model = gridUI.getModel();
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
		controlUI.showMessage(format("\n%s (%d cells)", generator.getDescription(), grid.numVertices()));
		if (model.isGenerationAnimated()) {
			generatorInstance.createMaze(x, y);
		}
		else {
			gridUI.getAnimation().setEnabled(false);
			gridUI.clearView();
			StopWatch watch = new StopWatch();
			watch.start();
			generatorInstance.createMaze(x, y);
			watch.stop();
			controlUI.showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> gridUI.drawGrid());
			controlUI.showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			gridUI.getAnimation().setEnabled(true);
		}
	}
}
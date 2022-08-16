package de.amr.demos.maze.swingapp.ui.control.action;

import static java.lang.String.format;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.graph.pathfinder.util.GraphSearchUtils;
import de.amr.graph.util.GraphUtils;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.util.StopWatch;

public abstract class CreateMazeAction extends AbstractAction {

	protected final ControlUI controlUI;
	protected final GridUI gridUI;
	protected MazeDemoModel model;

	protected CreateMazeAction(String name, ControlUI controlUI, GridUI gridUI) {
		super(name);
		this.gridUI = gridUI;
		this.controlUI = controlUI;
		if (gridUI != null) { // avoid exception in WindowBuilder
			model = gridUI.getModel();
		}
	}

	protected void createMaze(Algorithm genInfo, GridPosition startPosition) {
		ObservableGridGraph<TraversalState, Integer> grid = model.getGrid();
		MazeGenerator gen = null;
		try {
			gen = (MazeGenerator) genInfo.getAlgorithmClass().getConstructor(GridGraph2D.class).newInstance(grid);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int startCell = grid.cell(startPosition);
		int x = grid.col(startCell), y = grid.row(startCell);
		controlUI.showMessage("Generating maze (%d cells) using '%s' algorithm...", grid.numVertices(),
				genInfo.getDescription());
		if (model.isGenerationAnimated()) {
			gen.createMaze(x, y);
			// TODO: make Pearls renderer work correctly for algorithms that remove edges,
			// render resulting grid correctly for now
			if (genInfo.isTagged(GeneratorTag.EDGE_DELETING) && gridUI.getRenderer() instanceof PearlsGridRenderer) {
				gridUI.clear();
				gridUI.drawGrid();
			}
		} else {
			gridUI.enableAnimation(false);
			gridUI.clear();
			StopWatch watch = new StopWatch();
			watch.start();
			gen.createMaze(x, y);
			watch.stop();
			controlUI.showMessage(format("Maze generation: %.0f ms.", watch.getMillis()));
			watch.measure(() -> gridUI.drawGrid());
			controlUI.showMessage(format("Grid rendering:  %.0f ms.", watch.getMillis()));
			gridUI.enableAnimation(true);
		}
		verifyMaze(grid);
	}

	private void verifyMaze(ObservableGridGraph<TraversalState, Integer> grid) {
		boolean maze = true;
		if (grid.numEdges() != grid.numVertices() - 1) {
			maze = false;
			controlUI
					.showMessage(format("Number of edges not ok, is %d, should be %d", grid.numEdges(), grid.numVertices() - 1));
		}
		if (GraphUtils.containsCycle(grid)) {
			maze = false;
			controlUI.showMessage("Graph contains cycle");
		}
		if (!GraphSearchUtils.isConnectedGraph(grid)) {
			maze = false;
			controlUI.showMessage("Graph is not connected");
		}
		if (!maze) {
			controlUI.showMessage("NO MAZE!");
		}
	}
}
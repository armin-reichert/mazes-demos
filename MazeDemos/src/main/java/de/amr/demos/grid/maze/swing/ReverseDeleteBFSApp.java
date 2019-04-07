package de.amr.demos.grid.maze.swing;

import java.util.stream.IntStream;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.SwingGridSampleApp;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.core.ObservableMazesFactory;
import de.amr.maze.alg.mst.ReverseDeleteMST_BFS;

public class ReverseDeleteBFSApp extends SwingGridSampleApp {

	public static void main(String[] args) {
		launch(new ReverseDeleteBFSApp());
	}

	public ReverseDeleteBFSApp() {
		super(128);
		setAppName("Reverse-Delete-MST Maze (BFS)");
	}

	@Override
	public void run() {
		IntStream.of(128, 64, 32).forEach(cellSize -> {
			setCellSize(cellSize);
			MazeGenerator generator = new ReverseDeleteMST_BFS(ObservableMazesFactory.get(),
					getCanvas().getWidth() / cellSize, getCanvas().getHeight() / cellSize);
			setGrid((ObservableGridGraph<TraversalState, Integer>) generator.getGrid());
			generator.createMaze(0, 0);
			floodFill(GridPosition.TOP_LEFT);
			sleep(1000);
		});
		System.exit(0);
	}
}
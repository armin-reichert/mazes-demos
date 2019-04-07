package de.amr.demos.grid.maze.swing;

import java.util.stream.IntStream;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.SwingGridSampleApp;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.core.ObservableMazesFactory;
import de.amr.maze.alg.mst.ReverseDeleteMST_DFS;

public class ReverseDeleteDFSApp extends SwingGridSampleApp {

	public static void main(String[] args) {
		launch(new ReverseDeleteDFSApp());
	}

	public ReverseDeleteDFSApp() {
		super(128);
		setAppName("Reverse-Delete-MST Maze (DFS)");
	}

	@Override
	public void run() {
		IntStream.of(128, 64, 32).forEach(cellSize -> {
			setCellSize(cellSize);
			MazeGenerator generator = new ReverseDeleteMST_DFS(ObservableMazesFactory.get(),
					getCanvas().getWidth() / cellSize, getCanvas().getHeight() / cellSize);
			setGrid((ObservableGridGraph<TraversalState, Integer>) generator.getGrid());
			generator.createMaze(0, 0);
			floodFill(GridPosition.TOP_LEFT);
			sleep(1000);
		});
		System.exit(0);
	}
}
package de.amr.demos.grid.maze.swing;

import java.util.stream.IntStream;

import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.OrthogonalGrid;
import de.amr.graph.grid.ui.SwingGridSampleApp;
import de.amr.maze.alg.core.MazeGenerator;
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
			MazeGenerator<OrthogonalGrid> generator = new ReverseDeleteMST_BFS(getCanvas().getWidth() / cellSize,
					getCanvas().getHeight() / cellSize);
			setGrid(generator.getGrid());
			generator.createMaze(0, 0);
			floodfill(GridPosition.TOP_LEFT);
			sleep(1000);
		});
		System.exit(0);
	}
}
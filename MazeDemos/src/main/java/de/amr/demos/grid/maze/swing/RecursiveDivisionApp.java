package de.amr.demos.grid.maze.swing;

import java.util.stream.IntStream;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.SwingGridSampleApp;
import de.amr.maze.alg.RecursiveDivision;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.core.ObservableMazesFactory;

public class RecursiveDivisionApp extends SwingGridSampleApp {

	public static void main(String[] args) {
		launch(new RecursiveDivisionApp());
	}

	public RecursiveDivisionApp() {
		super(128);
		setAppName("Recursive Division Maze");
	}

	@Override
	public void run() {
		IntStream.of(128, 64, 32, 16, 8, 4, 2).forEach(cellSize -> {
			setCellSize(cellSize);
			MazeGenerator generator = new RecursiveDivision(ObservableMazesFactory.get(),
					getCanvas().getWidth() / cellSize, getCanvas().getHeight() / cellSize);
			setGrid((ObservableGridGraph<TraversalState, Integer>) generator.getGrid());
			generator.createMaze(0, 0);
			sleep(1000);
			floodFill();
			sleep(1000);
		});
		System.exit(0);
	}
}
package de.amr.demos.grid.maze.swing;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.SwingGridSampleApp;
import de.amr.maze.alg.core.MazeGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;

import static de.amr.graph.core.api.TraversalState.UNVISITED;

/**
 * Helper class for visualizing maze creation and flood-fill.
 * <p>
 * Subclasses just implement a main method, e.g.:
 * 
 * <pre>
 * 
 * public static void main(String[] args) {
 * 	MazeDemoApp.launch(AldousBroderUST.class);
 * }
 * </pre>
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp extends SwingGridSampleApp {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				launch((Class<? extends MazeGenerator>) ClassLoader.getSystemClassLoader().loadClass(args[0]));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: java de.amr.demos.grid.maze.swing.MazeDemoApp <maze generator class>");
		}
	}

	public static void launch(Class<? extends MazeGenerator> generatorClass) {
		launch(generatorClass, false);
	}

	public static void launch(Class<? extends MazeGenerator> generatorClass, boolean fullscreen) {
		if (fullscreen) {
			MazeDemoApp app = new MazeDemoApp(generatorClass.getSimpleName(), generatorClass);
			launch(app);
		} else {
			MazeDemoApp app = new MazeDemoApp(generatorClass.getSimpleName(), generatorClass, 800, 600);
			launch(app);
		}
	}

	private final Class<? extends MazeGenerator> generatorClass;

	public MazeDemoApp(String appName, Class<? extends MazeGenerator> generatorClass, int w, int h) {
		super(w, h, 128);
		this.generatorClass = generatorClass;
		setAppName(appName);
	}

	public MazeDemoApp(String appName, Class<? extends MazeGenerator> generatorClass) {
		super(128);
		this.generatorClass = generatorClass;
		setAppName(appName);
	}

	private MazeGenerator createGenerator(GridGraph2D<TraversalState, Integer> grid) {
		try {
			return generatorClass.getConstructor(GridGraph2D.class).newInstance(grid);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		IntStream.of(128, 64, 32, 16, 8, 4, 2).forEach(cellSize -> {
			setCellSize(cellSize);
			int numCols = getCanvas().getWidth() / cellSize;
			int numRows = getCanvas().getHeight() / cellSize;
			ObservableGridGraph<TraversalState, Integer> grid = GridFactory.emptyObservableGrid(numCols, numRows,
					Grid4Topology.get(), UNVISITED, 0);
			setGrid(grid);
			createGenerator(grid).createMaze(0, 0);
			floodFill();
			sleep(1000);
			getCanvas().clear();
		});
		System.exit(0);
	}
}
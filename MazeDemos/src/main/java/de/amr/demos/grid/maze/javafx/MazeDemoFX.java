package de.amr.demos.grid.maze.javafx;

import static de.amr.graph.grid.api.GridPosition.BOTTOM_RIGHT;
import static de.amr.graph.grid.api.GridPosition.TOP_LEFT;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.amr.graph.grid.impl.OrthogonalGrid;
import de.amr.graph.pathfinder.api.Path;
import de.amr.graph.pathfinder.impl.BreadthFirstSearch;
import de.amr.maze.alg.Armin;
import de.amr.maze.alg.BinaryTreeRandom;
import de.amr.maze.alg.Eller;
import de.amr.maze.alg.HuntAndKillRandom;
import de.amr.maze.alg.RecursiveDivision;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.mst.KruskalMST;
import de.amr.maze.alg.mst.PrimMST;
import de.amr.maze.alg.traversal.GrowingTreeLastOrRandom;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Generates mazes using different generation algorithms, draws them and shows the path from top
 * left to bottom right cell.
 * <p>
 * By pressing the PLUS-/MINUS-key the user can change the grid resolution.
 * 
 * @author Armin Reichert
 */
public class MazeDemoFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private static final int MAZE_WIDTH = 1000;

	private static final Class<?> GENERATOR_CLASSES[] = {
		/*@formatter:off*/
		BinaryTreeRandom.class, 
		Eller.class, 
		Armin.class,
		GrowingTreeLastOrRandom.class, 
		HuntAndKillRandom.class, 
		KruskalMST.class, 
		PrimMST.class,
		IterativeDFS.class, 
		RandomBFS.class, 
		RecursiveDivision.class, 
		WilsonUSTRandomCell.class,
		/*@formatter:on*/
	};

	private Canvas canvas;
	private Timer timer;
	private OrthogonalGrid maze;
	private int cols;
	private int rows;
	private int cellSize;

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Maze Generation & Pathfinding");
		primaryStage.setOnCloseRequest(event -> timer.cancel());
		primaryStage.setScene(createScene());
		primaryStage.show();
	}

	public MazeDemoFX() {
		cellSize = 8;
		computeGridSize();
	}

	private void computeGridSize() {
		cols = MAZE_WIDTH / cellSize;
		rows = cols / 2;
		System.out.println(String.format("Cellsize: %d, cols: %d, rows: %d", cellSize, cols, rows));
	}

	private Scene createScene() {
		Pane root = new Pane();
		Scene scene = new Scene(root);

		canvas = new Canvas((cols + 1) * cellSize, (rows + 1) * cellSize);
		root.getChildren().add(canvas);

		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Platform.runLater(MazeDemoFX.this::nextMaze);
			}
		}, 0, 2000);

		scene.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.MINUS) {
				if (cellSize < 128) {
					cellSize *= 2;
					computeGridSize();
				}
			} else if (event.getCode() == KeyCode.PLUS) {
				if (cellSize > 4) {
					cellSize /= 2;
					computeGridSize();
				}
			}
		});
		return scene;
	}

	private void nextMaze() {
		canvas.resize((cols + 1) * cellSize, (rows + 1) * cellSize);
		MazeGenerator<OrthogonalGrid> generator = randomMazeGenerator();
		maze = generator.createMaze(0, 0);
		drawGrid();
		Path path = Path.computePath(maze.cell(TOP_LEFT), maze.cell(BOTTOM_RIGHT),
				new BreadthFirstSearch<>(maze));
		drawPath(path);
	}

	@SuppressWarnings("unchecked")
	private MazeGenerator<OrthogonalGrid> randomMazeGenerator() {
		Class<?> generatorClass = GENERATOR_CLASSES[new Random().nextInt(GENERATOR_CLASSES.length)];
		try {
			return (MazeGenerator<OrthogonalGrid>) generatorClass.getConstructor(Integer.TYPE, Integer.TYPE)
					.newInstance(cols, rows);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create maze generator instance");
		}
	}

	private void drawPassage(Integer u, Integer v) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.strokeLine(maze.col(u) * cellSize, maze.row(u) * cellSize, maze.col(v) * cellSize,
				maze.row(v) * cellSize);
	}

	private void drawGrid() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		gc.translate(cellSize, cellSize);
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(cellSize / 2);
		maze.edges().forEach(edge -> {
			int u = edge.either(), v = edge.other();
			drawPassage(u, v);
		});
		gc.translate(-cellSize, -cellSize);
	}

	private void drawPath(Path path) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.RED);
		gc.setLineWidth(cellSize / 4);
		gc.translate(cellSize, cellSize);
		Integer u = null;
		for (Integer v : path) {
			if (u != null) {
				drawPassage(u, v);
			}
			u = v;
		}
		gc.translate(-cellSize, -cellSize);
	}
}
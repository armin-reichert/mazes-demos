package de.amr.demos.grid.rendering;

import static de.amr.graph.core.api.TraversalState.UNVISITED;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;
import de.amr.maze.alg.mst.KruskalMST;
import de.amr.maze.alg.others.RecursiveDivision;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;

/**
 * Sample app demonstrating how to create a maze image.
 * 
 * <pre>
 * java -cp <i>classpath</i> de.amr.demos.grid.rendering.MazeToImage -alg dfs -width 50 -height 25 -cellSize 8 -floodfill
 * java -cp <i>classpath</i> de.amr.demos.grid.rendering.MazeToImage -alg dfs -w 50 -h 25 -cs 8 -ff
 * </pre>
 * 
 * @author Armin Reichert
 */
public class MazeToImage {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	static class Params {

		@Parameter(names = { "-algorithm", "-alg" }, description = "maze algorithm (dfs, bfs, kruskal, wilson, division)")
		public String algorithm = "dfs";

		@Parameter(names = { "-width", "-w" }, description = "maze width (num columns")
		public int width = 40;

		@Parameter(names = { "-height", "-h" }, description = "maze height (num rows")
		public int height = 25;

		@Parameter(names = { "-cellSize", "-cs" }, description = "maze cell size")
		public int cellSize = 16;

		@Parameter(names = { "-floodfill", "-ff" }, description = "maze gets flood-filled")
		public boolean floodfill = false;
	}

	public static void main(String[] args) {
		var params = new Params();
		JCommander.newBuilder().addObject(params).build().parse(args);
		new MazeToImage(params);
	}

	public MazeToImage(Params params) {
		var maze = buildMaze(params.width, params.height, params.algorithm);
		var canvas = new GridCanvas(maze, params.cellSize);
		var renderer = new WallPassageGridRenderer();
		renderer.fnCellSize = () -> params.cellSize;
		canvas.pushRenderer(renderer);
		renderer.drawGrid(canvas.getDrawGraphics(), maze);
		if (params.floodfill) {
			LOGGER.info("Flood-filling maze");
			BFSAnimation.builder().canvas(canvas).distanceVisible(false).build().floodFill(GridPosition.CENTER);
		}
		try {
			var file = new File("maze.png");
			ImageIO.write(canvas.getDrawingBuffer(), "png", file);
			LOGGER.info(() -> "Saved maze to file '%s'".formatted(file.getAbsolutePath()));
		} catch (IOException e) {
			LOGGER.catching(e);
		}
	}

	private GridGraph2D<TraversalState, Integer> buildMaze(int width, int height, String algorithm) {
		LOGGER.info(() -> "Creating maze of size %dx%d using %s".formatted(width, height, algorithm));
		var grid = GridFactory.emptyObservableGrid(width, height, Grid4Topology.get(), UNVISITED, 0);
		switch (algorithm) {
		case "dfs" -> new IterativeDFS(grid).createMaze(0, 0);
		case "bfs" -> new RandomBFS(grid).createMaze(0, 0);
		case "kruskal" -> new KruskalMST(grid).createMaze(0, 0);
		case "wilson" -> new WilsonUSTRandomCell(grid).createMaze(0, 0);
		case "division" -> new RecursiveDivision(grid).createMaze(0, 0);
		default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
		}
		return grid;
	}
}
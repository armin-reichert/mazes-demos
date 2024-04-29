package de.amr.demos.grid.maze.eight;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.Grid8Topology;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.graph.pathfinder.util.GraphSearchUtils;
import de.amr.graph.util.GraphUtils;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.mst.BoruvkaMST;
import de.amr.maze.alg.mst.KruskalMST;
import de.amr.maze.alg.mst.PrimMST;
import de.amr.maze.alg.others.*;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;

class GeneratorInfo {
	String name;
	Class<?> impl;

	static GeneratorInfo of(String name, Class<?> impl) {
		GeneratorInfo gen = new GeneratorInfo();
		gen.name = name;
		gen.impl = impl;
		return gen;
	}
}

public class EightNeighborsMazeApp {

	private static final int GRID_SIZE = 40;
	private static final int CANVAS_SIZE = 640;

	private static final GeneratorInfo[] GENERATORS = {
		//@formatter:off
		GeneratorInfo.of("DFS", IterativeDFS.class), 
		GeneratorInfo.of("BFS", RandomBFS.class),
		GeneratorInfo.of("Kruskal", KruskalMST.class), 
		GeneratorInfo.of("Boruvka", BoruvkaMST.class), 
		GeneratorInfo.of("Prim", PrimMST.class),
		GeneratorInfo.of("Wilson", WilsonUSTRandomCell.class), 
		GeneratorInfo.of("Eller", Eller.class),
		GeneratorInfo.of("Armin", Armin.class),
		GeneratorInfo.of("BinaryTree", BinaryTreeRandom.class), 
		GeneratorInfo.of("HuntAndKill", HuntAndKill.class), 
		GeneratorInfo.of("Sidewinder", Sidewinder.class),
		GeneratorInfo.of("RecursiveDivision", RecursiveDivision.class),
		//@formatter:on
	};

	public static void main(String[] args) {
		new EightNeighborsMazeApp();
	}

	private GridGraph<TraversalState, Integer> grid;
	private GridCanvas canvas;

	public EightNeighborsMazeApp() {
		SwingUtilities.invokeLater(this::showUI);
	}

	private void create8NeighborGrid() {
		grid = GridFactory.emptyGrid(GRID_SIZE, GRID_SIZE, Grid8Topology.get(), TraversalState.UNVISITED, 0);
	}

	private void createMazes() {
		for (GeneratorInfo genInfo : GENERATORS) {
			createMaze(genInfo);
			render();
			try {
				Thread.sleep(10);
			} catch (InterruptedException x) {
				Logger.info("Interrupted!", x);
				Thread.currentThread().interrupt();
			}
		}
	}

	private void createMaze(GeneratorInfo genInfo) {
		create8NeighborGrid();
		int center = grid.cell(GridPosition.CENTER);
		try {
			var gen = (MazeGenerator) genInfo.impl.getConstructor(GridGraph2D.class).newInstance(grid);
			Logger.info("Generator: %s", genInfo.name);
			gen.createMaze(grid.col(center), grid.row(center));
			if (!isMaze()) {
				Logger.info("No maze!");
			}
		} catch (Exception x) {
			Logger.trace(x);
		}
	}

	private boolean isMaze() {
		boolean edgeCountOk = grid.numEdges() == grid.numVertices() - 1;
		if (!edgeCountOk) {
			Logger.info(() -> "Edge count failed: %d edges, should be %d".formatted(grid.numEdges(), grid.numVertices() - 1));
		}
		boolean cycleFree = !GraphUtils.containsCycle(grid);
		if (!cycleFree) {
			Logger.info("Cycle detected");
		}
		boolean connected = GraphSearchUtils.isConnectedGraph(grid);
		if (!connected) {
			Logger.info("Generated graph is disconnected");
		}
		return edgeCountOk && connected && cycleFree;
	}

	private void render() {
		canvas.setGrid(grid);
		canvas.drawGrid();
	}

	private void showUI() {
		JFrame f = new JFrame();
		f.setTitle("Maze from grid with 8-neighbor topology");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setResizable(false);
		canvas = new GridCanvas();
		canvas.setCellSize(CANVAS_SIZE / GRID_SIZE);
		f.getContentPane().add(canvas);
		PearlsGridRenderer gr = new PearlsGridRenderer();
		gr.fnPassageColor = (u, v) -> Color.YELLOW;
		gr.fnRelativePearlSize = () -> .25;
		gr.fnCellSize = () -> canvas.getCellSize();
		gr.fnPassageWidth = (u, v) -> 2;
		canvas.pushRenderer(gr);
		create8NeighborGrid();
		render();
		f.pack();
		f.setVisible(true);
		new Thread(this::createMazes).start();
	}
}
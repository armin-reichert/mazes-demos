package de.amr.demos.grid.maze.eight;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
import de.amr.maze.alg.others.Armin;
import de.amr.maze.alg.others.BinaryTreeRandom;
import de.amr.maze.alg.others.Eller;
import de.amr.maze.alg.others.HuntAndKill;
import de.amr.maze.alg.others.RecursiveDivision;
import de.amr.maze.alg.others.Sidewinder;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;

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

	static final int GRID_SIZE = 40;
	static final int CANVAS_SIZE = 640;
	static final int ITERATIONS = 1;

	static final GeneratorInfo[] GENERATORS = {
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

	GridGraph<TraversalState, Integer> grid;
	MazeGenerator gen;
	GridCanvas canvas;
	volatile boolean nextMaze = true;

	public EightNeighborsMazeApp() {
		SwingUtilities.invokeLater(this::showUI);
	}

	void grid() {
		grid = GridFactory.emptyGrid(GRID_SIZE, GRID_SIZE, Grid8Topology.get(), TraversalState.UNVISITED, 0);
	}

	void mazes() {
		for (int i = 0; i < ITERATIONS; ++i) {
			System.out.println("--- Round #" + i);
			for (GeneratorInfo genInfo : GENERATORS) {
				maze(genInfo);
				render();
				try {
					Thread.sleep(10);
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
			}
		}
	}

	void maze(GeneratorInfo genInfo) {
		grid();
		int center = grid.cell(GridPosition.CENTER);
		try {
			gen = (MazeGenerator) genInfo.impl.getConstructor(GridGraph2D.class).newInstance(grid);
			System.out.println("Generator: " + genInfo.name);
			gen.createMaze(grid.col(center), grid.row(center));
			if (!isMaze()) {
				System.out.println("No maze!");
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException x) {
			x.printStackTrace();
		}
	}

	boolean isMaze() {
		boolean edgeCountOk = grid.numEdges() == grid.numVertices() - 1;
		if (!edgeCountOk) {
			System.out.println("Edge count failed " + grid.numEdges() + ", should be " + (grid.numVertices() - 1));
		}
		boolean cycleFree = !GraphUtils.containsCycle(grid);
		if (!cycleFree) {
			System.out.println("Cycle detected");
		}
		boolean connected = GraphSearchUtils.isConnectedGraph(grid);
		if (!connected) {
			System.out.println("Generated graph is disconnected");
		}
		return edgeCountOk && connected && cycleFree;
	}

	void render() {
		canvas.setGrid(grid);
		canvas.drawGrid();
	}

	void showUI() {
		JFrame f = new JFrame();
		f.setTitle("Maze from grid with 8-neighbor topology");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					nextMaze = true;
				}
			}
		});
		canvas = new GridCanvas();
		canvas.setCellSize(CANVAS_SIZE / GRID_SIZE);
		f.getContentPane().add(canvas);
		PearlsGridRenderer gr = new PearlsGridRenderer();
		gr.fnPassageColor = (u, v) -> Color.YELLOW;
		gr.fnRelativePearlSize = () -> .25;
		gr.fnCellSize = () -> canvas.getCellSize();
		gr.fnPassageWidth = (u, v) -> 2;
		canvas.pushRenderer(gr);
		grid();
		render();
		f.pack();
		f.setVisible(true);
		new Thread(this::mazes).start();
	}
}
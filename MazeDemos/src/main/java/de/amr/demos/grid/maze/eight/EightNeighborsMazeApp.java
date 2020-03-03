package de.amr.demos.grid.maze.eight;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

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
import de.amr.maze.alg.mst.KruskalMST;
import de.amr.maze.alg.mst.PrimMST;
import de.amr.maze.alg.others.BinaryTree;
import de.amr.maze.alg.others.Eller;
import de.amr.maze.alg.others.HuntAndKill;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;

class Generator {
	String name;
	Class<?> genClass;

	static Generator of(String name, Class<?> genClass) {
		Generator gen = new Generator();
		gen.name = name;
		gen.genClass = genClass;
		return gen;
	}
}

public class EightNeighborsMazeApp {

	static final int GRID_SIZE = 80;
	static final int CANVAS_SIZE = 640;

	static final Generator[] GEN = {
		//@formatter:off
		Generator.of("DFS", IterativeDFS.class), 
		Generator.of("BFS", RandomBFS.class),
		Generator.of("Kruskal", KruskalMST.class), 
		Generator.of("Prim", PrimMST.class),
		Generator.of("Wilson", WilsonUSTRandomCell.class), 
		Generator.of("Eller", Eller.class),
		Generator.of("BinaryTree", BinaryTree.class), 
		Generator.of("HuntAndKill", HuntAndKill.class), 
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
		for (int i = 0; i < 60; ++i) {
			System.out.println("#" + i);
			maze();
			render();
			try {
				Thread.sleep(100);
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}

	void maze() {
		grid();
		int center = grid.cell(GridPosition.CENTER);
		int choice = new Random().nextInt(GEN.length);
		try {
			gen = (MazeGenerator) GEN[choice].genClass.getConstructor(GridGraph2D.class).newInstance(grid);
			System.out.println(GEN[choice].name);
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
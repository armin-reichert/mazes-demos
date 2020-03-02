package de.amr.demos.grid.maze.eight;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.graph.core.api.TraversalState;
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
import de.amr.maze.alg.ust.WilsonUSTRecursiveCrosses;

public class EightNeighborsMazeApp {

	static final int GRID_SIZE = 40;
	static final int CANVAS_SIZE = 640;

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

	void maze() {
		grid();
		int center = grid.cell(GridPosition.CENTER);
		int choice = new Random().nextInt(8);
		switch (choice) {
		case 0:
			System.out.println("DFS");
			gen = new IterativeDFS(grid);
			break;
		case 1:
			System.out.println("BFS");
			gen = new RandomBFS(grid);
			break;
		case 2:
			System.out.println("Wilson");
			gen = new WilsonUSTRecursiveCrosses(grid);
			break;
		case 3:
			System.out.println("Kruskal");
			gen = new KruskalMST(grid);
			break;
		case 4:
			System.out.println("Eller");
			gen = new Eller(grid);
			break;
		case 5:
			System.out.println("Binary Tree");
			gen = new BinaryTree(grid);
			break;
		case 6:
			System.out.println("Hunt and Kill");
			gen = new HuntAndKill(grid);
			break;
		case 7:
			System.out.println("Prim");
			gen = new PrimMST(grid);
			break;
		default:
			System.out.println("DFS");
			gen = new IterativeDFS(grid);
			break;
		}
		gen.createMaze(grid.col(center), grid.row(center));
		if (isMaze()) {
			System.out.println("Maze created.");
		} else {
			System.out.println("No maze!");
		}
	}

	boolean isMaze() {
		boolean edgeCountOk = grid.numEdges() == grid.numVertices() - 1;
		if (!edgeCountOk) {
			System.out.println("Edge count failed " + grid.numEdges() + ", should be " + (grid.numVertices() - 1));
		} else {
			System.out.println("Edge count ok: " + grid.numEdges());
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
		canvas.clear();
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
		f.getContentPane().add(canvas);
		PearlsGridRenderer gr = new PearlsGridRenderer();
		gr.fnPassageColor = (u,v) -> Color.YELLOW;
		gr.fnRelativePearlSize = () -> .25;
		gr.fnCellSize = () -> CANVAS_SIZE / GRID_SIZE;
		gr.fnPassageWidth = (u, v) -> 2;
		canvas.pushRenderer(gr);
		canvas.setCellSize(CANVAS_SIZE / GRID_SIZE);
		grid();
		render();
		f.pack();
		f.setVisible(true);
		new Thread(this::mazes).start();
	}

	void mazes() {
		for (int i = 0; i < 1000; ++i) {
			maze();
			render();
			try {
				Thread.sleep(10);
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}
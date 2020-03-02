package de.amr.demos.grid.maze.eight;

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
import de.amr.graph.util.GraphUtils;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.others.RecursiveDivision;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.ust.WilsonUSTRecursiveCrosses;

public class EightNeighborsMazeApp {

	public static void main(String[] args) {
		new EightNeighborsMazeApp();
	}

	GridGraph<TraversalState, Integer> grid;
	MazeGenerator gen;
	GridCanvas canvas;

	public EightNeighborsMazeApp() {
		SwingUtilities.invokeLater(this::showUI);
	}

	void maze() {
		grid = GridFactory.emptyGrid(20, 20, Grid8Topology.get(), TraversalState.UNVISITED, 0);
		int center = grid.cell(GridPosition.CENTER);
		int choice = new Random().nextInt(4);
		switch (choice) {
		case 0:
			System.out.println("DFS");
			gen = new IterativeDFS(grid);
			break;
		case 1:
			System.out.println("Wilson");
			gen = new WilsonUSTRecursiveCrosses(grid);
			break;
		case 2:
			System.out.println("BFS");
			gen = new RandomBFS(grid);
			break;
		case 3:
			System.out.println("Recursive Division");
			gen = new RecursiveDivision(grid);
			break;
		default:
			System.out.println("DFS");
			gen = new IterativeDFS(grid);
			break;
		}
		gen.createMaze(grid.col(center), grid.row(center));
		if (GraphUtils.containsCycle(grid)) {
			System.out.println("No maze! Cycle detected.");
		} else {
			System.out.println("Maze created.");
		}
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
		canvas = new GridCanvas();
		f.getContentPane().add(canvas);
		PearlsGridRenderer gr = new PearlsGridRenderer();
		gr.fnRelativePearlSize = () -> .25;
		gr.fnCellSize = () -> 32;
		gr.fnPassageWidth = (u, v) -> 4;
		canvas.pushRenderer(gr);
		maze();
		render();
		f.pack();
		f.setVisible(true);
		new Thread(this::loop).start();
	}

	void loop() {
		while (true) {
			maze();
			render();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}
	}
}
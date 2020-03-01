package de.amr.demos.grid.maze.eight;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.Grid8Topology;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.maze.alg.core.MazeGenerator;
import de.amr.maze.alg.traversal.IterativeDFS;

public class EightNeighborsMazeApp {

	public static void main(String[] args) {
		new EightNeighborsMazeApp();
	}

	GridGraph<TraversalState, Integer> grid;
	MazeGenerator mg;
	GridCanvas canvas;

	public EightNeighborsMazeApp() {
		SwingUtilities.invokeLater(this::showUI);
	}

	void maze() {
		grid = GridFactory.emptyGrid(20, 20, Grid8Topology.get(), TraversalState.UNVISITED, 0);
		int center = grid.cell(GridPosition.CENTER);
		mg = new IterativeDFS(grid);
		mg.createMaze(grid.col(center), grid.row(center));
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
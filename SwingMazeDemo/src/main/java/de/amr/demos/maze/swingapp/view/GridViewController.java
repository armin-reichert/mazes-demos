package de.amr.demos.maze.swingapp.view;

import javax.swing.JFrame;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.graph.grid.ui.animation.BFSAnimation;

/**
 * View Controller for the grid display UI.
 * 
 * @author Armin Reichert
 */
public class GridViewController {

	private JFrame window;
	private GridView gridView;

	public GridViewController() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setUndecorated(true);
	}

	public GridViewController(MazeDemoModel model) {
		this();
		createGridView(model);
	}

	public void showWindow() {
		window.setVisible(true);
	}

	public GridView getGridView() {
		return gridView;
	}

	public void clear() {
		gridView.clear();
	}

	public void drawGrid() {
		gridView.drawGrid();
	}

	public void floodFill(int startCell, boolean distanceVisible) {
		BFSAnimation.builder().canvas(gridView).distanceVisible(distanceVisible).build().floodFill(startCell);
	}

	public void enableGridAnimation(boolean enabled) {
		gridView.enableAnimation(enabled);
	}

	private void createGridView(MazeDemoModel model) {
		GridView oldCanvas = gridView;
		gridView = new GridView(model);
		model.changeHandler.addPropertyChangeListener(gridView);
		if (oldCanvas != null) {
			gridView.setGridBackgroundColor(oldCanvas.getGridBackgroundColor());
			gridView.setCompletedCellColor(oldCanvas.getCompletedCellColor());
			gridView.setVisitedCellColor(oldCanvas.getVisitedCellColor());
			gridView.setUnvisitedCellColor(oldCanvas.getUnvisitedCellColor());
			gridView.setPathColor(oldCanvas.getPathColor());
			gridView.setStyle(oldCanvas.getStyle());
		}
		window.setContentPane(gridView);
	}

	public void replaceGridView(MazeDemoModel model) {
		model.changeHandler.removePropertyChangeListener(gridView);
		createGridView(model);
		gridView.clear();
		gridView.drawGrid();
		window.setContentPane(gridView);
		window.validate();
	}
}
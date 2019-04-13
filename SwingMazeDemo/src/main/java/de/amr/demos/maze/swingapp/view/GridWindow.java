package de.amr.demos.maze.swingapp.view;

import javax.swing.JFrame;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;

public class GridWindow extends JFrame {

	private GridView gridView;

	public GridWindow() {
		setTitle("Maze Display Window");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
	}

	public GridWindow(MazeDemoModel model) {
		this();
		createCanvas(model);
	}

	public GridView getGridView() {
		return gridView;
	}

	public void clear() {
		gridView.clear();
	}

	public void replaceGridView(MazeDemoModel model) {
		model.changeHandler.removePropertyChangeListener(gridView);
		createCanvas(model);
		gridView.clear();
		gridView.drawGrid();
		setContentPane(gridView);
		validate();
	}

	private void createCanvas(MazeDemoModel model) {
		GridView oldCanvas = gridView;
		gridView = new GridView(model);
		if (oldCanvas != null) {
			gridView.setGridBackgroundColor(oldCanvas.getGridBackgroundColor());
			gridView.setCompletedCellColor(oldCanvas.getCompletedCellColor());
			gridView.setVisitedCellColor(oldCanvas.getVisitedCellColor());
			gridView.setUnvisitedCellColor(oldCanvas.getUnvisitedCellColor());
			gridView.setPathColor(oldCanvas.getPathColor());
			gridView.setStyle(oldCanvas.getStyle());
		}
		setContentPane(gridView);
	}
}
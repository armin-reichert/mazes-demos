package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.util.StopWatch;

/**
 * View Controller for the grid display UI.
 * 
 * @author Armin Reichert
 */
public class GridViewController implements PropertyChangeListener {

	private JFrame window;
	private GridView gridView;
	private GridCanvasAnimation<TraversalState, Integer> animation;

	private final Action actionShowControls = new AbstractAction("Show Controls") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().getControlViewController().showWindow();
		}
	};

	public GridViewController() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setUndecorated(true);
	}

	public GridViewController(MazeDemoModel model) {
		this();
		model.changeHandler.addPropertyChangeListener(this);
		createGridView(model);
	}

	private void createGridView(MazeDemoModel model) {
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
		gridView.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "showSettings");
		gridView.getActionMap().put("showSettings", actionShowControls);
		window.setContentPane(gridView);
		gridView.clear();
		gridView.drawGrid();

		animation = new GridCanvasAnimation<>(gridView);
		animation.fnDelay = model::getDelay;
		model.getGrid().addGraphObserver(animation);
	}

	public void replaceGridView(MazeDemoModel model) {
		createGridView(model);
		window.validate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "grid":
			if (change.getOldValue() != null) {
				ObservableGridGraph<TraversalState, Integer> oldGrid = (ObservableGridGraph<TraversalState, Integer>) change
						.getOldValue();
				ObservableGridGraph<TraversalState, Integer> newGrid = (ObservableGridGraph<TraversalState, Integer>) change
						.getNewValue();
				oldGrid.removeGraphObserver(animation);
				newGrid.addGraphObserver(animation);
				gridView.setGrid(newGrid, false);
				clear();
				drawGrid();
			}
			break;
		case "passageWidthPercentage":
			clear();
			drawGrid();
		}
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
		StopWatch watch = new StopWatch();
		watch.measure(gridView::drawGrid);
		System.out.println(String.format("%s, drawing time: %.0f ms", gridView.getGrid(), watch.getMillis()));
	}

	public void floodFill(int startCell, boolean distanceVisible) {
		BFSAnimation.builder().canvas(gridView).distanceVisible(distanceVisible).build().floodFill(startCell);
	}

	public void enableGridAnimation(boolean enabled) {
		animation.setEnabled(enabled);
	}
}
package de.amr.demos.maze.swingapp.view.grid;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static java.lang.Math.max;
import static java.lang.Math.min;

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

	private final MazeDemoModel model;

	private JFrame window;
	private GridView view;
	private GridCanvasAnimation<TraversalState, Integer> animation;

	private final Action actionShowControlWindow = new AbstractAction("Show Controls") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().getControlViewController().showWindow();
		}
	};

	public GridViewController(MazeDemoModel model) {
		this.model = model;
		createView();
		createWindow();
		window.setContentPane(view);
		startModelChangeListening();
	}

	private void createView() {
		GridView oldView = view;

		view = new GridView(model.getGrid(), model.getGridCellSize(), this::computePassageWidth);
		view.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "showSettings");
		view.getActionMap().put("showSettings", actionShowControlWindow);

		if (oldView != null) {
			view.setGridBackgroundColor(oldView.getGridBackgroundColor());
			view.setCompletedCellColor(oldView.getCompletedCellColor());
			view.setVisitedCellColor(oldView.getVisitedCellColor());
			view.setUnvisitedCellColor(oldView.getUnvisitedCellColor());
			view.setPathColor(oldView.getPathColor());
			view.setStyle(oldView.getStyle());
			model.getGrid().removeGraphObserver(animation);
		}

		animation = new GridCanvasAnimation<>(view);
		animation.fnDelay = model::getDelay;
		model.getGrid().addGraphObserver(animation);
	}

	public void replaceView() {
		createView();
		window.setContentPane(view);
		window.validate();
		view.clear();
		view.drawGrid();
	}

	public void createWindow() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.setUndecorated(true);
	}

	public void stopModelChangeListening() {
		model.changeHandler.removePropertyChangeListener(this);
	}

	public void startModelChangeListening() {
		model.changeHandler.addPropertyChangeListener(this);
	}

	private int computePassageWidth(int u, int v) {
		int passageWidth = model.getGridCellSize() * model.getPassageWidthPercentage() / 100;
		if (model.isPassageWidthFluent()) {
			float factor = (float) model.getGrid().col(u) / model.getGrid().numCols();
			passageWidth = Math.round(factor * passageWidth);
		}
		passageWidth = max(1, passageWidth);
		passageWidth = min(model.getGridCellSize() - 1, passageWidth);
		return passageWidth;
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
				getView().setGrid(newGrid, false);
				clearView();
				drawGrid();
				window.validate();
			}
			break;
		case "passageWidthPercentage":
			clearView();
			drawGrid();
		}
	}

	public GridView getView() {
		return view;
	}

	public JFrame getWindow() {
		return window;
	}

	public void showWindow() {
		window.setVisible(true);
	}

	public void clearView() {
		view.clear();
	}

	public void drawGrid() {
		StopWatch watch = new StopWatch();
		watch.measure(view::drawGrid);
		System.out.println(String.format("%s, drawing time: %.0f ms", view.getGrid(), watch.getMillis()));
	}

	public void enableGridAnimation(boolean enabled) {
		animation.setEnabled(enabled);
	}

	public void floodFill(int startCell, boolean distanceVisible) {
		BFSAnimation.builder().canvas(view).distanceVisible(distanceVisible).build().floodFill(startCell);
	}

}
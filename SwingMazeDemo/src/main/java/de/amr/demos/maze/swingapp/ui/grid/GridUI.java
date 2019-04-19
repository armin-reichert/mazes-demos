package de.amr.demos.maze.swingapp.ui.grid;

import static de.amr.swing.Swing.getDisplaySize;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;

/**
 * View Controller for the grid display UI.
 * 
 * @author Armin Reichert
 */
public class GridUI implements PropertyChangeListener {

	private final MazeDemoModel model;

	private final JFrame window;
	private final GridView view;
	private GridCanvasAnimation<TraversalState, Integer> animation;

	public GridUI(MazeDemoModel model, Dimension displayAreaSize) {
		this.model = model;
		model.createGrid(displayAreaSize.width / model.getGridCellSize(), displayAreaSize.height / model.getGridCellSize(),
				false, TraversalState.UNVISITED);

		view = new GridView(model.getGrid(), model.getGridCellSize(), this::computePassageWidth);
		addAnimation(model.getGrid());

		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setContentPane(view.getCanvas());
		window.setResizable(false);
		if (displayAreaSize.equals(getDisplaySize())) {
			window.setSize(displayAreaSize);
			window.setExtendedState(JFrame.MAXIMIZED_BOTH);
			window.setUndecorated(true);
		} else {
			view.getCanvas().setSize(displayAreaSize);
			window.pack();
			window.setLocationRelativeTo(null);
		}
		startModelChangeListening();
	}

	private void addAnimation(ObservableGridGraph<TraversalState, Integer> grid) {
		animation = new GridCanvasAnimation<>(view.getCanvas());
		animation.fnDelay = model::getDelay;
		grid.addGraphObserver(animation);
	}

	private void removeAnimation(ObservableGridGraph<TraversalState, Integer> grid) {
		grid.removeGraphObserver(animation);
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

	public void resetView() {
		view.reset(model.getGrid(), model.getGridCellSize());
		window.validate();
	}

	public void stopModelChangeListening() {
		model.changePublisher.removePropertyChangeListener(this);
	}

	public void startModelChangeListening() {
		model.changePublisher.addPropertyChangeListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "grid":
			if (change.getOldValue() != null) {
				ObservableGridGraph<TraversalState, Integer> oldGrid = (ObservableGridGraph<TraversalState, Integer>) change
						.getOldValue();
				removeAnimation(oldGrid);
			}
			ObservableGridGraph<TraversalState, Integer> newGrid = (ObservableGridGraph<TraversalState, Integer>) change
					.getNewValue();
			getView().changeGridSize(newGrid, model.getGridCellSize());
			addAnimation(newGrid);
			clearView();
			drawGrid();
			window.validate();
			break;
		case "passageWidthPercentage":
			clearView();
			drawGrid();
			break;
		default:
			break;
		}
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public GridView getView() {
		return view;
	}

	public JFrame getWindow() {
		return window;
	}

	public GridCanvasAnimation<TraversalState, Integer> getAnimation() {
		return animation;
	}

	public void show() {
		window.setVisible(true);
	}

	public void clearView() {
		view.getCanvas().clear();
	}

	public void drawGrid() {
		view.getCanvas().drawGrid();
	}

	public void floodFill(int startCell, boolean distanceVisible) {
		BFSAnimation.builder().canvas(view.getCanvas()).distanceVisible(distanceVisible).build()
				.floodFill(startCell);
	}
}
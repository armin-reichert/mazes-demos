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
		model.createGrid(displayAreaSize.width / model.getGridCellSize(),
				displayAreaSize.height / model.getGridCellSize(), false, TraversalState.UNVISITED);

		view = new GridView(model.getGrid(), model.getGridCellSize(), this::passageWidth);
		addAnimation(model.getGrid());

		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setContentPane(view.getCanvas());
		window.setResizable(false);
		if (displayAreaSize.equals(getDisplaySize())) {
			window.setSize(displayAreaSize);
			window.setExtendedState(JFrame.MAXIMIZED_BOTH);
			window.setUndecorated(true);
		}
		else {
			view.getCanvas().setSize(displayAreaSize);
			window.pack();
			window.setLocationRelativeTo(null);
		}
		startModelChangeListening();
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public JFrame getWindow() {
		return window;
	}

	public GridView getView() {
		return view;
	}

	private void addAnimation(ObservableGridGraph<TraversalState, Integer> grid) {
		animation = new GridCanvasAnimation<>(view.getCanvas());
		animation.fnDelay = model::getDelay;
		grid.addGraphObserver(animation);
	}

	private void removeAnimation(ObservableGridGraph<TraversalState, Integer> grid) {
		grid.removeGraphObserver(animation);
	}

	public void enableAnimation(boolean enabled) {
		animation.setEnabled(enabled);
	}

	private int passageWidth(int either, int other) {
		int w = model.getGridCellSize() * model.getPassageWidthPercentage() / 100;
		if (model.isPassageWidthFluent()) {
			float factor = (float) model.getGrid().col(either) / model.getGrid().numCols();
			w = Math.round(factor * w);
		}
		w = max(1, w);
		w = min(model.getGridCellSize() - 1, w);
		return w;
	}

	public void stopModelChangeListening() {
		model.changePublisher.removePropertyChangeListener(this);
	}

	public void startModelChangeListening() {
		model.changePublisher.addPropertyChangeListener(this);
	}

	public void reset() {
		view.reset(model.getGrid(), model.getGridCellSize());
		window.validate();
	}

	public void show() {
		window.setVisible(true);
	}

	public void clear() {
		view.getCanvas().clear();
	}

	public void drawGrid() {
		view.getCanvas().drawGrid();
	}

	public void floodFill() {
		int startCell = model.getGrid().cell(model.getSolverSource());
		BFSAnimation.builder().canvas(view.getCanvas()).delay(() -> model.getDelay())
				.distanceVisible(model.isDistancesVisible()).build().floodFill(startCell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent change) {
//		System.err.println("GridUI received property change:\n" + change);
		switch (change.getPropertyName()) {
		case "grid":
			getView().changeGridSize(model.getGrid(), model.getGridCellSize());
			if (change.getOldValue() != null) {
				removeAnimation((ObservableGridGraph<TraversalState, Integer>) change.getOldValue());
			}
			addAnimation(model.getGrid());
			clear();
			drawGrid();
			window.validate();
			break;
		case "passageWidthPercentage":
			clear();
			drawGrid();
			break;
		default:
//			System.err.println("Unhandled property change " + change);
			break;
		}
	}
}
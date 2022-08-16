package de.amr.demos.maze.swingapp.ui.grid;

import static de.amr.swing.Swing.getDisplaySize;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import de.amr.demos.maze.swingapp.model.GridRenderingStyle;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.Grid8Topology;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.graph.grid.ui.rendering.GridRenderer;
import de.amr.swing.Swing;

/**
 * View Controller for the grid display UI.
 * 
 * @author Armin Reichert
 */
public class GridUI implements PropertyChangeListener {

	private final MazeDemoModel model;

	private JFrame window;
	private final GridView view;
	private GridCanvasAnimation<TraversalState, Integer> animation;
	private final Dimension gridViewSize;

	public GridUI(MazeDemoModel model, int width, int height) {
		this.model = model;
		this.gridViewSize = new Dimension(width, height);

		view = new GridView(model.getGrid(), model.getGridCellSize(), () -> model.getGrid().cell(model.getSolverSource()),
				() -> model.getGrid().cell(model.getSolverTarget()), this::passageWidth);
		addCanvasAnimation();

		view.getCanvas().getActionMap().put("toggleFullscreen", Swing.action("toggleFullscreen", e -> toggleFullscreen()));
		view.getCanvas().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");

		if (gridViewSize.equals(getDisplaySize())) {
			showFullscreenWindow();
		} else {
			showNormalWindow();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "delay":
			break;
		case "grid":
			getView().changeGridSize(model.getGrid(), model.getGridCellSize());
			if (change.getOldValue() != null) {
				removeCanvasAnimation((ObservableGridGraph<TraversalState, Integer>) change.getOldValue());
			}
			addCanvasAnimation();
			clear();
			if (model.getGridTopology() == Grid8Topology.get()) {
				view.setStyle(GridRenderingStyle.PEARLS);
			}
			drawGrid();
			window.validate();
			break;
		case "gridCellSizeIndex":
			break;
		case "passageWidthPercentage":
			clear();
			drawGrid();
			break;
		case "renderingStyle":
			view.setStyle(model.getRenderingStyle());
			break;
		case "solverSource":
		case "solverTarget":
			drawGrid();
			break;
		default:
			System.out.println(String.format("%10s: unhandled event %s", getClass().getSimpleName(), change));
			break;
		}
	}

	private void createWindow() {
		if (window != null) {
			window.dispose();
		}
		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setContentPane(view.getCanvas());
		window.setResizable(false);
	}

	private void showNormalWindow() {
		createWindow();
		view.getCanvas().setSize(gridViewSize);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	private void showFullscreenWindow() {
		createWindow();
		window.setSize(gridViewSize);
		window.setExtendedState(Frame.MAXIMIZED_BOTH);
		window.setUndecorated(true);
		window.setVisible(true);
	}

	private void toggleFullscreen() {
		boolean fullscreen = (window.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
		if (fullscreen) {
			showNormalWindow();
		} else {
			showFullscreenWindow();
		}
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

	public GridRenderer getRenderer() {
		return view.getCanvas().getRenderer();
	}

	private void addCanvasAnimation() {
		animation = new GridCanvasAnimation<>(view.getCanvas());
		animation.fnDelay = model::getDelay;
		model.getGrid().addGraphObserver(animation);
	}

	private void removeCanvasAnimation(ObservableGridGraph<TraversalState, Integer> grid) {
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
		model.changes.removePropertyChangeListener(this);
	}

	public void startModelChangeListening() {
		model.changes.addPropertyChangeListener(this);
	}

	public void setEscapeAction(Action action) {
		view.getCanvas().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escapeAction");
		view.getCanvas().getActionMap().put("escapeAction", action);
	}

	public void reset() {
		stopModelChangeListening();
		view.reset(model.getGrid(), model.getGridCellSize());
		startModelChangeListening();
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
}
package de.amr.maze.demo.ui.grid;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.Grid8Topology;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.graph.grid.ui.rendering.GridRenderer;
import de.amr.maze.demo.model.GridRenderingStyle;
import de.amr.maze.demo.model.MazeDemoModel;
import de.amr.swing.MySwing;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static de.amr.swing.MySwing.getDisplaySize;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * View Controller for the grid display UI.
 * 
 * @author Armin Reichert
 */
public class GridUI implements PropertyChangeListener {

	private static final String ACTION_ESCAPE = "Escape";
	private static final String ACTION_TOGGLE_FULLSCREEN = "ToggleFullscreen";

	private final MazeDemoModel model;

	private JFrame window;
	private GridView gridView;
	private Dimension gridViewSize;
	private GridCanvasAnimation<TraversalState, Integer> animation;

	public GridUI(MazeDemoModel model, int width, int height) {
		this.model = model;
		this.gridViewSize = new Dimension(width, height);

		gridView = new GridView(model.getGrid(), model.getGridCellSize(),
				() -> model.getGrid().cell(model.getSolverSource()), () -> model.getGrid().cell(model.getSolverTarget()),
				this::passageWidth);
		addCanvasAnimation();

		gridView.getCanvas().getActionMap().put(ACTION_TOGGLE_FULLSCREEN,
				MySwing.action(ACTION_TOGGLE_FULLSCREEN, e -> toggleFullscreen()));
		gridView.getCanvas().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), ACTION_TOGGLE_FULLSCREEN);

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
		case "delay" -> {
		}
		case "grid" -> {
			getView().changeGridSize(model.getGrid(), model.getGridCellSize());
			if (change.getOldValue() != null) {
				removeCanvasAnimation((ObservableGridGraph<TraversalState, Integer>) change.getOldValue());
			}
			addCanvasAnimation();
			clear();
			if (model.getGridTopology() == Grid8Topology.get()) {
				gridView.setStyle(GridRenderingStyle.PEARLS);
			}
			drawGrid();
			window.validate();
		}
		case "gridCellSizeIndex" -> {
		}
		case "passageWidthPercentage" -> {
			clear();
			drawGrid();
		}
		case "renderingStyle" -> {
			gridView.setStyle(model.getRenderingStyle());
		}
		case "solverSource", "solverTarget" -> {
			drawGrid();
		}
		default -> {
			Logger.info(() -> "Unhandled property change '%s'".formatted(change));
		}
		}
	}

	private void createWindow() {
		if (window != null) {
			window.dispose();
		}
		window = new JFrame();
		window.setTitle("Maze Demo App - Display View");
		window.setContentPane(gridView.getCanvas());
		window.setResizable(false);
	}

	private void showNormalWindow() {
		createWindow();
		gridView.getCanvas().setSize(gridViewSize);
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
		return gridView;
	}

	public GridRenderer getRenderer() {
		return gridView.getCanvas().getRenderer();
	}

	private void addCanvasAnimation() {
		animation = new GridCanvasAnimation<>(gridView.getCanvas());
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
		gridView.getCanvas().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), ACTION_ESCAPE);
		gridView.getCanvas().getActionMap().put(ACTION_ESCAPE, action);
	}

	public void reset() {
		stopModelChangeListening();
		gridView.reset(model.getGrid(), model.getGridCellSize());
		startModelChangeListening();
		window.validate();
	}

	public void show() {
		window.setVisible(true);
	}

	public void clear() {
		gridView.getCanvas().clear();
	}

	public void drawGrid() {
		gridView.getCanvas().drawGrid();
	}

	public void floodFill() {
		int startCell = model.getGrid().cell(model.getSolverSource());
		BFSAnimation.builder().canvas(gridView.getCanvas()).delay(model::getDelay)
				.distanceVisible(model.isDistancesVisible()).build().floodFill(startCell);
	}
}
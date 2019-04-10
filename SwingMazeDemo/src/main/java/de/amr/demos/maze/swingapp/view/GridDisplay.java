package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.DISPLAY_MODE;
import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Style;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.graph.grid.ui.rendering.ConfigurableGridRenderer;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;

/**
 * Display area for maze generation and traversal.
 * 
 * @author Armin Reichert
 */
public class GridDisplay extends GridCanvas implements PropertyChangeListener {

	private final MazeDemoModel model;
	private final GridCanvasAnimation<TraversalState, Integer> animation;

	public GridDisplay(MazeDemoModel model) {
		super(model.getGrid(), model.getGridCellSize());
		this.model = model;
		model.changeHandler.addPropertyChangeListener(this);
		replaceRenderer(createRenderer());
		animation = new GridCanvasAnimation<>(this);
		animation.fnDelay = () -> model.getDelay();
		model.getGrid().addGraphObserver(animation);
		getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "showSettings");
		getActionMap().put("showSettings", app().actionShowControls);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		int dx = (DISPLAY_MODE.getWidth() - getPreferredSize().width) / 2;
		int dy = (DISPLAY_MODE.getHeight() - getPreferredSize().height) / 2;
		g.translate(dx, dy);
		super.paintComponent(g);
		g.translate(-dx, -dy);
	}

	public void enableAnimation(boolean enabled) {
		animation.setEnabled(enabled);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent change) {
		if ("grid".equals(change.getPropertyName())) {
			if (change.getOldValue() != null) {
				ObservableGridGraph<TraversalState, Integer> oldGrid = (ObservableGridGraph<TraversalState, Integer>) change
						.getOldValue();
				ObservableGridGraph<TraversalState, Integer> newGrid = (ObservableGridGraph<TraversalState, Integer>) change
						.getNewValue();
				setGrid(newGrid);
				oldGrid.removeGraphObserver(animation);
				newGrid.addGraphObserver(animation);
			}
		} else if ("passageWidthPercentage".equals(change.getPropertyName())) {
			clear();
			drawGrid();
		}
	}

	@Override
	public void drawGrid() {
		System.out.println("GridDisplay.drawGrid: " + getGrid());
		super.drawGrid();
	}
	
	@Override
	public void setGrid(GridGraph<?, ?> grid) {
		System.out.println("GridDisplay.setGrid:  " + grid);
		super.setGrid(grid);
	}

	private ConfigurableGridRenderer createRenderer() {
		ConfigurableGridRenderer r = model.getStyle() == Style.PEARLS ? new PearlsGridRenderer()
				: new WallPassageGridRenderer();
		r.fnGridBgColor = () -> Color.BLACK;
		r.fnCellSize = () -> model.getGridCellSize();
		r.fnPassageWidth = (u, v) -> {
			int passageWidth = model.getGridCellSize() * model.getPassageWidthPercentage() / 100;
			if (model.isPassageWidthFluent()) {
				float factor = (float) model.getGrid().col(u) / model.getGridWidth();
				passageWidth = Math.round(factor * passageWidth);
			}
			passageWidth = max(1, passageWidth);
			passageWidth = min(model.getGridCellSize() - 1, passageWidth);
			return passageWidth;
		};
		r.fnCellBgColor = cell -> {
			TraversalState state = model.getGrid().get(cell);
			switch (state) {
			case COMPLETED:
				return model.getCompletedCellColor();
			case UNVISITED:
				return model.getUnvisitedCellColor();
			case VISITED:
				return model.getVisitedCellColor();
			default:
				return r.getGridBgColor();
			}
		};
		r.fnPassageColor = (u, dir) -> {
			return r.getCellBgColor(u);
		};
		return r;
	}
}
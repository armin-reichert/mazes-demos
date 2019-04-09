package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.DISPLAY_MODE;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics;

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
public class DisplayArea extends GridCanvas {

	private final GridCanvasAnimation<TraversalState, Integer> animation;

	public DisplayArea() {
		super(model().getGrid(), model().getGridCellSize());
		pushRenderer(createRenderer());
		animation = new GridCanvasAnimation<>(this);
		animation.fnDelay = () -> model().getDelay();
		model().getGrid().addGraphObserver(animation);
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
	public void setGrid(GridGraph<?, ?> grid) {
		super.setGrid(grid);
		ObservableGridGraph<TraversalState, Integer> oldGrid = (ObservableGridGraph<TraversalState, Integer>) getGrid();
		ObservableGridGraph<TraversalState, Integer> newGrid = (ObservableGridGraph<TraversalState, Integer>) grid;
		oldGrid.removeGraphObserver(animation);
		model().setGrid(newGrid);
		newGrid.addGraphObserver(animation);
	}

	public void updateRenderer() {
		popRenderer();
		pushRenderer(createRenderer());
		repaint();
	}

	private ConfigurableGridRenderer createRenderer() {
		ConfigurableGridRenderer r = model().getStyle() == Style.PEARLS ? new PearlsGridRenderer()
				: new WallPassageGridRenderer();
		r.fnGridBgColor = () -> Color.BLACK;
		r.fnCellSize = () -> model().getGridCellSize();
		r.fnPassageWidth = (u, v) -> {
			int passageWidth = model().getGridCellSize() * model().getPassageWidthPercentage() / 100;
			if (model().isPassageWidthFluent()) {
				float factor = (float) model().getGrid().col(u) / model().getGridWidth();
				passageWidth = Math.round(factor * passageWidth);
			}
			passageWidth = max(1, passageWidth);
			passageWidth = min(model().getGridCellSize() - 1, passageWidth);
			return passageWidth;
		};
		r.fnCellBgColor = cell -> {
			TraversalState state = model().getGrid().get(cell);
			switch (state) {
			case COMPLETED:
				return model().getCompletedCellColor();
			case UNVISITED:
				return model().getUnvisitedCellColor();
			case VISITED:
				return model().getVisitedCellColor();
			default:
				return r.fnGridBgColor.get();
			}
		};
		r.fnPassageColor = (u, dir) -> {
			if (model().getGrid().degree(u) == 0) {
				return Color.BLACK;
			}
			return r.getCellBgColor(u);
		};
		return r;
	}
}
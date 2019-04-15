package de.amr.demos.maze.swingapp.ui.grid;

import java.awt.Color;
import java.awt.Graphics;
import java.util.function.BiFunction;

import de.amr.demos.maze.swingapp.model.MazeDemoModel.Style;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.GridGraph;
import de.amr.graph.grid.ui.rendering.ConfigurableGridRenderer;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;

/**
 * Display area for maze generation and traversal.
 * 
 * @author Armin Reichert
 */
public class GridView extends GridCanvas {

	private Color gridBackgroundColor;
	private Color unvisitedCellColor;
	private Color visitedCellColor;
	private Color completedCellColor;
	private Color pathColor;
	private Style style;
	private BiFunction<Integer, Integer, Integer> fnPassageWidth;

	public GridView() {
		initProperties();
	}

	public GridView(GridGraph<TraversalState, Integer> grid, int cellSize,
			BiFunction<Integer, Integer, Integer> fnPassageWidth) {
		super(grid, cellSize);
		initProperties();
		this.fnPassageWidth = fnPassageWidth;
		reset(grid, cellSize);
	}

	private void initProperties() {
		gridBackgroundColor = Color.BLACK;
		unvisitedCellColor = Color.BLACK;
		visitedCellColor = Color.BLUE;
		completedCellColor = Color.WHITE;
		pathColor = Color.RED;
		style = Style.WALL_PASSAGES;
		fnPassageWidth = (u, v) -> 1;
	}

	public void reset(GridGraph<TraversalState, Integer> grid, int cellSize) {
		resize(grid, cellSize);
		replaceRenderer(createRenderer(cellSize));
		clear();
		drawGrid();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (getParent() != null) {
			int dx = (getParent().getWidth() - getWidth()) / 2;
			int dy = (getParent().getHeight() - getHeight()) / 2;
			g.translate(dx, dy);
			super.paintComponent(g);
			g.translate(-dx, -dy);
		}
		else {
			super.paintComponent(g);
		}
	}

	public Color getGridBackgroundColor() {
		return gridBackgroundColor;
	}

	public void setGridBackgroundColor(Color color) {
		this.gridBackgroundColor = color;
	}

	public Color getUnvisitedCellColor() {
		return unvisitedCellColor;
	}

	public void setUnvisitedCellColor(Color color) {
		this.unvisitedCellColor = color;
	}

	public Color getVisitedCellColor() {
		return visitedCellColor;
	}

	public void setVisitedCellColor(Color color) {
		this.visitedCellColor = color;
	}

	public Color getCompletedCellColor() {
		return completedCellColor;
	}

	public void setCompletedCellColor(Color color) {
		this.completedCellColor = color;
	}

	public Color getPathColor() {
		return pathColor;
	}

	public void setPathColor(Color color) {
		this.pathColor = color;
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

	private ConfigurableGridRenderer createRenderer(int cellSize) {
		ConfigurableGridRenderer r = getStyle() == Style.PEARLS ? new PearlsGridRenderer()
				: new WallPassageGridRenderer();
		r.fnGridBgColor = () -> getGridBackgroundColor();
		r.fnCellSize = () -> cellSize;
		r.fnPassageWidth = fnPassageWidth;
		r.fnCellBgColor = cell -> {
			TraversalState state = (TraversalState) getGrid().get(cell);
			switch (state) {
			case COMPLETED:
				return getCompletedCellColor();
			case UNVISITED:
				return getUnvisitedCellColor();
			case VISITED:
				return getVisitedCellColor();
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
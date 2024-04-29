package de.amr.maze.demo.ui.grid;

import java.awt.Color;
import java.awt.Font;
import java.util.function.BinaryOperator;
import java.util.function.IntSupplier;

import de.amr.maze.demo.model.GridRenderingStyle;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridGraph2D;
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
public class GridView {

	private final GridCanvas canvas;
	private Color gridBackgroundColor;
	private Color unvisitedCellColor;
	private Color visitedCellColor;
	private Color completedCellColor;
	private GridRenderingStyle style;
	private BinaryOperator<Integer> fnPassageWidth;
	private IntSupplier fnSourceCell;
	private IntSupplier fnTargetCell;

	/**
	 * @wbp.parser.constructor
	 */
	public GridView() {
		initProperties();
		canvas = new GridCanvas();
		canvas.setCentered(true);
	}

	public GridView(GridGraph<TraversalState, Integer> grid, int cellSize, IntSupplier fnSourceCell,
			IntSupplier fnTargetCell, BinaryOperator<Integer> fnPassageWidth) {
		initProperties();
		this.fnPassageWidth = fnPassageWidth;
		this.fnSourceCell = fnSourceCell;
		this.fnTargetCell = fnTargetCell;
		canvas = new GridCanvas(grid, cellSize);
		canvas.setCentered(true);
		canvas.pushRenderer(createRenderer(grid, cellSize));
		reset(grid, cellSize);
	}

	@SuppressWarnings("unchecked")
	public void setStyle(GridRenderingStyle style) {
		this.style = style;
		GridGraph<TraversalState, Integer> grid = (GridGraph<TraversalState, Integer>) canvas.getGrid();
		reset(grid, canvas.getCellSize());
	}

	public void changeGridSize(GridGraph<TraversalState, Integer> grid, int cellSize) {
		canvas.setGrid(grid, false);
		canvas.replaceRenderer(createRenderer(grid, cellSize));
	}

	private void initProperties() {
		gridBackgroundColor = new Color(112, 128, 144);
		unvisitedCellColor = Color.WHITE;
		visitedCellColor = Color.BLUE;
		completedCellColor = Color.WHITE;
		style = GridRenderingStyle.WALL_PASSAGES;
		fnPassageWidth = (u, v) -> 1;
	}

	private ConfigurableGridRenderer createRenderer(GridGraph2D<TraversalState, ?> grid, int cellSize) {
		ConfigurableGridRenderer r;
		if (style == GridRenderingStyle.PEARLS) {
			r = createPearlsRenderer(cellSize);
		} else {
			r = createWallPassageRenderer(cellSize);
		}
		r.fnGridBgColor = () -> gridBackgroundColor;
		r.fnCellSize = () -> cellSize;
		r.fnCellBgColor = cell -> {
			switch (grid.get(cell)) {
			case COMPLETED:
				return completedCellColor;
			case UNVISITED:
				return unvisitedCellColor;
			case VISITED:
				return visitedCellColor;
			default:
				return unvisitedCellColor;
			}
		};
		r.fnText = cell -> {
			if (cell == fnSourceCell.getAsInt()) {
				return "S";
			}
			if (cell == fnTargetCell.getAsInt()) {
				return "T";
			}
			return "";
		};
		r.fnTextColor = cell -> Color.RED;
		r.fnTextFont = cell -> new Font("Arial Narrow", Font.BOLD, cellSize / 2);
		return r;
	}

	private WallPassageGridRenderer createWallPassageRenderer(int cellSize) {
		WallPassageGridRenderer r = new WallPassageGridRenderer();
		r.fnPassageWidth = fnPassageWidth;
		r.fnPassageColor = (cell, dir) -> r.getCellBgColor(cell);
		return r;
	}

	private PearlsGridRenderer createPearlsRenderer(int cellSize) {
		PearlsGridRenderer r = new PearlsGridRenderer();
		r.fnRelativePearlSize = () -> 0.5;
		r.fnPassageWidth = (u, v) -> 1;
		r.fnPassageColor = (u, v) -> completedCellColor;
		return r;
	}

	public void reset(GridGraph<TraversalState, Integer> grid, int cellSize) {
		canvas.resize(grid, cellSize);
		canvas.replaceRenderer(createRenderer(grid, cellSize));
		canvas.clear();
		canvas.drawGrid();
	}

	public GridCanvas getCanvas() {
		return canvas;
	}
}
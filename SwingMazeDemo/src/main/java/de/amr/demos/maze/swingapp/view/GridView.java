package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import de.amr.demos.maze.swingapp.MazeDemoApp;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Style;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.graph.grid.ui.rendering.ConfigurableGridRenderer;
import de.amr.graph.grid.ui.rendering.GridCanvas;
import de.amr.graph.grid.ui.rendering.PearlsGridRenderer;
import de.amr.graph.grid.ui.rendering.WallPassageGridRenderer;
import de.amr.util.StopWatch;

/**
 * Display area for maze generation and traversal.
 * 
 * @author Armin Reichert
 */
public class GridView extends GridCanvas implements PropertyChangeListener {

	private MazeDemoModel model;
	private GridCanvasAnimation<TraversalState, Integer> animation;
	private Color gridBackgroundColor;
	private Color unvisitedCellColor;
	private Color visitedCellColor;
	private Color completedCellColor;
	private Color pathColor;
	private Style style;

	private final Action actionShowControls = new AbstractAction("Show Controls") {

		@Override
		public void actionPerformed(ActionEvent e) {
			MazeDemoApp.app().getControlWindow().setVisible(true);
		}
	};

	public GridView() {
		gridBackgroundColor = Color.BLACK;
		unvisitedCellColor = Color.BLACK;
		visitedCellColor = Color.BLUE;
		completedCellColor = Color.WHITE;
		pathColor = Color.RED;
		style = Style.WALL_PASSAGES;
	}

	public GridView(MazeDemoModel model) {
		this();
		this.model = model;

		animation = new GridCanvasAnimation<>(this);
		animation.fnDelay = () -> model.getDelay();
		model.getGrid().addGraphObserver(animation);

		getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "showSettings");
		getActionMap().put("showSettings", actionShowControls);

		setGrid(model.getGrid(), false);
		setCellSize(model.getGridCellSize(), false);
		replaceRenderer(createRenderer());
		clear();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		int dx = (app().getDisplayMode().getWidth() - getPreferredSize().width) / 2;
		int dy = (app().getDisplayMode().getHeight() - getPreferredSize().height) / 2;
		g.translate(dx, dy);
		super.paintComponent(g);
		g.translate(-dx, -dy);
	}

	public void enableAnimation(boolean enabled) {
		animation.setEnabled(enabled);
	}

	public Color getGridBackgroundColor() {
		return gridBackgroundColor;
	}

	public void setGridBackgroundColor(Color gridBackgroundColor) {
		this.gridBackgroundColor = gridBackgroundColor;
	}

	public Color getUnvisitedCellColor() {
		return unvisitedCellColor;
	}

	public void setUnvisitedCellColor(Color unvisitedCellColor) {
		this.unvisitedCellColor = unvisitedCellColor;
	}

	public Color getVisitedCellColor() {
		return visitedCellColor;
	}

	public void setVisitedCellColor(Color visitedCellColor) {
		this.visitedCellColor = visitedCellColor;
	}

	public Color getCompletedCellColor() {
		return completedCellColor;
	}

	public void setCompletedCellColor(Color completedCellColor) {
		this.completedCellColor = completedCellColor;
	}

	public Color getPathColor() {
		return pathColor;
	}

	public void setPathColor(Color pathColor) {
		this.pathColor = pathColor;
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
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
		}
		else if ("passageWidthPercentage".equals(change.getPropertyName())) {
			clear();
			drawGrid();
		}
	}

	@Override
	public void drawGrid() {
		StopWatch watch = new StopWatch();
		watch.measure(super::drawGrid);
		System.out.println(String.format("%s, drawing time: %.0f ms", getGrid(), watch.getMillis()));
	}

	private ConfigurableGridRenderer createRenderer() {
		ConfigurableGridRenderer r = getStyle() == Style.PEARLS ? new PearlsGridRenderer()
				: new WallPassageGridRenderer();
		r.fnGridBgColor = () -> getGridBackgroundColor();
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
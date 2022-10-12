package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.swing.MySwingUtils.action;
import static de.amr.swing.MySwingUtils.icon;
import static de.amr.swing.MySwingUtils.setEnabled;
import static de.amr.swing.MySwingUtils.setNormalCursor;
import static de.amr.swing.MySwingUtils.setWaitCursor;

import java.awt.Desktop;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GridRenderingStyle;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGeneration;
import de.amr.demos.maze.swingapp.ui.control.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.ui.control.action.CreateSingleMazeAction;
import de.amr.demos.maze.swingapp.ui.control.action.FloodFillAction;
import de.amr.demos.maze.swingapp.ui.control.action.SaveImageAction;
import de.amr.demos.maze.swingapp.ui.control.action.SolveMazeAction;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.demos.maze.swingapp.ui.grid.GridView;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridTopology;
import de.amr.graph.grid.impl.Grid4Topology;
import de.amr.graph.grid.impl.Grid8Topology;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.DFSAnimation;
import de.amr.graph.pathfinder.api.ObservableGraphSearch;
import de.amr.util.StopWatch;

/**
 * Controls the UI for maze generation and solving.
 * 
 * @author Armin Reichert
 */
public class ControlUI implements PropertyChangeListener {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final String WALLS_PASSAGES = "Walls-Passages";
	public static final String PEARLS = "Pearls";

	private final MazeDemoModel model;

	private final GridUI gridUI;

	private Thread bgThread;
	private boolean hiddenWhenBusy;
	private AfterGeneration afterGeneration;

	private final JFrame window;
	private final ControlUIMenus menus;
	private final ControlView view;

	// package visible to be accessible by menus
	Action actionCollapseWindow;
	Action actionExpandWindow;
	Action actionChangeGridResolution;
	Action actionChangeGridTopology;
	Action actionChangeRenderingStyle;
	Action actionCreateEmptyGrid;
	Action actionCreateFullGrid;
	Action actionCreateSparseRandomGrid;
	Action actionCreateDenseRandomGrid;
	Action actionStopBackgroundThread;
	Action actionClearCanvas;
	Action actionCreateSingleMaze;
	Action actionCreateAllMazes;
	Action actionSolveMaze;
	Action actionFloodFill;
	Action actionSaveImage;
	Action actionVisitOnGitHub;

	private ComboBoxModel<String> renderingStyles4neighbors = new DefaultComboBoxModel<>(
			new String[] { WALLS_PASSAGES, PEARLS });

	private ComboBoxModel<String> renderingStyles8neighbors = new DefaultComboBoxModel<>(new String[] { PEARLS });

	public ControlUI(GridUI gridUI, MazeDemoModel model) {
		this.gridUI = gridUI;
		this.model = model;
		view = new ControlView();

		createActions(gridUI, model);

		// create and initialize UI

		var entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = getCanvasWidth() / cellSize;
			int numRows = getCanvasHeight() / cellSize;
			return String.format("%d cells (%d cols x %d rows, cell size %d)", numCols * numRows, numCols, numRows, cellSize);
		}).toArray(String[]::new);
		view.getComboGridResolution().setModel(new DefaultComboBoxModel<>(entries));
		view.getComboGridResolution().setSelectedIndex(model.getGridCellSizeIndex());
		view.getComboGridResolution().setAction(actionChangeGridResolution);

		var topologies = new GridTopology[] { Grid4Topology.get(), Grid8Topology.get() };
		view.getComboGridTopology().setModel(new DefaultComboBoxModel<>(topologies));
		view.getComboGridTopology().setSelectedItem(model.getGridTopology());
		view.getComboGridTopology().setAction(actionChangeGridTopology);

		view.getComboRenderingStyle().setModel(
				model.getGridTopology() == Grid4Topology.get() ? renderingStyles4neighbors : renderingStyles8neighbors);
		view.getComboRenderingStyle().setSelectedIndex(0);
		view.getComboRenderingStyle().setAction(actionChangeRenderingStyle);

		view.getSliderPassageWidth().setValue(model.getPassageWidthPercentage());
		view.getSliderPassageWidth().addChangeListener(e -> {
			if (!view.getSliderPassageWidth().getValueIsAdjusting()) {
				model.setPassageWidthPercentage(view.getSliderPassageWidth().getValue());
			}
		});

		view.getSliderDelay().setMinimum(0);
		view.getSliderDelay().setMaximum(100);
		view.getSliderDelay().setValue(model.getDelay());
		view.getSliderDelay().setMinorTickSpacing(10);
		view.getSliderDelay().setMajorTickSpacing(50);
		view.getSliderDelay().addChangeListener(e -> {
			if (!view.getSliderDelay().getValueIsAdjusting()) {
				model.setDelay(view.getSliderDelay().getValue());
			}
		});

		view.getBtnCreateMaze().setAction(actionCreateSingleMaze);
		view.getBtnCreateAllMazes().setAction(actionCreateAllMazes);
		view.getBtnSolve().setAction(actionSolveMaze);
		view.getBtnStop().setAction(actionStopBackgroundThread);

		window = new JFrame();
		window.setTitle("Maze Demo App - Control View");
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setContentPane(view);

		menus = new ControlUIMenus(this);
		menus.updateSelection();
		window.setJMenuBar(new JMenuBar());
		window.getJMenuBar().add(menus.getGeneratorMenu());
		window.getJMenuBar().add(menus.getSolverMenu());
		window.getJMenuBar().add(menus.getCanvasMenu());
		window.getJMenuBar().add(menus.getOptionMenu());
		window.getJMenuBar().add(menus.getAboutMenu());
	}

	private void createActions(GridUI gridUI, MazeDemoModel model) {
		afterGeneration = AfterGeneration.DO_NOTHING;
		actionCollapseWindow = action("Hide Details", icon("/zoom_out.png"), e -> collapseWindow());
		actionExpandWindow = action("Show Details", icon("/zoom_in.png"), e -> expandWindow());
		actionChangeGridResolution = action("Change Resolution", e -> {
			JComboBox<?> combo = (JComboBox<?>) e.getSource();
			model.setGridCellSizeIndex(combo.getSelectedIndex());
			reset();
			combo.requestFocusInWindow();
		});
		actionChangeGridTopology = action("Change Topology", e -> {
			@SuppressWarnings("unchecked")
			JComboBox<GridTopology> combo = (JComboBox<GridTopology>) e.getSource();
			model.setGridTopology(combo.getItemAt(combo.getSelectedIndex()));
			model.emptyGrid();
			view.getComboRenderingStyle().setModel(
					model.getGridTopology() == Grid4Topology.get() ? renderingStyles4neighbors : renderingStyles8neighbors);
			view.getComboRenderingStyle().setSelectedIndex(0);
		});
		actionChangeRenderingStyle = action("Change Style", e -> {
			@SuppressWarnings("unchecked")
			JComboBox<GridTopology> combo = (JComboBox<GridTopology>) e.getSource();
			if (WALLS_PASSAGES.equals(combo.getSelectedItem())) {
				if (model.getGridTopology() == Grid4Topology.get()) {
					model.setRenderingStyle(GridRenderingStyle.WALL_PASSAGES);
				} else {
					combo.setSelectedIndex(1);
				}
			} else if (PEARLS.equals(combo.getSelectedItem())) {
				model.setRenderingStyle(GridRenderingStyle.PEARLS);
			}
		});
		actionCreateEmptyGrid = action("Empty Grid", e -> model.emptyGrid());
		actionCreateFullGrid = action("Full Grid", e -> model.fullGrid());
		actionCreateSparseRandomGrid = action("Sparse Grid", e -> model.randomGrid(true));
		actionCreateDenseRandomGrid = action("Dense Grid", e -> model.randomGrid(false));
		actionClearCanvas = action("Clear Canvas", e -> {
			gridUI.clear();
			gridUI.drawGrid();
		});
		actionStopBackgroundThread = action("Stop", e -> stopBackgroundThread());
		actionCreateAllMazes = new CreateAllMazes("All Mazes", this, gridUI);
		actionCreateSingleMaze = new CreateSingleMazeAction("New Maze", this, gridUI);
		actionSolveMaze = new SolveMazeAction("Solve", this, gridUI);
		actionFloodFill = new FloodFillAction("Flood-fill", this, gridUI);
		actionSaveImage = new SaveImageAction("Save Image...", this, gridUI);

		actionVisitOnGitHub = action("Visit me on GitHub", icon("/GitHub-Mark-32px.png"), e -> {
			if (Desktop.isDesktopSupported()) {
				var url = "https://github.com/armin-reichert/mazes";
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (Exception x) {
					LOGGER.error("Could not browse URL '%s'".formatted(url));
				}
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "delay":
			view.getSliderDelay().setValue((Integer) change.getNewValue());
			break;
		case "grid":
			if (model.getGridTopology() == Grid8Topology.get()) {
				view.getComboRenderingStyle().setSelectedItem(PEARLS);
			}
			break;
		case "gridCellSizeIndex":
			break;
		case "metric":
			getSelectedSolver().ifPresent(this::updateSolverText);
			break;
		case "passageWidthPercentage":
			break;
		case "renderingStyle": {
			GridRenderingStyle style = (GridRenderingStyle) change.getNewValue();
			int selection = style == GridRenderingStyle.WALL_PASSAGES ? 0 : 1;
			view.getComboRenderingStyle().setSelectedIndex(selection);
			break;
		}
		default:
			LOGGER.info(() -> "Unhandled property change %s".formatted(change));
			break;
		}
	}

	public void startBackgroundThread(Runnable code, Consumer<AnimationInterruptedException> onInterruption,
			Consumer<Throwable> onFailure) {
		bgThread = new Thread(() -> {
			setBusy(true);
			code.run();
			setBusy(false);
		}, "MazeDemoWorker");
		bgThread.setUncaughtExceptionHandler((thread, e) -> {
			if (e.getClass() == AnimationInterruptedException.class) {
				onInterruption.accept((AnimationInterruptedException) e);
				getModel().setDelay(0); // HACK: reset delay that has been set to allow stopping animation
			} else {
				onFailure.accept(e);
			}
			setBusy(false);
		});
		bgThread.start();
	}

	public void stopBackgroundThread() {
		// TODO this is a hack to be able to interrupt thread in case no delay is set
		if (getModel().getDelay() == 0) {
			getModel().setDelay(10);
		}
		bgThread.interrupt();
	}

	public boolean isHiddenWhenBusy() {
		return hiddenWhenBusy;
	}

	public void setHiddenWhenBusy(boolean b) {
		this.hiddenWhenBusy = b;
		if (menus != null) {
			menus.updateSelection();
		}
	}

	public AfterGeneration getAfterGeneration() {
		return afterGeneration;
	}

	public void setAfterGeneration(AfterGeneration action) {
		this.afterGeneration = action;
		if (menus != null) {
			menus.updateSelection();
		}
	}

	public JFrame getWindow() {
		return window;
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public void placeWindowRelativeTo(Window parentWindow) {
		window.setLocation(parentWindow.getX() + (parentWindow.getWidth() - window.getWidth()) / 2,
				parentWindow.getY() + 42);
	}

	public void show() {
		window.setVisible(true);
		window.requestFocusInWindow();
	}

	public void collapseWindow() {
		view.getCollapsibleArea().setVisible(false);
		view.getBtnShowHideDetails().setAction(actionExpandWindow);
		window.pack();
		window.setSize(window.getWidth(), window.getHeight() - view.getCollapsibleArea().getHeight());
	}

	public void expandWindow() {
		view.getCollapsibleArea().setVisible(true);
		view.getBtnShowHideDetails().setAction(actionCollapseWindow);
		window.pack();
	}

	public void runSelectedSolver() {
		getSelectedSolver().ifPresent(this::runSolver);
	}

	private void runSolver(Algorithm solver) {
		ObservableGraphSearch solverInstance = model.createSolverInstance(solver);
		int source = model.getGrid().cell(model.getSolverSource());
		int target = model.getGrid().cell(model.getSolverTarget());
		GridView gridView = gridUI.getView();
		StopWatch watch = new StopWatch();
		if (solver.isTagged(SolverTag.BFS)) {
			/*@formatter:off*/
			BFSAnimation anim = BFSAnimation.builder()
					.canvas(gridView.getCanvas())
					.delay(model::getDelay)
					.distanceVisible(model.isDistancesVisible())
					.build();
			/*@formatter:on*/
			watch.measure(() -> anim.run(solverInstance, source, target));
			anim.showPath(solverInstance, source, target);
		} else if (solver.isTagged(SolverTag.DFS)) {
			/*@formatter:off*/
			DFSAnimation anim = DFSAnimation.builder()
					.canvas(gridView.getCanvas())
					.delay(model::getDelay)
					.build();
			/*@formatter:on*/
			watch.measure(() -> anim.run(solverInstance, source, target));
		}
		if (solver.isTagged(SolverTag.INFORMED)) {
			showMessage("%s (%s): %.2f seconds.", solver.getDescription(), model.getMetric(), watch.getSeconds());
		} else {
			showMessage("%s: %.2f seconds.", solver.getDescription(), watch.getSeconds());
		}
	}

	public void setBusy(boolean busy) {
		if (busy) {
			if (hiddenWhenBusy) {
				window.setVisible(false);
			}
			setEnabled(false, menus.getGeneratorMenu(), menus.getSolverMenu(), menus.getCanvasMenu(), menus.getOptionMenu());
			setEnabled(false, actionChangeGridResolution, actionChangeGridTopology, actionChangeRenderingStyle,
					actionCreateSingleMaze, actionCreateAllMazes, actionSolveMaze);
			view.getSliderPassageWidth().setEnabled(false);
			setWaitCursor(view);
			setNormalCursor(view.getBtnStop(), view.getBtnShowHideDetails(), view.getSliderDelay());
		} else {
			window.setVisible(true);
			setEnabled(true, menus.getGeneratorMenu(), menus.getSolverMenu(), menus.getCanvasMenu(), menus.getOptionMenu());
			setEnabled(true, actionChangeGridResolution, actionChangeGridTopology, actionChangeRenderingStyle,
					actionCreateSingleMaze, actionCreateAllMazes, actionSolveMaze);
			view.getSliderPassageWidth().setEnabled(true);
			setNormalCursor(view);
		}
	}

	public void reset() {
		setBusy(true);
		int numCols = getCanvasWidth() / model.getGridCellSize();
		int numRows = getCanvasHeight() / model.getGridCellSize();
		boolean full = model.getGrid().isFull();
		model.createGridSilently(numCols, numRows, full, TraversalState.UNVISITED);
		gridUI.reset();
		setBusy(false);
	}

	private int getCanvasHeight() {
		return gridUI.getView().getCanvas().getHeight();
	}

	private int getCanvasWidth() {
		return gridUI.getView().getCanvas().getWidth();
	}

	public void showMessage(String msg, Object... args) {
		String text = msg;
		if (args.length > 0) {
			text = String.format(msg, args);
		}
		view.getTextArea().append(text + "\n");
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<Algorithm> getSelectedGenerator() {
		return menus.getSelectedGenerator();
	}

	public void selectGenerator(Algorithm generator) {
		menus.selectGenerator(generator);
		updateGeneratorText(generator);
		model.emptyGrid();
	}

	public Optional<Algorithm> getSelectedSolver() {
		return menus.getSelectedSolver();
	}

	public void selectSolver(Algorithm solver) {
		menus.selectSolver(solver);
		updateSolverText(solver);
	}

	private void updateSolverText(Algorithm solver) {
		String text = solver.getDescription();
		if (solver.isTagged(SolverTag.INFORMED)) {
			String metric = model.getMetric().toString();
			metric = metric.substring(0, 1) + metric.substring(1).toLowerCase();
			text += " (" + metric + ")";
		}
		view.getLblSolverName().setText(text);
	}

	private void updateGeneratorText(Algorithm generator) {
		view.getLblGeneratorName().setText(generator.getDescription());
	}
}
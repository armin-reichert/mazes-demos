package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.demos.maze.swingapp.ui.common.MenuBuilder.updateMenuSelection;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildCanvasMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildGeneratorMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildOptionMenu;
import static de.amr.demos.maze.swingapp.ui.control.ControlWindowMenus.buildSolverMenu;
import static de.amr.swing.Swing.action;
import static de.amr.swing.Swing.icon;
import static de.amr.swing.Swing.setEnabled;
import static de.amr.swing.Swing.setNormalCursor;
import static de.amr.swing.Swing.setWaitCursor;
import static java.lang.String.format;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.demos.maze.swingapp.ui.control.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.ui.control.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.ui.control.action.FloodFill;
import de.amr.demos.maze.swingapp.ui.control.action.SaveImage;
import de.amr.demos.maze.swingapp.ui.control.action.SolveMaze;
import de.amr.demos.maze.swingapp.ui.grid.GridView;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.DFSAnimation;
import de.amr.graph.pathfinder.api.ObservableGraphSearch;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.graph.pathfinder.impl.BidiAStarSearch;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.graph.pathfinder.impl.BidiDijkstraSearch;
import de.amr.graph.pathfinder.impl.BreadthFirstSearch;
import de.amr.graph.pathfinder.impl.DepthFirstSearch;
import de.amr.graph.pathfinder.impl.DepthFirstSearch2;
import de.amr.graph.pathfinder.impl.DijkstraSearch;
import de.amr.graph.pathfinder.impl.HillClimbingSearch;
import de.amr.graph.pathfinder.impl.IDDFS;
import de.amr.util.StopWatch;

/**
 * Controls the UI for maze generation and solving.
 * 
 * @author Armin Reichert
 */
public class ControlViewController implements PropertyChangeListener {

	private final MazeDemoModel model;

	private final GridViewController gridViewController;

	private Thread bgThread;
	private boolean hiddenWhenBusy;
	private AfterGenerationAction afterGenerationAction;

	private final JFrame window;
	private final JMenu generatorMenu;
	private final JMenu canvasMenu;
	private final JMenu solverMenu;
	private final JMenu optionMenu;
	private final ControlView view;

	final Action actionCollapseWindow;
	final Action actionExpandWindow;
	final Action actionChangeGridResolution;
	final Action actionCreateEmptyGrid;
	final Action actionCreateFullGrid;
	final Action actionStopBackgroundThread;
	final Action actionClearCanvas;
	final Action actionCreateSingleMaze;
	final Action actionCreateAllMazes;
	final Action actionSolveMaze;
	final Action actionFloodFill;
	final Action actionSaveImage;

	public ControlViewController(GridViewController gridViewController) {

		this.gridViewController = gridViewController;

		// connect controller with model
		this.model = gridViewController.getModel();
		model.changePublisher.addPropertyChangeListener(this);

		afterGenerationAction = AfterGenerationAction.NOTHING;

		// create actions
		actionCollapseWindow = action("Hide Details", icon("/zoom_out.png"), e -> collapseWindow());
		actionExpandWindow = action("Show Details", icon("/zoom_in.png"), e -> expandWindow());
		actionChangeGridResolution = action("Change Resolution", e -> {
			JComboBox<?> combo = (JComboBox<?>) e.getSource();
			model.setGridCellSizeIndex(combo.getSelectedIndex());
			resetDisplay();
			combo.requestFocusInWindow();
		});
		actionCreateEmptyGrid = action("Create Empty Grid", e -> {
			model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), false, TraversalState.COMPLETED);
		});
		actionCreateFullGrid = action("Create Full Grid", e -> {
			model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), true, TraversalState.COMPLETED);
		});
		actionClearCanvas = action("Clear Canvas", e -> {
			gridViewController.clearView();
			gridViewController.drawGrid();
		});
		actionStopBackgroundThread = action("Stop", e -> stopBackgroundThread());
		actionCreateAllMazes = new CreateAllMazes("All Mazes", this, gridViewController);
		actionCreateSingleMaze = new CreateSingleMaze("New Maze", this, gridViewController);
		actionSolveMaze = new SolveMaze("Solve", this, gridViewController);
		actionFloodFill = new FloodFill("Flood-fill", this, gridViewController);
		actionSaveImage = new SaveImage("Save Image...", this, gridViewController);

		// create and initialize UI
		view = new ControlView();

		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = gridViewController.getWindow().getWidth() / cellSize;
			int numRows = gridViewController.getWindow().getHeight() / cellSize;
			return String.format("%d cells (%d cols x %d rows, cell size %d)", numCols * numRows, numCols, numRows,
					cellSize);
		}).toArray(String[]::new);
		view.getComboGridResolution().setModel(new DefaultComboBoxModel<>(entries));
		view.getComboGridResolution().setSelectedIndex(model.getGridCellSizeIndex());
		view.getComboGridResolution().setAction(actionChangeGridResolution);

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
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setContentPane(view);

		window.setJMenuBar(new JMenuBar());
		window.getJMenuBar().add(generatorMenu = buildGeneratorMenu(this));
		window.getJMenuBar().add(solverMenu = buildSolverMenu(this));
		window.getJMenuBar().add(canvasMenu = buildCanvasMenu(this));
		window.getJMenuBar().add(optionMenu = buildOptionMenu(this));

		updateMenuSelection(generatorMenu);
		updateMenuSelection(solverMenu);
		updateMenuSelection(canvasMenu);
		updateMenuSelection(optionMenu);
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
			}
			else {
				onFailure.accept(e);
			}
			setBusy(false);
		});
		bgThread.start();
	}

	public void stopBackgroundThread() {
		bgThread.interrupt();
	}

	@Override
	public void propertyChange(PropertyChangeEvent change) {
		switch (change.getPropertyName()) {
		case "metric":
			onMetricChanged((Metric) change.getNewValue());
			break;
		default:
			break;
		}
	}

	public boolean isHiddenWhenBusy() {
		return hiddenWhenBusy;
	}

	public void setHiddenWhenBusy(boolean b) {
		this.hiddenWhenBusy = b;
		updateMenuSelection(optionMenu);
	}

	public AfterGenerationAction getAfterGenerationAction() {
		return afterGenerationAction;
	}

	public void setAfterGenerationAction(AfterGenerationAction afterGenerationAction) {
		this.afterGenerationAction = afterGenerationAction;
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

	public void showWindow() {
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

	public void setBusy(boolean busy) {
		if (busy) {
			if (hiddenWhenBusy) {
				window.setVisible(false);
			}
			setEnabled(false, generatorMenu, solverMenu, canvasMenu, optionMenu);
			setEnabled(false, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setWaitCursor(view);
			setNormalCursor(view.getBtnStop(), view.getBtnShowHideDetails(), view.getSliderDelay());
		}
		else {
			window.setVisible(true);
			setEnabled(true, generatorMenu, solverMenu, canvasMenu, optionMenu);
			setEnabled(true, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setNormalCursor(view);
		}
	}

	public void resetDisplay() {
		setBusy(true);
		gridViewController.stopModelChangeListening();
		int numCols = gridViewController.getWindow().getWidth() / model.getGridCellSize();
		int numRows = gridViewController.getWindow().getHeight() / model.getGridCellSize();
		boolean full = model.getGrid().isFull();
		model.createGrid(numCols, numRows, full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
		gridViewController.resetView();
		gridViewController.startModelChangeListening();
		setBusy(false);
	}

	public void showMessage(String msg) {
		view.getTextArea().append(msg + "\n");
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<Algorithm> getSelectedGenerator() {
		return ControlWindowMenus.getSelectedAlgorithm(generatorMenu);
	}

	public void selectGenerator(Algorithm generatorInfo) {
		ControlWindowMenus.selectAlgorithm(generatorMenu, generatorInfo);
		updateGeneratorText(generatorInfo);
		boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
		model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), full,
				full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
	}

	public Optional<Algorithm> getSelectedSolver() {
		return ControlWindowMenus.getSelectedAlgorithm(solverMenu);
	}

	public void selectSolver(Algorithm solverInfo) {
		ControlWindowMenus.selectAlgorithm(solverMenu, solverInfo);
		updateSolverText(solverInfo);
	}

	public void solve() {
		if (!getSelectedSolver().isPresent()) {
			return;
		}
		Algorithm solver = getSelectedSolver().get();
		int targetCell = model.getGrid().cell(model.getSolverTarget());

		if (solver.getAlgorithmClass() == BreadthFirstSearch.class) {
			solve(new BreadthFirstSearch(model.getGrid()), solver);
		}
		else if (solver.getAlgorithmClass() == BidiBreadthFirstSearch.class) {
			solve(new BidiBreadthFirstSearch(model.getGrid(), (u, v) -> 1), solver);
		}
		else if (solver.getAlgorithmClass() == DijkstraSearch.class) {
			solve(new DijkstraSearch(model.getGrid(), (u, v) -> 1), solver);
		}
		else if (solver.getAlgorithmClass() == BidiDijkstraSearch.class) {
			solve(new BidiDijkstraSearch(model.getGrid(), (u, v) -> 1), solver);
		}
		else if (solver.getAlgorithmClass() == BestFirstSearch.class) {
			solve(new BestFirstSearch(model.getGrid(), v -> metric().applyAsDouble(v, targetCell)), solver);
		}
		else if (solver.getAlgorithmClass() == AStarSearch.class) {
			solve(new AStarSearch(model.getGrid(), (u, v) -> 1, metric()), solver);
		}
		else if (solver.getAlgorithmClass() == BidiAStarSearch.class) {
			solve(new BidiAStarSearch(model.getGrid(), (u, v) -> 1, metric(), metric()), solver);
		}
		else if (solver.getAlgorithmClass() == DepthFirstSearch.class) {
			solve(new DepthFirstSearch(model.getGrid()), solver);
		}
		else if (solver.getAlgorithmClass() == DepthFirstSearch2.class) {
			solve(new DepthFirstSearch2(model.getGrid()), solver);
		}
		else if (solver.getAlgorithmClass() == IDDFS.class) {
			solve(new IDDFS(model.getGrid()), solver);
		}
		else if (solver.getAlgorithmClass() == HillClimbingSearch.class) {
			solve(new HillClimbingSearch(model.getGrid(), v -> metric().applyAsDouble(v, targetCell)), solver);
		}
	}

	private void solve(ObservableGraphSearch solverInstance, Algorithm solver) {
		GridView gridView = gridViewController.getView();
		int source = model.getGrid().cell(model.getSolverSource());
		int target = model.getGrid().cell(model.getSolverTarget());
		boolean informed = solver.isTagged(SolverTag.INFORMED);
		StopWatch watch = new StopWatch();
		if (solver.isTagged(SolverTag.BFS)) {
			BFSAnimation anim = BFSAnimation.builder().canvas(gridView.getCanvas()).delay(() -> model.getDelay())
					.pathColor(gridView.getPathColor()).distanceVisible(model.isDistancesVisible()).build();
			watch.measure(() -> anim.run(solverInstance, source, target));
			anim.showPath(solverInstance, source, target);
		}
		else if (solver.isTagged(SolverTag.DFS)) {
			DFSAnimation anim = DFSAnimation.builder().canvas(gridView.getCanvas()).delay(() -> model.getDelay())
					.pathColor(gridView.getPathColor()).build();
			watch.measure(() -> anim.run(solverInstance, source, target));
		}
		showMessage(informed
				? format("%s (%s): %.2f seconds.", solver.getDescription(), model.getMetric(), watch.getSeconds())
				: format("%s: %.2f seconds.", solver.getDescription(), watch.getSeconds()));
	}

	private ToDoubleBiFunction<Integer, Integer> metric() {
		switch (model.getMetric()) {
		case CHEBYSHEV:
			return model.getGrid()::chebyshev;
		case EUCLIDEAN:
			return model.getGrid()::euclidean;
		case MANHATTAN:
			return model.getGrid()::manhattan;
		default:
			throw new IllegalStateException();
		}
	}

	private void onMetricChanged(Metric metric) {
		getSelectedSolver().ifPresent(this::updateSolverText);
	}

	private void updateSolverText(Algorithm solverInfo) {
		String text = solverInfo.getDescription();
		if (solverInfo.isTagged(SolverTag.INFORMED)) {
			String metric = model.getMetric().toString();
			metric = metric.substring(0, 1) + metric.substring(1).toLowerCase();
			text += " (" + metric + ")";
		}
		view.getLblSolverName().setText(text);
	}

	private void updateGeneratorText(Algorithm generatorInfo) {
		view.getLblGeneratorName().setText(generatorInfo.getDescription());
	}
}
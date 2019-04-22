package de.amr.demos.maze.swingapp.ui.control;

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

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.demos.maze.swingapp.ui.control.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.ui.control.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.ui.control.action.FloodFill;
import de.amr.demos.maze.swingapp.ui.control.action.SaveImage;
import de.amr.demos.maze.swingapp.ui.control.action.SolveMaze;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.demos.maze.swingapp.ui.grid.GridView;
import de.amr.graph.core.api.TraversalState;
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

	private final MazeDemoModel model;

	private final GridUI gridUI;

	private Thread bgThread;
	private boolean hiddenWhenBusy;
	private AfterGenerationAction afterGenerationAction;

	private final JFrame window;
	private final ControlUIMenus menus;
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

	public ControlUI(GridUI gridUI) {

		this.gridUI = gridUI;

		// connect controller with model
		this.model = gridUI.getModel();
		model.changePublisher.addPropertyChangeListener(this);

		afterGenerationAction = AfterGenerationAction.NOTHING;

		// create actions
		actionCollapseWindow = action("Hide Details", icon("/zoom_out.png"), e -> collapseWindow());
		actionExpandWindow = action("Show Details", icon("/zoom_in.png"), e -> expandWindow());
		actionChangeGridResolution = action("Change Resolution", e -> {
			JComboBox<?> combo = (JComboBox<?>) e.getSource();
			model.setGridCellSizeIndex(combo.getSelectedIndex());
			reset();
			combo.requestFocusInWindow();
		});
		actionCreateEmptyGrid = action("Create Empty Grid", e -> model.emptyGrid());
		actionCreateFullGrid = action("Create Full Grid", e -> model.fullGrid());
		actionClearCanvas = action("Clear Canvas", e -> {
			gridUI.clear();
			gridUI.drawGrid();
		});
		actionStopBackgroundThread = action("Stop", e -> stopBackgroundThread());
		actionCreateAllMazes = new CreateAllMazes("All Mazes", this, gridUI);
		actionCreateSingleMaze = new CreateSingleMaze("New Maze", this, gridUI);
		actionSolveMaze = new SolveMaze("Solve", this, gridUI);
		actionFloodFill = new FloodFill("Flood-fill", this, gridUI);
		actionSaveImage = new SaveImage("Save Image...", this, gridUI);

		// create and initialize UI
		view = new ControlView();

		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = getCanvasWidth() / cellSize;
			int numRows = getCanvasHeight() / cellSize;
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

		menus = new ControlUIMenus(this);
		menus.updateSelection();
		window.setJMenuBar(new JMenuBar());
		window.getJMenuBar().add(menus.getGeneratorMenu());
		window.getJMenuBar().add(menus.getSolverMenu());
		window.getJMenuBar().add(menus.getCanvasMenu());
		window.getJMenuBar().add(menus.getOptionMenu());
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
			getSelectedSolver().ifPresent(this::updateSolverText);
			break;
		default:
			System.out.println(String.format("%10s ignored %s", getClass().getSimpleName(), change));
			break;
		}
	}

	public boolean isHiddenWhenBusy() {
		return hiddenWhenBusy;
	}

	public void setHiddenWhenBusy(boolean b) {
		this.hiddenWhenBusy = b;
		menus.updateSelection();
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

	public void solve() {
		getSelectedSolver().ifPresent(this::solve);
	}

	private void solve(Algorithm solver) {
		ObservableGraphSearch solverInstance = model.createSolverInstance(solver);
		int source = model.getGrid().cell(model.getSolverSource());
		int target = model.getGrid().cell(model.getSolverTarget());
		GridView gridView = gridUI.getView();
		StopWatch watch = new StopWatch();
		if (solver.isTagged(SolverTag.BFS)) {
			/*@formatter:off*/
			BFSAnimation anim = BFSAnimation.builder()
					.canvas(gridView.getCanvas())
					.delay(() -> model.getDelay())
					.distanceVisible(model.isDistancesVisible())
					.build();
			/*@formatter:on*/
			watch.measure(() -> anim.run(solverInstance, source, target));
			anim.showPath(solverInstance, source, target);
		}
		else if (solver.isTagged(SolverTag.DFS)) {
			/*@formatter:off*/
			DFSAnimation anim = DFSAnimation.builder()
					.canvas(gridView.getCanvas())
					.delay(() -> model.getDelay())
					.build();
			/*@formatter:on*/
			watch.measure(() -> anim.run(solverInstance, source, target));
		}
		showMessage(solver.isTagged(SolverTag.INFORMED)
				? format("%s (%s): %.2f seconds.", solver.getDescription(), model.getMetric(), watch.getSeconds())
				: format("%s: %.2f seconds.", solver.getDescription(), watch.getSeconds()));
	}

	public void setBusy(boolean busy) {
		if (busy) {
			if (hiddenWhenBusy) {
				window.setVisible(false);
			}
			setEnabled(false, menus.getGeneratorMenu(), menus.getSolverMenu(), menus.getCanvasMenu(),
					menus.getOptionMenu());
			setEnabled(false, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setWaitCursor(view);
			setNormalCursor(view.getBtnStop(), view.getBtnShowHideDetails(), view.getSliderDelay());
		}
		else {
			window.setVisible(true);
			setEnabled(true, menus.getGeneratorMenu(), menus.getSolverMenu(), menus.getCanvasMenu(),
					menus.getOptionMenu());
			setEnabled(true, actionChangeGridResolution, actionCreateSingleMaze, actionCreateAllMazes,
					actionSolveMaze);
			setNormalCursor(view);
		}
	}

	public void reset() {
		setBusy(true);
		int numCols = getCanvasWidth() / model.getGridCellSize();
		int numRows = getCanvasHeight() / model.getGridCellSize();
		boolean full = model.getGrid().isFull();
		model.createGrid(numCols, numRows, full, TraversalState.UNVISITED);
		gridUI.reset();
		setBusy(false);
	}

	private int getCanvasHeight() {
		return gridUI.getView().getCanvas().getHeight();
	}

	private int getCanvasWidth() {
		return gridUI.getView().getCanvas().getWidth();
	}

	public void showMessage(String msg) {
		view.getTextArea().append(msg + "\n");
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<Algorithm> getSelectedGenerator() {
		return menus.getSelectedGenerator();
	}

	public void selectGenerator(Algorithm generator) {
		menus.selectGenerator(generator);
		updateGeneratorText(generator);
		if (generator.isTagged(GeneratorTag.FullGridRequired)) {
			model.fullGrid();
		}
		else {
			model.emptyGrid();
		}
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
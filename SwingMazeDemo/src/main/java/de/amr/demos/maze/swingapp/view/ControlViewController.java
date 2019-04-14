package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import de.amr.demos.maze.swingapp.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.action.FloodFill;
import de.amr.demos.maze.swingapp.action.SaveImage;
import de.amr.demos.maze.swingapp.action.SolveMaze;
import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.view.menu.GeneratorMenu;
import de.amr.demos.maze.swingapp.view.menu.OptionMenu;
import de.amr.demos.maze.swingapp.view.menu.SolverMenu;
import de.amr.graph.core.api.TraversalState;

/**
 * View controller for control UI.
 * 
 * @author Armin Reichert
 */
public class ControlViewController {

	private static final String ICON_ZOOM_IN = "/zoom_in.png";
	private static final String ICON_ZOOM_OUT = "/zoom_out.png";
	private static final int COLLAPSED_WINDOW_HEIGHT = 160;

	private MazeDemoModel model;

	private JFrame window;
	private ControlView view;
	private GeneratorMenu generatorMenu;
	private JMenu canvasMenu;
	private SolverMenu solverMenu;
	private OptionMenu optionMenu;

	private final Action actionCollapseWindow = new AbstractAction("Hide Details", loadIcon(ICON_ZOOM_OUT)) {

		@Override
		public void actionPerformed(ActionEvent e) {
			collapseWindow();
		}
	};

	private final Action actionExpandWindow = new AbstractAction("Show Details", loadIcon(ICON_ZOOM_IN)) {

		@Override
		public void actionPerformed(ActionEvent e) {
			expandWindow();
		}
	};

	private final Action actionChangeGridResolution = new AbstractAction("Change Resolution") {

		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> combo = (JComboBox<?>) e.getSource();
			int cellSize = model.getGridCellSizes()[combo.getSelectedIndex()];
			app().resizeGrid(cellSize);
		}
	};

	private final Action actionCreateEmptyGrid = new AbstractAction("Create Empty Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model.createGrid(false, TraversalState.COMPLETED);
		}
	};

	private final Action actionCreateFullGrid = new AbstractAction("Create Full Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model.createGrid(true, TraversalState.COMPLETED);
		}
	};

	private final Action actionStopBackgroundThread = new AbstractAction("Stop") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().stopBackgroundThread();
		}
	};

	private final Action actionClearCanvas = new AbstractAction("Clear Canvas") {

		@Override
		public void actionPerformed(ActionEvent e) {
			app().getGridViewController().clear();
			app().getGridViewController().drawGrid();
		}
	};

	private final Action actionCreateSingleMaze = new CreateSingleMaze();
	private final Action actionCreateAllMazes = new CreateAllMazes();
	private final Action actionSolveMaze = new SolveMaze();
	private final Action actionFloodFill = new FloodFill();
	private final Action actionSaveImage = new SaveImage();

	public ControlViewController() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Control View");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		view = new ControlView();
		window.setContentPane(view);
	}

	public ControlViewController(MazeDemoModel model) {
		this();
		this.model = model;

		view.getComboGridResolution().setModel(createGridResolutionModel());
		view.getComboGridResolution().setSelectedIndex(getSelectedGridResolutionIndex().orElse(-1));
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
		view.getBtnFindPath().setAction(actionSolveMaze);
		view.getBtnStop().setAction(actionStopBackgroundThread);

		// Menus
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);

		generatorMenu = new GeneratorMenu(this);
		menuBar.add(generatorMenu);

		solverMenu = new SolverMenu(this);
		menuBar.add(solverMenu);

		canvasMenu = new JMenu("Canvas");
		canvasMenu.add(actionClearCanvas);
		canvasMenu.add(actionFloodFill);
		canvasMenu.addSeparator();
		canvasMenu.add(actionCreateEmptyGrid);
		canvasMenu.add(actionCreateFullGrid);
		canvasMenu.addSeparator();
		canvasMenu.add(actionSaveImage);
		menuBar.add(canvasMenu);

		optionMenu = new OptionMenu(model);
		menuBar.add(optionMenu);
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public void placeWindow() {
		window.setLocation((app().getDisplayMode().getWidth() - window.getWidth()) / 2, 42);
	}

	public void showWindow() {
		window.setVisible(true);
		window.requestFocusInWindow();
	}

	public void collapseWindow() {
		view.getContent().setVisible(false);
		view.getBtnShowHideDetails().setAction(actionExpandWindow);
		window.pack();
		window.setSize(window.getWidth(), COLLAPSED_WINDOW_HEIGHT);
	}

	public void expandWindow() {
		view.getContent().setVisible(true);
		view.getBtnShowHideDetails().setAction(actionCollapseWindow);
		window.pack();
	}

	public void setBusy(boolean busy) {
		window.setVisible(!busy || !model.isHidingControlsWhenRunning());
		boolean enabled = !busy;
		generatorMenu.setEnabled(enabled);
		solverMenu.setEnabled(enabled);
		canvasMenu.setEnabled(enabled);
		optionMenu.setEnabled(enabled);
		actionChangeGridResolution.setEnabled(enabled);
		actionCreateSingleMaze.setEnabled(enabled);
		actionCreateAllMazes.setEnabled(enabled);
		actionSolveMaze.setEnabled(enabled);
		view.getSliderPassageWidth().setEnabled(enabled);
		view.getBtnStop().setEnabled(busy);
	}

	public void showMessage(String msg) {
		view.getTextArea().append(msg);
		view.getTextArea().setCaretPosition(view.getTextArea().getDocument().getLength());
	}

	public Optional<AlgorithmInfo> getSelectedGenerator() {
		return generatorMenu.getSelectedAlgorithm();
	}

	public void selectSolver(AlgorithmInfo solverInfo) {
		solverMenu.selectAlgorithm(solverInfo);
		updateSolverText(solverInfo);
	}

	public Optional<AlgorithmInfo> getSelectedSolver() {
		return solverMenu.getSelectedAlgorithm();
	}

	public void selectGenerator(AlgorithmInfo generatorInfo) {
		generatorMenu.selectAlgorithm(generatorInfo);
		updateGeneratorText(generatorInfo);
	}

	private void updateSolverText(AlgorithmInfo solverInfo) {
		String text = solverInfo.getDescription();
		if (solverInfo.isTagged(SolverTag.INFORMED)) {
			String metric = model.getMetric().toString();
			metric = metric.substring(0, 1) + metric.substring(1).toLowerCase();
			text += " (" + metric + ")";
		}
		view.getLblSolverName().setText(text);
	}

	private void updateGeneratorText(AlgorithmInfo generatorInfo) {
		view.getLblGeneratorName().setText(generatorInfo.getDescription());
	}

	private ComboBoxModel<String> createGridResolutionModel() {
		String tmpl = "%d cells (%d cols x %d rows, cell size %d)";
		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = app().getDisplayMode().getWidth() / cellSize;
			int numRows = app().getDisplayMode().getHeight() / cellSize;
			return String.format(tmpl, numCols * numRows, numCols, numRows, cellSize);
		}).toArray(String[]::new);
		return new DefaultComboBoxModel<>(entries);
	}

	private OptionalInt getSelectedGridResolutionIndex() {
		return IntStream.range(0, model.getGridCellSizes().length)
				.filter(index -> model.getGridCellSizes()[index] == model.getGridCellSize()).findFirst();
	}

	private Icon loadIcon(String resourceName) {
		return new ImageIcon(getClass().getResource(resourceName));
	}
}
package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.DisplayMode;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;

import de.amr.demos.maze.swingapp.MazeDemoApp;
import de.amr.demos.maze.swingapp.action.ChangeGridResolution;
import de.amr.demos.maze.swingapp.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.action.FloodFill;
import de.amr.demos.maze.swingapp.action.SaveImage;
import de.amr.demos.maze.swingapp.action.SolveMaze;
import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.PathFinderTag;
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

	private ComboBoxModel<String> createGridResolutionModel() {
		String tmpl = "%d cells (%d cols x %d rows, cell size %d)";
		String[] entries = Arrays.stream(model.getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = app().getDisplayMode().getWidth() / cellSize;
			int numRows = app().getDisplayMode().getHeight() / cellSize;
			return String.format(tmpl, numCols * numRows, numCols, numRows, cellSize);
		}).toArray(String[]::new);
		return new DefaultComboBoxModel<>(entries);
	}

	private int getSelectedGridResolutionIndex() {
		int index = 0;
		for (int size : model.getGridCellSizes()) {
			if (size == model.getGridCellSize()) {
				return index;
			}
			++index;
		}
		return -1;
	}

	private Icon loadIcon(String resourceName) {
		return new ImageIcon(getClass().getResource(resourceName));
	}

	private MazeDemoModel model;

	private JFrame window;
	private GeneratorMenu generatorMenu;
	private JMenu canvasMenu;
	private SolverMenu solverMenu;
	private OptionMenu optionMenu;
	private ControlView controlView;

	private final Action actionCollapseWindow = new AbstractAction() {

		{
			putValue(Action.NAME, "Hide Details");
			putValue(Action.LARGE_ICON_KEY, loadIcon(ICON_ZOOM_OUT));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			collapseWindow();
		}
	};

	private final Action actionExpandWindow = new AbstractAction() {

		{
			putValue(Action.NAME, "Show Details");
			putValue(Action.LARGE_ICON_KEY, loadIcon(ICON_ZOOM_IN));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			expandWindow();
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
			MazeDemoApp.app().stopBackgroundThread();
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
	private final Action actionChangeGridResolution = new ChangeGridResolution();
	private final Action actionFloodFill = new FloodFill();
	private final Action actionSaveImage = new SaveImage();

	public ControlViewController() {
		window = new JFrame();
		window.setTitle("Maze Demo App - Control View");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		controlView = new ControlView();
		window.setContentPane(controlView);
	}

	public ControlViewController(MazeDemoModel model) {
		this();
		this.model = model;

		controlView.getComboGridResolution().setModel(createGridResolutionModel());
		controlView.getComboGridResolution().setSelectedIndex(getSelectedGridResolutionIndex());
		controlView.getComboGridResolution().setAction(actionChangeGridResolution);

		controlView.getSliderPassageWidth().setValue(model.getPassageWidthPercentage());
		controlView.getSliderPassageWidth().addChangeListener(e -> {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				model.setPassageWidthPercentage(slider.getValue());
			}
		});

		controlView.getSliderDelay().setMinimum(0);
		controlView.getSliderDelay().setMaximum(100);
		controlView.getSliderDelay().setValue(model.getDelay());
		controlView.getSliderDelay().setMinorTickSpacing(10);
		controlView.getSliderDelay().setMajorTickSpacing(50);
		controlView.getSliderDelay().addChangeListener(e -> {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				model.setDelay(slider.getValue());
			}
		});

		controlView.getBtnCreateMaze().setAction(actionCreateSingleMaze);
		controlView.getBtnCreateAllMazes().setAction(actionCreateAllMazes);
		controlView.getBtnFindPath().setAction(actionSolveMaze);
		controlView.getBtnStop().setAction(actionStopBackgroundThread);

		// Menus
		JMenuBar mb = new JMenuBar();
		window.setJMenuBar(mb);
		generatorMenu = new GeneratorMenu(this);
		mb.add(generatorMenu);
		solverMenu = new SolverMenu(this);
		mb.add(solverMenu);
		canvasMenu = new JMenu("Canvas");
		canvasMenu.add(actionClearCanvas);
		canvasMenu.add(actionFloodFill);
		canvasMenu.addSeparator();
		canvasMenu.add(actionCreateEmptyGrid);
		canvasMenu.add(actionCreateFullGrid);
		canvasMenu.addSeparator();
		canvasMenu.add(actionSaveImage);

		mb.add(canvasMenu);
		optionMenu = new OptionMenu(model);
		mb.add(optionMenu);
	}

	public MazeDemoModel getModel() {
		return model;
	}

	public void placeWindow(DisplayMode displayMode) {
		window.setLocation((displayMode.getWidth() - window.getWidth()) / 2, 42);
	}

	public void showWindow() {
		window.setVisible(true);
		window.requestFocusInWindow();
	}

	public void collapseWindow() {
		controlView.getContent().setVisible(false);
		controlView.getBtnShowHideDetails().setAction(actionExpandWindow);
		window.pack();
		window.setSize(window.getWidth(), COLLAPSED_WINDOW_HEIGHT);
	}

	public void expandWindow() {
		controlView.getContent().setVisible(true);
		controlView.getBtnShowHideDetails().setAction(actionCollapseWindow);
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
		controlView.getSliderPassageWidth().setEnabled(enabled);
		controlView.getBtnStop().setEnabled(busy);
	}

	public void showMessage(String msg) {
		controlView.getTextArea().append(msg);
		controlView.getTextArea().setCaretPosition(controlView.getTextArea().getDocument().getLength());
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
		if (solverInfo.isTagged(PathFinderTag.INFORMED)) {
			String metric = model.getMetric().toString();
			metric = metric.substring(0, 1) + metric.substring(1).toLowerCase();
			text += " (" + metric + ")";
		}
		controlView.getLblSolverName().setText(text);
	}

	private void updateGeneratorText(AlgorithmInfo generatorInfo) {
		controlView.getLblGeneratorName().setText(generatorInfo.getDescription());
	}
}
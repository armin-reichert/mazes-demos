package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.DISPLAY_MODE;
import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;

import de.amr.demos.maze.swingapp.action.ChangeGridResolution;
import de.amr.demos.maze.swingapp.action.CreateAllMazes;
import de.amr.demos.maze.swingapp.action.CreateSingleMaze;
import de.amr.demos.maze.swingapp.action.FloodFill;
import de.amr.demos.maze.swingapp.action.SaveImage;
import de.amr.demos.maze.swingapp.action.SolveMaze;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.view.menu.GeneratorMenu;
import de.amr.demos.maze.swingapp.view.menu.OptionMenu;
import de.amr.demos.maze.swingapp.view.menu.SolverMenu;
import de.amr.graph.core.api.TraversalState;

/**
 * Window containing menus and control panel.
 * 
 * @author Armin Reichert
 */
public class ControlWindow extends JFrame {

	private static final ImageIcon ZOOM_IN = new ImageIcon(ControlWindow.class.getResource("/zoom_in.png"));
	private static final ImageIcon ZOOM_OUT = new ImageIcon(ControlWindow.class.getResource("/zoom_out.png"));

	private static final int COLLAPSED_HEIGHT = 160;

	private static ComboBoxModel<String> createGridResolutionModel() {
		String tmpl = "%d cells (%d cols x %d rows, cell size %d)";
		String[] entries = Arrays.stream(model().getGridCellSizes()).mapToObj(cellSize -> {
			int numCols = DISPLAY_MODE.getWidth() / cellSize;
			int numRows = DISPLAY_MODE.getHeight() / cellSize;
			return String.format(tmpl, numCols * numRows, numCols, numRows, cellSize);
		}).toArray(String[]::new);
		return new DefaultComboBoxModel<>(entries);
	}

	private static int getSelectedGridResolutionIndex(MazeDemoModel model) {
		int index = 0;
		for (int size : model.getGridCellSizes()) {
			if (size == model.getGridCellSize()) {
				return index;
			}
			++index;
		}
		return -1;
	}

	public final GeneratorMenu generatorMenu;
	public final JMenu canvasMenu;
	public final SolverMenu solverMenu;
	public final OptionMenu optionMenu;
	public final ControlPanel controlPanel;

	private final Action actionMinimize = new AbstractAction() {

		{
			putValue(Action.NAME, "Hide Details");
			putValue(Action.LARGE_ICON_KEY, ZOOM_OUT);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			minimize();
		}
	};

	private final Action actionMaximize = new AbstractAction() {

		{
			putValue(Action.NAME, "Show Details");
			putValue(Action.LARGE_ICON_KEY, ZOOM_IN);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			maximize();
		}
	};

	private final Action actionCreateEmptyGrid = new AbstractAction("Create Empty Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model().createGrid(false, TraversalState.COMPLETED);
		}
	};

	private final Action actionCreateFullGrid = new AbstractAction("Create Full Grid") {

		@Override
		public void actionPerformed(ActionEvent e) {
			model().createGrid(true, TraversalState.COMPLETED);
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
			canvas().clear();
			canvas().drawGrid();
		}
	};

	private final Action actionCreateSingleMaze = new CreateSingleMaze();
	private final Action actionCreateAllMazes = new CreateAllMazes();
	private final Action actionSolveMaze = new SolveMaze();
	private final Action actionChangeGridResolution = new ChangeGridResolution();
	private final Action actionFloodFill = new FloodFill();
	private final Action actionSaveImage = new SaveImage();

	public ControlWindow() {
		setTitle("Mazes");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Control panel
		controlPanel = new ControlPanel();

		controlPanel.getComboGridResolution().setModel(createGridResolutionModel());
		controlPanel.getComboGridResolution().setSelectedIndex(getSelectedGridResolutionIndex(model()));
		controlPanel.getComboGridResolution().setAction(actionChangeGridResolution);

		controlPanel.getSliderPassageWidth().setValue(model().getPassageWidthPercentage());
		controlPanel.getSliderPassageWidth().addChangeListener(e -> {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				model().setPassageWidthPercentage(slider.getValue());
			}
		});

		controlPanel.getSliderDelay().setMinimum(0);
		controlPanel.getSliderDelay().setMaximum(100);
		controlPanel.getSliderDelay().setValue(model().getDelay());
		controlPanel.getSliderDelay().setMinorTickSpacing(10);
		controlPanel.getSliderDelay().setMajorTickSpacing(50);
		controlPanel.getSliderDelay().addChangeListener(e -> {
			JSlider slider = (JSlider) e.getSource();
			if (!slider.getValueIsAdjusting()) {
				model().setDelay(slider.getValue());
			}
		});

		controlPanel.getBtnCreateMaze().setAction(actionCreateSingleMaze);
		controlPanel.getBtnCreateAllMazes().setAction(actionCreateAllMazes);
		controlPanel.getBtnFindPath().setAction(actionSolveMaze);
		controlPanel.getBtnStop().setAction(actionStopBackgroundThread);

		setContentPane(controlPanel);

		// Menus
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);
		generatorMenu = new GeneratorMenu();
		mb.add(generatorMenu);
		solverMenu = new SolverMenu();
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
		optionMenu = new OptionMenu();
		mb.add(optionMenu);
	}

	public void minimize() {
		controlPanel.getControls().setVisible(false);
		controlPanel.getBtnShowHideDetails().setAction(actionMaximize);
		pack();
		setSize(getWidth(), COLLAPSED_HEIGHT);
	}

	public void maximize() {
		controlPanel.getControls().setVisible(true);
		controlPanel.getBtnShowHideDetails().setAction(actionMinimize);
		pack();
	}

	@Override
	public void setEnabled(boolean enabled) {
		generatorMenu.setEnabled(enabled);
		solverMenu.setEnabled(enabled);
		canvasMenu.setEnabled(enabled);
		optionMenu.setEnabled(enabled);
		actionChangeGridResolution.setEnabled(enabled);
		actionCreateSingleMaze.setEnabled(enabled);
		actionCreateAllMazes.setEnabled(enabled);
		actionSolveMaze.setEnabled(enabled);
		controlPanel.getSliderPassageWidth().setEnabled(enabled);
	}
}
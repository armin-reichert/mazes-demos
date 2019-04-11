package de.amr.demos.maze.swingapp.view;

import static de.amr.demos.maze.swingapp.MazeDemoApp.DISPLAY_MODE;
import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.view.menu.CanvasMenu;
import de.amr.demos.maze.swingapp.view.menu.GeneratorMenu;
import de.amr.demos.maze.swingapp.view.menu.OptionMenu;
import de.amr.demos.maze.swingapp.view.menu.SolverMenu;

/**
 * Window containing menus and control panel.
 * 
 * @author Armin Reichert
 */
public class ControlWindow extends JFrame {

	private static final ImageIcon ZOOM_IN = new ImageIcon(ControlWindow.class.getResource("/zoom_in.png"));
	private static final ImageIcon ZOOM_OUT = new ImageIcon(ControlWindow.class.getResource("/zoom_out.png"));

	private static final int COLLAPSED_HEIGHT = 160;

	public final GeneratorMenu generatorMenu;
	public final CanvasMenu canvasMenu;
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

	public ControlWindow() {
		setTitle("Mazes");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Control panel
		controlPanel = new ControlPanel();

		controlPanel.getComboGridResolution().setModel(createGridResolutionModel());
		controlPanel.getComboGridResolution().setSelectedIndex(getSelectedGridResolutionIndex(model()));
		controlPanel.getComboGridResolution().setAction(app().actionChangeGridResolution);

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

		controlPanel.getBtnCreateMaze().setAction(app().actionCreateMaze);
		controlPanel.getBtnCreateAllMazes().setAction(app().actionCreateAllMazes);
		controlPanel.getBtnFindPath().setAction(app().actionRunMazeSolver);
		controlPanel.getBtnStop().setAction(app().actionCancelTask);

		setContentPane(controlPanel);

		// Menus
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);
		generatorMenu = new GeneratorMenu();
		mb.add(generatorMenu);
		solverMenu = new SolverMenu();
		mb.add(solverMenu);
		canvasMenu = new CanvasMenu();
		mb.add(canvasMenu);
		optionMenu = new OptionMenu();
		mb.add(optionMenu);
	}

	private void minimize() {
		JPanel controls = controlPanel.getControls();
		controls.setVisible(false);
		pack();
		setSize(getWidth(), COLLAPSED_HEIGHT);
		controlPanel.getBtnShowHideDetails().setAction(actionMaximize);
	}

	private void maximize() {
		JPanel controls = controlPanel.getControls();
		controls.setVisible(true);
		validate();
		pack();
		controlPanel.getBtnShowHideDetails().setAction(actionMinimize);
	}

	public void setMinimized(boolean minimized) {
		if (minimized) {
			minimize();
		}
		else {
			maximize();
		}
	}
}
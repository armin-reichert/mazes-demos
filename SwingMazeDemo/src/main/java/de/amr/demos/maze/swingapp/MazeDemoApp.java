package de.amr.demos.maze.swingapp;

import static de.amr.swing.Swing.action;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.common.ThemeConverter;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.swing.Swing;

class Settings {

	@Parameter(description = "Preview window content width", names = { "-width" })
	int width;

	@Parameter(description = "Preview window content height", names = { "-height" })
	int height;

	@Parameter(description = "Theme class name (or: 'system', 'cross', 'metal', 'nimbus')", names = { "-laf",
			"-theme" }, converter = ThemeConverter.class)
	String theme;

	public Settings() {
		Dimension displaySize = Swing.getDisplaySize();
		width = displaySize.width;
		height = displaySize.height;
		theme = NimbusLookAndFeel.class.getName();
	}
}

/**
 * This application visualizes different maze generation algorithms and path
 * finders.
 * <p>
 * The application provides
 * <ul>
 * <li>a preview window (full-screen by default) where the maze generation and
 * path finding animations are shown.
 * <li>a control window where the following settings can be made
 * <ul>
 * <li>the maze generation algorithm
 * <li>the pathfinder algorithm
 * <li>the resolution of the grid
 * <li>the topology of the grid
 * <li>the rendering style and some other settings settings.
 * </ul>
 * </ul>
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	public static void main(String[] commandLineArgs) {
		Settings settings = new Settings();
		JCommander commandLineProcessor = JCommander.newBuilder().addObject(settings).build();
		commandLineProcessor.usage();
		commandLineProcessor.parse(commandLineArgs);
		try {
			UIManager.setLookAndFeel(settings.theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(() -> new MazeDemoApp().start(settings));
	}

	private void start(Settings settings) {
		MazeDemoModel model = new MazeDemoModel();
		model.createGrid(settings.width / model.getGridCellSize(), settings.height / model.getGridCellSize(), false,
				TraversalState.UNVISITED);

		GridUI gridUI = new GridUI(model, settings.width, settings.height);
		ControlUI controlUI = new ControlUI(gridUI, model);

		// configure control UI
		controlUI.setBusy(false);
		controlUI.setHiddenWhenBusy(false);
		controlUI.setAfterGenerationAction(AfterGenerationAction.IDLE);
		controlUI.expandWindow();
		model.findGenerator(RandomBFS.class).ifPresent(controlUI::selectGenerator);
		model.findSolver(AStarSearch.class).ifPresent(controlUI::selectSolver);
		model.changePublisher.addPropertyChangeListener(controlUI);

		// configure preview UI
		gridUI.setEscapeAction(action("Escape", e -> controlUI.show()));
		gridUI.startModelChangeListening();

		// show both windows
		gridUI.show();
		controlUI.placeWindowRelativeTo(gridUI.getWindow());
		controlUI.show();
	}
}
package de.amr.demos.maze.swingapp;

import static de.amr.swing.Swing.action;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.common.ThemeConverter;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.swing.Swing;

/**
 * This application visualizes different maze generation algorithms and path
 * finders.
 * <p>
 * The application provides a (by default full-screen, undecorated) grid display
 * area where the maze generation and path finding animations are shown.
 * <p>
 * A control window allows changing the generation and path finder algorithm,
 * changing the resolution of the grid, the rendering style and other settings.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	@Parameter(description = "Theme class name (or: 'system', 'cross', 'metal', 'nimbus')", names = { "-laf",
			"-theme" }, converter = ThemeConverter.class)
	private String theme = NimbusLookAndFeel.class.getName();

	@Parameter(description = "Grid window width", names = { "-width" })
	private int windowWidth = Swing.getDisplaySize().width;

	@Parameter(description = "Grid window height", names = { "-height" })
	private int windowHeight = Swing.getDisplaySize().height;

	private final MazeDemoModel model = new MazeDemoModel();

	public static void main(String[] args) {
		MazeDemoApp theApp = new MazeDemoApp();
		JCommander argsParser = JCommander.newBuilder().addObject(theApp).build();
		argsParser.usage();
		argsParser.parse(args);
		EventQueue.invokeLater(theApp::createAndShowUI);
	}

	private void createAndShowUI() {
		try {
			UIManager.setLookAndFeel(theme);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		GridUI gridUI = new GridUI(model, windowWidth, windowHeight);

		ControlUI controlUI = new ControlUI(gridUI);
		controlUI.setBusy(false);
		controlUI.setHiddenWhenBusy(false);
		controlUI.setAfterGenerationAction(AfterGenerationAction.SOLVE);
		controlUI.expandWindow();
		controlUI.placeWindowRelativeTo(gridUI.getWindow());
		model.findGenerator(RandomBFS.class).ifPresent(controlUI::selectGenerator);
		model.findSolver(AStarSearch.class).ifPresent(controlUI::selectSolver);

		gridUI.setEscapeAction(action("Escape", e -> controlUI.show()));

		gridUI.show();
		controlUI.show();
	}
}
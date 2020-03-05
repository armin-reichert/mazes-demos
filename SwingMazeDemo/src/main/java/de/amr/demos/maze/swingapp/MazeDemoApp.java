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
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.swing.Swing;

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

	public static void main(String[] args) {
		MazeDemoApp theApp = new MazeDemoApp();
		JCommander argsParser = JCommander.newBuilder().addObject(theApp).build();
		argsParser.usage();
		argsParser.parse(args);
		try {
			UIManager.setLookAndFeel(theApp.theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(theApp::createAndShowUI);
	}

	@Parameter(description = "Grid window width", names = { "-width" })
	private int windowWidth;

	@Parameter(description = "Grid window height", names = { "-height" })
	private int windowHeight;

	@Parameter(description = "Theme class name (or: 'system', 'cross', 'metal', 'nimbus')", names = { "-laf",
			"-theme" }, converter = ThemeConverter.class)
	private String theme;

	private final MazeDemoModel model;

	public MazeDemoApp() {
		Dimension displaySize = Swing.getDisplaySize();
		windowWidth = displaySize.width;
		windowHeight = displaySize.height;
		theme = NimbusLookAndFeel.class.getName();
		model = new MazeDemoModel();
	}

	private void createAndShowUI() {
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
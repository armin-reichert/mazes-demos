package de.amr.demos.maze.swingapp;

import static de.amr.swing.Swing.action;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.common.ThemeConverter;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.maze.alg.Armin;
import de.amr.swing.Swing;

/**
 * This application visualizes different maze generation algorithms and path finders.
 * <p>
 * The application provides an undecorated (by default full-screen) grid display area where the maze
 * generation and path finding animations are show. Using a control window one can change the maze
 * generation path finder algorithm. the size/resolution of the grid, the rendering style and other
 * settings.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	@Parameter(
			description = "Theme class name (or: 'system', 'cross', 'metal', 'nimbus')",
			names = { "-laf", "-theme" },
			converter = ThemeConverter.class)
	private String theme = NimbusLookAndFeel.class.getName();

	@Parameter(description = "Grid window width", names = { "-width" })
	private int windowWidth = Swing.getDisplaySize().width;

	@Parameter(description = "Grid window height", names = { "-height" })
	private int windowHeight = Swing.getDisplaySize().height;

	private final MazeDemoModel model = new MazeDemoModel();
	private ControlUI controlUI;
	private GridUI gridUI;

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
		gridUI = new GridUI(model, new Dimension(windowWidth, windowHeight));
		controlUI = new ControlUI(gridUI);
		controlUI.setBusy(false);
		controlUI.setHiddenWhenBusy(false);
		model.findGenerator(Armin.class).ifPresent(controlUI::selectGenerator);
		model.findSolver(BidiBreadthFirstSearch.class).ifPresent(controlUI::selectSolver);
		controlUI.expandWindow();
		controlUI.placeWindowRelativeTo(gridUI.getWindow());
		gridUI.getView().getCanvas().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "showControlUI");
		gridUI.getView().getCanvas().getActionMap().put("showControlUI", action("", e -> controlUI.show()));
		gridUI.show();
		controlUI.show();
	}
}
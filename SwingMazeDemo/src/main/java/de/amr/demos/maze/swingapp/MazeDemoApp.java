package de.amr.demos.maze.swingapp;

import static de.amr.swing.Swing.action;
import static de.amr.swing.Swing.getDisplaySize;

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

/**
 * This application visualizes different maze generation algorithms and path finders.
 * <p>
 * The application provides an undecorated full-screen preview area where the maze generation and
 * path finding algorithms are displayed as animations. Using a control window one can change the
 * maze generation path finder algorithm. the size/resolution of the grid, the rendering style and
 * other settings.
 * 
 * @author Armin Reichert
 */
public class MazeDemoApp {

	public static void main(String[] args) {
		MazeDemoApp theApp = new MazeDemoApp();
		JCommander.newBuilder().addObject(theApp).build().parse(args);
		EventQueue.invokeLater(() -> theApp.createAndShowUI(getDisplaySize()));
	}

	@Parameter(names = { "-theme" }, description = "Theme", converter = ThemeConverter.class)
	private String theme;

	private MazeDemoModel model;

	private ControlUI controlUI;

	private GridUI gridUI;

	private MazeDemoApp() {
		model = new MazeDemoModel();
		theme = NimbusLookAndFeel.class.getName();
	}

	private void createAndShowUI(Dimension gridWindowSize) {
		try {
			UIManager.setLookAndFeel(theme);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		gridUI = new GridUI(model, gridWindowSize);

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
package de.amr.demos.maze.swingapp;

import static de.amr.swing.MySwingUtils.action;

import java.awt.EventQueue;

import javax.swing.UIManager;

import com.beust.jcommander.JCommander;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGeneration;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.maze.alg.traversal.IterativeDFS;

/**
 * This application visualizes different maze generation algorithms and path finders. It provides
 * <ul>
 * <li>a preview window (full-screen by default) where the maze generation and path finding animations are shown.
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

	public static void main(String[] cmdLineArgs) {
		Settings settings = new Settings();
		JCommander cmdLineProcessor = JCommander.newBuilder().addObject(settings).build();
		cmdLineProcessor.usage();
		cmdLineProcessor.parse(cmdLineArgs);
		try {
			UIManager.setLookAndFeel(settings.theme);
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(() -> new MazeDemoApp().start(settings));
	}

	private void start(Settings settings) {
		MazeDemoModel model = new MazeDemoModel();
		var gridWidth = settings.width / model.getGridCellSize();
		var gridHeight = settings.height / model.getGridCellSize();
		model.createGrid(gridWidth, gridHeight, false, TraversalState.UNVISITED);

		var gridUI = new GridUI(model, settings.width, settings.height);

		var controlUI = new ControlUI(gridUI, model);
		controlUI.setBusy(false);
		controlUI.setHiddenWhenBusy(false);
		controlUI.setAfterGeneration(AfterGeneration.SOLVE);
		controlUI.expandWindow();
		controlUI.collapseWindow();

		model.findGenerator(IterativeDFS.class).ifPresent(controlUI::selectGenerator);
		model.findSolver(BestFirstSearch.class).ifPresent(controlUI::selectSolver);
		model.changes.addPropertyChangeListener(controlUI);

		gridUI.setEscapeAction(action("Escape", e -> controlUI.show()));
		gridUI.startModelChangeListening();

		gridUI.show();
		controlUI.placeWindowRelativeTo(gridUI.getWindow());
		controlUI.show();
	}
}
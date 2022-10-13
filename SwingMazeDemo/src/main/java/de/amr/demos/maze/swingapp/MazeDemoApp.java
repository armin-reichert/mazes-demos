package de.amr.demos.maze.swingapp;

import static de.amr.swing.MySwingUtils.action;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGeneration;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.swing.MySwingUtils;

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

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final Settings settings;
	private final MazeDemoModel model;

	public static void main(String[] args) {
		Dimension defaultSize = MySwingUtils.getDisplaySize();
		Settings settings = new Settings(defaultSize.width, defaultSize.height);
		JCommander cmdLineParser = JCommander.newBuilder().addObject(settings).build();
		cmdLineParser.usage();
		cmdLineParser.parse(args);
		var app = new MazeDemoApp(settings);
		EventQueue.invokeLater(app::createAndShowUI);
	}

	public MazeDemoApp(Settings settings) {
		this.settings = settings;
		model = new MazeDemoModel();
		var gridWidth = settings.width / model.getGridCellSize();
		var gridHeight = settings.height / model.getGridCellSize();
		model.createGrid(gridWidth, gridHeight, false, TraversalState.UNVISITED);
		LOGGER.info("Maze demo app created");
	}

	private void createAndShowUI() {
		try {
			UIManager.setLookAndFeel(settings.theme);
		} catch (Exception e) {
			LOGGER.error("Could not set '%s' Look and Feel".formatted(settings.theme));
			LOGGER.throwing(e);
		}
		var gridUI = new GridUI(model, settings.width, settings.height);
		var controlUI = new ControlUI(gridUI, model);
		controlUI.setBusy(false);
		controlUI.setHiddenWhenBusy(false);
		controlUI.setAfterGeneration(AfterGeneration.SOLVE);
		controlUI.expandWindow();
		controlUI.collapseWindow();

		gridUI.setEscapeAction(action("Escape", e -> controlUI.show()));
		gridUI.startModelChangeListening();

		model.findGenerator(IterativeDFS.class).ifPresent(controlUI::selectGenerator);
		model.findSolver(BestFirstSearch.class).ifPresent(controlUI::selectSolver);
		model.changes.addPropertyChangeListener(controlUI);

		gridUI.show();
		controlUI.placeWindowRelativeTo(gridUI.getWindow());
		controlUI.show();
		LOGGER.info("Maze demo app UI created");
	}
}
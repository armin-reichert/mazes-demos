package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.core.api.TraversalState;

/**
 * Action for creating a maze using the currently selected generation algorithm.
 * 
 * @author Armin Reichert
 */
public class CreateSingleMaze extends CreateMazeAction {

	public CreateSingleMaze(String name, GridViewController gridViewController,
			ControlViewController controlViewController) {
		super(name, gridViewController, controlViewController);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlViewController.getSelectedGenerator().ifPresent(generatorInfo -> {
			theApp.startBackgroundThread(

					() -> {
						boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
						model.createGrid(model.getGrid().numCols(), model.getGrid().numRows(), full,
								full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						gridViewController.clearView();
						createMaze(generatorInfo, model.getGenerationStart());
						if (model.isFloodFillAfterGeneration()) {
							pause(1);
							floodFill();
						}
					},

					interruption -> {
						theApp.showMessage("Animation interrupted");
						theApp.reset();
					},

					failure -> {
						theApp.showMessage("Maze generation failed: " + failure.getClass().getSimpleName());
						theApp.reset();
					});
		});
	}
}
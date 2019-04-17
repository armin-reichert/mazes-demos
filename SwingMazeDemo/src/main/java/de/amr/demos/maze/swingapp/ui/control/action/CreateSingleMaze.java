package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.graph.core.api.TraversalState;

/**
 * Action for creating a maze using the currently selected generation algorithm.
 * 
 * @author Armin Reichert
 */
public class CreateSingleMaze extends CreateMazeAction {

	public CreateSingleMaze(String name) {
		putValue(NAME, name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		theApp.getControlViewController().getSelectedGenerator().ifPresent(generatorInfo -> {
			theApp.startBackgroundThread(

					() -> {
						boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
						theApp.getModel().createGrid(theApp.getModel().getGrid().numCols(),
								theApp.getModel().getGrid().numRows(), full,
								full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						theApp.getGridViewController().clearView();
						createMaze(generatorInfo, theApp.getModel().getGenerationStart());
						if (theApp.getModel().isFloodFillAfterGeneration()) {
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
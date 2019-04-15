package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.graph.core.api.TraversalState;

/**
 * Action for creating a maze using the currently selected generation algorithm.
 * 
 * @author Armin Reichert
 */
public class CreateSingleMaze extends CreateMazeAction {

	public CreateSingleMaze() {
		putValue(NAME, "New Maze");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().getControlViewController().getSelectedGenerator().ifPresent(generatorInfo -> {
			app().startBackgroundThread(

					() -> {
						boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
						app().getModel().createGrid(app().getModel().getGrid().numCols(),
								app().getModel().getGrid().numRows(), full,
								full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						app().getGridViewController().clearView();
						createMaze(generatorInfo, app().getModel().getGenerationStart());
						if (app().getModel().isFloodFillAfterGeneration()) {
							pause(1);
							floodFill();
						}
					},

					interruption -> {
						app().showMessage("Animation interrupted");
						app().reset();
					},

					failure -> {
						app().showMessage("Maze generation failed: " + failure.getClass().getSimpleName());
						app().reset();
					});
		});
	}
}
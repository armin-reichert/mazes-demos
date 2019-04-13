package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag;
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
						boolean full = generatorInfo.isTagged(MazeGenerationAlgorithmTag.FullGridRequired);
						app().getModel().createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						app().getGridViewController().clear();
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
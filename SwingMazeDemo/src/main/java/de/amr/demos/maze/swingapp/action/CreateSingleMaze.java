package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.gridWindow;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

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
		app().currentGenerator().ifPresent(generatorInfo -> {
			app().startBackgroundThread(

					() -> {
						boolean full = generatorInfo.isTagged(MazeGenerationAlgorithmTag.FullGridRequired);
						model().createGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						gridWindow().clear();
						createMaze(generatorInfo, model().getGenerationStart());
						if (model().isFloodFillAfterGeneration()) {
							pause(1);
							floodFill();
						}
					},

					interruption -> {
						app().showMessage("Animation interrupted");
						app().resetDisplay();
					},

					failure -> {
						app().showMessage("Maze generation failed: " + failure.getClass().getSimpleName());
						app().resetDisplay();
					});
		});
	}
}
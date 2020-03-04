package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;

/**
 * Action for creating a maze using the currently selected generation algorithm.
 * 
 * @author Armin Reichert
 */
public class CreateSingleMaze extends CreateMazeAction {

	public CreateSingleMaze(String name, ControlUI controlUI, GridUI gridUI) {
		super(name, controlUI, gridUI);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlUI.getSelectedGenerator().ifPresent(generatorInfo -> {
			controlUI.startBackgroundThread(

					() -> {
						model.emptyGrid();
						gridUI.clear();
						createMaze(generatorInfo, model.getGenerationStart());
						AfterGenerationAction andNow = controlUI.getAfterGenerationAction();
						if (andNow == AfterGenerationAction.FLOOD_FILL) {
							GridCanvasAnimation.pause(1);
							gridUI.floodFill();
						} else if (andNow == AfterGenerationAction.SOLVE) {
							GridCanvasAnimation.pause(1);
							controlUI.solve();
						}
					},

					interruption -> {
						controlUI.showMessage("Animation interrupted");
						controlUI.reset();
					},

					failure -> {
						controlUI.showMessage("Maze generation failed: " + failure.getClass().getSimpleName());
						controlUI.reset();
					});
		});
	}
}
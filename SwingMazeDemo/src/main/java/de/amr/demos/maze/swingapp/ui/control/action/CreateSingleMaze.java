package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.core.api.TraversalState;
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
						boolean full = generatorInfo.isTagged(GeneratorTag.FullGridRequired);
						model.replaceGrid(full, full ? TraversalState.COMPLETED : TraversalState.UNVISITED);
						gridUI.clear();
						createMaze(generatorInfo, model.getGenerationStart());
						switch (controlUI.getAfterGenerationAction()) {
						case FLOOD_FILL:
							GridCanvasAnimation.pause(1);
							gridUI.floodFill();
							break;
						case NOTHING:
							break;
						case SOLVE:
							GridCanvasAnimation.pause(1);
							controlUI.solve();
							break;
						default:
							break;
						}
					},

					interruption -> {
						controlUI.showMessage("Animation interrupted");
						controlUI.resetDisplay();
					},

					failure -> {
						controlUI.showMessage("Maze generation failed: " + failure.getClass().getSimpleName());
						controlUI.resetDisplay();
					});
		});
	}
}
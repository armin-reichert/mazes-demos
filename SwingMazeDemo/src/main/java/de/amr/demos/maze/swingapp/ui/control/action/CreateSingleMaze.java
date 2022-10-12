package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;
import de.amr.util.StopWatch;

/**
 * Action for creating a maze using the currently selected generation algorithm.
 * 
 * @author Armin Reichert
 */
public class CreateSingleMaze extends CreateSingleMazeAction {

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
						AfterGeneration andNow = controlUI.getAfterGeneration();
						if (andNow == AfterGeneration.FLOOD_FILL) {
							GridCanvasAnimation.pause(1);
							StopWatch watch = new StopWatch();
							watch.measure(gridUI::floodFill);
							controlUI.showMessage("Flood-fill: %.3f seconds.", watch.getSeconds());
						} else if (andNow == AfterGeneration.SOLVE) {
							GridCanvasAnimation.pause(1);
							controlUI.runSelectedSolver();
						}
					},

					interruption -> {
						controlUI.showMessage("Animation interrupted");
						controlUI.reset();
					},

					failure -> {
						controlUI.showMessage("Maze generation failed: %s", failure.getClass().getSimpleName());
						controlUI.reset();
					});
		});
	}
}
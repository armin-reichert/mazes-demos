package de.amr.maze.demo.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.maze.demo.ui.control.ControlUI;
import de.amr.maze.demo.ui.grid.GridUI;

/**
 * Animated execution of the selected path finding algorithm ("maze solver") on the current grid.
 * 
 * @author Armin Reichert
 */
public class SolveMazeAction extends MazeDemoAction {

	public SolveMazeAction(String name, ControlUI controlUI, GridUI gridUI) {
		super(name, controlUI, gridUI);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlUI.startBackgroundThread(

				() -> {
					gridUI.drawGrid();
					controlUI.runSelectedSolver();
				},

				interruption -> {
					controlUI.showMessage("Animation interrupted");
					controlUI.reset();
				},

				failure -> {
					failure.printStackTrace(System.err);
					controlUI.showMessage("Solving failed: %s", failure.getMessage());
					controlUI.reset();
				});
	}
}
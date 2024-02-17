package de.amr.maze.demo.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.maze.demo.ui.control.ControlUI;
import de.amr.maze.demo.ui.grid.GridUI;
import de.amr.util.StopWatch;

/**
 * Action for running a "flood-fill" on the current grid/maze.
 * 
 * @author Armin Reichert
 */
public class FloodFillAction extends MazeDemoAction {

	public FloodFillAction(String name, ControlUI controlUI, GridUI gridUI) {
		super(name, controlUI, gridUI);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlUI.startBackgroundThread(
				//
				() -> {
					gridUI.drawGrid();
					StopWatch watch = new StopWatch();
					watch.measure(gridUI::floodFill);
					controlUI.showMessage("Flood-fill: %.3f seconds.", watch.getSeconds());
				},
				//
				interruption -> controlUI.showMessage("Flood-fill interrupted"),
				//
				failure -> failure.printStackTrace(System.err));
	}
}
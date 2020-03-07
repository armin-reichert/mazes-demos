package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;

/**
 * Animated execution of the selected path finding algorithm ("maze solver") on
 * the current grid.
 * 
 * @author Armin Reichert
 */
public class SolveMaze extends AbstractAction {

	private final ControlUI controlUI;
	private final GridUI gridUI;

	public SolveMaze(String name, ControlUI controlUI, GridUI gridUI) {
		super(name);
		this.controlUI = controlUI;
		this.gridUI = gridUI;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlUI.startBackgroundThread(

				() -> {
					gridUI.drawGrid();
					controlUI.solve();
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
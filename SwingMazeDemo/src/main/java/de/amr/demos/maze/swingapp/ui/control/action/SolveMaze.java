package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;

/**
 * Animated execution of the selected path finding algorithm ("maze solver") on the current grid.
 * 
 * @author Armin Reichert
 */
public class SolveMaze extends AbstractAction {

	private final ControlViewController controlUI;
	private final GridViewController gridUI;

	public SolveMaze(String name, ControlViewController controlUI, GridViewController gridUI) {
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
					controlUI.resetDisplay();
				},

				failure -> {
					failure.printStackTrace(System.err);
					controlUI.showMessage("Solving failed: " + failure.getMessage());
					controlUI.resetDisplay();
				});
	}
}
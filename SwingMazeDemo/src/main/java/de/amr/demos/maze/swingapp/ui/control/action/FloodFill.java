package de.amr.demos.maze.swingapp.ui.control.action;

import static java.lang.String.format;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.util.StopWatch;

/**
 * Action for running a "flood-fill" on the current grid/maze.
 * 
 * @author Armin Reichert
 */
public class FloodFill extends AbstractAction {

	private final ControlViewController controlViewController;
	private final GridViewController gridViewController;
	private final MazeDemoModel model;

	public FloodFill(String name, ControlViewController controlViewController,
			GridViewController gridViewController) {
		super(name);
		this.controlViewController = controlViewController;
		this.gridViewController = gridViewController;
		this.model = controlViewController.getModel();
	}

	private void floodFill() {
		gridViewController.floodFill(model.getGrid().cell(model.getSolverSource()), model.isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlViewController.startBackgroundThread(

				() -> {
					gridViewController.drawGrid();
					StopWatch watch = new StopWatch();
					watch.measure(this::floodFill);
					controlViewController.showMessage(format("Flood-fill: %.3f seconds.", watch.getSeconds()));
				},

				interruption -> {
					controlViewController.showMessage("Flood-fill interrupted");
				},

				failure -> {
					failure.printStackTrace(System.err);
				});
	}
}
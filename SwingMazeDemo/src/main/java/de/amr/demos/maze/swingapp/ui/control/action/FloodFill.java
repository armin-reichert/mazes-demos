package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static java.lang.String.format;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.util.StopWatch;

/**
 * Action for running a "flood-fill" on the current grid/maze.
 * 
 * @author Armin Reichert
 */
public class FloodFill extends AbstractAction {

	private final GridViewController controller;

	public FloodFill(String name, GridViewController controller) {
		super(name);
		this.controller = controller;
	}

	private void floodFill() {
		MazeDemoModel model = controller.getModel();
		controller.floodFill(model.getGrid().cell(model.getSolverSource()), model.isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		theApp.startBackgroundThread(

				() -> {
					controller.drawGrid();
					StopWatch watch = new StopWatch();
					watch.measure(this::floodFill);
					theApp.showMessage(format("Flood-fill: %.3f seconds.", watch.getSeconds()));
				},

				interruption -> {
					theApp.showMessage("Flood-fill interrupted");
				},

				failure -> {
					failure.printStackTrace(System.err);
				});
	}
}
package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static java.lang.String.format;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.util.StopWatch;

/**
 * Action for running a "flood-fill" on the current grid/maze.
 * 
 * @author Armin Reichert
 */
public class FloodFill extends AbstractAction {

	public FloodFill(String name) {
		super(name);
	}

	private void floodFill() {
		theApp.getGridViewController().floodFill(
				theApp.getModel().getGrid().cell(theApp.getModel().getPathFinderSource()),
				theApp.getModel().isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		theApp.startBackgroundThread(

				() -> {
					theApp.getGridViewController().drawGrid();
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
package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
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

	public FloodFill() {
		super("Flood-fill");
	}

	private void floodFill() {
		app().getGridViewController().floodFill(
				app().getModel().getGrid().cell(app().getModel().getPathFinderSource()),
				app().getModel().isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().startBackgroundThread(

				() -> {
					app().getGridViewController().drawGrid();
					StopWatch watch = new StopWatch();
					watch.measure(this::floodFill);
					app().showMessage(format("Flood-fill: %.3f seconds.", watch.getSeconds()));
				},

				interruption -> {
					app().showMessage("Flood-fill interrupted");
				},

				failure -> {
					failure.printStackTrace(System.err);
				});
	}
}
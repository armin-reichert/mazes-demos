package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.MazeDemoApp;
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

	private void runFloodFill() {
		MazeDemoApp.app().getGridWindow().floodFill(app().getModel().getGrid().cell(app().getModel().getPathFinderSource()),
				app().getModel().isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().startBackgroundThread(

				() -> {
					app().getGridWindow().drawGrid();
					StopWatch watch = new StopWatch();
					watch.measure(this::runFloodFill);
					app().showMessage(String.format("Flood-fill: %.2f seconds.", watch.getSeconds()));
				},

				interruption -> {
					app().showMessage("Flood-fill interrupted");
				},

				failure -> {
					failure.printStackTrace(System.err);
				});
	}
}
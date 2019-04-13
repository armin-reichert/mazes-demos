package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.gridWindow;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

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
		MazeDemoApp.gridWindow().floodFill(model().getGrid().cell(model().getPathFinderSource()),
				model().isDistancesVisible());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().startBackgroundThread(

				() -> {
					gridWindow().drawGrid();
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
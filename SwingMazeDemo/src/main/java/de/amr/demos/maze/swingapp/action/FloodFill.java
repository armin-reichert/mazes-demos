package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.graph.grid.ui.animation.BFSAnimation;
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
		BFSAnimation.builder().canvas(canvas()).distanceVisible(model().isDistancesVisible()).build()
				.floodFill(model().getPathFinderSource());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().startBackgroundThread(

				() -> {
					canvas().drawGrid();
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
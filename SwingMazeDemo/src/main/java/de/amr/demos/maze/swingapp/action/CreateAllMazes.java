package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static de.amr.demos.maze.swingapp.model.MazeDemoModel.GENERATOR_ALGORITHMS;
import static de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag.Slow;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;

/**
 * Action for running all maze generators (except slow ones) one by one.
 * 
 * @author Armin Reichert
 */
public class CreateAllMazes extends CreateMazeAction {

	public CreateAllMazes() {
		putValue(NAME, "All Mazes");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().startBackgroundThread(

				this::createAllMazes,

				interrupted -> {
					app().showMessage("Animation interrupted");
					app().resetDisplay();
				},

				any -> {
					any.printStackTrace(System.err);
					app().showMessage("Error during maze creation: " + any.getClass().getSimpleName());
					app().resetDisplay();
				});
	}

	private void createAllMazes() {
		List<AlgorithmInfo> generators = Stream.of(GENERATOR_ALGORITHMS).filter(algo -> !algo.isTagged(Slow))
				.collect(Collectors.toList());
		for (AlgorithmInfo algo : generators) {
			app().changeGenerator(algo);
			try {
				createMaze(algo, model().getGenerationStart());
				if (model().isFloodFillAfterGeneration()) {
					pause(1);
					floodFill();
				}
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError | Exception x) {
				app().showMessage("Error during generation: " + x.getClass().getSimpleName());
				app().resetDisplay();
				x.printStackTrace();
			}
			pause(2);
		}
		app().showMessage("Done.");
	}
}
package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.model.MazeGenerationAlgorithmTag.Slow;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

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

				interruption -> {
					app().showMessage("Animation interrupted");
					app().reset();
				},

				failure -> {
					failure.printStackTrace(System.err);
					app().showMessage("Maze creation failed: " + failure.getClass().getSimpleName());
					app().reset();
				});
	}

	private void createAllMazes() {
		List<AlgorithmInfo> fastGenerators = app().getModel().generators().filter(alg -> !alg.isTagged(Slow))
				.collect(Collectors.toList());
		for (AlgorithmInfo generatorInfo : fastGenerators) {
			app().changeGenerator(generatorInfo);
			try {
				createMaze(generatorInfo, app().getModel().getGenerationStart());
				if (app().getModel().isFloodFillAfterGeneration()) {
					pause(1);
					floodFill();
				}
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError | Exception x) {
				throw new RuntimeException(x);
			}
			pause(2);
		}
		app().showMessage("Done.");
	}
}
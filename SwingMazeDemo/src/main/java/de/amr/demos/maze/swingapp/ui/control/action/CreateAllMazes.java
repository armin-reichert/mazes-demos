package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Slow;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;

/**
 * Action for running all maze generators (except slow ones) one by one.
 * 
 * @author Armin Reichert
 */
public class CreateAllMazes extends CreateMazeAction {

	public CreateAllMazes(String name, GridViewController gridViewController,
			ControlViewController controlViewController) {
		super(name, gridViewController, controlViewController);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		theApp.startBackgroundThread(

				this::createAllMazes,

				interruption -> {
					theApp.showMessage("Animation interrupted");
					theApp.reset();
				},

				failure -> {
					failure.printStackTrace(System.err);
					theApp.showMessage("Maze creation failed: " + failure.getClass().getSimpleName());
					theApp.reset();
				});
	}

	private void createAllMazes() {
		List<AlgorithmInfo> fastGenerators = model.generators().filter(alg -> !alg.isTagged(Slow))
				.collect(Collectors.toList());
		for (AlgorithmInfo generatorInfo : fastGenerators) {
			theApp.changeGenerator(generatorInfo);
			try {
				createMaze(generatorInfo, model.getGenerationStart());
				if (model.isFloodFillAfterGeneration()) {
					pause(1);
					floodFill();
				}
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError x) {
				theApp.showMessage("Maze creation failed because of stack overflow (recursion too deep)");
				theApp.reset();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
			pause(2);
		}
		theApp.showMessage("Done.");
	}
}
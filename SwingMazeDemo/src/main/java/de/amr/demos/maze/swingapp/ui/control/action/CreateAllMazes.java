package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;

/**
 * Action for running all maze generators (except slow ones) one by one.
 * 
 * @author Armin Reichert
 */
public class CreateAllMazes extends CreateMazeAction {

	public CreateAllMazes(String name, ControlViewController controlViewController,
			GridViewController gridViewController) {
		super(name, controlViewController, gridViewController);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlViewController.startBackgroundThread(

				this::createAllMazes,

				interruption -> {
					controlViewController.showMessage("Animation interrupted");
					controlViewController.resetDisplay();
				},

				failure -> {
					failure.printStackTrace(System.err);
					controlViewController.showMessage("Maze creation failed: " + failure.getClass().getSimpleName());
					controlViewController.resetDisplay();
				});
	}

	private void createAllMazes() {
		Algorithm[] fastOnes = model.generators().filter(alg -> !alg.isTagged(GeneratorTag.Slow))
				.toArray(Algorithm[]::new);
		for (Algorithm generator : fastOnes) {
			controlViewController.selectGenerator(generator);
			try {
				createMaze(generator, model.getGenerationStart());
				if (model.isFloodFillAfterGeneration()) {
					pause(1);
					floodFill();
				}
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError x) {
				controlViewController
						.showMessage("Maze creation failed because of stack overflow (recursion too deep)");
				controlViewController.resetDisplay();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
			pause(2);
		}
		controlViewController.showMessage("Done.");
	}
}
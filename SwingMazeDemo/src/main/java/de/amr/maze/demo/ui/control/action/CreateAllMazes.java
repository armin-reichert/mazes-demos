package de.amr.maze.demo.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.maze.demo.model.Algorithm;
import de.amr.maze.demo.model.GeneratorTag;
import de.amr.maze.demo.ui.control.ControlUI;
import de.amr.maze.demo.ui.grid.GridUI;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.GridCanvasAnimation;

/**
 * Action for running all maze generators (except slow ones) one by one.
 * 
 * @author Armin Reichert
 */
public class CreateAllMazes extends CreateMazeAction {

	public CreateAllMazes(String name, ControlUI controlUI, GridUI gridUI) {
		super(name, controlUI, gridUI);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlUI.startBackgroundThread(

				this::createAllMazes,

				interruption -> {
					controlUI.showMessage("Animation interrupted");
					controlUI.reset();
				},

				failure -> {
					failure.printStackTrace(System.err);
					controlUI.showMessage("Maze creation failed: %s", failure.getClass().getSimpleName());
					controlUI.reset();
				});
	}

	private void createAllMazes() {
		Algorithm[] fastGenerators = model.generators().filter(alg -> !alg.isTagged(GeneratorTag.SLOW))
				.toArray(Algorithm[]::new);
		for (Algorithm generator : fastGenerators) {
			controlUI.selectGenerator(generator);
			try {
				createMaze(generator, model.getGenerationStart());
				afterGenerationAction();
				GridCanvasAnimation.pause(2);
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError x) {
				controlUI.showMessage("Maze creation failed: stack overflow (recursion too deep)");
				controlUI.reset();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
		controlUI.showMessage("Done.");
	}

	private void afterGenerationAction() {
		switch (controlUI.getAfterGeneration()) {
		case FLOOD_FILL -> {
			GridCanvasAnimation.pause(1);
			gridUI.floodFill();
		}
		case SOLVE -> {
			GridCanvasAnimation.pause(1);
			controlUI.runSelectedSolver();
		}
		default -> {
			// nothing to do
		}
		}
	}
}
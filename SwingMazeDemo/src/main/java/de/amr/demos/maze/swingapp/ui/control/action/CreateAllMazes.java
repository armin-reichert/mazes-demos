package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.GeneratorTag;
import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;

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
					controlUI.resetDisplay();
				},

				failure -> {
					failure.printStackTrace(System.err);
					controlUI.showMessage("Maze creation failed: " + failure.getClass().getSimpleName());
					controlUI.resetDisplay();
				});
	}

	private void createAllMazes() {
		Algorithm[] fastOnes = model.generators().filter(alg -> !alg.isTagged(GeneratorTag.Slow))
				.toArray(Algorithm[]::new);
		for (Algorithm generator : fastOnes) {
			controlUI.selectGenerator(generator);
			try {
				createMaze(generator, model.getGenerationStart());
				switch (controlUI.getAfterGenerationAction()) {
				case FLOOD_FILL:
					pause(1);
					gridUI.floodFill();
					break;
				case NOTHING:
					break;
				case SOLVE:
					pause(1);
					controlUI.solve();
					break;
				default:
					break;
				}
			} catch (AnimationInterruptedException x) {
				throw x;
			} catch (StackOverflowError x) {
				controlUI.showMessage("Maze creation failed because of stack overflow (recursion too deep)");
				controlUI.resetDisplay();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
			pause(2);
		}
		controlUI.showMessage("Done.");
	}
}
package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;
import static java.lang.String.format;

import java.awt.event.ActionEvent;
import java.util.function.ToDoubleBiFunction;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.PathFinderTag;
import de.amr.graph.grid.ui.animation.AnimationInterruptedException;
import de.amr.graph.grid.ui.animation.BFSAnimation;
import de.amr.graph.grid.ui.animation.DFSAnimation;
import de.amr.graph.pathfinder.api.ObservableGraphSearch;
import de.amr.graph.pathfinder.impl.AStarSearch;
import de.amr.graph.pathfinder.impl.BestFirstSearch;
import de.amr.graph.pathfinder.impl.BidiAStarSearch;
import de.amr.graph.pathfinder.impl.BidiBreadthFirstSearch;
import de.amr.graph.pathfinder.impl.BidiDijkstraSearch;
import de.amr.graph.pathfinder.impl.BreadthFirstSearch;
import de.amr.graph.pathfinder.impl.DepthFirstSearch;
import de.amr.graph.pathfinder.impl.DepthFirstSearch2;
import de.amr.graph.pathfinder.impl.DijkstraSearch;
import de.amr.graph.pathfinder.impl.HillClimbingSearch;
import de.amr.graph.pathfinder.impl.IDDFS;
import de.amr.util.StopWatch;

/**
 * Animated execution of the selected path finding algorithm ("maze solver") on the current grid.
 * 
 * @author Armin Reichert
 */
public class SolveMazeAction extends AbstractAction {

	public SolveMazeAction() {
		putValue(NAME, "Solve");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().currentSolver().ifPresent(solver -> {
			app().enableUI(false);
			canvas().drawGrid(); // to possibly overwrite older search
			app().startWorkerThread(() -> {
				try {
					runSolverAnimation(solver);
				} catch (AnimationInterruptedException x) {
					app().showMessage("Animation interrupted");
					app().resetDisplay();
				} catch (Exception x) {
					x.printStackTrace();
					app().showMessage("Error during path finding: " + x.getMessage());
				} finally {
					app().enableUI(true);
				}
			});
		});
	}

	private void runSolverAnimation(AlgorithmInfo info) {

		if (info.getAlgorithmClass() == BreadthFirstSearch.class) {
			runSolver(new BreadthFirstSearch(model().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == BidiBreadthFirstSearch.class) {
			runSolver(new BidiBreadthFirstSearch(model().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == DijkstraSearch.class) {
			runSolver(new DijkstraSearch(model().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BidiDijkstraSearch.class) {
			runSolver(new BidiDijkstraSearch(model().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BestFirstSearch.class) {
			runSolver(new BestFirstSearch(model().getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
		else if (info.getAlgorithmClass() == AStarSearch.class) {
			runSolver(new AStarSearch(model().getGrid(), (u, v) -> 1, metric()), info);
		}
		else if (info.getAlgorithmClass() == BidiAStarSearch.class) {
			runSolver(new BidiAStarSearch(model().getGrid(), (u, v) -> 1, metric(), metric()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch.class) {
			runSolver(new DepthFirstSearch(model().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch2.class) {
			runSolver(new DepthFirstSearch2(model().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == IDDFS.class) {
			runSolver(new IDDFS(model().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == HillClimbingSearch.class) {
			runSolver(new HillClimbingSearch(model().getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
	}

	private void runSolver(ObservableGraphSearch solver, AlgorithmInfo solverInfo) {
		int source = model().getGrid().cell(model().getPathFinderSource());
		int target = model().getGrid().cell(model().getPathFinderTarget());
		boolean informed = solverInfo.isTagged(PathFinderTag.INFORMED);
		StopWatch watch = new StopWatch();
		if (solverInfo.isTagged(PathFinderTag.BFS)) {
			BFSAnimation anim = BFSAnimation.builder().canvas(canvas()).delay(() -> model().getDelay())
					.pathColor(canvas().getPathColor()).distanceVisible(model().isDistancesVisible()).build();
			watch.measure(() -> anim.run(solver, source, target));
			anim.showPath(solver, source, target);
		}
		else if (solverInfo.isTagged(PathFinderTag.DFS)) {
			DFSAnimation anim = DFSAnimation.builder().canvas(canvas()).delay(() -> model().getDelay())
					.pathColor(canvas().getPathColor()).build();
			watch.measure(() -> anim.run(solver, source, target));
		}
		app().showMessage(informed
				? format("%s (%s): %.2f seconds.", solverInfo.getDescription(), model().getMetric(),
						watch.getSeconds())
				: format("%s: %.2f seconds.", solverInfo.getDescription(), watch.getSeconds()));
	}

	private int target() {
		return model().getGrid().cell(model().getPathFinderTarget());
	}

	private ToDoubleBiFunction<Integer, Integer> metric() {
		switch (model().getMetric()) {
		case CHEBYSHEV:
			return model().getGrid()::chebyshev;
		case EUCLIDEAN:
			return model().getGrid()::euclidean;
		case MANHATTAN:
			return model().getGrid()::manhattan;
		}
		throw new IllegalStateException();
	}
}
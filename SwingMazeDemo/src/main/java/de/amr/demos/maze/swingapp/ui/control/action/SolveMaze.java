package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.theApp;
import static java.lang.String.format;

import java.awt.event.ActionEvent;
import java.util.function.ToDoubleBiFunction;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.grid.GridView;
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
public class SolveMaze extends AbstractAction {

	public SolveMaze(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		theApp.getControlViewController().getSelectedSolver().ifPresent(solver -> {
			theApp.startBackgroundThread(

					() -> {
						theApp.getGridViewController().drawGrid(); // overwrite older search
						solve(solver);
					},

					interruption -> {
						theApp.showMessage("Animation interrupted");
						theApp.reset();
					},

					failure -> {
						failure.printStackTrace(System.err);
						theApp.showMessage("Solving failed: " + failure.getMessage());
						theApp.reset();
					});
		});
	}

	private void solve(Algorithm info) {

		if (info.getAlgorithmClass() == BreadthFirstSearch.class) {
			solve(new BreadthFirstSearch(theApp.getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == BidiBreadthFirstSearch.class) {
			solve(new BidiBreadthFirstSearch(theApp.getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == DijkstraSearch.class) {
			solve(new DijkstraSearch(theApp.getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BidiDijkstraSearch.class) {
			solve(new BidiDijkstraSearch(theApp.getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BestFirstSearch.class) {
			solve(new BestFirstSearch(theApp.getModel().getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
		else if (info.getAlgorithmClass() == AStarSearch.class) {
			solve(new AStarSearch(theApp.getModel().getGrid(), (u, v) -> 1, metric()), info);
		}
		else if (info.getAlgorithmClass() == BidiAStarSearch.class) {
			solve(new BidiAStarSearch(theApp.getModel().getGrid(), (u, v) -> 1, metric(), metric()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch.class) {
			solve(new DepthFirstSearch(theApp.getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch2.class) {
			solve(new DepthFirstSearch2(theApp.getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == IDDFS.class) {
			solve(new IDDFS(theApp.getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == HillClimbingSearch.class) {
			solve(new HillClimbingSearch(theApp.getModel().getGrid(), v -> metric().applyAsDouble(v, target())),
					info);
		}
	}

	private void solve(ObservableGraphSearch solver, Algorithm solverInfo) {
		GridView gridView = theApp.getGridViewController().getView();
		int source = theApp.getModel().getGrid().cell(theApp.getModel().getSolverSource());
		int target = theApp.getModel().getGrid().cell(theApp.getModel().getSolverTarget());
		boolean informed = solverInfo.isTagged(SolverTag.INFORMED);
		StopWatch watch = new StopWatch();
		if (solverInfo.isTagged(SolverTag.BFS)) {
			BFSAnimation anim = BFSAnimation.builder().canvas(gridView.getCanvas())
					.delay(() -> theApp.getModel().getDelay()).pathColor(gridView.getPathColor())
					.distanceVisible(theApp.getModel().isDistancesVisible()).build();
			watch.measure(() -> anim.run(solver, source, target));
			anim.showPath(solver, source, target);
		}
		else if (solverInfo.isTagged(SolverTag.DFS)) {
			DFSAnimation anim = DFSAnimation.builder().canvas(gridView.getCanvas())
					.delay(() -> theApp.getModel().getDelay()).pathColor(gridView.getPathColor()).build();
			watch.measure(() -> anim.run(solver, source, target));
		}
		theApp.showMessage(informed
				? format("%s (%s): %.2f seconds.", solverInfo.getDescription(), theApp.getModel().getMetric(),
						watch.getSeconds())
				: format("%s: %.2f seconds.", solverInfo.getDescription(), watch.getSeconds()));
	}

	private int target() {
		return theApp.getModel().getGrid().cell(theApp.getModel().getSolverTarget());
	}

	private ToDoubleBiFunction<Integer, Integer> metric() {
		switch (theApp.getModel().getMetric()) {
		case CHEBYSHEV:
			return theApp.getModel().getGrid()::chebyshev;
		case EUCLIDEAN:
			return theApp.getModel().getGrid()::euclidean;
		case MANHATTAN:
			return theApp.getModel().getGrid()::manhattan;
		default:
			throw new IllegalStateException();
		}
	}
}
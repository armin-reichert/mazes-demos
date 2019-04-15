package de.amr.demos.maze.swingapp.ui.control.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static java.lang.String.format;

import java.awt.event.ActionEvent;
import java.util.function.ToDoubleBiFunction;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
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

	public SolveMaze() {
		super("Solve");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().getControlViewController().getSelectedSolver().ifPresent(solver -> {
			app().startBackgroundThread(

					() -> {
						app().getGridViewController().drawGrid(); // overwrite older search
						solve(solver);
					},

					interruption -> {
						app().showMessage("Animation interrupted");
						app().reset();
					},

					failure -> {
						failure.printStackTrace(System.err);
						app().showMessage("Solving failed: " + failure.getMessage());
						app().reset();
					});
		});
	}

	private void solve(AlgorithmInfo info) {

		if (info.getAlgorithmClass() == BreadthFirstSearch.class) {
			solve(new BreadthFirstSearch(app().getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == BidiBreadthFirstSearch.class) {
			solve(new BidiBreadthFirstSearch(app().getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == DijkstraSearch.class) {
			solve(new DijkstraSearch(app().getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BidiDijkstraSearch.class) {
			solve(new BidiDijkstraSearch(app().getModel().getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BestFirstSearch.class) {
			solve(new BestFirstSearch(app().getModel().getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
		else if (info.getAlgorithmClass() == AStarSearch.class) {
			solve(new AStarSearch(app().getModel().getGrid(), (u, v) -> 1, metric()), info);
		}
		else if (info.getAlgorithmClass() == BidiAStarSearch.class) {
			solve(new BidiAStarSearch(app().getModel().getGrid(), (u, v) -> 1, metric(), metric()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch.class) {
			solve(new DepthFirstSearch(app().getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch2.class) {
			solve(new DepthFirstSearch2(app().getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == IDDFS.class) {
			solve(new IDDFS(app().getModel().getGrid()), info);
		}
		else if (info.getAlgorithmClass() == HillClimbingSearch.class) {
			solve(new HillClimbingSearch(app().getModel().getGrid(), v -> metric().applyAsDouble(v, target())),
					info);
		}
	}

	private void solve(ObservableGraphSearch solver, AlgorithmInfo solverInfo) {
		GridView gridView = app().getGridViewController().getView();
		int source = app().getModel().getGrid().cell(app().getModel().getPathFinderSource());
		int target = app().getModel().getGrid().cell(app().getModel().getPathFinderTarget());
		boolean informed = solverInfo.isTagged(SolverTag.INFORMED);
		StopWatch watch = new StopWatch();
		if (solverInfo.isTagged(SolverTag.BFS)) {
			BFSAnimation anim = BFSAnimation.builder().canvas(gridView).delay(() -> app().getModel().getDelay())
					.pathColor(gridView.getPathColor()).distanceVisible(app().getModel().isDistancesVisible()).build();
			watch.measure(() -> anim.run(solver, source, target));
			anim.showPath(solver, source, target);
		}
		else if (solverInfo.isTagged(SolverTag.DFS)) {
			DFSAnimation anim = DFSAnimation.builder().canvas(gridView).delay(() -> app().getModel().getDelay())
					.pathColor(gridView.getPathColor()).build();
			watch.measure(() -> anim.run(solver, source, target));
		}
		app().showMessage(informed
				? format("%s (%s): %.2f seconds.", solverInfo.getDescription(), app().getModel().getMetric(),
						watch.getSeconds())
				: format("%s: %.2f seconds.", solverInfo.getDescription(), watch.getSeconds()));
	}

	private int target() {
		return app().getModel().getGrid().cell(app().getModel().getPathFinderTarget());
	}

	private ToDoubleBiFunction<Integer, Integer> metric() {
		switch (app().getModel().getMetric()) {
		case CHEBYSHEV:
			return app().getModel().getGrid()::chebyshev;
		case EUCLIDEAN:
			return app().getModel().getGrid()::euclidean;
		case MANHATTAN:
			return app().getModel().getGrid()::manhattan;
		}
		throw new IllegalStateException();
	}
}
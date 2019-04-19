package de.amr.demos.maze.swingapp.ui.control.action;

import static java.lang.String.format;

import java.awt.event.ActionEvent;
import java.util.function.ToDoubleBiFunction;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridView;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;
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

	private final ControlViewController controlViewController;
	private final GridViewController gridViewController;
	private final MazeDemoModel model;

	public SolveMaze(String name, ControlViewController controlViewController,
			GridViewController gridViewController) {
		super(name);
		this.controlViewController = controlViewController;
		this.gridViewController = gridViewController;
		this.model = controlViewController.getModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controlViewController.getSelectedSolver().ifPresent(solver -> {
			controlViewController.startBackgroundThread(

					() -> {
						gridViewController.drawGrid(); // overwrite older search
						solve(solver);
					},

					interruption -> {
						controlViewController.showMessage("Animation interrupted");
						controlViewController.resetDisplay();
					},

					failure -> {
						failure.printStackTrace(System.err);
						controlViewController.showMessage("Solving failed: " + failure.getMessage());
						controlViewController.resetDisplay();
					});
		});
	}

	private void solve(Algorithm info) {

		if (info.getAlgorithmClass() == BreadthFirstSearch.class) {
			solve(new BreadthFirstSearch(model.getGrid()), info);
		}
		else if (info.getAlgorithmClass() == BidiBreadthFirstSearch.class) {
			solve(new BidiBreadthFirstSearch(model.getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == DijkstraSearch.class) {
			solve(new DijkstraSearch(model.getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BidiDijkstraSearch.class) {
			solve(new BidiDijkstraSearch(model.getGrid(), (u, v) -> 1), info);
		}
		else if (info.getAlgorithmClass() == BestFirstSearch.class) {
			solve(new BestFirstSearch(model.getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
		else if (info.getAlgorithmClass() == AStarSearch.class) {
			solve(new AStarSearch(model.getGrid(), (u, v) -> 1, metric()), info);
		}
		else if (info.getAlgorithmClass() == BidiAStarSearch.class) {
			solve(new BidiAStarSearch(model.getGrid(), (u, v) -> 1, metric(), metric()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch.class) {
			solve(new DepthFirstSearch(model.getGrid()), info);
		}
		else if (info.getAlgorithmClass() == DepthFirstSearch2.class) {
			solve(new DepthFirstSearch2(model.getGrid()), info);
		}
		else if (info.getAlgorithmClass() == IDDFS.class) {
			solve(new IDDFS(model.getGrid()), info);
		}
		else if (info.getAlgorithmClass() == HillClimbingSearch.class) {
			solve(new HillClimbingSearch(model.getGrid(), v -> metric().applyAsDouble(v, target())), info);
		}
	}

	private void solve(ObservableGraphSearch solver, Algorithm solverInfo) {
		GridView gridView = gridViewController.getView();
		int source = model.getGrid().cell(model.getSolverSource());
		int target = model.getGrid().cell(model.getSolverTarget());
		boolean informed = solverInfo.isTagged(SolverTag.INFORMED);
		StopWatch watch = new StopWatch();
		if (solverInfo.isTagged(SolverTag.BFS)) {
			BFSAnimation anim = BFSAnimation.builder().canvas(gridView.getCanvas()).delay(() -> model.getDelay())
					.pathColor(gridView.getPathColor()).distanceVisible(model.isDistancesVisible()).build();
			watch.measure(() -> anim.run(solver, source, target));
			anim.showPath(solver, source, target);
		}
		else if (solverInfo.isTagged(SolverTag.DFS)) {
			DFSAnimation anim = DFSAnimation.builder().canvas(gridView.getCanvas()).delay(() -> model.getDelay())
					.pathColor(gridView.getPathColor()).build();
			watch.measure(() -> anim.run(solver, source, target));
		}
		controlViewController.showMessage(informed
				? format("%s (%s): %.2f seconds.", solverInfo.getDescription(), model.getMetric(), watch.getSeconds())
				: format("%s: %.2f seconds.", solverInfo.getDescription(), watch.getSeconds()));
	}

	private int target() {
		return model.getGrid().cell(model.getSolverTarget());
	}

	private ToDoubleBiFunction<Integer, Integer> metric() {
		switch (model.getMetric()) {
		case CHEBYSHEV:
			return model.getGrid()::chebyshev;
		case EUCLIDEAN:
			return model.getGrid()::euclidean;
		case MANHATTAN:
			return model.getGrid()::manhattan;
		default:
			throw new IllegalStateException();
		}
	}
}
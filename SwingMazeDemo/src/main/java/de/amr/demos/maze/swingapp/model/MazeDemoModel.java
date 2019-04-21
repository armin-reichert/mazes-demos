package de.amr.demos.maze.swingapp.model;

import static de.amr.demos.maze.swingapp.model.GeneratorTag.FullGridRequired;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.MST;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Slow;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.SmallGrid;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Traversal;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.UST;
import static de.amr.demos.maze.swingapp.model.SolverTag.BFS;
import static de.amr.demos.maze.swingapp.model.SolverTag.DFS;
import static de.amr.demos.maze.swingapp.model.SolverTag.INFORMED;
import static de.amr.graph.grid.api.GridPosition.BOTTOM_RIGHT;
import static de.amr.graph.grid.api.GridPosition.CENTER;
import static de.amr.graph.grid.api.GridPosition.TOP_LEFT;
import static de.amr.graph.grid.impl.GridFactory.emptyObservableGrid;
import static de.amr.graph.grid.impl.GridFactory.fullObservableGrid;

import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.api.GridPosition;
import de.amr.graph.grid.impl.ObservableGridGraph;
import de.amr.graph.grid.impl.Top4;
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
import de.amr.maze.alg.Armin;
import de.amr.maze.alg.BinaryTree;
import de.amr.maze.alg.BinaryTreeRandom;
import de.amr.maze.alg.Eller;
import de.amr.maze.alg.HuntAndKill;
import de.amr.maze.alg.HuntAndKillRandom;
import de.amr.maze.alg.RecursiveDivision;
import de.amr.maze.alg.Sidewinder;
import de.amr.maze.alg.mst.BoruvkaMST;
import de.amr.maze.alg.mst.KruskalMST;
import de.amr.maze.alg.mst.PrimMST;
import de.amr.maze.alg.mst.ReverseDeleteMST_BFS;
import de.amr.maze.alg.mst.ReverseDeleteMST_BestFS;
import de.amr.maze.alg.mst.ReverseDeleteMST_BidiAStar;
import de.amr.maze.alg.mst.ReverseDeleteMST_DFS;
import de.amr.maze.alg.mst.ReverseDeleteMST_HillClimbing;
import de.amr.maze.alg.traversal.GrowingTreeAlwaysFirst;
import de.amr.maze.alg.traversal.GrowingTreeAlwaysLast;
import de.amr.maze.alg.traversal.GrowingTreeAlwaysRandom;
import de.amr.maze.alg.traversal.GrowingTreeLastOrRandom;
import de.amr.maze.alg.traversal.IterativeDFS;
import de.amr.maze.alg.traversal.RandomBFS;
import de.amr.maze.alg.traversal.RecursiveDFS;
import de.amr.maze.alg.ust.AldousBroderUST;
import de.amr.maze.alg.ust.AldousBroderWilsonUST;
import de.amr.maze.alg.ust.WilsonUSTCollapsingCircle;
import de.amr.maze.alg.ust.WilsonUSTCollapsingRectangle;
import de.amr.maze.alg.ust.WilsonUSTCollapsingWalls;
import de.amr.maze.alg.ust.WilsonUSTExpandingCircle;
import de.amr.maze.alg.ust.WilsonUSTExpandingCircles;
import de.amr.maze.alg.ust.WilsonUSTExpandingRectangle;
import de.amr.maze.alg.ust.WilsonUSTExpandingSpiral;
import de.amr.maze.alg.ust.WilsonUSTHilbertCurve;
import de.amr.maze.alg.ust.WilsonUSTLeftToRightSweep;
import de.amr.maze.alg.ust.WilsonUSTMooreCurve;
import de.amr.maze.alg.ust.WilsonUSTNestedRectangles;
import de.amr.maze.alg.ust.WilsonUSTPeanoCurve;
import de.amr.maze.alg.ust.WilsonUSTRandomCell;
import de.amr.maze.alg.ust.WilsonUSTRecursiveCrosses;
import de.amr.maze.alg.ust.WilsonUSTRightToLeftSweep;
import de.amr.maze.alg.ust.WilsonUSTRowsTopDown;

/**
 * Data model of the maze demo application.
 * 
 * @author Armin Reichert
 */
public class MazeDemoModel {

	public enum Style {
		WALL_PASSAGES, PEARLS
	};

	public enum Metric {
		EUCLIDEAN, MANHATTAN, CHEBYSHEV
	}

	private static final Algorithm[] GENERATOR_ALGORITHMS = {
		/*@formatter:off*/
		new Algorithm(RecursiveDFS.class, "Random recursive DFS (small grids only!)", Traversal, SmallGrid),
		new Algorithm(IterativeDFS.class, "Random non-recursive DFS", Traversal),
		new Algorithm(RandomBFS.class, "Random BFS", Traversal),
		new Algorithm(GrowingTreeAlwaysFirst.class, "Growing Tree (always select first)", Traversal),
		new Algorithm(GrowingTreeAlwaysLast.class, "Growing Tree (always select last)", Traversal),
		new Algorithm(GrowingTreeAlwaysRandom.class, "Growing Tree (always select random)", Traversal),
		new Algorithm(GrowingTreeLastOrRandom.class, "Growing Tree (last or random)", Traversal),
		new Algorithm(KruskalMST.class, "Kruskal MST", MST),
		new Algorithm(PrimMST.class, "Prim MST", MST),
		new Algorithm(BoruvkaMST.class, "Boruvka MST", MST),
		new Algorithm(ReverseDeleteMST_BFS.class, "Reverse-Delete MST (BFS, very slow!)", MST, Slow, FullGridRequired),
		new Algorithm(ReverseDeleteMST_BestFS.class, "Reverse-Delete MST (Best-First Search, very slow!)", MST, Slow, FullGridRequired),
		new Algorithm(ReverseDeleteMST_DFS.class, "Reverse-Delete MST (DFS, very slow!)", MST, Slow, FullGridRequired),
		new Algorithm(ReverseDeleteMST_HillClimbing.class, "Reverse-Delete MST (Hill-Climbing, very slow!)", MST, Slow, FullGridRequired),
		new Algorithm(ReverseDeleteMST_BidiAStar.class, "Reverse-Delete MST (Bidi A*, very slow!)", MST, Slow, FullGridRequired),
		new Algorithm(AldousBroderUST.class, "Aldous-Broder UST (rather slow)", UST, Slow),
		new Algorithm(AldousBroderWilsonUST.class, "Houston UST (rather slow)", UST, Slow),
		new Algorithm(WilsonUSTRandomCell.class, "Wilson UST (random)", UST, Slow),
		new Algorithm(WilsonUSTRowsTopDown.class, "Wilson UST (row-wise, top-to-bottom)", UST),
		new Algorithm(WilsonUSTLeftToRightSweep.class, "Wilson UST (column-wise, left to right)", UST),
		new Algorithm(WilsonUSTRightToLeftSweep.class, "Wilson UST (column-wise, right to left)", UST),
		new Algorithm(WilsonUSTCollapsingWalls.class, "Wilson UST (column-wise, collapsing)", UST),
		new Algorithm(WilsonUSTCollapsingRectangle.class, "Wilson UST (collapsing rectangle)", UST),
		new Algorithm(WilsonUSTExpandingCircle.class, "Wilson UST (expanding circle)", UST),
		new Algorithm(WilsonUSTCollapsingCircle.class, "Wilson UST (collapsing circle)", UST),
		new Algorithm(WilsonUSTExpandingCircles.class, "Wilson UST (expanding circles)", UST),
		new Algorithm(WilsonUSTExpandingSpiral.class, "Wilson UST (expanding spiral)", UST),
		new Algorithm(WilsonUSTExpandingRectangle.class, "Wilson UST (expanding rectangle)", UST),
		new Algorithm(WilsonUSTNestedRectangles.class, "Wilson UST (nested rectangles)", UST),
		new Algorithm(WilsonUSTRecursiveCrosses.class, "Wilson UST (recursive crosses)", UST),
		new Algorithm(WilsonUSTHilbertCurve.class, "Wilson UST (Hilbert curve)", UST),
		new Algorithm(WilsonUSTMooreCurve.class, "Wilson UST (Moore curve)", UST),
		new Algorithm(WilsonUSTPeanoCurve.class, "Wilson UST (Peano curve)", UST),
		new Algorithm(BinaryTree.class, "Binary Tree (row-wise, top-to-bottom)"),
		new Algorithm(BinaryTreeRandom.class, "Binary Tree (random)"), 
		new Algorithm(Sidewinder.class, "Sidewinder"),
		new Algorithm(Eller.class, "Eller's Algorithm"), 
		new Algorithm(Armin.class, "Armin's Algorithm"), 
		new Algorithm(HuntAndKill.class, "Hunt-And-Kill"),
		new Algorithm(HuntAndKillRandom.class, "Hunt-And-Kill (random)"),
		new Algorithm(RecursiveDivision.class, "Recursive Division", FullGridRequired),
		/*@formatter:on*/
	};

	private static final Algorithm[] PATHFINDER_ALGORITHMS = {
		/*@formatter:off*/
		new Algorithm(BreadthFirstSearch.class, "Breadth-First Search", BFS),
		new Algorithm(BidiBreadthFirstSearch.class, "Bidirectional Breadth-First Search", BFS),
		new Algorithm(DepthFirstSearch.class, "Depth-First Search", DFS),
		new Algorithm(DepthFirstSearch2.class, "Depth-First Search (variation)", DFS), 
		new Algorithm(IDDFS.class, "Iterative-Deepening DFS (very slow!)", DFS),
		new Algorithm(DijkstraSearch.class, "Uniform-Cost (Dijkstra) Search", BFS),
		new Algorithm(BidiDijkstraSearch.class, "Bidirectional Dijkstra Search", BFS),
		new Algorithm(HillClimbingSearch.class, "Hill-Climbing Search", DFS, INFORMED),
		new Algorithm(BestFirstSearch.class, "Greedy Best-First Search", BFS, INFORMED),
		new Algorithm(AStarSearch.class, "A* Search", BFS, INFORMED),
		new Algorithm(BidiAStarSearch.class, "Bidirectional A* Search", BFS, INFORMED),
		/*@formatter:on*/
	};

	private ObservableGridGraph<TraversalState, Integer> grid;
	private int[] gridCellSizes;
	private int gridCellSizeIndex;
	private int passageWidthPercentage;
	private boolean passageWidthFluent;
	private boolean generationAnimated;
	private int delay;
	private GridPosition generationStart;
	private boolean distancesVisible;
	private Metric metric;
	private GridPosition solverSource;
	private GridPosition solverTarget;

	public final PropertyChangeSupport changePublisher = new PropertyChangeSupport(this);

	public MazeDemoModel() {
		setGridCellSizes(256, 128, 64, 32, 16, 8, 4, 2);
		setGridCellSizeIndex(3);
		setPassageWidthPercentage(100);
		setDelay(0);
		setGenerationStart(CENTER);
		setSolverSource(TOP_LEFT);
		setSolverTarget(BOTTOM_RIGHT);
		setMetric(Metric.EUCLIDEAN);
		setGenerationAnimated(true);
		setDistancesVisible(false);
	}

	public Optional<Algorithm> findGenerator(Class<?> clazz) {
		return generators().filter(generatorInfo -> generatorInfo.getAlgorithmClass() == clazz).findFirst();
	}

	public Stream<Algorithm> generators() {
		return Arrays.stream(GENERATOR_ALGORITHMS);
	}

	public Stream<Algorithm> solvers() {
		return Arrays.stream(PATHFINDER_ALGORITHMS);
	}

	public Optional<Algorithm> findSolver(Class<?> clazz) {
		return solvers().filter(generatorInfo -> generatorInfo.getAlgorithmClass() == clazz).findFirst();
	}

	public int[] getGridCellSizes() {
		return gridCellSizes;
	}

	public void setGridCellSizes(int... gridCellSizes) {
		this.gridCellSizes = gridCellSizes;
	}

	public int getGridCellSize() {
		return gridCellSizes[gridCellSizeIndex];
	}

	public int getGridCellSizeIndex() {
		return gridCellSizeIndex;
	}

	public void setGridCellSizeIndex(int newValue) {
		if (0 <= newValue && newValue < gridCellSizes.length) {
			int oldValue = gridCellSizeIndex;
			gridCellSizeIndex = newValue;
			changePublisher.firePropertyChange("gridCellSizeIndex", oldValue, newValue);
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	public int getPassageWidthPercentage() {
		return passageWidthPercentage;
	}

	public void setPassageWidthPercentage(int newValue) {
		int oldValue = passageWidthPercentage;
		passageWidthPercentage = newValue;
		changePublisher.firePropertyChange("passageWidthPercentage", oldValue, newValue);
	}

	public boolean isPassageWidthFluent() {
		return passageWidthFluent;
	}

	public void setPassageWidthFluent(boolean newValue) {
		boolean oldValue = passageWidthFluent;
		passageWidthFluent = newValue;
		changePublisher.firePropertyChange("passageWidthFluent", oldValue, newValue);
	}

	public boolean isGenerationAnimated() {
		return generationAnimated;
	}

	public void setGenerationAnimated(boolean newValue) {
		boolean oldValue = generationAnimated;
		generationAnimated = newValue;
		changePublisher.firePropertyChange("generationAnimated", oldValue, newValue);
	}

	public ObservableGridGraph<TraversalState, Integer> getGrid() {
		return grid;
	}

	public void createGrid(int numCols, int numRows, boolean full, TraversalState defaultState) {
		ObservableGridGraph<TraversalState, Integer> oldGrid = this.grid;
		grid = full ? fullObservableGrid(numCols, numRows, Top4.get(), defaultState, 0)
				: emptyObservableGrid(numCols, numRows, Top4.get(), defaultState, 0);
		changePublisher.firePropertyChange("grid", oldGrid, grid);
	}

	public void replaceGrid(boolean full, TraversalState defaultState) {
		createGrid(grid.numCols(), grid.numRows(), full, defaultState);
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int newValue) {
		int oldValue = delay;
		delay = newValue;
		changePublisher.firePropertyChange("delay", oldValue, newValue);
	}

	public GridPosition getGenerationStart() {
		return generationStart;
	}

	public void setGenerationStart(GridPosition newValue) {
		GridPosition oldValue = generationStart;
		generationStart = newValue;
		changePublisher.firePropertyChange("generationStart", oldValue, newValue);
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric newValue) {
		Metric oldValue = metric;
		metric = newValue;
		changePublisher.firePropertyChange("metric", oldValue, newValue);
	}

	public boolean isDistancesVisible() {
		return distancesVisible;
	}

	public void setDistancesVisible(boolean newValue) {
		boolean oldValue = distancesVisible;
		distancesVisible = newValue;
		changePublisher.firePropertyChange("distancesVisible", oldValue, newValue);
	}

	public GridPosition getSolverSource() {
		return solverSource;
	}

	public void setSolverSource(GridPosition newValue) {
		GridPosition oldValue = solverSource;
		solverSource = newValue;
		changePublisher.firePropertyChange("solverSource", oldValue, newValue);
	}

	public GridPosition getSolverTarget() {
		return solverTarget;
	}

	public void setSolverTarget(GridPosition newValue) {
		GridPosition oldValue = solverTarget;
		solverTarget = newValue;
		changePublisher.firePropertyChange("solverTarget", oldValue, newValue);
	}
}
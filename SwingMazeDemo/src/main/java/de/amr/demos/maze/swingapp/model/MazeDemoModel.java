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

	private static final AlgorithmInfo[] GENERATOR_ALGORITHMS = {
		/*@formatter:off*/
		new AlgorithmInfo(RecursiveDFS.class, "Random recursive DFS (small grids only!)", Traversal, SmallGrid),
		new AlgorithmInfo(IterativeDFS.class, "Random non-recursive DFS", Traversal),
		new AlgorithmInfo(RandomBFS.class, "Random BFS", Traversal),
		new AlgorithmInfo(GrowingTreeAlwaysFirst.class, "Growing Tree (always select first)", Traversal),
		new AlgorithmInfo(GrowingTreeAlwaysLast.class, "Growing Tree (always select last)", Traversal),
		new AlgorithmInfo(GrowingTreeAlwaysRandom.class, "Growing Tree (always select random)", Traversal),
		new AlgorithmInfo(GrowingTreeLastOrRandom.class, "Growing Tree (last or random)", Traversal),
		new AlgorithmInfo(KruskalMST.class, "Kruskal MST", MST),
		new AlgorithmInfo(PrimMST.class, "Prim MST", MST),
		new AlgorithmInfo(BoruvkaMST.class, "Boruvka MST", MST),
		new AlgorithmInfo(ReverseDeleteMST_BFS.class, "Reverse-Delete MST (BFS, very slow!)", MST, Slow, FullGridRequired),
		new AlgorithmInfo(ReverseDeleteMST_BestFS.class, "Reverse-Delete MST (Best-First Search, very slow!)", MST, Slow, FullGridRequired),
		new AlgorithmInfo(ReverseDeleteMST_DFS.class, "Reverse-Delete MST (DFS, very slow!)", MST, Slow, FullGridRequired),
		new AlgorithmInfo(ReverseDeleteMST_HillClimbing.class, "Reverse-Delete MST (Hill-Climbing, very slow!)", MST, Slow, FullGridRequired),
		new AlgorithmInfo(ReverseDeleteMST_BidiAStar.class, "Reverse-Delete MST (Bidi A*, very slow!)", MST, Slow, FullGridRequired),
		new AlgorithmInfo(AldousBroderUST.class, "Aldous-Broder UST (rather slow)", UST, Slow),
		new AlgorithmInfo(AldousBroderWilsonUST.class, "Houston UST (rather slow)", UST, Slow),
		new AlgorithmInfo(WilsonUSTRandomCell.class, "Wilson UST (random)", UST, Slow),
		new AlgorithmInfo(WilsonUSTRowsTopDown.class, "Wilson UST (row-wise, top-to-bottom)", UST),
		new AlgorithmInfo(WilsonUSTLeftToRightSweep.class, "Wilson UST (column-wise, left to right)", UST),
		new AlgorithmInfo(WilsonUSTRightToLeftSweep.class, "Wilson UST (column-wise, right to left)", UST),
		new AlgorithmInfo(WilsonUSTCollapsingWalls.class, "Wilson UST (column-wise, collapsing)", UST),
		new AlgorithmInfo(WilsonUSTCollapsingRectangle.class, "Wilson UST (collapsing rectangle)", UST),
		new AlgorithmInfo(WilsonUSTExpandingCircle.class, "Wilson UST (expanding circle)", UST),
		new AlgorithmInfo(WilsonUSTCollapsingCircle.class, "Wilson UST (collapsing circle)", UST),
		new AlgorithmInfo(WilsonUSTExpandingCircles.class, "Wilson UST (expanding circles)", UST),
		new AlgorithmInfo(WilsonUSTExpandingSpiral.class, "Wilson UST (expanding spiral)", UST),
		new AlgorithmInfo(WilsonUSTExpandingRectangle.class, "Wilson UST (expanding rectangle)", UST),
		new AlgorithmInfo(WilsonUSTNestedRectangles.class, "Wilson UST (nested rectangles)", UST),
		new AlgorithmInfo(WilsonUSTRecursiveCrosses.class, "Wilson UST (recursive crosses)", UST),
		new AlgorithmInfo(WilsonUSTHilbertCurve.class, "Wilson UST (Hilbert curve)", UST),
		new AlgorithmInfo(WilsonUSTMooreCurve.class, "Wilson UST (Moore curve)", UST),
		new AlgorithmInfo(WilsonUSTPeanoCurve.class, "Wilson UST (Peano curve)", UST),
		new AlgorithmInfo(BinaryTree.class, "Binary Tree (row-wise, top-to-bottom)"),
		new AlgorithmInfo(BinaryTreeRandom.class, "Binary Tree (random)"), 
		new AlgorithmInfo(Sidewinder.class, "Sidewinder"),
		new AlgorithmInfo(Eller.class, "Eller's Algorithm"), 
		new AlgorithmInfo(Armin.class, "Armin's Algorithm"), 
		new AlgorithmInfo(HuntAndKill.class, "Hunt-And-Kill"),
		new AlgorithmInfo(HuntAndKillRandom.class, "Hunt-And-Kill (random)"),
		new AlgorithmInfo(RecursiveDivision.class, "Recursive Division", FullGridRequired),
		/*@formatter:on*/
	};

	private static final AlgorithmInfo[] PATHFINDER_ALGORITHMS = {
		/*@formatter:off*/
		new AlgorithmInfo(BreadthFirstSearch.class, "Breadth-First Search", BFS),
		new AlgorithmInfo(BidiBreadthFirstSearch.class, "Bidirectional Breadth-First Search", BFS),
		new AlgorithmInfo(DepthFirstSearch.class, "Depth-First Search", DFS),
		new AlgorithmInfo(DepthFirstSearch2.class, "Depth-First Search (variation)", DFS), 
		new AlgorithmInfo(IDDFS.class, "Iterative-Deepening DFS (very slow!)", DFS),
		new AlgorithmInfo(DijkstraSearch.class, "Uniform-Cost (Dijkstra) Search", BFS),
		new AlgorithmInfo(BidiDijkstraSearch.class, "Bidirectional Dijkstra Search", BFS),
		new AlgorithmInfo(HillClimbingSearch.class, "Hill-Climbing Search", DFS, INFORMED),
		new AlgorithmInfo(BestFirstSearch.class, "Greedy Best-First Search", BFS, INFORMED),
		new AlgorithmInfo(AStarSearch.class, "A* Search", BFS, INFORMED),
		new AlgorithmInfo(BidiAStarSearch.class, "Bidirectional A* Search", BFS, INFORMED),
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
	private boolean floodFillAfterGeneration;
	private boolean distancesVisible;
	private Metric metric;
	private GridPosition pathFinderStart;
	private GridPosition pathFinderTarget;

	public final PropertyChangeSupport changeHandler = new PropertyChangeSupport(this);

	public MazeDemoModel() {
		setGridCellSizes(256, 128, 64, 32, 16, 8, 4, 2);
		setGridCellSizeIndex(3);
		setPassageWidthPercentage(100);
		setDelay(0);
		setGenerationStart(CENTER);
		setPathFinderStart(TOP_LEFT);
		setPathFinderTarget(BOTTOM_RIGHT);
		setMetric(Metric.EUCLIDEAN);
		setGenerationAnimated(true);
		setFloodFillAfterGeneration(false);
		setDistancesVisible(false);
	}

	public Optional<AlgorithmInfo> findGenerator(Class<?> clazz) {
		return generators().filter(generatorInfo -> generatorInfo.getAlgorithmClass() == clazz).findFirst();
	}

	public Stream<AlgorithmInfo> generators() {
		return Arrays.stream(GENERATOR_ALGORITHMS);
	}

	public Stream<AlgorithmInfo> solvers() {
		return Arrays.stream(PATHFINDER_ALGORITHMS);
	}

	public Optional<AlgorithmInfo> findSolver(Class<?> clazz) {
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

	public void setGridCellSizeIndex(int gridCellSizeIndex) {
		if (0 <= gridCellSizeIndex && gridCellSizeIndex < gridCellSizes.length) {
			this.gridCellSizeIndex = gridCellSizeIndex;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	public int getPassageWidthPercentage() {
		return passageWidthPercentage;
	}

	public void setPassageWidthPercentage(int newWidthPercentage) {
		int oldWidthPercentage = this.passageWidthPercentage;
		this.passageWidthPercentage = newWidthPercentage;
		changeHandler.firePropertyChange("passageWidthPercentage", oldWidthPercentage, newWidthPercentage);
	}

	public boolean isPassageWidthFluent() {
		return passageWidthFluent;
	}

	public void setPassageWidthFluent(boolean fluent) {
		this.passageWidthFluent = fluent;
	}

	public boolean isGenerationAnimated() {
		return generationAnimated;
	}

	public void setGenerationAnimated(boolean generationAnimated) {
		this.generationAnimated = generationAnimated;
	}

	public ObservableGridGraph<TraversalState, Integer> getGrid() {
		return grid;
	}

	public void createGrid(int numCols, int numRows, boolean full, TraversalState defaultState) {
		ObservableGridGraph<TraversalState, Integer> oldGrid = this.grid;
		grid = full ? fullObservableGrid(numCols, numRows, Top4.get(), defaultState, 0)
				: emptyObservableGrid(numCols, numRows, Top4.get(), defaultState, 0);
		changeHandler.firePropertyChange("grid", oldGrid, grid);
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public GridPosition getGenerationStart() {
		return generationStart;
	}

	public void setGenerationStart(GridPosition pos) {
		this.generationStart = pos;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public boolean isFloodFillAfterGeneration() {
		return floodFillAfterGeneration;
	}

	public void setFloodFillAfterGeneration(boolean floodFillAfterGeneration) {
		this.floodFillAfterGeneration = floodFillAfterGeneration;
	}

	public boolean isDistancesVisible() {
		return distancesVisible;
	}

	public void setDistancesVisible(boolean distancesVisible) {
		this.distancesVisible = distancesVisible;
	}

	public GridPosition getPathFinderSource() {
		return pathFinderStart;
	}

	public void setPathFinderStart(GridPosition pos) {
		this.pathFinderStart = pos;
	}

	public GridPosition getPathFinderTarget() {
		return pathFinderTarget;
	}

	public void setPathFinderTarget(GridPosition pos) {
		this.pathFinderTarget = pos;
	}
}
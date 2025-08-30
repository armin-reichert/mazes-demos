module mazes.demos.MazeDemos {

    requires de.amr.graph.core;
    requires de.amr.graph.grid;
    requires de.amr.graph.pathfinder;
    requires de.amr.graph.viz;
    requires de.amr.maze.alg;
    requires java.desktop;
    requires jcommander;

    exports de.amr.demos.grid.maze.eight;
    exports de.amr.demos.grid.maze.recording;
    exports de.amr.demos.grid.maze.swing;
    exports de.amr.demos.grid.pathfinding;
    exports de.amr.demos.grid.rendering;
}
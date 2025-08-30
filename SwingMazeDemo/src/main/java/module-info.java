module mazes.demos.SwingMazeDemo {
    requires java.desktop;
    requires jcommander;
    requires de.amr.graph.core;
    requires de.amr.graph.pathfinder;
    requires de.amr.maze.alg;
    requires de.amr.graph.viz;
    requires com.miglayout.swing;

    exports de.amr.maze.demo;
    exports de.amr.maze.demo.model;
    exports de.amr.maze.demo.ui.common;
    exports de.amr.maze.demo.ui.control;
    exports de.amr.maze.demo.ui.control.action;
    exports de.amr.maze.demo.ui.grid;
}
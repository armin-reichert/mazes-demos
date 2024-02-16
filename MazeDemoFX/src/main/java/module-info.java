module de.amr.demos.maze.javafx {

	requires org.tinylog.api;

	exports de.amr.demos.maze.javafx to javafx.graphics;

	requires transitive javafx.controls;
	requires transitive de.amr.maze.alg;
}
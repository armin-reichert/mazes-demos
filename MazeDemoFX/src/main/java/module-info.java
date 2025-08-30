module de.amr.demos.maze.javafx {

    requires transitive javafx.controls;
    requires transitive de.amr.maze.alg;
	requires org.tinylog.api;

	exports de.amr.demos.maze.javafx to javafx.graphics;

}
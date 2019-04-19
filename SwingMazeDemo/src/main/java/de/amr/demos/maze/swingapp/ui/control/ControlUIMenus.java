package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.demos.maze.swingapp.model.GeneratorTag.MST;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Traversal;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.UST;

import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import de.amr.demos.maze.swingapp.model.Algorithm;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.common.MenuBuilder;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.graph.grid.api.GridPosition;

/**
 * The menus of the control window.
 * 
 * @author Armin Reichert
 */
public class ControlUIMenus {

	public static Optional<Algorithm> getSelectedAlgorithm(JMenu radioButtonMenu) {
		ButtonGroup radio = (ButtonGroup) radioButtonMenu.getClientProperty("radio");
		return Collections.list(radio.getElements()).stream().filter(AbstractButton::isSelected)
				.map(button -> (Algorithm) button.getClientProperty("algorithm")).findFirst();
	}

	public static void selectAlgorithm(JMenu radioButtonMenu, Algorithm algorithm) {
		ButtonGroup radio = (ButtonGroup) radioButtonMenu.getClientProperty("radio");
		Collections.list(radio.getElements()).stream()
				.filter(button -> algorithm.equals(button.getClientProperty("algorithm"))).findFirst()
				.ifPresent(button -> button.setSelected(true));
	}

	// Maze generator menu

	public static JMenu buildGeneratorMenu(ControlUI controller) {
		ButtonGroup radio = new ButtonGroup();
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Generators")
			.property("radio", radio)
			.items(
				generatorMenu(controller, radio, "Graph Traversal", algorithm -> algorithm.isTagged(Traversal)),
				generatorMenu(controller, radio, "Minimum Spanning Tree", algorithm -> algorithm.isTagged(MST)),
				generatorMenu(controller, radio, "Uniform Spanning Tree", algorithm -> algorithm.isTagged(UST)),
				generatorMenu(controller, radio, "Others", 
					algorithm -> !(algorithm.isTagged(Traversal) || algorithm.isTagged(MST) || algorithm.isTagged(UST)))
			)
		.build();
		//@formatter:on
	}

	private static JMenu generatorMenu(ControlUI controller, ButtonGroup radio, String title,
			Predicate<Algorithm> selection) {
		JMenu menu = new JMenu(title);
		controller.getModel().generators().filter(selection).forEach(generator -> {
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
			radioButton.addActionListener(e -> controller.selectGenerator(generator));
			radioButton.setText(generator.getDescription());
			radioButton.putClientProperty("algorithm", generator);
			radio.add(radioButton);
			menu.add(radioButton);
		});
		return menu;
	}

	// Solver menu

	public static JMenu buildSolverMenu(ControlUI controller) {
		ButtonGroup radio = new ButtonGroup();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Solvers")
				.property("radio", radio)
				.caption("Uninformed Solvers")
				.items(solverItems(controller, radio, solver -> !solver.isTagged(SolverTag.INFORMED)))
				.separator()
				.caption("Informed Solvers")
				.menu(buildMetricsMenu(controller))
				.items(solverItems(controller, radio, solver -> solver.isTagged(SolverTag.INFORMED)))
		.build();
		//@formatter:on
	}

	private static Stream<JMenuItem> solverItems(ControlUI controller, ButtonGroup radio,
			Predicate<Algorithm> selection) {
		return controller.getModel().solvers().filter(selection).map(solver -> {
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
			radioButton.addActionListener(event -> controller.selectSolver(solver));
			radioButton.setText(solver.getDescription());
			radioButton.putClientProperty("algorithm", solver);
			radio.add(radioButton);
			return radioButton;
		});
	}

	private static JMenu buildMetricsMenu(ControlUI controller) {
		Function<Metric, String> translation = metric -> metric.name().substring(0, 1)
				+ metric.name().substring(1).toLowerCase();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Metric")
				.radioButtonGroup(Metric.class)
					.onSelect(controller.getModel()::setMetric)
					.selection(controller.getModel()::getMetric)
					.button().selectionValue(Metric.EUCLIDEAN).text(translation.apply(Metric.EUCLIDEAN)).build()
					.button().selectionValue(Metric.MANHATTAN).text(translation.apply(Metric.MANHATTAN)).build()
					.button().selectionValue(Metric.CHEBYSHEV).text(translation.apply(Metric.CHEBYSHEV)).build()
				.build()
		.build();
		//@formatter:on
	}

	// Canvas menu

	public static JMenu buildCanvasMenu(ControlUI controller) {
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Canvas")
			.action(controller.actionClearCanvas)
			.action(controller.actionFloodFill)
			.separator()
			.action(controller.actionCreateEmptyGrid)
			.action(controller.actionCreateFullGrid)
			.separator()
			.action(controller.actionSaveImage)
		.build();		
		//@formatter:on
	}

	// Option menu

	public static JMenu buildOptionMenu(ControlUI controller) {
		final MazeDemoModel model = controller.getModel();
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Options")
			.menu(buildPositionMenu("Generation Start", model::setGenerationStart, model::getGenerationStart))
			.menu(buildPositionMenu("Solution Start", model::setSolverSource, model::getSolverSource))
			.menu(buildPositionMenu("Solution Target", model::setSolverTarget, model::getSolverTarget))
			.separator()
			.radioButtonGroup(AfterGenerationAction.class)
				.onSelect(controller::setAfterGenerationAction)
				.selection(controller::getAfterGenerationAction)
				.button()
					.text("No action after generation")
					.selectionValue(AfterGenerationAction.NOTHING)
					.build()
				.button()
					.text("Solve after generation")
					.selectionValue(AfterGenerationAction.SOLVE)
					.build()
				.button()
					.text("Flood-fill after generation")
					.selectionValue(AfterGenerationAction.FLOOD_FILL)
					.build()
			.build()
			.separator()
			.checkBox()
				.text("Animate Generation")
				.onSelect(model::setGenerationAnimated)
				.selection(model::isGenerationAnimated)
				.build()
			.checkBox()
				.text("Show distances")
				.onSelect(model::setDistancesVisible)
				.selection(model::isDistancesVisible)
				.build()
			.checkBox()
				.text("Fluent Passage Width")
				.onSelect(model::setPassageWidthFluent)
				.selection(model::isPassageWidthFluent)
				.build()
			.separator()	
			.checkBox()
				.text("Hide this dialog when running")
				.onSelect(controller::setHiddenWhenBusy)
				.selection(controller::isHiddenWhenBusy)
				.build()
		.build();
		//@formatter:on
	}

	private static JMenu buildPositionMenu(String title, Consumer<GridPosition> onSelect,
			Supplier<GridPosition> selection) {
		Function<GridPosition, String> translation = position -> ResourceBundle.getBundle("texts")
				.getString(position.name());
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title(title)
			.radioButtonGroup(GridPosition.class)
				.selection(selection)
				.onSelect(onSelect)
				.button()
					.selectionValue(GridPosition.CENTER)
					.text(translation.apply(GridPosition.CENTER))
					.build()
				.button()
					.selectionValue(GridPosition.TOP_LEFT)
					.text(translation.apply(GridPosition.TOP_LEFT))
					.build()
				.button()
					.selectionValue(GridPosition.TOP_RIGHT)
					.text(translation.apply(GridPosition.TOP_RIGHT))
					.build()
				.button()
					.selectionValue(GridPosition.BOTTOM_LEFT)
					.text(translation.apply(GridPosition.BOTTOM_LEFT))
					.build()
				.button()
					.selectionValue(GridPosition.BOTTOM_RIGHT)
					.text(translation.apply(GridPosition.BOTTOM_RIGHT))
					.build()
			.build()
		.build();
		//@formatter:on
	}
}
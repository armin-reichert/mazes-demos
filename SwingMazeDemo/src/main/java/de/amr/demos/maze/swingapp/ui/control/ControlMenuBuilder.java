package de.amr.demos.maze.swingapp.ui.control;

import static de.amr.demos.maze.swingapp.model.GeneratorTag.MST;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Traversal;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.UST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.common.MenuBuilder;
import de.amr.graph.grid.api.GridPosition;

/**
 * Builds menus of control window.
 * 
 * @author Armin Reichert
 */
public class ControlMenuBuilder {

	public static Optional<AlgorithmInfo> getSelectedAlgorithm(JMenu radioButtonMenu) {
		ButtonGroup radio = (ButtonGroup) radioButtonMenu.getClientProperty("radio");
		return Collections.list(radio.getElements()).stream().filter(AbstractButton::isSelected)
				.map(button -> (AlgorithmInfo) button.getClientProperty("algorithm")).findFirst();
	}

	public static void selectAlgorithm(JMenu radioButtonMenu, AlgorithmInfo algorithmInfo) {
		ButtonGroup radio = (ButtonGroup) radioButtonMenu.getClientProperty("radio");
		Collections.list(radio.getElements()).stream()
				.filter(button -> algorithmInfo.equals(button.getClientProperty("algorithm"))).findFirst()
				.ifPresent(button -> button.setSelected(true));
	}

	// Generator menu

	public static JMenu buildGeneratorMenu(ControlViewController controller) {
		ButtonGroup radio = new ButtonGroup();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Generators")
				.property("radio", radio)
				.menu(buildGeneratorMenu(controller, radio, "Graph Traversal", alg -> alg.isTagged(Traversal)))
				.menu(buildGeneratorMenu(controller, radio, "Minimum Spanning Tree", alg -> alg.isTagged(MST)))
				.menu(buildGeneratorMenu(controller, radio, "Uniform Spanning Tree", alg -> alg.isTagged(UST)))
				.menu(buildGeneratorMenu(controller, radio, "Others", 
						alg -> !(alg.isTagged(Traversal) || alg.isTagged(MST) || alg.isTagged(UST))))
		.build();
		//@formatter:on
	}

	private static JMenu buildGeneratorMenu(ControlViewController controller, ButtonGroup radio, String title,
			Predicate<AlgorithmInfo> selection) {
		JMenu menu = new JMenu(title);
		controller.getModel().generators().filter(selection).forEach(generator -> {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem();
			item.addActionListener(e -> controller.selectGenerator(generator));
			item.setText(generator.getDescription());
			item.putClientProperty("algorithm", generator);
			radio.add(item);
			menu.add(item);
		});
		return menu;
	}

	// Solver menu

	public static JMenu buildSolverMenu(ControlViewController controller) {
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

	private static Stream<JMenuItem> solverItems(ControlViewController controller, ButtonGroup radio,
			Predicate<AlgorithmInfo> selection) {
		List<JMenuItem> items = new ArrayList<>();
		controller.getModel().solvers().filter(selection).forEach(solver -> {
			items.add(buildSolverRadioButton(controller, radio, solver));
		});
		return items.stream();
	}

	private static JRadioButtonMenuItem buildSolverRadioButton(ControlViewController controller,
			ButtonGroup radio, AlgorithmInfo solver) {
		JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
		radioButton.addActionListener(event -> controller.selectSolver(solver));
		radioButton.setText(solver.getDescription());
		radioButton.putClientProperty("algorithm", solver);
		radio.add(radioButton);
		return radioButton;
	}

	private static JMenu buildMetricsMenu(ControlViewController controller) {
		JMenu menu = new JMenu("Metric");
		ButtonGroup radio = new ButtonGroup();
		for (Metric metric : Metric.values()) {
			String text = metric.name().substring(0, 1) + metric.name().substring(1).toLowerCase();
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(text);
			radioButton.addActionListener(e -> controller.getModel().setMetric(metric));
			radioButton.setSelected(metric == controller.getModel().getMetric());
			radio.add(radioButton);
			menu.add(radioButton);
		}
		return menu;
	}

	// Canvas menu

	public static JMenu buildCanvasMenu(ControlViewController controller) {
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Canvas")
				.button().action(controller.actionClearCanvas).build()
				.button().action(controller.actionFloodFill).build()
				.separator()
				.button().action(controller.actionCreateEmptyGrid).build()
				.button().action(controller.actionCreateFullGrid).build()
				.separator()
				.button().action(controller.actionSaveImage).build()
		.build();		
		//@formatter:on
	}

	// Option menu

	public static JMenu buildOptionMenu(ControlViewController controller) {
		final MazeDemoModel model = controller.getModel();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Options")
				.menu(buildPositionMenu("Generation Start", model::setGenerationStart, model::getGenerationStart))
				.menu(buildPositionMenu("Solution Start", model::setPathFinderStart, model::getPathFinderSource))
				.menu(buildPositionMenu("Solution Target", model::setPathFinderTarget, model::getPathFinderTarget))
				.separator()
				.checkBox()
					.text("Animate Generation")
				  .onChecked(model::setGenerationAnimated)
				  .selection(model::isGenerationAnimated)
				  .build()
				.checkBox()
					.text("Flood-fill after generation")
				  .onChecked(model::setFloodFillAfterGeneration)
				  .selection(model::isFloodFillAfterGeneration)
				  .build()
				.checkBox()
					.text("Show distances")
				  .onChecked(model::setDistancesVisible)
				  .selection(model::isDistancesVisible)
				  .build()
				.checkBox()
					.text("Fluent Passage Width")
					.onChecked(model::setPassageWidthFluent)
					.selection(model::isPassageWidthFluent)
					.build()
				.separator()	
				.checkBox()
					.text("Hide this dialog when running")
					.onChecked(controller::setHidingWindowWhenBusy)
					.selection(controller::isHidingWindowWhenBusy)
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
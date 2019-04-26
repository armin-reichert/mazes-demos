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
import de.amr.demos.maze.swingapp.model.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.ui.control.action.AfterGenerationAction;
import de.amr.graph.grid.api.GridPosition;
import de.amr.swing.MenuBuilder;

/**
 * The menus of the control window.
 * 
 * @author Armin Reichert
 */
class ControlUIMenus {

	private static Optional<Algorithm> getSelectedAlgorithm(JMenu menu) {
		ButtonGroup radio = (ButtonGroup) menu.getClientProperty("radio");
		return Collections.list(radio.getElements()).stream().filter(AbstractButton::isSelected)
				.map(button -> (Algorithm) button.getClientProperty("algorithm")).findFirst();
	}

	private static void selectAlgorithm(JMenu menu, Algorithm algorithm) {
		ButtonGroup radio = (ButtonGroup) menu.getClientProperty("radio");
		Collections.list(radio.getElements()).stream()
				.filter(button -> algorithm.equals(button.getClientProperty("algorithm"))).findFirst()
				.ifPresent(button -> button.setSelected(true));
	}

	private final ControlUI controlUI;
	private final JMenu generatorMenu;
	private final JMenu canvasMenu;
	private final JMenu solverMenu;
	private final JMenu optionMenu;

	public ControlUIMenus(ControlUI controlUI) {
		this.controlUI = controlUI;
		generatorMenu = buildGeneratorMenu();
		solverMenu = buildSolverMenu();
		canvasMenu = buildCanvasMenu();
		optionMenu = buildOptionMenu();
	}

	public JMenu getGeneratorMenu() {
		return generatorMenu;
	}

	public JMenu getSolverMenu() {
		return solverMenu;
	}

	public JMenu getCanvasMenu() {
		return canvasMenu;
	}

	public JMenu getOptionMenu() {
		return optionMenu;
	}

	public void updateSelection() {
		MenuBuilder.updateMenuSelection(generatorMenu);
		MenuBuilder.updateMenuSelection(solverMenu);
		MenuBuilder.updateMenuSelection(canvasMenu);
		MenuBuilder.updateMenuSelection(optionMenu);
	}

	public void selectGenerator(Algorithm generator) {
		selectAlgorithm(generatorMenu, generator);
	}

	public Optional<Algorithm> getSelectedGenerator() {
		return getSelectedAlgorithm(generatorMenu);
	}

	public void selectSolver(Algorithm solver) {
		selectAlgorithm(solverMenu, solver);
	}

	public Optional<Algorithm> getSelectedSolver() {
		return getSelectedAlgorithm(solverMenu);
	}

	// Maze generator menu

	private JMenu buildGeneratorMenu() {
		ButtonGroup radio = new ButtonGroup();
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Generators")
			.property("radio", radio)
			.items(
				generatorMenu("Graph Traversal", radio, algorithm -> algorithm.isTagged(Traversal)),
				generatorMenu("Minimum Spanning Tree", radio, algorithm -> algorithm.isTagged(MST)),
				generatorMenu("Uniform Spanning Tree", radio, algorithm -> algorithm.isTagged(UST)),
				generatorMenu("Others", radio,
					algorithm -> !(algorithm.isTagged(Traversal) || algorithm.isTagged(MST) || algorithm.isTagged(UST)))
			)
		.build();
		//@formatter:on
	}

	private JMenu generatorMenu(String title, ButtonGroup radio, Predicate<Algorithm> selection) {
		JMenu menu = new JMenu(title);
		controlUI.getModel().generators().filter(selection).forEach(generator -> {
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
			radioButton.addActionListener(e -> controlUI.selectGenerator(generator));
			radioButton.setText(generator.getDescription());
			radioButton.putClientProperty("algorithm", generator);
			radio.add(radioButton);
			menu.add(radioButton);
		});
		return menu;
	}

	// Solver menu

	private JMenu buildSolverMenu() {
		ButtonGroup radio = new ButtonGroup();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Solvers")
				.property("radio", radio)
				.caption("Uninformed Solvers")
				.items(solverItems(radio, solver -> !solver.isTagged(SolverTag.INFORMED)))
				.separator()
				.caption("Informed Solvers")
				.menu(buildMetricsMenu())
				.items(solverItems(radio, solver -> solver.isTagged(SolverTag.INFORMED)))
		.build();
		//@formatter:on
	}

	private Stream<JMenuItem> solverItems(ButtonGroup radio, Predicate<Algorithm> selection) {
		return controlUI.getModel().solvers().filter(selection).map(solver -> {
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
			radioButton.addActionListener(event -> controlUI.selectSolver(solver));
			radioButton.setText(solver.getDescription());
			radioButton.putClientProperty("algorithm", solver);
			radio.add(radioButton);
			return radioButton;
		});
	}

	private JMenu buildMetricsMenu() {
		Function<Metric, String> displayName = metric -> metric.name().substring(0, 1)
				+ metric.name().substring(1).toLowerCase();
		//@formatter:off
		return MenuBuilder.newBuilder()
				.title("Metric")
				.radioButtonGroup(Metric.class)
					.onSelect(controlUI.getModel()::setMetric)
					.selection(controlUI.getModel()::getMetric)
					.button().selectionValue(Metric.EUCLIDEAN).text(displayName.apply(Metric.EUCLIDEAN)).build()
					.button().selectionValue(Metric.MANHATTAN).text(displayName.apply(Metric.MANHATTAN)).build()
					.button().selectionValue(Metric.CHEBYSHEV).text(displayName.apply(Metric.CHEBYSHEV)).build()
				.build()
		.build();
		//@formatter:on
	}

	// Canvas menu

	private JMenu buildCanvasMenu() {
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Canvas")
			.action(controlUI.actionClearCanvas)
			.action(controlUI.actionFloodFill)
			.separator()
			.action(controlUI.actionCreateEmptyGrid)
			.action(controlUI.actionCreateFullGrid)
			.separator()
			.action(controlUI.actionSaveImage)
		.build();		
		//@formatter:on
	}

	// Option menu

	private JMenu buildOptionMenu() {
		final MazeDemoModel model = controlUI.getModel();
		//@formatter:off
		return MenuBuilder.newBuilder()
			.title("Options")
			.menu(buildPositionMenu("Generation Start", model::setGenerationStart, model::getGenerationStart))
			.menu(buildPositionMenu("Solution Start", model::setSolverSource, model::getSolverSource))
			.menu(buildPositionMenu("Solution Target", model::setSolverTarget, model::getSolverTarget))
			.separator()
			.radioButtonGroup(AfterGenerationAction.class)
				.onSelect(controlUI::setAfterGenerationAction)
				.selection(controlUI::getAfterGenerationAction)
				.button()
					.text("No action after generation")
					.selectionValue(AfterGenerationAction.IDLE)
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
				.onToggle(model::setGenerationAnimated)
				.selection(model::isGenerationAnimated)
				.build()
			.checkBox()
				.text("Show distances")
				.onToggle(model::setDistancesVisible)
				.selection(model::isDistancesVisible)
				.build()
			.checkBox()
				.text("Fluent Passage Width")
				.onToggle(model::setPassageWidthFluent)
				.selection(model::isPassageWidthFluent)
				.build()
			.separator()	
			.checkBox()
				.text("Hide this dialog when running")
				.onToggle(controlUI::setHiddenWhenBusy)
				.selection(controlUI::isHiddenWhenBusy)
				.build()
		.build();
		//@formatter:on
	}

	private JMenu buildPositionMenu(String title, Consumer<GridPosition> onSelect,
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
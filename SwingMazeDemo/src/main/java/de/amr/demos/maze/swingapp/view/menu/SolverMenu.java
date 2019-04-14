package de.amr.demos.maze.swingapp.view.menu;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;
import de.amr.demos.maze.swingapp.model.MazeDemoModel.Metric;
import de.amr.demos.maze.swingapp.model.SolverTag;
import de.amr.demos.maze.swingapp.view.ControlViewController;

/**
 * Menu for selecting the path finder algorithm.
 * 
 * @author Armin Reichert
 */
public class SolverMenu extends AlgorithmMenu {

	private final ControlViewController controller;

	public SolverMenu(ControlViewController controller) {
		this.controller = controller;
		setText("Solvers");
		add(new JMenuItem("Uninformed Solvers")).setEnabled(false);
		controller.getModel().solvers().filter(solverInfo -> !solverInfo.isTagged(SolverTag.INFORMED))
				.forEach(this::addSolverMenuItem);
		addSeparator();
		add(new JMenuItem("Informed Solvers")).setEnabled(false);
		addMetricsMenu();
		controller.getModel().solvers().filter(solverInfo -> solverInfo.isTagged(SolverTag.INFORMED))
				.forEach(this::addSolverMenuItem);
	}

	private void addSolverMenuItem(AlgorithmInfo solverInfo) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem();
		item.addActionListener(event -> controller.selectSolver(solverInfo));
		item.setText(solverInfo.getDescription());
		item.putClientProperty("algorithm", solverInfo);
		buttonGroup.add(item);
		add(item);
	}

	private void addMetricsMenu() {
		JMenu menu = new JMenu("Metric");
		ButtonGroup radio = new ButtonGroup();
		for (Metric metric : Metric.values()) {
			String text = metric.name().substring(0, 1) + metric.name().substring(1).toLowerCase();
			JRadioButtonMenuItem rb = new JRadioButtonMenuItem(text);
			rb.addActionListener(e -> {
				controller.getModel().setMetric(metric);
				getSelectedAlgorithm().ifPresent(controller::selectSolver);
			});
			radio.add(rb);
			menu.add(rb);
			rb.setSelected(metric == controller.getModel().getMetric());
		}
		add(menu);
	}
}
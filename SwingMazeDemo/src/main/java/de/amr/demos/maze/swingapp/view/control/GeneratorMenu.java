package de.amr.demos.maze.swingapp.view.control;

import static de.amr.demos.maze.swingapp.model.GeneratorTag.MST;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.Traversal;
import static de.amr.demos.maze.swingapp.model.GeneratorTag.UST;

import java.util.function.Predicate;

import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;

/**
 * Menu for selecting the maze generation algorithm.
 * 
 * @author Armin Reichert
 */
public class GeneratorMenu extends AlgorithmMenu {

	private final ControlViewController controller;

	public GeneratorMenu(ControlViewController controller) {
		this.controller = controller;
		setText("Generators");
		addMenu("Graph Traversal", alg -> alg.isTagged(Traversal));
		addMenu("Minimum Spanning Tree", alg -> alg.isTagged(MST));
		addMenu("Uniform Spanning Tree", alg -> alg.isTagged(UST));
		addMenu("Others", alg -> !(alg.isTagged(Traversal) || alg.isTagged(MST) || alg.isTagged(UST)));
	}

	private void addMenu(String title, Predicate<AlgorithmInfo> includeInMenu) {
		JMenu menu = new JMenu(title);
		controller.getModel().generators().filter(includeInMenu).forEach(generatorInfo -> {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem();
			item.addActionListener(e -> controller.selectGenerator(generatorInfo));
			item.setText(generatorInfo.getDescription());
			item.putClientProperty("algorithm", generatorInfo);
			buttonGroup.add(item);
			menu.add(item);
		});
		add(menu);
	}
}
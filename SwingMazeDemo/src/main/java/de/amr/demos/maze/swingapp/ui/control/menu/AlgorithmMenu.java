package de.amr.demos.maze.swingapp.ui.control.menu;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;

import de.amr.demos.maze.swingapp.model.AlgorithmInfo;

/**
 * Base class for algorithm menus.
 * 
 * @author Armin Reichert
 */
public abstract class AlgorithmMenu extends JMenu {

	protected final ButtonGroup buttonGroup = new ButtonGroup();

	protected Stream<AbstractButton> buttons() {
		return Collections.list(buttonGroup.getElements()).stream();
	}

	public Optional<AlgorithmInfo> getSelectedAlgorithm() {
		return buttons().filter(AbstractButton::isSelected)
				.map(btn -> (AlgorithmInfo) btn.getClientProperty("algorithm")).findFirst();
	}

	public void selectAlgorithm(AlgorithmInfo alg) {
		buttons().filter(item -> alg.equals(item.getClientProperty("algorithm"))).findFirst()
				.ifPresent(btn -> btn.setSelected(true));
	}
}
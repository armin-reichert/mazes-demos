package de.amr.demos.maze.swingapp.ui.control.menu;

import java.awt.event.ItemEvent;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import de.amr.demos.maze.swingapp.model.MazeDemoModel;
import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.graph.grid.api.GridPosition;

/**
 * Menu for selecting generation and path finder options.
 * 
 * @author Armin Reichert
 */
public class OptionMenu extends JMenu {

	public OptionMenu(ControlViewController controller) {
		final MazeDemoModel model = controller.getModel();
		setText("Options");
		addPositionMenu("Generation Start", model::setGenerationStart, model::getGenerationStart);
		addPositionMenu("Solution Start", model::setPathFinderStart, model::getPathFinderSource);
		addPositionMenu("Solution Target", model::setPathFinderTarget, model::getPathFinderTarget);
		addSeparator();
		addCheckBox("Animate Generation", model::setGenerationAnimated, model::isGenerationAnimated);
		addCheckBox("Flood-fill after generation", model::setFloodFillAfterGeneration,
				model::isFloodFillAfterGeneration);
		addCheckBox("Show distances", model::setDistancesVisible, model::isDistancesVisible);
		addCheckBox("Fluent Passage Width", model::setPassageWidthFluent, model::isPassageWidthFluent);
		addSeparator();
		addCheckBox("Hide this dialog when running", controller::setHidingWindowWhenBusy,
				controller::isHidingWindowWhenBusy);
	}

	public void updateState() {
		for (int i = 0; i < getItemCount(); ++i) {
			if (getItem(i) instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) getItem(i);
				BooleanSupplier fnSelection = (BooleanSupplier) cb.getClientProperty("selection");
				cb.setSelected(fnSelection.getAsBoolean());
			}
		}
	}

	private void addCheckBox(String title, Consumer<Boolean> onChecked, BooleanSupplier fnSelection) {
		JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(title);
		checkBox.putClientProperty("selection", fnSelection);
		checkBox.addActionListener(evt -> onChecked.accept(checkBox.isSelected()));
		checkBox.setSelected(fnSelection.getAsBoolean());
		add(checkBox);
	}

	private void addPositionMenu(String title, Consumer<GridPosition> onSelection,
			Supplier<GridPosition> selection) {
		JMenu menu = new JMenu(title);
		ButtonGroup group = new ButtonGroup();
		ResourceBundle texts = ResourceBundle.getBundle("texts");
		for (GridPosition pos : GridPosition.values()) {
			JRadioButtonMenuItem radio = new JRadioButtonMenuItem();
			radio.setText(texts.getString(pos.name()));
			radio.setSelected(pos == selection.get());
			radio.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					onSelection.accept(pos);
				}
			});
			menu.add(radio);
			group.add(radio);
		}
		add(menu);
	}
}
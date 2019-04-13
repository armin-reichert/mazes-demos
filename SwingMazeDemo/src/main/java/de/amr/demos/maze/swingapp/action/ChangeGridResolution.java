package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

/**
 * Action for changing the grid resolution.
 * 
 * @author Armin Reichert
 */
public class ChangeGridResolution extends AbstractAction {

	public ChangeGridResolution() {
		super("Change Resolution");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<?> combo = (JComboBox<?>) e.getSource();
		int cellSize = app().getModel().getGridCellSizes()[combo.getSelectedIndex()];
		app().getModel().setGridCellSize(cellSize);
		app().getModel().setGridWidth(app().getDisplayMode().getWidth() / cellSize);
		app().getModel().setGridHeight(app().getDisplayMode().getHeight() / cellSize);
		app().resetDisplay();
	}
}
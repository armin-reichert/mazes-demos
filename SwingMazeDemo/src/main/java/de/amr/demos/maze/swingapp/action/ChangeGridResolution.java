package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.DISPLAY_MODE;
import static de.amr.demos.maze.swingapp.MazeDemoApp.app;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

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
		int cellSize = model().getGridCellSizes()[combo.getSelectedIndex()];
		model().setGridCellSize(cellSize);
		model().setGridWidth(DISPLAY_MODE.getWidth() / cellSize);
		model().setGridHeight(DISPLAY_MODE.getHeight() / cellSize);
		app().resetDisplay();
	}
}
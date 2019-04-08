package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.impl.Top4;

public class CreateEmptyGridAction extends AbstractAction {

	public CreateEmptyGridAction() {
		putValue(NAME, "Create Empty Grid");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas().setGrid(GridFactory.emptyObservableGrid(model().getGridWidth(), model().getGridHeight(),
				Top4.get(), TraversalState.COMPLETED, 0));
		canvas().clear();
		canvas().drawGrid();
	}
}
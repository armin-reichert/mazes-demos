package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.canvas;
import static de.amr.demos.maze.swingapp.MazeDemoApp.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.graph.core.api.TraversalState;
import de.amr.graph.grid.impl.GridFactory;
import de.amr.graph.grid.impl.Top4;

public class CreateFullGridAction extends AbstractAction {

	public CreateFullGridAction() {
		putValue(NAME, "Create Full Grid");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		canvas().setGrid(GridFactory.fullObservableGrid(model().getGridWidth(), model().getGridHeight(),
				Top4.get(), TraversalState.COMPLETED, 0));
	}
}
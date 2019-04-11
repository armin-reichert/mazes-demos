package de.amr.demos.maze.swingapp.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.MazeDemoApp;
import de.amr.graph.core.api.TraversalState;

public class CreateEmptyGridAction extends AbstractAction {

	public CreateEmptyGridAction() {
		putValue(NAME, "Create Empty Grid");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MazeDemoApp.model().createGrid(false, TraversalState.COMPLETED);
	}
}
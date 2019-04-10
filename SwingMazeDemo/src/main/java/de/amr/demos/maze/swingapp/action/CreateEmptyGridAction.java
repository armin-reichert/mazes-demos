package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.graph.core.api.TraversalState;

public class CreateEmptyGridAction extends AbstractAction {

	public CreateEmptyGridAction() {
		putValue(NAME, "Create Empty Grid");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app().setGrid(false, TraversalState.COMPLETED);
	}
}
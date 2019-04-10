package de.amr.demos.maze.swingapp.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.demos.maze.swingapp.MazeDemoApp;
import de.amr.graph.core.api.TraversalState;

public class CreateFullGridAction extends AbstractAction {

	public CreateFullGridAction() {
		putValue(NAME, "Create Full Grid");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MazeDemoApp.app().setGrid(true, TraversalState.COMPLETED);
	}
}
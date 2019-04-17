package de.amr.demos.maze.swingapp.ui.common;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class SwingGoodies {

	public static Icon icon(String path) {
		return new ImageIcon(SwingGoodies.class.getResource(path));
	}

	public static Action action(String name, ActionListener listener) {
		return action(name, null, listener);
	}

	public static Action action(String name, Icon icon, ActionListener listener) {
		return new AbstractAction(name, icon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}
		};
	}

	public static void setEnabled(boolean b, Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setEnabled(b));
	}

	public static void setEnabled(boolean b, Action... actions) {
		Arrays.stream(actions).forEach(action -> action.setEnabled(b));
	}

	public static void setWaitCursor(Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
	}

	public static void setNormalCursor(Component... components) {
		Arrays.stream(components).forEach(comp -> comp.setCursor(Cursor.getDefaultCursor()));
	}

	private SwingGoodies() {
	}

}

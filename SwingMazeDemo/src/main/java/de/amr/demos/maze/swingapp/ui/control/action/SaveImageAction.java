package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;

/**
 * Saves the current maze as an image file.
 * 
 * @author Armin Reichert
 */
public class SaveImageAction extends MazeDemoAction {

	public SaveImageAction(String name, ControlUI controlUI, GridUI gridUI) {
		super(name, controlUI, gridUI);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics", "png"));
		int status = fileChooser.showSaveDialog(controlUI.getWindow());
		if (status == JFileChooser.APPROVE_OPTION) {
			var file = fileChooser.getSelectedFile();
			var fileName = file.getName();
			if (!fileName.endsWith(".png")) {
				file = new File(file.getParentFile(), fileName + ".png");
			}
			try {
				ImageIO.write(gridUI.getView().getCanvas().getDrawingBuffer(), "png", file);
				controlUI.showMessage("PNG image saved as '%s'", file);
			} catch (IOException x) {
				controlUI.showMessage("Image could not be saved: '%s'", x.getMessage());
			}
		}
	}
}
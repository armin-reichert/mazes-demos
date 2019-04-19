package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.amr.demos.maze.swingapp.ui.control.ControlViewController;
import de.amr.demos.maze.swingapp.ui.grid.GridViewController;

public class SaveImage extends AbstractAction {

	private final ControlViewController controlViewController;
	private final GridViewController gridViewController;

	public SaveImage(String name, ControlViewController controlViewController,
			GridViewController gridViewController) {
		super(name);
		this.controlViewController = controlViewController;
		this.gridViewController = gridViewController;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics", "png"));
		if (fileChooser.showSaveDialog(controlViewController.getWindow()) == JFileChooser.APPROVE_OPTION) {
			File pngFile = fileChooser.getSelectedFile();
			String fileName = pngFile.getName();
			if (!fileName.endsWith(".png")) {
				pngFile = new File(pngFile.getParentFile(), fileName + ".png");
			}
			try {
				ImageIO.write(gridViewController.getView().getCanvas().getDrawingBuffer(), "png", pngFile);
				controlViewController.showMessage("Image saved as " + pngFile);
			} catch (IOException x) {
				controlViewController.showMessage("Image could not be saved: " + x.getMessage());
			}
		}
	}
}
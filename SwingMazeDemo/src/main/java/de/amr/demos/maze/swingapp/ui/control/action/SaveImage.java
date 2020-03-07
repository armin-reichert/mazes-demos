package de.amr.demos.maze.swingapp.ui.control.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.amr.demos.maze.swingapp.ui.control.ControlUI;
import de.amr.demos.maze.swingapp.ui.grid.GridUI;

public class SaveImage extends AbstractAction {

	private final ControlUI controlUI;
	private final GridUI gridUI;

	public SaveImage(String name, ControlUI controlUI, GridUI gridUI) {
		super(name);
		this.controlUI = controlUI;
		this.gridUI = gridUI;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics", "png"));
		if (fileChooser.showSaveDialog(controlUI.getWindow()) == JFileChooser.APPROVE_OPTION) {
			File pngFile = fileChooser.getSelectedFile();
			String fileName = pngFile.getName();
			if (!fileName.endsWith(".png")) {
				pngFile = new File(pngFile.getParentFile(), fileName + ".png");
			}
			try {
				ImageIO.write(gridUI.getView().getCanvas().getDrawingBuffer(), "png", pngFile);
				controlUI.showMessage("Image saved as '%s'", pngFile);
			} catch (IOException x) {
				controlUI.showMessage("Image could not be saved: '%s'", x.getMessage());
			}
		}
	}
}
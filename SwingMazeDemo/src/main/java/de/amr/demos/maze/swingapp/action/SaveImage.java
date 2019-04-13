package de.amr.demos.maze.swingapp.action;

import static de.amr.demos.maze.swingapp.MazeDemoApp.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SaveImage extends AbstractAction {

	public SaveImage() {
		putValue(NAME, "Save Image...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics", "png"));
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File pngFile = fileChooser.getSelectedFile();
			String fileName = pngFile.getName();
			if (!fileName.endsWith(".png")) {
				pngFile = new File(pngFile.getParentFile(), fileName + ".png");
			}
			try {
				ImageIO.write(app().getGridWindow().getGridView().getDrawingBuffer(), "png", pngFile);
				app().showMessage("Image saved as " + pngFile);
			} catch (IOException x) {
				app().showMessage("Image could not be saved: " + x.getMessage());
			}
		}
	}
}
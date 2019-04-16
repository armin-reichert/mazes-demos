package de.amr.demos.maze.swingapp.ui.common;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.IStringConverter;

/**
 * Converts command-line value to theme class name.
 * 
 * @author Armin Reichert
 */
public class ThemeConverter implements IStringConverter<String> {

	@Override
	public String convert(String value) {
		switch (value) {
		case "system":
			return UIManager.getSystemLookAndFeelClassName();
		case "crossplatform":
			return UIManager.getCrossPlatformLookAndFeelClassName();
		case "metal":
			return MetalLookAndFeel.class.getName();
		case "nimbus":
		default:
			return NimbusLookAndFeel.class.getName();
		}
	}
}

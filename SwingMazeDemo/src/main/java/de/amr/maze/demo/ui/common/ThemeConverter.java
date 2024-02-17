package de.amr.maze.demo.ui.common;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.converters.StringConverter;

/**
 * Converts command-line value to theme class name.
 * 
 * @author Armin Reichert
 */
public class ThemeConverter extends StringConverter {

	@Override
	public String convert(String value) {
		switch (value.toLowerCase()) {
		case "system":
			return UIManager.getSystemLookAndFeelClassName();
		case "cross":
			return UIManager.getCrossPlatformLookAndFeelClassName();
		case "metal":
			return MetalLookAndFeel.class.getName();
		case "nimbus":
			return NimbusLookAndFeel.class.getName();
		default:
			return super.convert(value);
		}
	}
}
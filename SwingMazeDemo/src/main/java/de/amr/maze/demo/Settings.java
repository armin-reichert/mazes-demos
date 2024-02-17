/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.maze.demo;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.beust.jcommander.Parameter;

import de.amr.maze.demo.ui.common.ThemeConverter;

/**
 * Maze demo app command-line settings.
 * 
 * @author Armin Reichert
 */
public class Settings {

	@Parameter(description = "Preview window width", names = { "-width" })
	public int width;

	@Parameter(description = "Preview window height", names = { "-height" })
	public int height;

	@Parameter(description = "Theme (class name or: 'system', 'cross', 'metal', 'nimbus')", names = { "-laf",
			"-theme" }, converter = ThemeConverter.class)
	public String theme = NimbusLookAndFeel.class.getName();

	public Settings(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
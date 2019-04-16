package de.amr.demos.maze.swingapp.ui.common;

import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class MenuBuilder {

	public static MenuBuilder newBuilder() {
		return new MenuBuilder();
	}

	public static void updateState(JMenu menu) {
		for (int i = 0; i < menu.getItemCount(); ++i) {
			if (menu.getItem(i) instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) menu.getItem(i);
				BooleanSupplier fnSelection = (BooleanSupplier) cb.getClientProperty("selection");
				cb.setSelected(fnSelection.getAsBoolean());
			}
		}
	}

	private JMenu menu;

	public class ButtonBuilder {

		private Action action;
		private String text;

		public ButtonBuilder action(Action action) {
			this.action = action;
			return this;
		}

		public ButtonBuilder text(String text) {
			this.text = text;
			return this;
		}

		public MenuBuilder build() {
			JMenuItem item = new JMenuItem(action);
			if (text != null) {
				item.setText(text);
			}
			menu.add(item);
			return MenuBuilder.this;
		}
	}

	public class CheckBoxBuilder {

		private Consumer<Boolean> onChecked;
		private BooleanSupplier selection;
		private String text;

		public CheckBoxBuilder onChecked(Consumer<Boolean> onChecked) {
			this.onChecked = onChecked;
			return this;
		}

		public CheckBoxBuilder selection(BooleanSupplier selection) {
			this.selection = selection;
			return this;
		}

		public CheckBoxBuilder text(String text) {
			this.text = text;
			return this;
		}

		public MenuBuilder build() {
			JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
			if (onChecked != null) {
				checkBox.addActionListener(actionEvent -> onChecked.accept(checkBox.isSelected()));
			}
			if (selection != null) {
				checkBox.putClientProperty("selection", selection);
			}
			if (text != null) {
				checkBox.setText(text);
			}
			menu.add(checkBox);
			return MenuBuilder.this;
		}
	}

	public class RadioButtonBuilder<T> {

		private ButtonGroup radio;
		private T selectionValue;
		private Consumer<T> onSelect;
		private Supplier<T> selection;
		private String text;

		public RadioButtonBuilder(T selectionValue) {
			this.selectionValue = selectionValue;
		}
		
		public RadioButtonBuilder<T> selection(Supplier<T> selection) {
			this.selection = selection;
			return this;
		}

		public RadioButtonBuilder<T> radio(ButtonGroup radio) {
			this.radio = radio;
			return this;
		}

		public RadioButtonBuilder<T> onSelect(Consumer<T> onSelect) {
			this.onSelect = onSelect;
			return this;
		}

		public RadioButtonBuilder<T> text(String text) {
			this.text = text;
			return this;
		}

		public MenuBuilder build() {
			Objects.requireNonNull(selectionValue);
			Objects.requireNonNull(onSelect);
			Objects.requireNonNull(radio);
			
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
			if (onSelect != null) {
				radioButton.addItemListener(e -> {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						onSelect.accept(selectionValue);
					}
				});
			}
			if (selection != null) {
				radioButton.setSelected(selection.get().equals(selectionValue));
			}
			if (text != null) {
				radioButton.setText(text);
			}
			radio.add(radioButton);
			menu.add(radioButton);
			return MenuBuilder.this;
		}
	}

	private MenuBuilder() {
		menu = new JMenu();
	}

	public MenuBuilder property(String key, Object value) {
		menu.putClientProperty(key, value);
		return this;
	}

	public MenuBuilder title(String text) {
		menu.setText(text);
		return this;
	}

	public MenuBuilder separator() {
		menu.addSeparator();
		return this;
	}

	public MenuBuilder caption(String text) {
		JMenuItem caption = new JMenuItem(text);
		caption.setEnabled(false);
		menu.add(caption);
		return this;
	}

	public MenuBuilder items(Stream<JMenuItem> items) {
		items.forEach(menu::add);
		return this;
	}

	public MenuBuilder menu(JMenu subMenu) {
		menu.add(subMenu);
		return this;
	}

	public ButtonBuilder button() {
		return new ButtonBuilder();
	}

	public CheckBoxBuilder checkBox() {
		return new CheckBoxBuilder();
	}

	public <T> RadioButtonBuilder<T> radioButton(T selectionValue) {
		return new RadioButtonBuilder<T>(selectionValue);
	}

	public JMenu build() {
		return menu;
	}
}
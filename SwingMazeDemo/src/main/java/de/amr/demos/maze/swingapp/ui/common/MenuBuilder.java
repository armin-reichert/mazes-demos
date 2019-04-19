package de.amr.demos.maze.swingapp.ui.common;

import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class MenuBuilder {

	public static MenuBuilder newBuilder() {
		return new MenuBuilder();
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<T> getProperty(JComponent comp, String key) {
		T value = (T) comp.getClientProperty(key);
		return Optional.ofNullable(value);
	}

	public static void updateMenuSelection(JMenu menu) {
		for (int i = 0; i < menu.getItemCount(); ++i) {
			JMenuItem item = menu.getItem(i);
			if (item instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) item;
				Optional<Supplier<Boolean>> selection = getProperty(checkBox, "selection");
				if (selection.isPresent()) {
					checkBox.setSelected(selection.get().get());
				}
			}
			else if (item instanceof JRadioButtonMenuItem) {
				JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) item;
				Optional<Supplier<?>> selection = getProperty(radioButton, "selection");
				Optional<?> selectionValue = getProperty(radioButton, "selectionValue");
				if (selection.isPresent() && selectionValue.isPresent()
						&& selection.get().get().equals(selectionValue.get())) {
					radioButton.setSelected(true);
				}
			}
		}
	}

	// Menu button builder

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
			Objects.requireNonNull(action);
			JMenuItem item = new JMenuItem(action);
			if (text != null) {
				item.setText(text);
			}
			menu.add(item);
			return MenuBuilder.this;
		}
	}

	// CheckBox builder

	public class CheckBoxBuilder {

		private Consumer<Boolean> onSelect;
		private Supplier<Boolean> selection;
		private String text;

		public CheckBoxBuilder onSelect(Consumer<Boolean> onSelect) {
			this.onSelect = onSelect;
			return this;
		}

		public CheckBoxBuilder selection(Supplier<Boolean> selection) {
			this.selection = selection;
			return this;
		}

		public CheckBoxBuilder text(String text) {
			this.text = text;
			return this;
		}

		public MenuBuilder build() {
			JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
			if (onSelect != null) {
				checkBox.addActionListener(ignore -> onSelect.accept(checkBox.isSelected()));
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

	// Radio button group builder

	public class RadioButtonGroupBuilder<T> {

		private final ButtonGroup radio;
		private Supplier<T> selection;
		private Consumer<T> onSelect;

		public class RadioButtonBuilder {

			private T selectionValue;
			private String text;

			public RadioButtonBuilder selectionValue(T selectionValue) {
				this.selectionValue = selectionValue;
				return this;
			}

			public RadioButtonBuilder text(String text) {
				this.text = text;
				return this;
			}

			public RadioButtonGroupBuilder<T> build() {
				Objects.requireNonNull(selectionValue, "selection value is required");
				Objects.requireNonNull(selection, "selection function is required");
				JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem();
				radioButton.putClientProperty("selectionValue", selectionValue);
				radioButton.putClientProperty("selection", selection);
				if (text != null) {
					radioButton.setText(text);
				}
				radioButton.addItemListener(e -> {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						onSelect.accept(selectionValue);
					}
				});
				radioButton.setSelected(selection.get().equals(selectionValue));
				radio.add(radioButton);
				menu.add(radioButton);
				return RadioButtonGroupBuilder.this;
			}
		}

		public RadioButtonGroupBuilder(Class<T> selectionType) {
			radio = new ButtonGroup();
		}

		public RadioButtonGroupBuilder<T> selection(Supplier<T> selection) {
			this.selection = selection;
			return this;
		}

		public RadioButtonGroupBuilder<T> onSelect(Consumer<T> onSelect) {
			this.onSelect = onSelect;
			return this;
		}

		public RadioButtonBuilder button() {
			return new RadioButtonBuilder();
		}

		public MenuBuilder build() {
			return MenuBuilder.this;
		}
	}

	// Menu builder

	private final JMenu menu;

	private MenuBuilder() {
		menu = new JMenu();
	}

	public JMenu build() {
		return menu;
	}

	public MenuBuilder property(String key, Object value) {
		menu.putClientProperty(key, value);
		return this;
	}

	public MenuBuilder title(String text) {
		menu.setText(text);
		return this;
	}

	public MenuBuilder action(Action action) {
		JMenuItem item = new JMenuItem(action);
		menu.add(item);
		return this;
	}

	public MenuBuilder caption(String text) {
		JMenuItem caption = new JMenuItem(text);
		caption.setEnabled(false);
		menu.add(caption);
		return this;
	}

	public MenuBuilder separator() {
		menu.addSeparator();
		return this;
	}

	public ButtonBuilder button() {
		return new ButtonBuilder();
	}

	public CheckBoxBuilder checkBox() {
		return new CheckBoxBuilder();
	}

	public <T> RadioButtonGroupBuilder<T> radioButtonGroup(Class<T> selectionType) {
		return new RadioButtonGroupBuilder<>(selectionType);
	}

	public MenuBuilder items(Stream<JMenuItem> items) {
		items.forEach(menu::add);
		return this;
	}

	public MenuBuilder items(JMenuItem... items) {
		return items(Arrays.stream(items));
	}

	public MenuBuilder menu(JMenu subMenu) {
		Objects.requireNonNull(subMenu);
		menu.add(subMenu);
		return this;
	}
}
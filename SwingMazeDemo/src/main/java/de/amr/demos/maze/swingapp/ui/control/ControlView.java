package de.amr.demos.maze.swingapp.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.amr.graph.grid.api.GridTopology;
import net.miginfocom.swing.MigLayout;
import javax.swing.DefaultComboBoxModel;

/**
 * View for setting parameters and running maze generator and path finder.
 * 
 * @author Armin Reichert
 */
public class ControlView extends JPanel {

	private JButton btnCreateMaze;
	private JComboBox<?> comboGridResolution;
	private JSlider sliderDelay;
	private JTextArea textArea;
	private JLabel lblPassageWidth;
	private JSlider sliderPassageWidth;
	private JButton btnCreateAllMazes;
	private JButton btnStop;
	private JLabel lblGeneratorName;
	private JButton btnSolve;
	private JLabel lblSolverName;
	private JLabel lblGenerator;
	private JLabel lblSolver;
	private JScrollPane scrollPane;
	private JPanel collapsibleArea;
	private JButton btnShowHideDetails;
	private JPanel fixedArea;
	private JLabel lblGridTopology;
	private JComboBox<GridTopology> comboGridTopology;
	private JLabel lblRenderingStyle;
	private JComboBox<String> comboRenderingStyle;

	public ControlView() {
		setPreferredSize(new Dimension(520, 520));
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));

		fixedArea = new JPanel();
		fixedArea.setOpaque(false);
		add(fixedArea, BorderLayout.NORTH);
		fixedArea.setLayout(new MigLayout("", "[][][][][grow][right]", "[28px][]"));

		btnCreateMaze = new JButton("Create");
		fixedArea.add(btnCreateMaze, "flowy,cell 0 0,alignx left,aligny top");

		btnSolve = new JButton("Solve");
		fixedArea.add(btnSolve, "cell 1 0,alignx left,aligny top");

		btnCreateAllMazes = new JButton("All Mazes");
		fixedArea.add(btnCreateAllMazes, "cell 2 0,alignx left,aligny top");

		btnStop = new JButton("Stop");
		fixedArea.add(btnStop, "cell 3 0,alignx left,aligny top");

		btnShowHideDetails = new JButton("Show/Hide Details");
		btnShowHideDetails.setIcon(null);
		fixedArea.add(btnShowHideDetails, "cell 5 0,aligny top");

		sliderDelay = new JSlider();
		sliderDelay.setPaintLabels(true);
		sliderDelay.setPaintTicks(true);
		fixedArea.add(sliderDelay, "flowx,cell 0 1 6 1,growx");
		sliderDelay.setToolTipText("Delay [milliseconds]");
		sliderDelay.setValue(0);
		sliderDelay.setMaximum(100);
		sliderDelay.setMinorTickSpacing(10);
		sliderDelay.setMajorTickSpacing(50);

		collapsibleArea = new JPanel();
		collapsibleArea.setOpaque(false);
		add(collapsibleArea, BorderLayout.CENTER);
		collapsibleArea.setLayout(new MigLayout("", "[100px:n][3px:n][grow,fill]", "[][][][][][][][]"));

		lblGenerator = new JLabel("Generator");
		collapsibleArea.add(lblGenerator, "cell 0 0,growx");

		lblGeneratorName = new JLabel("Generator Algorithm");
		lblGeneratorName.setBorder(new EmptyBorder(5, 0, 5, 0));
		lblGeneratorName.setForeground(Color.BLUE);
		lblGeneratorName.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblGeneratorName.setHorizontalAlignment(SwingConstants.LEFT);
		collapsibleArea.add(lblGeneratorName, "flowy,cell 2 0,alignx left");

		lblSolver = new JLabel("Solver");
		collapsibleArea.add(lblSolver, "flowx,cell 0 1,growx");

		lblSolverName = new JLabel("Solver Algorithm");
		collapsibleArea.add(lblSolverName, "cell 2 1");
		lblSolverName.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolverName.setForeground(Color.BLUE);
		lblSolverName.setFont(new Font("Arial Black", Font.PLAIN, 14));
		lblSolverName.setBorder(new EmptyBorder(5, 0, 5, 0));

		JLabel lblGridResolution = new JLabel("Grid Resolution");
		collapsibleArea.add(lblGridResolution, "cell 0 2,growx");

		comboGridResolution = new JComboBox<>();
		comboGridResolution.setMaximumRowCount(16);
		lblGridResolution.setLabelFor(comboGridResolution);
		collapsibleArea.add(comboGridResolution, "cell 2 2,growx");

		lblGridTopology = new JLabel("Grid Topology");
		collapsibleArea.add(lblGridTopology, "cell 0 3");

		comboGridTopology = new JComboBox<>();
		lblGridTopology.setLabelFor(comboGridTopology);
		collapsibleArea.add(comboGridTopology, "cell 2 3,growx");

		lblRenderingStyle = new JLabel("Rendering style");
		collapsibleArea.add(lblRenderingStyle, "cell 0 4");

		comboRenderingStyle = new JComboBox<>();
		lblRenderingStyle.setLabelFor(comboRenderingStyle);
		comboRenderingStyle.setModel(new DefaultComboBoxModel<>(new String[] { "Walls-Passages", "Pearls" }));
		collapsibleArea.add(comboRenderingStyle, "cell 2 4,growx");

		lblPassageWidth = new JLabel("Passage Width (%)");
		collapsibleArea.add(lblPassageWidth, "cell 0 5,growx,aligny top");

		sliderPassageWidth = new JSlider();
		lblPassageWidth.setLabelFor(sliderPassageWidth);
		sliderPassageWidth.setMinorTickSpacing(10);
		sliderPassageWidth.setMajorTickSpacing(50);
		sliderPassageWidth.setPaintLabels(true);
		sliderPassageWidth.setToolTipText("Passage Width (%)");
		sliderPassageWidth.setPaintTicks(true);
		collapsibleArea.add(sliderPassageWidth, "cell 2 5,growx");

		scrollPane = new JScrollPane();
		collapsibleArea.add(scrollPane, "cell 0 7 3 1,grow");

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		textArea.setTabSize(2);
		textArea.setLineWrap(true);
		textArea.setRows(100);

	}

	public JPanel getCollapsibleArea() {
		return collapsibleArea;
	}

	public JLabel getLblSolverName() {
		return lblSolverName;
	}

	public JLabel getLblGeneratorName() {
		return lblGeneratorName;
	}

	public JButton getBtnCreateMaze() {
		return btnCreateMaze;
	}

	@SuppressWarnings("unchecked")
	public JComboBox<String> getComboGridResolution() {
		return (JComboBox<String>) comboGridResolution;
	}

	public JSlider getSliderDelay() {
		return sliderDelay;
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	public JSlider getSliderPassageWidth() {
		return sliderPassageWidth;
	}

	public JButton getBtnCreateAllMazes() {
		return btnCreateAllMazes;
	}

	public JButton getBtnStop() {
		return btnStop;
	}

	public JButton getBtnSolve() {
		return btnSolve;
	}

	public JButton getBtnShowHideDetails() {
		return btnShowHideDetails;
	}

	public JPanel getFixedArea() {
		return fixedArea;
	}

	public JComboBox<GridTopology> getComboGridTopology() {
		return comboGridTopology;
	}

	public JComboBox<String> getComboRenderingStyle() {
		return comboRenderingStyle;
	}
}

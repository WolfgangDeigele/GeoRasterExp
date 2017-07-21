package org.georasterexp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// This class generates the dialog shown when the user is asked to select the export as single image or as tiles

@SuppressWarnings("serial")
class TileDialog extends JDialog implements ActionListener, PropertyChangeListener {

	int width, height;
	JOptionPane optionPane;
	JTextField widthTextField, heightTextField;
	JRadioButton radio1, radio2;
	double faktorWidth, faktorHeight;
	double unitFaktor;
	int[] subPx = new int[2];
	boolean showUnitUnknownWarning = false;
	boolean unitUnknown = false;
	final String[] labels = { "Breite (Meter): ", "Höhe (Meter): " };
	int subsetWidthPixel, subsetHeightPixel, selectedWidth, selectedHeight;

	public TileDialog(Frame frame, int width, int height, double factorWidth, double factorHeight, int subsetWidthPixel,
			int subsetHeightPixel, String unit, boolean tileYesNo) {
		super(frame, true);
		setResizable(false);
		setTitle("GeoRasterExp");
		setPreferredSize(new Dimension(350, 280));

		this.faktorWidth = factorWidth;
		this.faktorHeight = factorHeight;
		this.subsetWidthPixel = subsetWidthPixel;
		this.subsetHeightPixel = subsetHeightPixel;
		this.selectedWidth = width;
		this.selectedHeight = height;

		if (unit.toLowerCase().equalsIgnoreCase("m") || unit.toLowerCase().contains("meter")) {
			unitFaktor = 1;
		} else if (unit.toLowerCase().equalsIgnoreCase("f") || unit.toLowerCase().contains("feet") || unit.toLowerCase().contains("foot")) {
			unitFaktor = 3.28084;
		} else {
			showUnitUnknownWarning = true;
			unitUnknown = true;
			labels[0] = "Breite (Pixel): ";
			labels[1] = "Höhe (Pixel): ";
		}

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) screenSize.getWidth() / 2 - 150, (int) screenSize.getHeight() / 2 - 120);

		JLabel dialog = new JLabel("<html>Möchten Sie den Export als Einzelbild<br>oder gekachelt durchführen?</html>");
		ButtonGroup group = new ButtonGroup();
		radio1 = new JRadioButton("Einzelbild");
		radio2 = new JRadioButton("Gekachelt");
		group.add(radio1);
		group.add(radio2);

		widthTextField = new JTextField(7);
		widthTextField.setText("" + selectedWidth);
		heightTextField = new JTextField(7);
		heightTextField.setText("" + selectedHeight);

		if (tileYesNo) {
			if (showUnitUnknownWarning) {
				showUnitUnknownWarning = false;
				JOptionPane.showMessageDialog(null,
						"Die Einheit des Korodinatensystems wurde nicht erkannt.\nDie Kachelgröße bitte in Pixeln angeben.",
						"GeoRasterExp", JOptionPane.WARNING_MESSAGE);
			}
			radio2.setSelected(true);
		} else {
			radio1.setSelected(true);
			widthTextField.setEnabled(false);
			heightTextField.setEnabled(false);
		}

		JPanel borderPanelMain = new JPanel();
		borderPanelMain.setLayout(new BorderLayout());
		borderPanelMain.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JPanel borderPanellabel = new JPanel();
		borderPanellabel.setLayout(new BorderLayout());
		borderPanellabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		borderPanellabel.add(dialog, BorderLayout.CENTER);

		JPanel borderPanelSelection = new JPanel();
		borderPanelSelection.setLayout(new BorderLayout());
		borderPanelSelection.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

		JPanel borderPanelRadio1 = new JPanel();
		borderPanelRadio1.setLayout(new BorderLayout());
		borderPanelRadio1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		borderPanelRadio1.add(radio1, BorderLayout.CENTER);

		JPanel borderPanelRadio2AndFields = new JPanel();
		borderPanelRadio2AndFields.setLayout(new BorderLayout());
		borderPanelRadio2AndFields.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		JPanel borderPanelRadio2 = new JPanel();
		borderPanelRadio2.setLayout(new BorderLayout());
		borderPanelRadio2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		borderPanelRadio2.add(radio2);

		JComponent[] components = { widthTextField, heightTextField };

		JPanel borderPanelFields = new JPanel();
		borderPanelFields.setLayout(new BorderLayout());
		borderPanelFields.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
		borderPanelFields.add(getTwoColumnLayout(labels, components), BorderLayout.CENTER);

		borderPanelRadio2AndFields.add(borderPanelRadio2, BorderLayout.PAGE_START);
		borderPanelRadio2AndFields.add(borderPanelFields, BorderLayout.PAGE_END);

		borderPanelSelection.add(borderPanelRadio1, BorderLayout.PAGE_START);
		borderPanelSelection.add(borderPanelRadio2AndFields, BorderLayout.PAGE_END);

		borderPanelMain.add(borderPanellabel, BorderLayout.PAGE_START);
		borderPanelMain.add(borderPanelSelection, BorderLayout.PAGE_END);

		Object[] options = { "Weiter", "Abbrechen" };

		optionPane = new JOptionPane(borderPanelMain, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null,
				options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				Main.setExpCancel(true);
				dispose();
			}
		});

		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		pack();

		radio1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				widthTextField.setEnabled(false);
				heightTextField.setEnabled(false);
			}
		});

		radio2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (showUnitUnknownWarning) {
					showUnitUnknownWarning = false;
					JOptionPane.showMessageDialog(null,
							"Die Einheit des Korodinatensystems wurde nicht erkannt.\nDie Kachelgröße bitte in Pixeln angeben.",
							"GeoRasterExp", JOptionPane.WARNING_MESSAGE);
				}
				widthTextField.setEnabled(true);
				heightTextField.setEnabled(true);

			}
		});

		widthTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setLabel();
			}

			public void insertUpdate(DocumentEvent e) {
				setLabel();
			}

			public void removeUpdate(DocumentEvent e) {
				setLabel();
			}
		});

		heightTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setLabel();
			}

			public void insertUpdate(DocumentEvent e) {
				setLabel();
			}

			public void removeUpdate(DocumentEvent e) {
				setLabel();
			}
		});

		radio1.setText("Einzelbild (" + (int) subsetWidthPixel + " x " + (int) subsetHeightPixel + " Pixel)");
		int[] subPx = unit2pixel(width, height);
		radio2.setText("Gekachelt (" + subPx[0] + " x " + subPx[1] + " Pixel pro Kachel)");
	}

	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();
			if (value.equals("Weiter")) {

				try {

					selectedWidth = Integer.parseInt(widthTextField.getText());
					selectedHeight = Integer.parseInt(heightTextField.getText());

					subPx = unit2pixel(selectedWidth, selectedHeight);

					if (radio1.isSelected()) {
						width = subsetWidthPixel;
						height = subsetHeightPixel;
					} else {
						width = subPx[0];
						height = subPx[1];
					}
					if (width == 0 || height == 0) {
						Exception e2 = new Exception("2");
						throw e2;
					}
					if (width > 10000 || height > 10000) {
						Exception e2 = new Exception("1");
						throw e2;
					}

					// sends the selected values to main class
					if (radio2.isSelected()) {
						Main.setIfTileWidthHeight(width, height, selectedWidth, selectedHeight, true);
					} else {
						Main.setIfTileWidthHeight(width, height, selectedWidth, selectedHeight, false);
					}
					Main.setExpCancel(false);
					dispose();

				} catch (Exception e2) {

					if (e2.getMessage().equals("1")) {
						JOptionPane.showMessageDialog(null,
								"Maximale Größe für einzelnen Export: 10000 x 10000 Pixel.\nBitte Kachelung mit einer kleineren Kachelgröße verwenden.",
								"GeoRasterExp", JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Nur positive Zahlen 0-9 erlaubt.", "GeoRasterExp",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			} else if (value.equals("Abbrechen")) {
				Main.setExpCancel(true);
				dispose();
			}
		}

		// Reset the JOptionPane's value.
		// If you don't do this, then if the user
		// presses the same button next time, no
		// property change event will be fired.
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

	}

	public int[] unit2pixel(double width, double height) {
		int px[] = new int[2];

		if (unitUnknown) {
			px[0] = (int) width;
			px[1] = (int) height;
		} else {
			px[0] = (int) (width * unitFaktor * faktorWidth);
			px[1] = (int) (height * unitFaktor * faktorHeight);
		}
		return px;
	}

	public void setLabel() {
		if (widthTextField.getText().length() > 0 && heightTextField.getText().length() > 0) {
			try {
				subPx = unit2pixel(Double.parseDouble(widthTextField.getText()),
						Double.parseDouble(heightTextField.getText()));
				radio2.setText("Gekachelt (" + subPx[0] + " x " + subPx[1] + " Pixel pro Kachel)");
			} catch (Exception e2) {
				JOptionPane.showMessageDialog(null, "Nur positive Zahlen 0-9 erlaubt.", "GeoRasterExp",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {

	}

	/**
	 * http://stackoverflow.com/questions/16349347/java-gui-organizing-a-dialog-box-to-get-data-from-the-user
	 * Provides a JPanel with two columns (labels & fields) laid out using
	 * GroupLayout. The arrays must be of equal size.
	 *
	 * Typical fields would be single line textual/input components such as
	 * JTextField, JPasswordField, JFormattedTextField, JSpinner, JComboBox,
	 * JCheckBox.. & the multi-line components wrapped in a JScrollPane -
	 * JTextArea or (at a stretch) JList or JTable.
	 *
	 * @param labels
	 *            The first column contains labels.
	 * @param fields
	 *            The last column contains fields.
	 * @param addMnemonics
	 *            Add mnemonic by next available letter in label text.
	 * @return JComponent A JPanel with two columns of the components provided.
	 */
	public static JComponent getTwoColumnLayout(JLabel[] labels, JComponent[] fields) {
		if (labels.length != fields.length) {
			String s = labels.length + " labels supplied for " + fields.length + " fields!";
			throw new IllegalArgumentException(s);
		}
		JComponent panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		// Turn on automatically adding gaps between components
		layout.setAutoCreateGaps(true);
		// Create a sequential group for the horizontal axis.
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		GroupLayout.Group yLabelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		hGroup.addGroup(yLabelGroup);
		GroupLayout.Group yFieldGroup = layout.createParallelGroup();
		hGroup.addGroup(yFieldGroup);
		layout.setHorizontalGroup(hGroup);
		// Create a sequential group for the vertical axis.
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setVerticalGroup(vGroup);

		int p = GroupLayout.PREFERRED_SIZE;
		// add the components to the groups
		for (JLabel label : labels) {
			yLabelGroup.addComponent(label);
		}
		for (Component field : fields) {
			yFieldGroup.addComponent(field, p, p, p);
		}
		for (int ii = 0; ii < labels.length; ii++) {
			vGroup.addGroup(layout.createParallelGroup().addComponent(labels[ii]).addComponent(fields[ii], p, p, p));
		}
		return panel;
	}

	/**
	 * Provides a JPanel with two columns (labels & fields) laid out using
	 * GroupLayout. The arrays must be of equal size.
	 *
	 * @param labelStrings
	 *            Strings that will be used for labels.
	 * @param fields
	 *            The corresponding fields.
	 * @return JComponent A JPanel with two columns of the components provided.
	 */
	public static JComponent getTwoColumnLayout(String[] labelStrings, JComponent[] fields) {
		JLabel[] labels = new JLabel[labelStrings.length];
		for (int ii = 0; ii < labels.length; ii++) {
			labels[ii] = new JLabel(labelStrings[ii]);
		}
		return getTwoColumnLayout(labels, fields);
	}

}
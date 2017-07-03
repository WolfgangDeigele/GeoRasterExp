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
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
class KachelDialog extends JDialog implements ActionListener, PropertyChangeListener {

	JOptionPane optionPane;
	int width, height;
	String extension;

	JTextField widthTextField, heightTextField;
	@SuppressWarnings("rawtypes")
	JComboBox comboExtension;

	public KachelDialog(Frame frame, int width, int height) {
		super(frame, true);
		setResizable(false);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) screenSize.getWidth() / 2 - 150, (int) screenSize.getHeight() / 2 - 120);

		this.width = width;
		this.height = height;

		widthTextField = new JTextField(5);
		widthTextField.setText("" + width);
		heightTextField = new JTextField(5);
		heightTextField.setText("" + height);
		String[] formats = { "Tagged Image File Format (.tif)", "Portable Network Graphic (.png)", "JPEG (.jpg)",
				"Bitmap (.bmp)", "Graphics Interchange Format (.gif)" };
		comboExtension = new JComboBox(formats);

		JComponent[] components = { widthTextField, heightTextField };

		String[] labels = { "Breite der Kacheln in Pixeln:", "Höhe der Kacheln in Pixeln:" };

		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { "OK", "Abbrechen" };

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		JLabel label = new JLabel(
				"<html>Der ausgewählte Subset ist zu groß,<br>um ihn in einer Datei zu speichern.<br>Die Speicherung erfolgt über Bildkacheln.</html>");
		JPanel borderPanel2 = new JPanel();
		borderPanel2.setLayout(new BorderLayout());
		borderPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		borderPanel2.add(label, BorderLayout.PAGE_START);

		JPanel borderPanel3 = new JPanel();
		borderPanel3.setLayout(new BorderLayout());
		borderPanel3.setBorder(BorderFactory.createEmptyBorder(35, 0, 25, 0));
		JLabel label2 = new JLabel("Dateiformat für die Kacheln wählen:");
		label2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		borderPanel3.add(label2, BorderLayout.PAGE_START);
		borderPanel3.add(comboExtension, BorderLayout.PAGE_END);

		borderPanel.add(borderPanel2, BorderLayout.PAGE_START);
		borderPanel.add(getTwoColumnLayout(labels, components), BorderLayout.CENTER);
		borderPanel.add(borderPanel3, BorderLayout.PAGE_END);

		// Create the JOptionPane.
		optionPane = new JOptionPane(borderPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options,
				options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				sendToMain(false);
				dispose();
			}
		});

		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		pack();
	}

	/**
	 * This method reacts to state changes in the option pane.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();
		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();
			if (value.equals("OK")) {
				width = Integer.parseInt(widthTextField.getText());
				height = Integer.parseInt(heightTextField.getText());

				if (width > 5000 || height > 5000) {
					JOptionPane.showMessageDialog(null, "Maxmimale Größe für Kacheln: 5000x5000 Pixel", "GeoRasterExp",
							JOptionPane.ERROR_MESSAGE);
				} else {

					int ext = comboExtension.getSelectedIndex();
					if (ext == 0) {
						extension = "tif";
					}
					if (ext == 1) {
						extension = "png";
					}
					if (ext == 2) {
						extension = "jpg";
					}
					if (ext == 3) {
						extension = "bmp";
					}
					if (ext == 4) {
						extension = "gif";
					}
					sendToMain(true);
					dispose();
				}

			} else if (value.equals("Abbrechen")) {
				sendToMain(false);
				dispose();
			}
		}

		// Reset the JOptionPane's value.
		// If you don't do this, then if the user
		// presses the same button next time, no
		// property change event will be fired.
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

	}

	private void sendToMain(boolean jaNein) {
		Main.setExpPix(width, height, extension, jaNein);
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

	public void actionPerformed(ActionEvent arg0) {

	}

}
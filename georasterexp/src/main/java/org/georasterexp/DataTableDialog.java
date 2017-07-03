package org.georasterexp;

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

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
class DataTableDialog extends JDialog implements ActionListener, PropertyChangeListener {

	String branches, idT, rasterProperty;
	JTextField branchesField, idTField, rasterPropertyField;
	JOptionPane optionPane;

	public DataTableDialog(Frame frame) {
		super(frame, true);

		setTitle("GeoRasterExp");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) screenSize.getWidth() / 2 - 170, (int) screenSize.getHeight() / 2 - 120);

		String[] str = NetworkDialog.getTableData();
		branches = str[0];
		idT = str[1];
		rasterProperty = str[2];

		branchesField = new JTextField(20);
		branchesField.setText(branches);
		idTField = new JTextField(20);
		idTField.setText(idT);
		rasterPropertyField = new JTextField(20);
		rasterPropertyField.setText(rasterProperty);

		JComponent[] components = { branchesField, idTField, rasterPropertyField };

		String[] labels = { "Tabellenname: ", "Spaltenname ID: ", "Spaltenname SDO_GEORASTER: " };

		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { "OK", "Abbrechen" };

		// Create the JOptionPane.
		optionPane = new JOptionPane(getTwoColumnLayout(labels, components), JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_OPTION, null, options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
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
				branches = branchesField.getText();
				idT = idTField.getText();
				rasterProperty = rasterPropertyField.getText();
				// String[] values = { branches, rasterProperty, idT };
				NetworkDialog.setTableData(branches, idT, rasterProperty);
				dispose();
			} else if (value.equals("Abbrechen")) {
				dispose();
			}
		}

		// Reset the JOptionPane's value.
		// If you don't do this, then if the user
		// presses the same button next time, no
		// property change event will be fired.
		optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

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
		// TODO Auto-generated method stub

	}

}
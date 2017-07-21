package org.georasterexp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

// This class shows the login-window

@SuppressWarnings("serial")
class NetworkDialog extends JDialog implements ActionListener, PropertyChangeListener {

	String API, database, driver, IP, port, oracleServiceName, user, pass;
	static String branches, idT, rasterProperty;
	JTextField APIField, databaseField, driverField, IPField, portField, oracleServiceNameField, userField, passField;
	JButton bezButton;
	JOptionPane optionPane;
	boolean startUp;

	public NetworkDialog(Frame frame, String API, String database, String driver, String IP, String port,
			String oracleServiceName, String user, String pass, String branches2, String idT2, String rasterProperty2,
			boolean showPass, final boolean startUp) {
		super(frame, true);
		setResizable(false);
		this.startUp = startUp;
		branches = branches2;
		idT = idT2;
		rasterProperty = rasterProperty2;

		setTitle("GeoRasterExp");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) screenSize.getWidth() / 2 - 150, (int) screenSize.getHeight() / 2 - 150);

		APIField = new JTextField(5);
		APIField.setText(API);
		databaseField = new JTextField(5);
		databaseField.setText(database);
		driverField = new JTextField(5);
		driverField.setText(driver);
		IPField = new JTextField(12);
		IPField.setText(IP);
		portField = new JTextField(5);
		portField.setText(port);
		oracleServiceNameField = new JTextField(5);
		oracleServiceNameField.setText(oracleServiceName);
		userField = new JTextField(10);
		userField.setText(user);
		passField = new JPasswordField(10);

		bezButton = new JButton();
		URL icon = getClass().getResource("resources/bezIcon.png");
		bezButton.setIcon(new ImageIcon(icon));
		bezButton.setPreferredSize(new Dimension(30, 21));

		if (showPass) {
			passField.setText(pass);
		}

		JComponent[] components = { APIField, databaseField, driverField, IPField, portField, oracleServiceNameField,
				userField, passField, new JPanel(), bezButton, new JLabel() };

		String[] labels = { "API:", "Datenbank:", "Driver:", "IP-Adresse:", "Port:", "Oracle-Service-Name:",
				"Benutzername:", "Passwort:", "", "Tabellen-Bezeichnungen:", " " };

		// Create an array specifying the number of dialog buttons
		// and their text.
		Object[] options = { "OK", "Abbrechen", "Hilfe" };

		// Create the JOptionPane.
		optionPane = new JOptionPane(getTwoColumnLayout(labels, components), JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_OPTION, null, options, options[0]);

		// Make this dialog display it.
		setContentPane(optionPane);

		// Handle window closing correctly.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (startUp) {
					System.exit(0);
				} else {
					Main.setConnect(false);
					dispose();
				}
			}
		});

		bezButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new DataTableDialog(null).setVisible(true);
				;
			}
		});

		// Ensure the text field always gets the first focus.
		if (startUp && API != null) {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent ce) {
					passField.requestFocusInWindow();
				}
			});
		}
		// Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		pack();
	}

	public static void setTableData(String branches2, String idT2, String rasterProperty2) {
		branches = branches2;
		idT = idT2;
		rasterProperty = rasterProperty2;
	}

	public static String[] getTableData() {
		String[] str = { branches, idT, rasterProperty };
		return str;
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
				API = APIField.getText();
				database = databaseField.getText();
				driver = driverField.getText();
				IP = IPField.getText();
				port = portField.getText();
				oracleServiceName = oracleServiceNameField.getText();
				user = userField.getText();
				pass = passField.getText();
				
				// check if all fields are non-empty
				if (API.length() == 0 || database.length() == 0 || driver.length() == 0 || IP.length() == 0
						|| port.length() == 0 || oracleServiceName.length() == 0 || user.length() == 0) {
					JOptionPane.showMessageDialog(null, "Bitte alle Felder ausfüllen.", "GeoRasterExp",
							JOptionPane.ERROR_MESSAGE);
				} else {
					if (branches.length() == 0 || idT.length() == 0 || rasterProperty.length() == 0) {
						JOptionPane.showMessageDialog(null, "Bitte Tabellen-Bezeichnungen ausfüllen", "GeoRasterExp",
								JOptionPane.ERROR_MESSAGE);
					} else {
						String[] values = { API, database, driver, IP, port, oracleServiceName, user, pass, branches,
								idT, rasterProperty };
						
						// send connection values to the main class
						Main.setConValues(values);
						Main.setConnect(true);
						dispose();
					}
				}
			} else if (value.equals("Abbrechen")) {
				if (startUp) {
					System.exit(0);
				} else {
					Main.setConnect(false);
					dispose();
				}
			} else if (value.equals("Hilfe")) {
				String hilfeStr = "Alle Felder müssen für die Verbindung ausgefüllt werden.\n"
						+ "Beispiele dafür sind:\n\n" + "API: jdbc\n" + "Datenbank: oracle\n" + "Driver: thin\n"
						+ "IP-Adresse: 123.4.567.890 / localhost\n" + "Port: 1521\n" + "Oracle-Service-Name: orcl\n"
						+ "Benutzername: Mustermann\n" + "Passwort: 12345";
				JOptionPane.showMessageDialog(null, hilfeStr, "GeoRasterExp", JOptionPane.INFORMATION_MESSAGE);
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
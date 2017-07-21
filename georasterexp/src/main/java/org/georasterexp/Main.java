package org.georasterexp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.LogManager;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicArrowButton;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseSrsType;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.Position;
import org.citydb.api.gui.BoundingBoxPanel;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.controller.PluginConfigControllerImpl;
import org.citydb.config.language.Language;
import org.citydb.config.project.global.LanguageType;
import org.citydb.database.DatabaseControllerImpl;
import org.citydb.gui.ImpExpGui;
import org.citydb.io.IOControllerImpl;
import org.citydb.log.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.georaster.JGeoRaster;
import oracle.spatial.georaster.image.GeoRasterImage;
import oracle.sql.STRUCT;

@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public class Main {

	static JTextField idTextField, sridTextField, pyramidTextField;
	static JPanel panelInput, panelButton, panelDisplay, panelInfoAndProgress;
	static JLabel infoLabel;
	static JTextArea console;
	static JScrollPane scrollPaneConsole;
	static JButton buttonSort, buttonShowExtent, buttonExport, buttonNetworkSettings, buttonCancelExport;
	static JPanel errorPanel = new JPanel();
	static JComboBox comboIDSRID;
	static JProgressBar progressBar;
	static JCheckBox checkBoxIsRGBImage;
	static Config config;
	static ObjectRegistry registry;
	static BoundingBoxPanel bboxPanel;
	static PrintStream printStream;
	static String API, database, driver, IP, port, oracleServiceName, user, pass;
	static String branches, idTable, rasterProperty;
	static FileInputStream in;
	static Properties props = new Properties();
	static FileOutputStream out;
	static Connection con;
	static BoundingBox bbox;
	static int id, srid, pyramid = 0, idCombo;
	static double xMin, yMin, xMax, yMax, xMin2, yMin2, xMax2, yMax2;
	static RenderedImage image;
	static int width = 450;
	static int height = 500;
	static int posX = 200;
	static int posY = 100;
	static JFrame gui;
	static JGeoRaster jGeoRaster;
	static JGeometry geom;
	static List<String> availableIDSRID = new ArrayList<String>();
	static boolean sorted, startup = true, connect = true, showSorted = true, startUpCon = true;
	static RenderedImage img = null;
	static BufferedImage image2;
	static Thread worker;
	static Timer timer;
	static int tileWidth = 250, tileHeight = 250;
	static boolean exportAsTiles = false;
	static String tileExtension;
	static JRadioButton radio1, radio2, radio3;
	static boolean saveTifAsGeotiff = false;
	static double subsetWidthKoord;
	static double subsetHeightKoord;
	static double factorWidth;
	static double factorHeight;
	static int subsetStartRow, subsetStartColumn;
	static boolean createWorldFile;
	static String ext;
	static int maxPixel = 10000;
	static int progressCounter, totalTileCount;
	static long currentTime, currentTime2, elapsedTime;
	static long timeCounter;
	static boolean cancelExport;
	static List<String> tileMergeList = new ArrayList<String>();
	static String mergeReturn, mergeError;
	static BufferedReader stdInput1, stdError1, stdInput2, stdError2;
	static int xMinP, yMinP, xMaxP, yMaxP;
	static boolean expCancel = false;
	static boolean saveError = false;
	static int selectedWidth, selectedHeight;

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadNetworkSettings();
				loadDataBaseTableNames();
				new NetworkDialog(null, API, database, driver, IP, port, oracleServiceName, user, pass, branches,
						idTable, rasterProperty, false, true).setVisible(true);
				checkConnection();
				gui = new JFrame("GeoRasterExp");
				loadWindowSettings();
				setWindowSettings();
				loadBoundingBoxPreSets();
				loadKachelPreSets();
				createTopPanel();
				setBoundingBoxPreSets();
				createConsole();
				addListeners();
				log("Verbindung mit Datenbank testen", false);
				retrieveIDs(false);
				setComboBox();
				startup = false;
			}
		});
	}

	// creates all the input fields
	public static void createTopPanel() {
		panelInput = new JPanel();
		panelInput.setLayout(new BorderLayout());

		panelButton = new JPanel();
		panelButton.setLayout(new BorderLayout());

		JPanel buttonPanel2 = new JPanel();
		buttonPanel2.setLayout(new BorderLayout());

		panelDisplay = new JPanel();
		panelDisplay.setLayout(new BorderLayout());

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());

		createIDSRIDInputField(panelInput);
		createBoundingBoxPanel(panelInput);
		createButtons(panelButton);
		createExportPanel(panelDisplay);

		panelButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		buttonPanel2.add(panelButton, BorderLayout.PAGE_START);
		buttonPanel2.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.PAGE_END);

		borderPanel.add(panelInput, BorderLayout.PAGE_START);
		borderPanel.add(buttonPanel2, BorderLayout.CENTER);
		borderPanel.add(panelDisplay, BorderLayout.PAGE_END);

		gui.add(borderPanel, BorderLayout.PAGE_START);
	}

	// creates the buttons for export and network settings
	public static void createButtons(JPanel panel) {

		JPanel borderPanel3 = new JPanel();
		borderPanel3.setLayout(new BorderLayout());
		borderPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

		checkBoxIsRGBImage = new JCheckBox("Georaster hat mehr als 1 Band (z.B. RGB)");
		// ToolTipManager.sharedInstance().setDismissDelay(60000);
		checkBoxIsRGBImage.setToolTipText(
				"<html>Beim Export erhalten manche Grayscale-Georaster fälschlicherweise 3 Bänder.<br>Dies wird automatisch korrigiert wenn der Haken nicht gesetzt wird.</html>");
		borderPanel3.add(checkBoxIsRGBImage, BorderLayout.LINE_START);

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 0));

		buttonExport = new JButton("Export");
		borderPanel.add(buttonExport, BorderLayout.LINE_START);

		JPanel borderPanel2 = new JPanel();
		borderPanel2.setLayout(new BorderLayout());
		borderPanel2.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 10));

		buttonNetworkSettings = new JButton("Neue Verbindung");
		borderPanel2.add(buttonNetworkSettings, BorderLayout.CENTER);

		panel.add(borderPanel3, BorderLayout.PAGE_START);
		panel.add(borderPanel, BorderLayout.LINE_START);
		panel.add(borderPanel2, BorderLayout.LINE_END);
	}

	// creates the export panel
	public static void createExportPanel(JPanel panel) {
		panelInfoAndProgress = new JPanel();
		panelInfoAndProgress.setLayout(new BorderLayout());
		panelInfoAndProgress.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

		infoLabel = new JLabel("<html> <br> <br> <br></html>");

		borderPanel.add(infoLabel, BorderLayout.LINE_START);
		panelInfoAndProgress.add(borderPanel, BorderLayout.LINE_START);

		progressBar = new JProgressBar();
		panelInfoAndProgress.add(progressBar, BorderLayout.PAGE_START);
		progressBar.setVisible(false);

		JPanel borderPanel2 = new JPanel();
		borderPanel2.setLayout(new BorderLayout());
		borderPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		buttonCancelExport = new JButton("Abbrechen");
		buttonCancelExport.setEnabled(false);
		borderPanel2.add(buttonCancelExport, BorderLayout.LINE_END);

		panel.add(panelInfoAndProgress, BorderLayout.PAGE_START);
		panel.add(borderPanel2, BorderLayout.LINE_END);
	}

	// creates the console to show the output
	public static void createConsole() {
		LogManager.getLogManager().reset();
		console = new JTextArea();
		console.setEditable(false);
		console.setFont(new JLabel().getFont());
		scrollPaneConsole = new JScrollPane(console);

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(BorderFactory.createEmptyBorder(10, 1, 0, 0));
		borderPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.PAGE_START);
		borderPanel.add(scrollPaneConsole, BorderLayout.CENTER);
		gui.add(borderPanel, BorderLayout.CENTER);
	}

	// creates the input field for ID
	public static void createIDSRIDInputField(JPanel panel) {
		JLabel IDSRIDLabel = new JLabel("ID auswählen");
		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
		borderPanel.add(IDSRIDLabel, BorderLayout.CENTER);

		comboIDSRID = new JComboBox();
		comboIDSRID.setMaximumRowCount(15);

		JPanel borderPanel2 = new JPanel();
		borderPanel2.setLayout(new BorderLayout());
		borderPanel2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		borderPanel2.add(borderPanel, BorderLayout.LINE_START);
		borderPanel2.add(comboIDSRID, BorderLayout.LINE_END);

		buttonSort = new BasicArrowButton(BasicArrowButton.SOUTH);
		buttonSort.setToolTipText("Nach IDs sortieren");
		JPanel borderPanel3 = new JPanel();
		borderPanel3.setLayout(new BorderLayout());
		borderPanel3.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
		borderPanel3.add(buttonSort, BorderLayout.CENTER);

		buttonShowExtent = new JButton("Bbx anpassen");
		buttonShowExtent.setToolTipText("Kopiert den Bereich, den das Georaster abdeckt, in die Bounding Box");
		JPanel borderPanel5 = new JPanel();
		borderPanel5.setLayout(new BorderLayout());
		borderPanel5.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
		borderPanel5.add(buttonShowExtent, BorderLayout.LINE_END);

		JPanel borderPanel4 = new JPanel();
		borderPanel4.setLayout(new BorderLayout());
		borderPanel4.add(borderPanel2, BorderLayout.LINE_START);
		borderPanel4.add(borderPanel3, BorderLayout.LINE_END);

		panel.add(borderPanel4, BorderLayout.LINE_START);
		panel.add(borderPanel5, BorderLayout.LINE_END);
	}

	// creates the bounding box from 3Dcitydb
	public static void createBoundingBoxPanel(JPanel panel) {
		config = new Config();

		// init internationalized labels
		LanguageType lang = config.getProject().getGlobal().getLanguage();
		if (lang == null)
			lang = LanguageType.fromValue(System.getProperty("user.language"));

		if (!Language.existsLanguagePack(new Locale(lang.value())))
			lang = LanguageType.EN;

		Language.I18N = ResourceBundle.getBundle("org.citydb.config.language.Label", new Locale(lang.value()));
		config.getProject().getGlobal().setLanguage(lang);

		// initialize object registry
		registry = ObjectRegistry.getInstance();

		// register log controller
		registry.setLogController(Logger.getInstance());

		// create and register application-wide event dispatcher
		EventDispatcher eventDispatcher = new EventDispatcher();
		registry.setEventDispatcher(eventDispatcher);

		// create and register plugin config controller
		PluginConfigControllerImpl pluginConfigController = new PluginConfigControllerImpl(config);
		registry.setPluginConfigController(pluginConfigController);

		// create and register database controller
		DatabaseControllerImpl databaseController = new DatabaseControllerImpl(config);
		registry.setDatabaseController(databaseController);

		// create and register i/o controller
		IOControllerImpl ioController = new IOControllerImpl(config);
		registry.setIOController(ioController);

		ImpExpGui guiImpExp = new ImpExpGui(config);
		registry.setViewController(guiImpExp);

		bboxPanel = guiImpExp.getComponentFactory().createBoundingBoxPanel();
		bboxPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

		bboxPanel.getSrsComboBox().addItem(new DatabaseSrs(4326, "urn:ogc:def:crs:EPSG::4326", "[Default] WGS 84", "",
				DatabaseSrsType.GEOGRAPHIC2D, true));
		bboxPanel.getSrsComboBox().setSelectedIndex(1);
		panel.add(bboxPanel, BorderLayout.PAGE_END);
	}

	// add listeners to various components
	public static void addListeners() {

		// this segment is run when the gui is getting closed
		gui.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				Dimension windowSize = gui.getSize();
				Point windowLocation = gui.getLocation();
				props = new Properties();

				// saves gui size and position
				try {
					in = new FileInputStream("GeoRasterExp.properties");
					props.load(in);
				} catch (Exception e) {
				}
				try {
					out = new FileOutputStream("GeoRasterExp.properties");
					props.setProperty("WindowSizeWidth", (int) windowSize.getWidth() + "");
					props.setProperty("WindowSizeHeight", (int) windowSize.getHeight() + "");
					props.setProperty("WindowPositionX", (int) windowLocation.getX() + "");
					props.setProperty("WindowPositionY", (int) windowLocation.getY() + "");
					props.store(out, null);
					out.close();
				} catch (Exception e) {
				}

				// saves login data
				try {
					in = new FileInputStream("GeoRasterExp.properties");
					props.load(in);
				} catch (Exception e) {
				}
				try {
					props.setProperty("API", API);
					props.setProperty("Database", database);
					props.setProperty("Driver", driver);
					props.setProperty("IP", IP);
					props.setProperty("Port", port);
					props.setProperty("OracleServiceName", oracleServiceName);
					props.setProperty("User", user);
					out = new FileOutputStream("GeoRasterExp.properties");
					props.store(out, null);
					out.close();
				} catch (Exception e) {
				}

				// saves the values of the bounding-box
				try {
					in = new FileInputStream("GeoRasterExp.properties");
					props.load(in);
				} catch (Exception e) {
				}
				try {
					checkExportSettings();
				} catch (Exception e) {
				}
				try {
					props.setProperty("lowerCornerX", "" + xMin);
					props.setProperty("lowerCornerY", "" + yMin);
					props.setProperty("upperCornerX", "" + xMax);
					props.setProperty("upperCornerY", "" + yMax);
					props.setProperty("idCombo", "" + id);
					out = new FileOutputStream("GeoRasterExp.properties");
					props.store(out, null);
					out.close();
				} catch (Exception e) {
				}

				// saves the database table names
				try {
					in = new FileInputStream("GeoRasterExp.properties");
					props.load(in);
				} catch (Exception e) {
				}
				try {
					out = new FileOutputStream("GeoRasterExp.properties");
					props.setProperty("branches", branches);
					props.setProperty("idTable", idTable);
					props.setProperty("rasterProperty", rasterProperty);
					props.store(out, null);
					out.close();
				} catch (Exception e) {
				}

				// saves the selected values for tiled export
				try {
					in = new FileInputStream("GeoRasterExp.properties");
					props.load(in);
				} catch (Exception e) {
				}
				try {
					out = new FileOutputStream("GeoRasterExp.properties");
					props.setProperty("tileWidth", "" + selectedWidth);
					props.setProperty("tileHeight", "" + selectedHeight);
					props.setProperty("tileYesNo", "" + exportAsTiles);
					props.store(out, null);
					out.close();
				} catch (Exception e) {
				}
				try {
					con.close();
				} catch (Exception e) {
				}
				gui.dispose();
				System.exit(0);
			}
		});

		// listener for sort IDs button
		buttonSort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!sorted) {

					// custom sorting algorithm
					Collections.sort(availableIDSRID, new Comparator<String>() {
						public int compare(String o1, String o2) {
							return extractInt(o1) - extractInt(o2);
						}

						int extractInt(String s) {
							String[] s2 = s.split(" ");
							return Integer.parseInt(s2[1]);
						}
					});

					DefaultComboBoxModel model = new DefaultComboBoxModel(availableIDSRID.toArray());
					comboIDSRID.setModel(model);
					sorted = true;
					buttonSort.setToolTipText("Nach Reihenfolge in Datenbank sortieren");
				} else {
					showSorted = false;
					checkConnection();
					retrieveIDs(true);
					DefaultComboBoxModel model = new DefaultComboBoxModel(availableIDSRID.toArray());
					comboIDSRID.setModel(model);
					sorted = false;
					buttonSort.setToolTipText("Nach IDs sortieren");
				}
			}
		});

		// listener for the button to copy the extent to the bounding box
		buttonShowExtent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					con = DriverManager.getConnection(
							API + ":" + database + ":" + driver + ":@" + IP + ":" + port + ":" + oracleServiceName,
							user, pass);
					checkExportSettings();
					PreparedStatement ps = con
							.prepareStatement("SELECT * FROM " + branches + " WHERE " + idTable + " = " + id);
					ResultSet rs = ps.executeQuery();
					JGeoRaster jGeor;
					rs.next();
					STRUCT struct = (STRUCT) rs.getObject(rasterProperty);
					jGeor = new JGeoRaster(struct);
					double[] ext0 = jGeor.getSpatialExtent().getOrdinatesArray();
					double[] ext = new double[4];
					if (ext0.length == 18) {
						ext[0] = ext0[0];
						ext[1] = ext0[5];
						ext[2] = ext0[8];
						ext[3] = ext0[1];
					}
					if (ext0.length == 4) {
						ext[0] = ext0[0];
						ext[1] = ext0[1];
						ext[2] = ext0[2];
						ext[3] = ext0[3];
					}

					int sourceSrid = srid;
					int targetSrid = 4326;

					StringBuilder query = new StringBuilder()
							.append("select SDO_CS.TRANSFORM(MDSYS.SDO_GEOMETRY(2003, ").append(sourceSrid)
							.append(", NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 1), ")
							.append("MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), ").append(targetSrid).append(") from dual");

					PreparedStatement psQuery;
					psQuery = con.prepareStatement(query.toString());
					psQuery.setDouble(1, ext[0]);
					psQuery.setDouble(2, ext[1]);
					psQuery.setDouble(3, ext[2]);
					psQuery.setDouble(4, ext[3]);

					ResultSet rs2 = psQuery.executeQuery();
					if (rs2.next()) {
						Struct struct2 = (Struct) rs2.getObject(1);
						if (!rs2.wasNull() && struct2 != null) {
							geom = JGeometry.loadJS(struct2);
							double[] ordinatesArray = geom.getOrdinatesArray();
							ext[0] = Math.round(ordinatesArray[0] * 100000000d) / 100000000d;
							ext[1] = Math.round(ordinatesArray[1] * 100000000d) / 100000000d;
							ext[2] = Math.round(ordinatesArray[2] * 100000000d) / 100000000d;
							ext[3] = Math.round(ordinatesArray[3] * 100000000d) / 100000000d;
						}
					}

					con.close();
					BoundingBox bbSet = new BoundingBox();
					bbSet.setLowerCorner(new Position(ext[0], ext[1]));
					bbSet.setUpperCorner(new Position(ext[2], ext[3]));
					bboxPanel.setBoundingBox(bbSet);
				} catch (Exception e) {
					if (e.getMessage().equals("5")) {
						JOptionPane.showMessageDialog(errorPanel, "Ordinate-Array kann nicht gelesen werden", "Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(errorPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		// listener for exporting the image
		buttonExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				createWorldFile = true;
				cancelExport = false;
				saveError = false;
				console.append("\n\n");
				log("Export aus Datenbank", false);
				try {
					log("ID, SRID und Bounding-Box lesen", true);
					checkExportSettings();
					log(true);

					log("Verbinde mit Datenbank als User: " + user, true);
					con = DriverManager.getConnection(
							API + ":" + database + ":" + driver + ":@" + IP + ":" + port + ":" + oracleServiceName,
							user, pass);
					log(true);

					jGeoRaster = null;
					PreparedStatement ps = null;
					ResultSet rs = null;
					log("SQL Abfrage ausfuehren", true);

					ps = con.prepareStatement(
							"SELECT " + rasterProperty + " FROM " + branches + " WHERE " + idTable + " = " + id);
					rs = ps.executeQuery();
					rs.next();
					log(true);

					log("JGeoRaster erstellen", true);
					STRUCT struct = (STRUCT) rs.getObject(1);
					jGeoRaster = new JGeoRaster(struct);
					log(true);

					log("Transformation von WGS 84 (BoundingBox) zu Raster-System", true);
					int sourceSrid = 4326;
					int targetSrid = srid;

					StringBuilder query = new StringBuilder()
							.append("select SDO_CS.TRANSFORM(MDSYS.SDO_GEOMETRY(2003, ").append(sourceSrid)
							.append(", NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 1), ")
							.append("MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), ").append(targetSrid).append(") from dual");

					PreparedStatement psQuery;
					psQuery = con.prepareStatement(query.toString());
					psQuery.setDouble(1, xMin);
					psQuery.setDouble(2, yMin);
					psQuery.setDouble(3, xMax);
					psQuery.setDouble(4, yMax);

					ResultSet rs2 = psQuery.executeQuery();
					if (rs2.next()) {
						Struct struct2 = (Struct) rs2.getObject(1);
						if (!rs2.wasNull() && struct2 != null) {
							geom = JGeometry.loadJS(struct2);
							double[] ordinatesArray = geom.getOrdinatesArray();
							xMin2 = ordinatesArray[0];
							yMin2 = ordinatesArray[1];
							xMax2 = ordinatesArray[2];
							yMax2 = ordinatesArray[3];
						}
					}
					log(true);

					// check if the subset is entirely inside the georaster
					// which is present in the database
					log("Subset Position überprüfen", true);
					double[] ordinates = jGeoRaster.getSpatialExtent().getOrdinatesArray();
					if ((ordinates.length == 18 && (xMin2 < ordinates[0] || yMin2 < ordinates[5] || xMax2 > ordinates[8]
							|| yMax2 > ordinates[1]))
							|| (ordinates.length == 4 && (xMin2 < ordinates[0] || yMin2 < ordinates[1]
									|| xMax2 > ordinates[2] || yMax2 > ordinates[3]))) {
						int value = JOptionPane.showOptionDialog(null,
								"Der zu exportierender Bereich liegt nicht vollständig im vorhandenen Georaster.\nDas exportierte Bild wird bei der Georeferenzierung verzerrt werden.\nMöchten Sie die Bounding-Box automatisch auf die Größe\ndes Georasters zuschneiden?\n ",
								"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
								new Object[] { "Zuschneiden", "Ignorieren", "Abbrechen" }, "Zuschneiden");
						if (value == 0) {
							if (ordinates.length == 18) {
								if (xMin2 < ordinates[0]) {
									xMin2 = ordinates[0];
								}
								if (yMin2 < ordinates[5]) {
									yMin2 = ordinates[5];
								}
								if (xMax2 > ordinates[8]) {
									xMax2 = ordinates[8];
								}
								if (yMax2 > ordinates[1]) {
									yMax2 = ordinates[1];
								}
							}
							if (ordinates.length == 4) {
								if (xMin2 < ordinates[0]) {
									xMin2 = ordinates[0];
								}
								if (yMin2 < ordinates[1]) {
									yMin2 = ordinates[1];
								}
								if (xMax2 > ordinates[2]) {
									xMax2 = ordinates[2];
								}
								if (yMax2 > ordinates[3]) {
									yMax2 = ordinates[3];
								}
							}
							console.append(" SUBSET ANGEPASST");
						}
						if (value == 1) {
							console.append(" WARNUNG IGNORIERT");
						}
						if (value == 2) {
							console.append(" ABBRUCH");
							return;
						}
					} else {
						log(true);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(console, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				long rasterWidthPixel = jGeoRaster.getMetadataObject().getRasterInfo().getDimensionSize(1);
				long rasterHeightPixel = jGeoRaster.getMetadataObject().getRasterInfo().getDimensionSize(0);

				final double[] ordinates = jGeoRaster.getSpatialExtent().getOrdinatesArray();
				double rasterWidthKoord = 0;
				double rasterHeightKoord = 0;
				if (ordinates.length == 18) {
					rasterWidthKoord = Math.abs(ordinates[8] - ordinates[0]);
					rasterHeightKoord = Math.abs(ordinates[5] - ordinates[1]);
				}
				if (ordinates.length == 4) {
					rasterWidthKoord = Math.abs(ordinates[2] - ordinates[0]);
					rasterHeightKoord = Math.abs(ordinates[3] - ordinates[1]);
				}

				CoordinateReferenceSystem crs = null;
				try {
					crs = CRS.decode("EPSG:" + srid, true);
				} catch (Exception e) {

				}
				String unit = crs.getCoordinateSystem().getAxis(0).getUnit().toString();

				subsetWidthKoord = xMax2 - xMin2;
				subsetHeightKoord = yMax2 - yMin2;

				factorWidth = rasterWidthPixel / rasterWidthKoord;
				factorHeight = rasterHeightPixel / rasterHeightKoord;

				int subsetWidthPixel = ((int) Math.round(subsetWidthKoord * factorWidth));
				int subsetHeightPixel = ((int) Math.round(subsetHeightKoord * factorHeight));

				subsetStartRow = (int) Math.round((ordinates[1] - yMax2) * factorHeight);
				subsetStartColumn = (int) Math.round((xMin2 - ordinates[0]) * factorWidth);

				final int subsetEndRow = subsetStartRow + subsetWidthPixel;
				final int subsetEndColumn = subsetStartColumn + subsetWidthPixel;

				xMinP = subsetStartRow;
				yMinP = subsetStartColumn;
				xMaxP = subsetEndRow;
				yMaxP = subsetEndColumn;

				if (unit.equalsIgnoreCase("m") || unit.contains("meter")) {
					log("Einheit des Georasters erkannt: Meter", true);
				} else if (unit.toLowerCase().equalsIgnoreCase("f") || unit.contains("feet") || unit.contains("foot")) {
					log("Einheit des Georasters erkannt: Feet", true);
				} else {
					log("Einheit des Georasters nicht erkannt", true);
				}

				// shows the dialog to select an export as single image or tiled
				new TileDialog(null, selectedWidth, selectedHeight, factorWidth, factorHeight, subsetWidthPixel,
						subsetHeightPixel, unit, exportAsTiles).setVisible(true);

				if (expCancel) {
					console.append(" ABBRUCH");
					console.setCaretPosition(console.getDocument().getLength());
					return;
				}
				
				// this segment is run if the export is tiled
				if (exportAsTiles) {
					console.append(" KACHELUNG");
					if (tileWidth > subsetWidthPixel) {
						tileWidth = subsetWidthPixel;
					}
					if (tileHeight > subsetHeightPixel) {
						tileHeight = subsetHeightPixel;
					}

					JFileChooser chooser = new JFileChooser(
							javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());

					FileNameExtensionFilter tifFilter = new FileNameExtensionFilter("Tagged Image File Format (.tif)",
							"tif");
					FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("Portable Network Graphic (.png)",
							"png");
					FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG (.jpg)", "jpg");
					FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("Bitmap (.bmp)", "bmp");
					FileNameExtensionFilter gifFilter = new FileNameExtensionFilter(
							"Graphics Interchange Format (.gif)", "gif");
					chooser.addChoosableFileFilter(tifFilter);
					chooser.addChoosableFileFilter(pngFilter);
					chooser.addChoosableFileFilter(jpgFilter);
					chooser.addChoosableFileFilter(bmpFilter);
					chooser.addChoosableFileFilter(gifFilter);
					chooser.setFileFilter(tifFilter);

					chooser.setDialogTitle("Ordner wählen");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					final String dir;
					if (chooser.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {

						FileFilter choosedFilter = chooser.getFileFilter();
						ext = ((FileNameExtensionFilter) choosedFilter).getExtensions()[0];

						if (ext.equalsIgnoreCase("tif")) {
							int option = showTiffSpeichernInfo();
							if (!(option == JOptionPane.OK_OPTION)) {
								return;
							}
							if (radio1.isSelected()) {
								createWorldFile = false;
							}
							if (radio1.isSelected() || radio3.isSelected()) {
								saveTifAsGeotiff = true;
							}
						}

						dir = chooser.getSelectedFile().toString();

						final int kachelZahlWidth = (int) Math.floor(subsetWidthPixel / tileWidth);
						final int kachelZahlHeight = (int) Math.floor(subsetHeightPixel / tileHeight);
						final int tileRestWidthPixel = (int) subsetWidthPixel - (kachelZahlWidth * tileWidth);
						final int tileRestHeightPixel = (int) subsetHeightPixel - (kachelZahlHeight * tileHeight);

						totalTileCount = kachelZahlWidth * kachelZahlHeight;
						if (tileRestWidthPixel > 0) {
							totalTileCount = totalTileCount + kachelZahlHeight;
						}
						if (tileRestHeightPixel > 0) {
							totalTileCount = totalTileCount + kachelZahlWidth;
						}
						if (tileRestWidthPixel > 0 && tileRestHeightPixel > 0) {
							totalTileCount++;
						}
						progressCounter = 0;
						timeCounter = 0;
						currentTime = System.currentTimeMillis();

						infoLabel.setText("<html>Anzahl an Kacheln: " + totalTileCount + "<br> <br> </html>");

						// timer for showing the time while exporting
						int interval = 1000;
						timer = new Timer(interval, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								timeCounter++;
								long remainingTime = (int) (((double) timeCounter / (double) progressCounter)
										* (double) (totalTileCount - progressCounter));
								infoLabel.setText("<html>Anzahl an Kacheln: " + totalTileCount + " ("
										+ (totalTileCount - progressCounter) + " verbleibend)<br>Verstrichene Zeit: "
										+ s2t(timeCounter) + "<br>Verbleibende Zeit: " + s2t(remainingTime)
										+ "</html>");
							}
						});

						buttonCancelExport.setEnabled(true);
						worker = new Thread() {
							public void run() {
								try {
									log(totalTileCount + " Kacheln exportieren", true);
									console.setCaretPosition(console.getDocument().getLength());
									progressBar.setVisible(true);

									int aktRow = subsetStartRow;
									int aktColumn = subsetStartColumn;

									con = DriverManager.getConnection(API + ":" + database + ":" + driver + ":@" + IP
											+ ":" + port + ":" + oracleServiceName, user, pass);

									PreparedStatement ps = con.prepareStatement("SELECT " + rasterProperty + " FROM "
											+ branches + " WHERE " + idTable + " = " + id);
									ResultSet rs = ps.executeQuery();
									rs.next();

									STRUCT struct = (STRUCT) rs.getObject(1);
									JGeoRaster jGeoRaster2 = new JGeoRaster(struct);

									GeoRasterImage geoRasterImage = jGeoRaster2.getGeoRasterImageObject();
									long[] outWindow = new long[4];

									// iterates over all tiles
									for (int i = 0; i <= kachelZahlHeight; i++) {
										for (int j = 0; j <= kachelZahlWidth; j++) {
											if (i < kachelZahlHeight) {
												
												// all tiles at the inside
												if (j < kachelZahlWidth) {
													img = geoRasterImage.getRasterImage(pyramid, aktRow, aktColumn,
															aktRow + tileHeight - 1, aktColumn + tileWidth - 1,
															outWindow);
													tileMergeList.add(i + "x" + j + ".tif");

													double[] koord = { ordinates[0] + (aktColumn / factorWidth),
															ordinates[1] - (aktRow + tileHeight) / factorHeight,
															ordinates[0] + (aktColumn + tileWidth) / factorWidth,
															ordinates[1] - aktRow / factorHeight };

													saveImage(new File(dir + "\\" + i + "x" + j), img, ext,
															createWorldFile, koord);

													progressCounter++;
													progressBar.setValue((int) ((double) progressCounter
															/ (double) totalTileCount * 100));
													if (cancelExport) {
														con.close();
														ps.close();
														rs.close();
														progressBar.setVisible(false);
														infoLabel.setText("<html> <br> <br> <br></html>");
														buttonCancelExport.setEnabled(false);
														return;
													}
												}

												// all tiles at the right border
												else {
													if (kachelZahlWidth > 0) {
														img = geoRasterImage.getRasterImage(pyramid, aktRow, aktColumn,
																aktRow + tileHeight - 1,
																aktColumn + tileRestWidthPixel - 1, outWindow);
														tileMergeList.add(i + "x" + j + ".tif");

														double[] koord = { ordinates[0] + (aktColumn / factorWidth),
																ordinates[1] - (aktRow + tileHeight) / factorHeight,
																ordinates[0] + (aktColumn + tileRestWidthPixel)
																		/ factorWidth,
																ordinates[1] - aktRow / factorHeight };

														saveImage(new File(dir + "\\" + i + "x" + j), img, ext,
																createWorldFile, koord);

														progressCounter++;
														progressBar.setValue((int) ((double) progressCounter
																/ (double) totalTileCount * 100));
														if (cancelExport) {
															con.close();
															ps.close();
															rs.close();
															progressBar.setVisible(false);
															infoLabel.setText("<html> <br> <br> <br></html>");
															buttonCancelExport.setEnabled(false);
															return;
														}
													}
												}
											}

											// all tiles at the bottom border
											else {
												if (j < kachelZahlWidth) {
													if (kachelZahlHeight > 0) {
														img = geoRasterImage.getRasterImage(pyramid, aktRow, aktColumn,
																aktRow + tileRestHeightPixel - 1,
																aktColumn + tileWidth - 1, outWindow);
														tileMergeList.add(i + "x" + j + ".tif");

														double[] koord = { ordinates[0] + (aktColumn / factorWidth),
																ordinates[1]
																		- (aktRow + tileRestHeightPixel) / factorHeight,
																ordinates[0] + (aktColumn + tileWidth) / factorWidth,
																ordinates[1] - aktRow / factorHeight };

														saveImage(new File(dir + "\\" + i + "x" + j), img, ext,
																createWorldFile, koord);
														progressCounter++;
														progressBar.setValue((int) ((double) progressCounter
																/ (double) totalTileCount * 100));
														if (cancelExport) {
															con.close();
															ps.close();
															rs.close();
															progressBar.setVisible(false);
															infoLabel.setText("<html> <br> <br> <br></html>");
															buttonCancelExport.setEnabled(false);
															return;
														}
													}
												}

												// last tile at the right bottom corner
												else {
													if (kachelZahlWidth > 0 && kachelZahlHeight > 0) {
														img = geoRasterImage.getRasterImage(pyramid, aktRow, aktColumn,
																aktRow + tileRestHeightPixel - 1,
																aktColumn + tileRestWidthPixel - 1, outWindow);
														tileMergeList.add(i + "x" + j + ".tif");

														double[] koord = { ordinates[0] + (aktColumn / factorWidth),
																ordinates[1]
																		- (aktRow + tileRestHeightPixel) / factorHeight,
																ordinates[0] + (aktColumn + tileRestWidthPixel)
																		/ factorWidth,
																ordinates[1] - aktRow / factorHeight };

														saveImage(new File(dir + "\\" + i + "x" + j), img, ext,
																createWorldFile, koord);
														progressCounter++;
														progressBar.setValue((int) ((double) progressCounter
																/ (double) totalTileCount * 100));
														if (cancelExport) {
															con.close();
															ps.close();
															rs.close();
															progressBar.setVisible(false);
															infoLabel.setText("<html> <br> <br> <br></html>");
															buttonCancelExport.setEnabled(false);
															return;
														}
													}
												}
											}

											aktColumn = aktColumn + tileWidth;
										}
										aktColumn = subsetStartColumn;
										aktRow = aktRow + tileHeight;
									}
									log(true);
									console.append("\n       -- Kacheln gespeichert unter:\n           " + dir);
									console.setCaretPosition(console.getDocument().getLength());
									progressBar.setVisible(false);
									timer.stop();
									buttonCancelExport.setEnabled(false);
									infoLabel.setText(
											"<html>Anzahl an Kacheln: " + totalTileCount + "<br>Verstrichene Zeit: "
													+ s2t(timeCounter) + "<br>Verbleibende Zeit: 0s</html>");

								} catch (Exception e) {
									e.printStackTrace();
									log(false);
									progressBar.setVisible(false);
									timer.stop();
									buttonCancelExport.setEnabled(false);
									JOptionPane.showMessageDialog(errorPanel,
											"Bei der Kachelung ist ein Fehler aufgetreten.", "Error",
											JOptionPane.INFORMATION_MESSAGE);
								}
								if (!saveError) {
									
									// shows the dialog weather to combine the tiles or not
									int option = JOptionPane.showConfirmDialog(null,
											"Georaster in Kacheln exportiert.\nMöchten Sie die Kacheln in einer Datei zusammenführen?\nFür diesen Vorgang müssen GDAL installiert\nund die Systemvariablen entsprechend gesetzt sein.",
											"GeoRasterExp", JOptionPane.YES_NO_OPTION);
									if (option == JOptionPane.YES_OPTION) {
										try {
											log("tiles.txt mit Liste aller Kacheln erstellen", true);
											FileWriter writer = new FileWriter(dir + "\\tiles.txt");
											for (String str : tileMergeList) {
												writer.write(dir + "\\" + str);
												writer.write(System.lineSeparator());
											}
											writer.close();
											log(true);

											Thread.sleep(1000);

											log("tiles.vrt mit GDAL erstellen", true);
											
											// use command-line and gdalbuildvrt
											String cmd = "gdalbuildvrt -input_file_list " + dir
													+ "\\tiles.txt -overwrite " + dir + "\\tiles.vrt";
											System.out.println(cmd);

											Process process = Runtime.getRuntime().exec(cmd);
											stdInput1 = new BufferedReader(
													new InputStreamReader(process.getInputStream()));

											stdError1 = new BufferedReader(
													new InputStreamReader(process.getErrorStream()));

											String sI = null;
											while ((sI = stdInput1.readLine()) != null) {
												System.out.println(sI);
												if (sI.contains(
														"0...10...20...30...40...50...60...70...80...90...100 - done.")) {
													log(true);
													log("Kacheln anhand von tiles.vrt zusammenführen", true);
													String outFormat = null;
													if (ext.equalsIgnoreCase("tif")) {
														outFormat = "GTiff";
													}
													if (ext.equalsIgnoreCase("png")) {
														outFormat = "PNG";
													}
													if (ext.equalsIgnoreCase("jpg")) {
														outFormat = "JPEG";
													}
													if (ext.equalsIgnoreCase("bmp")) {
														outFormat = "BMP";
													}
													if (ext.equalsIgnoreCase("gif")) {
														outFormat = "GIF";
													}
													
													// use command-line and gdalwarp
													cmd = "gdalwarp -overwrite -of " + outFormat + " " + dir
															+ "\\tiles.vrt " + dir + "\\output." + ext;
													System.out.println(cmd);

													process = Runtime.getRuntime().exec(cmd);
													stdInput2 = new BufferedReader(
															new InputStreamReader(process.getInputStream()));

													stdError2 = new BufferedReader(
															new InputStreamReader(process.getErrorStream()));

													String sI2 = null;
													while ((sI2 = stdInput2.readLine()) != null) {
														System.out.println(sI2);
														if (sI2.contains(
																"0...10...20...30...40...50...60...70...80...90...100 - done.")) {
															log(true);
															console.append(
																	"\n       -- Kacheln zusammengeführt unter:\n           "
																			+ dir + "\\output.tif");
															console.setCaretPosition(console.getDocument().getLength());
															JOptionPane.showMessageDialog(errorPanel,
																	"Georaster exportiert und Kacheln zusammengeführt.",
																	"GeoRasterExp", JOptionPane.INFORMATION_MESSAGE);
														}
													}
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
											JOptionPane.showMessageDialog(errorPanel,
													"GDAL ist nicht installiert oder kann nicht gefunden werden.\nStellen Sie sicher, dass die Systemvariablen richtig gesetzt sind\nund testen Sie in der Windows-Commandline\ndie Befehle 'gdalbuildvrt' und 'gdalwarp'",
													"GeoRasterExp", JOptionPane.ERROR_MESSAGE);
										}
									}
								} else {
									return;
								}

							}
						};

						timer.start();
						worker.start();

					} else {
						return;
					}
				} else {
					
					// this segment is run if the export is not tiled
					console.append(" EINZELBILD");
					final JFileChooser fileChooser = new JFileChooser(
							javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory());

					FileNameExtensionFilter tifFilter = new FileNameExtensionFilter("Tagged Image File Format (.tif)",
							"tif");
					FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("Portable Network Graphic (.png)",
							"png");
					FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG (.jpg)", "jpg");
					FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("Bitmap (.bmp)", "bmp");
					FileNameExtensionFilter gifFilter = new FileNameExtensionFilter(
							"Graphics Interchange Format (.gif)", "gif");
					fileChooser.addChoosableFileFilter(tifFilter);
					fileChooser.addChoosableFileFilter(pngFilter);
					fileChooser.addChoosableFileFilter(jpgFilter);
					fileChooser.addChoosableFileFilter(bmpFilter);
					fileChooser.addChoosableFileFilter(gifFilter);
					fileChooser.setFileFilter(tifFilter);
					String chooserTitle = "Export speichern unter";
					fileChooser.setSelectedFile(new File("Unbenannt"));

					fileChooser.setDialogTitle(chooserTitle);
					int returnVal = fileChooser.showOpenDialog(gui);
					if (returnVal == JFileChooser.APPROVE_OPTION) {

						FileFilter choosedFilter = fileChooser.getFileFilter();
						ext = ((FileNameExtensionFilter) choosedFilter).getExtensions()[0];
						if (ext.equalsIgnoreCase("tif")) {
							int option = showTiffSpeichernInfo();
							if (!(option == JOptionPane.OK_OPTION)) {
								return;
							}
							if (radio1.isSelected()) {
								createWorldFile = false;
							}
							if (radio1.isSelected() || radio3.isSelected()) {
								saveTifAsGeotiff = true;
							}
						}

						int interval = 1000;
						timer = new Timer(interval, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								timeCounter++;
								infoLabel.setText("Verstrichene Zeit: " + s2t(timeCounter));
							}
						});

						worker = new Thread() {
							public void run() {

								try {
									progressBar.setIndeterminate(true);
									progressBar.setVisible(true);

									log("Export", true);
									con = DriverManager.getConnection(API + ":" + database + ":" + driver + ":@" + IP
											+ ":" + port + ":" + oracleServiceName, user, pass);

									PreparedStatement ps = con.prepareStatement("SELECT " + rasterProperty + " FROM "
											+ branches + " WHERE " + idTable + " = " + id);
									ResultSet rs = ps.executeQuery();
									rs.next();

									STRUCT struct = (STRUCT) rs.getObject(1);
									JGeoRaster jGeoRaster2 = new JGeoRaster(struct);
									
									GeoRasterImage geoRasterImage = jGeoRaster2.getGeoRasterImageObject();
									
									long[] outWindow = new long[4];
									img = geoRasterImage.getRasterImage(pyramid, xMinP, yMinP, xMaxP, yMaxP, outWindow);
									
									double[] koord = { ordinates[0] + (subsetStartColumn / factorWidth),
											ordinates[1] - (subsetEndRow + 1) / factorHeight,
											ordinates[0] + (subsetEndColumn + 1) / factorWidth,
											ordinates[1] - subsetStartRow / factorHeight };

									log(true);

									saveImage(fileChooser.getSelectedFile(), img, ext, createWorldFile, koord);

									console.append("\n       -- Export gespeichert unter:\n           "
											+ fileChooser.getSelectedFile() + "." + ext);
									console.setCaretPosition(console.getDocument().getLength());
									progressBar.setIndeterminate(false);
									progressBar.setVisible(false);
									buttonCancelExport.setEnabled(false);
									timer.stop();
									infoLabel.setText("Verstrichene Zeit: " + s2t(timeCounter));
									if (!saveError) {
										JOptionPane.showMessageDialog(errorPanel, "Georaster exportiert!",
												"GeoRasterExp", JOptionPane.INFORMATION_MESSAGE);
									}
								} catch (Exception e) {
									log(false);
									log(e.getMessage(), false);
									e.printStackTrace();
									log(false);
									progressBar.setIndeterminate(false);
									progressBar.setVisible(false);
									buttonCancelExport.setEnabled(false);
									timer.stop();
									infoLabel.setText("Verstrichene Zeit: " + s2t(timeCounter));

								}
							}
						};

						timer.start();
						worker.start();

					} else {
						return;
					}
					// }
					try {
						con.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		// listener for network settings button
		buttonNetworkSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new NetworkDialog(null, API, database, driver, IP, port, oracleServiceName, user, pass, branches,
						idTable, rasterProperty, true, false).setVisible(true);
				console.append("\n\n");
				log("Neue Verbindung herstellen", false);
				checkConnection();
				retrieveIDs(false);
				DefaultComboBoxModel model = new DefaultComboBoxModel(availableIDSRID.toArray());
				comboIDSRID.setModel(model);
				sorted = false;
			}
		});

		// listener for cancel export button
		buttonCancelExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				timer.stop();
				cancelExport = true;
			}
		});
	}

	// save world-file
	public static void saveWorldFile(File file, RenderedImage image, double xMin, double yMin, double xMax,
			double yMax) {
		try {
			String[] str = file.getAbsolutePath().split("\\.");
			String path = str[0];
			String extension = str[1];
			if (extension.equals("tif")) {
				extension = "tfw";
			}
			if (extension.equals("png")) {
				extension = "pgw";
			}
			if (extension.equals("jpg")) {
				extension = "jgw";
			}
			if (extension.equals("bmp")) {
				extension = "bpw";
			}
			if (extension.equals("gif")) {
				extension = "gfw";
			}
			PrintWriter out = new PrintWriter(path + "." + extension);
			out.println((xMax - xMin) / image.getWidth());
			out.println(0);
			out.println(0);
			out.println(-(yMax - yMin) / image.getHeight());
			out.println(xMin);
			out.println(yMax);
			out.close();
			System.out.println("World File erstellt unter:\n      " + path + "." + extension);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(errorPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// load the network settings if possible from the properties file
	public static void loadNetworkSettings() {
		try {
			in = new FileInputStream("GeoRasterExp.properties");
			props.load(in);

			API = props.getProperty("API");
			database = props.getProperty("Database");
			driver = props.getProperty("Driver");
			IP = props.getProperty("IP");
			port = props.getProperty("Port");
			oracleServiceName = props.getProperty("OracleServiceName");
			user = props.getProperty("User");

			in.close();
		} catch (Exception e) {
		}
	}

	// load the window settings
	public static void loadWindowSettings() {
		try {
			in = new FileInputStream("GeoRasterExp.properties");
			props = new Properties();
			props.load(in);
			width = Integer.valueOf(props.getProperty("WindowSizeWidth"));
			height = Integer.valueOf(props.getProperty("WindowSizeHeight"));
			posX = Integer.valueOf(props.getProperty("WindowPositionX"));
			posY = Integer.valueOf(props.getProperty("WindowPositionY"));
			in.close();
		} catch (Exception e) {
		}
	}

	// load bounding box pre-sets
	public static void loadBoundingBoxPreSets() {
		try {
			in = new FileInputStream("GeoRasterExp.properties");
			props = new Properties();
			props.load(in);
			idCombo = Integer.parseInt(props.getProperty("idCombo"));
			xMin = Double.valueOf(props.getProperty("lowerCornerX"));
			yMin = Double.valueOf(props.getProperty("lowerCornerY"));
			xMax = Double.valueOf(props.getProperty("upperCornerX"));
			yMax = Double.valueOf(props.getProperty("upperCornerY"));
			in.close();
		} catch (Exception e) {
		}
	}

	// loads tile-size pre-sets
	public static void loadKachelPreSets() {
		try {
			in = new FileInputStream("GeoRasterExp.properties");
			props = new Properties();
			props.load(in);
			selectedWidth = Integer.parseInt(props.getProperty("tileWidth"));
			selectedHeight = Integer.parseInt(props.getProperty("tileHeight"));
			exportAsTiles = Boolean.parseBoolean(props.getProperty("tileYesNo"));
			in.close();
		} catch (Exception e) {
		}
	}

	// load database tabel names
	public static void loadDataBaseTableNames() {
		try {
			in = new FileInputStream("GeoRasterExp.properties");
			props = new Properties();
			props.load(in);
			branches = props.getProperty("branches", "GRID_COVERAGE");
			idTable = props.getProperty("idTable", "ID");
			rasterProperty = props.getProperty("rasterProperty", "RASTERPROPERTY");
			in.close();
		} catch (Exception e) {
		}
	}

	// set window settings
	public static void setWindowSettings() {
		gui.setLayout(new BorderLayout());
		URL icon = Main.class.getResource("resources/GeoRasterExp128.png");
		gui.setIconImage(new ImageIcon(icon).getImage());
		gui.setSize(width, height);
		gui.setLocation(posX, posY);
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.setMinimumSize(new Dimension(445, 400));
		gui.setVisible(true);
	}

	// sets the pre-sets for the bounding box, id and srid
	public static void setBoundingBoxPreSets() {
		bbox = new BoundingBox(new Position(xMin, yMin), new Position(xMax, yMax));
		bboxPanel.setBoundingBox(bbox);
	}

	// checks the export settings and validates them
	public static void checkExportSettings() {
		xMin = bboxPanel.getBoundingBox().getLowerCorner().getX();
		yMin = bboxPanel.getBoundingBox().getLowerCorner().getY();
		xMax = bboxPanel.getBoundingBox().getUpperCorner().getX();
		yMax = bboxPanel.getBoundingBox().getUpperCorner().getY();
		String str = comboIDSRID.getSelectedItem().toString();
		String[] str2 = str.split(" ");
		id = Integer.parseInt(str2[1]);
		String[] str3 = str2[5].split("\\)");
		srid = Integer.parseInt(str3[0]);
	}

	// receives the connection data from input window
	public static void setConValues(String[] str) {
		API = str[0];
		database = str[1];
		driver = str[2];
		IP = str[3];
		port = str[4];
		oracleServiceName = str[5];
		user = str[6];
		pass = str[7];
		branches = str[8];
		idTable = str[9];
		rasterProperty = str[10];
	}

	// receives wether to re-coonect or not when switching connections
	public static void setConnect(boolean con) {
		connect = con;
	}

	// creates a buffered image from a rendered image
	public static BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable properties = new Hashtable();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}

	// checks the database connection
	public static void checkConnection() {
		if (connect) {
			try {
				con = DriverManager.getConnection(
						API + ":" + database + ":" + driver + ":@" + IP + ":" + port + ":" + oracleServiceName, user,
						pass);

				con.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(errorPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				new NetworkDialog(null, API, database, driver, IP, port, oracleServiceName, user, pass, branches,
						idTable, rasterProperty, false, startup).setVisible(true);
				checkConnection();
			}
		}
	}

	// retrieves the available IDs from the database
	public static void retrieveIDs(boolean sortCall) {
		if (connect) {
			int i = 0;
			try {
				con = DriverManager.getConnection(
						API + ":" + database + ":" + driver + ":@" + IP + ":" + port + ":" + oracleServiceName, user,
						pass);
				if (!sortCall) {
					log("Verfuegbare IDs mit SRID auslesen", true);
				}
				PreparedStatement ps = con.prepareStatement("SELECT * FROM " + branches);
				ResultSet rs = ps.executeQuery();
				JGeoRaster jGeor;
				availableIDSRID.clear();
				while (rs.next()) {
					try {
						STRUCT struct = (STRUCT) rs.getObject(rasterProperty);
						jGeor = new JGeoRaster(struct);
						availableIDSRID.add(
								"ID: " + rs.getInt(idTable) + "   (SRID: " + jGeor.getSpatialExtent().getSRID() + ")");
					} catch (Exception e) {
						i++;
					}
				}
				if (!sortCall) {
					log(true);
					console.append("\n       -- Georaster, die nicht gelesen werden koennen: " + i);
					if (!startUpCon) {
						JOptionPane.showMessageDialog(null, "Mit neuer Verbindung angemeldet.");
					}
				}
				startUpCon = false;
				showSorted = true;
				con.close();
			} catch (Exception e) {
				log(false);
				JOptionPane.showMessageDialog(errorPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// sets the combo box after starting up the program
	public static void setComboBox() {
		DefaultComboBoxModel model = new DefaultComboBoxModel(availableIDSRID.toArray());
		comboIDSRID.setModel(model);
		Object[] IDSRIDArray = availableIDSRID.toArray();
		for (int i = 0; i < IDSRIDArray.length; i++) {
			String IDSRIDStr = IDSRIDArray[i].toString();
			if (IDSRIDStr.contains("ID: " + idCombo + " ")) {
				comboIDSRID.setSelectedIndex(i);
			}
		}
	}

	// receives the input values for tiled export
	public static void setIfTileWidthHeight(int width, int height, int selWidth, int selHeight, boolean tileYesNo) {
		exportAsTiles = tileYesNo;
		if (tileYesNo) {
			tileWidth = width;
			tileHeight = height;
			selectedWidth = selWidth;
			selectedHeight = selHeight;
		}
	}

	// saves a renderedimage to a file
	public static void saveImage(File file, RenderedImage img, String extension, boolean createWorldFile,
			double[] koord) {
		double xMin = koord[0];
		double yMin = koord[1];
		double xMax = koord[2];
		double yMax = koord[3];
		file = new File(file.getAbsolutePath() + "." + extension);
		RenderedImage image;
		if (!checkBoxIsRGBImage.isSelected()) {
			ImageWorker iwo = new ImageWorker(img);
			iwo.retainFirstBand();
			img = iwo.getRenderedImage();
			image = img;
		} else {
			image = img;
		}
		
		try {
			BufferedImage newBufferedImage = new BufferedImage(image.getWidth(), image.getHeight(),
					BufferedImage.TYPE_BYTE_GRAY);
			newBufferedImage.createGraphics().drawImage(convertRenderedImage(image), 0, 0, Color.BLACK, null);
			BufferedImage image2;

			if (!checkBoxIsRGBImage.isSelected()) {
				image2 = newBufferedImage;
			} else {
				image2 = convertRenderedImage(image);
			}

			if (!extension.equalsIgnoreCase("tif")) {
				if (extension.equalsIgnoreCase("png")) {
					ImageIO.write(convertRenderedImage(image2), "png", file);
				}
				if (extension.equalsIgnoreCase("jpg")) {
					log(file.getAbsolutePath(), false);
					log(convertRenderedImage(image2).getWidth() + "", false);
					ImageIO.write(convertRenderedImage(image2), "jpg", file);
					log("x" + convertRenderedImage(image2).getHeight(), false);
				}
				if (extension.equalsIgnoreCase("bmp")) {
					ImageIO.write(convertRenderedImage(image2), "bmp", file);
				}
				if (extension.equalsIgnoreCase("gif")) {
					ImageIO.write(convertRenderedImage(image2), "gif", file);
				}
				System.out.println("Exportiertes Georaster gespeichert unter:\n      " + file.getAbsolutePath());
			}

			if (extension.equalsIgnoreCase("tif")) {

				if (saveTifAsGeotiff) {
					GridCoverageFactory gcf = new GridCoverageFactory();
					CoordinateReferenceSystem finalCRS = null;
					finalCRS = CRS.decode("EPSG:" + srid, true); // Info
					Envelope2D finalEnvelope = new Envelope2D(finalCRS, xMin, yMin, xMax - xMin, yMax - yMin);
					// creating final coverage
					GridCoverage cov = gcf.create("coverage", image, finalEnvelope);
					GridCoverage2D cov2d = (GridCoverage2D) Operations.DEFAULT.resample(cov, finalCRS);
					CoverageProcessor processor = new CoverageProcessor(null);
					ParameterValueGroup params = processor.getOperation("Resample").getParameters();
					params.parameter("CoordinateReferenceSystem").setValue(finalCRS);
					params.parameter("Source").setValue(cov2d);
					cov2d = (GridCoverage2D) processor.doOperation(params);
					GeoTiffWriter gtw = new GeoTiffWriter(file);
					gtw.write(cov2d, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
					gtw.dispose();
					cov2d.dispose(true);
				}
				if (!saveTifAsGeotiff) {
					ImageIO.write(image2, "tif", file);
				}
				if (createWorldFile) {
					saveWorldFile(file, image2, xMin, yMin, xMax, yMax);
				}
			}
			if (createWorldFile) {
				saveWorldFile(file, image2, xMin, yMin, xMax, yMax);
			}

		} catch (Exception e) {
			log(false);
			log(e.getMessage(), false);
			saveError = true;
			JOptionPane.showMessageDialog(errorPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// shows the dialog informing about saving as geotiff
	public static int showTiffSpeichernInfo() {
		JLabel tifLabel = new JLabel(
				"<html>Das TIFF-Format unterstützt die Speicherung der<br>Gereferenzierung in den Metadaten des Bildes.<br>Georeferenzierung speichern...</html>");
		ButtonGroup group = new ButtonGroup();
		radio1 = new JRadioButton("Im TIFF-Tag des Bildes");
		radio1.setSelected(true);
		radio2 = new JRadioButton("Als World-File");
		radio3 = new JRadioButton("Beides");
		group.add(radio1);
		group.add(radio2);
		group.add(radio3);
		Object[] message = { tifLabel, radio1, radio2, radio3 };
		int option = JOptionPane.showConfirmDialog(null, message, "TIFF-Format", JOptionPane.OK_CANCEL_OPTION);
		return option;
	}

	// converts s to s/m/h
	public static String s2t(long sec) {
		int days = (int) (sec / 86400);
		int hours = (int) (sec % 86400) / 3600;
		int min = (int) ((sec % 86400) % 3600) / 60;
		int seconds = (int) ((sec % 86400) % 3600) % 60;

		if (days > 365) {
			days = 365;
		}
		if (hours > 0) {
			return hours + days * 24 + "h " + min + "m " + seconds + "s";
		}
		if (min > 0) {
			return min + "m " + seconds + "s";
		}
		return seconds + "s";
	}

	// receives whether to cancel the export or not
	public static void setExpCancel(boolean yesNo) {
		expCancel = yesNo;
	}

	// method for console output
	public static void log(String str, boolean sub) {
		if (sub) {
			console.append("\n       -- " + str + "... ");
		} else {
			console.append(" -- " + str + "");

		}
		console.setCaretPosition(console.getDocument().getLength());
	}

	// method for console output
	public static void log(boolean ok) {
		if (ok) {
			console.append("OK");
		} else {
			console.append("ERROR");
		}
		console.setCaretPosition(console.getDocument().getLength());
	}
}
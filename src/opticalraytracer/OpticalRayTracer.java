/***************************************************************************
 *   Copyright (C) 2014 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package opticalraytracer;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

final public class OpticalRayTracer {

	protected String appName;
	protected String VERSION = "9.6";
	protected String fullAppName;
	double textFieldDoubleSensitivity;
	double textFieldIntSensitivity;
	double curvatureFactorSensitivity;
	ProgramValues programValues;
	NumberFormat numberFormat;
	HashMap<String, ControlManager> objectControlList;
	HashMap<String, ProgramControl> programControlList;
	ArrayList<JRadioButton> typeRadioButtonList;
	ArrayList<JRadioButton> leftCurvatureRadioButtonList;
	ArrayList<JRadioButton> rightCurvatureRadioButtonList;
	Stack<String> undoStack;
	Stack<String> redoStack;
	int undoRedoMaxStack = 128;
	int maxLightRays = 1000;
	InitializationManager initManager;
	BufferedImage image = null;
	int xSize = -1;
	int ySize = -1;
	int xCenter = -1;
	int yCenter = -1;
	OpticalComponent mouseTarget = null;
	OpticalComponent selectedComponent = null;
	ArrayList<OpticalComponent> componentList;
	HashSet<String> componentNames;
	double mousePressX, mousePressY;
	int popupMouseX = -1;
	int popupMouseY = -1;
	int overlappedLensSelector = 0;
	RayTraceComputer rayTraceComputer;
	LineAnalysis lineAnalysis;
	GraphicDisplay gPaneDesign, gPaneConfigure;
	double lensMinThickness = .0001; // % of lens radius
	boolean undoFlag = true;
	boolean redoFlag = true;
	boolean suppressCombo = false;

	MyHelpPane helpPanel;
	JScrollPane tableScrollPane;
	DataTableDisplay dataTableDisplay;

	MyJFrame frame;

	JTextField leftSphereRadiusTextField;
	JTextField rightSphereRadiusTextField;
	private JTextField leftZTextField;
	private JTextField rightZTextField;
	private JTextField xPosTextField;
	private JTextField yPosTextField;
	JTextField lensRadiusTextField;
	JTextField thicknessTextField;
	private JTextField iorTextField;
	private JTextField dispersionTextField;
	private JTextField angleTextField;
	private JCheckBox symmetricalCheckBox;
	private JCheckBox inverseCheckBox;
	private JCheckBox gridCheckBox;
	private JCheckBox antiAliasCheckBox;
	private JCheckBox showControlsCheckBox;
	private JPanel lensDesignControlPane;
	protected JButton unselectButton;
	private JToolBar colorToolBar;
	private JPanel statusBar;
	JLabel statusLabel;
	private JPanel configurePane;
	private JPanel designPane;
	JTabbedPane tabbedPane;
	private JTextField arrowSizeTextField;
	private JTextField snapValueTextField;
	private JTextField beamWidthTextField;
	private JTextField beamCountTextField;
	private JTextField interactionsTextField;
	private JTextField yStartTextField;
	private JTextField beamOffsetTextField;
	private JTextField dispersionCountTextField;
	private JTextField yEndTextField;
	private JTextField xSourcePlaneTextField;
	private JTextField xTargetPlaneTextField;
	private JCheckBox divergingBeamsCheckBox;
	private JButton redoButton;
	private JButton undoButton;
	private JButton button;
	private JLabel lblSurfaceEpsilon;
	private JTextField interLensEpsilonTextField;
	private JCheckBox activeCheckBox;
	private JButton newMIrrorButton;
	private JRadioButton refractRadioButton;
	private JRadioButton reflectRadioButton;
	private JRadioButton absorbRadioButton;
	protected final ButtonGroup typeButtonGroup = new ButtonGroup();
	private JLabel lblName;
	private JTextField nameTextField;
	private JLabel lblLensEpsilon;
	private JTextField surfaceEpsilonTextField;
	private JPanel tablePane;

	private JPanel tableControlPane;
	private JTextField lineLimitTextField;
	JLabel tableDataLabel;
	private JButton btnCopyHtml;
	private JLabel lblSpaceBoxLimit;
	private JTextField spaceBoxLimitTextField;
	private JPanel helpPane;
	private JLabel lblEffectiveThickness;
	JTextField internalThicknessTextField;
	protected final ButtonGroup leftCurvButtonGroup = new ButtonGroup();
	protected final ButtonGroup rightCurvButtonGroup = new ButtonGroup();
	private JComboBox<String> leftCurvComboBox;
	private JComboBox<String> rightCurvComboBox;
	JTextField centerThicknessTextField;
	private JLabel lblCenterThickness;
	private JButton resetProgramButton;
	String lineSep = System.getProperty("line.separator");

	/**
	 * Create the application.
	 * 
	 * @param args
	 */
	public OpticalRayTracer(final String[] args) {
		// just for testing locale issues
		// Locale.setDefault(Locale.GERMANY);
		// undocumented logging feature
		undoStack = new Stack<>();
		redoStack = new Stack<>();
		numberFormat = NumberFormat.getNumberInstance();
		appName = getClass().getSimpleName();
		fullAppName = appName + " " + VERSION;
		componentList = new ArrayList<>();
		componentNames = new HashSet<>();
		programValues = new ProgramValues();
		textFieldDoubleSensitivity = .1;
		textFieldIntSensitivity = 1;
		curvatureFactorSensitivity = 0.001;
		rayTraceComputer = new RayTraceComputer(this);
		lineAnalysis = new LineAnalysis(this);
		initialize();

		JRadioButton[] trb = new JRadioButton[] { refractRadioButton,
				reflectRadioButton, absorbRadioButton };
		// this allows numerical indexing of the radio buttons
		typeRadioButtonList = new ArrayList<>(Arrays.asList(trb));

		gPaneDesign = new GraphicDisplay(this, "design pane", Common.TAB_DESIGN);
		designPane.add(gPaneDesign, SwingConstants.CENTER);
		gPaneConfigure = new GraphicDisplay(this, "configure pane",
				Common.TAB_CONFIGURE);
		configurePane.add(gPaneConfigure, SwingConstants.CENTER);
		frame.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				gPaneDesign.requestFocusInWindow();
			}
		});
		initManager = new InitializationManager(this, programValues);

		dataTableDisplay = new DataTableDisplay(this);
		dataTableDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableScrollPane = new JScrollPane(dataTableDisplay,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.getViewport().setBackground(Color.white);
		tableControlPane = new JPanel();
		tablePane.add(tableScrollPane, BorderLayout.CENTER);
		tablePane.add(tableControlPane, BorderLayout.SOUTH);
		tableControlPane.setLayout(new MigLayout("", "[117px][][][grow][]",
				"[25px]"));

		JButton copyTableButton = new JButton("Copy Data");
		copyTableButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				copyLineList(false);
			}
		});
		copyTableButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/document-save.png")));
		copyTableButton
				.setToolTipText("Copy a full tab-separated data table to the system clipboard");
		tableControlPane
				.add(copyTableButton, "cell 0 0,alignx left,aligny top");

		btnCopyHtml = new JButton("Copy HTML");
		btnCopyHtml.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				copyLineList(true);
			}
		});
		btnCopyHtml
				.setToolTipText("Copy a Web-formatted full data table to the system clipboard");
		btnCopyHtml.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/text-html.png")));
		tableControlPane.add(btnCopyHtml, "cell 1 0");

		JLabel lblNewLabel_2 = new JLabel("Line limit:");
		tableControlPane.add(lblNewLabel_2, "cell 2 0,alignx trailing");

		lineLimitTextField = new JTextField();
		lineLimitTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lineLimitTextField
				.setToolTipText("A limit to the number of displayed lines (to prevent slow operation).");
		tableControlPane.add(lineLimitTextField, "cell 3 0");
		lineLimitTextField.setColumns(10);

		tableDataLabel = new JLabel("");
		tableControlPane.add(tableDataLabel, "flowx,cell 4 0");
		setupOpticalControlFields();
		programControlList = new HashMap<>();
		setupProgramControlFields();
		setupColorButtons();
		ImageIcon programIcon = new ImageIcon(
				OpticalRayTracer.class
						.getResource("/opticalraytracer/icons/OpticalRayTracer.png"));
		frame.setIconImage(programIcon.getImage());
		frame.setTitle(fullAppName);

		resetButtonColors();
		helpPane = new JPanel();
		tabbedPane
				.addTab("Help",
						new ImageIcon(
								OpticalRayTracer.class
										.getResource("/opticalraytracer/icons/system-help.png")),
						helpPane, "Show the help document");
		helpPane.setLayout(new BorderLayout(0, 0));
		helpPanel = new MyHelpPane(this, 0);
		helpPane.add(helpPanel);
		tabbedPane.setMnemonicAt(Common.TAB_DESIGN, KeyEvent.VK_D);
		tabbedPane.setMnemonicAt(Common.TAB_CONFIGURE, KeyEvent.VK_C);
		tabbedPane.setMnemonicAt(Common.TAB_TABLE, KeyEvent.VK_T);
		tabbedPane.setMnemonicAt(Common.TAB_HELP, KeyEvent.VK_H);
		clearSelection();
		initManager.readConfig();
		frame.setBounds(programValues.windowX, programValues.windowY,
				programValues.defaultWindowWidth,
				programValues.defaultWindowHeight);
		writeProgramControls();
		writeElementControls();
		resetUndoRedo();
		setSelectedComponent(programValues.selectedComponent);
		tabbedPane.setSelectedIndex(programValues.selectedTab);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				processTabChange();
			}
		});
		// test for command-line arguments
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				processComline(args);
			}
		});
	}

	void p(String s) {
		System.out.println(s);
	}

	// process command-line arguments
	void processComline(String[] args) {
		for (String arg : args) {
			String data = null;
			switch (arg) {
			// read configuration from input stream
			case "-r":
				initManager.readStream(System.in);
				writeProgramControls();
				writeElementControls();
				break;
			// emit html table to output stream
			case "-h":
				data = lineAnalysis.makeHTMLTable(true);
				break;
			// emit tab-separated field table to output stream
			case "-t":
				data = lineAnalysis.makeCSVTable();
				break;
			// "quit" : exit OpticalRayTracer
			case "-q":
				exit();
				break;
			default:
				showNotifyMessage(String.format(
						"Don't recognize command-line argument \"%s\".", arg),
						"Argument Error");
				break;
			}
			if (data != null) {
				System.out.println(data);
			}
		}
	}

	void setupOpticalControlFields() {
		ControlManager[] array = new ControlManager[] {
				// The tag names must correspond to declared field names
				// in the ComponentValues class
				new ControlManager(nameTextField, this, "name"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						lensRadiusTextField, this, "lensRadius"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						thicknessTextField, this, "thickness"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						leftSphereRadiusTextField, this, "leftSphereRadius"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						rightSphereRadiusTextField, this, "rightSphereRadius"),
				new ControlManager(textFieldDoubleSensitivity, 0,
						OpticalComponent.maxZValue, leftZTextField, this,
						"leftZValue"),
				new ControlManager(textFieldDoubleSensitivity, 0,
						OpticalComponent.maxZValue, rightZTextField, this,
						"rightZValue"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						iorTextField, this, "ior"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						dispersionTextField, this, "dispersion"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						xPosTextField, this, "xPos"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						xPosTextField, this, "xPos"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						yPosTextField, this, "yPos"),
				new ControlManager(1, -1e10, 1e10, angleTextField, this,
						"angle"),
				new ControlManager(symmetricalCheckBox, this, "symmetrical"),
				new ControlManager(activeCheckBox, this, "active"),
				new ControlManager(refractRadioButton, this, "function"),
				new ControlManager(reflectRadioButton, this, "function"),
				new ControlManager(absorbRadioButton, this, "function"),
				new ControlManager(leftCurvComboBox, this, "leftCurvature"),
				new ControlManager(rightCurvComboBox, this, "rightCurvature"), };
		objectControlList = new HashMap<>();
		for (ControlManager cm : array) {
			objectControlList.put(cm.getTag(), cm);
		}
	}

	void setupProgramControlFields() {
		ControlManager[] array = new ControlManager[] {
				// The tag names must correspond to declared field names
				// in the ProgramValues class
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						arrowSizeTextField, this, "intersectionArrowSize"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						snapValueTextField, this, "snapValue"),
				new ControlManager(textFieldIntSensitivity, 1, 1000,
						beamWidthTextField, this, "beamWidth"),
				new ControlManager(textFieldIntSensitivity, 1, maxLightRays,
						beamCountTextField, this, "beamCount"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						interLensEpsilonTextField, this, "interLensEpsilon"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						surfaceEpsilonTextField, this, "surfEpsilon"),
				new ControlManager(textFieldIntSensitivity, 0, 1000,
						interactionsTextField, this, "maxIntersections"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						yStartTextField, this, "yStartBeamPos"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						yEndTextField, this, "yEndBeamPos"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						xSourcePlaneTextField, this, "xBeamSourceRefPlane"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						xTargetPlaneTextField, this, "xBeamRotationPlane"),
				new ControlManager(textFieldDoubleSensitivity, 0, 1e10,
						spaceBoxLimitTextField, this, "virtualSpaceSize"),
				new ControlManager(textFieldDoubleSensitivity, -1e10, 1e10,
						beamOffsetTextField, this, "beamAngle"),
				new ControlManager(textFieldIntSensitivity, 0, 1000,
						dispersionCountTextField, this, "dispersionBeams"),
				new ControlManager(textFieldIntSensitivity, 0, 50000,
						lineLimitTextField, this, "tableLineLimit"),
				new ControlManager(inverseCheckBox, this, "inverse"),
				new ControlManager(gridCheckBox, this, "showGrid"),
				new ControlManager(antiAliasCheckBox, this, "antialias"),
				new ControlManager(showControlsCheckBox, this, "showControls"),
				new ControlManager(divergingBeamsCheckBox, this,
						"divergingSource") };
		// new ControlManager(rotateFromXCheckBox, this, "rotXZero"), };

		for (ControlManager cm : array) {
			programControlList.put(cm.getTag(), cm);
		}
	}

	void setupColorButtons() {
		ColorButton[] array = new ColorButton[] {
				// The tag names must correspond to names of declared fields
				// in the program values class
				new ColorButton(this, "colorBaseline",
						"X/Y Zero Baseline color"),
				new ColorButton(this, "colorGrid", "Grid color"),
				new ColorButton(this, "colorLensOutline",
						"Lens body color : unselected"),
				new ColorButton(this, "colorLensSelected",
						"Lens body color : selected"),
				new ColorButton(this, "colorHighBackground",
						"High background color"),
				new ColorButton(this, "colorLowBackground",
						"Low background color"),
				new ColorButton(this, "colorArrow", "Intersection arrow color"),
				new ColorButton(this, "colorBeam", "Light Beam color"),
				new ColorButton(this, "colorTerminator",
						"Ray Termination color"),
				new ColorButton(this, "colorLightSource",
						"Light source bar color") };
		// colorButtonList = new HashMap<>();
		for (ColorButton cb : array) {
			programControlList.put(cb.getTag(), cb);
			colorToolBar.add(cb);
		}
	}

	int currentTab() {
		return tabbedPane.getSelectedIndex();
	}

	void resetButtonColors() {
		for (ProgramControl cb : programControlList.values()) {
			cb.reset();
		}
	}

	void setSelectedComponent(OpticalComponent oc) {
		selectedComponent = oc;
		selectedComponent.writeObjectControls();
		// move to lens design pane
		tabbedPane.setSelectedIndex(Common.TAB_DESIGN);
		processTabChange();
		programValues.selectedComponent = getSelectedComponent();
	}

	void setSelectedComponent(int sel) {
		if (sel >= 0 && sel < componentList.size()) {
			OpticalComponent oc = componentList.get(sel);
			setSelectedComponent(oc);
		} else {
			clearSelection();
		}
	}

	int getSelectedComponent() {
		int sel = componentList.indexOf(selectedComponent);
		return sel;
	}

	void clearSelection() {
		selectedComponent = null;
		programValues.selectedComponent = -1;
		for (ControlManager cm : objectControlList.values()) {
			cm.enable(false);
		}
		enableTextField(internalThicknessTextField, false);
		enableTextField(centerThicknessTextField, false);
	}

	void enableTextField(JTextField tf, boolean enabled) {
		tf.setEnabled(enabled);
		if (!enabled) {
			tf.setText("(Select a lens)");
		}
	}

	void selectNextObject() {
		if (selectedComponent == null) {
			if (componentList.size() > 0) {
				setSelectedComponent(componentList.get(0));
			} else {
				Common.beep();
			}
		} else {
			int n = componentList.size();
			for (int i = 0; i < n; i++) {
				if (componentList.get(i) == selectedComponent) {
					i = (i + 1) % n;
					setSelectedComponent(componentList.get(i));
				}
			}
		}
		updateGraphicDisplay();
	}

	void updateGraphicDisplay() {
		gPaneDesign.updateDisplay();
		gPaneConfigure.updateDisplay();
	}

	void enableComponentControls(boolean enabled) {
		for (ControlManager cm : objectControlList.values()) {
			cm.enable(enabled);
		}
		internalThicknessTextField.setEnabled(enabled);
		centerThicknessTextField.setEnabled(enabled);
		if (selectedComponent != null) {
			int rc = rightCurvComboBox.getSelectedIndex();
			int lc = leftCurvComboBox.getSelectedIndex();
			boolean lhp = lc == Common.CURVATURE_PLANAR;
			boolean rhp = rc == Common.CURVATURE_PLANAR;
			leftSphereRadiusTextField.setEnabled(!lhp);
			boolean state = enabled & !selectedComponent.values.symmetrical;
			rightSphereRadiusTextField.setEnabled(state && !rhp);
			rightCurvComboBox.setEnabled(state);
			boolean rhs = rc == Common.CURVATURE_HYPERBOLIC;
			boolean lhs = lc == Common.CURVATURE_HYPERBOLIC;
			rightZTextField.setEnabled(state && rhs);
			leftZTextField.setEnabled(lhs);
		}
	}

	double getDouble(String s) {
		return LocaleHandler.getDouble(s, LocaleHandler.localeDecimalSeparator);
	}

	void readProgramControls() {
		for (ProgramControl cm : programControlList.values()) {
			programValues.setOneValue(cm.getTag(), cm.getValue());
		}
		checkXSourcePlane();
		setupSelectedComponent();
	}

	void writeProgramControls() {
		for (ProgramControl cm : programControlList.values()) {
			cm.setValue(programValues.getOneValue(cm.getTag()));
		}
		setupSelectedComponent();

	}

	void setupSelectedComponent() {
		lensDesignControlPane.setVisible(programValues.showControls);
		if (selectedComponent != null) {
			selectedComponent.readObjectControls();
		}
		enableComponentControls(selectedComponent != null);
		updateGraphicDisplay();
	}

	// required during startup to assure all elements
	// are initialized with configuration file values
	void writeElementControls() {
		for (OpticalComponent e : componentList) {
			e.writeObjectControls();
		}
	}

	void checkXSourcePlane() {
		double x = programValues.xBeamSourceRefPlane;
		if (abs(x) > programValues.virtualSpaceSize) {
			showNotifyMessage(
					"The X source plane cannot lie outside the\nvirtual space box size -- adjusting value.",
					"X source outside domain");
			x = max(x, -programValues.virtualSpaceSize);
			x = min(x, programValues.virtualSpaceSize);
			programValues.xBeamSourceRefPlane = x;
			writeProgramControls();
		}
	}

	void makeNewObjectPopup(boolean moveToRight, double x, double y,
			int function) {
		OpticalComponent oc = makeNewComponent(moveToRight, function);
		Vector p = displayToSpace(x, y);
		oc.values.xPos = p.x + programValues.xOffset;
		oc.values.yPos = p.y + programValues.yOffset;
		oc.reconfigure();
		oc.writeObjectControls();
	}

	void makeNewLensPopup(double x, double y) {
		makeNewObjectPopup(false, x, y, Common.OBJECT_REFRACTOR);
	}

	void makeNewMirrorPopup(double x, double y) {
		makeNewObjectPopup(false, x, y, Common.OBJECT_REFLECTOR);
	}

	public void unSelectLens() {
		undoPush();
		clearSelection();
		updateGraphicDisplay();
	}

	void deleteSelectedLens() {
		if (selectedComponent != null) {
			boolean delete = false;
			if (programValues.askBeforeDeleting) {
				boolean[] reply = showConfirmMessage("Okay to delete "
						+ selectedComponent.values.name + "?", "Delete Lens",
						"In future, delete without asking");
				if (reply[0]) {
					delete = true;
				}
				if (reply[1]) {
					programValues.askBeforeDeleting = false;
				}

			} else {
				delete = true;
			}
			if (delete) {
				this.componentList.remove(selectedComponent);
				clearSelection();
				updateGraphicDisplay();
			}
		}
	}

	boolean[] showConfirmMessage(String message, String title, String extra) {
		int reply = -1;
		boolean extraReply = false;
		if (extra.length() > 0) {
			JCheckBox checkbox = new JCheckBox(extra);
			Object[] params = { message, checkbox };
			reply = JOptionPane.showConfirmDialog(frame, params, appName + ": "
					+ title, JOptionPane.YES_NO_CANCEL_OPTION);
			extraReply = checkbox.isSelected();
		} else {
			reply = JOptionPane.showConfirmDialog(frame, message, appName
					+ ": " + title, JOptionPane.YES_NO_CANCEL_OPTION);
		}
		return new boolean[] { reply == JOptionPane.YES_OPTION, extraReply };
	}

	void showNotifyMessage(String message, String title) {
		JOptionPane.showMessageDialog(frame, message, appName + ": " + title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	void showNotifyMessageFormatted(String message, String title) {
		JTextArea ta = new JTextArea(message);
		ta.setBackground(frame.getBackground());
		Font f = new Font("Monospaced", Font.PLAIN, 11);
		ta.setFont(f);
		JOptionPane.showMessageDialog(frame, ta, appName + ": " + title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	OpticalComponent makeNewComponent(String data, boolean moveToRight,
			int function) {
		undoPush();
		OpticalComponent oc = makeGenericComponent(data, function);
		if (moveToRight && componentList.size() > 0) {
			double ox = (selectedComponent != null) ? selectedComponent.values.xPos
					: 0;
			for (OpticalComponent v : componentList) {
				if (v.values.function != Common.OBJECT_ABSORBER) {
					ox = max(ox, v.values.xPos);
				}
			}
			oc.values.xPos = ox + 1;
			oc.reconfigure();
		}
		componentList.add(oc);
		selectedComponent = oc;
		oc.snapToGrid();
		updateGraphicDisplay();
		return oc;
	}

	OpticalComponent makeNewComponent(boolean moveToRight, int function) {
		return makeNewComponent(null, moveToRight, function);
	}

	OpticalComponent makeGenericComponent(String data, int function) {
		return new OpticalComponent(this, data, function);
	}

	OpticalComponent makeGenericComponent(int function) {
		return new OpticalComponent(this, function);
	}

	void setDefaults(boolean newLenses) {
		ProgramValues pv = new ProgramValues();
		String config = pv.getValues();
		programValues.setValues(config);
		xSize = -1;
		ySize = -1;
		xCenter = -1;
		yCenter = -1;
		resetButtonColors();
		if (newLenses) {
			componentList = new ArrayList<>();
			makeDefaultObjects(true);
		}
		writeProgramControls();
		resetUndoRedo();
		clearSelection();
	}

	// create two generic default lenses
	void makeDefaultObjects(boolean update) {
		if (componentList.size() == 0) {
			OpticalComponent oc = new OpticalComponent(this,
					Common.OBJECT_REFRACTOR);
			oc.values.xPos = 0;
			oc.reconfigure();
			componentList.add(oc);
			oc = new OpticalComponent(this, Common.OBJECT_REFRACTOR);
			oc.values.leftSphereRadius = -4;
			oc.values.rightSphereRadius = -4;
			oc.values.thickness = 1.4;
			oc.values.xPos = 2;
			oc.reconfigure();
			componentList.add(oc);
			oc = new OpticalComponent(this, Common.OBJECT_ABSORBER);
			oc.values.xPos = 30;
			oc.values.lensRadius = 10;
			oc.values.name = "Terminal Plane";
			oc.reconfigure();
			componentList.add(oc);
			oc.snapToGrid();
			clearSelection();
			if (update) {
				updateGraphicDisplay();
			}
		}
	}

	protected String formatNum(double v) {
		return (Double.isNaN(v)) ? "-" : LocaleHandler.formatDouble(v,
				programValues.decimalPlaces);
	}

	void clipboardCopyImage() {
		// use the same aspect ratio as the display
		// but a fixed horizontal size defined in program values
		double height = this.gPaneDesign.getHeight()
				/ (double) this.gPaneDesign.getWidth();
		int h = (int) (programValues.clipboardGraphicXSize * height);
		this.gPaneDesign.rayTraceProcessCore(
				programValues.clipboardGraphicXSize, h, true);
		ImageTransferable imt = new ImageTransferable(image);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(imt, null);
	}

	void clipboardCopyFullConfig() {
		String config = this.initManager.getFullConfiguration(true);
		setClipboardContents(config);
	}

	void clipboardCopyString(String s) {
		setClipboardContents(s);
	}

	void clipboardPasteFullConfig() {
		String data = getClipboardContents();
		if (data != null) {

			boolean[] reply = showConfirmMessage(
					"Okay to read full configuration (erases all current settings)?",
					"Read Full Configuration", "");
			if (reply[0]) {
				initManager.setFullConfiguration(data);
				writeProgramControls();
			}
		}
	}

	void setClipboardContents(String s) {
		StringSelection stringSelection = new StringSelection(s);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, frame);
	}

	String getClipboardContents() {
		String s = null;

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable content = clipboard.getContents(this);
		if (content != null) {
			try {
				s = (String) content.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
				System.out.println("getClipboardContents2: " + e);
			}
		}

		return s;
	}

	void clipboardCutLens() {
		OpticalComponent lens;
		if ((lens = clipboardCopyLens()) != null) {
			undoPush();
			this.componentList.remove(lens);
			clearSelection();
			updateGraphicDisplay();
		}
	}

	OpticalComponent clipboardCopyLens() {
		OpticalComponent lens = null;
		if (selectedComponent != null) {
			lens = selectedComponent;
			setClipboardContents(lens.toString());
		}
		return lens;
	}

	void clipboardPasteObject(boolean toMouse) {
		String s = getClipboardContents();
		if (s != null) {
			if (!decodeMultiLensString(s)) {
				OpticalComponent newLens = new OpticalComponent(this, s,
						Common.OBJECT_REFRACTOR);
				if (newLens.valid) {
					undoPush();
					if (toMouse) {
						// position lens at mouse cursor
						Vector p = displayToSpace(popupMouseX, popupMouseY);
						newLens.values.xPos = p.x + programValues.xOffset;
						newLens.values.yPos = p.y + programValues.yOffset;
					}
					componentList.add(newLens);
					newLens.snapToGrid();
					// select this lens
					setSelectedComponent(newLens);
					updateGraphicDisplay();
				}
			}
		}
	}

	protected boolean decodeMultiLensString(String data) {
		boolean result = false;
		String s = "(?s)component \\{\\s*(.*?)\\s*\\}";
		Pattern pat = Pattern.compile(s);
		Matcher m = pat.matcher(data);
		while (m.find()) {
			String v = m.group(1);
			makeNewComponent(v, false, Common.OBJECT_REFRACTOR);
			result = true;
		}
		return result;
	}

	void eraseResetAll() {
		boolean[] reply = showConfirmMessage(
				"Okay to reset program values and erase optical components\n(resets all entries and setting changes)?",
				"Reset All", "");
		if (reply[0]) {
			setDefaults(true);
		}
	}
	
	void eraseResetProgram() {
		boolean[] reply = showConfirmMessage(
				"Okay to reset program values to defaults\n(preserves entered components)?",
				"Reset Program", "");
		if (reply[0]) {
			setDefaults(false);
		}
	}

	void resetUndoRedo() {
		undoStack.clear();
		redoStack.clear();
		updateUndoRedoButtons();
	}

	void limitUndoRedoStackSize() {
		while (undoStack.size() > undoRedoMaxStack) {
			undoStack.remove(0);
		}
		while (redoStack.size() > undoRedoMaxStack) {
			redoStack.remove(0);
		}
	}

	void undoPush() {
		if (undoFlag && redoFlag) {
			String state = initManager.getFullConfiguration(false);
			String current = (undoStack.size() > 0) ? undoStack.lastElement()
					: "";
			if (!current.equals(state)) {
				undoStack.push(state);
			}
			updateUndoRedoButtons();
			limitUndoRedoStackSize();
		}
	}

	void redoPush() {
		if (redoFlag) {
			String state = initManager.getFullConfiguration(false);
			String current = (redoStack.size() > 0) ? undoStack.lastElement()
					: "";
			if (!current.equals(state)) {
				redoStack.push(state);
			}
			updateUndoRedoButtons();
			limitUndoRedoStackSize();
		}
	}

	void undoPop() {
		if (undoStack.size() > 0 && undoFlag) {
			undoFlag = false;
			redoPush();
			initManager.setFullConfiguration(undoStack.pop());
			this.selectedComponent = null;
			updateUndoRedoButtons();
			undoFlag = true;
			updateGraphicDisplay();
			setupSelectedComponent();
		} else {
			Common.beep();
		}
	}

	void redoPop() {
		if (redoStack.size() > 0 && redoFlag) {
			undoPush();
			redoFlag = false;
			initManager.setFullConfiguration(redoStack.pop());
			this.selectedComponent = null;
			updateUndoRedoButtons();
			redoFlag = true;
			updateGraphicDisplay();
			setupSelectedComponent();
		} else {
			Common.beep();
		}
	}

	void updateUndoRedoButtons() {
		redoButton.setEnabled(redoStack.size() > 0);
		undoButton.setEnabled(undoStack.size() > 0);
	}

	void setSelectedLens(OpticalComponent p) {
		selectedComponent = p;
		setupSelectedComponent();
	}

	protected void copyLineList(boolean html) {
		String data = (html) ? lineAnalysis.makeHTMLTable(true) : lineAnalysis
				.makeCSVTable();
		setClipboardContents(data);
	}

	ComplexInt spaceToDisplay(double x, double y) {
		int dx = (int) (((x - programValues.xOffset) * programValues.dispScale * ySize) + xCenter);
		int dy = (int) (yCenter - ((y - programValues.yOffset)
				* programValues.dispScale * ySize));
		return new ComplexInt(dx, dy);
	}

	Vector displayToSpace(double dx, double dy) {
		double x = ((dx - xCenter) / (programValues.dispScale * ySize));
		double y = ((yCenter - dy) / (programValues.dispScale * ySize));
		return new Vector(x, y);
	}

	Vector displayToSpaceOffset(Vector p) {
		double x = ((p.x - xCenter) / (programValues.dispScale * ySize))
				+ programValues.xOffset;
		double y = ((yCenter - p.y) / (programValues.dispScale * ySize))
				+ programValues.yOffset;
		return new Vector(x, y);
	}

	void writeLog(String data) {
		try {
			String path = System.getProperty("user.home")
					+ "/OPticalRayTracerDebugLog.txt";
			FileWriter fw = new FileWriter(path, true);
			fw.write(data + "\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void processTabChange() {
		switch (currentTab()) {
		case Common.TAB_TABLE:
			if (dataTableDisplay != null) {
				dataTableDisplay.requestFocus();
			}
			break;
		case Common.TAB_CONFIGURE:
			if (gPaneConfigure != null) {
				gPaneConfigure.acquireFocus();
			}
			break;
		case Common.TAB_DESIGN:
			if (gPaneDesign != null) {
				gPaneDesign.acquireFocus();
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OpticalRayTracer window = new OpticalRayTracer(args);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected void exit() {
		readProgramControls();
		programValues.windowX = frame.getX();
		programValues.windowY = frame.getY();
		programValues.defaultWindowWidth = frame.getWidth();
		programValues.defaultWindowHeight = frame.getHeight();
		programValues.selectedTab = tabbedPane.getSelectedIndex();
		this.helpPanel.onQuit();
		initManager.writeConfig(true);
		frame.setVisible(false);
		frame.dispose();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new MyJFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		Dimension minSize = new Dimension(700, 500);
		frame.setBounds(100, 100, minSize.width, minSize.height);
		frame.setMinimumSize(new Dimension(900, 400));
		frame.setPreferredSize(new Dimension(900, 600));
		frame.setSize(minSize);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				processTabChange();
			}
		});
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		designPane = new JPanel();

		designPane.setToolTipText("");
		designPane.setBackground(SystemColor.window);
		tabbedPane
				.addTab("Design",
						new ImageIcon(
								OpticalRayTracer.class
										.getResource("/opticalraytracer/icons/applications-graphics.png")),
						designPane, "Design elements and optical layout");
		designPane.setLayout(new BorderLayout(0, 0));

		JPanel controlPane = new JPanel();
		designPane.add(controlPane, BorderLayout.SOUTH);
		controlPane.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar(fullAppName + " Program Controls");
		controlPane.add(toolBar, BorderLayout.NORTH);

		JButton newLensButton = new JButton("");
		newLensButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeNewComponent(true, Common.OBJECT_REFRACTOR);
			}
		});
		newLensButton
				.setToolTipText("Create new lens to right of existing objects");
		newLensButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/document-new.png")));
		toolBar.add(newLensButton);

		JButton resetAllButton = new JButton("");
		resetAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eraseResetAll();
			}
		});

		newMIrrorButton = new JButton("");
		newMIrrorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeNewComponent(true, Common.OBJECT_REFLECTOR);
			}
		});

		newMIrrorButton
				.setToolTipText("Create new reflector/absorber to right of existing objects");
		newMIrrorButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/view-fullscreen.png")));
		toolBar.add(newMIrrorButton);
		
		resetProgramButton = new JButton("");
		resetProgramButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eraseResetProgram();
			}
		});
		resetProgramButton.setToolTipText("<html>Reset only program values<br/>(asks for confirmation)");
		resetProgramButton.setIcon(new ImageIcon(OpticalRayTracer.class.getResource("/opticalraytracer/icons/network-offline.png")));
		toolBar.add(resetProgramButton);
		resetAllButton
				.setToolTipText("<html>Reset everything : program values and optical components<br/>(asks for confirmation)");
		resetAllButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/process-stop.png")));
		toolBar.add(resetAllButton);

		JButton copyImageButton = new JButton("");
		copyImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clipboardCopyImage();
			}
		});
		copyImageButton
				.setToolTipText("Copy workspace graphic image to clipboard");
		copyImageButton
				.setIcon(new ImageIcon(
						OpticalRayTracer.class
								.getResource("/opticalraytracer/icons/applications-multimedia.png")));
		toolBar.add(copyImageButton);

		JButton copyConfigurationButton = new JButton("");
		copyConfigurationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clipboardCopyFullConfig();
			}
		});
		copyConfigurationButton
				.setToolTipText("Copy full configuration to clipboard");
		copyConfigurationButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/edit-copy.png")));
		toolBar.add(copyConfigurationButton);

		JButton pasteConfigurationButton = new JButton("");
		pasteConfigurationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clipboardPasteFullConfig();
			}
		});
		pasteConfigurationButton
				.setToolTipText("<html>Paste full configuration from clipboard<br/>(Asks for confirmation)");
		pasteConfigurationButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/edit-paste.png")));
		toolBar.add(pasteConfigurationButton);

		undoButton = new JButton("");
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoPop();
			}
		});
		undoButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/edit-undo.png")));
		undoButton.setToolTipText("Undo most recent action");
		toolBar.add(undoButton);

		redoButton = new JButton("");
		redoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				redoPop();
			}
		});
		redoButton.setToolTipText("Redo most recent undone action");
		redoButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/edit-redo.png")));
		toolBar.add(redoButton);

		button = new JButton("");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNextObject();
			}
		});

		unselectButton = new JButton("");
		unselectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unSelectLens();
			}
		});
		unselectButton.setToolTipText("Clear present selection");
		unselectButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/edit-clear.png")));
		toolBar.add(unselectButton);
		button.setToolTipText("Cycle through lens selections");
		button.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/view-refresh.png")));
		toolBar.add(button);

		inverseCheckBox = new JCheckBox("Inverse");
		inverseCheckBox.setAlignmentY(0.53f);
		inverseCheckBox.setBorder(new CompoundBorder(new LineBorder(new Color(
				153, 153, 153)), new LineBorder(new Color(238, 238, 238), 8)));
		inverseCheckBox.setBorderPainted(true);
		inverseCheckBox.setToolTipText("Dark background");
		toolBar.add(inverseCheckBox);

		gridCheckBox = new JCheckBox("Grid");
		gridCheckBox.setAlignmentY(0.53f);
		gridCheckBox.setBorderPainted(true);
		gridCheckBox.setBorder(new CompoundBorder(new LineBorder(new Color(153,
				153, 153)), new LineBorder(new Color(238, 238, 238), 8)));
		gridCheckBox.setSelected(true);
		gridCheckBox.setToolTipText("Show grid lines");
		toolBar.add(gridCheckBox);

		antiAliasCheckBox = new JCheckBox("Antialias");
		antiAliasCheckBox.setAlignmentY(0.53f);
		antiAliasCheckBox.setBorderPainted(true);
		antiAliasCheckBox.setBorder(new CompoundBorder(new LineBorder(
				new Color(153, 153, 153)), new LineBorder(new Color(238, 238,
				238), 8)));
		antiAliasCheckBox.setSelected(true);
		antiAliasCheckBox.setToolTipText("Best appearance, slower drawing");
		toolBar.add(antiAliasCheckBox);

		showControlsCheckBox = new JCheckBox("Controls");
		showControlsCheckBox.setAlignmentY(0.53f);
		showControlsCheckBox.setBorderPainted(true);
		showControlsCheckBox.setBorder(new CompoundBorder(new LineBorder(
				new Color(153, 153, 153)), new LineBorder(new Color(238, 238,
				238), 8)));
		showControlsCheckBox.setSelected(true);
		showControlsCheckBox
				.setToolTipText("Show/hide lens design control panel");
		toolBar.add(showControlsCheckBox);

		JButton quitButton = new JButton("");
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		quitButton.setToolTipText("Quit OpticalRayTracer");
		quitButton.setIcon(new ImageIcon(OpticalRayTracer.class
				.getResource("/opticalraytracer/icons/application-exit.png")));
		toolBar.add(quitButton);

		lensDesignControlPane = new JPanel();
		controlPane.add(lensDesignControlPane, BorderLayout.CENTER);
		lensDesignControlPane.setLayout(new MigLayout("", "[399px][492px]",
				"[110px][35px]"));

		JPanel westSubPanel = new JPanel();
		westSubPanel.setToolTipText("Optical element controls");
		westSubPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		lensDesignControlPane.add(westSubPanel,
				"cell 0 0,alignx left,aligny top");
		westSubPanel.setLayout(new MigLayout("", "[111px][grow][][grow]",
				"[23px][][][][]"));

		lblName = new JLabel("Name");
		westSubPanel.add(lblName, "cell 0 0,alignx left");

		nameTextField = new JTextField();
		nameTextField
				.setToolTipText("Distinctive names help organize complex optical configurations");

		westSubPanel.add(nameTextField, "cell 1 0 3 1,growx");
		nameTextField.setColumns(10);

		JLabel lblLensRadius = new JLabel("Radius");
		westSubPanel.add(lblLensRadius, "cell 0 1");

		lensRadiusTextField = new JTextField();
		lensRadiusTextField
				.setToolTipText("<html>Center-to-edge radius<br/>(red if value conflict exists)");
		lensRadiusTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		westSubPanel.add(lensRadiusTextField, "cell 1 1");
		lensRadiusTextField.setColumns(10);

		JLabel lblX = new JLabel("X");
		westSubPanel.add(lblX, "cell 2 2,alignx center");

		JLabel lblPosition = new JLabel("Position");
		westSubPanel.add(lblPosition, "cell 3 1,alignx center");

		JLabel lblThickness = new JLabel("Edge Thickness");
		lblThickness.setHorizontalAlignment(SwingConstants.LEFT);
		westSubPanel.add(lblThickness, "cell 0 2,alignx left");

		thicknessTextField = new JTextField();
		thicknessTextField
				.setToolTipText("<html>Element edge thickness<br/>(red if value conflict exists)");
		thicknessTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		westSubPanel.add(thicknessTextField, "cell 1 2");
		thicknessTextField.setColumns(10);

		JLabel lblY = new JLabel("Y");
		westSubPanel.add(lblY, "cell 2 3,alignx center");

		xPosTextField = new JTextField();
		westSubPanel.add(xPosTextField, "cell 3 2");
		xPosTextField.setToolTipText("Lens X (horizontal) coordinate");
		xPosTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		xPosTextField.setColumns(10);

		lblEffectiveThickness = new JLabel("Effective Thickness");
		lblEffectiveThickness.setHorizontalAlignment(SwingConstants.LEFT);
		westSubPanel.add(lblEffectiveThickness, "cell 0 3,alignx left");

		internalThicknessTextField = new JTextField();
		internalThicknessTextField.setEditable(false);
		internalThicknessTextField
				.setToolTipText("<html>Element edge thickness after crossover prevention<br/>(red if value conflict exists)");
		internalThicknessTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		internalThicknessTextField.setColumns(10);
		westSubPanel.add(internalThicknessTextField, "cell 1 3,alignx right");

		yPosTextField = new JTextField();
		westSubPanel.add(yPosTextField, "cell 3 3");
		yPosTextField.setToolTipText("Lens Y (vertical) coordinate");
		yPosTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		yPosTextField.setColumns(10);

		lblCenterThickness = new JLabel("Center Thickness");
		lblCenterThickness.setHorizontalAlignment(SwingConstants.LEFT);
		westSubPanel.add(lblCenterThickness, "cell 0 4,alignx left");

		centerThicknessTextField = new JTextField();
		centerThicknessTextField
				.setToolTipText("Element center thickness (computed)");
		centerThicknessTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		centerThicknessTextField.setEditable(false);
		centerThicknessTextField.setColumns(10);
		westSubPanel.add(centerThicknessTextField, "cell 1 4,growx");

		JPanel lensDesignControlBox = new JPanel();
		lensDesignControlBox.setToolTipText("Optical surface controls");
		lensDesignControlBox.setBorder(new LineBorder(new Color(0, 0, 0)));
		lensDesignControlPane.add(lensDesignControlBox, "cell 1 0,grow");
		lensDesignControlBox.setLayout(new MigLayout("", "[][][][][][][]",
				"[][][][]"));

		symmetricalCheckBox = new JCheckBox("Sym");
		lensDesignControlBox.add(symmetricalCheckBox, "cell 0 0");
		symmetricalCheckBox
				.setToolTipText("Make lens symmetrical (right and left the same)");

		JLabel lblSphereRadius = new JLabel("Sphere Radius");
		lblSphereRadius.setHorizontalAlignment(SwingConstants.LEFT);
		lensDesignControlBox.add(lblSphereRadius, "cell 1 0,alignx center");

		JLabel lblSelectCurvature = new JLabel("Curvature Class");
		lensDesignControlBox.add(lblSelectCurvature, "cell 2 0,alignx center");

		JLabel lblCurvatureFactor = new JLabel("Hyperbolic Factor");
		lblCurvatureFactor.setHorizontalAlignment(SwingConstants.CENTER);
		lensDesignControlBox.add(lblCurvatureFactor, "cell 3 0,alignx center");

		JLabel lblLeft = new JLabel("Left");
		lensDesignControlBox.add(lblLeft, "cell 0 1,alignx left");

		leftSphereRadiusTextField = new JTextField();
		leftSphereRadiusTextField
				.setToolTipText("<html>Radius of left sphere that constructs this lens<br/>(red if invalid value)");
		leftSphereRadiusTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensDesignControlBox.add(leftSphereRadiusTextField, "cell 1 1");
		leftSphereRadiusTextField.setColumns(10);

		leftCurvComboBox = new JComboBox<String>();
		leftCurvComboBox.setToolTipText("Left surface curvature class");

		lensDesignControlBox.add(leftCurvComboBox, "cell 2 1,alignx center");

		leftZTextField = new JTextField();
		leftZTextField.setToolTipText("Left hyperbolic curvature factor");
		leftZTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensDesignControlBox.add(leftZTextField, "cell 3 1");
		leftZTextField.setColumns(10);

		JLabel lblRight = new JLabel("Right");
		lensDesignControlBox.add(lblRight, "cell 0 2,alignx left");

		rightSphereRadiusTextField = new JTextField();
		rightSphereRadiusTextField
				.setToolTipText("<html>Radius of right sphere that constructs this lens<br/>(red if invalid value)");
		rightSphereRadiusTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensDesignControlBox.add(rightSphereRadiusTextField, "cell 1 2");
		rightSphereRadiusTextField.setColumns(10);

		rightCurvComboBox = new JComboBox<String>();
		rightCurvComboBox.setToolTipText("Right surface curvature class");
		lensDesignControlBox.add(rightCurvComboBox, "cell 2 2,alignx center");

		rightZTextField = new JTextField();
		rightZTextField.setToolTipText("Right hyperbolic curvature factor");
		rightZTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensDesignControlBox.add(rightZTextField, "cell 3 2");
		rightZTextField.setColumns(10);

		activeCheckBox = new JCheckBox("Active");
		lensDesignControlBox.add(activeCheckBox, "cell 0 3");
		activeCheckBox
				.setToolTipText("Include this component in the optical calculation");

		refractRadioButton = new JRadioButton("Refract");
		lensDesignControlBox.add(refractRadioButton, "cell 1 3");
		refractRadioButton.setToolTipText("Refract all intersecting rays");
		refractRadioButton.setName("");
		typeButtonGroup.add(refractRadioButton);

		reflectRadioButton = new JRadioButton("Reflect");
		lensDesignControlBox.add(reflectRadioButton, "cell 2 3");
		reflectRadioButton.setToolTipText("Reflect all intersecting rays");
		reflectRadioButton.setName("");
		typeButtonGroup.add(reflectRadioButton);

		absorbRadioButton = new JRadioButton("Absorb");
		lensDesignControlBox.add(absorbRadioButton, "cell 3 3");
		absorbRadioButton.setToolTipText("Absorb all intersecting rays");
		absorbRadioButton.setName("");
		typeButtonGroup.add(absorbRadioButton);

		JPanel lensOptionsControlBox = new JPanel();
		lensOptionsControlBox.setToolTipText("Optical behavior/angle controls");
		lensOptionsControlBox.setBorder(new LineBorder(new Color(0, 0, 0)));
		lensDesignControlPane.add(lensOptionsControlBox,
				"cell 0 1 2 1,growx,aligny top");
		lensOptionsControlBox.setLayout(new MigLayout("", "[][][][][][][][][]",
				"[]"));

		JLabel lblIor = new JLabel("IOR");
		lensOptionsControlBox.add(lblIor, "cell 0 0");

		iorTextField = new JTextField();
		iorTextField.setToolTipText("Index of refraction");
		iorTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensOptionsControlBox.add(iorTextField, "cell 1 0");
		iorTextField.setColumns(10);

		JLabel lblDispersion = new JLabel("Abbe number");
		lensOptionsControlBox.add(lblDispersion, "cell 2 0");

		dispersionTextField = new JTextField();
		dispersionTextField
				.setToolTipText("Wavelength-dependent property of some media");
		dispersionTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensOptionsControlBox.add(dispersionTextField, "cell 3 0");
		dispersionTextField.setColumns(10);

		JLabel lblAngle = new JLabel("Angle");
		lensOptionsControlBox.add(lblAngle, "cell 4 0");

		angleTextField = new JTextField();
		angleTextField.setToolTipText("Lens rotation angle");
		angleTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		lensOptionsControlBox.add(angleTextField, "cell 5 0");
		angleTextField.setColumns(10);

		configurePane = new JPanel();
		configurePane.setFocusTraversalKeysEnabled(false);
		tabbedPane
				.addTab("Configure",
						new ImageIcon(
								OpticalRayTracer.class
										.getResource("/opticalraytracer/icons/applications-accessories.png")),
						configurePane, "Set global options");
		tabbedPane.setDisplayedMnemonicIndexAt(1, 1);
		configurePane.setLayout(new BorderLayout(0, 0));

		JPanel controlBox = new JPanel();
		configurePane.add(controlBox, BorderLayout.SOUTH);
		controlBox.setLayout(new BorderLayout(0, 0));

		colorToolBar = new JToolBar(fullAppName + " Program Colors");
		controlBox.add(colorToolBar, BorderLayout.NORTH);

		JPanel programControlPane = new JPanel();
		controlBox.add(programControlPane, BorderLayout.CENTER);
		programControlPane.setLayout(new MigLayout("",
				"[][grow][][grow][][grow][][]", "[][][][][]"));

		JLabel lblIntersectionDotSize = new JLabel("Insersection arrow size");
		programControlPane.add(lblIntersectionDotSize,
				"cell 0 0,alignx trailing");

		arrowSizeTextField = new JTextField();
		arrowSizeTextField.setToolTipText("These mark each beam interaction");
		arrowSizeTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(arrowSizeTextField, "cell 1 0");
		arrowSizeTextField.setColumns(10);

		JLabel lblSnapValue = new JLabel("Snap-to-grid value");
		programControlPane.add(lblSnapValue, "cell 2 0,alignx trailing");

		snapValueTextField = new JTextField();
		snapValueTextField
				.setToolTipText("A nonzero value causes lenses to align themselves to the grid");
		snapValueTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(snapValueTextField, "cell 3 0");
		snapValueTextField.setColumns(10);

		JLabel lblBeamWidth = new JLabel("Beam width");
		programControlPane.add(lblBeamWidth, "cell 4 0,alignx trailing");

		beamWidthTextField = new JTextField();
		beamWidthTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(beamWidthTextField, "cell 5 0");
		beamWidthTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Light beam Count");
		programControlPane.add(lblNewLabel, "cell 0 1,alignx trailing");

		beamCountTextField = new JTextField();
		beamCountTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(beamCountTextField, "cell 1 1");
		beamCountTextField.setColumns(10);

		JLabel lblSourceYStart = new JLabel("Source Y start");
		programControlPane.add(lblSourceYStart, "cell 2 1,alignx trailing");

		yStartTextField = new JTextField();
		yStartTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(yStartTextField, "cell 3 1");
		yStartTextField.setColumns(10);

		JLabel lblSourceYEnd = new JLabel("Source Y end");
		programControlPane.add(lblSourceYEnd, "cell 4 1,alignx trailing");

		yEndTextField = new JTextField();
		yEndTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(yEndTextField, "cell 5 1");
		yEndTextField.setColumns(10);

		JLabel lblInteractionCount = new JLabel("Maximum interactions");
		programControlPane.add(lblInteractionCount, "cell 0 2,alignx trailing");

		interactionsTextField = new JTextField();
		interactionsTextField.setToolTipText("Number of interactions per beam");
		interactionsTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(interactionsTextField, "cell 1 2");
		interactionsTextField.setColumns(10);

		JLabel lblXSourcePlane = new JLabel("X source plane");
		programControlPane.add(lblXSourcePlane, "cell 2 2,alignx trailing");

		xSourcePlaneTextField = new JTextField();
		xSourcePlaneTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(xSourcePlaneTextField, "cell 3 2");
		xSourcePlaneTextField.setColumns(10);

		JLabel lblXTargetPlane = new JLabel("X beam rotation plane");
		programControlPane.add(lblXTargetPlane, "cell 4 2,alignx trailing");

		xTargetPlaneTextField = new JTextField();
		xTargetPlaneTextField
				.setToolTipText("The vertical plane around which the light beams rotate");
		xTargetPlaneTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(xTargetPlaneTextField, "cell 5 2");
		xTargetPlaneTextField.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Beam offset angle");
		programControlPane.add(lblNewLabel_1, "cell 0 3,alignx trailing");

		beamOffsetTextField = new JTextField();
		beamOffsetTextField
				.setToolTipText("Allows beams to pass at an angle to the horizonal");
		beamOffsetTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(beamOffsetTextField, "cell 1 3");
		beamOffsetTextField.setColumns(10);

		JLabel lblDispersionBeamCount = new JLabel("Dispersion beam count");
		programControlPane.add(lblDispersionBeamCount,
				"cell 2 3,alignx trailing");

		dispersionCountTextField = new JTextField();
		dispersionCountTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(dispersionCountTextField, "cell 3 3");
		dispersionCountTextField.setColumns(10);

		lblSurfaceEpsilon = new JLabel("Interlens Epsilon");
		programControlPane.add(lblSurfaceEpsilon, "cell 4 3,alignx trailing");

		interLensEpsilonTextField = new JTextField();
		interLensEpsilonTextField
				.setToolTipText("This value tells the ray tracer how to distinguish between lens surfaces");
		interLensEpsilonTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		interLensEpsilonTextField.setColumns(10);
		programControlPane.add(interLensEpsilonTextField, "cell 5 3");

		lblLensEpsilon = new JLabel("Surface Epsilon");
		programControlPane.add(lblLensEpsilon, "cell 0 4,alignx trailing");

		surfaceEpsilonTextField = new JTextField();
		surfaceEpsilonTextField
				.setToolTipText("<html>This value represents an acceptance boundary around<br/>\n objects that allows them to be recognized by the ray tracer");
		surfaceEpsilonTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		programControlPane.add(surfaceEpsilonTextField, "cell 1 4");
		surfaceEpsilonTextField.setColumns(10);

		lblSpaceBoxLimit = new JLabel("Virtual space box size");
		programControlPane.add(lblSpaceBoxLimit, "cell 2 4,alignx trailing");

		spaceBoxLimitTextField = new JTextField();
		spaceBoxLimitTextField
				.setToolTipText("The space box size (±) in the X and Y dimensions");
		spaceBoxLimitTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		spaceBoxLimitTextField.setColumns(10);
		programControlPane.add(spaceBoxLimitTextField, "cell 3 4");

		divergingBeamsCheckBox = new JCheckBox("Diverging beams");
		programControlPane.add(divergingBeamsCheckBox, "cell 4 4 2 1");

		tablePane = new JPanel();
		tablePane.setBackground(Color.WHITE);
		tabbedPane
				.addTab("Table",
						new ImageIcon(
								OpticalRayTracer.class
										.getResource("/opticalraytracer/icons/x-office-spreadsheet.png")),
						tablePane, "Show a table of the current traced rays");
		tablePane.setLayout(new BorderLayout(0, 0));
		statusBar = new JPanel();
		statusBar.setBorder(new EmptyBorder(2, 4, 2, 0));
		statusBar.setToolTipText("Program status and cursor position");
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
		statusBar.setLayout(new BorderLayout(0, 0));

		statusLabel = new JLabel("Program Status");
		statusLabel.setFont(new Font("Courier", Font.BOLD, 12));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar.add(statusLabel);
	}
}

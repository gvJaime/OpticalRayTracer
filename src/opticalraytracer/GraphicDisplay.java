package opticalraytracer;

import static java.lang.Math.abs;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

@SuppressWarnings("serial")
final public class GraphicDisplay extends JPanel {

	boolean mouseInside = false;
	OpticalRayTracer parent;
	OpticalComponent ocUnderMouse = null;
	String name;
	int tabValue;
	int testCount = 0;
	ProgramValues programValues;
	RayTraceComputer rayTraceComputer;
	JPopupMenu popupMenu;
	boolean hasFocus = false;
	
	Cursor handCursor, moveCursor, crossCursor, defaultCursor;

	boolean shiftKey, ctrlKey, altKey;

	public GraphicDisplay(OpticalRayTracer p, String name, int tab) {
		this.name = name;
		tabValue = tab;
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				hasFocus = true;
				repaint();
			}
			@Override
			public void focusLost(FocusEvent e) {
				hasFocus = false;
				repaint();
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				handleMouseWheelEvent(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				handleMouseMove(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				handleMouseDrag(e);
			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				repaint();
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				handleMousePressEvent(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setMouseInside(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setMouseInside(false);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				handleMouseReleaseEvent(e);
			}
		});
		// this allows the tab key to be captured
		// setFocusTraversalKeysEnabled(false);
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				// parent.p("key typed");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				// parent.p("key pressed");
				handleKeyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				// parent.p("key released");

			}
		});

		parent = p;
		programValues = parent.programValues;
		rayTraceComputer = parent.rayTraceComputer;
		handCursor = new Cursor(Cursor.HAND_CURSOR);
		moveCursor = new Cursor(Cursor.MOVE_CURSOR);
		crossCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		// initComponents();
		// setFocusable(true);
		// setRequestFocusEnabled(true);

		popupMenu = new JPopupMenu();
		// popupMenu.setLightWeightPopupEnabled(false);
		// popupMenu.setInvoker(this);
		addPopup(this, popupMenu);

		JMenuItem mntmNewLens = new JMenuItem("New Lens");
		mntmNewLens.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.makeNewLensPopup(parent.popupMouseX, parent.popupMouseY);
			}
		});
		mntmNewLens.setToolTipText("Create new lens at cursor position");
		mntmNewLens.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/document-new.png")));
		popupMenu.add(mntmNewLens);

		JMenuItem mntmNewMirror = new JMenuItem("New Mirror");
		mntmNewMirror.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.makeNewMirrorPopup(parent.popupMouseX,
						parent.popupMouseY);
			}
		});
		mntmNewMirror.setToolTipText("Create new mirror at cursor position");
		mntmNewMirror.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/view-fullscreen.png")));
		popupMenu.add(mntmNewMirror);

		JMenuItem mntmLineProperties = new JMenuItem("Line Properties");
		mntmLineProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.lineAnalysis.nearestLineProperties(parent.mousePressX,
						parent.mousePressY);
			}
		});
		mntmLineProperties
				.setToolTipText("<html>List properties of line closest to mouse cursor<br/>(double-click also works)");

		mntmLineProperties.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/document-save.png")));
		popupMenu.add(mntmLineProperties);

		JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.clipboardCutLens();
			}
		});
		mntmCut.setToolTipText("Cut selected object");
		mntmCut.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/edit-cut.png")));
		popupMenu.add(mntmCut);

		JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.clipboardCopyLens();
			}
		});
		mntmCopy.setToolTipText("Copy selected object");
		mntmCopy.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/edit-copy.png")));
		popupMenu.add(mntmCopy);

		JMenuItem mntmPaste = new JMenuItem("Paste: mouse cursor");
		mntmPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.clipboardPasteObject(true);
			}
		});
		mntmPaste.setToolTipText("Paste object to mouse cursor position");
		mntmPaste.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/edit-paste.png")));
		popupMenu.add(mntmPaste);

		JMenuItem mntmNewMenuItem = new JMenuItem("Paste: defined position");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.clipboardPasteObject(false);
			}
		});
		mntmNewMenuItem.setToolTipText("Paste object to its defined position");
		mntmNewMenuItem.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/edit-paste.png")));
		popupMenu.add(mntmNewMenuItem);

		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.deleteSelectedLens();
			}
		});
		mntmDelete.setToolTipText("Delete selected object");
		mntmDelete.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/process-stop.png")));
		popupMenu.add(mntmDelete);

		JMenuItem mntmContextHelp = new JMenuItem("Context Help");
		mntmContextHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contextHelp();
			}
		});
		mntmContextHelp
				.setToolTipText("Show a brief explanation of this display's controls");

		mntmContextHelp.setIcon(new ImageIcon(GraphicDisplay.class
				.getResource("/opticalraytracer/icons/system-help.png")));
		popupMenu.add(mntmContextHelp);
	}

	protected void acquireFocus() {
		setFocusable(true);
		setRequestFocusEnabled(true);
		requestFocusInWindow();
	}

	protected Vector setupCursorPosition(int centerX, int centerY) {
		parent.popupMouseX = centerX;
		parent.popupMouseY = centerY;
		Vector p = parent.displayToSpace(parent.popupMouseX, parent.popupMouseY);
		parent.mousePressX = p.x + programValues.xOffset;
		parent.mousePressY = p.y + programValues.yOffset;
		return p;
	}
	
	protected void centerCursorOnScreen() {
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		Point ps = getLocationOnScreen();
		// this.setCursor(crossCursor);
		try {
			Robot r = new Robot();
			r.mouseMove(centerX + ps.x, centerY + ps.y);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleKeyPressed(KeyEvent evt) {
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		boolean ctrlKey = evt.isControlDown();
		boolean shiftKey = evt.isShiftDown();
		boolean altKey = evt.isAltDown();
		double multiplier = 1;
		multiplier = (shiftKey) ? multiplier * 0.1 : multiplier;
		multiplier = (altKey) ? multiplier * 0.1 : multiplier;

		double panStep = .05 * multiplier / programValues.dispScale;
		double zoomStep = .1 * multiplier;
		boolean consume = true;

		int kcode = evt.getKeyCode();
		switch (kcode) {
		case KeyEvent.VK_F1:
			contextHelp();
			break;
		case KeyEvent.VK_ENTER:
			Vector c = parent.displayToSpace(centerX, centerY).translate(programValues.xOffset,programValues.yOffset);
			//c.translate(programValues.xOffset,programValues.yOffset);
			OpticalComponent p = testMouseInsideLens(c,false);
			if (p != null) {
				parent.setSelectedComponent(p);
				parent.mouseTarget = p;
			} else {
				setupCursorPosition(centerX, centerY);
				parent.lineAnalysis.nearestLineProperties(parent.mousePressX,
						parent.mousePressY);
			}
			break;
		case KeyEvent.VK_L:
			setupCursorPosition(centerX, centerY);
			parent.lineAnalysis.nearestLineProperties(parent.mousePressX,
					parent.mousePressY);
			break;
		case KeyEvent.VK_O:
			parent.selectNextObject();
			break;
		case KeyEvent.VK_U:
			parent.unSelectLens();
			break;
		case KeyEvent.VK_M:
		case KeyEvent.VK_CONTEXT_MENU:
		case KeyEvent.VK_WINDOWS:
			setupCursorPosition(centerX, centerY);
			popupMenu.show(this, parent.popupMouseX, parent.popupMouseY);
			break;
		case KeyEvent.VK_TAB:
			// break out to allow keys to work in the remainder
			// of the program including tab itself
			break;
		case KeyEvent.VK_NUMPAD7:
		case KeyEvent.VK_HOME:
		case KeyEvent.VK_ADD:
		case KeyEvent.VK_PLUS:
		case KeyEvent.VK_EQUALS:
			processZoom(ctrlKey, shiftKey, zoomStep);
			break;
		case KeyEvent.VK_NUMPAD1:
		case KeyEvent.VK_SUBTRACT:
		case KeyEvent.VK_END:
		case KeyEvent.VK_MINUS:
			processZoom(ctrlKey, shiftKey, -zoomStep);
			break;
		case KeyEvent.VK_NUMPAD8:
		case KeyEvent.VK_KP_UP:
		case KeyEvent.VK_UP:
			processPan(true, ctrlKey, panStep);
			break;
		case KeyEvent.VK_NUMPAD2:
		case KeyEvent.VK_KP_DOWN:
		case KeyEvent.VK_DOWN:
			processPan(true, ctrlKey, -panStep);
			break;
		case KeyEvent.VK_NUMPAD4:
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			processPan(false, ctrlKey, -panStep);
			break;
		case KeyEvent.VK_NUMPAD6:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			processPan(false, ctrlKey, panStep);
			break;
		default:
			consume = false;
			break;
		}
		if (consume) {
			evt.consume();
			centerCursorOnScreen();
			rayTraceProcess(true);
		}
	}

	protected void processPan(boolean vertical, boolean ctrlKey, double step) {
		if (ctrlKey) {
			OpticalComponent oc = parent.selectedComponent;
			if (oc != null) {
				// move the object and pan the view simultaneously
				if (vertical) {
					oc.values.yPos += step;
					parent.programValues.yOffset += step;
				} else {
					oc.values.xPos += step;
					parent.programValues.xOffset += step;
				}
				oc.writeObjectControls();
			}
		} else {
			// just pan the view
			if (vertical) {
				programValues.yOffset += step;
			} else {
				programValues.xOffset += step;
			}
		}
	}

	protected void processZoom(boolean ctrlKey, boolean shiftKey, double step) {
		OpticalComponent oc = parent.selectedComponent;
		if (shiftKey && oc != null) {
			oc.values.angle = (oc.values.angle + (step * 100 + 720)) % 360;
			oc.writeObjectControls();
		} else if (ctrlKey && oc != null) {
			oc.values.lensRadius += step;
			oc.writeObjectControls();
		} else {
			programValues.dispScale *= (1 + step);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		//parent.p("paintcomponent: " + name + " " + testCount);
		//testCount += 1;
		int w = getWidth();
		int h = getHeight();
		rayTraceProcessCore(w, h,false);
		g.drawImage(parent.image, 0, 0, null);
	}

	// allows drawing any size
	void drawData(Graphics g, int x, int y) {
		rayTraceProcessCore(x, y,false);
		g.drawImage(parent.image, 0, 0, null);
	}

	void rayTraceProcess(boolean paint) {
		if (!paint || parent.currentTab() == tabValue) {
			// parent.p("repainting: " + name + " " + testCount);
			// testCount += 1;
			int w = getWidth();
			int h = getHeight();
			if (paint) {
				repaint();
			} else {
				rayTraceProcessCore(w, h,false);
			}
		}
	}

	void rayTraceProcessCore(int w, int h, boolean forceFocus) {
		// parent.p("raytraceprocesscore: " + name);
		if (updateGraphicBuffer(w, h)) {
			parent.unselectButton.setEnabled(parent.selectedComponent != null);
			Graphics2D bg = (Graphics2D) parent.image.getGraphics();
			if (programValues.antialias) {
				RenderingHints rh = new RenderingHints(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				bg.addRenderingHints(rh);
			}
			Color bgColor = (hasFocus || forceFocus)?new Color(
					programValues.inverse ? programValues.colorLowBackground
							: programValues.colorHighBackground):
								programValues.inverse ?Common.noFocusInverse:
								Common.noFocusHi;
			bg.setColor(bgColor);
			bg.fillRect(0, 0, parent.image.getWidth(), parent.image.getHeight());
			if (programValues.beamWidth > 1) {
				bg.setStroke(new BasicStroke((int) programValues.beamWidth));
			}
			if (programValues.showGrid) {
				rayTraceComputer.drawGrid(bg);
				rayTraceComputer.drawBaselines(bg);
			}
			rayTraceComputer.drawLenses(bg);
			rayTraceComputer.traceRays(bg, false);
			bg.dispose();
		}
	}

	boolean updateGraphicBuffer(int x, int y) {
		boolean success = false;
		if (x > 0 && y > 0) {
			success = true;
			if (parent.image == null || parent.xSize != x || parent.ySize != y) {
				parent.xSize = x;
				parent.ySize = y;
				parent.image = new BufferedImage(x, y,
						BufferedImage.TYPE_INT_RGB);
				parent.xCenter = parent.xSize / 2;
				parent.yCenter = parent.ySize / 2;
			}
		}
		return success;
	}

	void updateStatusBar(int mx, int my, boolean erase) {
		String s = String.format("       %10s   %10s      %10s", "", "", "");
		if (!erase) {
			Vector sp = parent.displayToSpaceOffset(new Vector(mx, my));
			String sx = dispRoundNum(sp.x);
			String sy = dispRoundNum(sp.y);
			String sz = dispRoundNum(programValues.dispScale);
			s = String.format("Pos: X:%10s Y:%10s Magnification:%10s", sx, sy, sz);
		}
		parent.statusLabel.setText(s);
	}

	String dispRoundNum(double v) {
		String result;
		double av = abs(v);
		if (av > 100 || av < .1) {
			result = String.format("%9.4e", v);
		} else {
			result = String.format("%9.4f", v);
		}
		return result;
	}

	public boolean hasMouse() {
		return mouseInside;
	}

	void setMouseInside(boolean inside) {
		mouseInside = inside;
		if (!inside) {
			updateStatusBar(0, 0, true);
		}
	}

	void updateDisplay() {
		rayTraceProcess(true);
	}

	void handleMouseMove(MouseEvent evt) {
		int mx = evt.getX();
		int my = evt.getY();
		Vector p = parent.displayToSpace(mx, my).translate(programValues.xOffset,programValues.yOffset );
		//p.translate(programValues.xOffset,programValues.yOffset );
		//double sx = dx.v + programValues.xOffset;
	//	double sy = dy.v + programValues.yOffset;
		if (testMouseInsideLens(p, true) != null) {
			this.setCursor(handCursor);
		} else {
			this.setCursor(defaultCursor);
		}
		updateStatusBar(evt.getX(), evt.getY(), false);
		StringBuilder sb = new StringBuilder();
		if (ocUnderMouse != null) {
			sb.append(ocUnderMouse.values.name);
			sb.append(" ");
		}
		sb.append(String.format("{%s,%s}", parent.formatNum(p.x),
				parent.formatNum(p.y)));
		setToolTipText(sb.toString());
	}

	void handleMouseDrag(MouseEvent evt) {
		updateStatusBar(evt.getX(), evt.getY(), false);
		int mx = evt.getX();
		int my = evt.getY();
		Vector p = parent.displayToSpace(mx, my);
		if (!(shiftKey || ctrlKey)) {
			programValues.xOffset = -p.x + parent.mousePressX;
			programValues.yOffset = -p.y + parent.mousePressY;
		} else if (parent.selectedComponent != null && (shiftKey || ctrlKey)) {
			parent.selectedComponent.values.xPos = p.x + parent.mousePressX;
			parent.selectedComponent.values.yPos = p.y + parent.mousePressY;
			parent.selectedComponent.writeObjectControls();
		}

		rayTraceProcess(true);
	}

	void detectKeys(MouseEvent evt) {
		shiftKey = (evt.getModifiers() & InputEvent.SHIFT_MASK) != 0;
		ctrlKey = (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
		altKey = (evt.getModifiers() & InputEvent.ALT_MASK) != 0;
	}

	void handleMouseWheelEvent(MouseWheelEvent evt) {
		int mx = evt.getX();
		int my = evt.getY();
		detectKeys(evt);
		double v = evt.getWheelRotation();
		double mv = v * ((altKey) ? 1 : 5);
		if (parent.selectedComponent != null) {
			if (shiftKey) {
				parent.selectedComponent.values.angle = (parent.selectedComponent.values.angle + (mv + 720)) % 360;
				parent.selectedComponent.writeObjectControls();
			} else if (ctrlKey) {
				parent.selectedComponent.values.lensRadius -= mv * .01;
				parent.selectedComponent.writeObjectControls();
			} else {
				programValues.dispScale *= 1 - mv * .02;
			}

		} else {
			programValues.dispScale *= 1 - mv * .02;
		}

		rayTraceProcess(true);
		updateStatusBar(mx, my, false);
		evt.consume();
	}

	void handleMousePressEvent(MouseEvent evt) {
		parent.undoPush();
		requestFocus();
		boolean doubleClick = evt.getClickCount() == 2;
		boolean isPopup = evt.isPopupTrigger();
		this.setCursor(moveCursor);
		int mx = evt.getX();
		int my = evt.getY();
		detectKeys(evt);
		Vector op = parent.displayToSpace(mx, my);
		Vector offset = op.translate(programValues.xOffset,programValues.yOffset);
		//offset.translate(programValues.xOffset,programValues.yOffset);
		OpticalComponent p = testMouseInsideLens(offset, isPopup);
		if (p != null) {
			//parent.p("clicked with xPos1: " + p.values.xPos);
			parent.setSelectedComponent(p);
			//parent.p("clicked with xPos2: " + p.values.xPos);
			parent.mouseTarget = p;
		}
		if (!(shiftKey || ctrlKey)) {
			parent.mousePressX = op.x + programValues.xOffset;
			parent.mousePressY = op.y + programValues.yOffset;
		} else if (p != null) {
			if (ctrlKey || shiftKey) {
				parent.mousePressX = -op.x + p.values.xPos;
				parent.mousePressY = -op.y + p.values.yPos;
			}
			rayTraceProcess(true);
		}
		if (!(shiftKey || ctrlKey || altKey)) {
			if (isPopup && isVisible()) {
				this.setCursor(defaultCursor);
				parent.popupMouseX = evt.getX();
				parent.popupMouseY = evt.getY();
			}
			if (doubleClick) {
				parent.lineAnalysis.nearestLineProperties(parent.mousePressX,
						parent.mousePressY);
			}
		}
	}

	OpticalComponent testMouseInsideLens(Vector mp, boolean isPopup) {
		ArrayList<OpticalComponent> lensSet = new ArrayList<OpticalComponent>();
		for (OpticalComponent oc : parent.componentList) {
			if (oc.inside(mp, oc.mouseProximityPolygon)) {
				lensSet.add(oc);
			}
		}
		if (lensSet.size() == 0) {
			ocUnderMouse = null;
			return null;
		} else {
			// cycle between overlapped lenses
			// as the user presses the mouse repeatedly
			// but only if this is not a context-menu mouse press
			// and only if no modifier keys are pressed
			if(!(isPopup | shiftKey | ctrlKey | altKey)) {
				parent.overlappedLensSelector += 1;
			}
			ocUnderMouse = lensSet.get(parent.overlappedLensSelector % lensSet.size());
			return ocUnderMouse;
		}
	}

	
	void handleMouseReleaseEvent(MouseEvent evt) {
		this.setCursor(defaultCursor);
		if (parent.selectedComponent != null) {
			parent.selectedComponent.snapToGrid();
			if (parent.selectedComponent != null) {
				parent.selectedComponent.writeObjectControls();
			}
		}
		rayTraceProcess(true);
		if (evt.isPopupTrigger()) {
			parent.popupMouseX = evt.getX();
			parent.popupMouseY = evt.getY();
		}
	}

	protected void contextHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("Mouse related:\n");
		sb.append("  Click once: select object\n");
		sb.append("  Click more: cycle through overlapping objects\n");
		sb.append("  Double-click: list properties of nearest line\n");
		sb.append("  Drag mouse: pan display\n");
		sb.append("  Drag mouse with Shift or Ctrl key: move selected object\n");
		sb.append("  Mouse wheel: zoom display\n");
		sb.append("  Mouse wheel with Shift key: rotate selected object\n");
		sb.append("  Mouse wheel with Ctrl key: rescale selected object\n");
		sb.append("Keyboard related:\n");
		sb.append("  Tab: Move forward through all program controls\n");
		sb.append("  Shift|Tab: Move in reverse through all program controls\n");
		sb.append("  Alt-D: Design tab\n");
		sb.append("  Alt-C: Configure tab\n");
		sb.append("  Alt-T: Table tab\n");
		sb.append("  Alt-H: Help tab\n");
		sb.append("  F1: Concise help dialog (this dialog)\n");
		sb.append("  M or Context-menu key: context menu\n");
		sb.append("  Enter (over object): select object under cursor\n");
		sb.append("  Enter (outside objects): List properties of nearest line\n");
		sb.append("  L: [L]ist properties of nearest line (even inside objects)\n");
		sb.append("  U: [U]nselect all objects\n");
		sb.append("  O: Cycle through [O]bject selections\n");
		sb.append("  Up/down/left/right arrow keys: pan display\n");
		sb.append("  Ctrl|Arrow or Shift|Arrow keys: move selected object\n");
		sb.append("  +/- or Home/End: zoom display in/out\n");
		sb.append("  Ctrl|(+/-) or Ctrl|(Home/End): resize selected object\n");
		sb.append("  Shift|(+/-) or Shift|(Home/End): rotate selected object\n");
		sb.append("Most of the above with Alt key: slower change\n");
		sb.append("This information is also in the Help file, under\n\"Using the mouse and keyboard\".");
		parent.showNotifyMessageFormatted(sb.toString(), "Context help");
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				try {
					if (e.getComponent().isVisible()) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				} catch (Exception ex) {
				}
			}
		});
	}
}

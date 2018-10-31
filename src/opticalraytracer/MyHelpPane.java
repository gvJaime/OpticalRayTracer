package opticalraytracer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
final public class MyHelpPane extends JPanel {
	int invocationCount = 0;
	protected JTextField findTextField;
	JScrollPane helpScrollPane;
	OpticalRayTracer parent;
	Stack<HelpState> undoStack;
	Stack<HelpState> redoStack;
	Document doc;
	String oldSearch = "";
	int oldPos = 0;
	Object oldHighlight = null;
	Highlighter highlighter;
	Highlighter.HighlightPainter highlightPainter;
	JTextPane helpTextPane;
	JButton undoButton, redoButton;
	JButton launchButton;
	JFrame separateFrame;
	MyHelpPane childHelp = null;

	/**
	 * Create the panel.
	 */
	public MyHelpPane(OpticalRayTracer p, int count) {
		invocationCount = count + 1;
		parent = p;
		setLayout(new BorderLayout(0, 0));
		setFocusable(true);
		setRequestFocusEnabled(true);
		helpTextPane = new JTextPane();
		helpTextPane.setFocusable(false);
		helpTextPane.setBackground(java.awt.Color.white);
		helpTextPane.setContentType("text/html;charset=UTF-8");
		helpTextPane.setEditorKit(new HTMLEditorKit());
		helpTextPane.setEditable(false);
		// helpTextPane.setFont(new Font("monospaced",Font.PLAIN,16));
		// try DejaVu Sans?
		// helpTextPane.setFocusable(false);
		helpTextPane
				.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
					public void hyperlinkUpdate(
							javax.swing.event.HyperlinkEvent evt) {
						manageHyperlinks(evt);
					}
				});
		doc = helpTextPane.getDocument();
		undoStack = new Stack<>();
		redoStack = new Stack<>();
		highlighter = helpTextPane.getHighlighter();
		highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(200, 255, 200));

		helpScrollPane = new JScrollPane();
		add(helpScrollPane, BorderLayout.CENTER);

		helpScrollPane.setViewportView(helpTextPane);

		JPanel controlPanel = new JPanel();
		add(controlPanel, BorderLayout.SOUTH);

		launchButton = new JButton("");
		launchButton.setIcon(new ImageIcon(MyHelpPane.class
				.getResource("/opticalraytracer/icons/system-help.png")));
		launchButton.setToolTipText("Launch copy of help in separate window");
		launchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchHelpInFrame();
			}
		});
		controlPanel.setLayout(new MigLayout("", "[]push[][][][]push"));
		controlPanel.add(launchButton, "cell 0 0,alignx left");

		findTextField = new JTextField();
		findTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		findTextField
				.setToolTipText("<html>Quick search: type a search string,<br/>press Enter to find the next case</html>");
		findTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				manageHelpTextField(e);
			}
		});

		redoButton = new JButton("");
		redoButton.setToolTipText("Redo previously undone action");
		redoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				redoPop();
			}
		});

		undoButton = new JButton("");
		undoButton.setToolTipText("Undo prior action");
		undoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoPop();
			}
		});
		undoButton.setIcon(new ImageIcon(MyHelpPane.class
				.getResource("/opticalraytracer/icons/edit-undo.png")));
		controlPanel.add(undoButton, "cell 2 0,alignx right");
		redoButton.setIcon(new ImageIcon(MyHelpPane.class
				.getResource("/opticalraytracer/icons/edit-redo.png")));
		controlPanel.add(redoButton, "cell 3 0,alignx right");
		controlPanel.add(findTextField, "cell 4 0,alignx right");
		findTextField.setColumns(10);
		setupHelp();
		setButtons();
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				findTextField.requestFocus();
				findTextField.requestFocusInWindow();
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				onStart();
			}
		});
	}

	protected void onStart() {
		helpScrollPane.getVerticalScrollBar().setValue(
				parent.programValues.helpScrollPos);
		findTextField.requestFocus();
		findTextField.requestFocusInWindow();

	}

	protected void onQuit() {
		parent.programValues.helpScrollPos = helpScrollPane
				.getVerticalScrollBar().getValue();
		if (childHelp != null) {
			childHelp.onQuit();
			separateFrame.setVisible(false);
			separateFrame.dispose();
		}
	}

	public void setScrollPos() {
		parent.programValues.helpScrollPos = helpScrollPane
				.getVerticalScrollBar().getValue();
	}

	protected void launchHelpInFrame() {
		if (invocationCount > 4) {
			parent.showNotifyMessage(
					"Yes, you really can launch any number of\nchild help windows. But ... should you? :)",
					"More Help");
		}
		setScrollPos();
		separateFrame = new JFrame();
		childHelp = new MyHelpPane(parent, invocationCount);
		separateFrame.getContentPane().add(childHelp);
		separateFrame.setSize(getSize());
		separateFrame.setTitle(parent.frame.getTitle());
		separateFrame.setIconImage(parent.frame.getIconImage());
		separateFrame.setVisible(true);
	}

	String readFile(String path) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream is = OpticalRayTracer.class.getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	// help resource related
	void setupHelp() {
		String fn = "/opticalraytracer/helpresources/HelpText.html";
		try {
			String s = readFile(fn);
			s = s.replaceAll("#version#", parent.VERSION);
			String ud = parent.initManager.userPath.replaceAll("\\\\",
					"\\\\\\\\");
			ud = ud.replaceAll("\\$","\\\\\\\\$"); 
			s = s.replaceAll("#userdir#", ud);
			helpTextPane.setText(s);
			helpTextPane.select(0, 0);
			URL url = parent.getClass().getResource(fn).toURI().toURL();
			((HTMLDocument) helpTextPane.getDocument()).setBase(url);
		} catch (Exception e) {
			System.out.println("setupHelp: " + e);
			e.printStackTrace();
		}
	}

	// manageHyperlinks tries to find and launch a browser
	void manageHyperlinks(HyperlinkEvent evt) {
		URL url = evt.getURL();
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String surl = evt.getURL().toString();
			if (surl.matches("http://.*")) {
				// if (parent.applet) {
				// parent.setStatus("Applets can't follow hyperlinks");
				// } else {
				try {
					java.awt.Desktop.getDesktop().browse(
							java.net.URI.create(surl));
				} catch (Exception e) {
					System.out.println(e);
				}
				// }
			} else if (surl.matches(".*#.*")) { // bookmark?
				try {
					undoPush();
					helpTextPane.scrollToReference(url.getRef());

				} catch (Exception e) {
					System.out.println(e);
				}
			} else if (surl.matches(".*file:.*")) {
				String lensSpec = surl.replaceFirst(
						"(?ms).*?file:\\s*(\\S*?)\\s*", "$1");
				String s = readFile(lensSpec);
				parent.setClipboardContents(s);
			}
		}
	}

	void undoPop() {
		if (undoStack.size() > 0) {
			redoPush();
			removeOldHighlight();
			HelpState hs = undoStack.pop();
			helpScrollPane.getVerticalScrollBar().setValue(hs.scrollBar);
			try {
				oldHighlight = highlighter.addHighlight(hs.selectStart,
						hs.selectEnd, highlightPainter);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setButtons();

		} else {
			Common.beep();
		}

	}

	void redoPop() {
		if (redoStack.size() > 0) {
			undoPush();
			removeOldHighlight();
			HelpState hs = redoStack.pop();
			helpScrollPane.getVerticalScrollBar().setValue(hs.scrollBar);
			try {
				oldHighlight = highlighter.addHighlight(hs.selectStart,
						hs.selectEnd, highlightPainter);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setButtons();

		} else {
			Common.beep();
		}

	}

	void undoPush() {
		int ss = 0;
		int se = 0;
		Highlight[] ha = highlighter.getHighlights();
		if (ha.length > 0) {
			ss = ha[0].getStartOffset();
			se = ha[0].getEndOffset();
		}
		int sbp = helpScrollPane.getVerticalScrollBar().getValue();
		undoStack.push(new HelpState(sbp, ss, se));
		setButtons();

	}

	void redoPush() {
		int ss = 0;
		int se = 0;
		Highlight[] ha = highlighter.getHighlights();
		if (ha.length > 0) {
			ss = ha[0].getStartOffset();
			se = ha[0].getEndOffset();
		}
		int sbp = helpScrollPane.getVerticalScrollBar().getValue();
		redoStack.push(new HelpState(sbp, ss, se));
		setButtons();

	}

	void setButtons() {
		undoButton.setEnabled(undoStack.size() > 0);
		redoButton.setEnabled(redoStack.size() > 0);
	}

	void removeOldHighlight() {
		if (oldHighlight != null) {
			highlighter.removeHighlight(oldHighlight);
			oldHighlight = null;
		}

	}

	void manageHelpTextField(KeyEvent evt) {
		// String code = KeyEvent.getKey_Text(evt.getKeyCode());
		// if a function key, go to main command switchboard
		// if (code.matches("F\\d")) {
		// parent.handleKeyPressed(evt);
		// } else {
		try {

			removeOldHighlight();
			doc = helpTextPane.getDocument();
			int len = doc.getLength();
			String content = doc.getText(0, len).toLowerCase();
			String search = findTextField.getText().toLowerCase();
			if (!search.equals(oldSearch)) {
				oldPos = 0;
			}

			oldSearch = search;
			int p = content.indexOf(search, oldPos);
			if (p == -1) {
				oldPos = 0;
				p = content.indexOf(search, oldPos);
			}

			if (p >= 0) { // if found
				int slen = search.length();
				Rectangle r = helpTextPane.modelToView(p);
				// aim for the middle of the screen
				int pos = r.y - helpScrollPane.getHeight() / 2;
				// but don't try for the impossible
				pos = (pos < 0) ? 0 : pos;
				helpScrollPane.getVerticalScrollBar().setValue(pos);
				// now highlight the found text in our nonfocused text pane
				oldHighlight = highlighter.addHighlight(p, p + slen,
						highlightPainter);
				oldPos = p + 1; // to find next case
				undoPush();
			} else {
				Common.beep();
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		// }

	}
}

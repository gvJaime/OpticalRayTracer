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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

final public class ControlManager implements ProgramControl {

	JTextField numberField = null;
	JTextField nameField = null;
	JCheckBox box = null;
	JRadioButton radio;
	JComboBox<String> comboBox = null;
	ArrayList<JRadioButton> radioButtonArray;
	static int[] radioButtonList = new int[] { -1, -1, -1 };
	int radioButtonIndex = -1;
	OpticalRayTracer parent;
	double sens = 1;
	double dmin, dmax;
	int imin, imax;
	private String tag = "";

	public ControlManager(JTextField field, OpticalRayTracer p, String tag) {
		nameField = field;
		init(p, tag);
		assignHandlers(field);
		nameField.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				int kcode = evt.getKeyCode();
				// Common.p("" + evt);
				if (kcode == KeyEvent.VK_ENTER) {
					evt.consume();
					updateAllControls();
				}
			}
		});
		nameField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateAllControls();
			}
		});
	}

	public ControlManager(double sens, double min, double max,
			JTextField field, OpticalRayTracer p, String tag) {
		this.numberField = field;
		dmin = min;
		dmax = max;
		this.sens = sens;
		init(p, tag);
		assignHandlers(field);
	}

	public ControlManager(final JCheckBox box, OpticalRayTracer p,
			final String tag) {
		this.box = box;
		init(p, tag);
		box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				updateAllControls();
			}
		});
	}

	public ControlManager(final JRadioButton b, OpticalRayTracer p,
			final String tag) {
		radio = b;
		init(p, tag);
		int i = 0;
		// choose which radio button this is
		ArrayList<ArrayList<JRadioButton>> radioArrayList = new ArrayList<>(
				Arrays.asList(parent.typeRadioButtonList));
		for (ArrayList<JRadioButton> bl : radioArrayList) {
			if (bl.indexOf(b) != -1) {
				radioButtonIndex = i;
				radioButtonArray = bl;
			}
			i++;
		}
		radio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				radioButtonList[radioButtonIndex] = radioButtonArray
						.indexOf(radio);
				updateAllControls();
			}
		});
	}

	public ControlManager(JComboBox<String> cb, OpticalRayTracer p, String tag) {
		comboBox = cb;
		init(p, tag);
		//comboBox.removeAll();
		init(p, tag);
		for (String s : Common.curvatures) {
			comboBox.addItem(s);
		}
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!parent.suppressCombo) {
					updateAllControls();
				}
			}
		});
	}

	public String getTag() {
		return tag;
	}

	void init(OpticalRayTracer p, String tag) {
		parent = p;
		this.tag = tag;
	}

	public void reset() {
	};

	public void assignHandlers(Component comp) {
		comp.addMouseWheelListener(new java.awt.event.MouseWheelListener() {

			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				handleMouseWheelMoved(evt);
			}
		});
		comp.addKeyListener(new java.awt.event.KeyAdapter() {

			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				handleKeyPressed(evt);
			}
		});
	}

	void enable(boolean enabled) {
		if (numberField != null) {
			numberField.setEnabled(enabled);
			if (!enabled) {
				numberField.setText("(Select a lens)");
			}
		} else if (nameField != null) {
			nameField.setEnabled(enabled);
			if (!enabled) {
				nameField.setText("");
			}
		} else if (box != null) {
			box.setEnabled(enabled);
		} else if (radio != null) {
			for (JRadioButton jb : radioButtonArray) {
				jb.setEnabled(enabled);
			}
		} else if (comboBox != null) {
			comboBox.setEnabled(enabled);
		}
	}

	public String getValue() {
		String result = "";
		if (numberField != null) {
			result = numberField.getText();
		} else if (nameField != null) {
			result = nameField.getText();
		} else if (box != null) {
			result = "" + box.isSelected();
		} else if (comboBox != null) {
			result = "" + comboBox.getSelectedIndex();
		} else {
			result = "" + radioButtonList[radioButtonIndex];
		}
		// parent.p("controlmanager get value: tag: " + tag + ", value:" +
		// result);
		return result;
	}

	public void setValue(String s) {
		// parent.p("controlmanager set value: tag: " + tag + ", value:" + s);
		if (numberField != null) {
			double v = 0;
			try {
				v = LocaleHandler.getDouble(s,
						LocaleHandler.localeDecimalSeparator);
			} catch (Exception e) {
			}
			numberField.setText(parent.formatNum(v));
		} else if (nameField != null) {
			nameField.setText(s);

		} else if (box != null) {
			box.setSelected(s.matches("(?i).*true.*"));
		} else if (comboBox != null) {
			int v = Integer.parseInt(s);
			parent.suppressCombo = true;
			comboBox.setSelectedIndex(v);
			parent.suppressCombo = false;
		} else if (radio != null) {
			int v = Integer.parseInt(s);
			radioButtonArray.get(v).setSelected(true);
			radioButtonList[radioButtonIndex] = v;
		}
	}

	void setDoubleValue(double v) {
		try {
			numberField.setText(parent.formatNum(v));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	double getDoubleValue() {
		double v = 0;
		try {
			v = LocaleHandler.getDouble(numberField.getText(),
					LocaleHandler.localeDecimalSeparator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	void setBooleanValue(boolean v) {
		try {
			box.setSelected(v);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean getBooleanValue() {
		boolean v = false;
		try {
			v = box.isSelected();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	private void handleKeyPressed(KeyEvent evt) {
		int n = 0;
		double sign = 0;
		double v = sens;
		boolean consume = true;
		v = (evt.isShiftDown()) ? v * 0.1 : v;
		v = (evt.isAltDown()) ? v * 0.1 : v;
		int kcode = evt.getKeyCode();
		if (kcode == KeyEvent.VK_ENTER) {
			sign = 1;
		} else if (kcode == KeyEvent.VK_HOME) {
			n = 100;
			sign = 1;
		} else if (kcode == KeyEvent.VK_END) {
			n = -100;
			sign = 1;
		} else if (kcode == KeyEvent.VK_PAGE_UP) {
			n = 10;
			sign = 1;
		} else if (kcode == KeyEvent.VK_PAGE_DOWN) {
			n = -10;
			sign = 1;
		} else if (kcode == KeyEvent.VK_DOWN) {
			n = -1;
			sign = 1;
		} else if (kcode == KeyEvent.VK_UP) {
			n = 1;
			sign = 1;
		} else if (kcode == KeyEvent.VK_ESCAPE) {
			n = 0;
			sign = -1;
		} else {
			consume = false;
		}
		if (consume) {
			evt.consume();
		}
		handleIncrement(n, sign, v);
		evt.consume();
	}

	private void handleMouseWheelMoved(MouseWheelEvent evt) {
		double v = sens;
		v = (evt.isShiftDown()) ? v * 0.1 : v;
		v = (evt.isAltDown()) ? v * 0.1 : v;
		handleIncrement(-evt.getWheelRotation(), 1, v);
		evt.consume();
	}

	void handleIncrement(int n, double sign, double sv) {
		if (sign != 0) {
			if (numberField != null) {
				String text = numberField.getText();
				double dv = 0;
				try {
					dv = LocaleHandler.getDouble(text,
							LocaleHandler.localeDecimalSeparator);
				} catch (Exception e) {
					System.out.println(getClass().getName() + ": Error: " + e);
				}
				dv += (n * sv);
				dv *= sign;
				dv = min(dmax, dv);
				dv = max(dmin, dv);
				String s = parent.formatNum(dv);
				numberField.setText(s);
				// parent.p("handleincrement setting value: " + s);
			}
			updateAllControls();

		}
	}

	private void updateAllControls() {
		parent.undoPush();
		if (parent != null) {
			parent.readProgramControls();
			if (parent.selectedComponent != null) {
				parent.selectedComponent.readObjectControls();
			}
			if (parent.currentTab() == Common.TAB_TABLE) {
				parent.dataTableDisplay.updateDisplay();
			}
		}
	}

}

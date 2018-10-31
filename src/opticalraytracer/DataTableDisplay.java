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

import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
final public class DataTableDisplay extends JTable {
	OpticalRayTracer parent;
	DataTableModel dataTableModel;
	int hScroll, vScroll;
	
	public DataTableDisplay(OpticalRayTracer p) {
		parent = p;
		Font origfont = getFont();
		// change everything except the size
		Font f = new Font(Font.MONOSPACED, Font.PLAIN,origfont.getSize());
		setFont(f);
		getTableHeader().setFont(f);
		dataTableModel = new DataTableModel(p, this);
		setModel(dataTableModel);
		updateDisplay();
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				updateDisplay();
			}

			@Override
			public void focusLost(FocusEvent e) {
				hScroll = parent.tableScrollPane.getHorizontalScrollBar()
						.getValue();
				vScroll = parent.tableScrollPane.getVerticalScrollBar()
						.getValue();
			}
		});
		setToolTipText("Double-click: copy line to clipboard");
		addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		        JTable table =(JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        if (me.getClickCount() == 2) {
		            LineData ld = dataTableModel.getRowData(row);
		            if(ld != null) {
		            	parent.lineAnalysis.copyLineToClipboard(ld);
		            }
		        }
		    }
		});
	}

	protected void updateDisplay() {

		dataTableModel.updateDisplay();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				parent.tableScrollPane.getHorizontalScrollBar().setValue(
						hScroll);
				parent.tableScrollPane.getVerticalScrollBar().setValue(vScroll);
			}
		});
	}

}

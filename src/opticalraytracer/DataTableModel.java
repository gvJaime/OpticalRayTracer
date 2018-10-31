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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

@SuppressWarnings("serial")
final public class DataTableModel extends AbstractTableModel {
	OpticalRayTracer parent;
		ArrayList<ArrayList<String>> array;
		String[] header;
		int[] columnWidths;
		boolean[] rightJust;
		JTable table;
		FontMetrics fm;

		public DataTableModel(OpticalRayTracer p, JTable table) {
			parent = p;
			this.table = table;
			JTableHeader hd = table.getTableHeader(); 
			hd.setBackground(new Color(0xc0d0c0));
			hd.setFont(new Font("Arial",Font.BOLD,12));
			fm = table.getFontMetrics(table.getFont());
			array = new ArrayList<>();
			header = parent.lineAnalysis.header;
			columnWidths = new int[header.length];
			rightJust = new boolean[header.length];
		}
		
		protected boolean getRightJust(int i) {
			boolean result = false;
			if(i >= 0 && i < rightJust.length) {
				result = rightJust[i];
			}
			return result;
		}
		
		// this is a goofy but necessary way to
		// pad the contents on table cells
		protected String padString(String s) {
			return " " + s + " ";
		}

		protected ArrayList<String> makeRow(LineData ld) {
			ArrayList<String> row = new ArrayList<>();
			row.add(padString(ld.fromEvent));
			row.add(padString(ld.toEvent));
			row.add(padString(ld.from));
			row.add(padString(ld.to));
			row.add(padString(ld.type));
			for (double v : ld.numericValues()) {
				row.add(padString(parent.formatNum(v)));
			}
			
			return row;
		}

		protected void updateDisplay() {
			if (parent.rayTraceComputer != null) {
				parent.rayTraceComputer.traceRays(null, true);
				if (parent.rayTraceComputer.lineList != null && parent.rayTraceComputer.lineList.size() > 0) {
					final int limit = parent.programValues.tableLineLimit;
					int len = parent.rayTraceComputer.lineList.size();
					int top = (len > limit) ? limit : len;
					String s = "Displaying " + top + " of " + len + " lines";
					if (parent.tableDataLabel != null) {
						parent.tableDataLabel.setText(s);
					}
					array.clear();
					int marg = 8;
					// this only works because monospace font is in use
					int cw = (int) (fm.stringWidth("X"));
					
					for (int n = 0; n < columnWidths.length; n++) {
						columnWidths[n] = header[n].length() * cw + marg;
					}
					int lines = 0;
					for (LineData ld : parent.rayTraceComputer.lineList) {
						ArrayList<String> row = makeRow(ld);
						for (int n = 0; n < row.size(); n++) {
							String ss = row.get(n);
							columnWidths[n] = max(ss.length() * cw + marg,
									columnWidths[n]);
						}
						array.add(row);
						lines += 1;
						if (lines > limit) {
							break;
						}
					}
					int ch = fm.getHeight();
					for (int n = 0; n < getColumnCount(); n++) {
						table.getColumnModel().getColumn(n)
								.setMinWidth(columnWidths[n]);
						DataTableCellRenderer rr = new DataTableCellRenderer(rightJust);
						table.getColumnModel().getColumn(n)
								.setCellRenderer(rr);
					}
					// initialize the right-justify array
					for (int n = 0; n < getColumnCount(); n++) {
						String ts = array.get(0).get(n);
						// if the field contains any uppercase alphas
						// then left-justify, otherwise right
						rightJust[n] = !ts.matches(".*[A-Z].*");
					}
					table.setRowHeight(ch+8);
				}
			}
		}
		
		protected LineData getRowData(int row) {
			LineData result = null;
			if(parent.rayTraceComputer.lineList != null && row >= 0 && row <= parent.rayTraceComputer.lineList.size()) {
			result =  parent.rayTraceComputer.lineList.get(row);
			}
			return result;
		}

		@Override
		public String getColumnName(int column) {
			return header[column];
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return array.size();
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return header.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return array.get(rowIndex).get(columnIndex);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	
}

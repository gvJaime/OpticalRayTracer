package opticalraytracer;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
class DataTableCellRenderer extends JLabel implements TableCellRenderer {
	Color evenColor;
	Color oddColor;
	Color[] rowColors;
	// an array of right-justification signals
	boolean[] rightJust;

	public DataTableCellRenderer(boolean[] rightJust) {
		this.rightJust = rightJust;
		setOpaque(true);
		evenColor = new Color(0xffffff);
		oddColor = new Color(0xf0f0f0);
		rowColors = new Color[] { evenColor, oddColor };
	}

	@Override
	public java.awt.Component getTableCellRendererComponent(
			javax.swing.JTable table, java.lang.Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null) {
			setText((String) value);
		}
		setOpaque(true);
		setBackground(rowColors[row % 2]);
		setHorizontalAlignment(rightJust[column] ? JLabel.RIGHT : JLabel.LEFT);
		setFont(table.getFont());
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}

		return this;
	}

}
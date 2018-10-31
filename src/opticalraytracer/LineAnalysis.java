package opticalraytracer;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

final public class LineAnalysis {
	OpticalRayTracer parent;
	LineData closestLine;
	double minValue;
	String[] header = new String[] { "From","To", "Source", "Destination",
			"DestinationType", "FromX", "FromY", "ToX", "ToY", "DeltaX",
			"DeltaY", "Magnitude", "BeamAngle", "SurfaceNormalAngle",
			"WavelengthNM" };

	String[] css = {"<style type=\"text/css\">",
			"   body {",
			"     font-family:monospace;",
			"   }",
			"   table {",
			"     border-collapse:collapse;",
			"   }",
			"   table * {",
			"     border:1px solid gray;",
			"     white-space:nowrap;",
			"   }",
			"   td {",
			"     text-align:right;",
			"   }",
			"   td.lj {",
			"     text-align:left;",
			"   }",
			"   tr:nth-child(even) {",
			"     background: #ffffff;",
			"   }",
			"   tr:nth-child(odd) {",
			"     background: #f0f0f0;",
			"   }",
			"   th {",
			"     text-align:center;",
			"     font-weight:bold;",
			"     background:#c0d0c0;",
			"   }",
			"  </style>" };

	String meta = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n";

	public LineAnalysis(OpticalRayTracer p) {
		parent = p;
	}

	protected String fmtNum(double v) {
		return parent.formatNum(v);
	}

	protected String makeRow(LineData ld, String token) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> array = makeRow(ld);
		boolean start = true;
		for(String s : array) {
			if(!start) {
				sb.append(token);
			}
			start = false;
			sb.append(s);
		}
		return sb.toString();
	}
	
	protected ArrayList<String> makeRow(LineData ld) {
		ArrayList<String> array = new ArrayList<>();
		array.add(ld.fromEvent);
		array.add(ld.toEvent);
		array.add(ld.from);
		array.add(ld.to);
		array.add(ld.type);
		for (double v : ld.numericValues()) {
			array.add(parent.formatNum(v));
		}
		return array;
	}
	
	protected boolean isRightJust(int n) {
		boolean rj = true;
		if(parent.dataTableDisplay.dataTableModel != null) {
			boolean[] rightJust = parent.dataTableDisplay.dataTableModel.rightJust;
			if(n >= 0 && n < rightJust.length) {
				rj = rightJust[n];
			}
		}
		return rj;
	}

	protected String createHTMLTable(boolean linefeeds) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><th>");
		sb.append(makeHeader("</th><th>"));
		sb.append("</th></tr>");
		if (linefeeds) {
			sb.append("\n");
		}
		for (LineData ld : parent.rayTraceComputer.lineList) {
			sb.append("<tr>");
			ArrayList<String> sa = makeRow(ld);
			int cn = 0;
			for(String s : sa) {
				boolean rj = isRightJust(cn);
				String cls = (rj)?"":"class = \"lj\"";
				String ws = Common.wrapTag("td",s,cls,false);
				sb.append(ws);
				cn += 1;
			}
			sb.append("</tr>");
			if (linefeeds) {
				sb.append("\n");
			}
			
		}
		String result = "";
		result = Common.wrapTag("table", sb.toString(),
				"cellspacing=\"0\" cellpadding=\"2\"", linefeeds);
		result = Common.wrapTag("body", result, "", linefeeds);
		sb = new StringBuilder();
		sb.append("<head>\n");
		sb.append(meta);
		for (String s : css) {
			sb.append(s);
			sb.append("\n");
		}
		sb.append("</head>\n");
		sb.append(result);
		result = sb.toString();
		result = Common.wrapTag("html", result, "", linefeeds);
		return result;
	}

	protected String makeHTMLTable(boolean linefeeds) {
		parent.rayTraceComputer.traceRays(null, true);
		return createHTMLTable(linefeeds);
	}

	protected String makeCSVTable() {
		parent.rayTraceComputer.traceRays(null, true);
		StringBuilder sb = new StringBuilder();
		sb.append(makeHeader("\t"));
		sb.append("\n");
		for (LineData ld : parent.rayTraceComputer.lineList) {
			sb.append(makeRow(ld,"\t"));
			sb.append("\n");
		}
		return sb.toString();
	}

	protected String makeHeader(String token) {
		StringBuilder sb = new StringBuilder();
		int n = 0;
		int len = header.length;
		for (String s : header) {
			sb.append(s);
			n += 1;
			if (n < len) {
				sb.append(token);
			}
		}
		return sb.toString();
	}

	protected void nearestLineProperties(double mx, double my) {
		//Common.p("nearestlineproperties");
		closestLine = null;
		parent.rayTraceComputer.traceRays(null, true);
		minValue = 1e9;
		for (LineData ld : parent.rayTraceComputer.lineList) {
			compare(ld, mx, my);
		}
		if (closestLine != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%-24s: %13s%13s\n", "Property", "x", "y"));
			sb.append("----------------------------------------------------\n");
			sb.append(String.format("%-24s: {%12s,%12s}\n", "Origin",
					fmtNum(closestLine.a.x), fmtNum(closestLine.a.y)));
			sb.append(String.format("%-24s: {%12s,%12s}\n", "Destination",
					fmtNum(closestLine.b.x), fmtNum(closestLine.b.y)));
			sb.append(String.format("%-24s: {%12s,%12s}\n", "Line length",
					fmtNum(closestLine.b.x - closestLine.a.x),
					fmtNum(closestLine.b.y - closestLine.a.y)));
			sb.append(String.format("%-24s: %26s\n", "Magnitude",
					fmtNum(closestLine.m)));
			sb.append(String.format("%-24s: %26s°\n", "Beam Angle",
					fmtNum(closestLine.ar)));
			sb.append(String.format("%-24s: %26s°\n", "Dest. Surface Normal",
					fmtNum(closestLine.sa)));
			sb.append(String.format("%-24s: %26s\n", "Wavelength NM",
					fmtNum(closestLine.wavelength)));
			
			sb.append(String
					.format("%-24s: %26s\n", "From", closestLine.fromEvent));
			sb.append(String
					.format("%-24s: %26s\n", "To", closestLine.toEvent));
			sb.append(String.format("%-24s: %26s\n", "Source",
					closestLine.from));
			sb.append(String.format("%-24s: %26s\n", "Destination", closestLine.to));
			sb.append(String.format("%-24s: %26s\n", "Destination Type",
					closestLine.type));
			showInfoDialog(sb.toString(), "Line Properties");
		} else {
			parent.showNotifyMessage("No Nearby Line", "Line Properties");
		}
	}
	
	protected void showInfoDialog(String message, String title) {
		JTextArea ta = new JTextArea(message);
		JButton copyButton = new JButton("Copy to clipboard");
		copyButton.setToolTipText("Copy this line to the system clipboard as a tab-separated record");
		copyButton.addActionListener(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
					copyNearestLineToClipboard();	
					}
				}
				);
		Object content = new Object[] {ta,copyButton};
		ta.setBackground(parent.frame.getBackground());
		Font f = new Font("Monospaced", Font.PLAIN, 11);
		ta.setFont(f);
		JOptionPane.showMessageDialog(parent.frame, content, parent.appName + ": " + title,
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	protected void copyLineToClipboard(LineData ld) {
		if(ld != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(makeHeader("\t"));
			sb.append("\n");
			sb.append(makeRow(ld,"\t"));
			sb.append("\n");
			parent.clipboardCopyString(sb.toString());
		}
		else {
			Common.beep();
		}
	}
	
	protected void copyNearestLineToClipboard() {
		copyLineToClipboard(closestLine);
	}

	protected void compare(LineData ld, double mx, double my) {
		double lx = Common.xCoordinateOnLine(mx, my, ld.a.x, ld.a.y, ld.b.x, ld.b.y);
		double ly = Common.yCoordinateOnLine(mx, my, ld.a.x, ld.a.y, ld.b.x, ld.b.y);
		double m = Common.distanceToLine(mx, my, ld.a.x, ld.a.y, ld.b.x, ld.b.y);
		if (Common.inBounds(lx, ly, ld.a.x, ld.a.y, ld.b.x, ld.b.y)) {
			// update closest line
			if (minValue > m) {
				closestLine = ld;
				minValue = m;
			}
		}
	}

	
}

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
import static java.lang.Math.pow;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

final public class LocaleHandler {
	
	static final String localeDecimalSeparator = "" + new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();

	static double getDouble(String s, String decSep) {
		double v = 0;
		double pow = 0;
		String exp = "";
		try {
			if (s != null) {
				s = s.replaceAll("[.,]", decSep);
				Locale l = Locale.getDefault();
				NumberFormat nf = NumberFormat.getInstance(l);
				s = s.toLowerCase();
				s = s.trim();
				String[] array = s.split("e");
				if (array.length > 1) {
					// get signed exponent
					exp = array[1].trim().replaceAll("\\+", "");
					if (exp.length() > 0) {
						pow = Double.parseDouble(exp);
					}
				}
				v = nf.parse(array[0]).doubleValue();
				v = v * pow(10, pow);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
	}

	static int getInt(String s) {
		int v = 0;
		Locale l = Locale.getDefault();
		NumberFormat nf = NumberFormat.getInstance(l);
		try {
			v = nf.parse(s).intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return v;
	}

	static String formatDouble(double v, int decimals) {
		Locale l = Locale.getDefault();
		String result = "";
		if (abs(v) >= 1e-3 && abs(v) < 1e7) {
			result = String.format(l, "%." + decimals + "f", v);
		} else {
			result = String.format(l, "%." + decimals + "e", v);
		}
		return result;
	}

	static String formatDouble(double v) {
		Locale l = Locale.getDefault();
		String result = "";
		if (abs(v) >= 1e-3) {
			result = String.format(l, "%f", v);
		} else {
			result = String.format(l, "%e", v);
		}
		return result;
	}

	static String formatInt(int v) {
		Locale l = Locale.getDefault();
		return String.format(l, "%d", v);
	}
}

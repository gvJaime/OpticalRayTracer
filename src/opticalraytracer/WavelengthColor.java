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

import static java.lang.Math.*;

// This class performs all dispersion-related computations and conversions

final public class WavelengthColor {
	static final double redNM = 650;
	static final double violetNM = 400;
	static final double dispersionPivotNM = 589.3;
	static final double dispersionFactor = 5e5;

	double r;
	double g;
	double b;
	double wvl;
	MyColor col;

	public WavelengthColor(double h) {
		// provide an equivalent wavelength for h
		wvl = Common.ntrp(h, 0, 1, redNM, violetNM);
		
		// adjust to red-violet range in HSV spectrum
		h *= .75;

		// hToRGB provides a spectrum between red and violet
		// for arguments between 0 and .75
		col = hToRGB(h);
		this.r = col.getRed() / 255.0;
		this.g = col.getGreen() / 255.0;
		this.b = col.getBlue() / 255.0;
	}

	// with hue component of HSV, creates RGB at full saturation

	static  MyColor hToRGB(double h) {
		double hp = h * 6;
		double c = 1;
		double x = c * (1 - abs(hp % 2.0 - 1));
		MyColor rgb;
		switch ((int) hp) {
		case 0:
			rgb = new MyColor(c, x, 0.0);
			break;
		case 1:
			rgb = new MyColor(x, c, 0.0);
			break;
		case 2:
			rgb = new MyColor(0.0, c, x);
			break;
		case 3:
			rgb = new MyColor(0.0, x, c);
			break;
		case 4:
			rgb = new MyColor(x, 0.0, c);
			break;
		case 5:
			rgb = new MyColor(c, 0.0, x);
			break;
		default:
			rgb = new MyColor(0.0, 0.0, 0.0);
			break;
		}
		return rgb;
	}
	
	// dior = ior + ((dp - w) * 5e5) / (abbe * dp * w^2)
	static double dispersionIndex(double ior, double wavelength, double abbe) {
		return ior + ((dispersionPivotNM - wavelength) * dispersionFactor)
				/ (abbe * dispersionPivotNM * wavelength * wavelength);
	}

}

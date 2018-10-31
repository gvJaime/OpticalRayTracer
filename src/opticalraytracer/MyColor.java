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

import java.awt.*;

@SuppressWarnings("serial")
final public class MyColor extends Color {

	// use alpha if present but set alpha = 0xff if not
	public MyColor(int rgb) {
		super((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,rgb & 0xff,(((rgb >> 24 & 0xff) == 0)?0xff:rgb >> 24 & 0xff));
	}

	public MyColor(double r, double g, double b, double a) {
		super((int) (r * 255), (int) (g * 255), (int) (b * 255),
				(int) (a * 255));
	}

	public MyColor(double r, double g, double b) {
		super((int) (r * 255), (int) (g * 255), (int) (b * 255));
	}

	public MyColor(int r, int g, int b) {
		super(r, g, b);
	}

	public MyColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	public MyColor(final int rgb, int alpha) {
		super((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,rgb & 0xff, alpha);
	}
	
	public String toString() {
		return String.format("{%d,%d,%d}" , getRed(),getGreen(),getBlue());
	}
}

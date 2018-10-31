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

final public class Vector {

	double x = 0, y = 0;

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector(Vector p) {
		x = p.x;
		y = p.y;
	}

	public Vector() {
	}

	public void assign(Vector v) {
		x = v.x;
		y = v.y;
	}

	public Vector translate(Vector p) {
		return new Vector(x + p.x, y + p.y);
	}

	public Vector translate(double px, double py) {
		return new Vector(x + px, y + py);
	}

	public Vector translateSub(Vector p) {
		return new Vector(x - p.x, y - p.y);
	}

	public Vector scale(double scale) {
		return new Vector(x * scale, y * scale);
	}

	public Vector scale(double xs, double ys) {
		return new Vector(x * xs, y * ys);
	}

	public Vector rotate(double x, double y, double angleRadians) {
		double xx = x * cos(angleRadians) - y * sin(angleRadians);
		double yy = y * cos(angleRadians) + x * sin(angleRadians);
		return new Vector(xx, yy);
	}

	public Vector rotate(double angleRadians) {
		double xx = x * cos(angleRadians) - y * sin(angleRadians);
		double yy = y * cos(angleRadians) + x * sin(angleRadians);
		return new Vector(xx, yy);
	}

	public Vector rotate(Vector v, double angleRadians) {
		double xx = v.x * cos(angleRadians) - v.y * sin(angleRadians);
		double yy = v.y * cos(angleRadians) + v.x * sin(angleRadians);
		return new Vector(xx, yy);
	}

	public Vector mul(double v) {
		return new Vector(x * v, y * v);
	}

	public Vector mul(Vector v) {
		return new Vector(x * v.x, y * v.y);
	}

	public Vector div(double v) {
		return new Vector(x / v, y / v);
	}

	public Vector div(Vector v) {
		return new Vector(x / v.x, y / v.y);
	}

	public Vector add(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}

	public Vector add(double v) {
		return new Vector(x + v, y + v);
	}

	public Vector sub(Vector v) {
		return new Vector(x - v.x, y - v.y);
	}

	public Vector sub(double v) {
		return new Vector(x - v, y - v);
	}

	public Vector negate() {
		return new Vector(-x, -y);
	}

	public Vector normalize() {
		// don't try to normalize a zero vector
		if (x != 0 || y != 0) {
			double m = m();
			return new Vector(x/m,y/m);
		} else {
			return new Vector(0, 0);
		}
	}

	// create a scaled vector from polar coordinates

	static public Vector polar(double m, double a) {
		return new Vector(m * cos(a), m * sin(a));
	}

	// create a unit vector from a polar angle

	static public Vector polar(double a) {
		return new Vector(cos(a), sin(a));
	}

	public double angle() {
		return atan2(y, x);
	}

	public double m() {
		return sqrt(x * x + y * y);
	}

	// dot product

	public double dot(Vector v) {
		return x * v.x + y * v.y;
	}

	// mark an instance as being in error

	public static Vector invalidState() {
		return new Vector(Double.NaN, Double.NaN);
	}

	// test for invalid condition

	public boolean isValid() {
		return (!Double.isNaN(x) && !Double.isNaN(y));
	}

	public String toString() {
		return String.format("{%f,%f}", x, y);
	}

}

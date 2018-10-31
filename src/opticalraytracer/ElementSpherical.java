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

import java.util.ArrayList;
import java.util.Iterator;

final public class ElementSpherical implements ElementBase {
	OpticalRayTracer parent;
	private double srValue = -1;
	private double lrValue = -1;
	private double bValue = -1;
	private double epsilon = 1e-8;
	// private int testCount = 0;
	private ArrayList<Vector> points;
	private int pointsSize = 4;

	public ElementSpherical(OpticalRayTracer p) {
		parent = p;
		points = new ArrayList<Vector>();
		for (int i = 0; i < pointsSize; i++) {
			points.add(new Vector());
		}
	}

	public ArrayList<Vector> getPoints() {
		return points;
	}

	/*
	 * ------------------------------------------------------
	 * 
	 * begin import from
	 * ../python_equation_generator/new_circular_equation_solutions.py
	 * 
	 * pa1 - pa3 = x,y intersection 1, x,y intersection 2
	 * 
	 * pb0, pb1 = profile generators for display
	 * 
	 * pc0 = first derivative of the curve profile for optical computations
	 * 
	 * ------------------------------------------------------
	 */

	// From Python script:
	// /netbackup/data/java2/OpticalRayTracer_eclipse/python_equation_generator/new_spherical_equation_solutions.py

	private double pa0(double r, double s, double x_1, double y_1) {
		return -(s * s * y_1 + s * x_1 + sqrt(s
				* s
				* (r * r * s * s + r * r - s * s * x_1 * x_1 + 2 * s * x_1
						* y_1 - y_1 * y_1)))
				/ (s * (s * s + 1));
	}

	private double pa1(double r, double s, double x_1, double y_1) {
		return -(s * s * y_1 + s * x_1 + sqrt(s
				* s
				* (r * r * s * s + r * r - s * s * x_1 * x_1 + 2 * s * x_1
						* y_1 - y_1 * y_1)))
				/ (s * s + 1);
	}

	private double pa2(double r, double s, double x_1, double y_1) {
		return (-s * (s * y_1 + x_1) + sqrt(s
				* s
				* (r * r * s * s + r * r - s * s * x_1 * x_1 + 2 * s * x_1
						* y_1 - y_1 * y_1)))
				/ (s * (s * s + 1));
	}

	private double pa3(double r, double s, double x_1, double y_1) {
		return (-s * (s * y_1 + x_1) + sqrt(s
				* s
				* (r * r * s * s + r * r - s * s * x_1 * x_1 + 2 * s * x_1
						* y_1 - y_1 * y_1)))
				/ (s * s + 1);
	}

	private double pb(double y, double r, double x_1, double y_1) {
		return -x_1 - sqrt(-(-r + y + y_1) * (r + y + y_1));
	}

	private double pd(double y, double r, double y_1) {
		return -sqrt(-(-r + y + y_1) * (r + y + y_1)) * (y + y_1)
				/ ((-r + y + y_1) * (r + y + y_1));
	}

	/*
	 * ------------------------------------------------------
	 * 
	 * end import from
	 * ../python_equation_generator/new_circular_equation_solutions.py
	 * 
	 * ------------------------------------------------------
	 */

	public void intersections(OpticalComponent oc, boolean leftSide,
			Vector op1, Vector op2) {
		// Common.p("---------- intersections ------------");

		for (Vector p : points) {
			p.assign(Vector.invalidState());
		}

		double angleRadians = oc.angleRadians();

		Vector thr, tc, p1, p2;
		double s;

		// a somewhat hacky way to avoid the on-axis zero problem
		while (true) {
			thr = new Vector(-oc.signedThickness(leftSide), 0)
					.rotate(angleRadians);
			tc = new Vector(oc.xPos(), oc.yPos()).translate(thr);
			p1 = new Vector(op1).translateSub(tc).rotate(-angleRadians);
			p2 = new Vector(op2).translateSub(tc).rotate(-angleRadians);

			// the all-important line slope value
			// which must not approach zero
			s = (p2.y - p1.y) / (p2.x - p1.x);

			if (abs(s) >= epsilon) {
				break;
			}
			angleRadians += epsilon;
		}
		
		updateFactors(oc, leftSide);

		Vector pt;
		int len_d2 = points.size() / 2;
		int n = 0;
		Iterator<Vector> ic = points.iterator();
		// must compute four possible intersections
		// each with either sign of the bias variable
		// this is the only way to allow multiple internal reflections
		// on the surface of a reflector
		double sb = bValue;
		double sbb;
		while (ic.hasNext()) {
			sbb = (n >= len_d2) ? -sb : sb;
			pt = ic.next();
			pt.x = pa0(srValue, s, p1.x - sbb, p1.y) + p1.x;
			pt.y = pa1(srValue, s, p1.x - sbb, p1.y) + p1.y;
			pt = ic.next();
			pt.x = pa2(srValue, s, p1.x + sbb, p1.y) + p1.x;
			pt.y = pa3(srValue, s, p1.x + sbb, p1.y) + p1.y;
			n += 2;
		}

		// test, then restore original rotation and position
		for (Vector p : points) {
			if (leftSide ^ oc.radiusSign(leftSide) < 0) {
				p.x = (p.x < 0) ? Double.NaN : p.x;
			} else {
				p.x = (p.x > 0) ? Double.NaN : p.x;
			}
			p.assign(p.rotate(angleRadians).translate(tc));
		}
	}

	public double lensProfileXforY(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		updateFactors(oc, leftSide);
		return lensProfileXforYCore(oc, leftSide, y, cx);
	}

	public double lensProfileXforYCore(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		double x = pb(y, srValue, cx + bValue, 0) * oc.radiusSign(leftSide)
				- oc.thickness();
		if (leftSide) {
			x = -x;
		}
		// testCount += 1;
		// if (testCount % 101 == 0) {
		// parent.p("spherical profile: left: " + leftSide + ", y: " + y
		// + ", x: " + x + ", b: " + bValue);
		// }
		return x;
	}

	// 1st derivative
	public double lensProfileDXforY(OpticalComponent oc, boolean leftSide,
			boolean entering, double y) {
		updateFactors(oc, leftSide);
		double dx = pd(y, srValue, 0) * oc.radiusSign(leftSide);
		if (leftSide) {
			dx = -dx;
		}
		// Common.p("spherical DX: left: " + leftSide + ", needRotation: " +
		// needRotation + ", y: " + y + ", sr: " +
		// srValue + ", dx: " + dx);
		return dx;
	}

	private void updateFactors(OpticalComponent oc, boolean leftSide) {
		double newSR = oc.sphereRadius(leftSide);
		double newLR = oc.lensRadius();
		double newSRA = abs(newSR);
		if (srValue != newSR || lrValue != newLR) {
			// mValue = pm(newSR);
			bValue = pb(newLR, newSRA, 0, 0);
			// parent.p("spherical updateFactors recalc: r: " + newSR + ", lr: "
			// + newLR + ", b = " + bValue);
			srValue = newSR;
			lrValue = newLR;
		}
	}

}

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

final public class ElementHyperbolic implements ElementBase {
	@SuppressWarnings("unused")
	private OpticalRayTracer parent;
	private double zValue = -1;
	private double mValue = -1;
	private double bValue = -1;
	private double lrValue = -1;
	private double invLrValue = -1;
	private double srValue = -1;
	private double epsilon = 1e-8;
	// private int testCount = 0;
	private ArrayList<Vector> points;
	private int pointsSize = 4;

	/*
	 * This class creates hyperbolic lenses. The basic model is a conic section
	 * of a plane through a unit cone (a cone having a slope of 1). The
	 * curvature factor that controls the lens appearance represents a location
	 * in the Z dimension of the plane that bisects the cone. A curvature factor
	 * (z) of zero creates a triangular slice through the cone's apex.
	 * Successively larger values for z produce increasingly more moderate
	 * curvatures until as z approaches oo, the curvature approaches parabolic.
	 * This class allows full translation and rotation of its objects, a
	 * long-sought goal.
	 */

	public ElementHyperbolic(OpticalRayTracer p) {
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
	 * ../python_equation_generator/new_hyperbolic_equation_solutions.py
	 * 
	 * pa1 - pa3 = x,y intersection 1, x,y intersection 2
	 * 
	 * pb = profile generator for display
	 * 
	 * pd = first derivative of the curve profile for optical computations
	 * 
	 * pm = "M" factor generator
	 * 
	 * ------------------------------------------------------
	 */

	// From Python script:
	// /netbackup/data/java2/OpticalRayTracer_eclipse/python_equation_generator/new_hyperbolic_equation_solutions.py

	private double pa0(double z, double s, double m, double x_1, double y_1) {
		return (-m * m * s * s * y_1 + s * x_1 - sqrt(s
				* s
				* (m * m * s * s * x_1 * x_1 - m * m * s * s * z - 2 * m * m
						* s * x_1 * y_1 + m * m * y_1 * y_1 + z)))
				/ (s * (m * m * s * s - 1));
	}

	private double pa1(double z, double s, double m, double x_1, double y_1) {
		return (-m * m * s * s * y_1 + s * x_1 - sqrt(s
				* s
				* (m * m * s * s * x_1 * x_1 - m * m * s * s * z - 2 * m * m
						* s * x_1 * y_1 + m * m * y_1 * y_1 + z)))
				/ (m * m * s * s - 1);
	}

	private double pa2(double z, double s, double m, double x_1, double y_1) {
		return (-m * m * s * s * y_1 + s * x_1 + sqrt(s
				* s
				* (m * m * s * s * x_1 * x_1 - m * m * s * s * z - 2 * m * m
						* s * x_1 * y_1 + m * m * y_1 * y_1 + z)))
				/ (s * (m * m * s * s - 1));
	}

	private double pa3(double z, double s, double m, double x_1, double y_1) {
		return (-m * m * s * s * y_1 + s * x_1 + sqrt(s
				* s
				* (m * m * s * s * x_1 * x_1 - m * m * s * s * z - 2 * m * m
						* s * x_1 * y_1 + m * m * y_1 * y_1 + z)))
				/ (m * m * s * s - 1);
	}

	private double pb(double y, double z, double m, double x_1, double y_1) {
		return -x_1
				- sqrt(m * m * y * y + 2 * m * m * y * y_1 + m * m * y_1 * y_1
						+ z);
	}

	private double pd(double y, double z, double m, double y_1) {
		return -m
				* m
				* (y + y_1)
				/ sqrt(m * m * y * y + 2 * m * m * y * y_1 + m * m * y_1 * y_1
						+ z);
	}

	private double pm(double z, double r) {
		return -sqrt(r * r + 2 * sqrt(r * r * z));
	}

	/*
	 * ------------------------------------------------------
	 * 
	 * end import from
	 * ../python_equation_generator/new_hyperbolic_equation_solutions.py
	 * 
	 * ------------------------------------------------------
	 */

	public void intersections(OpticalComponent oc, boolean leftSide,
			Vector op1, Vector op2) {

		for (Vector p : points) {
			p.assign(Vector.invalidState());
		}

		double signedScale = oc.signedScale(leftSide);
		double lensRadius = oc.lensRadius();
		double angleRadians = oc.angleRadians();

		Vector thr, tc, p1, p2;
		double s;

		// a somewhat hacky way to avoid the on-axis zero problem
		while (true) {
			thr = new Vector(-oc.signedThickness(leftSide), 0)
					.rotate(angleRadians);
			tc = new Vector(oc.xPos(), oc.yPos()).translate(thr);
			p1 = new Vector(op1).translateSub(tc).rotate(-angleRadians)
					.scale(1 / signedScale, 1 / lensRadius);
			p2 = new Vector(op2).translateSub(tc).rotate(-angleRadians)
					.scale(1 / signedScale, 1 / lensRadius);

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
		// this is the only way to allow multiple reflections
		// on the surface of a reflector
		double sb = (s < 0) ? -bValue : bValue;
		double sbb;

		while (ic.hasNext()) {
			sbb = (n >= len_d2) ? -sb : sb;
			pt = ic.next();
			pt.x = pa0(zValue, s, mValue, p1.x - sbb, p1.y) + p1.x;
			pt.y = pa1(zValue, s, mValue, p1.x - sbb, p1.y) + p1.y;
			pt = ic.next();
			pt.x = pa2(zValue, s, mValue, p1.x + sbb, p1.y) + p1.x;
			pt.y = pa3(zValue, s, mValue, p1.x + sbb, p1.y) + p1.y;
			n += 2;
		}

		double count = 0;
		double minx = 1e6, maxx = -1e6;
		// filter out spurious surface detections
		for (Vector p : points) {
			if (leftSide) {
				p.x = (p.x < 0) ? Double.NaN : p.x;
			} else {
				p.x = (p.x > 0) ? Double.NaN : p.x;
			}
			if (!Double.isNaN(p.x)) {
				minx = min(minx, p.x);
				maxx = max(maxx, p.x);
				count += 1;
			}
		}

		if (!oc.isReflector() && count > 1) {

			for (Vector p : points) {
				p.x = (p.x < 0 && p.x == minx) ? Double.NaN : p.x;
				p.x = (p.x > 0 && p.x == maxx) ? Double.NaN : p.x;
			}
		}

		// parent.p("intersections:");
		for (Vector p : points) {
			// parent.p("" + p);
			p.assign(p.scale(signedScale, lensRadius).rotate(angleRadians)
					.translate(tc));
		}

	}

	// hyperbolic curve profile
	public double lensProfileXforY(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		updateFactors(oc, leftSide);
		return lensProfileXforYCore(oc, leftSide, y, cx);
	}

	// hyperbolic curve profile
	public double lensProfileXforYCore(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		double x = -pb(y / oc.lensRadius(), zValue, mValue, cx + bValue, 0)
				* oc.scale(leftSide) * oc.radiusSign(leftSide) - oc.thickness();
		if (leftSide) {
			x = -x;
		}
		// testCount += 1;
		// if(testCount % 101 == 0) {
		// parent.p("hyperbolic profile: left: " + leftSide + ", y = " + y +
		// ",v = " + x);
		// }
		return x;
	}

	// 1st derivative of curve profile
	public double lensProfileDXforY(OpticalComponent oc, boolean leftSide,
			boolean entering, double y) {

		updateFactors(oc, leftSide);
		// note the square factor divisor
		double dx = pd(y * invLrValue, zValue, mValue, 0) * oc.scale(leftSide)
				* oc.radiusSign(leftSide) * invLrValue;
		if (leftSide) {
			dx = -dx;
		}
		// parent.p("hyperbolic DX: left: " + leftSide + ", needRotation: "
		// + needRotation + ", y: " + y + ", sr: " + srValue + ", dx: "
		// + v);
		return -dx;
	}

	private void updateFactors(OpticalComponent oc, boolean leftSide) {
		double newZ = oc.zValue(leftSide);
		double newSR = oc.sphereRadius(leftSide);
		double newLR = oc.lensRadius();
		if (zValue != newZ || newLR != lrValue || newSR != srValue) {
			mValue = pm(newZ, 1);
			bValue = pb(1, newZ, mValue, 0, 0);
			// testCount += 1;
			// if(testCount % 10 == 0) {
			// parent.p("hyperbolic updateFactors recalc: z: " + newZ
			// + ", m = " + mValue + ", b = " + bValue);
			// }
			zValue = newZ;
			lrValue = newLR;
			invLrValue = 1 / newLR;
			srValue = newSR;
		}
	}
}

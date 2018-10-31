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
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Iterator;

final public class ElementParabolic implements ElementBase {
	OpticalRayTracer parent;
	private double srValue = -1;
	private double lrValue = -1;
	private double mValue = -1;
	private double bValue = -1;
	// private double epsilon = 1e-3;
	double leftMax, rightMax;
	private ArrayList<Vector> points;
	private int pointsSize = 8;
	private double epsilon = 1e-6;

	// private int testCount = 0;

	public ElementParabolic(OpticalRayTracer p) {
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
	 * ../python_equation_generator/new_parabolic_equation_solutions.py
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
	// /netbackup/data/java2/OpticalRayTracer_eclipse/python_equation_generator/new_parabolic_equation_solutions.py

	private double pa0(double s, double x_1, double y_1) {
		return (-2 * s * y_1 - sqrt(4 * (s * s) * x_1 - 4 * s * y_1 + 1) + 1)
				/ (2 * (s * s));
	}

	private double pa1(double s, double x_1, double y_1) {
		return (-2 * s * y_1 - sqrt(4 * (s * s) * x_1 - 4 * s * y_1 + 1) + 1)
				/ (2 * s);
	}

	private double pa2(double s, double x_1, double y_1) {
		return (-2 * s * y_1 + sqrt(4 * (s * s) * x_1 - 4 * s * y_1 + 1) + 1)
				/ (2 * (s * s));
	}

	private double pa3(double s, double x_1, double y_1) {
		return (-2 * s * y_1 + sqrt(4 * (s * s) * x_1 - 4 * s * y_1 + 1) + 1)
				/ (2 * s);
	}

	private double pa4(double s, double x_1, double y_1) {
		return -(2 * s * y_1 + sqrt(-4 * (s * s) * x_1 + 4 * s * y_1 + 1) + 1)
				/ (2 * (s * s));
	}

	private double pa5(double s, double x_1, double y_1) {
		return -(2 * s * y_1 + sqrt(-4 * (s * s) * x_1 + 4 * s * y_1 + 1) + 1)
				/ (2 * s);
	}

	private double pa6(double s, double x_1, double y_1) {
		return (-2 * s * y_1 + sqrt(-4 * (s * s) * x_1 + 4 * s * y_1 + 1) - 1)
				/ (2 * (s * s));
	}

	private double pa7(double s, double x_1, double y_1) {
		return (-2 * s * y_1 + sqrt(-4 * (s * s) * x_1 + 4 * s * y_1 + 1) - 1)
				/ (2 * s);
	}

	private double pb(double y, double x_1, double y_1) {
		return -x_1 + ((y + y_1) * (y + y_1));
	}

	private double pd(double y, double y_1) {
		return 2 * y + 2 * y_1;
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
		int n = 0;
		int len_d2 = points.size() / 2;
		Iterator<Vector> ic = points.iterator();
		// must compute four possible intersections
		// each with either sign of the bias variable
		// this is the only way to allow multiple reflections
		// on the surface of a reflector
		// parent.pr("values: " + p1 + "," + p2 + ", s value:" + s);
		// double radiusSign = oc.radiusSign(leftSide);
		double sb = -1;
		double sbb;

		while (ic.hasNext()) {
			sbb = (n < len_d2) ? -sb : sb;
			pt = ic.next();
			pt.x = pa0(s, p1.x - sbb, p1.y) + p1.x;
			pt.y = pa1(s, p1.x - sbb, p1.y) + p1.y;
			pt = ic.next();
			pt.x = pa2(s, p1.x + sbb, p1.y) + p1.x;
			pt.y = pa3(s, p1.x + sbb, p1.y) + p1.y;
			pt = ic.next();
			pt.x = pa4(s, p1.x - sbb, p1.y) + p1.x;
			pt.y = pa5(s, p1.x - sbb, p1.y) + p1.y;
			pt = ic.next();
			pt.x = pa6(s, p1.x + sbb, p1.y) + p1.x;
			pt.y = pa7(s, p1.x + sbb, p1.y) + p1.y;
			n += 4;
		}

		double slmaxx = leftMax * 2 / abs(signedScale);
		double srmaxx = rightMax * 2 / abs(signedScale);

		double count = 0;
		double minx = 1e6, maxx = -1e6;
		// filter out spurious surface detections
		for (Vector p : points) {

			if (leftSide) {
				p.x = (p.x < 0) ? Double.NaN : p.x;
				p.x = (p.x > slmaxx) ? Double.NaN : p.x;
			} else {
				p.x = (p.x > 0) ? Double.NaN : p.x;
				p.x = (p.x < -srmaxx) ? Double.NaN : p.x;
			}
			if (!Double.isNaN(p.x)) {
				minx = min(minx, p.x);
				maxx = max(maxx, p.x);
				count += 1;
			}
		}

		if (!oc.isReflector() && count > 1) {

			// if count > 1, the maxima
			// or minima are invalid
			for (Vector p : points) {
				if (p.x < 0) {
					p.x = (p.x == minx) ? Double.NaN : p.x;
				} else {
					p.x = (p.x == maxx) ? Double.NaN : p.x;
				}
			}
		}

		// parent.p("intersections for left: " + leftSide);
		for (Vector p : points) {
			p.assign(p.scale(signedScale, lensRadius).rotate(angleRadians)
					.translate(tc));
			// p.scale(signedScale, lensRadius);
			// p.rotate(oc.angleRadians);
			// p.translate(tc);
			// parent.p("" + p);
		}
	}

	public double lensProfileXforY(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		updateFactors(oc, leftSide);
		return lensProfileXforYCore(oc, leftSide, y, cx);

	}

	private double lensProfileXforYCore(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		double x = pb(y, cx + bValue, 0) * mValue * oc.scale(leftSide)
				* oc.radiusSign(leftSide) - oc.thickness();
		if (leftSide) {
			x = -x;
		}
		// testCount += 1;
		// if (testCount % 101 == 0) {
		// parent.p("parabolic profile: left: " + leftSide + ", y: " + y
		// + ", x: " + x + ", b: " + bValue);
		// }
		return x;
	}

	// 1st derivative
	public double lensProfileDXforY(OpticalComponent oc, boolean leftSide,
			boolean entering, double y) {
		// parent.p("parabolic DX left: " + leftSide);
		updateFactors(oc, leftSide);
		double dx = pd(y, 0) * mValue * oc.scale(leftSide)
				* oc.radiusSign(leftSide);
		if (leftSide) {
			dx = -dx;
		}
		// parent.p("parabolic DX: left: " + leftSide + ", needRotation: " +
		// needRotation + ", y: " + y + ", sr: " +
		// srValue + ", dx: " + v);
		return dx;
	}

	private void updateFactors(OpticalComponent oc, boolean leftSide) {
		double newSR = oc.sphereRadius(leftSide);
		double newLR = oc.lensRadius();
		// double newSRA = abs(newSR);
		if (srValue != newSR || lrValue != newLR) {
			mValue = 1 / (newLR * newLR);
			bValue = pb(newLR, 0, 0);

			srValue = newSR;
			lrValue = newLR;
			double a = abs(lensProfileXforYCore(oc, true, 0, 0));
			double b = abs(lensProfileXforYCore(oc, true, newLR, 0));
			leftMax = max(a, b);
			a = abs(lensProfileXforYCore(oc, false, 0, 0));
			b = abs(lensProfileXforYCore(oc, false, newLR, 0));
			rightMax = max(a, b);
			// parent.pr("parabolic updateFactors recalc: sr: " + newSR +
			// ", lr: "
			// + newLR + ", m: " + mValue + ", b = " + bValue + ", maxx: "
			// + maxx + ", minx: " + minx);

		}
	}

}

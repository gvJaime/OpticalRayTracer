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

import java.util.Date;

final public class Common {
	static final double radians = PI / 180;
	static final double degrees = 180 / PI;
	static final int OBJECT_REFRACTOR = 0;
	static final int OBJECT_REFLECTOR = 1;
	static final int OBJECT_ABSORBER = 2;
	static final String[] opticalTypes = new String[] { "Lens", "Mirror",
			"Absorber" };
	
	static final int CURVATURE_SPHERICAL = 0;
	static final int CURVATURE_PARABOLIC = 1;
	static final int CURVATURE_HYPERBOLIC = 2;
	static final int CURVATURE_PLANAR = 3;
	static final String[] curvatures = new String[] {"Spherical", "Parabolic",
			"Hyperbolic", "Planar" };
	static final int TAB_DESIGN = 0;
	static final int TAB_CONFIGURE = 1;
	static final int TAB_TABLE = 2;
	static final int TAB_HELP = 3;

	static int noFocusHiInt = 0xf0f0f0;
	static int noFocusLoInt = 0xa0a0a0;
	static int noFocusInverseInt = 0x202020;
	static MyColor noFocusHi = new MyColor(noFocusHiInt);
	static MyColor noFocusLo = new MyColor(noFocusLoInt);
	static MyColor noFocusInverse = new MyColor(noFocusInverseInt);

	final static void p(String s) {
		s = s.replaceAll("(\\.)(\\d{5})\\d+", "$1$2");
		Date d = new Date();
		System.out.println(d + " : " + s);
	}

	final static String pd(double v) {
		return pf(v * degrees);

	}

	final static String pf(double v) {
		return String.format("%8.4f", v);

	}

	final static String pb(boolean v) {
		return String.format("%5s", "" + v);

	}

	final static void pr(String s) {
		s = s.replaceAll("(\\.)(\\d{3})\\d+","$1$2");
		System.out.println(s);
	}

	final static String getObjectType(int function) {
		return opticalTypes[function];
	}

	// reflection in a plane mirror, angles radians
	
	// http://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector

	final static Vector computeReflectionAngle(Vector via, Vector vsa) {
		return via.sub(vsa.mul(via.dot(vsa)).mul(2));
	}

	final static double ntrp(double x, double xa, double xb, double ya,
			double yb) {
		return ((x - xa) / (xb - xa)) * (yb - ya) + ya;
	}

	final static void beep() {
		Beep.beep();
	}

	// is a given point within the bound formed by
	// line segment (x1,y1) -> (x2,y2)?
	final static boolean inBounds(double x, double y, double x1, double y1,
			double x2, double y2) {
		if (x1 != x2) {
			return ((x1 <= x && x <= x2) || (x2 <= x && x <= x1));
		} else {
			return ((y1 <= y && y <= y2) || (y2 <= y && y <= y1));
		}
	}

	final static String wrapTag(String tag, String data, String also,
			boolean linefeeds) {
		if (linefeeds) {
			return String.format("<%s %s>\n%s\n</%s>\n", tag, also, data, tag);
		} else {
			return String.format("<%s %s>%s</%s>", tag, also, data, tag);
		}
	}

	final static double sign(double v) {
		return (v < 0) ? -1 : 1;
	}

	final static double distanceToLine(double x, double y, double x1,
			double y1, double x2, double y2) {
		// see Sage "OpticalRayTracer_technical_article" worksheet
		return abs(-(x1 - x2) * y + x * (y1 - y2) - x2 * y1 + x1 * y2)
				/ sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
	}

	final static double xCoordinateOnLine(double x, double y, double x1,
			double y1, double x2, double y2) {
		// see Sage "OpticalRayTracer_technical_article" worksheet
		return ((x * (x1 - x2) + y * (y1 - y2)) * (x1 - x2) + (x2 * y1 - x1
				* y2)
				* (y1 - y2))
				/ (pow(x1 - x2, 2) + pow(y1 - y2, 2));
	}

	final static double yCoordinateOnLine(double x, double y, double x1,
			double y1, double x2, double y2) {
		// see Sage "OpticalRayTracer_technical_article" worksheet
		return -((x2 * y1 - x1 * y2) * (x1 - x2) - (x * (x1 - x2) + y
				* (y1 - y2))
				* (y1 - y2))
				/ (pow(x1 - x2, 2) + pow(y1 - y2, 2));
	}

	// (x1,y1) is an arbitrarily distant line beginning
	// (x2,y2) is the point of interest
	// (x1,y1) -> (x2,y2) is line segment to be tested
	// (x3,y3) -> (x4,y4) is lens perimeter segment under test
	// see Sage worksheet "OpticalRayTracer_technical_article"
	// for the derivation
	final static int linesIntersect(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		double divisor = ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));
		if (divisor == 0 || Double.isNaN(divisor)) {
			return -1;
		}
		double x = -((x4 * y3 - x3 * y4) * (x1 - x2) - (x2 * y1 - x1 * y2)
				* (x3 - x4))
				/ divisor;
		double y = -((x4 * y3 - x3 * y4) * (y1 - y2) - (x2 * y1 - x1 * y2)
				* (y3 - y4))
				/ divisor;
		if (Double.isNaN(x) || Double.isNaN(y)) {
			// parent.p("NaN detected in \"inside\" test.");
			return -1;
		}
		return Common.inBounds(x, y, x1, y1, x2, y2)
				&& Common.inBounds(x, y, x3, y3, x4, y4) ? 1 : 0;
	}

	final static double distanceToParallelLine(Vector a, Vector b, Vector p) {
		double lx = xCoordinateOnLine(p.x, p.y, a.x, a.y, b.x, b.y);
		double ly = yCoordinateOnLine(p.x, p.y, a.x, a.y, b.x, b.y);
		if (inBounds(lx, ly, a.x, a.y, b.x, b.y)) {
			return distanceToLine(p.x, p.y, a.x, a.y, b.x, b.y);
		} else {
			return Double.NaN;
		}
	}

	// a1 = incident angle (a2 assumed = 0) radians
	// n1 = incident IOR
	// n2 = media IOR
	final static double snell(double a1, double n1, double n2) {
		return asin(sin(a1) * n1 / n2);
	}
	
	// http://www.cs.cmu.edu/~ph/refr.ps.gz ("derivation of refraction methods")

	// Heckbert, shown on page 4, likely the most efficient of those tested:

	// a1 = incident angle radians
	// a2 = surface normal radians
	// n1 = incident IOR
	// n2 = media IOR
	
	// scalar form
	final static double snell2d(double a1, double a2, double n1, double n2) {
		Vector i = Vector.polar(a1);
		Vector n = Vector.polar(-1,a2);
		return snell2d(i,n,n1,n2).angle();
	}
	
	// vector form
	final static Vector snell2d(Vector i, Vector n, double n1, double n2) {
		double c1 = -n.dot(i);
		// this sign change eliminates much
		// hacky code in the main program
		if (c1 < 0) {
			c1 = -c1;
			n = n.negate();
		}
		double r = n1 / n2;
		double c2 = sqrt(1 - r * r * (1 - c1 * c1));
		return (i.mul(r)).add(n.mul(r * c1 - c2));
	}
}

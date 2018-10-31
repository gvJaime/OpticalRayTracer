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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;

final public class OpticalComponent {
	OpticalRayTracer parent;
	ProgramValues programValues;
	RayTraceComputer rayTraceComputer;
	ObjectValues values;
	ArrayList<Vector> opticalTestPolygon;
	ArrayList<Vector> objectDrawingImage;
	ArrayList<Vector> mouseProximityPolygon;
	Vector farFarAway;
	double objectInsideEpsilon;
	double internalThickness = 0;
	double leftInsideRadius = 0;
	double rightInsideRadius = 0;
	double drawCount = 32;
	boolean valid = false;
	double leftCenter = 0;
	double rightCenter = 0;
	private double angleRadians = 0;
	private double localSurfaceEpsilon = 0;
	static final double maxZValue = 1e8;
	
	//double epsilon = 1e-8;
	private ElementBase[] elements = null;

	public OpticalComponent(OpticalRayTracer p, int function) {
		parent = p;
		rayTraceComputer = parent.rayTraceComputer;
		programValues = parent.programValues;
		String name = nextObjectName(function);
		values = new ObjectValues(name);
		setObjectSize(function);
		valid = true;
		setup();
		writeObjectControls();
	}

	public OpticalComponent(OpticalRayTracer p, int function, String name) {
		parent = p;
		rayTraceComputer = parent.rayTraceComputer;
		programValues = parent.programValues;
		values = new ObjectValues(name);
		renameIfRequired();
		setObjectSize(function);
		valid = true;
		setup();
		writeObjectControls();
	}

	public OpticalComponent(OpticalRayTracer p, String data, int function) {
		parent = p;
		programValues = parent.programValues;
		rayTraceComputer = parent.rayTraceComputer;
		String name = nextObjectName(function);
		values = new ObjectValues(name);
		setObjectSize(function);
		valid = values.setValues(data);
		setup();
		writeObjectControls();
	}

	// set up array of optical element types

	protected void setup() {
		elements = new ElementBase[] { new ElementSpherical(parent),
				new ElementParabolic(parent), new ElementHyperbolic(parent),
				new ElementPlanar(parent) };
		reconfigure();
	}

	protected void setObjectSize(int function) {
		values.function = function;
		if (function == Common.OBJECT_REFLECTOR
				|| function == Common.OBJECT_ABSORBER) {
			values.leftCurvature = Common.CURVATURE_PLANAR;
			values.rightCurvature = Common.CURVATURE_PLANAR;
			values.thickness = .1;
		}
	}

	protected String nextObjectName(int function) {
		parent.componentNames = new HashSet<>();
		for (OpticalComponent oc : parent.componentList) {
			parent.componentNames.add(oc.values.name);
		}
		int n = 1;
		String s = "";
		while (true) {
			s = String.format("%s %d", Common.getObjectType(function), n);
			if (!nameInUse(s)) {
				break;
			}
			n += 1;
		}
		return s;
	}

	protected boolean nameInUse() {
		return parent.componentNames.contains(values.name);
	}

	protected boolean nameInUse(String s) {
		return parent.componentNames.contains(s);
	}

	protected void renameIfRequired() {
		if (nameInUse()) {
			values.name = nextObjectName(values.function);
		}
	}

	// begin common access functions

	ElementBase getElement(boolean leftSide) {
		return elements[leftSide ? values.leftCurvature : values.rightCurvature];
	}

	protected double xPos() {
		return values.xPos;
	}

	protected double yPos() {
		return values.yPos;
	}

	protected double sphereRadius(boolean left) {
		return (left) ? values.leftSphereRadius : values.rightSphereRadius;
	}

	protected double lensRadius() {
		return values.lensRadius;
	}

	protected double offset(boolean left) {
		return (left) ? leftCenter : rightCenter;
	}

	protected double zValue(boolean left) {
		return (left) ? values.leftZValue : values.rightZValue;
	}

	protected double thickness() {
		return internalThickness;
	}

	protected double signedThickness(boolean left) {
		// left - right +
		return (left) ? -internalThickness : internalThickness;
	}

	protected double scale(boolean left) {
		return abs(sphereRadius(left)) - offset(left);
	}

	protected double revScale(boolean left) {
		return abs(sphereRadius(left)) + offset(left);
	}

	protected double signedScale(boolean left) {
		return scale(left) * radiusSign(left);
	}

	protected double radiusSign(boolean left) {
		return (sphereRadius(left) < 0) ? -1 : 1;
	}

	protected double sideSign(boolean left) {
		return (left) ? -1 : 1;
	}

	protected double angleRadians() {
		return angleRadians;
	}

	protected boolean isReflector() {
		return values.function == Common.OBJECT_REFLECTOR;
	}

	protected double halfRadius(boolean left) {
		double sr = abs(sphereRadius(left));
		double lr = lensRadius();
		return sr - sqrt(sr * sr - lr * lr);
	}

	// end common access functions

	protected void reconfigure() {
		// must maintain and update the local copy of this value
		localSurfaceEpsilon = parent.programValues.surfEpsilon;
		// this is the opposite end of the line used in inside-lens calculations
		farFarAway = new Vector(1e6, 1e6);
		limitPositions();
		// modulo to first circle for consistency
		// with large entered angles
		angleRadians = (values.angle % 360.0) * -Common.radians;
		values.thickness = max(values.thickness, 0);
		values.ior = max(values.ior, 1);
		values.lensRadius = abs(values.lensRadius);
		values.rightZValue = min(values.rightZValue, maxZValue);
		values.leftZValue = min(values.leftZValue, maxZValue);
		if (values.symmetrical) {
			values.rightSphereRadius = values.leftSphereRadius;
			values.rightZValue = values.leftZValue;
			values.rightCurvature = values.leftCurvature;
		}
		double lsr = checkRadius(values.leftSphereRadius);
		double rsr = checkRadius(values.rightSphereRadius);
		boolean lch = lsr != values.leftSphereRadius;
		boolean rch = rsr != values.rightSphereRadius;
		parent.leftSphereRadiusTextField.setForeground(lch ? Color.RED
				: Color.BLACK);
		parent.rightSphereRadiusTextField.setForeground(rch ? Color.RED
				: Color.BLACK);
		parent.lensRadiusTextField.setForeground(lch || rch ? Color.RED
				: Color.BLACK);
		values.leftSphereRadius = lsr;
		values.rightSphereRadius = rsr;
		leftCenter = sqrt(lsr * lsr - values.lensRadius * values.lensRadius);
		rightCenter = sqrt(rsr * rsr - values.lensRadius * values.lensRadius);
		int leftSign = (values.leftSphereRadius < 0) ? -1 : 1;
		int rightSign = (values.rightSphereRadius < 0) ? -1 : 1;
		// prevent lens sides from crossing
		internalThickness = values.thickness / 2;
		double leftSurface = (abs(lsr) - leftCenter) * leftSign;
		double rightSurface = (abs(rsr) - rightCenter) * -rightSign;
		leftSurface += internalThickness;
		rightSurface -= internalThickness;
		// an exception to the above if surface is planar
		if(values.leftCurvature == Common.CURVATURE_PLANAR) leftSurface = internalThickness;
		if(values.rightCurvature == Common.CURVATURE_PLANAR) rightSurface = -internalThickness;
		// compute lens center thickness
		values.centerThickness = max(leftSurface - rightSurface , 0);
		// values.centerThickness is included in the 
		// ObjectValues instance only to make it
		// accessible externally -- setting its value has no effect,
		// it is a "write-only" value
		parent.centerThicknessTextField.setText(parent
				.formatNum(values.centerThickness));
		//Common.p("" + centerThickness);
		// compute bias to prevent lens crossover
		double bias = max(rightSurface - leftSurface, 0);
		// assert minimum lens thickness
		internalThickness = internalThickness + bias / 2;
		// don't allow a lens to be thinner than the surface epsilon
		// otherwise the algorithm can't distinguish between its sides
		internalThickness = max(internalThickness,
				parent.programValues.surfEpsilon);
		parent.internalThicknessTextField.setText(parent
				.formatNum(internalThickness * 2));
		parent.internalThicknessTextField.setForeground((bias > 0 ? Color.RED
				: Color.BLACK));
		parent.thicknessTextField.setForeground((bias > 0 ? Color.RED
				: Color.BLACK));
		drawCount = 32;
		double leastRadius = min(abs(values.leftSphereRadius),
				abs(values.rightSphereRadius));
		double v = 1024 * pow(values.lensRadius / leastRadius, 3);
		drawCount = max(drawCount, v);
		objectInsideEpsilon = parent.programValues.surfEpsilon
				* sqrt(values.lensRadius);
		// for comparing with circle-line intersection results
		opticalTestPolygon = createObjectPerimeter((int) drawCount,
				objectInsideEpsilon);
		// for graphic imaging
		objectDrawingImage = createObjectPerimeter((int) drawCount, 0);
		// for mouse object detection
		mouseProximityPolygon = createObjectPerimeter((int) drawCount,
				objectInsideEpsilon * 200);
		// showPerimeter(opticalTestPolygon);
	}

	// this routine establishes whether a point is inside a polygon
	// and also determines how close the point is to the polygon border
	// which makes it very slow

	boolean inside(Vector p1, ArrayList<Vector> polygon, double epsilon) {
		double closest = 1e9;
		double m;
		Vector op = null;
		int crossings = 0;
		int intersect;
		for (Vector p : polygon) {
			if (op != null) {
				intersect = Common.linesIntersect(farFarAway.x, farFarAway.y,
						p1.x, p1.y, op.x, op.y, p.x, p.y);
				m = Common.distanceToParallelLine(p, op, p1);
				if (!Double.isNaN(m)) {
					closest = min(closest, abs(m));
				}
				switch (intersect) {
				case -1:
					return false;
				case 1:
					crossings += 1;
				}
			}
			op = p;
		}
		// parent.p("closest: " + closest);
		boolean inside = (((crossings & 1) != 0) && (closest < epsilon));
		if (inside) {
			// parent.p("item " + values.name + ", p: " + p1 + " is inside.");
		}
		return inside;
	}

	// this function is much faster, it only determines
	// that a point is inside a polygon
	boolean inside(Vector p1, ArrayList<Vector> polygon) {
		if (p1 == null || Double.isNaN(p1.x) || Double.isNaN(p1.y)) {
			return false;
		}
		Vector op = null;
		int crossings = 0;
		int intersect;
		for (Vector p : polygon) {
			if (op != null) {
				intersect = Common.linesIntersect(farFarAway.x, farFarAway.y,
						p1.x, p1.y, op.x, op.y, p.x, p.y);
				switch (intersect) {
				case -1:
					return false;
				case 1:
					crossings += 1;
				}
			}
			op = p;
		}
		boolean inside = (crossings & 1) != 0;
		if (inside) {
			// parent.p("item " + values.name + ", p: " + p1 + " is inside.");
		}
		return inside;
	}

	// debugging tool
	void showPerimeter(ArrayList<Vector> points) {
		StringBuilder sb = new StringBuilder();
		for (Vector p : points) {
			sb.append(String.format("(%f,%f),", p.x, p.y));
		}
		Common.p(sb.toString());
	}

	ArrayList<Vector> createObjectPerimeter(int segments, double epsilon) {
		Vector p;
		ArrayList<Vector> points = new ArrayList<>();
		for (int j = 0; j <= 1; j++) {
			double sgn = (j == 0) ? 1 : -1;
			for (int i = 0; i <= segments; i++) {
				double y = values.lensRadius
						* ((2.0 * i / (double) segments) - 1.0);
				y *= sgn;
				p = lensXforY(y, 0);
				if (p.x < p.y) {
					p.x -= epsilon;
					p.y += epsilon;
				} else {
					p.x += epsilon;
					p.y -= epsilon;
				}
				Vector ca = new Vector().rotate(p.x, y, angleRadians)
						.translate(values.xPos, values.yPos);
				// ca.rotate(p.x, y, angleRadians);
				// ca.translate(values.xPos, values.yPos);
				Vector cb = new Vector().rotate(p.y, y, angleRadians)
						.translate(values.xPos, values.yPos);
				// cb.rotate(p.y, y, angleRadians);
				// cb.translate(values.xPos, values.yPos);
				if (i == 0) {
					if (j == 0) {
						points.add(cb);
					}
				}
				if (j == 0) {
					points.add(ca);
				} else {
					points.add(cb);
				}
			}
		}
		// Collections.reverse(points);
		return points;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("object {" + parent.lineSep);
		sb.append(values.getValues());
		sb.append("}" + parent.lineSep);
		return sb.toString();
	}

	protected void limitPositions() {
		values.xPos = min(values.xPos, programValues.virtualSpaceSize);
		values.xPos = max(values.xPos, -programValues.virtualSpaceSize);
		values.yPos = min(values.yPos, programValues.virtualSpaceSize);
		values.yPos = max(values.yPos, -programValues.virtualSpaceSize);
	}

	double checkRadius(double v) {
		double sign = (v < 0) ? -1 : 1;
		v = abs(v);
		v = max(v, values.lensRadius);
		return v * sign;
	}

	protected void setValues(String s) {
		values.setValues(s);
	}

	protected String getValues() {
		return toString();
	}

	double computeSnap(double v) {
		double sign = 1;
		if (programValues.snapValue != 0) {
			sign = (v < 0) ? -1 : 1;
			v = abs(v);
			double q = programValues.snapValue / 2.0;
			double delta = (v + q) % programValues.snapValue;
			v = v - delta + q;
		}
		return v * sign;
	}

	// force lens to specified X and Y grid value
	void snapToGrid() {
		values.yPos = computeSnap(values.yPos);
		values.xPos = computeSnap(values.xPos);
	}

	protected void writeObjectControls() {
		//parent.p("writeObjectControls1: value: " + values.xPos);
		for (ControlManager cm : parent.objectControlList.values()) {
			String tag = cm.getTag();
			String value = values.getOneValue(tag);
			cm.setValue(value);
		}
		//parent.p("writeObjectControls2: value: " + values.xPos);
		reconfigure();
		parent.enableComponentControls(true);
	}

	protected void readObjectControls() {
		for (ControlManager cm : parent.objectControlList.values()) {
			String tag = cm.getTag();
			String value = cm.getValue();
			values.setOneValue(tag, value);
		}
		// parent.p("readObjectControls2: value: " + values.leftCurvature);
		reconfigure();
		parent.enableComponentControls(true);
		// parent.updateGraphicDisplay();
	}

	Vector profile(boolean leftSide, double ar) {
		double lo = (values.leftSphereRadius < 0) ? -leftCenter : leftCenter;
		double ro = (values.rightSphereRadius < 0) ? -rightCenter : rightCenter;
		double x = (leftSide) ? values.xPos - lo + internalThickness
				: values.xPos + ro - internalThickness;
		Vector v = new Vector(x - values.xPos, 0).rotate(ar).translate(
				values.xPos, values.yPos);
		// v.rotate(ar);
		// v.translate(values.xPos, values.yPos);
		return v;
	}

	void computeIntersections(RayLensIntersection oldrli, OpticalComponent oc,
			boolean leftSide, Vector p1, Vector p2) {
		ElementBase element = getElement(leftSide);

		element.intersections(oc, leftSide, p1, p2);

	}

	double tangent(boolean leftSide, boolean entering, Vector p, double ar,
			boolean reflector) {

		Vector rp = p.translate(-values.xPos, -values.yPos).rotate(-ar);
		// parent.p("tangentA: left: " + leftSide + ", angle: " + ar *
		// Common.degrees);
		double dx = getElement(leftSide).lensProfileDXforY(this, leftSide,
				entering, rp.y);
		// double sr = this.sphereRadius(leftSide);
		// Complex r = new Complex(dx/sr,sr).rotate(ar);
		// p.rotate(ar);
		// parent.p("tangent B: entering: " + entering + ", left: " + leftSide +
		// ", y: " + v.y +
		// ", result: " + qy);
		return dx;
	}

	void drawLens(Graphics2D g) {
		// solve subtle bug caused by user changes to this value
		if(localSurfaceEpsilon != parent.programValues.surfEpsilon) {
			reconfigure();
		}
		Polygon p = new Polygon();
		int col = programValues.colorLensOutline;
		boolean selected = parent.selectedComponent != null
				&& parent.selectedComponent == this;
		if (selected) {
			col = programValues.colorLensSelected;
		} else if (!values.active) {
			col = programValues.colorGrid;
		}
		MyColor cc = new MyColor(col, 255);
		g.setColor(cc);
		boolean drawing = false;
		ComplexInt i = new ComplexInt();
		for (Vector pt : objectDrawingImage) {
			rayTraceComputer.drawScaledLine(pt.x, pt.y, i, g, drawing);
			rayTraceComputer.addToPolygon(p, pt.x, pt.y);
			drawing = true;
		}
		cc = new MyColor(col);
		g.setColor(cc);
		g.fillPolygon(p);
	}

	Vector lensXforY(double y, double ccx) {
		// left
		double a = getElement(true).lensProfileXforY(this, true, y, ccx);
		// right
		double b = getElement(false).lensProfileXforY(this, false, y, ccx);
		return new Vector(a, b);
	}

}

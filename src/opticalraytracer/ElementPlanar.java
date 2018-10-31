package opticalraytracer;

import java.util.ArrayList;

final public class ElementPlanar implements ElementBase {
	OpticalRayTracer parent;
	private ArrayList<Vector> points;
	Vector pt;

	// private double epsilon = 1e-8;

	public ElementPlanar(OpticalRayTracer p) {
		parent = p;
		points = new ArrayList<Vector>();
		pt = new Vector();
		points.add(pt);
	}

	@Override
	public void intersections(OpticalComponent oc, boolean leftSide,
			Vector op1, Vector op2) {

		double angleRadians = oc.angleRadians();

		Vector thr = new Vector(-oc.signedThickness(leftSide), 0)
				.rotate(angleRadians);
		Vector tc = new Vector(oc.xPos(), oc.yPos()).translate(thr);
		Vector p1 = new Vector(op1).translateSub(tc).rotate(-angleRadians);
		Vector p2 = new Vector(op2).translateSub(tc).rotate(-angleRadians);

		pt.x = 0;
		pt.y = Common.ntrp(pt.x, p1.x, p2.x, p1.y, p2.y);
		pt.assign(pt.rotate(angleRadians).translate(tc));
	}

	@Override
	public double lensProfileXforY(OpticalComponent oc, boolean leftSide,
			double y, double cx) {
		double x = (leftSide) ? oc.thickness() : -oc.thickness();
		// this unattractive hack avoids an overflow when x is perfectly vertical
		return x + y * 1e-9;
	}

	@Override
	public double lensProfileDXforY(OpticalComponent oc, boolean leftSide,
			boolean entering, double y) {
		// always vertical, therefore x' = 0
		return 0;
	}

	@Override
	public ArrayList<Vector> getPoints() {
		return points;
	}

}

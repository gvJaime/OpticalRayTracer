package opticalraytracer;

import static java.lang.Math.*;

final public class LineData {
	String from = "", to = "", type = "",fromEvent = "",toEvent = "";
	Vector a, b;
	double dx, dy;
	double m, ar, sa;
	double wavelength;

	public LineData(int ray, int dbeam, double wavelength,
			RayLensIntersection oldrli, RayLensIntersection rli, Vector ia,
			Vector ib, double surfaceAngle, String fromEvent, String toEvent) {
		this.fromEvent = fromEvent;
		this.toEvent = toEvent;
		if (oldrli == null) {
			from = String.format("Origin Ray %d", ray + 1);
			if (wavelength != 0) {
				from += String.format(" Dbeam %d", dbeam + 1);
			}
		} else {
			from = oldrli.lens.values.name;
		}
		if (rli == null) {
			this.to = "Virtual space boundary";
			type = "Domain Limit"; 
		} else {
			this.to = rli.lens.values.name;
			type = Common.getObjectType(rli.function);
		}
		// must deep copy
		this.a = new Vector(ia.x, ia.y);
		this.b = new Vector(ib.x, ib.y);
		dx = ib.x - ia.x;
		dy = ib.y - ia.y;
		m = sqrt(dx * dx + dy * dy);
		this.wavelength = (wavelength == 0)?WavelengthColor.dispersionPivotNM:wavelength;
		ar = atan2(dy, dx) * Common.degrees;
		sa = surfaceAngle * Common.degrees;
		
	}

	public double[] numericValues() {
		return new double[] { a.x, a.y, b.x, b.y, dx, dy, m, ar, sa, wavelength };
	}
}

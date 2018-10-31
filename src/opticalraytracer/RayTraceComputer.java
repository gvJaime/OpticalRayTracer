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
import java.util.Collections;

final public class RayTraceComputer {

	OpticalRayTracer parent;
	ProgramValues programValues;
	ArrayList<LineData> lineList;
	int testCount = 0;
	Vector[] arrowLines;

	public RayTraceComputer(OpticalRayTracer p) {
		parent = p;
		programValues = p.programValues;
		// polygon vertices for arrow
		arrowLines = new Vector[] { new Vector(0, 0), new Vector(-1, .5),
				new Vector(-1, -.5) };

	}

	void drawLenses(Graphics2D g) {
		for (OpticalComponent oc : parent.componentList) {
			oc.drawLens(g);
		}
	}

	void drawGrid(Graphics2D g) {
		Vector p1 = parent.displayToSpaceOffset(new Vector(0, 0));
		Vector p2 = parent.displayToSpaceOffset(new Vector(parent.xSize,
				parent.ySize));
		double fact = 2.0; // increases the density of grid lines
		double e = log(programValues.dispScale * fact) / log(5) - 100.0;
		e = e - (e % 1.0) + 100.0;
		double step = pow(5.0, -e);
		double xstart = gridRound(p1.x, step) - step;
		double xend = p2.x + step;
		double ystart = gridRound(p2.y, step) - step;
		double yend = p1.y + step;
		Color col = new MyColor(programValues.colorGrid);
		g.setColor(col);
		ComplexInt i = new ComplexInt();
		double x, y;
		for (int j = 0; (y = ystart + j * step) <= yend; j++) {
			for (int k = 0; (x = xstart + k * step) <= xend; k++) {
				drawScaledLine(x, ystart, i, g, false);
				drawScaledLine(x, yend, i, g, true);
			}
		}
		for (int j = 0; (x = xstart + j * step) <= xend; j++) {
			for (int k = 0; (y = ystart + k * step) <= yend; k++) {
				drawScaledLine(xstart, y, i, g, false);
				drawScaledLine(xend, y, i, g, true);
			}
		}
	}

	double gridRound(double v, double modulus, boolean roundUp) {
		int sign = (v < 0) ? -1 : 1;
		v = abs(v);
		if (roundUp) {
			v = v + modulus;
		}
		v = v - v % modulus;
		v *= sign;
		return v;
	}

	void fillScaledPoint(Vector p, double radius, Graphics2D g, MyColor col) {
		boolean filled = true;
		// Color alphaColor = new Color(col.getRed(), col.getGreen(),
		// col.getBlue(), 128);
		radius = abs(radius);
		g.setColor(col);
		// int ix,iy;
		ComplexInt ip = scalePoint(p);
		int r = (int) radius;
		if (filled) {
			g.fillOval(ip.x - r / 2, ip.y - r / 2, r, r);
		} else {
			g.drawOval(ip.x - r / 2, ip.y - r / 2, r, r);
		}
	}

	void drawArrowhead(Vector a, double angle, double radius, Graphics2D g,
			Color col) {
		if (a != null) {
			Polygon p = new Polygon();
			for (Vector op : arrowLines) {
				Vector dp = op.scale(radius).rotate(angle).translate(a);
				ComplexInt ip = scalePoint(dp);
				p.addPoint(ip.x, ip.y);
			}
			g.setColor(col);
			g.fillPolygon(p);
		}
	}

	void drawScaledLine(Vector p, ComplexInt op, Graphics2D g, boolean draw) {
		drawScaledLine(p.x, p.y, op, g, draw);
	}

	void drawScaledLine(double x, double y, ComplexInt op, Graphics2D g,
			boolean draw) {
		if (Double.isNaN(x) || Double.isNaN(y)) {
			return;
		}
		y = min(y, parent.programValues.virtualSpaceSize);
		y = max(y, -parent.programValues.virtualSpaceSize);
		x = min(x, parent.programValues.virtualSpaceSize);
		x = max(x, -parent.programValues.virtualSpaceSize);
		ComplexInt sp = scalePoint(x, y);
		if (draw) {
			g.drawLine(op.x, op.y, sp.x, sp.y);
		}
		op.assign(sp);
	}

	void addToPolygon(Polygon p, double x, double y) {

		ComplexInt p1 = scalePoint(x, y);
		p.addPoint(p1.x, p1.y);
	}

	ComplexInt scalePoint(double x, double y) {
		return parent.spaceToDisplay(x, y);
	}

	ComplexInt scalePoint(Vector p) {
		return parent.spaceToDisplay(p.x, p.y);
	}

	double gridRound(double v, double modulus) {
		return gridRound(v, modulus, false);
	}

	void drawBaselines(Graphics2D g) {
		// y = 0 line
		double x1 = 0;
		double x2 = parent.xSize;
		double y = 0;
		Vector p1 = parent.displayToSpaceOffset(new Vector(x1, y));
		Vector p2 = parent.displayToSpaceOffset(new Vector(x2, y));
		Color col = new MyColor(programValues.colorBaseline);
		g.setColor(col);
		ComplexInt i = new ComplexInt();
		drawScaledLine(p1.x, y, i, g, false);
		drawScaledLine(p2.x, y, i, g, true);
		// x = 0 line
		double y2 = parent.ySize;
		double x = 0;
		p1 = parent.displayToSpaceOffset(new Vector(x1, y));
		p2 = parent.displayToSpaceOffset(new Vector(x, y2));
		drawScaledLine(x, p1.y, i, g, false);
		drawScaledLine(x, p2.y, i, g, true);
	}

	public void traceRays(Graphics2D g2d, boolean collectLines) {
		if (collectLines) {
			lineList = new ArrayList<>();
		}
		// parent.p("traceRays: " + testCount);
		// testCount += 1;
		// generate some rays

		double xSource = programValues.xBeamSourceRefPlane;
		double xTarget = programValues.xBeamRotationPlane;
		double ba = -programValues.beamAngle * Common.radians;
		double tba = tan(ba) * (xSource - xTarget);
		double min = programValues.yStartBeamPos;
		double max = programValues.yEndBeamPos;
		double rmin = min + tba;
		double rmax = max + tba;
		double xs = programValues.xBeamSourceRefPlane;
		if (!collectLines) {
			g2d.setColor(new MyColor(programValues.colorLightSource));
			ComplexInt i = new ComplexInt();
			drawScaledLine(xs, rmin, i, g2d, false);
			drawScaledLine(xs, rmax, i, g2d, true);
		}
		if(xSource == xTarget) {
			Common.beep();
			return;
		}
		double count = max(programValues.beamCount, 1);
		double topcount = max(programValues.beamCount - 1, 1);
		for (int ray = 0; ray < count && ray < parent.maxLightRays; ray++) {
			double y = Common.ntrp(ray, 0, topcount, min, max);
			double mya = (programValues.divergingSource) ? 0 : y;
			double myb = y;
			mya += tba;
			Color term = new MyColor(programValues.colorTerminator);
			Color pbcol = new MyColor(programValues.colorBeam);
			Color arrowCol = new MyColor(programValues.colorArrow);
			// an alpha value used by the dispersion calculation
			double pbalpha = pbcol.getAlpha() / 255.0;
			if (programValues.dispersionBeams > 0) {
				for (int dbeam = 0; dbeam < programValues.dispersionBeams; dbeam++) {
					double top = max(programValues.dispersionBeams - 1, 1);
					// h = hue component of HSV
					double h = Common.ntrp(dbeam, 0, top, 0, 1);
					WavelengthColor cw = new WavelengthColor(h);
					// borrow alpha from normal beam color
					MyColor colorWavelength = new MyColor(cw.r, cw.g, cw.b,
							pbalpha);
					traceOneRay(ray, dbeam, xSource, mya, xTarget, myb, g2d,
							parent.componentList, term, colorWavelength,
							arrowCol, cw.wvl, programValues.maxIntersections,
							collectLines);
				}
			} else { // no dispersion
				traceOneRay(ray, 0, xSource, mya, xTarget, myb, g2d,
						parent.componentList, term, pbcol, arrowCol, 0,
						programValues.maxIntersections, collectLines);
			}
		}
	}

	boolean testIntersection(RayLensIntersection r) {
		return ((r.dot > 0) && (r.m > parent.programValues.interLensEpsilon));
	}

	void traceOneRay(int ray, int dbeam, double x1, double y1, double x2,
			double y2, Graphics2D g2d, ArrayList<OpticalComponent> componentList,
			Color terminalColor, Color beamColor, Color arrowColor,
			double wavelength, int maxIntersections, boolean collectLines) {
		//Common.p("------------------------------------------------------");
		//testCount += 1;
		// int interactions = 0;
		double arrowRadius = programValues.intersectionArrowSize
				/ sqrt(programValues.dispScale);
		boolean drawing = true;

		ComplexInt op = new ComplexInt();
		double oldAngle = 0;
		Vector linea = new Vector(x1, y1);
		Vector lineb = new Vector(x2, y2);
		RayLensIntersection oldrli = null;
		RayLensIntersection rli = null;
		double airIOR = 1.0;
		// initial assumption is that we start in open air
		double oldIOR = airIOR;
		boolean intersecting = true;
		// entering means moving from air to medium IOR
		boolean entering = true;
		boolean reflector = false;
		int rays = 0;
		String oldEvent = "Beam Origin";
		String newEvent = oldEvent;
		double initialBeamAngle = atan2(lineb.y - linea.y, lineb.x - linea.x);
		oldAngle = initialBeamAngle;
		//boolean needRotation = false;
		boolean internalReflection = false;
		while (intersecting && rays++ < maxIntersections) {
			//Common.p("------------------------------------------------------");
			ArrayList<RayLensIntersection> intersections = new ArrayList<RayLensIntersection>();
			for (OpticalComponent lens : componentList) {
				if (lens.values.active) {
					for (int i = 0; i <= 1; i++) {
						// lens profile and intersection routines must be
						// kept synchronized as to the lens side
						lens.computeIntersections(oldrli, lens, i == 0, linea,
								lineb);
						for (Vector pt : lens.getElement(i == 0).getPoints()) {
							if (lens.inside(pt, lens.opticalTestPolygon)) {
								intersections.add(new RayLensIntersection(
										linea, lineb, wavelength, pt, i == 0,
										lens));
							}
						}

					} // for i = 0; i <= 1
				}
			} // for lens : lensVec
			if (!collectLines) {
				drawArrowhead(linea, oldAngle, arrowRadius, g2d,
						(wavelength != 0) ? beamColor : arrowColor);
			}
			oldrli = rli;
			rli = null;
			// sort the array based on range, nearest is first in array
			Collections.sort(intersections, new IntersectionSortComparator());
			// choose the next target
			double n = 0;
			double len = intersections.size();
			boolean debugIntersections = false;
			// parent.p("begin testing " + intersections.size());
			for (RayLensIntersection r : intersections) {
				// take the first target from the distance-sorted array
				// that has an acceptable vector magnitude
				// and whose vector points in the current direction
				// parent.p("candidate: m: " + r.m + " dot: "+r.dot);
				// parent.p("dot: " + r.dot + ", distance: " + r.m);
				MyColor cc = null;
				if (debugIntersections) {
					cc = WavelengthColor.hToRGB(n * len / 2);
					cc = new MyColor(cc.getRGB(), 64);
					// parent.p("cc: " + cc);

					if (testIntersection(r)) {
						if (rli == null) {
							rli = r;
							// accepted target: green
							cc = new MyColor(0x00ff00);
						} else {
							// passed up: purple
							cc = new MyColor(0x80ff00ff);
						}
						if (!debugIntersections) {
							break;
						}
					} 
					if (debugIntersections && !collectLines) {
						fillScaledPoint(r.p, 8, g2d, cc);
					}
				} else {
					//Common.p("test r: " + r);
					if (testIntersection(r)) {
						rli = r;
						break;
					}
				}
				n += 1;
			}
			intersecting = (rli != null);
			if (!collectLines) {
				g2d.setColor(beamColor);
			}

			if (intersecting) {
				double n1 = oldIOR, n2 = oldIOR;
				reflector = rli.function == Common.OBJECT_REFLECTOR;
				internalReflection = false;
				if (!collectLines) {
					drawScaledLine(linea, op, g2d, false);
					drawScaledLine(rli.p, op, g2d, true);
				}
				// incident light angle vector
				Vector via = lineb.sub(linea).normalize();
				oldAngle = via.angle();
				double la = rli.lens.angleRadians();
				if (rli.function != Common.OBJECT_ABSORBER) {
					// surface is tangent to lens intersection point
					// so dx is the first derivative, the curvature, at that
					// point
					double dx = rli.lens.tangent(rli.leftSide, entering, rli.p,
							la, reflector);
					Vector vsa = Vector.polar(atan2(1,dx)+PI/2).rotate(la);
					Vector sr = null;
					// test for reflector
					if (reflector) {
						newEvent = "Reflection";
						sr = Common.computeReflectionAngle(via, vsa);
					} else {
						// Snell's Law refraction calculation block
						// a bit more complicated than reflection
						double abbe = rli.lens.values.dispersion;
						double mediaIOR = (wavelength == 0 || abbe == 0) ? rli.lens.values.ior
								: WavelengthColor.dispersionIndex(
										rli.lens.values.ior, wavelength, abbe);
						n2 = (entering) ? mediaIOR : airIOR;
						// the vector form of Snell's Law is required to deal with
						// the case of acute angles between incident and surface normal
						sr = (Common.snell2d(via,vsa,n1, n2));
						//Common.p("entering: " + Common.pb(entering)
						//		+ ", n1/n2: " + Common.pf(n1 / n2) + ", via: "
						//		+ Common.pd(via.angle()) + ", vsa: " + Common.pd(vsa.angle())
						//		+ ", sr: " + Common.pd(sr.angle()) + ", sr-via: "
						//		+ Common.pd(sr.sub(via).angle()));
						// 3. if computed angle is too acute, expect
						// math domain error, which signals
						// total internal reflection (TIR)
						if (!sr.isValid()) {
							// the result exceeds
							// the critical angle of reflection
							// so reflect the beam inside the lens
							internalReflection = true;
							newEvent = "Internal Reflection";
							sr = Common.computeReflectionAngle(via, vsa);
							//Common.p("NaN detected");
						} else {
							newEvent = "Refraction";
						}
					}
					if (collectLines) {
						lineList.add(new LineData(ray, dbeam, wavelength,
								oldrli, rli, linea, rli.p, vsa.angle(), oldEvent,
								newEvent));
					}
					// create new vector using Snell result angle
					linea = new Vector(rli.p);
					lineb = linea.add(sr);
					// a reflector always has entering = true
					if (!reflector && !internalReflection) {
						entering = !entering;
						oldIOR = n2;
					}
				} else {
					newEvent = "Absorption";
					// absorber, so terminate ray trace
					if (!collectLines) {
						// fillScaledPoint(rli.p, pointRad, g2d, terminalColor);
						drawArrowhead(rli.p, oldAngle, arrowRadius, g2d,
								terminalColor);
					} else {
						lineList.add(new LineData(ray, dbeam, wavelength,
								oldrli, rli, linea, rli.p, 0, oldEvent,
								newEvent));
					}
					drawing = false;
					intersecting = false;
				}
			}
			oldEvent = newEvent;
		} // while rays++ < maxIntersections
			// terminal line, to space boundary
		if (drawing) {
			newEvent = (rays >= maxIntersections) ? "Maximum Interaction Limit"
					: "Termination";
			// choose nearest space boundary
			double xq1 = (lineb.x - linea.x > 0) ? programValues.virtualSpaceSize
					: -programValues.virtualSpaceSize;
			double yq1 = Common.ntrp(xq1, linea.x, lineb.x, linea.y, lineb.y);
			double yq2 = (lineb.y - linea.y > 0) ? programValues.virtualSpaceSize
					: -programValues.virtualSpaceSize;
			double xq2 = Common.ntrp(yq2, linea.y, lineb.y, linea.x, lineb.x);
			Vector p;
			if (abs(yq1) > abs(xq2)) {
				p = new Vector(xq2, yq2);
			} else {
				p = new Vector(xq1, yq1);
			}
			if (!collectLines) {
				g2d.setColor(beamColor);
				drawScaledLine(linea, op, g2d, false);
				drawScaledLine(p, op, g2d, true);
				double angle = atan2(p.y - linea.y, p.x - linea.x);
				drawArrowhead(p, angle, arrowRadius, g2d, terminalColor);
			} else {
				lineList.add(new LineData(ray, dbeam, wavelength, oldrli, rli,
						linea, p, 0, oldEvent, newEvent));
			}
		}
	}
}

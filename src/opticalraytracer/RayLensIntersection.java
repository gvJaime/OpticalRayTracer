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


final public class RayLensIntersection {

    int function;
    boolean leftSide;
    Vector a,b,p;
    double wavelength;
    double dot;
    double m;
    OpticalComponent lens = null;

    public RayLensIntersection(Vector a, Vector b, double wavelength, Vector p, boolean leftSide, OpticalComponent lens) {
    	this.a = new Vector(a);
    	this.b = new Vector(b);
    	this.p = new Vector(p);
    	this.wavelength = wavelength;
        this.leftSide = leftSide;
        this.lens = lens;
        this.function = lens.values.function;
        
        double cx = b.x - a.x;
		double cy = b.y - a.y;
		double dx = p.x - a.x;
		double dy = p.y - a.y;
		// current direction by dot product
		dot = dx * cx  + dy * cy;
		// magnitude
		m = dx * dx + dy * dy;
        
    }
    public RayLensIntersection() {
    }
    
    public String toString() {
    	return String.format("ax = %f, ay = %f, bx = %f, by = %f, x = %f, y = %f, dot = %f, m = %f",a.x,a.y,b.x,b.y,p.x,p.y,dot,m);
    }
}

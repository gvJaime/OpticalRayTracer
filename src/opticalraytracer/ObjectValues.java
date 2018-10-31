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

final public class ObjectValues extends ValueManager {
	String name = "";
	double xPos = 0;
	double yPos = 0;
	double lensRadius = 2;
	double leftSphereRadius = 6;
	double rightSphereRadius = 6;
	double thickness = 0; // edge thickness
	double centerThickness = 0; // a write-only value
	double ior = 1.52;
	double leftZValue = 20;
	double rightZValue = 20;
	double dispersion = 59;
	double angle = 0;
	int leftCurvature = Common.CURVATURE_SPHERICAL;
	int rightCurvature = Common.CURVATURE_SPHERICAL;
	boolean symmetrical = true;
	int function = Common.OBJECT_REFRACTOR;
	boolean active = true;

	public ObjectValues(String name) {
		this.name = name;
	}
}

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

final public class ProgramValues extends ValueManager {
	
	int decimalPlaces = 4;
	int windowX = 100;
	int windowY = 100;
	int defaultWindowWidth = 900;
	int defaultWindowHeight = 600;
	boolean inverse = false;
	boolean showGrid = true;
	boolean antialias = true;
	boolean showControls = true;
	int colorLensOutline =  0x1080a0ff;
	int colorLensSelected = 0x1000c000;
	int colorBaseline = 0x004000;
	int colorGrid = 0x40c0c0c0;
	int colorArrow = 0x800000ff;
	int colorBeam = 0xc00000;
	int colorHighBackground = 0xffffff;
	int colorLowBackground = 0x000000;
	int colorLightSource = 0x0000ff;
	int colorTerminator = 0x000000;
	int selectedTab = 0;
	int selectedComponent = 0;
	
	int beamWidth = 1;
	double xOffset = 0;
	double yOffset = 0;
	double dispScale = 3e-2;
	double snapValue = .5;
	double intersectionArrowSize = .05;
    double yStartBeamPos = -1.8;
    double yEndBeamPos = 1.8;
    int dispersionBeams = 0;
    int beamCount = 4;
    int maxIntersections = 64;
    double xBeamSourceRefPlane = -30.0;
    double xBeamRotationPlane = 0;
    double virtualSpaceSize = 1e2;
    double beamAngle = 0;
    double interLensEpsilon = 1e-6;
    double surfEpsilon = 5e-4;
    boolean divergingSource = false;
    boolean askBeforeDeleting = true;
    int clipboardGraphicXSize = 1280;
    int helpScrollPos = 0;
    int tableLineLimit = 500;
}

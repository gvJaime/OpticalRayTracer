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

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

final public class ImageTransferable implements Transferable {

    Image image;

    public ImageTransferable(Image im) {
        image = im;
    }

    public Object getTransferData(DataFlavor flavor) {
        //System.out.println("get data: " + flavor.toString());
        if (flavor.equals(DataFlavor.imageFlavor)) {
            return image;
        }
        return null;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //System.out.println("is this flavor: " + flavor.toString());
        return DataFlavor.imageFlavor.equals(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        //System.out.println("get flavors");
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }
}

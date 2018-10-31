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
import java.awt.event.*;

import javax.swing.*;

@SuppressWarnings("serial")
final public class ColorButton extends JButton implements ProgramControl {

    Color col;
    private String tag = "";
    OpticalRayTracer parent;

    public ColorButton(OpticalRayTracer p, String tag, String tip) {
        init(p, tag,tip);
    }

    public ColorButton(OpticalRayTracer p, String s, String tag, String tip) {
        this.col = new Color(Integer.parseInt(s));
        init(p, tag,tip);
    }

    public ColorButton(OpticalRayTracer p, int c, String tag, String tip) {
        this.col = new Color(c);
        init(p, tag,tip);
    }
    
    public String getTag() {
    	return tag;
    }

    void init(OpticalRayTracer p, String tag, String tip) {
        this.parent = p;
        this.tag = tag;
        setValue(parent.programValues.getOneValue(tag));
        setToolTipText(tip);
        Dimension d = new Dimension(48,32);
        this.setMaximumSize(d);
        this.setMinimumSize(d);
        this.setPreferredSize(d);

        addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClicked(evt);
            }
        });
        paintButton();
    }
    
    public void reset() {
    	int cc = Integer.parseInt(parent.programValues.getOneValue(tag));
    	col = new MyColor(cc);
    	paintButton();
    }

    // all this because you can't define colors
    // for Swing buttons
    void paintButton() {
        int rgb = col.getRGB();
        String hex = String.format("%06x",rgb & 0xffffff);
        String str = "<html><span style=\"background:#" +
                hex + ";\">&nbsp;&nbsp;&nbsp;&nbsp;</html>";
        setText(str);
        parent.updateGraphicDisplay();
    }

    void handleMouseClicked(MouseEvent evt) {
        Color cc = JColorChooser.showDialog(parent.frame, "Choose a color", col);
        if (cc != null) {
            col = cc;
            parent.undoPush();
            parent.programValues.setOneValue(tag,getValue());
            paintButton();
        }
    }
    
    public String getValue() {
    	int ic = col.getBlue() & 0xff;
        ic |= (col.getGreen() & 0xff) << 8;
        ic |= (col.getRed() & 0xff) << 16;
        ic |= (col.getAlpha() & 0xff) << 24;
        return "" + ic;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public void setValue(String s) {
    	int cc = LocaleHandler.getInt(s);
        col = new MyColor(cc);
        paintButton();
    }

    public void setColor(Color c) {
        col = c;
        paintButton();
    }

    public Color getColor() {
        return col;
    }
}

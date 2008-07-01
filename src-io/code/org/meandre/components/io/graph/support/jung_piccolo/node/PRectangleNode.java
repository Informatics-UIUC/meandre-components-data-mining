/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 

package org.meandre.components.io.graph.support.jung_piccolo.node;

import edu.uci.ics.jung.graph.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * TODO: testing
 * 
 * @author D. Searsmith (conversion to SEASR 6/08)
 */
public class PRectangleNode extends PVertexNode {

	private static final long serialVersionUID = 1L;

	private double width, height;
	private Rectangle2D rectangle;

	public PRectangleNode(Vertex v) {

		super(v);

		rectangle = new Rectangle2D.Double();

		Object datum = v.getUserDatum(WIDTH);
		if (datum != null) {
			width = ((Double) datum).doubleValue();
		} else {
			width = 2d;
		}

		datum = v.getUserDatum(HEIGHT);
		if (datum != null) {
			height = ((Double) datum).doubleValue();
		} else {
			height = 2d;
		}

		setBounds(-width / 2d, -height / 2d, width, height);

	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public void paint(PPaintContext aPaintContext) {

		Graphics2D g2 = aPaintContext.getGraphics();

		g2.setPaint(baseColor);
		g2.fill(rectangle);

		if (borderColor != null) {
			g2.setPaint(borderColor);
			g2.setStroke(borderStroke);
			g2.draw(rectangle);
		}

	}

	public boolean setBounds(double x, double y, double w, double h) {

		if (super.setBounds(x, y, w, h)) {
			rectangle.setFrame(x, y, w, h);
			return true;
		}

		return false;

	}

	public void setSize(double d) {
		width = d;
		height = d;
	}

	public void setSize(double w, double h) {
		width = w;
		height = h;
	}

}

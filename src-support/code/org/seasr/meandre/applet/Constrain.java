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

package org.seasr.meandre.applet;

import java.awt.*;

/**
 * This is a helper class making the use of GridBagLayout more convenient.
 * @author redman
 * @author Lily Dong
 */
public class Constrain {

    /**
     * Create and set up a grid bag constraint with the values given.
     * @param x the x corrdinate in the layout grid.
     * @param y the y coordinate.
     * @param width the number of rows the object spans.
     * @param height the number of cols the object spans.
     * @param fill determines how the objects size is computed.
     * @param anchor how the object is aligned within the cell.
     * @param weightX fraction of the leftover horizontal space the object will
     *        use.
     * @param weightY fraction of the leftover vertical space the object will
     *        use.
     * @return the grid bag constraints.
     */
    static private GridBagConstraints getConstraints(int x, int y, int width,
            int height, int fill, int anchor, double weightX, double weightY) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = fill;
        c.anchor = anchor;
        c.weightx = weightX;
        c.weighty = weightY;
        return c;
    }

    /**
     * Set up the grid bag constraints for the object.
     * @param cont the container added to.
     * @param it the component being added.
     * @param x the x corrdinate in the layout grid.
     * @param y the y coordinate.
     * @param width the number of rows the object spans.
     * @param height the number of cols the object spans.
     * @param fill determines how the objects size is computed.
     * @param anchor how the object is aligned within the cell.
     * @param weightX fraction of the leftover horizontal space the object will
     *        use.
     * @param weightY fraction of the leftover vertical space the object will
     *        use.
     */
    static public void setConstraints(Container cont, Component it, int x,
            int y, int width, int height, int fill, int anchor, double weightX,
            double weightY) {
        GridBagConstraints c = Constrain.getConstraints(x, y, width, height,
                fill, anchor, weightX, weightY);
        c.insets = new Insets(2, 2, 2, 2);
        ((GridBagLayout) cont.getLayout()).setConstraints(it, c);
        cont.add(it);
    }

    /**
     * @param cont the contain inserted into.
     * @param it the component being added.
     * @param x the x corrdinate in the layout grid.
     * @param y the y coordinate.
     * @param width the number of rows the object spans.
     * @param height the number of cols the object spans.
     * @param fill determines how the objects size is computed.
     * @param anchor how the object is aligned within the cell.
     * @param weightX fraction of the leftover horizontal space the object will
     *        use.
     * @param weightY fraction of the leftover vertical space the object will
     *        use.
     * @param insets the insets.
     */
    static public void setConstraints(Container cont, Component it, int x,
            int y, int width, int height, int fill, int anchor, double weightX,
            double weightY, Insets insets) {
        GridBagConstraints c = Constrain.getConstraints(x, y, width, height,
                fill, anchor, weightX, weightY);
        c.insets = insets;
        ((GridBagLayout) cont.getLayout()).setConstraints(it, c);
        cont.add(it);
    }

    /**
     * @param cont the component being added to.
     * @param it the component being added.
     * @param x the x corrdinate in the layout grid.
     * @param y the y corrdinate in the layout grid.
     * @param width the number of rows the object spans.
     * @param height the number of cols the object spans.
     * @param fill determines how the objects size is computed.
     * @param anchor how the object is aligned within the cell.
     * @param weightX fraction of the leftover horizontal space the object will
     *        use.
     * @param weightY fraction of the leftover vertical space the object will
     *        use.
     * @param insetTop the top inset.
     * @param insetLeft the left inset.
     * @param insetBottom bottom inset.
     * @param insetRight inset on the right.
     */
    static public void setConstraints(Container cont, Component it, int x,
            int y, int width, int height, int fill, int anchor, double weightX,
            double weightY, int insetTop, int insetLeft, int insetBottom,
            int insetRight) {
        GridBagConstraints c = Constrain.getConstraints(x, y, width, height,
                fill, anchor, weightX, weightY);
        c.insets = new Insets(insetTop, insetLeft, insetBottom, insetRight);
        ((GridBagLayout) cont.getLayout()).setConstraints(it, c);
    }
}

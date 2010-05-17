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

package org.seasr.meandre.applet.prediction.decisiontree.widgets;

import java.awt.*;
import javax.swing.*;

import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTModel;
import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTNode;

/**
 * @author Lily Dong
 * @version 1.0
 */

/**
 * This interface defines the methods implemented by the various "views" of
 * nodes in the decision tree visualization.
 */
public interface View {

    /**
     * Set the data for this component.
     *
     * @param model The decision tree model
     * @param node  decision tree node
     */
    public void setData(ViewableDTModel model, ViewableDTNode node);

    /**
     * Draw this node to the specified graphics context.
     *
     * @param g2 graphics context
     */
    public void drawView(Graphics2D g2);

    /**
     * Get the width for this component.
     *
     * @return width
     */
    public double getWidth();

    /**
     * Get the height for this component.
     * @return height
     */
    public double getHeight();

    /**
     * When the mouse brushes over this node, draw the total and percentages of
     * each class for this node.
     *
     * @param g2 graphics context
     */
    public void drawBrush(Graphics2D g2);

    /**
     * Get the width of the brushable area that contains bar chart
     *
     * @return width of brushable area
     */
    public double getBrushWidth();

    /**
     * Get the height of the brushable area that contains bar chart
     *
     * @return height of brushable area
     */
    public double getBrushHeight();

    /**
     * Get expanded conponent.  This component shows the contents of the node
     * in more detail.
     *
     * @return expanded component
     */
    public JComponent expand();
}

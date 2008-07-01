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

package org.meandre.applet.prediction.decisiontree.widgets;

import org.meandre.components.prediction.decisiontree.support.ViewableDTModel;
import org.meandre.components.prediction.decisiontree.support.ViewableDTNode;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * A Viewport that draws a scaled-down node.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class ScaledNode extends Viewport {

   //~ Constructors ************************************************************

   /**
    * Creates a new ScaledNode object.
    *
    * @param model decision tree model
    * @param node  node to draw
    * @param snode scaled node
    */
   public ScaledNode(ViewableDTModel model, ViewableDTNode node,
                     ScaledNode snode) { this(model, node, snode, null); }

   /**
    * Creates a new ScaledNode object.
    *
    * @param model decision tree model
    * @param node  decision tree node
    * @param snode scaled node
    * @param label label
    */
   public ScaledNode(ViewableDTModel model, ViewableDTNode node,
                     ScaledNode snode, String label) {
      super(model, node, snode, label);
   }

   //~ Methods *****************************************************************

   /**
    * Draw the scaled node.  This is just a filled rectangle.
    *
    * @param g2 graphics context
    */
   public void drawScaledNode(Graphics2D g2) {

      // Background
      g2.setColor(DecisionTreeScheme.scaledviewbackgroundcolor);
      g2.fill(new Rectangle2D.Double(x - width / 2, y, width, height));
   }
} // end class ScaledNode

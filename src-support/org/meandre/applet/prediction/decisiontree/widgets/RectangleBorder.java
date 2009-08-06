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

import javax.swing.border.AbstractBorder;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;


/**
 * DecisionTreeVis Border for navigator panel and brush panel.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public final class RectangleBorder extends AbstractBorder {

   //~ Instance fields *********************************************************

   /** Description of field ascent. */
   int ascent;

   /** left inset. */
   int left;
   /** right inset. */
   int right;
   /** top inset. */
   int top;
   /** bottom inset. */
   int bottom;

   /** font metrics used */
   FontMetrics metrics;

   /** title */
   String title;

   /** title top inset. */
   int titletop;
   /** title bottom inset. */
   int titlebottom;
   /** title space inset. */
   int titlespace;

   //~ Constructors ************************************************************

   /**
    * Creates a new RectangleBorder object.
    *
    * @param title title to show
    */
   public RectangleBorder(String title) {
      this.title = title;

      // Insets
      left = 10;
      right = 10;
      bottom = 10;
      titletop = 4;
      titlebottom = 8;
      titlespace = 12;
      top = titletop + 10 + titlebottom + titlespace;
   }

   //~ Methods *****************************************************************


    /**
     * This default implementation returns a new <code>Insets</code>
     * instance where the <code>top</code>, <code>left</code>,
     * <code>bottom</code>, and
     * <code>right</code> fields are set to <code>0</code>.
     *
     * @param c the component for which this border insets value applies
     * @return the new <code>Insets</code> object initialized to 0
     */
    public Insets getBorderInsets(Component c) {
        return new Insets(top, left, bottom, right);
    }


    /**
     * Paint the border around the specified component.
     *
     * @param c      the component for which this border is being painted
     * @param g      the paint graphics
     * @param x      the x position of the painted border
     * @param y      the y position of the painted border
     * @param width  the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
                            int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        metrics = g2.getFontMetrics(DecisionTreeScheme.textfont);
        ascent = metrics.getAscent();

        // Background
        g2.setColor(DecisionTreeScheme.borderbackgroundcolor);
        g2.fill(new Rectangle2D.Double(x, y, width, top));
        g2.fill(new Rectangle2D.Double(x, y, left, height));
        g2.fill(new Rectangle2D.Double(x + width - right, y, right, height));
        g2.fill(new Rectangle2D.Double(x, y + height - bottom, width, bottom));

        // Bevel
        double ybevel = y + titletop + ascent + titlebottom;
        g2.setColor(DecisionTreeScheme.bordershadowcolor);
        g2.draw(new Line2D.Double(x, y + ybevel, x + width - 1, y + ybevel));
        g2.setColor(DecisionTreeScheme.borderhighlightcolor);
        g2.draw(new Line2D.Double(x, y + ybevel + 2, x + width - 1,
                y + ybevel + 2));

        // Upper bevel
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(DecisionTreeScheme.borderupperbevelcolor);
        g2.draw(new Line2D.Double(x, y, x + width - 1, y));
        g2.draw(new Line2D.Double(x, y, x, y + height - 1));

        // Lower bevel
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(DecisionTreeScheme.borderlowerbevelcolor);
        g2.draw(new Line2D.Double(x, y + height - 1, x + width - 1,
                y + height - 1));
        g2.draw(new Line2D.Double(x + width - 1, y, x + width - 1,
                y + height - 1));

        // Title
        g2.setFont(DecisionTreeScheme.textfont);
        g2.setColor(DecisionTreeScheme.textcolor);
        g2.drawString(title, x + left, y + titletop + ascent);
    } // end method paintBorder
} // end class RectangleBorder

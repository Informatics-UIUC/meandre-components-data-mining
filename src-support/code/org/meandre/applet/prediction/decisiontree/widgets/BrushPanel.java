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

import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTModel;

import javax.swing.*;

import java.awt.*;


/**
 * Draws data when mouse moves over a node in tree scroll pane.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public final class BrushPanel extends JPanel {

   //~ Instance fields *********************************************************

   /** the view */
   View view;

   //~ Constructors ************************************************************

   /**
    * Creates a new BrushPanel object.
    *
    * @param model the decision tree model
    */
   public BrushPanel(ViewableDTModel model) {
      DecisionTreeScheme scheme = new DecisionTreeScheme();

      setOpaque(true);
      setBackground(scheme.borderbackgroundcolor);
   }

   //~ Methods *****************************************************************


    /**
     * The minimum size is large enough to show the brush panel and the insets
     * around the brush panel
     * @return minimum size
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }


    /**
     * The preferred size is large enough to show the brush panel and the insets
     * around the brush panel
     * @return preferred size
     */
    public Dimension getPreferredSize() {

        Insets insets = getInsets();

        double width = insets.left + insets.right;
        double height = insets.top + insets.bottom;

        if (view != null) {
            width += view.getBrushWidth();
            height += view.getBrushHeight();
        }

        return new Dimension((int) width, (int) height);
    }


    /**
     * Calls the UI delegate's paint method, if the UI delegate
     * is non-<code>null</code>.  We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes
     * (for example, <code>Graphics.translate</code>).
     * <p/>
     * If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you
     * should not alter the clip <code>Rectangle</code> or modify the
     * transform. If you need to do these operations you may find it
     * easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoker super's implementation you must honor the opaque property,
     * that is
     * if this component is opaque, you must completely fill in the background
     * in a non-opaque color. If you do not honor the opaque property you
     * will likely see visual artifacts.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint
     * @see javax.swing.plaf.ComponentUI
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Insets insets = getInsets();

        if (view != null) {
            g2.translate(insets.left, insets.top);
            view.drawBrush(g2);
            g2.translate(-insets.left, -insets.top);
        }
    }

   /**
    * update
    *
    * @param view the view
    */
   public void updateBrush(View view) {
      this.view = view;

      revalidate();
      repaint();
   }
} // end class BrushPanel

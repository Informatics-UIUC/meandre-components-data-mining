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
import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTNode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


/**
 * Displays a scaled view of decision tree from tree scroll pane
 * Draws a navigator that shows how much of tree is visible Dimensions of
 * navigator based on scale of tree scroll pane.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public final class NavigatorPanel extends JPanel {

   //~ Instance fields *********************************************************

   /** pointer to the navigator */
   Navigator navigator;

   //~ Constructors ************************************************************

   /**
    * Creates a new NavigatorPanel object.
    *
    * @param model      decision tree model
    * @param scrollpane scroll pane that holds the visualization
    */
   public NavigatorPanel(ViewableDTModel model, TreeScrollPane scrollpane) {
      navigator = new Navigator(model, scrollpane);

      setBackground(DecisionTreeScheme.borderbackgroundcolor);
      add(navigator);
   }

   //~ Methods *****************************************************************

   /**
    * Rebuild the tree in the navigator.
    */
   public void rebuildTree() { navigator.rebuildTree(); }

   //~ Inner Classes ***********************************************************

    /**
     * The navigator is a smaller view of the decision tree
     */
   class Navigator extends JPanel implements MouseListener, MouseMotionListener,
                                             ChangeListener {

      /** Decision tree model */
      ViewableDTModel dmodel;

       /** drawable */
      boolean drawable = true;

      /** Decision tree root */
      ViewableDTNode droot;

      /** Width of decision tree */
      double dwidth;
       /** height of decision tree */
      double dheight;

       /** offscreen buffer */
      BufferedImage image;

      /** Maximum depth */
      int mdepth;

      /** Width of navigator */
      double nwidth;
       /** Height of navigator */
      double nheight;


      /** Scaled tree root */
      ScaledNode sroot;

       /** did state change? */
      boolean statechanged;

      /** Scaled width of decision tree */
      double swidth = 200;
        /** scaled height of decision tree */
      double sheight;

       /** the tree scroll pane */
      TreeScrollPane treescrollpane;
       /** the viewport */
      JViewport viewport;

      // Offsets of navigator
        /** x location */
      double x;
        /** y location */
      double y;
        /** x location of last mouse press */
      double lastx;
        /** y location of last mouse press */
      double lasty;

       /** scale in x direction */
      double xscale;
       /** scale in y direction */
      double yscale;

       /**
        * Constructor
        * @param model decision tree model
        * @param scrollpane tree scroll pane
        */
      public Navigator(ViewableDTModel model, TreeScrollPane scrollpane) {
         dmodel = model;
         droot = dmodel.getViewableRoot();
         sroot = new ScaledNode(dmodel, droot, null);

         treescrollpane = scrollpane;
         viewport = treescrollpane.getViewport();

         findMaximumDepth(droot);
         buildTree(droot, sroot);

         sroot.x = sroot.findLeftSubtreeWidth();
         sroot.y = sroot.yspace;

         findTreeOffsets(sroot);

         dwidth = sroot.findSubtreeWidth();
         dheight = (sroot.yspace + sroot.height) * (mdepth + 1) + sroot.yspace;

         findSize();

         setOpaque(true);

         if (drawable) {
            addMouseListener(this);
            addMouseMotionListener(this);
            viewport.addChangeListener(this);
            image =
               new BufferedImage((int) swidth, (int) sheight,
                                 BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = image.createGraphics();
            paintBuffer(g2);
         }
      }

       /**
        * Build the view.  Create a UI component for each node in the tree
        * recursively.
        * @param dnode  decision tree node
        * @param snode  scaled node
        */
      public void buildTree(ViewableDTNode dnode, ScaledNode snode) {

         for (int index = 0; index < dnode.getNumChildren(); index++) {
            ViewableDTNode dchild = dnode.getViewableChild(index);
            ScaledNode schild =
               new ScaledNode(dmodel, dchild, snode,
                              dnode.getBranchLabel(index));
            snode.addChild(schild);
            buildTree(dchild, schild);
         }
      }

       /**
        * Draw a line from (x1, y1) to (x2, y2)
        * @param g2 graphics2D
        * @param x1 x location 1
        * @param y1 y location 1
        * @param x2 x location 2
        * @param y2 y location 2
        */
      public void drawLine(Graphics2D g2, double x1, double y1, double x2,
                           double y2) {
         int linestroke = 1;

         g2.setStroke(new BasicStroke(linestroke));
         g2.setColor(DecisionTreeScheme.scaledviewbackgroundcolor);
         g2.draw(new Line2D.Double(x1, y1, x2, y2));
      }

       /**
        * Draw the tree in this navigator
        * @param g2 graphics context
        * @param snode scaled node
        */
      public void drawTree(Graphics2D g2, ScaledNode snode) {
         snode.drawScaledNode(g2);

         for (int index = 0; index < snode.getNumChildren(); index++) {
            ScaledNode schild = (ScaledNode) snode.getChild(index);

            double x1 = snode.x;
            double y1 = snode.y + snode.height;
            double x2 = schild.x;
            double y2 = schild.y;

            drawLine(g2, x1, y1, x2, y2);

            drawTree(g2, schild);
         }
      }

       /**
        * find the maximum depth below the given node
        * @param dnode maximum depth
        */
      public void findMaximumDepth(ViewableDTNode dnode) {
         int depth = dnode.getDepth();

         if (depth > mdepth) {
            mdepth = depth;
         }

         for (int index = 0; index < dnode.getNumChildren(); index++) {
            ViewableDTNode dchild = dnode.getViewableChild(index);
            findMaximumDepth(dchild);
         }
      }

       /**
        * Determine size and position of navigator
        */
      public void findSize() {
         double scale = treescrollpane.getScale();

         sheight = swidth * dheight / dwidth;

         if (sheight < 1) {
            drawable = false;
         }

         xscale = swidth / (scale * dwidth);
         yscale = sheight / (scale * dheight);

         Point position = viewport.getViewPosition();
         Dimension vpdimension = viewport.getExtentSize();

         double vpwidth = vpdimension.getWidth();
         nwidth = swidth * vpwidth / (scale * dwidth);

         if (nwidth > swidth) {
            nwidth = swidth;
         }

         x = xscale * position.x;

         double vpheight = vpdimension.getHeight();
         nheight = sheight * vpheight / (scale * dheight);

         if (nheight > sheight) {
            nheight = sheight;
         }

         y = yscale * position.y;
      } // end method findSize

       /**
        * find the offets for the tree
        * @param snode scaled node
        */
      public void findTreeOffsets(ScaledNode snode) {
         snode.findOffsets();

         for (int index = 0; index < snode.getNumChildren(); index++) {
            ScaledNode schild = (ScaledNode) snode.getChild(index);
            findTreeOffsets(schild);
         }
      }

      /**
        * The minimum size is swidth x sheight.  Large enough to show the
        * whole navigator.
        * @return minimum size
        */
       public Dimension getMinimumSize() {
           return getPreferredSize();
       }

       /**
        * The preferred size is swidth x sheight.  Large enough to show the
        * whole navigator.
        * @return preferred size
        */
       public Dimension getPreferredSize() {
           return new Dimension((int) swidth, (int) sheight);
       }

       /**
        * Invoked when the mouse button has been clicked (pressed
        * and released) on a component.
        */
       public void mouseClicked(MouseEvent event) {
       }

       /**
        * Scale the view when the mouse is dragged
        * @event mouse event
        */
       public void mouseDragged(MouseEvent event) {
           int x1 = event.getX();
           int y1 = event.getY();

           double xchange = x1 - lastx;
           double ychange = y1 - lasty;

           x += xchange;
           y += ychange;

           if (x < 0) {
               x = 0;
           }

           if (y < 0) {
               y = 0;
           }

           if (x + nwidth > swidth) {
               x = swidth - nwidth;
           }

           if (y + nheight > sheight) {
               y = sheight - nheight;
           }

           statechanged = false;

           double scale = treescrollpane.getScale();
           xscale = swidth / (scale * dwidth);
           yscale = sheight / (scale * dheight);
           treescrollpane.scroll((int) (x / xscale), (int) (y / yscale));

           lastx = x1;
           lasty = y1;

           repaint();
       } // end method mouseDragged

       /**
        * Invoked when the mouse enters a component.
        */
       public void mouseEntered(MouseEvent event) {
       }

       /**
        * Invoked when the mouse exits a component.
        */
       public void mouseExited(MouseEvent event) {
       }

       /**
        * Invoked when the mouse cursor has been moved onto a component
        * but no buttons have been pushed.
        */
       public void mouseMoved(MouseEvent event) {
       }

       /**
        * Invoked when a mouse button has been pressed on a component.
        */
       public void mousePressed(MouseEvent event) {
           lastx = event.getX();
           lasty = event.getY();
       }

       /**
        * Invoked when a mouse button has been released on a component.
        */
       public void mouseReleased(MouseEvent event) {
       }

        /**
         * Paint to an offscreen buffer
         * @param g2 graphics2D object
         */
      public void paintBuffer(Graphics2D g2) {
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setPaint(DecisionTreeScheme.borderbackgroundcolor);
         g2.fill(new Rectangle((int) dwidth, (int) dheight));

         AffineTransform transform = g2.getTransform();
         AffineTransform sinstance =
            AffineTransform.getScaleInstance(swidth / dwidth, swidth / dwidth);
         g2.transform(sinstance);

         drawTree(g2, sroot);

         g2.setTransform(transform);
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

           if (drawable) {
               Graphics2D g2 = (Graphics2D) g;
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);

               g2.drawImage(image, 0, 0, null);

               g2.setColor(DecisionTreeScheme.viewercolor);
               g2.setStroke(new BasicStroke(1));
               g2.draw(new Rectangle2D.Double(x, y, nwidth - 1, nheight - 1));
           }
       }

       /**
        *  Rebuild the tree view
        */
      public void rebuildTree() {
         sroot = new ScaledNode(dmodel, droot, null);

         findMaximumDepth(droot);
         buildTree(droot, sroot);

         sroot.x = sroot.findLeftSubtreeWidth();
         sroot.y = sroot.yspace;

         findTreeOffsets(sroot);

         dwidth = sroot.findSubtreeWidth();
         dheight = (sroot.yspace + sroot.height) * (mdepth + 1) + sroot.yspace;

         findSize();

         image =
            new BufferedImage((int) swidth, (int) sheight,
                              BufferedImage.TYPE_INT_RGB);

         Graphics2D g2 = image.createGraphics();
         paintBuffer(g2);

         revalidate();
         repaint();
      }

      /*
       * Scrolling causes a change event, but scrolling caused by moving the
       * navigator should not cause a change event.
       */
      public void stateChanged(ChangeEvent event) {

         if (statechanged) {
            findSize();
            repaint();
         }

         statechanged = true;
      }
   } // end class Navigator
} // end class NavigatorPanel

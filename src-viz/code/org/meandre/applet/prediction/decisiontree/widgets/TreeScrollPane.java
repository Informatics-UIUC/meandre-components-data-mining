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

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.util.LinkedList;


/**
 * Scroll Pane that holds the main tree view
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public final class TreeScrollPane extends JScrollPane {

    //~ Instance fields *********************************************************

    /** panel to paint the tree on */
    TreePanel treepanel;

    /** viewport into the scroll pane */
    JViewport viewport;

    //~ Constructors ************************************************************

    /**
     * Creates a new TreeScrollPane object.
     *
     * @param model the decision tree model
     * @param panel the panel for brushing
     */
    public TreeScrollPane(ViewableDTModel model, BrushPanel panel) {
        treepanel = new TreePanel(model, panel);

        viewport = getViewport();
        viewport.setBackground(DecisionTreeScheme.treebackgroundcolor);

        setBackground(DecisionTreeScheme.treebackgroundcolor);

        JPanel lowerright = new JPanel();
        setCorner(LOWER_RIGHT_CORNER, lowerright);
        viewport.setView(treepanel);
    }

    //~ Methods *****************************************************************

    /**
     * Clear the search
     */
    public void clearSearch() {}

    /**
     * Get the depth of the tree
     *
     * @return depth of the tree
     */
    public int getDepth() {
        return treepanel.getDepth();
    }

    /**
     * Get the panel the tree is painted on as a Printable
     *
     * @return Printable tree
     */
    public Printable getPrintable() {
        return treepanel;
    }

    /**
     * Get the scale
     *
     * @return scale
     */
    public double getScale() {
        return treepanel.scale;
    }

    /**
     * Get the decision tree model
     *
     * @return decision tree model
     */
    public ViewableDTModel getViewableModel() {
        return treepanel.model;
    }

    /**
     * Get the viewport for the root of the tree
     *
     * @return viewport for tree root
     */
    public Viewport getViewRoot() {
        return treepanel.viewportroot;
    }

    /**
     * Rebuild the tree view.
     */
    public void rebuildTree() {
        treepanel.rebuildTree();
        revalidate();
        repaint();
    }

    /**
     * Reset the scale to default and scroll to upper left corner
     */
    public void reset() {
        treepanel.scale = 1;
        scroll(0, 0);
    }

    /**
     * Called by navigator and search panel.  Scroll the view to the specified
     * location
     *
     * @param x x location
     * @param y y location
     */
    public void scroll(int x, int y) {
        viewport.setViewPosition(new Point(x, y));
        revalidate();
        repaint();
    }

    /**
     * Set the depth
     *
     * @param value new depth
     */
    public void setDepth(int value) {
        treepanel.setDepth(value);
        revalidate();
        repaint();
    }

    /**
     * Toggle showing labels on or off and then repaint
     */
    public void toggleLabels() {

        if (treepanel.labels == true) {
            treepanel.labels = false;
        } else {
            treepanel.labels = true;
        }

        treepanel.repaint();
    }

    /**
     * Toggle the zoom on or off
     */
    public void toggleZoom() {

        if (treepanel.zoom) {
            treepanel.zoom = false;
        } else {
            treepanel.zoom = true;
        }
    }

    //~ Inner Classes ***********************************************************

    /**
     * Panel to paint the decision tree on
     */
    public class TreePanel extends JPanel implements MouseListener,
            MouseMotionListener,
            Printable {

        //JMenuItem barchartitem;

        /** Brush panel */
        BrushPanel brushpanel;

        /** Maximum depth to draw */
        int depth;

        /** Width of decision tree */
        double dwidth;
        /** Height of decision tree */
        double dheight;

        /** */
        Viewport emptyview;

        /** Draw labels */
        boolean labels = true;

        /** last mouse press x location */
        int lastx;
        /** last mouse press y location */
        int lasty;
        /** smallest scale difference bound */
        double lowerscale = .1;

        /** Maximum depth */
        int mdepth;

        /** Decision tree model */
        ViewableDTModel model;

        /** List of offsets for each depth */
        LinkedList[] offsets;

        /** Decision tree root */
        ViewableDTNode root;

        /** Scale */
        double scale = 1;
        /** increment for scale */
        double scaleincrement = .1;
        /** upper scale difference bound */
        double upperscale = 2;

        /** View tree root */
        Viewport viewportroot;

        /** true if in zoomed mode */
        boolean zoom = false;

        /**
         * Constructor
         * @param model decision tree model
         * @param panel the brushing panel
         */
        public TreePanel(ViewableDTModel model, BrushPanel panel) {
            this.model = model;

            brushpanel = panel;

            root = model.getViewableRoot();
            viewportroot = new Viewport(model, root);

            findMaximumDepth(root);
            depth = mdepth;
            buildViewTree(root, viewportroot);

            offsets = new LinkedList[mdepth + 1];

            for (int index = 0; index <= mdepth; index++) {
                offsets[index] = new LinkedList();
            }

            viewportroot.x = viewportroot.findLeftSubtreeWidth();
            viewportroot.y = viewportroot.yspace;
            offsets[0].add(viewportroot);

            findViewTreeOffsets(viewportroot);

            dwidth = viewportroot.findSubtreeWidth();
            dheight =
                    (viewportroot.yspace + viewportroot.height) * (mdepth + 1) +
                    viewportroot.yspace;

            addMouseListener(this);
            addMouseMotionListener(this);
        }


        /**
         *  Builds the decision tree using view nodes
         * @param dnode decision tree node
         * @param emptyview viewport
         */
        void buildViewTree(ViewableDTNode dnode, Viewport emptyview) {

            for (int index = 0; index < dnode.getNumChildren(); index++) {
                ViewableDTNode dchild = dnode.getViewableChild(index);
                Viewport vchild =
                        new Viewport(model, dchild, emptyview,
                                     emptyview.getBranchLabel(index));
                emptyview.addChild(vchild);
                buildViewTree(dchild, vchild);
            }
        }


        /**
         * Draws a line between nodes from (x1,y1) to (x2,y2)
         * @param g2 graphics context
         * @param label label to draw
         * @param x1 x1
         * @param y1 y1
         * @param x2 x2
         * @param y2 y2
         */
        void drawLine(Graphics2D g2, String label, double x1, double y1,
                      double x2, double y2) {
            int linestroke = 1;

            double diameter = 8;
            double radius = diameter / 2;
            double xcircle;
            double ycircle;
            int circlestroke = 2;

            FontMetrics metrics = getFontMetrics(DecisionTreeScheme.textfont);
            int fontascent = metrics.getAscent();

            double xlabel;
            double ylabel;
            double labelspace = 20;
            int labelwidth = metrics.stringWidth(label);

            // Line
            g2.setStroke(new BasicStroke(linestroke));
            g2.setColor(DecisionTreeScheme.treelinecolor);
            g2.draw(new Line2D.Double(x1, y1, x2, y2 - 1));

            if (x1 < x2) {
                xcircle = x1 + (x2 - x1) / 2 - radius;
                xlabel = xcircle + diameter - 2 * circlestroke + labelspace;
            } else {
                xcircle = x1 - radius - (x1 - x2) / 2;
                xlabel = xcircle - labelspace - labelwidth;
            }

            ycircle = y1 + (y2 - y1) / 2 - radius;
            ylabel = ycircle + diameter;

            // Label
            if (labels) {
                g2.setFont(DecisionTreeScheme.textfont);
                g2.setColor(DecisionTreeScheme.textcolor);
                g2.drawString(label, (int) xlabel, (int) ylabel);
            }

            // Circle
            g2.setColor(DecisionTreeScheme.treecirclebackgroundcolor);
            g2.fill(new Ellipse2D.Double(xcircle, ycircle, 8, 8));

            g2.setColor(DecisionTreeScheme.treecirclestrokecolor);
            g2.setStroke(new BasicStroke(circlestroke));
            g2.draw(new Ellipse2D.Double(xcircle, ycircle, 8, 8));
        } // end method drawLine

        /**
         * Draws the view tree
         * @param g2 graphics object
         * @param emptyview viewport
         */
        void drawViewTree(Graphics2D g2, Viewport emptyview) {
            Shape shape = g2.getClip();

            if (
                    shape.intersects((emptyview.x - emptyview.width / 2),
                                     emptyview.y,
                                     emptyview.width, emptyview.height)) {
                emptyview.draw(g2);
            }

            if (emptyview.collapsed) {
                return;
            }

            for (int index = 0; index < emptyview.getNumChildren(); index++) {
                Viewport vchild = emptyview.getChild(index);

                if (vchild.getDepth() > depth) {
                    return;
                }

                double x1 = emptyview.x;
                double y1 = emptyview.y + emptyview.height;
                double x2 = vchild.x;
                double y2 = vchild.y;

                drawLine(g2, emptyview.getBranchLabel(index), x1, y1, x2, y2);

                drawViewTree(g2, vchild);
            }
        } // end method drawViewTree

        /**
         * Find the maximum depth of the subtree rooted at node
         * @param node node in decision tree
         */
        void findMaximumDepth(ViewableDTNode node) {
            int depth = node.getDepth();

            if (depth > mdepth) {
                mdepth = depth;
            }

            for (int index = 0; index < node.getNumChildren(); index++) {
                ViewableDTNode dchild = node.getViewableChild(index);
                findMaximumDepth(dchild);
            }
        }

        /**
         * Finds the offsets for each node
         * @param emptyview viewport
         */
        void findViewTreeOffsets(Viewport emptyview) {
            emptyview.findOffsets();
            offsets[emptyview.getDepth()].add(emptyview);

            for (int index = 0; index < emptyview.getNumChildren(); index++) {
                Viewport vchild = emptyview.getChild(index);
                findViewTreeOffsets(vchild);
            }
        }

        /**
         * Expand the view
         * @param emptyview viewport
         */
        public void expandView(Viewport emptyview) {
            View view = emptyview.getView();

            if (view != null) {
                JComponent component = view.expand();

                if (component != null) {
                    StringBuffer title;

                    if (emptyview.getNumChildren() != 0) {
                        title = new StringBuffer("Split: ");
                    } else {
                        title = new StringBuffer("Leaf: ");
                    }

                    title.append(emptyview.getLabel());

                    JFrame frame = new JFrame(title.toString());
                    frame.getContentPane().add(new JScrollPane(component));
                    frame.pack();
                    frame.setVisible(true);
                }
            }
        }

        //

        /**
         * Returns the tree depth given y offset
         * @param y y offset
         * @return the tree depth at the given y offset
         */
        public int findDepth(int y) {

            if (y > scale * (dheight - viewportroot.yspace)) {
                return -1;
            }

            int depth =
                    (int) (y /
                           (scale * (viewportroot.yspace + viewportroot.height)));

            if (
                    (y - scale * depth * (viewportroot.yspace +
                                          viewportroot.height)) >=
                    scale * viewportroot.yspace) {
                return depth;
            }

            return -1;
        }

        /**
         * Get the depth
         * @return depth
         */
        public int getDepth() {
            return depth;
        }


        /**
         * The minimum size is (0,0)
         * @return the minimum size
         */
        public Dimension getMinimumSize() {
            return new Dimension(0, 0);
        }

        /**
         * The preferred size is large enough to show the scaled width and
         * scaled height of the tree
         * @return the preferred size
         */
        public Dimension getPreferredSize() {
            return new Dimension((int) getSWidth(), (int) getSHeight());
        }

        /**
         * Returns scaled height of tree
         * @return scaled height
         */
        public double getSHeight() {
            return scale * dheight;
        }

        /**
         * Returns scaled width of tree
         * @return scaled width
         */
        public double getSWidth() {
            return scale * dwidth;
        }

        /**
         * Invoked when the mouse button has been clicked (pressed
         * and released) on a component.
         */
        public void mouseClicked(MouseEvent event) {
            int x = event.getX();
            int y = event.getY();

            if (zoom) {

                if (SwingUtilities.isLeftMouseButton(event)) {

                    if (scale + scaleincrement < upperscale) {
                        scale += scaleincrement;
                        revalidate();
                        repaint();
                    }
                } else if (SwingUtilities.isRightMouseButton(event)) {

                    if (scale - scaleincrement > lowerscale) {
                        scale -= scaleincrement;
                        revalidate();
                        repaint();
                    }
                }
            } else {
                int depth = findDepth(y);

                if (depth == -1) {
                    return;
                }

                LinkedList list = offsets[depth];
                boolean valid = true;
                int index = 0;

                while (valid) {
                    emptyview = (Viewport) list.get(index);

                    int test = emptyview.test(x, y, getScale());

                    if (test == -1) {
                        index++;
                    } else if (test == 1) {
                        valid = false;

                        if (emptyview.isVisible()) {

                            if (SwingUtilities.isLeftMouseButton(event)) {
                                expandView(emptyview);
                            } else
                            if (SwingUtilities.isRightMouseButton(event)) {
                            }
                        }
                    } else if (test == 2) {
                        valid = false;
                        emptyview.toggle();
                        repaint();
                    }

                    if (index == list.size()) {
                        valid = false;
                    }
                }
            } // end if
        } // end method mouseClicked

        /**
         * Invoked when a mouse button is pressed on a component and then
         * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
         * delivered to the component where the drag originated until the
         * mouse button is released (regardless of whether the mouse position
         * is within the bounds of the component).
         * <p/>
         * Due to platform-dependent Drag&Drop implementations,
         * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
         * Drag&Drop operation.
         */
        public void mouseDragged(MouseEvent event) {
            int x = event.getX();
            int y = event.getY();

            int xchange = x - lastx;
            int ychange = y - lasty;

            Dimension vpdimension = viewport.getExtentSize();
            double vpwidth = viewport.getWidth();
            double vpheight = viewport.getHeight();

            Point point = viewport.getViewPosition();

            point.x -= xchange;
            point.y -= ychange;

            if (point.x < 0) {
                point.x = 0;
            } else if (vpwidth > getSWidth()) {
                point.x = 0;
            } else if (point.x + vpwidth > getSWidth()) {
                point.x = (int) (getSWidth() - vpwidth);
            }

            if (point.y < 0) {
                point.y = 0;
            } else if (vpheight > getSHeight()) {
                point.y = 0;
            } else if (point.y + vpheight > getSHeight()) {
                point.y = (int) (getSHeight() - vpheight);
            }

            viewport.setViewPosition(point);
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
            int x = event.getX();
            int y = event.getY();

            int depth = findDepth(y);

            if (depth == -1) {
                brushpanel.updateBrush(null);

                return;
            }

            LinkedList list = offsets[depth];
            boolean valid = true;
            int index = 0;

            while (valid) {
                Viewport emptyview = (Viewport) list.get(index);
                int test = emptyview.test(x, y, getScale());

                if (test == -1) {
                    index++;
                } else if (test == 1) {
                    valid = false;

                    if (emptyview.isVisible()) {
                        brushpanel.updateBrush(emptyview.getView());
                    }
                } else if (test == 2) {
                    valid = false;
                }

                if (index == list.size()) {
                    valid = false;
                    brushpanel.updateBrush(null);
                }
            }
        } // end method mouseMoved

        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        public void mousePressed(MouseEvent event) {
            int x = event.getX();
            int y = event.getY();

            lastx = x;
            lasty = y;
        }

        /**
         * Invoked when a mouse button has been released on a component.
         */
        public void mouseReleased(MouseEvent event) {
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
         * <p/>
         * The passed in <code>Graphics</code> object might
         * have a transform other than the identify transform
         * installed on it.  In this case, you might get
         * unexpected results if you cumulatively apply
         * another transform.
         *
         * @param g the <code>Graphics</code> object to protect
         * @see #paint
         * @see javax.swing.plaf.ComponentUI
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle rectangle =
                    new Rectangle((int) getSWidth(), (int) getSHeight());
            g2.setColor(DecisionTreeScheme.treebackgroundcolor);
            g2.fill(rectangle);

            AffineTransform transform = g2.getTransform();
            AffineTransform sinstance =
                    AffineTransform.getScaleInstance(scale, scale);
            g2.transform(sinstance);

            drawViewTree(g2, viewportroot);

            g2.setTransform(transform);
        }

        /**
         * Prints the page at the specified index into the specified
         * {@link java.awt.Graphics} context in the specified
         * format.  A <code>PrinterJob</code> calls the
         * <code>Printable</code> interface to request that a page be
         * rendered into the context specified by
         * <code>graphics</code>.  The format of the page to be drawn is
         * specified by <code>pageFormat</code>.  The zero based index
         * of the requested page is specified by <code>pageIndex</code>.
         * If the requested page does not exist then this method returns
         * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
         * The <code>Graphics</code> class or subclass implements the
         * {@link java.awt.print.PrinterGraphics} interface to provide additional
         * information.  If the <code>Printable</code> object
         * aborts the print job then it throws a {@link java.awt.print.PrinterException}.
         *
         * @param g   the context into which the page is drawn
         * @param pf the size and orientation of the page being drawn
         * @param pi  the zero based index of the page to be drawn
         * @return PAGE_EXISTS if the page is rendered successfully
         *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
         *         non-existent page.
         * @throws java.awt.print.PrinterException
         *          thrown when the print job is terminated.
         */
        public int print(Graphics g, PageFormat pf, int pi) throws
                PrinterException {

            double pageHeight = pf.getImageableHeight();
            double pageWidth = pf.getImageableWidth();

            double cWidth = getWidth();
            double cHeight = getHeight();

            double scale = 1;

            if (cWidth >= pageWidth) {
                scale = pageWidth / cWidth;
            }

            if (cHeight >= pageHeight) {
                scale = Math.min(scale, pageHeight / cHeight);
            }

            double cWidthOnPage = cWidth * scale;
            double cHeightOnPage = cHeight * scale;

            if (pi >= 1) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            g2.scale(scale, scale);
            print(g2);

            return Printable.PAGE_EXISTS;
        } // end method print

        /**
         * Rebuild the view
         */
        public void rebuildTree() {
            Viewport viewportroot = new Viewport(model, root);

            findMaximumDepth(root);
            buildViewTree(root, viewportroot);

            offsets = new LinkedList[mdepth + 1];

            for (int index = 0; index <= mdepth; index++) {
                offsets[index] = new LinkedList();
            }

            viewportroot.x = viewportroot.findLeftSubtreeWidth();
            viewportroot.y = viewportroot.yspace;
            offsets[0].add(viewportroot);

            findViewTreeOffsets(viewportroot);

            dwidth = viewportroot.findSubtreeWidth();
            dheight =
                    (viewportroot.yspace + viewportroot.height) * (mdepth + 1) +
                    viewportroot.yspace;

            // copySearch(viewportroot, viewportroot);

            this.viewportroot = viewportroot;

            revalidate();
            repaint();
        } // end method rebuildTree

        /**
         * Set the depth
         * @param value new depth
         */
        public void setDepth(int value) {
            depth = value;
        }
    } // end class TreePanel
} // end class TreeScrollPane

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

import org.meandre.components.datatype.model.Model;

import org.meandre.components.prediction.mean.continuous.MeanOutputModel;
import org.meandre.components.prediction.regression.continuous.StepwiseLinearModel;

import org.meandre.components.prediction.decisiontree.ScalarViewableDTNode;
import org.meandre.components.prediction.decisiontree.ViewableDTModel;
import org.meandre.components.prediction.decisiontree.ViewableDTNode;


import javax.swing.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;


/**
 * Draws a small bar chart for the predicted, scalar outputs of a model. Each
 * bar is scaled based on the minimum and maximum values from the example set.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class ScalarView implements View {

    //~ Static fields/initializers **********************************************

    /**
     * A GUI component with a native peer that can be used to compute font
     * metrics.
     */
    private static JFrame graphics;

    //~ Instance fields *********************************************************

    /** width of bars. */
    private double barwidth = 5;

    /** the height of the brushing panel */
    private double brushheight;

    /** the width of the brushing panel */
    private double brushwidth;

    /** error amount */
    private double error;

    /** feature width */
    private double featurewidth;

    /** height */
    private double height;

    /** inset. */
    private double inset = 5;

    /** the maximum values */
    private double[] maximumvalues;

    /** the mean values */
    private double[] meanvalues;

    /** the minimum values */
    private double[] minimumvalues;

    /** used to format numbers for printing */
    private NumberFormat numberformat;

    /** ranges */
    private double[] ranges;

    /** reduction. */
    private double reduction;

    /** amount of space needed to print samples */
    private double samplesize = 10;

    /** spacing between printing of samples */
    private double samplespace = 8;

    /** the model that holds the data */
    private Model scalarmodel;

    /** root of the decision tree */
    private ScalarViewableDTNode scalarnode;

    /** scheme keeps the colors for the decision tree */
    private DecisionTreeScheme scheme;

    /** width. */
    private double width;

    //~ Constructors ************************************************************

    /**
     * Creates a new ScalarView object.
     */
    public ScalarView() {}

    //~ Methods *****************************************************************

    /**
     * When the mouse brushes over this node, draw the total and percentages of
     * each class for this node.
     *
     * @param g2 graphics context
     */
    public void drawBrush(Graphics2D g2) {
        double x;
        double y;
        double fontheight = g2.getFontMetrics().getHeight();

        y = 0;
        scheme.setIndex(0);

        for (int index = 0; index < meanvalues.length; index++) {
            x = 0;

            if (samplesize < fontheight) {
                y += fontheight - samplesize;
            }

            g2.setColor(scheme.getNextColor());
            g2.fill(new Rectangle2D.Double(x, y, samplesize, samplesize));

            x += samplesize + samplespace;

            y += samplesize;

            g2.setColor(scheme.textcolor);

            String value =
                    scalarmodel.getOutputFeatureName(index) + " = " +
                    numberformat.format(meanvalues[index]);
            g2.drawString(value, (int) x, (int) y);

            y += samplespace;
        }

        x = 0;
        y += fontheight;

        g2.drawString("Error " + numberformat.format(error), (int) x, (int) y);
        y += fontheight;
        g2.drawString("Best Error Reduction " + numberformat.format(reduction),
                      (int) x, (int) y);
    } // end method drawBrush

    /**
     * Draw this node to the specified graphics context.
     *
     * @param g2 graphics context
     */
    public void drawView(Graphics2D g2) {
        double x;

        // Background
        g2.setColor(scheme.viewbackgroundcolor);
        g2.fill(new Rectangle2D.Double(0, 0, width, height));

        // Bars
        x = inset;
        scheme.setIndex(0);

        for (int index = 0; index < ranges.length; index++) {
            double barheight =
                    meanvalues[index] * (height - inset * 2) / ranges[index];

            g2.setColor(scheme.getNextColor());
            g2.fill(new Rectangle2D.Double(x, height - inset - barheight,
                                           barwidth,
                                           barheight));

            x += barwidth;
        }
    }

    /**
     * Get expanded conponent.  This component shows the contents of the node
     * in more detail.
     *
     * @return expanded component
     */
    public JComponent expand() {

        if (scalarmodel instanceof MeanOutputModel) {
            return new ScalarExpanded();
        } else if (scalarmodel instanceof StepwiseLinearModel) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * Get the height of the brushable area that contains bar chart
     *
     * @return height of brushable area
     */
    public double getBrushHeight() {

        if (brushheight == 0) {
            FontMetrics metrics = graphics.getGraphics().getFontMetrics();

            double fontheight = metrics.getHeight();

            if (samplesize < fontheight) {
                brushheight =
                        (meanvalues.length) *
                        (metrics.getHeight() + samplespace) +
                        2 * metrics.getHeight();
            } else {
                brushheight =
                        (meanvalues.length) * (samplesize + samplespace) +
                        2 * metrics.getHeight();
            }
        }

        return brushheight;
    }

    /**
     * Get the width of the brushable area that contains bar chart
     *
     * @return width of brushable area
     */
    public double getBrushWidth() {

        if (brushwidth == 0) {
            FontMetrics metrics = graphics.getGraphics().getFontMetrics();

            String[] features = scalarmodel.getOutputFeatureNames();

            featurewidth = 0;

            double valuewidth = 0;
            double stringwidth = 0;

            for (int index = 0; index < features.length; index++) {
                stringwidth = metrics.stringWidth(features[index] + " = ");

                if (stringwidth > featurewidth) {
                    featurewidth = stringwidth;
                }

                stringwidth =
                        metrics.stringWidth(numberformat.format(meanvalues[
                        index]));

                if (stringwidth > valuewidth) {
                    valuewidth = stringwidth;
                }
            }

            double errorwidth =
                    metrics.stringWidth("Best Error Reduction " +
                                        numberformat.format(reduction));
            double datawidth =
                    samplesize + samplespace + featurewidth + valuewidth;

            if (datawidth > errorwidth) {
                brushwidth = datawidth;
            } else {
                brushwidth = errorwidth;
            }
        } // end if

        return brushwidth;
    } // end method getBrushWidth

    /**
     * Get the height of this component
     *
     * @return height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Get the width of this component
     *
     * @return width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Set the data for this component.
     *
     * @param model The decision tree model
     * @param node  decision tree node
     */
    public void setData(ViewableDTModel model, ViewableDTNode node) {
        scalarnode = (ScalarViewableDTNode) node;

        scalarmodel = scalarnode.getModel();

        if (scalarmodel instanceof MeanOutputModel) {
            MeanOutputModel meanmodel = (MeanOutputModel) scalarmodel;

            minimumvalues = scalarnode.getMinimumValues();
            maximumvalues = scalarnode.getMaximumValues();
            error = scalarnode.getError();
            reduction = scalarnode.getErrorReduction();
            meanvalues = meanmodel.Evaluate(new double[0]);
        } else if (scalarmodel instanceof StepwiseLinearModel) {} else {}

        ranges = new double[minimumvalues.length];

        for (int index = 0; index < minimumvalues.length; index++) {
            double minimum = minimumvalues[index];
            double maximum = maximumvalues[index];
            double mean = meanvalues[index];

            ranges[index] = maximum - minimum;
        }

        scheme = new DecisionTreeScheme(minimumvalues.length);

        height = 50;
        width = inset * 2 + barwidth * minimumvalues.length;

        numberformat = NumberFormat.getInstance();
        numberformat.setMaximumFractionDigits(3);
        numberformat.setMinimumFractionDigits(3);

        if (graphics == null) {
            graphics = new JFrame();
            graphics.addNotify();
            graphics.setFont(DecisionTreeScheme.textfont);
        }
    } // end method setData

    //~ Inner Classes ***********************************************************

    /**
     * Expanded view for a scalar node
     */
    private class ScalarExpanded extends JPanel {

        /** constant for Split: */
        static private final String SPLIT = "Split: ";

        /** constant for Leaf: */
        static private final String LEAF = "Leaf: ";

        /** space between bars */
        double barspace = 5;
        /** width of a bar */
        double barwidth = 60;
        /** bottom inset */
        double bottom = 15;
        /** data bottom inset */
        double databottom = 10;
        /** data height */
        double dataheight;
        /** data left inset */
        double dataleft = 10;
        /** data right inset */
        double dataright = 10;
        /** data top inset */
        double datatop = 10;
        /** width of data area */
        double datawidth;
        /** graph bottom inset */
        double graphbottom = 30;
        /** height of graph */
        double graphheight;
        /** graph left inset */
        double graphleft = 30;
        /** graph right inset */
        double graphright = 30;
        /** buffer space for graph */
        double graphspace = 15;
        /** buffer space for graph */
        double graphspacing = 15;
        /** top inset for graph */
        double graphtop = 30;
        /** width of graph */
        double graphwidth;
        /** height of graph */
        double gridheight = 300;
        /** grid size */
        double gridsize = 10;
        /** stroke for painting grid */
        float gridstroke = .1f;
        /** width of grid */
        double gridwidth;
        /** ascent for large font */
        int largeascent;
        /** FontMetrics for large font */
        FontMetrics largemetrics;
        /** large tick size */
        double largetick = 10;
        /** left inset */
        double left = 15;
        /** labels of all branches from the root to this node */
        String[] path;
        /** buffer space for drawing path */
        double pathbottom = 10;
        /** height of area for drawing path */
        double pathheight;
        /** index into path array */
        int pathindex;
        /** buffer space for drawing path */
        double pathleading = 2;
        /** left inset for path */
        double pathleft = 10;
        /** right inset for path */
        double pathright = 15;
        /** top inset for path */
        double pathtop = 6;
        /** width of path area */
        double pathwidth;
        /** right inset */
        double right = 15;
        /** width of rectangle used for sample */
        double samplesize = 10;
        /** buffer space */
        double samplespace = 8;
        /** ascent for small font */
        int smallascent;
        /** font metrics for small font */
        FontMetrics smallmetrics;
        /** small tick size */
        double smalltick = 4;
        /** space between ticks */
        double tickspace = 8;
        /** top inset */
        double top = 15;
        double valuespace = 5;

        double[] valuewidths;
        /** x location where the data area is painted */
        double xdata;
        /** x location where the graph is painted */
        double xgraph;
        /** x location to draw label */
        double xlabel;
        /** x location for path area */
        double xpath;
        /** y location for data area */
        double ydata;
        /** y location for graph */
        double ygraph;
        /** y location for label */
        double ylabel;
        /** buffer space in y direction for label */
        double ylabelspace = 15;
        /** y location for path area */
        double ypath;
        /** buffer space in y direction for path */
        double ypathspace = 15;

        /**
         * Constructor
         */
        ScalarExpanded() {
            int depth = scalarnode.getDepth();
            path = new String[depth];

            if (path.length > 0) {
                pathindex = depth - 1;
                findPath(scalarnode);
            }

            largemetrics = getFontMetrics(scheme.expandedfont);
            largeascent = largemetrics.getAscent();

            smallmetrics = getFontMetrics(scheme.textfont);
            smallascent = smallmetrics.getAscent();

            setBackground(scheme.expandedbackgroundcolor);
        }

        /**
         * Draw everything
         * @param g2 graphics context
         */
        void drawData(Graphics2D g2) {
            double x;
            double y;

            // Background
            g2.setColor(scheme.expandedborderbackgroundcolor);
            g2.fill(new Rectangle2D.Double(xdata, ydata, datawidth, dataheight));

            double fontheight = g2.getFontMetrics().getHeight();

            scheme.setIndex(0);
            y = ydata + datatop;

            for (int index = 0; index < meanvalues.length; index++) {
                x = xdata + dataleft;

                if (samplesize < fontheight) {
                    y += fontheight - samplesize;
                }

                g2.setColor(scheme.getNextColor());
                g2.fill(new Rectangle2D.Double(x, y, samplesize, samplesize));

                x += samplesize + samplespace;

                y += samplesize;

                g2.setColor(scheme.textcolor);

                String value =
                        scalarmodel.getOutputFeatureName(index) + " = " +
                        numberformat.format(meanvalues[index]);
                g2.drawString(value, (int) x, (int) y);

                y += samplespace;
            }

            x = xdata + dataleft;
            y += fontheight;

            g2.drawString("Error " + numberformat.format(error), (int) x,
                          (int) y);
            y += fontheight;
            g2.drawString("Best Error Reduction " +
                          numberformat.format(reduction),
                          (int) x, (int) y);
        } // end method drawData

        /**
         * Draw the graph
         * @param g2 graphics context
         */
        void drawGraph(Graphics2D g2) {
            scheme.setIndex(0);

            // Background
            g2.setColor(scheme.expandedborderbackgroundcolor);
            g2.fill(new Rectangle2D.Double(xgraph, ygraph, graphwidth,
                                           graphheight));

            double x = xgraph + graphleft;
            double y = ygraph + graphheight - graphbottom;

            for (int index = 0; index < meanvalues.length; index++) {
                double initial = x;
                double valueincrement = ranges[index] / gridsize;
                double gridincrement = (gridheight - smallascent) /
                                       ranges[index];
                double value = minimumvalues[index];

                for (int increment = 0; increment <= gridsize; increment++) {

                    // Value
                    g2.setColor(scheme.expandedfontcolor);
                    g2.drawString(numberformat.format(value), (int) x, (int) y);

                    // Tickmark
                    x += valuewidths[index] + valuespace;
                    y -= smallascent / 2;
                    g2.setColor(scheme.expandedgraphgridcolor);
                    g2.setStroke(new BasicStroke(gridstroke));
                    g2.draw(new Line2D.Double(x, y, x + largetick, y));

                    x = initial;
                    y -= gridincrement * valueincrement - smallascent / 2;
                    value += valueincrement;
                }

                // Axis
                x = initial + valuewidths[index] + valuespace + largetick;
                y = ygraph + graphheight - graphbottom;
                g2.draw(new Line2D.Double(x, y, x, y - gridheight));
                g2.draw(new Line2D.Double(x, y, x + 2 * barspace + barwidth, y));

                // Bar
                double barheight = meanvalues[index] * gridincrement;
                g2.setColor(scheme.getNextColor());
                g2.fill(new Rectangle2D.Double(x + barspace, y - barheight,
                                               barwidth, barheight));

                // Label
                String feature = scalarmodel.getOutputFeatureName(index);
                double featurenamewidth = smallmetrics.stringWidth(feature);
                g2.setColor(scheme.textcolor);
                g2.drawString(feature,
                              (int) (x + barspace +
                                     (barwidth - featurenamewidth) / 2),
                              (int) (y + smallascent));

                x += 2 * barspace + barwidth + graphspacing;
            } // end for
        } // end method drawGraph

        /**
         * draw the label
         * @param g2 graphics context
         */
        void drawLabel(Graphics2D g2) {
            StringBuffer label;

            if (scalarnode.getNumChildren() != 0) {
                label = new StringBuffer(SPLIT);
            } else {
                label = new StringBuffer(LEAF);
            }

            label.append(scalarnode.getLabel());

            g2.setFont(scheme.expandedfont);
            g2.setColor(scheme.expandedfontcolor);
            g2.drawString(label.toString(), (int) xlabel, (int) ylabel);
        }

        /**
         * Draw the items in the path
         * @param g2 graphics context
         */
        void drawLabelPath(Graphics2D g2) {
            g2.setFont(scheme.textfont);

            // Background
            g2.setColor(scheme.expandedborderbackgroundcolor);
            g2.fill(new Rectangle2D.Double(xpath, ypath, pathwidth, pathheight));

            // Path
            double y = ypath + pathtop + smallascent;
            double x = pathleft + xpath;
            g2.setColor(scheme.textcolor);

            for (int index = 0; index < path.length; index++) {
                g2.drawString(path[index], (int) x, (int) y);
                y += smallascent + pathleading;
            }
        }

        /**
         * Find the path for the given node.  The path is the path from the root
         * to this node.
         * @param node a node in the decision tree
         */
        void findPath(ViewableDTNode node) {
            ViewableDTNode parent = node.getViewableParent();

            if (parent == null) {
                return;
            }

            for (int index = 0; index < parent.getNumChildren(); index++) {
                ViewableDTNode child = parent.getViewableChild(index);

                if (child == node) {
                    path[pathindex] = parent.getBranchLabel(index);
                    pathindex--;
                }
            }

            findPath(parent);
        }

        /**
         * If the minimum size has been set to a non-<code>null</code> value
         * just returns it.  If the UI delegate's <code>getMinimumSize</code>
         * method returns a non-<code>null</code> value then return that; otherwise
         * defer to the component's layout manager.
         *
         * @return the value of the <code>minimumSize</code> property
         * @see #setMinimumSize
         * @see javax.swing.plaf.ComponentUI
         */
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        /**
         * If the <code>preferredSize</code> has been set to a
         * non-<code>null</code> value just returns it.
         * If the UI delegate's <code>getPreferredSize</code>
         * method returns a non <code>null</code> value then return that;
         * otherwise defer to the component's layout manager.
         *
         * @return the value of the <code>preferredSize</code> property
         * @see #setPreferredSize
         * @see javax.swing.plaf.ComponentUI
         */
        public Dimension getPreferredSize() {
            xlabel = left;
            ylabel = top + largeascent;

            xpath = xlabel;
            ypath = ylabel + ylabelspace;

            StringBuffer label;

            if (scalarnode.getNumChildren() != 0) {
                label = new StringBuffer(SPLIT);
            } else {
                label = new StringBuffer(LEAF);
            }

            label.append(scalarnode.getLabel());

            pathwidth = largemetrics.stringWidth(new String(label));

            for (int index = 0; index < path.length; index++) {
                int smallwidth = smallmetrics.stringWidth(path[index]);

                if (smallwidth > pathwidth) {
                    pathwidth = smallwidth;
                }
            }

            if (path.length > 0) {
                pathwidth += pathleft + pathright;
                pathheight =
                        pathtop + path.length * smallascent +
                        (path.length - 1) * pathleading + pathbottom;
            }

            xdata = xpath;
            ydata = ypath + pathheight + ypathspace;

            datawidth = dataleft + getBrushWidth() + dataright;
            dataheight = datatop + getBrushHeight() + databottom;

            if (pathwidth > datawidth) {
                datawidth = pathwidth;
            } else {
                pathwidth = datawidth;
            }

            ygraph = top;
            xgraph = xpath + pathwidth + graphspace;

            graphheight = graphtop + gridheight + graphbottom;

            if (featurewidth > barwidth) {
                barwidth = featurewidth;
            }

            graphwidth = 0;
            valuewidths = new double[maximumvalues.length];

            for (int index = 0; index < maximumvalues.length; index++) {
                valuewidths[index] =
                        smallmetrics.stringWidth(numberformat.format(
                        maximumvalues[index]));
                graphwidth +=
                        valuewidths[index] + valuespace + largetick +
                        2 * barspace +
                        barwidth + graphspacing;
            }

            graphwidth += graphleft + graphright;

            double width = left + pathwidth + graphspace + graphwidth + right;
            double height;

            double firstheight = ydata + dataheight + bottom;
            double secondheight = top + graphheight + bottom;

            if (firstheight > secondheight) {
                height = firstheight;
            } else {
                height = secondheight;
            }

            return new Dimension((int) width, (int) height);
        } // end method getPreferredSize

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            drawLabel(g2);
            drawLabelPath(g2);
            drawData(g2);
            drawGraph(g2);
        }
    } // end class ScalarExpanded
} // end class ScalarView

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

import org.meandre.components.prediction.decisiontree.support.CategoricalViewableDTNode;
import org.meandre.components.prediction.decisiontree.support.NominalViewableDTModel;
import org.meandre.components.prediction.decisiontree.support.NominalViewableDTNode;
import org.meandre.components.prediction.decisiontree.support.NumericViewableDTNode;
import org.meandre.components.prediction.decisiontree.support.ScalarViewableDTNode;
import org.meandre.components.prediction.decisiontree.support.ViewableDTModel;
import org.meandre.components.prediction.decisiontree.support.ViewableDTNode;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.GeneralPath;

import java.util.ArrayList;


/**
 * Viewport handles the drawing of a ViewableDTNode.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class Viewport {

    //~ Static fields/initializers **********************************************

    /** constant for > */
    static private final String GREATER_THAN = ">";

    /** constant for < */
    static private final String LESS_THAN = "<";

    /** constant for >= */
    static private final String GREATER_THAN_EQUAL_TO = ">=";

    /** constant for <= */
    static private final String LESS_THAN_EQUAL_TO = "<=";

    /** constant for != */
    static private final String NOT_EQUAL_TO = "!=";

    /** constant for == */
    static private final String EQUAL_TO = "==";

    /** horizontal space between nodes. */
    static public double xspace = 20;

    /** vertical space between nodes */
    static public double yspace = 80;

    /** component with a native peer to use for font metrics. */
    static JFrame graphics;

    //~ Instance fields *********************************************************

    /** a list of the viewports for the child nodes */
    protected ArrayList children;

    /** true if the view is collapsed */
    protected boolean collapsed = false;

    /** used in setView to denote the first call to setView */
    protected boolean first = true;

    /** height */
    protected double height = 40;

    /** label */
    protected String label;

    /** Decision tree model. */
    protected ViewableDTModel model;

    /** node for this viewport */
    protected ViewableDTNode node;

    /** viewport for the parent nodes */
    protected Viewport parent;

    /** scheme keeps the colors for the decision tree */
    protected DecisionTreeScheme scheme;

    /** true if in search mode */
    protected boolean search = false;

    /** theight, used within draw method only */
    protected double theight;

    /** tside. */
    protected double tside = 8;

    /** tspace. */
    protected double tspace = 10;

    /** the View to draw in this Viewport */
    protected View view;

    /** width */
    protected double width = 45;

    /** x is midpoint of node */
    protected double x;
    /** y is top left of bar graph. */
    protected double y;

    //~ Constructors ************************************************************

    /**
     * Creates a new Viewport object.
     *
     * @param model decision tree model
     * @param node  node to draw
     */
    public Viewport(ViewableDTModel model, ViewableDTNode node) {
        this(model, node, null, null);
    }

    /**
     * Creates a new Viewport object.
     *
     * @param model  decision tree model
     * @param node   node to draw
     * @param parent Viewport containing parent
     * @param label  branch label
     */
    public Viewport(ViewableDTModel model, ViewableDTNode node, Viewport parent,
                    String label) {
        this.model = model;
        this.node = node;
        this.parent = parent;
        this.label = label;

        children = new ArrayList(node.getNumChildren());

        scheme = new DecisionTreeScheme();

        if (node instanceof ScalarViewableDTNode) {
            ScalarView view = new ScalarView();
            view.setData(model, node);
            setView(view);
        } else if (node instanceof NominalViewableDTNode) {
            NominalView view = new NominalView();
            view.setData(model, node);
            setView(view);
        }

        if (graphics == null) {
            graphics = new JFrame();
            graphics.addNotify();
            graphics.setFont(DecisionTreeScheme.textfont);
        }
    }

    //~ Methods *****************************************************************

    /**
     * Evaluate double values based on operator.
     *
     * @param  dvalue   left hand side
     * @param  value    right hand side
     * @param  operator the operator, should be one of the static constants
     * defined in this class
     *
     * @return result of the evaluation.  false
     * will be returned when the operator was not recognized
     */
    boolean evaluate(double dvalue, double value, String operator) {

        if (operator == GREATER_THAN) {
            return value < dvalue;
        } else if (operator == GREATER_THAN_EQUAL_TO) {
            return value <= dvalue;
        } else if (operator == LESS_THAN) {
            return value > dvalue;
        } else if (operator == LESS_THAN_EQUAL_TO) {
            return value >= dvalue;
        } else if (operator == EQUAL_TO) {
            return value == dvalue;
        } else if (operator == NOT_EQUAL_TO) {
            return value != dvalue;
        }

        return false;
    }

    /**
     * Evaluate string values based on operator.
     *
     * @param  svalue   left hand side
     * @param  value    right hand side
     * @param  operator operator, should be EQUAL_TO or NOT_EQUAL_TO
     *
     * @return result of the evaluation.  false will
     * also be returned when the operator is not recognized
     */
    boolean evaluate(String svalue, String value, String operator) {

        if (operator == EQUAL_TO) {
            return value.equals(svalue);
        } else if (operator == NOT_EQUAL_TO) {
            return!value.equals(svalue);
        }

        return false;
    }

    /**
     * Evaluate double values based on operator.
     *
     * @param  index
     * @param  dvalue   left hand side
     * @param  value    right hand side
     * @param  operator the operator, should be one of the static constants
     * defined in this class
     *
     * @return result of the evaluation
     */
    boolean evaluate(int index, double dvalue, double value, String operator) {

        if (index == 0) {

            if (operator == GREATER_THAN) {
                return value < dvalue;
            } else if (operator == GREATER_THAN_EQUAL_TO) {
                return value < dvalue;
            } else if (operator == LESS_THAN) {
                return value >= dvalue;
            } else if (operator == LESS_THAN_EQUAL_TO) {
                return value >= dvalue;
            } else if (operator == EQUAL_TO) {
                return false;
            } else if (operator == NOT_EQUAL_TO) {
                return value != dvalue;
            }
        } else {
            return evaluate(dvalue, value, operator);
        }

        return false;
    }

    /**
     * Evaluate nominal split condition.
     *
     * @param  condition the condition
     *
     * @return result of the evaluation
     */
    boolean evaluateNominal(SearchPanel.SplitCondition condition) {

        if (parent == null) {
            return false;
        }

        if (!(parent.node instanceof CategoricalViewableDTNode)) {
            return false;
        }

        CategoricalViewableDTNode categoricalparent =
                (CategoricalViewableDTNode) parent.node;

        String attribute = categoricalparent.getSplitAttribute();

        if (!attribute.equals(condition.attribute)) {
            return false;
        }

        String[] splitvalues = categoricalparent.getSplitValues();
        int index = findBranchIndex();
        String splitvalue = splitvalues[index];

        return evaluate(splitvalue, condition.svalue, condition.operator);
    }

    /**
     * Evaluate scalar split condition.  The value is compared to the split value.
     *
     * @param  condition the condtion
     *
     * @return result of the evaluation
     */
    boolean evaluateScalar(SearchPanel.SplitCondition condition) {

        if (parent == null) {
            return false;
        }

        if (!(parent.node instanceof NumericViewableDTNode)) {
            return false;
        }

        NumericViewableDTNode numericparent = (NumericViewableDTNode) parent.
                                              node;

        String attribute = numericparent.getSplitAttribute();

        if (!attribute.equals(condition.attribute)) {
            return false;
        }

        double splitvalue = numericparent.getSplitValue();
        int index = findBranchIndex();

        return evaluate(index, splitvalue, condition.value, condition.operator);
    }

    /**
     * Determine type of condition and call specific evaluate function.
     *
     * @param  condition the condition.
     *
     * @return result of the evaluation
     */
    protected boolean evaluate(SearchPanel.Condition condition) {

        try {

            if (node instanceof NominalViewableDTNode) {

                if (condition instanceof SearchPanel.PopulationCondition) {
                    int population =
                            ((NominalViewableDTNode) node).getOutputTally(
                            condition.attribute);

                    return evaluate(population, condition.value,
                                    condition.operator);
                } else if (condition instanceof SearchPanel.PercentCondition) {
                    double percent =
                            100 *
                            (double) ((NominalViewableDTNode) node).
                            getOutputTally(condition.attribute) /
                            (double) node.getTotal();

                    return evaluate(percent, condition.value,
                                    condition.operator);
                }
            } else if (condition instanceof SearchPanel.PurityCondition) {
                double purity = findPurity();

                return evaluate(purity, condition.value, condition.operator);
            } else if (condition instanceof SearchPanel.SplitCondition) {
                SearchPanel.SplitCondition splitcondition =
                        (SearchPanel.SplitCondition) condition;

                if (splitcondition.scalar) {
                    return evaluateScalar(splitcondition);
                } else {
                    return evaluateNominal(splitcondition);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            return false;
        }

        return false;
    } // end method evaluate

    /**
     * Add a Viewport child to this Viewport
     *
     * @param viewport new child viewport
     */
    public void addChild(Viewport viewport) {
        children.add(viewport);
    }

    /**
     * Draw this viewport
     *
     * @param g2 graphics context
     */
    public void draw(Graphics2D g2) {

        if (view != null) {
            g2.translate((double) (x - width / 2), (double) y);
            view.drawView(g2);
            g2.translate((double) ( -x + width / 2), (double) - y);
        } else {
            g2.setColor(scheme.viewbackgroundcolor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect((int) (x - width / 2), (int) y, (int) width,
                        (int) height);
        }

        // Triangle
        if (isLeaf()) {
            return;
        }

        theight = .866025 * tside;

        double x1;
        double y1;
        double ycomponent = tside / 2;
        double xcomponent = .577350 * ycomponent;
        double xcenter;
        double ycenter;

        if (collapsed) {
            xcenter = x + width / 2 + tspace + xcomponent;
            ycenter = y + height - ycomponent;

            int[] xpoints = {
                    (int) (xcenter - xcomponent),
                    (int) (xcenter + theight - xcomponent),
                    (int) (xcenter - xcomponent)
            };
            int[] ypoints = {
                    (int) (ycenter - ycomponent), (int) ycenter,
                    (int) (ycenter + ycomponent)
            };

            GeneralPath triangle =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD, xpoints.length);
            triangle.moveTo((int) (xcenter - xcomponent),
                            (int) (ycenter - ycomponent));

            for (int index = 1; index < xpoints.length; index++) {
                triangle.lineTo(xpoints[index], ypoints[index]);
            }

            triangle.closePath();

            g2.setColor(scheme.viewtrianglecolor);
            g2.fill(triangle);
        } else {
            xcenter = x + width / 2 + tspace + xcomponent;
            ycenter = y + height - ycomponent;

            int[] xpoints = {
                    (int) (xcenter - ycomponent), (int) (xcenter + ycomponent),
                    (int) (xcenter)
            };
            int[] ypoints = {
                    (int) (ycenter - xcomponent), (int) (ycenter - xcomponent),
                    (int) (ycenter + ycomponent)
            };

            GeneralPath triangle =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD, xpoints.length);
            triangle.moveTo((int) (xcenter - ycomponent),
                            (int) (ycenter - xcomponent));

            for (int index = 1; index < xpoints.length; index++) {
                triangle.lineTo(xpoints[index], ypoints[index]);
            }

            triangle.closePath();

            g2.setColor(DecisionTreeScheme.viewtrianglecolor);
            g2.fill(triangle);
        } // end if
    } // end method draw

    /**
     * Find branch index of parent corresponding to node.
     *
     * @return branch index
     */
    public int findBranchIndex() {

        if (parent == null) {
            return -1;
        }

        for (int index = 0; index < parent.getNumChildren(); index++) {
            Viewport node = parent.getChild(index);

            if (node == this) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Find the width needed to draw the subtrees rooted at all children
     * between start and end.
     *
     * @param  start start child index
     * @param  end   end child index
     *
     * @return Description of return value.
     */
    public double findIntervalWidth(int start, int end) {
        double intervalwidth = 0;

        for (; start <= end; start++) {
            Viewport viewport = getChild(start);
            intervalwidth += viewport.findSubtreeWidth();
        }

        return intervalwidth;
    }

    /**
     * Width from midpoint to leftmost child node.
     *
     * @return width from midpoint to leftmost child node
     */
    public double findLeftSubtreeWidth() {

        if (isLeaf()) {
            return getWidth() / 2;
        }

        int children = getNumChildren();

        if (children % 2 == 0) {
            int middle = children / 2;

            return findIntervalWidth(0, middle - 1);
        } else {
            int middle = children / 2 + 1;
            Viewport viewport = getChild(middle - 1);

            return findIntervalWidth(0, middle - 2) +
                    viewport.findLeftSubtreeWidth();
        }
    }

    /**
     * Determines offsets of children.
     */
    public void findOffsets() {
        int children = getNumChildren();

        if (children % 2 == 0) {
            int middle = children / 2;

            for (int index = 0; index < children; index++) {
                Viewport viewport = getChild(index);

                if (index <= middle - 1) {
                    viewport.x =
                            x - findIntervalWidth(index + 1, middle - 1) -
                            viewport.findRightSubtreeWidth();
                } else {
                    viewport.x =
                            x + findIntervalWidth(middle, index - 1) +
                            viewport.findLeftSubtreeWidth();
                }

                viewport.y = y + height + yspace;
            }
        } else {
            int middle = children / 2 + 1;
            Viewport middleviewport = getChild(middle - 1);

            for (int index = 0; index < children; index++) {
                Viewport viewport = getChild(index);

                if (index < middle - 1) {

                    if (index == middle - 2) {
                        viewport.x =
                                x - middleviewport.findLeftSubtreeWidth() -
                                viewport.findRightSubtreeWidth();
                    } else {
                        viewport.x =
                                x - middleviewport.findLeftSubtreeWidth() -
                                findIntervalWidth(index + 1, middle - 2) -
                                viewport.findRightSubtreeWidth();
                    }
                } else if (index == middle - 1) {
                    viewport.x = x;
                } else {

                    if (index == middle) {
                        viewport.x =
                                x + middleviewport.findRightSubtreeWidth() +
                                viewport.findLeftSubtreeWidth();
                    } else {
                        viewport.x =
                                x + middleviewport.findRightSubtreeWidth() +
                                findIntervalWidth(middle, index - 1) +
                                viewport.findLeftSubtreeWidth();
                    }
                }

                viewport.y = y + height + yspace;
            } // end for
        } // end if
    } // end method findOffsets

    /**
     * Description of method findPurity.
     *
     * @return the purity
     */
    public double findPurity() {

        if (model instanceof NominalViewableDTModel) {
            double sum = 0;
            double numerator = 0;
            double base = Math.log(2.0);

            try {
                String[] outputs =
                        ((NominalViewableDTModel) model).getUniqueOutputValues();

                for (int index = 0; index < outputs.length; index++) {
                    double tally =
                            ((NominalViewableDTNode) node).getOutputTally(
                            outputs[index]);
                    numerator += -1.0 * tally * Math.log(tally) / base;
                    sum += tally;
                }

                numerator += sum * Math.log(sum) / base;
            } catch (Exception exception) {
                System.out.println(exception);
            }

            return numerator / sum;
        }

        return 0;
    } // end method findPurity

    /**
     * Width from midpoint to rightmost child node.
     *
     * @return Width from midpoint to rightmost child node
     */
    public double findRightSubtreeWidth() {

        if (isLeaf()) {
            return getWidth() / 2;
        }

        int children = getNumChildren();

        if (children % 2 == 0) {
            int middle = children / 2;

            return findIntervalWidth(middle, children - 1);
        } else {
            int middle = children / 2 + 1;
            Viewport viewport = getChild(middle - 1);

            return findIntervalWidth(middle, children - 1) +
                    viewport.findRightSubtreeWidth();
        }
    }

    /**
     * Width of the subtree rooted at this node.
     *
     * @return Width of the subtree rooted at this node
     */
    public double findSubtreeWidth() {

        if (isLeaf()) {
            return getWidth();
        }

        double subtreewidth = 0;

        for (int index = 0; index < getNumChildren(); index++) {
            Viewport viewport = getChild(index);
            subtreewidth += viewport.findSubtreeWidth();
        }

        return subtreewidth;
    }

    /**
     * Get the branch label for the branch from this node to the child at index
     *
     * @param  index child index
     *
     * @return branch label
     */
    public String getBranchLabel(int index) {
        return node.getBranchLabel(index);
    }

    /**
     * Get the Viewport for the child at index.
     *
     * @param  index child index
     *
     * @return Viewport
     */
    public Viewport getChild(int index) {
        return (Viewport) children.get(index);
    }

    /**
     * Get the depth of this node.
     *
     * @return depth of this node.
     */
    public int getDepth() {

        if (parent == null) {
            return 0;
        }

        return parent.getDepth() + 1;
    }

    /**
     * Get the label of this node.
     *
     * @return label
     */
    public String getLabel() {
        return node.getLabel();
    }

    /**
     * Get the number of children of this node.
     *
     * @return number of children
     */
    public int getNumChildren() {
        return children.size();
    }

    /**
     * Return true if in search
     *
     * @return true if node is in search
     */
    public boolean getSearch() {
        return search;
    }

    /**
     * Get the View
     *
     * @return View
     */
    public View getView() {
        return view;
    }

    /**
     * Get the height of the View
     *
     * @return height of the View
     */
    public double getViewHeight() {
        return view.getHeight();
    }

    /**
     * Get the width of the View
     *
     * @return Width of the View
     */
    public double getViewWidth() {
        return view.getWidth();
    }

    /**
     * Width for finding offset.
     *
     * @return Width for finding offset
     */
    public double getWidth() {
        FontMetrics metrics = graphics.getGraphics().getFontMetrics();

        double swidth1;
        double swidth2;

        if (label != null) {
            swidth1 = 2 * metrics.stringWidth(label);
        } else {
            swidth1 = 0;
        }

        if (view != null) {
            swidth2 = xspace + view.getWidth() + xspace;
        } else {
            swidth2 = xspace + width + xspace;
        }

        if (swidth1 > swidth2) {
            return swidth1;
        } else {
            return swidth2;
        }
    }

    /**
     * Return true if this node is a leaf
     *
     * @return true if this node is a leaf
     */
    public boolean isLeaf() {

        if (children.size() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Return true if this Viewport is to be visible
     *
     * @return true if this Viewport is to be visible
     */
    public boolean isVisible() {
        Viewport viewport = this;

        while (viewport.parent != null) {

            if (viewport.parent.collapsed) {
                return false;
            }

            viewport = viewport.parent;
        }

        return true;
    }

    /**
     * Set the search field
     *
     * @param value new search
     */
    public void setSearch(boolean value) {
        search = value;
    }

    /**
     * Set the search background
     *
     * @param value new search background
     */
    public void setSearchBackground(boolean value) {}

    /**
     * Set the view
     *
     * @param view new View
     */
    public void setView(View view) {
        this.view = view;

        width = view.getWidth();

        double viewheight = view.getHeight();

        if (first) {
            height = viewheight;
            first = false;
        } else if (viewheight > height) {
            height = viewheight;
        }
    }

    /**
     * Determine if given point falls in bounds of node.
     *
     * @param  x1    x location 1
     * @param  y1    y location 1
     * @param  scale the scale
     *
     * @return Description of return value.
     */
    public int test(int x1, int y1, double scale) {

        if (x1 >= scale * (x - width / 2) && x1 <= scale * (x + width / 2)) {
            return 1;
        }

        if (
                x1 >= scale * (x + width / 2) &&
                x1 <= scale * (x + width / 2 + tspace + tside + tspace)) {

            if (
                    y1 >= scale * (y + height - tside - tspace) &&
                    y1 <= scale * (y + height)) {
                return 2;
            }
        }

        return -1;
    }

    /**
     * Toggle the collapsing of the subtree rooted at this node.
     */
    public void toggle() {

        if (collapsed) {
            collapsed = false;
        } else {
            collapsed = true;
        }
    }
} // end class Viewport

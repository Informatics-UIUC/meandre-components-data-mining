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

import org.meandre.components.prediction.decisiontree.NominalViewableDTModel;
import org.meandre.components.prediction.decisiontree.NominalViewableDTNode;
import org.meandre.components.prediction.decisiontree.ViewableDTModel;
import org.meandre.components.prediction.decisiontree.ViewableDTNode;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;


/**
 * NominalView handles the drawing of a nominal node in a decision tree
 * visualization.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 *
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class NominalView implements View {

   //~ Static fields/initializers **********************************************

   /**
    * A GUI component with a native peer that can be used to compute font
    * metrics.
    */
   static private JFrame graphics;

   //~ Instance fields *********************************************************

   /** space between bars. */
   private double barspace = 5;

   /** width of bars. */
   private double barwidth = 16;

   /** height. */
   private double height = 45;

   /** left inset. */
   private double leftinset = 5;

   /** Decision tree model. */
   private NominalViewableDTModel model;

   /** node. */
   private NominalViewableDTNode node;

   /** number format. */
   private NumberFormat numberformat;

   /** unique outputs. */
   private String[] outputs;

   /** output space. */
   private double outputspace = 10;

   /** output width. */
   private double outputwidth = 80;

   /** percent width. */
   private double percentwidth;

   /** right inset. */
   private double rightinset = 5;

   /** sample size. */
   private double samplesize = 10;

   /** sample space. */
   private double samplespace = 8;

   /** scale size. */
   private double scalesize = 100;

   /** scheme defines the colors used. */
   private DecisionTreeScheme scheme;

   /** tallies. */
   private int[] tallies;

   /** space between tally. */
   private double tallyspace = 10;

   /** with of tally area. */
   private double tallywidth;

   /** tickmark. */
   private double tickmark = 3;

   /** tside. */
   private double tside = 8;

   /** tspace. */
   private double tspace = 10;

   /** values. */
   private double[] values;

   /** width. */
   private double width;

   /** ygrid. */
   private double ygrid = 5;

   /** yincrement. */
   private double yincrement;

   /** yscale. */
   private double yscale;

   //~ Constructors ************************************************************

   /**
    * Creates a new NominalView object.
    */
   public NominalView() {
      numberformat = NumberFormat.getInstance();
      numberformat.setMaximumFractionDigits(5);

      if (graphics == null) {
         graphics = new JFrame();
         graphics.addNotify();
         graphics.setFont(DecisionTreeScheme.textfont);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Compute the values, which corresponds to the height of the bars.
    */
   private void findValues() {
      outputs = model.getUniqueOutputValues();

      values = new double[outputs.length];
      tallies = new int[outputs.length];

      for (int index = 0; index < values.length; index++) {

         try {
            tallies[index] = node.getOutputTally(outputs[index]);
            values[index] =
               100 * (double) tallies[index] / (double) node.getTotal();
         } catch (Exception exception) {
            exception.printStackTrace();
         }
      }
   }

   /**
    * When the mouse brushes over this node, draw the total and percentages of
    * each class for this node.
    *
    * @param g2 graphics context
    */
   public void drawBrush(Graphics2D g2) {
      FontMetrics metrics = graphics.getGraphics().getFontMetrics();
      double fontheight = metrics.getHeight();

      double x = 0;
      double y = 0;

      scheme.setIndex(0);

      for (int index = 0; index < outputs.length; index++) {
         x = 0;

         if (samplesize < fontheight) {
            y += fontheight - samplesize;
         }

         g2.setColor(scheme.getNextColor());
         g2.fill(new Rectangle2D.Double(x, y, samplesize, samplesize));

         x += samplesize + samplespace;
         y += samplesize;

         g2.setColor(scheme.textcolor);
         g2.drawString(outputs[index], (int) x, (int) y);

         x += outputwidth + outputspace;

         g2.drawString(Integer.toString(tallies[index]), (int) x, (int) y);

         x += tallywidth + tallyspace;

         g2.drawString(numberformat.format(values[index]) + "%", (int) x,
                       (int) y);

         y += samplespace;
      } // end for
   } // end method drawBrush

   /**
    * Draw this node to the specified graphics context.
    *
    * @param g2 graphics context
    */
   public void drawView(Graphics2D g2) {
      double x1;
      double y1;

      // Background
      g2.setColor(scheme.viewbackgroundcolor);
      g2.fill(new Rectangle2D.Double(0, 0, width, height));

      // Tickmarks
      g2.setColor(scheme.viewtickcolor);
      g2.setStroke(new BasicStroke(1));
      x1 = leftinset;
      y1 = yincrement;

      for (int index = 0; index < ygrid; index++) {
         g2.draw(new Line2D.Double(x1, y1, x1 + tickmark, y1));
         y1 += yincrement;
      }

      // Bars
      x1 = leftinset + tickmark + barspace;
      scheme.setIndex(0);

      for (int index = 0; index < values.length; index++) {
         double barheight = yscale * values[index];
         y1 = 1 + height - yincrement - barheight;
         g2.setColor(scheme.getNextColor());
         g2.fill(new Rectangle2D.Double(x1, y1, barwidth, barheight));
         x1 += barwidth + barspace;
      }
   } // end method drawView

   /**
    * Get expanded conponent. This component shows the contents of the node in
    * more detail.
    *
    * @return expanded component
    */
   public JComponent expand() { return new NominalExpanded(); }

   /**
    * Get the height of the brushable area that contains bar chart.
    *
    * @return height of brushable area
    */
   public double getBrushHeight() {
      FontMetrics metrics = graphics.getGraphics().getFontMetrics();
      double fontheight = metrics.getHeight();

      double size;

      if (samplesize > fontheight) {
         size = samplesize;
      } else {
         size = fontheight;
      }

      return (size + samplespace) * outputs.length;
   }

   /**
    * Get the width of the brushable area that contains bar chart.
    *
    * @return width of brushable area
    */
   public double getBrushWidth() {
      FontMetrics metrics = graphics.getGraphics().getFontMetrics();
      double fontheight = metrics.getHeight();

      for (int index = 0; index < outputs.length; index++) {
         double stringwidth = metrics.stringWidth(outputs[index]);

         if (stringwidth > outputwidth) {
            outputwidth = stringwidth;
         }

         stringwidth =
            metrics.stringWidth(numberformat.format(values[index]) + "%");

         if (stringwidth > percentwidth) {
            percentwidth = stringwidth;
         }

         stringwidth = metrics.stringWidth(Integer.toString(tallies[index]));

         if (stringwidth > tallywidth) {
            tallywidth = stringwidth;
         }
      }

      return samplesize + samplespace + outputwidth + outputspace + tallywidth +
             tallyspace + percentwidth;
   } // end method getBrushWidth

   /**
    * Get the height of this component.
    *
    * @return height
    */
   public double getHeight() { return height; }

   /**
    * Get the width of this component.
    *
    * @return width
    */
   public double getWidth() { return width; }

   /**
    * Set the data for this component.
    *
    * @param model The decision tree model
    * @param node  decision tree node
    */
   public void setData(ViewableDTModel model, ViewableDTNode node) {
      this.model = (NominalViewableDTModel) model;
      this.node = (NominalViewableDTNode) node;

      findValues();

      scheme = new DecisionTreeScheme(outputs.length);

      width =
         leftinset + tickmark + (barwidth + barspace) * values.length +
         rightinset;
      yincrement = height / (ygrid + 1);
      yscale = (height - 2 * yincrement) / scalesize;
   }

   //~ Inner Classes ***********************************************************

   /**
    * Expanded view for a nominal node.
    *
    * @author  $Author: clutter $
    * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
    */
   private class NominalExpanded extends JPanel {

       /** constant for Split: */
      static private final String SPLIT = "Split: ";

       /** constant for Leaf: */
      static private final String LEAF = "Leaf: ";

       /** axis space */
      double axisspace = 4;

       /** space between bars */
      double barspace = 20;

       /** width of a bar */
      double barwidth = 80;

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

       /** the number of bars (equal to number of outputs) */
      int datasize;
       /** data top inset */
      double datatop = 10;

       /** width of data area */
      double datawidth;

       /** maximum width of percetage (100.0%) when painted to screen */
      double dpercentwidth;

       /** graph bottom inset */
      double graphbottom = 30;

       /** height of graph */
      double graphheight;

       /** graph left inset */
      double graphleft = 30;

       /** graph right inset */
      double graphright = 30;

       /** graph top inset */
      double graphtop = 30;

       /** width of graph */
      double graphwidth;

       /** height of graph */
      double gridheight = 300;

       /** stroke size for grid */
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

       /** used for formatting numbers */
      NumberFormat numberformat;

       /** output names */
      String[] outputs;

       /** buffer space between labels */
      double outputspace = 10;

       /** width to draw output labels */
      double outputwidth = 80;

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
        /** space needed to draw percent */
      double percentspace = 8;
       /** width of percent in pixels */
      double percentwidth;
       /** right inset */
      double right = 15;

       /** width of rectangle used for sample */
      double samplesize = 10;
       /** buffer space */
      double samplespace = 8;

       /** scheme holds the colors for decision tree vis */
      DecisionTreeScheme scheme;
       /** ascent for small font */
      int smallascent;
       /** font metrics for small font */
      FontMetrics smallmetrics;
       /** small tick size */
      double smalltick = 4;
       /** tallies */
      int[] tallies;
       /** buffer space for tally area */
      double tallyspace = 10;

       /** width of tally area */
      double tallywidth;
       /** space between ticks */
      double tickspace = 8;
       /** top inset */
      double top = 15;
       /** holds the percentages */
      double[] values;

       /** x location where the data area is painted */
      double xdata;

       /** x location where the graph is painted */
      double xgraph;
       /** buffer space in x direction for graph */
      double xgraphspace = 15;
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
      NominalExpanded() {
         outputs = model.getUniqueOutputValues();
         values = new double[outputs.length];
         tallies = new int[outputs.length];

         for (int index = 0; index < outputs.length; index++) {

            try {

               if (node.getTotal() == 0) {
                  values[index] = 0;
               } else {
                  values[index] =
                     100 * (double) node.getOutputTally(outputs[index]) /
                        (double) node.getTotal();
               }

               tallies[index] = node.getOutputTally(outputs[index]);
            } catch (Exception exception) {
               System.out.println("Exception from getOutputTally");
            }
         }

         datasize = values.length;

         int depth = node.getDepth();
         path = new String[depth];

         if (path.length > 0) {
            pathindex = depth - 1;
            findPath(node);
         }

         scheme = new DecisionTreeScheme();

         largemetrics = getFontMetrics(scheme.expandedfont);
         largeascent = largemetrics.getAscent();

         numberformat = NumberFormat.getInstance();
         numberformat.setMaximumFractionDigits(1);

         smallmetrics = getFontMetrics(scheme.textfont);
         smallascent = smallmetrics.getAscent();
         dpercentwidth = smallmetrics.stringWidth("100.0%");
         percentwidth = smallmetrics.stringWidth("100");

         for (int index = 0; index < tallies.length; index++) {
            double width =
               smallmetrics.stringWidth(Integer.toString(tallies[index]));

            if (width > tallywidth) {
               tallywidth = width;
            }
         }

         setBackground(scheme.expandedbackgroundcolor);
      }

       /**
        * Draw everything
        * @param g2 graphics context
        */
      void drawData(Graphics2D g2) {

         // Background
         g2.setColor(scheme.expandedborderbackgroundcolor);
         g2.fill(new Rectangle2D.Double(xdata, ydata, datawidth, dataheight));

         // Data
         double x = xdata + dataleft;
         double y = ydata + datatop;

         g2.setFont(scheme.textfont);

         for (int index = 0; index < datasize; index++) {
            Color color = scheme.getNextColor();
            g2.setColor(color);
            g2.fill(new Rectangle2D.Double(x, y, samplesize, samplesize));

            x += samplesize + samplespace;
            y += samplesize;
            g2.setColor(scheme.textcolor);
            g2.drawString(outputs[index], (int) x, (int) y);

            x += outputwidth + outputspace;

            String tally = Integer.toString(tallies[index]);
            g2.drawString(tally, (int) x, (int) y);

            x += tallywidth + tallyspace;

            String value = numberformat.format(values[index]) + "%";
            g2.drawString(value, (int) x, (int) y);

            x = xdata + dataleft;
            y += samplespace;
         }
      } // end method drawData

       /**
        * Draw the graph
        * @param g2 graphics context
        */
      void drawGraph(Graphics2D g2) {

         // Background
         g2.setColor(scheme.expandedborderbackgroundcolor);
         g2.fill(new Rectangle2D.Double(xgraph, ygraph, graphwidth,
                                        graphheight));

         // Grid
         g2.setColor(scheme.expandedgraphgridcolor);
         g2.setFont(scheme.textfont);

         double yincrement = gridheight / 10;
         double x = xgraph + graphleft;
         double y = ygraph + graphheight - graphbottom;
         int val = 0;

         for (int index = 0; index <= 10; index++) {
            Integer integer = new Integer(val);
            String svalue = integer.toString();
            g2.drawString(svalue, (int) x, (int) y);

            g2.setStroke(new BasicStroke(gridstroke));
            x += percentwidth + percentspace;
            g2.draw(new Line2D.Double(x, y, x + largetick, y));
            x += largetick + tickspace;
            g2.draw(new Line2D.Double(x, y, x + gridwidth, y));

            x = xgraph + graphleft;
            y -= yincrement;
            val += 10;
         }

         // Small grid
         x =
            xgraph + graphleft + percentwidth + percentspace + largetick -
            smalltick;
         yincrement = gridheight / 20;
         y = ygraph + graphheight - graphbottom - yincrement;

         for (int index = 0; index < 10; index++) {
            g2.draw(new Line2D.Double(x, y, x + smalltick, y));
            x += smalltick + tickspace;
            g2.draw(new Line2D.Double(x, y, x + gridwidth, y));

            x =
               xgraph + graphleft + percentwidth + percentspace + largetick -
               smalltick;
            y -= 2 * yincrement;
         }

         // Bars
         x =
            xgraph + graphleft + percentwidth + percentspace + largetick +
            tickspace + barspace;

         double yscale = gridheight / 100;

         for (int index = 0; index < values.length; index++) {
            double barheight = yscale * values[index];
            y = ygraph + graphheight - graphbottom - barheight;
            g2.setColor(scheme.getNextColor());
            g2.fill(new Rectangle2D.Double(x, y, barwidth, barheight));
            x += barspace + barwidth;
         }

         x =
            xgraph + graphleft + percentwidth + percentspace + largetick +
            tickspace + barspace + barwidth / 2;
         y = ygraph + graphheight - graphbottom + smallascent + axisspace;
         g2.setColor(scheme.textcolor);

         for (int index = 0; index < outputs.length; index++) {
            String output = outputs[index];
            int outputwidth = smallmetrics.stringWidth(output);
            g2.drawString(output, (int) (x - outputwidth / 2), (int) y);
            x += barspace + barwidth;
         }
      } // end method drawGraph

       /**
        * draw the label
        * @param g2 graphics context
        */
      void drawLabel(Graphics2D g2) {
         StringBuffer label;

         if (node.getNumChildren() != 0) {
            label = new StringBuffer(SPLIT);
         } else {
            label = new StringBuffer(LEAF);
         }

         label.append(node.getLabel());

         g2.setFont(scheme.expandedfont);
         g2.setColor(scheme.expandedfontcolor);
         g2.drawString(label.toString(), (int) xlabel, (int) ylabel);
      }

       /**
        * Draw the items in the path
        * @param g2 graphics context
        */
      void drawLabelPath(Graphics2D g2) {

         // Background
         g2.setColor(scheme.expandedborderbackgroundcolor);
         g2.fill(new Rectangle2D.Double(xpath, ypath, pathwidth, pathheight));

         // Path
         double y = ypath + pathtop + smallascent;
         double x = pathleft + xpath;

         g2.setColor(scheme.textcolor);
         g2.setFont(scheme.textfont);

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

           // Label bounds
           xlabel = left;
           ylabel = top + largeascent;

           // Path bounds
           xpath = xlabel;
           ypath = ylabel + ylabelspace;

           StringBuffer sb = new StringBuffer();

           if (node.getNumChildren() != 0) {
               sb.append(SPLIT);
           } else {
               sb.append(LEAF);
           }

           sb.append(node.getLabel());
           pathwidth = largemetrics.stringWidth(sb.toString());

           for (int index = 0; index < path.length; index++) {
               int twidth = smallmetrics.stringWidth(path[index]);

               if (twidth > pathwidth) {
                   pathwidth = twidth;
               }
           }

           if (path.length > 0) {
               pathwidth += pathleft + pathright;
               pathheight =
                       pathtop + path.length * smallascent +
                               (path.length - 1) * pathleading + pathbottom;
           }

           // Data bounds
           xdata = xpath;
           ydata = ypath + pathheight + ypathspace;

           datawidth =
                   dataleft + samplesize + samplespace + outputwidth +
                           outputspace +
                           tallywidth + tallyspace + dpercentwidth + dataright;
           dataheight =
                   datatop + datasize * samplesize +
                           (datasize - 1) * samplespace +
                           databottom;

           if (pathwidth > datawidth) {
               datawidth = pathwidth;
           } else {
               pathwidth = datawidth;
           }

           // Graph bounds
           ygraph = top;
           xgraph = xpath + pathwidth + xgraphspace;

           graphheight = graphtop + gridheight + graphbottom;

           gridwidth = barwidth * datasize + barspace * (datasize + 1);
           graphwidth =
                   graphleft + percentwidth + percentspace + largetick + tickspace +
                           gridwidth + graphright;

           double width = left + pathwidth + xgraphspace + graphwidth + right;

           double pdheight = ydata + dataheight + bottom;
           double gheight = top + graphheight + bottom;
           double height;

           if (pdheight > gheight) {
               height = pdheight;
           } else {
               height = gheight;
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
   } // end class NominalExpanded
} // end class NominalView

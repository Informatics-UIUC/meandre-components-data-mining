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


/**
 * Colors and fonts used in DecisionTreeVis.  A circular array is used to obtain
 * colors for the bar charts.  Call getNextColor() to get the next color in
 * the array.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public final class DecisionTreeScheme {

   //~ Static fields/initializers **********************************************

   /** Search color */
   static public final Color searchcolor = new Color(156, 0, 0);

   /** Background color */
   static public final Color backgroundcolor = new Color(219, 217, 203);

   /** Text font */
   static public final Font textfont = new Font("Sans Serif", Font.PLAIN, 12);

   /** text color */
   static public final Color textcolor = new Color(0, 0, 0);

   /** tree background color */
   static public final Color treebackgroundcolor = new Color(255, 255, 255);

   /**  tree line level color */
   static public final Color treelinelevelcolor = new Color(219, 217, 214);

   /** tree circle stroke color */
   static public final Color treecirclestrokecolor = new Color(100, 98, 87);

   /** tree circle background color */
   static public final Color treecirclebackgroundcolor =
      new Color(252, 252, 252);

   /** tree line color */
   static public final Color treelinecolor = new Color(100, 98, 87);

   /** View nodes */
   static public final Color viewbackgroundcolor = new Color(233, 232, 231);

   /** view tick color */
   static public final Color viewtickcolor = new Color(164, 164, 164);

   /** view triangle color */
   static public final Color viewtrianglecolor = new Color(76, 76, 76);

   /** view roll color */
   static public final Color viewrollcolor = new Color(197, 195, 184);

   /** view search color */
   static public final Color viewsearchcolor = new Color(177, 72, 69);

   /** view search background color */
   static public final Color viewsearchbackgroundcolor =
      new Color(207, 215, 224);

   /** Scaled nodes */
   static public final Color scaledviewbackgroundcolor = new Color(76, 76, 76);

   /** scaled view bar color */
   static public final Color scaledviewbarcolor = new Color(219, 217, 206);

   /** Expanded graphs */
   static public final Font expandedfont =
      new Font("Sans Serif", Font.PLAIN, 18);

   /** expanded font color */
   static public final Color expandedfontcolor = new Color(51, 51, 51);

   /** expanded background color */
   static public final Color expandedbackgroundcolor = new Color(219, 217, 206);

   /** expanded border background color */
   static public final Color expandedborderbackgroundcolor =
      new Color(252, 255, 255);

   /** expanded graph grid color */
   static public final Color expandedgraphgridcolor = new Color(0, 0, 0);

   /** viewer color */
   static public final Color viewercolor = new Color(0, 0, 0);

   /** Border background color */
   static public final Color borderbackgroundcolor = new Color(233, 232, 230);

   /** border upper bevel color */
   static public final Color borderupperbevelcolor = new Color(127, 127, 127);

   /** border lower bevel color */
   static public final Color borderlowerbevelcolor = new Color(10, 10, 10);

   /** border highlight color */
   static public final Color borderhighlightcolor = new Color(242, 242, 242);

   /** border shadow color */
   static public final Color bordershadowcolor = new Color(127, 127, 127);

   /** component button font */
   static public final Font componentbuttonfont =
      new Font("Sans Serif", Font.PLAIN, 10);

   /** component label font */
   static public final Font componentlabelfont =
      new Font("Sans Serif", Font.PLAIN, 12);

   /** bar colors. */
   static public BarColors barcolors = new BarColors();

   //~ Constructors ************************************************************

   /**
    * Creates a new DecisionTreeScheme object.
    */
   public DecisionTreeScheme() { }

   /**
    * Creates a new DecisionTreeScheme object.
    *
    * @param size number of colors
    */
   public DecisionTreeScheme(int size) { barcolors.setColors(size); }

   //~ Methods *****************************************************************

   /**
    * Get the BarColors
    *
    * @return BarColors the BarColors
    */
   public BarColors getBarColors() { return barcolors; }

   /**
    * Get the next color
    *
    * @return next color
    */
   public Color getNextColor() { return barcolors.getNextColor(); }

   /**
    * Set the colors array.
    *
    * @param values new colors
    */
   public void setColors(Color[] values) { barcolors.setColors(values); }

   /**
    * Set the index into barcolors
    *
    * @param index index into barcolors
    */
   public void setIndex(int index) { barcolors.setIndex(index); }
} // end class DecisionTreeScheme

/**
 * Circular array that holds bar graph colors.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
class BarColors {

   //~ Static fields/initializers **********************************************

   /** barcolor0. */
   static private final Color barcolor0 = new Color(71, 74, 98);

   /** barcolor1. */
   static private final Color barcolor1 = new Color(191, 191, 115);

   /** barcolor2. */
   static private final Color barcolor2 = new Color(111, 142, 116);

   /** barcolor3. */
   static private final Color barcolor3 = new Color(178, 198, 181);

   /** barcolor4. */
   static private final Color barcolor4 = new Color(153, 185, 216);

   /** barcolor5. */
   static private final Color barcolor5 = new Color(96, 93, 71);

   /** barcolor6. */
   static private final Color barcolor6 = new Color(146, 205, 163);

   /** barcolor7. */
   static private final Color barcolor7 = new Color(203, 84, 84);

   /** barcolor8. */
   static private final Color barcolor8 = new Color(217, 183, 170);

   /** barcolor9. */
   static private final Color barcolor9 = new Color(140, 54, 57);

   /** barcolor10. */
   static private final Color barcolor10 = new Color(203, 136, 76);

   /** barcolors. */
   static private final Color[] barcolors =
   {
      barcolor0, barcolor1, barcolor2, barcolor3, barcolor4, barcolor5,
      barcolor6, barcolor7, barcolor8, barcolor9, barcolor10
   };

   //~ Instance fields *********************************************************

   /** colors */
   Color[] colors;

   /** index into array */
   int index;

   /** size of array */
   int size;

   //~ Methods *****************************************************************

   /**
    * Get the next color in the colors array.  increment index so subsequent
    * call will get a different color.
    *
    * @return next color
    */
   Color getNextColor() {
      Color color = colors[index];
      index++;

      if (index == size) {
         index = 0;
      }

      return color;
   }

   /**
    * Set the size of colors.  Copy the colors from barcolors into colors.
    * Colors will be reused if value is greater than barcolors.size
    *
    * @param value the new size of colors
    */
   void setColors(int value) {
      size = value;

      colors = new Color[size];

      for (index = 0; index < size; index++) {
         colors[index] = barcolors[index % barcolors.length];
      }

      index = 0;
   }

   /**
    * Set the colors used
    *
    * @param values new set of colors
    */
   void setColors(Color[] values) { colors = values; }

   /**
    * Set the index into colors
    *
    * @param index new index
    */
   void setIndex(int index) { this.index = index; }
} // end class BarColors

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

package org.meandre.components.discovery.ruleassociation.fpgrowth.support;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;


/**
 * Representation of a pattern
 *
 * @author  $Author: dfleming $
 * @version $Revision: 2985 $, $Date: 2007-01-23 17:09:36 -0600 (Tue, 23 Jan 2007) $
 */
public class FPPattern implements java.io.Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 3553517370011965398L;

   /**
    * maps attribute index to attribute name. the keys are members of <code>
    * _patternElts</code>.
    */
   static private TIntObjectHashMap _eltMap = new TIntObjectHashMap();

   //~ Instance fields *********************************************************

   /** The features (column indices) that make this pattern, as a set. */
   private TIntHashSet _patternElts = new TIntHashSet();

   /** The support (expressed in percentage) of this pattern. */
   private int _support = 0;

   //~ Constructors ************************************************************

   /**
    * Creates a new FPPattern object.
    */
   public FPPattern() { }

   /**
    * Constructs an FPPAttern with the columns indices <code>col</codE> and the
    * support level <code>supp.</code>
    *
    * @param col  int[] Columns indices that make this pattern
    * @param supp int The support of this pattern.
    */
   public FPPattern(int[] col, int supp) {
      _support = supp;

      if (col != null) {
         _patternElts.addAll(col);
      }
   }

   /**
    * Constructs an FPPattern with one column <codE>col</code> and the support
    * level <codE>supp.</codE>
    *
    * @param col  int The index of the feature that makes this pattern
    * @param supp int The support level of the pattern
    */
   public FPPattern(int col, int supp) {
      _support = supp;
      _patternElts.add(col);
   }

   //~ Methods *****************************************************************

   /**
    * Adds a mapping of index <codE>k</code> to attribute name <code>v<c/code>.</code>
    *
    * @param k int The index of the element named <code>v</codE>
    * @param v String The name of attribute with index <codE>k</code>
    */
   static public void addElementMapping(int k, String v) { _eltMap.put(k, v); }

   /**
    * Clears the index to name map.
    */
   static public void clearElementMapping() { _eltMap.clear(); }

   /**
    * Returns the name of attribute index <code>i.</code>
    *
    * @param  i int The index of an attribute that is part of this pattern.
    *
    * @return String The name of attribute index <code>i</code>
    */
   static public String getElementLabel(int i) {
      return (String) _eltMap.get(i);
   }

   /**
    * Adds a feature index to the elements set.
    *
    * @param fte int An index of a feature that is part of this pattern
    */
   public void addPatternElt(int fte) { _patternElts.add(fte); }

   /**
    * Adds features indices to the elements set.
    *
    * @param col int[] feature indices that are part of this pattern.
    */
   public void addPatternElts(int[] col) { _patternElts.addAll(col); }

   /**
    * Clears the set of the elements.
    */
   public void clearPatterns() { _patternElts.clear(); }

   /**
    * Constructs a new FPPAttern object and copies the value in this pattern to
    * it. Returns the newly constructed object.
    *
    * @return FPPattern a copy of this FPPAttenr object.
    */
   public FPPattern copy() {
      FPPattern newpat = new FPPattern();
      newpat._support = this._support;
      newpat._patternElts.addAll(_patternElts.toArray());

      return newpat;
   }

   /**
    * Returns an iterator to the pattern's elements.
    *
    * @return TIntIterator an iterator for the pattern's elements.
    */
   public TIntIterator getPattern() { return _patternElts.iterator(); }

   /**
    * Returns the size of the integers hash set - the number of attributes this
    * pattenr contains.
    *
    * @return int The number of attributes this pattenr contains
    */
   public int getSize() { return _patternElts.size(); }

   /**
    * Returns the support for this pattenr.
    *
    * @return int The support for this pattenr.
    */
   public int getSupport() { return _support; }

   /**
    * Sets the support of this pattern.
    *
    * @param s int The value for the support of this pattern
    */
   public void setSupport(int s) { _support = s; }

   /**
    * A list like String representation of this pattern. For debug means
    *
    * @return String A list like String representation of this pattern.
    */
   public String toString() {
      String retVal = "[ ";
      TIntIterator it = this._patternElts.iterator();

      while (it.hasNext()) {
         int i = it.next();
         retVal += i + ", ";
      }

      if (retVal.length() > 2) {
         retVal = retVal.substring(0, retVal.length() - 2);
      }

      retVal += " ]";

      return retVal;
   }
} // end class FPPattern

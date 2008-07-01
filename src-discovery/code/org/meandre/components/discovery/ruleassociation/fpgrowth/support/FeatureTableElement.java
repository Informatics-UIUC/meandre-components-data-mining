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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A representaiton of a feature column in a table. Has references to the data,
 * the original index, label and count.
 *
 * @author  $Author: vered $
 * @version $Revision: 2904 $, $Date: 2006-08-15 15:39:36 -0500 (Tue, 15 Aug 2006) $
 */
public class FeatureTableElement {

   //~ Instance fields *********************************************************

   /**
    * The sum of the data of this feature in the original table.
    */
   private int _cnt = 0;

   /** The feature's index if all features are ordered by frequency in the original table.*/
   private int _lbl = -1;

   /** The position of this feature column in the original table. */
   private int _pos = -1;

   /**
    * An arrya list of <code>
    * ncsa.d2k.modules.core.discovery.ruleassociation.fpgrowth.FPTreeNode</code>
    * objects.
    */
   private ArrayList _ptrs = new ArrayList();

   //~ Constructors ************************************************************

   /**
    * Constructs a FeatureTableElement object with label, count and original
    * position.
    *
    * @param lbl int The feature's index if all features are ordered by frequency in the original table.
    * @param cnt int The sum of the data of this feature in the original table.
    * @param pos int Theoriginal position of this feature in the original table
    */
   public FeatureTableElement(int lbl, int cnt, int pos) {
      _lbl = lbl;
      _cnt = cnt;
      _pos = pos;
   }

   /**
    * Constructs a FeatureTableElement object with label, count, original
    * position and the data.
    *
    * @param lbl   int The feature's index if all features are ordered by frequency in the original table.
    * @param cnt   int The sum of the data of this feature in the original table.
    * @param pos   int Theoriginal position of this feature in the original
    *              table
    * @param nodes Collection The data elements. holds <code>
    *              ncsa.d2k.modules.core.discovery.ruleassociation.fpgrowth.FPTreeNode</code>
    *              objects
    */
   public FeatureTableElement(int lbl, int cnt, int pos, Collection nodes) {
      _lbl = lbl;
      _cnt = cnt;
      _pos = pos;

      if (nodes != null) {
         _ptrs = new ArrayList();
         _ptrs.addAll(nodes);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Adds <codE>node</code> to the list of data elements.
    *
    * @param node FPTreeNode A data elements to be added to the pointers list.
    */
   public void addPointer(FPTreeNode node) { _ptrs.add(node); }


   /**
    * Clears the array list with the data elements.
    */
   public void clearList() {
      _ptrs.clear();
      _ptrs = null;
   }

   /**
    * Returns the sum of the data of this feature in the original table.
    *
    * @return int The sum of the data of this feature in the original table.
    */
   public int getCnt() { return _cnt; }


   /**
    * Returns the feature's index if all features are ordered by frequency in the original table.
    *
    * @return int The feature's index if all features are ordered by frequency in the original table.
    */
   public int getLabel() { return _lbl; }

   /**
    * Returns the collection with the data elements.
    *
    * @return List The data elements of this feature. Hlds <code>
    *         ncsa.d2k.modules.core.discovery.ruleassociation.fpgrowth.FPTreeNode</code>
    *         objects
    */
   public List getPointers() { return _ptrs; }


   /**
    * Returns an iterator over the list of the data elements.
    *
    * @return Iterator an iterator over the list of the data elements
    */
   public Iterator getPointersIter() { return _ptrs.iterator(); }

   /**
    * Returns the original index of this feature's column in the original table.
    *
    * @return int
    */
   public int getPosition() { return _pos; }
} // end class FeatureTableElement

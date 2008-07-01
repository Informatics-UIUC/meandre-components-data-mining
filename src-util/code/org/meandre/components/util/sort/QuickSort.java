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

package org.meandre.components.util.sort;

/**
 * This function sorts a 2-d double array by row based on the first column
 * value.
 *
 * @author  $Author: mcgrath $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.3 $, $Date: 2006/07/27 17:22:59 $
 */
public class QuickSort {

   //~ Methods *****************************************************************

   /**
    * Private method to implement the recursive quick sort: partition, sort
    * left, sort right. Return the merged result.
    *
    * @param  A Description of parameter A.
    * @param  p Description of parameter p.
    * @param  r Description of parameter r.
    *
    * @return Description of return value.
    */
   static private double[][] doSort(double[][] A, int p, int r) {

      if (p < r) {
         int q = partition(A, p, r);
         doSort(A, p, q);
         doSort(A, q + 1, r);
      }

      return A;
   }

   /**
    * Private method to partition the array.
    *
    * @param  A Description of parameter A.
    * @param  p Description of parameter p.
    * @param  r Description of parameter r.
    *
    * @return The index of the partition.
    */
   static private int partition(double[][] A, int p, int r) {
      double x = A[p][0];

      int i = p - 1;
      int j = r + 1;

      while (true) {

         do {
            j--;
         } while (A[j][0] > x);

         do {
            i++;
         } while (A[i][0] < x);

         if (i < j) {
            double[] temp = A[i];
            A[i] = A[j];
            A[j] = temp;
         } else {
            return j;
         }
      }
   }

   /**
    * Implementation of standard quicksort.
    *
    * @param  A Description of parameter A.
    *
    * @return Description of return value.
    */
   static public double[][] sort(double[][] A) {
      return doSort(A, 0, A.length - 1);
   }
} // end class QuickSort

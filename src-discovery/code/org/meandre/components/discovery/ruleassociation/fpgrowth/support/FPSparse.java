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


import java.util.*;
import gnu.trove.*;


public class FPSparse implements java.io.Serializable{


  private int[] _columns = null;
  private int[] _labels = null;
  private TIntObjectHashMap _rows = new TIntObjectHashMap();
  private int _numcols = -1;
  private int _colcnt = 0;


  public FPSparse(int numcols) {
    _columns = new int[numcols];
    _labels = new int[numcols];
    _numcols = numcols;
  }


  public int getLabel(int col){
      return _labels[col];
  }

  public int getNumColumns(){
    return _colcnt;
  }

  public int getNumRows(){
    return _rows.size();
  }

  public void addColumn(int lbl){
    _labels[_colcnt++] = lbl;
  }

  public int getInt(int row, int col) {
      return ((TIntIntHashMap)_rows.get(row)).get(col);
  }

  public void setInt(int data, int row, int col){
    _columns[col] = _columns[col] + data;

    //check for row
    if (_rows.containsKey(row)){
      ((TIntIntHashMap)_rows.get(row)).put(col, data);
    } else {
      TIntIntHashMap iihm = new TIntIntHashMap();
      _rows.put(row, iihm);
      iihm.put(col, data);
    }
  }

  public int getColumnTots(int col){
      return _columns[col];
  }

  public int[] getRowIndices(int row){
      return ((TIntIntHashMap)_rows.get(row)).keys();
  }
}



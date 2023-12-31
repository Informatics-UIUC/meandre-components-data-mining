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

package org.seasr.meandre.applet.ruleassociation;
import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 Render a rule, the ifs and thens are represented by some
 appropriate graphic.
 */
class SortButtonCellEditor implements TableCellEditor {
    //JButton confidence;
    //JButton support;
    //JButton last = null;
    JRadioButton confidence;
    JRadioButton support;
    JRadioButton last = null;
    Vector listeners = new Vector();

    /**
     * Given the buttons we will return from the getCellRenderer.
     * @param c the confidence button.
     * @param s the support button.
     */
    SortButtonCellEditor(JRadioButton c, JRadioButton s) {
        super();
        this.confidence = c;
        this.support = s;
    }

    // Methods
    public Component getTableCellEditorComponent(JTable jTable, Object object,
                                                 boolean boolean2,
                                                 int row, int column) {
        if (row == 0) {
            this.last = this.confidence;
        } else {
            this.last = this.support;
        }
        return this.last;
    }

    // Methods
    public Object getCellEditorValue() {
        return new Boolean(true);
    }

    public boolean isCellEditable(EventObject eventObject) {
        return true;
    }

    public boolean shouldSelectCell(EventObject eventObject) {
        return true;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public void addCellEditorListener(CellEditorListener cellEditorListener) {
        listeners.addElement(cellEditorListener);
    }

    public void removeCellEditorListener(CellEditorListener cellEditorListener) {
        listeners.removeElement(cellEditorListener);
    }

    private void fireEditingCanceled() {
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
    }

    private void fireEditingStopped() {
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
    }
}


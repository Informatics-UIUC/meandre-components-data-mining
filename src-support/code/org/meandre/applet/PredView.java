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

package org.meandre.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.meandre.applet.widgets.GraphSettings;
import org.meandre.applet.widgets.DataSet;
import org.meandre.applet.widgets.PieChart;
import org.meandre.applet.widgets.ConfusionMatrix;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.basic.DoubleColumn;
import org.seasr.datatypes.table.basic.StringColumn;
import org.seasr.datatypes.table.basic.MutableTableImpl;
import org.seasr.datatypes.table.PredictionTable;


/**
 * The TableView class.  Uses a JTable and a VerticalTableModel to
 * display the contents of a VerticalTable.
 */
class PredView extends JPanel {

    public Dimension getPreferredSize() {
        Dimension d = jtp.getPreferredSize();
        double wid = d.getWidth();
        double hei = d.getHeight();

        if (wid > 400)
            wid = 400;
        if (hei > 400)
            hei = 400;

        return new Dimension((int) wid, (int) hei);
    }

    JTabbedPane jtp;

    /**
     * Called to pass the inputs received by the module to the view.
     *
     * @param input The object that has been input.
     * @param index The index of the module input that been received.
     */
    public void setInput(Object input, int idx) throws Exception {
        PredictionTable pt = (PredictionTable) input;
        int[] outputs = pt.getOutputFeatures();
        int[] preds = pt.getPredictionSet();

        if (outputs == null)
            throw new Exception("The output attributes were undefined.");
        if (preds == null)
            throw new Exception("The prediction features were undefined.");

        jtp = new JTabbedPane();
        for (int i = 0; i < outputs.length; i++) {
            // create a new InfoArea and ConfusionMatrix for each
            // and put it in a JPanel and put the JPanel in
            // the tabbed pane

            int outCol = outputs[i];
            int predCol = preds[i];

            // create the confusion matrix
            ConfusionMatrix cm = new ConfusionMatrix(pt, outputs[i], preds[i]);

            // get the number correct from the confusion matrix
            int numCorrect = cm.correct;
            int numIncorrect = pt.getNumRows() - numCorrect;

            // append data to the JTextArea
            JTextArea jta = new JTextArea();
            jta.append("Accuracy\n");
            jta.append("   Correct Predictions: " + numCorrect + "\n");
            jta.append("   Incorrect Predictions: " + numIncorrect + "\n");
            jta.append("   Total Number of Records: " + pt.getNumRows() + "\n");
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            jta.append("\n");

            double pCorrect = ((double) numCorrect) / ((double) pt.getNumRows()) *
                              100;
            double pIncorrect = ((double) numIncorrect) /
                                ((double) pt.getNumRows()) * 100;

            jta.append("   Percent correct: " + nf.format(pCorrect) + "%\n");
            jta.append("   Percent incorrect: " + nf.format(pIncorrect) + "%\n");

            jta.setEditable(false);

            StringColumn sc = new StringColumn(2);
            sc.setString("Correct", 0);
            sc.setString("Incorrect", 1);
            DoubleColumn ic = new DoubleColumn(2);
            ic.setDouble(((double) numCorrect) / ((double) pt.getNumRows()), 0);
            ic.setDouble(((double) numIncorrect) / ((double) pt.getNumRows()),
                         1);
            Column[] col = new Column[2];
            col[0] = sc;
            col[1] = ic;
            MutableTableImpl tbl = new MutableTableImpl(col);

            DataSet ds = new DataSet("Accuracy", null, 0, 1);
            GraphSettings gs = new GraphSettings();
            gs.title = "Accuracy";
            gs.displaytitle = true;
            gs.displaylegend = true;
            PieChart pc = new PieChart(tbl, ds, gs);

            JPanel p1 = new JPanel();
            p1.setLayout(new GridLayout(1, 2));
            p1.add(new JScrollPane(jta));
            p1.add(pc);

            // add everything to this
            JPanel pp = new JPanel();
            pp.setLayout(new GridLayout(2, 1));
            pp.add(p1);
            JPanel pq = new JPanel();
            pq.setLayout(new BorderLayout());
            pq.add(new JLabel("Confusion Matrix"), BorderLayout.NORTH);
            pq.add(cm, BorderLayout.CENTER);
            pp.add(pq);
            jtp.addTab(pt.getColumnLabel(outputs[i]), pp);
        }
        setLayout(new BorderLayout());
        add(jtp, BorderLayout.CENTER);
    }
}

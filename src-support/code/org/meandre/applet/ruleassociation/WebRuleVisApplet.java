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

package org.meandre.applet.ruleassociation;

import javax.swing.JApplet;

import java.util.ArrayList;
import java.util.Iterator;

import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;

import gnu.trove.TIntArrayList;

import org.meandre.components.datatype.table.basic.DoubleColumn;
import org.meandre.components.datatype.table.basic.MutableTableImpl;
import org.meandre.components.datatype.table.Column;
import org.meandre.components.datatype.table.basic.IntColumn;
import org.meandre.components.datatype.table.basic.TableImpl;

import org.meandre.components.discovery.ruleassociation.FreqItemSet;
import org.meandre.components.discovery.ruleassociation.RulePMMLTags;
import org.meandre.components.discovery.ruleassociation.RuleTable;

/**
* <p>Title: Rule Association Visualization</p>
*
* <p>Description: A visualization applet for understanding association rules</p>
*
* <p>Copyright: Copyright (c) 2008</p>
*
* <p>Company: Automated Learning Group, NCSA</p>
*
* @author Lily Dong
*/

public class WebRuleVisApplet extends JApplet implements RulePMMLTags {
    RuleTable ruleTable;
    Document document;
    //Image img = null;
    String message;

    double minimumConfidence, minimumSupport;
    int numberOfTransactions;

    /**
     * Executed each time the applet is loaded or reloaded.
     */
    public void init() {
        String location = getParameter("servletURL") + "?applet=true";

        try {
            URL testServlet = new URL(location);
            URLConnection servletConnection = testServlet.openConnection();
            //servletConnection.setRequestProperty("Content-Type","application/octet-stream");
            InputStream inputStreamFromServlet = servletConnection.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStreamFromServlet);
            //ruleTable = (RuleTable) ois.readObject();
            document = (Document)ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*try {
            MediaTracker m = new MediaTracker(this);
            InputStream is = getClass().getResourceAsStream("/images/abc.gif");
            //
            // if your image is in a subdir in the jar then
            //    InputStream is = getClass().getResourceAsStream("img/image.gif");
            //  for example
            //
            BufferedInputStream bis = new BufferedInputStream(is);
            // a buffer large enough for our image
            //
            // can be
            //   byte[] byBuf = = new byte[is.available()];
            //   is.read(byBuf);  or something like that...
            byte[] byBuf = new byte[10000];

            int byteRead = bis.read(byBuf, 0, 10000);
            img = Toolkit.getDefaultToolkit().createImage(byBuf);
            m.addImage(img, 0);
            m.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //---------------
        Element root = document.getRootElement();
        String version = root.attribute("version").getValue();

        /*if (Double.parseDouble(version) != 2)
            throw new Exception("PMML version 2.0 requried.");*/

        Element stats = root.element(ASSOC_MODEL);

        String functionName = stats.attribute("functionName").getValue();
        /*int*/ numberOfTransactions = Integer.parseInt(stats.attribute(NUM_TRANS).
                                                    getValue());
        /*double*/ minimumSupport = Double.parseDouble(stats.attribute(MIN_SUP).
                                                   getValue());
        /*double*/ minimumConfidence = Double.parseDouble(stats.attribute(MIN_CON).
                                                      getValue());
        int numberOfItems = Integer.parseInt(stats.attribute(NUM_ITEM).getValue());
        int numberOfItemsets = Integer.parseInt(stats.attribute(this.NUM_ITEMSETS).
                                                getValue());
        int numberOfRules = Integer.parseInt(stats.attribute(this.NUM_RULE).
                                             getValue());

        String[] items = new String[numberOfItems];
        FreqItemSet[] fis = new FreqItemSet[numberOfItemsets];

        int[] antecedents = new int[numberOfRules];
        int[] consequents = new int[numberOfRules];
        double[] support = new double[numberOfRules];
        double[] confidence = new double[numberOfRules];

        // read in the items
        Iterator itemIterator = stats.elementIterator(ITEM);
        while (itemIterator.hasNext()) {
            Element currentItem = (Element) itemIterator.next();
            int id = Integer.parseInt(currentItem.attribute(ID).getValue());
            String value = currentItem.attribute(VALUE).getValue();
            items[id] = value;
        }

        // read in the item sets
        Iterator itemsetIterator = stats.elementIterator(ITEMSET);
        while (itemsetIterator.hasNext()) {
            Element currentItemset = (Element) itemsetIterator.next();
            int id = Integer.parseInt(currentItemset.attribute(ID).getValue());
            Attribute sup = currentItemset.attribute(SUPPORT);
            double supp = 0;
            if (sup != null)
                supp = Double.parseDouble(sup.getValue());

            TIntArrayList itms = new TIntArrayList();
            Iterator itemrefIter = currentItemset.elementIterator(ITEMREF);
            while (itemrefIter.hasNext()) {
                Element ir = (Element) itemrefIter.next();
                int ref = Integer.parseInt(ir.attribute(ITEM_REF).getValue());
                itms.add(ref);
            }

            // now make a new FreqItemSet
            FreqItemSet is = new FreqItemSet();
            is.numberOfItems = itms.size();
            is.items = itms;
            is.support = supp;
            fis[id] = is;
        }

        int idx = 0;
        Iterator ruleIterator = stats.elementIterator(ASSOC_RULE);
        while (ruleIterator.hasNext()) {
            Element rule = (Element) ruleIterator.next();
            double sup = Double.parseDouble(rule.attribute(SUPPORT).getValue());
            double conf = Double.parseDouble(rule.attribute(CONFIDENCE).getValue());
            int ant = Integer.parseInt(rule.attribute(ANTECEDENT).getValue());
            int cons = Integer.parseInt(rule.attribute(CONSEQUENT).getValue());
            support[idx] = sup;
            confidence[idx] = conf;
            antecedents[idx] = ant;
            consequents[idx] = cons;
            idx++;
        }
        IntColumn ac = new IntColumn(antecedents);
        ac.setLabel("Head");
        IntColumn cc = new IntColumn(consequents);
        cc.setLabel("Body");
        DoubleColumn sc = new DoubleColumn(support);
        sc.setLabel("Support");
        DoubleColumn conc = new DoubleColumn(confidence);
        conc.setLabel("Confidence");

        Column[] cols = {ac, cc, sc, conc};
        TableImpl ti = new MutableTableImpl(cols);

        ArrayList names = new ArrayList(items.length);
        for (int i = 0; i < items.length; i++)
            names.add(items[i]);

        ArrayList sets = new ArrayList(fis.length);
        for (int i = 0; i < fis.length; i++) {
            sets.add(fis[i]);
        }

        ruleTable = new RuleTable(ti, minimumConfidence, minimumSupport,
                                     numberOfTransactions,
                                     names, sets);
        //---------------

        RuleVisView rvv = new RuleVisView();
        rvv.initView();

        try {
            rvv.setInput((Object)ruleTable, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getContentPane().add(rvv);
    }

    /**
     * Executed when the applet is loaded or revisited.
     */
    public void start() {}

    /**
    * Executed when the user leaves the applet's page.
    */
    public void stop() {}

    /**
     * Clean up the applet.
     */
    public void destroy() {}
}

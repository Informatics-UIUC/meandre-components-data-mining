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

package org.meandre.components.discovery.ruleassociation;
import java.io.*;
import java.util.*;

import org.meandre.components.discovery.ruleassociation.support.FreqItemSet;
import org.meandre.components.discovery.ruleassociation.support.RulePMMLTags;
import org.meandre.components.discovery.ruleassociation.support.RuleTable;

import org.dom4j.*;

import org.meandre.core.*;
import org.meandre.annotations.*;


/**
 * @author Lily Dong
 */

@Component(creator="Lily Dong",
           description="Write a RuleAssociationModel out in PMML(Predictive Model Markup Language) format complying with the PMML 2.0 DTD.",
           name="Write Rule Assoc PMML",
           tags="frequent pattern mining, rule association",
           baseURL="meandre://seasr.org/components/")

public class WriteRuleAssocPMML  implements ExecutableComponent, RulePMMLTags {
    @ComponentInput(description="Read a representaiton of association rules." +
    		"<br>TYPE: org.meandre.components.discovery.ruleassociation.support.RuleTable",
                   name= "ruleTable")
    final static String DATA_INPUT = "ruleTable";

    @ComponentOutput(description="Output document for PMML." +
    		"<br>TYPE: org.dom4j.Document",
                     name="document")
    public final static String DATA_OUTPUT = "document";

    public void initialize(ComponentContextProperties ccp) {}
    public void dispose(ComponentContextProperties ccp) {}

    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {
        RuleTable rt = (RuleTable)cc.getDataComponentFromInput(DATA_INPUT);
        cc.pushDataComponentToOutput(DATA_OUTPUT, writePMML(rt));
    }

    public static Document writePMML(RuleTable rt) {//, String fileName) {

        Document document = DocumentHelper.createDocument();

        // Root
        Element root = document.addElement("PMML");
        root.addAttribute("version", "3.0");
        root.addNamespace("", "http://www.dmg.org/PMML-3_0");
        root.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        // Header
        Element header = root.addElement("Header");
        header.addAttribute("copyright", "NCSA ALG");
        header.addAttribute("description", "association rules");

        // Data dictionary
        Element dataDictionary = root.addElement(DATA_DICT);
        Element datafield = dataDictionary.addElement(DATA_FIELD);

        datafield.addAttribute(NAME, "transaction");
        datafield.addAttribute(OPTYPE, CATEGORICAL);

        datafield = dataDictionary.addElement(DATA_FIELD);
        datafield.addAttribute(NAME, "item");
        datafield.addAttribute(OPTYPE, CATEGORICAL);

        // Association model
        List items = rt.getNamesList();
        List itemSets = rt.getItemSetsList();

        Element assocModel = root.addElement(ASSOC_MODEL);
        assocModel.addAttribute(FUNCTION_NAME, "associationRules");
        assocModel.addAttribute(NUM_TRANS,
                                Integer.toString(rt.getNumberOfTransactions()));
        assocModel.addAttribute(MIN_SUP,
                                Double.toString(rt.getMinimumSupport()));
        assocModel.addAttribute(MIN_CON,
                                Double.toString(rt.getMinimumConfidence()));
        assocModel.addAttribute(NUM_ITEM,
                                Integer.toString(items.size()));
        assocModel.addAttribute(NUM_ITEMSETS,
                                Integer.toString(itemSets.size()));
        assocModel.addAttribute(NUM_RULE,
                                Integer.toString(rt.getNumRules()));
        //Mining schema
        Element miningSchema = assocModel.addElement(MINING_SCHEMA);
        Element miningField = miningSchema.addElement(MINING_FIELD);

        miningField.addAttribute(NAME, "transaction");

        miningField = miningSchema.addElement(MINING_FIELD);
        miningField.addAttribute(NAME, "item");

        // Association items
        for (int i = 0; i < items.size(); i++) {
            Element assocItem = assocModel.addElement(ITEM);
            assocItem.addAttribute(ID, Integer.toString(i));
            assocItem.addAttribute(VALUE, (String) items.get(i));
        }

        // Association itemsets
        for (int i = 0; i < itemSets.size(); i++) {
            FreqItemSet fis = (FreqItemSet) itemSets.get(i);

            Element set = assocModel.addElement(ITEMSET);
            set.addAttribute(ID, Integer.toString(i));
            set.addAttribute(SUPPORT, Integer.toString((int) fis.support));

            int[] vals = fis.items.toNativeArray();
            for (int j = 0; j < vals.length; j++) {
                Element assocItemRef = set.addElement(ITEMREF);
                assocItemRef.addAttribute(ITEM_REF, Integer.toString(vals[j]));
            }
        }

        // Association rules
        for (int i = 0; i < rt.getNumRules(); i++) {
            int hd = rt.getRuleAntecedentID(i);
            int bd = rt.getRuleConsequentID(i);
            double conf = rt.getConfidence(i);
            double supp = rt.getSupport(i);

            Element assocRule = assocModel.addElement(ASSOC_RULE);
            assocRule.addAttribute(SUPPORT, Double.toString(supp));
            assocRule.addAttribute(CONFIDENCE, Double.toString(conf));
            assocRule.addAttribute(ANTECEDENT, Integer.toString(hd));
            assocRule.addAttribute(CONSEQUENT, Integer.toString(bd));
        }


        /* try {
            XMLWriter writer = new XMLWriter(new FileWriter("/tmp/tmp.out"),
                                             OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return document;
    }
}

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

package org.seasr.meandre.components.prediction.decisiontree.c45;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.C45TreeBuilderOPT;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode;


/**
 * Build a C4.5 decision tree. The tree is build recursively, always choosing
 * the attribute with the highest information gain as the root. The gain ratio
 * is used, whereby the information gain is divided by the information given by
 * the size of the subsets that each branch creates. This prevents highly
 * branching attributes from always becoming the root. The minimum number of
 * records per leaf can be specified. If a leaf is created that has less than
 * the minimum number of records per leaf, the parent will be turned into a leaf
 * itself.
 *
 * @author  David Clutter
 * @author  Lily Dong
 * @version $Revision: 3031 $, $Date: 2007-05-21 15:06:39 -0500 (Mon, 21 May 2007) $
 */
@Component(creator="Lily Dong",
           description="Build a C4.5 decision tree. The tree is build recursively, " +
           "always choosing the attribute with the highest information gain as the root. "+
           "The gain ratio is used, whereby the information gain is divided by the information given by " +
           "the size of the subsets that each branch creates. This prevents highly " +
           "branching attributes from always becoming the root. The minimum number of " +
           "records per leaf can be specified. If a leaf is created that has less than " +
           "the minimum number of records per leaf, the parent will be turned into a leaf itself.",
           name="C45TreeBuilder",
           tags="decision tree, c4.5, prediction",
           baseURL="meandre://seasr.org/components/data-mining/")

public class C45TreeBuilder extends C45TreeBuilderOPT implements ExecutableComponent {

    //~ Methods *****************************************************************

    @ComponentInput(description = "Read org.seasr.datatypes.datamining.table.ExampleTable to build a decision tree.",
                    name = "exampleTable")
    final static String DATA_INPUT = "exampleTable";

    @ComponentOutput(description = "Output the root of the decision tree built by this module. " +
                     "The root is of type org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode.",
                     name = "treeNode")
    public final static String DATA_OUTPUT = "treeNode";

    @ComponentProperty(defaultValue = "0.001",
                       description = "The minimum ratio of records in a leaf to "+
                       "the total number of records in the tree. " +
                       "The tree construction is terminated when "+
                       "this ratio is reached.",
                       name = "minimumRatioPerLeaf")
    final static String DATA_PROPERTY_1 = "minimumRatioPerLeaf";

    @ComponentProperty(defaultValue="true",
                       description="Control whether debugging information is output to the console.",
                       name="verbose")
    final static String DATA_PROPERTY_2 = "verbose";

    private boolean verbose = true;

    /**
    * Called when a flow is started.
    *
    * @param ccp ComponentContextProperties
    */
    public void initialize(ComponentContextProperties ccp) {}

    /**
   * Called at the end of an execution flow.
   *
   * @param ccp ComponentContextProperties
   */
    public void dispose(ComponentContextProperties ccp) {}

    /**
    * When ready for execution.
    *
    * @param cc ComponentContext
    * @throws ComponentExecutionException
    * @throws ComponentContextException
    */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        verbose = Boolean.valueOf(cc.getProperty(DATA_PROPERTY_2));

        try {
            super.setMinimumRatioPerLeaf(Double.valueOf(cc.getProperty(DATA_PROPERTY_1)));
        }catch(java.beans.PropertyVetoException pvex) {
            pvex.printStackTrace();
        }

        table = (ExampleTable) cc.getDataComponentFromInput(DATA_INPUT);

        numExamples = table.getNumRows();

        int[] inputs = table.getInputFeatures();

        try {
            if (inputs == null || inputs.length == 0) {
                throw new Exception(": No inputs were defined!");
            }

            outputs = table.getOutputFeatures();

            if (outputs == null || outputs.length == 0) {
                throw new Exception("No outputs were defined!");
            }

            if (table.isColumnScalar(outputs[0])) {
                throw new Exception(" C4.5 Decision Tree can only predict nominal values.");
            }

            // the set of examples.  the indices of the example rows
            int[] exampleSet;

            // use all rows as examples at first
            exampleSet = new int[table.getNumRows()];

            for (int i = 0; i < table.getNumRows(); i++) {
                exampleSet[i] = i;

                // use all columns as attributes at first
            }

            int[] atts = new int[inputs.length];

            for (int i = 0; i < inputs.length; i++) {
                atts[i] = inputs[i];

            }

            DecisionTreeNode rootNode = buildTree(exampleSet, atts);
            cc.pushDataComponentToOutput(DATA_OUTPUT, rootNode);

            /*if(verbose)
                rootNode.print();*/
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
} // end class C45TreeBuilder

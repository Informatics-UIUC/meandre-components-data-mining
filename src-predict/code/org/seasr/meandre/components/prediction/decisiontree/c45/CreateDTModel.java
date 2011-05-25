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
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeModel;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode;


/**
 * Given a DecisionTreeNode that is the root of a decision tree, create a new
 * DecisionTreeModel from it.
 *
 * @author  $Author: David Clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2816 $, $Date: 2006-07-28 15:47:58 -0500 (Fri, 28 Jul 2006) $
 */

@Component(creator="lily Dong",
           description="Overview: Given a DecisionTreeNode that is the root " +
           "of a decision tree, creates a new DecisionTreeModel." +
           "Detailed Description: Creates a DecisionTreeModel from " +
           "Decision Tree Root.  The Example Tablemust " +
           "be the same set of examples used to construct the decision tree." +
           "Data Type Restrictions: Output feature must be nominal." +
           "Data Handling: This module will create a PredictionTable " +
           "from Example Table and proceed to make a prediction " +
           "for each example in Example Table." +
           "Scalability: This module will make a prediction for each " +
           "example in Example Table.  There must be sufficient " +
           "memory to hold these predictions.",
           name="CreateDTModel",
           tags="decision tree, c4.5, prediction",
           baseURL="meandre://seasr.org/components/data-mining/"
)
public class CreateDTModel extends AbstractExecutableComponent {

    @ComponentInput(description = "Read the root of the decision tree. " +
                    "The root is of type org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode.",
                    name = "decisionTree")
    final static String IN_DECISION_TREE = "decisionTree";

    @ComponentInput(description = "Read the table used to build the tree. " +
                    "The table is of type org.seasr.datatypes.datamining.table.ExampleTable.",
                    name = "exampleTable")
    final static String IN_EXAMPLE_TABLE = "exampleTable";


   @ComponentOutput(description = "Output a decision tree model created from the decision tree root node. " +
                    "The model is of type org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeModel.",
                    name = "treeModel")
   public final static String OUT_TREEMODEL = "treeModel";

   @ComponentProperty(defaultValue="true",
                      description="Control whether debugging information is output to the console.",
                      name="verbose")
   final static String PROP_VERBOSE = "verbose";

   private boolean verbose = true;


   //~ Methods *****************************************************************

   /**
    * Called when a flow is started.
    *
    * @param ccp ComponentContextProperties
    */
   @Override
public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
       verbose = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_VERBOSE, ccp));
   }

   /**
   * Called at the end of an execution flow.
   *
   * @param ccp ComponentContextProperties
   */
   @Override
public void disposeCallBack(ComponentContextProperties ccp) throws Exception {}

   /**
    * When ready for execution.
    *
    * @param cc ComponentContext
    * @throws ComponentExecutionException
    * @throws ComponentContextException
    */
   @Override
public void executeCallBack(ComponentContext cc) throws Exception {
       DecisionTreeNode root = (DecisionTreeNode)cc.getDataComponentFromInput(IN_DECISION_TREE);
       ExampleTable table = (ExampleTable)cc.getDataComponentFromInput(IN_EXAMPLE_TABLE);
       DecisionTreeModel mdl = new DecisionTreeModel(root, table);

       /*if(verbose)
           System.out.println("depth = " + (mdl.getViewableRoot()).getDepth() + "\t" +
                              "numOfChildren = " + (mdl.getViewableRoot()).getNumChildren() + "\t" +
                              "total = " + (mdl.getViewableRoot()).getTotal());*/

       cc.pushDataComponentToOutput(OUT_TREEMODEL, mdl);
   }
} // end class CreateDTModel

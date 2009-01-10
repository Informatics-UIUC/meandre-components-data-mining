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

package  org.meandre.components.prediction.decisiontree.c45;

import org.meandre.components.datatype.table.Table;
import org.meandre.components.datatype.table.ExampleTable;


import org.meandre.components.prediction.decisiontree.c45.support.CategoricalDecisionTreeNode;
import org.meandre.components.prediction.decisiontree.c45.support.DecisionTreeModel;
import org.meandre.components.prediction.decisiontree.c45.support.DecisionTreeNode;

import java.util.HashSet;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;


/**
 * Implements pruning of C4.5 decision trees. The error is estimated at each
 * node using the training data. A confidence level of 25% is used.<br>
 * <br>
 * Each non-leaf node in the tree is examined for pruning. One of two types of
 * pruning can be attempted: subtree replacment or subtree raising. Subtree
 * replacement can occur when all the children of a node are leaves. If the
 * error of the leaves is less than the error at the node, the node will be
 * replaced with a leaf. Subtree raising can occur when the children of a node
 * are not all leaves. The branch with the most training examples is temporarily
 * raised, and if the error induced after the raising is less than the error of
 * the original node, the replacement is left intact.<br>
 * <br>
 * The pruning process can be time-intensive. Each time a possible pruning is
 * tested, the new, pruned tree must be applied to the training dataset to find
 * an estimate of the new error. When a possible pruning is not taken, the tree
 * is reverted to its original form, but the tree must again be applied to the
 * training dataset.
 *
 * @author  $Author: David Clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */

@Component(creator="Lily Dong",
           description="This module prunes a decision tree built by the C4.5 Tree Builder. " +
           "Detailed Description: This module prunes a decision tree using a reduced-error " +
           "pruning technique.  Error estimates for the leaves and subtrees are " +
           "computed by classifying all the examples of the Example Table. " +
           "Both subtree replacement and subtree raising are used.  Subtree " +
           "replacement will replace a node by one of its leaves if the " +
           "induced error of the replacement is less than the sum of the errors " +
           "for the leaves of the node.  Subtree raising will lift a subtree if " +
           "the error for the raised subtree is less than the original.  The " +
           "complexity of pruning the tree is O(n (log n)2)." +
           "References: C4.5: Programs for Machine Learning by J. Ross Quinlan" +
           "Data Type Restrictions: The Unpruned Root must be a DecisionTreeNode " +
           "built by the C4.5 Tree Builder." +
           "Data Handling: This module will attempt to classify the examples " +
           "in the Example Table N times, where N is the number of nodes in the tree." +
           "Scalability: This module will classify the examples in the Example Table " +
           "at least once for each node of the tree.  This module will need " +
           "enough memory to hold those predictions.",
           name="C45TreePruner",
           tags="decision tree, C4.5, pruner",
           baseURL="meandre://seasr.org/components/")


public class C45TreePruner implements ExecutableComponent {
    @ComponentInput(description =
            "Read the root node of the unpruned decision tree. " +
            "The root node is of type org.meandre.components.prediction.decisiontree.c45.DecisionTreeNode.",
                    name = "treeNode")
    final static String DATA_INPUT_1 = "treeNode";

    @ComponentInput(description =
            "Read the training data that was used to build the decision tree. " +
            "The training data is of type org.meandre.components.datatype.table.ExampleTable.",
                    name = "exampleTable")
    final static String DATA_INPUT_2 = "exampleTable";

    @ComponentOutput(description =
            "Output a decision tree node which is the root of the pruned tree. " +
            "The node is of type ncsa.d2k.modules.core.prediction.decisiontree.c45.DecisionTreeNode.",
                     name = "treeNode")
    public final static String DATA_OUTPUT = "treeNode";

    @ComponentProperty(defaultValue="true",
                       description="Control whether debugging information is output to the console",
                       name="verbose")
    final static String DATA_PROPERTY = "verbose";

    private boolean verbose = true;

    //~ Instance fields *********************************************************

    /** model that uses the tree. */
    private DecisionTreeModel dtm;

    /** training table. */
    private ExampleTable et;

    /** visited nodes, used in DFS */
    private HashSet gray;

    /** root node of the tree. */
    private DecisionTreeNode rootNode;

    /** unvisited nodes, used in DFS */
    private HashSet white;

    /** Z */
    private double Z = 0.69;

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
    public void dispose(ComponentContextProperties ccp) {
        rootNode = null;
        et = null;
        dtm = null;
    }

    //~ Methods *****************************************************************

    /**
     * f = E/N N = number of instances z = 0.69.
     *
     * @param  N Description of parameter N.
     * @param  E Description of parameter E.
     * @param  z Description of parameter z.
     *
     * @return Description of return value.
     */
    private static double errorEstimate(double N, double E, double z) {
        double f = E / N;

        double usq = (f / N);
        usq -= (Math.pow(f, 2) / N);
        usq += (Math.pow(z, 2) / (4 * Math.pow(N, 2)));

        double numerator =
                f + (Math.pow(z, 2) / (2 * N)) + (z * Math.pow(usq, .5));
        double denominator = 1 + (Math.pow(z, 2) / N);

        return numerator / denominator;
    }

    /**
     * Return true if the children of rt are all leaves.
     *
     * @param  rt decision tree node
     *
     * @return true if all the children of rt are leaves
     */
    private boolean areAllChildrenLeaves(DecisionTreeNode rt) {

        for (int i = 0; i < rt.getNumChildren(); i++) {

            if (!rt.getChild(i).isLeaf()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Perform a depth-first search, starting at the root.
     *
     * @param  rt the root
     *
     * @throws Exception when something goes wrong
     */
    private void DFS(DecisionTreeNode rt) throws Exception {

        for (int i = 0; i < rt.getNumChildren(); i++) {
            DecisionTreeNode dtn = rt.getChild(i);

            if (white.contains(dtn)) {
                DFSvisit(dtn);
            }
        }

        visit(rt);
    }

    /**
     * Visit a decision tree node.  Part of DFS.
     *
     * @param  rt decision tree node
     *
     * @throws Exception when something goes wrong
     */
    private void DFSvisit(DecisionTreeNode rt) throws Exception {
        white.remove(rt);
        gray.add(rt);

        for (int i = 0; i < rt.getNumChildren(); i++) {
            DecisionTreeNode dtn = rt.getChild(i);

            if (white.contains(dtn)) {
                DFSvisit(dtn);
            }
        }

        visit(rt);
    }

    /**
     * Put node and all its children in white set.  Part of DFS
     *
     * @param rt decision tree node
     */
    private void putAllInWhite(DecisionTreeNode rt) {
        white.add(rt);

        for (int i = 0; i < rt.getNumChildren(); i++) {
            putAllInWhite(rt.getChild(i));
        }
    }

    /**
     * Visit a node. The pruning operations are done here.
     *
     * @param  node the root
     *
     * @throws Exception when something goes wrong
     */
    private void visit(DecisionTreeNode node) throws Exception {
        gray.remove(node);

        // we cannot prune a leaf, only a node
        if (node.isLeaf()) {
            return;
        } else {

            // we will calculate the error estimation for the current node
            double originalNodeErrorEstimate =
                    errorEstimate((double) (node.getTotal()),
                                  (double) (node.getNumIncorrect()), Z);

            // now we must replace this node by the branch with the most
            // training examples and recalculate the error

            // find the child with the most training examples
            DecisionTreeNode cd = node.getChildWithMostTrainingExamples();

            // now find which branch of our parent that node represents
            DecisionTreeNode parent = node.getParent();

            if (parent != null) {

                // find the index of node
                int idx = -1;
                String lbl = null;

                for (int i = 0; i < parent.getNumChildren(); i++) {
                    DecisionTreeNode cld = (DecisionTreeNode) parent.getChild(i);

                    if (cld == node) {
                        idx = i;

                        if (parent instanceof CategoricalDecisionTreeNode) {
                            lbl =
                                    ((CategoricalDecisionTreeNode) parent).
                                    getSplitValues()[i];
                        } else {
                            lbl = parent.getBranchLabel(i);
                        }

                        break;
                    }
                }

                // find the index of the child to node
                int cdIdx = -1;
                String cdLabel = null;

                DecisionTreeNode prt = cd.getParent();

                for (int i = 0; i < prt.getNumChildren(); i++) {
                    DecisionTreeNode c = prt.getChild(i);

                    if (c == cd) {
                        cdIdx = i;

                        if (prt instanceof CategoricalDecisionTreeNode) {
                            cdLabel =
                                    ((CategoricalDecisionTreeNode) prt).
                                    getSplitValues()[i];
                        } else {
                            cdLabel = prt.getBranchLabel(i);
                        }

                        break;
                    }
                }

                // do the replacement, clear, re-do predict.
                parent.setBranch(idx, lbl, cd);

                rootNode.clear();
                dtm = new DecisionTreeModel(rootNode, et);
                dtm.predict(et);

                double replacementNodeErrorEstimate =
                        errorEstimate((double) cd.getTotal(),
                                      (double) cd.getNumIncorrect(), Z);

                // if the error of the replacement is less than the error of the
                // original, leave the replacement intact.
                if (replacementNodeErrorEstimate <= originalNodeErrorEstimate) {
                    ;
                } else {
                    parent.setBranch(idx, lbl, node);
                    node.setBranch(cdIdx, cdLabel, cd);
                    // otherwise revert to the original, unreplaced tree.

                    // clear, re-do predict
                    rootNode.clear();
                    dtm = new DecisionTreeModel(rootNode, et);
                    dtm.predict(et);
                }
            }
            // otherwise we are attempting to replace the root node.
            // node == rootNode
            else {
                // keep a temporary reference to the root node

                // perform prediction as if cd is the root
                rootNode.clear();
                dtm = new DecisionTreeModel(cd, et);
                dtm.predict(et);

                double replacementNodeErrorEstimate =
                        errorEstimate((double) cd.getTotal(),
                                      (double) cd.getNumIncorrect(), Z);

                // if the replacement is less than or equal to the error of the
                // original, leave the replacement intact
                if (replacementNodeErrorEstimate <= originalNodeErrorEstimate) {
                    rootNode = cd;
                }

                rootNode.clear();
                dtm = new DecisionTreeModel(rootNode, et);
                dtm.predict(et);
            }
        } // end if
    } // end method visit

    /**
     * When ready for execution.
     *
     * @param cc ComponentContext
     * @throws ComponentExecutionException
     * @throws ComponentContextException
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        verbose = Boolean.valueOf(cc.getProperty(DATA_PROPERTY));

        rootNode = (DecisionTreeNode) cc.getDataComponentFromInput(DATA_INPUT_1);;
        et = (ExampleTable) cc.getDataComponentFromInput(DATA_INPUT_2);;

        // clear
        rootNode.clear();

        // we need a decsion tree model so we can call the predict() method
        dtm = new DecisionTreeModel(rootNode, et);

        // now call the predict method.  we must predict so that we can
        // get the tallies of the correct and incorrect predictions on the
        // training data
        try {
            dtm.predict(et);
        }catch(Exception ex) {
            ex.printStackTrace();
        }

        white = new HashSet();
        gray = new HashSet();

        // now put all the nodes in the white category. they haven't been
        // seen yet
        putAllInWhite(rootNode);

        // perform a depth-first search
        try {
            DFS(rootNode);
        }catch(Exception ex) {
            ex.printStackTrace();
        }

        // clear the tallies of the tree
        rootNode.clear();

        // set training to false.
        rootNode.setTraining(false);

        // push the pruned tree out
        cc.pushDataComponentToOutput(DATA_OUTPUT, rootNode);

        /*if(verbose)
            rootNode.print();*/
    } // end method doit
} // end class C45TreePruner

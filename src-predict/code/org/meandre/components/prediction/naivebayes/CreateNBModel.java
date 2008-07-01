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

package org.meandre.components.prediction.naivebayes;

import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.prediction.naivebayes.support.NaiveBayesModel;
import org.meandre.components.transform.binning.support.BinTree;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;


/**
 * <p>Overview: Generates a NaiveBayesModel from the given BinTree. The Naive
 * Bayes Model performs all necessary calculations.</p>
 *
 * <p>Detailed Description: Given a BinTree object that contains counts for each
 * discrete item in the training data set, this module creates a Naive Bayesian
 * learning model. This method is based on Bayes's rule for conditional
 * probability. It \"naively\" assumes independence of the input features.</p>
 *
 * <p>Data Type Restrictions: This model can only use nominal data as the inputs
 * and can only classify one nominal output. The binning procedure will
 * discretize any scalar inputs in the training data, but the output data is not
 * binned and should be nominal. If the output data is binned, visualizations
 * and prediction generated by the created model might be wrong and/or too
 * corrupted to be displayed.</p>
 *
 * <p>Data Handling: The input data is neither modified nor destroyed.</p>
 *
 * <P>Missing Values Handling: Output data should be clean of missing values.
 * </P>
 *
 * <p>Scalability: The module utilizes the counts in the BinTree, and as such
 * does not perform any significant computations.";</p>
 *
 * @author  David Clutter
 * @author Lily Dong
 * @version $Revision: 2835 $, $Date: 2006-08-02 10:08:17 -0500 (Wed, 02 Aug 2006) $
 */

@Component(creator="Lily Dong",
           description="Overview: Generates a NaiveBayesModel from the given BinTree.  " +
             "The Naive Bayes Model performs all necessary calculations." +
             "Detailed Description: Given a BinTree object that contains counts for " +
             "each discrete item in the training data set, this module creates a " +
             "Naive Bayesian learning model.  This method is based on Bayes's rule " +
             "for conditional probability.  It \"naively\" assumes independence of " +
             "the input features." +
             "Data Type Restrictions: This model can only use nominal data as the inputs " +
             "and can only classify one nominal output.  The binning procedure will " +
             "discretize any scalar inputs in the training data, but the output data " +
             "is not binned and should be nominal. If the output data is binned, " +
             "visualizations and prediction generated by the created model might be wrong " +
             "and/or too corrupted to be displayed." +
             "Data Handling: The input data is neither modified nor destroyed." +
             "Missing Values Handling: Output data should be clean of missing values." +
             "Scalability: The module utilizes the counts in the BinTree, and " +
             "as such does not perform any significant computations.",
             name="CreateNBModel",
             tags="naive bayes, prediction")

public class CreateNBModel implements ExecutableComponent {
    @ComponentInput(description="Read org.meandre.components.transform.binning.BinTree " +
                    "which contains counts.",
                    name= "binTree")
    public final static String DATA_INPUT_1 = "binTree";
    @ComponentInput(description="Read org.meandre.components.datatype.table.ExampleTable " +
                    "with the data in it.",
                    name= "exampleTable")
    public final static String DATA_INPUT_2 = "exampleTable";

    @ComponentOutput(description="Outuput a Naive Bayes model module. " +
                     "It is type of org.meandre.components.prediction.naivebayes.NaiveBayesModel",
                     name="nbModel")
    public final static String DATA_OUTPUT = "nbModel";


    //~ Methods *****************************************************************

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
        BinTree bins = (BinTree) cc.getDataComponentFromInput(DATA_INPUT_1);
        ExampleTable et = (ExampleTable) cc.getDataComponentFromInput(
                DATA_INPUT_2);

        int[] outputs = et.getOutputFeatures();

        // It's got to have outputs.
        if (outputs == null || outputs.length == 0) {
            throw new ComponentExecutionException(
                    "Output feature is missing. Please select a output feature.");
        }

        // No missing values in the output columns allowed!
        for (int i = 0; i < outputs.length; i++) {

            if (et.hasMissingValues(outputs[i])) {
                throw new ComponentExecutionException(
                        "The data contains missing values in an output column, not supported by Naive Bayes.");
            }
        }

        if (et.isColumnScalar(outputs[0])) {
            throw new ComponentExecutionException(
                    "Output feature must be nominal.");
        }

        /*ModelModule*/ NaiveBayesModel mdl = new NaiveBayesModel(bins, et); //ModelModule is not in d2kbasic
        cc.pushDataComponentToOutput(DATA_OUTPUT, mdl);
    }
} // end class CreateNBModel

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

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.components.prediction.naivebayes.NaiveBayesModel;

/**
 * Prepare a NaiveBayesModel for visualization.
 *
 * @author  Lily Dong
 */

@Component(creator="Lily Dong",
           description="Overview: This module prepares a NaiveBayesModel for " +
                "visualization.  Many calculations that are needed by the visualization " +
                "are done here, before the model is visualized." +
                "Detailed Description: This module determines which of the input " +
                "features of the training data are the best predictors of the output. " +
                "This is done by performing predictions on the training data and leaving " +
                "out one input feature each time.  Data structures to hold values for " +
                "the pie charts in NaiveBayesVis are also created here." +
                "Data Type Restrictions: none" +
                "Data Handling: The data structures used to display pie charts in " +
                "NaiveBayesVis are created by this module.  These structures are stored " +
                "in Naive Bayes Model.  The number of pie charts is " +
                "proportional to the number of discrete values in the inputs of the " +
                "training data set." +
                "Scalability: This module will perform NxM predictions, " +
                "where N is the number of inputs and M is the number of training examples.",
           name="PrepareForVis",
           tags="naive bayes, visualization",
           baseURL="meandre://seasr.org/components/data-mining/")

public class PrepareForVis implements ExecutableComponent {
    @ComponentInput(description="Read org.seasr.meandre.support.components.prediction.naivebayes.NaiveBayesModel as input.",
                    name= "nbModel")
    public final static String DATA_INPUT = "nbModel";
    @ComponentOutput(description="Ouptut org.seasr.meandre.support.components.prediction.naivebayes.NaiveBayesModel " +
                     "after calculation.",
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
        NaiveBayesModel nbm = (NaiveBayesModel) cc.getDataComponentFromInput(DATA_INPUT);
        nbm.setupForVis();
        nbm.setIsReadyForVisualization(true);
        cc.pushDataComponentToOutput(DATA_OUTPUT, nbm);
    }
} // end class PrepareForVis

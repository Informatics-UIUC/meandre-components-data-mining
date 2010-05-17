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

package org.seasr.meandre.components.prediction;


import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.datatypes.datamining.model.PredictionModelModule;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.PredictionTable;



/**
 * <p>Overview: This module applies a prediction model to a table of examples
 * and makes predictions for each output attribute based on the values of the
 * input attributes.
 * Detailed Description: This module applies a previously built model to a
 * new set of examples that have the same attributes as those used to
 * train/build the model.  The module creates a new table that contains columns
 * for each of the values the model predicts, in addition to the columns found
 * in the original table.  The new columns are filled in with values predicted
 * by the model based on the values of the input attributes.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */

@Component(creator="Lily Dong",
           description="This module applies a prediction model " +
           "to a table of examples and makes predictions for each " +
           "output attribute based on the values of the input attributes." +
           "Detailed Description: This module applies a previously " +
           "built model to a new set of examples that have the " +
           "same attributes as those used to train/build the model.  " +
           "The module creates a new table that contains columns for " +
           "each of the values the model predicts, in addition to the " +
           "columns found in the original table. " +
           "The new columns are filled in with values predicted by the " +
           "model based on the values of the input attributes.",
           name="ModelPredict",
           tags="prediction",
           baseURL="meandre://seasr.org/components/data-mining/")

public class ModelPredict implements ExecutableComponent {
    @ComponentInput(description="Read the table containing the examples that the model will be applied to. " +
                    "The table is of the type org.meander.components.datatype.table.ExampleTable.",
                    name= "exampleTable")
    public final static String DATA_INPUT_1 = "exampleTable";
    @ComponentInput(description="Read the prediction model to apply, which is type of " +
                    "org.seasr.meandre.support.components.prediction.PredictionModelModule.",
                    name= "predictionModel")
    public final static String DATA_INPUT_2 = "predictionModel";

    @ComponentOutput(description="Output a table with the prediction columns filled in by the input model ." +
                     "The table is type of ncsa.d2k.modules.core.datatype.table.PredictionTable.",
                     name="predictionTable")
    public final static String DATA_OUTPUT = "predictionTable";


   //~ Methods *****************************************************************

   /**
    * When ready for execution.
    *
    * @param cc ComponentContext
    * @throws ComponentExecutionException
    * @throws ComponentContextException
    */
   public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {
      ExampleTable tt = (ExampleTable)(cc.getDataComponentFromInput(DATA_INPUT_1));
      PredictionModelModule pmm = (PredictionModelModule)(cc.getDataComponentFromInput(DATA_INPUT_2));

      PredictionTable pt;
      try {
          pt = pmm.predict(tt);
      }catch(Exception ex) {
          throw new ComponentContextException(ex.getMessage());
      }
      cc.pushDataComponentToOutput(DATA_OUTPUT, pt);
   }

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

} // end class ModelPredict

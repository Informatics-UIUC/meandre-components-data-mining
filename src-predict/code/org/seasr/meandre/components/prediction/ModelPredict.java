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

import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.model.PredictionModelModule;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.PredictionTable;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

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
 * @author  $Author: Boris Capitanu $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */

@Component(
        name = "Model Predict",
        creator = "Lily Dong",
        baseURL = "meandre://seasr.org/components/data-mining/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYTICS, prediction",
        description = "This module applies a prediction model " +
           "to a table of examples and makes predictions for each " +
           "output attribute based on the values of the input attributes." +
           "Detailed Description: This module applies a previously " +
           "built model to a new set of examples that have the " +
           "same attributes as those used to train/build the model.  " +
           "The module creates a new table that contains columns for " +
           "each of the values the model predicts, in addition to the " +
           "columns found in the original table. " +
           "The new columns are filled in with values predicted by the " +
           "model based on the values of the input attributes."
)
public class ModelPredict extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TABLE,
            description = "The table containing the examples that the model will be applied to." +
                "<br>TYPE: org.meandre.components.datatype.table.ExampleTable"
    )
    protected static final String IN_TABLE = Names.PORT_TABLE;

    @ComponentInput(
            name = "model",
            description = "The prediction model to apply." +
                "<br>TYPE: org.seasr.meandre.support.components.prediction.PredictionModelModule"
    )
    protected static final String IN_MODEL = "model";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TABLE,
            description = "A table with the prediction columns filled in by the input model." +
                "<br>TYPE: ncsa.d2k.modules.core.datatype.table.PredictionTable"
    )
    protected static final String OUT_TABLE = Names.PORT_TABLE;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        ExampleTable exampleTable = (ExampleTable) cc.getDataComponentFromInput(IN_TABLE);
        PredictionModelModule predictionModel = (PredictionModelModule) cc.getDataComponentFromInput(IN_MODEL);

        if (console.isLoggable(Level.FINE)) {
            console.fine(String.format("The example table has %,d row(s) and %,d column(s) with %,d input feature(s) and %,d output feature(s)",
                    exampleTable.getNumRows(), exampleTable.getNumColumns(), exampleTable.getInputFeatures().length, exampleTable.getOutputFeatures().length));
            console.fine(String.format("The prediction model was built with a training set of size: %,d", predictionModel.getTrainingSetSize()));
        }

        PredictionTable predictionTable = predictionModel.predict(exampleTable);
        cc.pushDataComponentToOutput(OUT_TABLE, predictionTable);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

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

package org.meandre.components.transform.binning;

import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.datatype.table.transformations.BinTransform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.beans.PropertyVetoException;
import java.text.NumberFormat;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

/**
 * Automatically discretize scalar data for the Naive Bayesian classification
 * model. This module requires a ParameterPoint to determine the method of
 * binning to be used.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2835 $, $Date: 2006-08-02 10:08:17 -0500 (Wed, 02 Aug 2006) $
 */
@Component(creator = "Lily Dong",
           description =
           "Overview: Automatically discretize scalar data for the " +
           "Naive Bayesian classification model." +
           "Detailed Description: Given a table of Examples, define the bins for each " +
           "scalar input column.  When binning Uniformly, the number of bins is determined " +
           "by Number of Bins property, and the boundaries of the bins are set so that " +
           "they divide evenly over the range of the binned column." +
           "When binning by weight, Number of Items per Bin sets the size of each bin. " +
           "The values are then binned so that in each bin there is the same number of items. " +
           "For more details see description of property Number of Items per Bin'." +
           "Data Handling: This module does not modify the input data." +
           "Scalability: The module requires enough memory to make copies of " +
           "each of the scalar input columns.",
           name = "AutoBin",
           tags = "binning, transform", dependency={"trove-2.0.3.jar"},
           baseURL="meandre://seasr.org/components/")

public class AutoBin extends AutoBinOPT implements ExecutableComponent {
    @ComponentInput(description = "Read a table of examples. It is type of " +
                    "org.meandre.components.datatype.table.ExampleTable",
                    name = "exampleTable")
    final static String DATA_INPUT = "exampleTable";

    @ComponentOutput(description =
            "Output ncsa.d2k.modules.core.datatype.table.transformations.BinTransform " +
            "that contains all the information needed to discretize the Example Table",
            name = "binTransform")
    final static String DATA_OUTPUT = "binTransform";

    @ComponentProperty(defaultValue = "0",
                       description =
            "This property is used to set the method for discretization. " +
            "Select 1 to create binsby weight.  This will create bins with " +
            "an equal number of items in each slot.  Select 0 to do uniform discretization " +
            "by specifying the number of bins. This will result in equally spaced bins " +
            "between the minimum and maximum for each scalar column. It must be 0 or 1.",
            name = "method")
    final static String DATA_PROPERTY_1 = "method";

    @ComponentProperty(defaultValue = "1",
                       description =
            "This property is used to set the number of items per bin " +
            "When binning by weight, this is the number of items that will go in each bin. " +
            "However, the bins may contain more or fewer values than weight values, " +
            "depending on how many items equal the bin limits. Typically " +
            "the last bin will contain less or equal to weight  values and the rest of the " +
            "bins will contain a number that is  equal or greater to weight values." +
            "It must be a positive integer.",
            name = "weight")
    final static String DATA_PROPERTY_2 = "weight";

    @ComponentProperty(defaultValue = "2",
                       description =
            "This property is used to set the number of bins absolutely. " +
            "This will give equally spaced bins between the minimum and maximum " +
            "for each scalar column. It must be higher than 1.",
            name = "nrOfBins")
    final static String DATA_PROPERTY_3 = "nrOfBins";


    //~ Static fields/initializers **********************************************

    /** constant for binning by weight. */
    static public final int WEIGHT = 1;

    /** constant for uniform binning. */
    static public final int UNIFORM = 0;

    //~ Instance fields *********************************************************

    /**
     * The method to use for discretization. Select 1 to create bins by weight.
     * This will create bins with an equal number of items in each slot. Select 0
     * to do uniform discretization by specifying the number of bins. This will
     * result in equally spaced bins between the minimum and maximum for each
     * scalar column.
     */
    private int binMethod = 0;

    /**
     * When binning by weight, this is the number of items that will go in each
     * bin. However, the bins may contain more or fewer values than weight
     * values, depending on how many items equal the bin limits. Typically the
     * last bin will contain less or equal to weight values and the rest of the
     * bins will contain a number that is equal or greater to weight values.
     */
    private int binWeight = 1;

    /**
     * Define the number of bins absolutely. This will give equally spaced bins
     * between the minimum and maximum for each scalar column.
     */
    private int numberOfBins = 2;

    //~ Methods *****************************************************************

    /**
     * Called when a flow is started.
     *
     * @param ccp ComponentContextProperties
     */
    public void initialize(ComponentContextProperties ccp) {
    }

    /**
     * Called at the end of an execution flow.
     *
     * @param ccp ComponentContextProperties
     */
    public void dispose(ComponentContextProperties ccp) {
        // TODO Auto-generated method stub
    }

    /**
     * When ready for execution.
     *
     * @param cc ComponentContext
     * @throws ComponentExecutionException
     * @throws ComponentContextException
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        binMethod = Integer.valueOf(cc.getProperty(DATA_PROPERTY_1));
        if (binMethod != 0 && binMethod != 1)
            throw new ComponentExecutionException(
                    "Discretization Method must be 0 or 1");

        binWeight = Integer.valueOf(cc.getProperty(DATA_PROPERTY_2));
        if (binWeight < 1)
            throw new ComponentExecutionException(
                    "Number of items per bin must be a positive integer.");

        numberOfBins = Integer.valueOf(cc.getProperty(DATA_PROPERTY_3));
        if (numberOfBins < 2)
            throw new ComponentExecutionException(
                    "Number of bins must be higher than 1.");

        tbl = (ExampleTable) cc.getDataComponentFromInput(DATA_INPUT);

        inputs = tbl.getInputFeatures();
        outputs = tbl.getOutputFeatures();

        if ((inputs == null) || (inputs.length == 0)) {
            throw new ComponentExecutionException(
            ": Please select the input features, they are missing.");
        }

        if (outputs == null || outputs.length == 0) {
            throw new ComponentExecutionException(
                    ": Please select an output feature, it is missing");
        }

        if (tbl.isColumnScalar(outputs[0])) {
            throw new ComponentExecutionException(
                    ": Output feature must be nominal. Please transform it.");
        }

        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        int type = binMethod;

        BinDescriptor[] bins;

        if (type == 0) {
            int number = numberOfBins;

            if (number < 0) {
                throw new ComponentExecutionException(
                        ": Number of bins not specified!");
            }
            try {
                bins = numberOfBins(number);
            }catch(Exception e) {
                throw new  ComponentExecutionException(e);
            }
        } else {
            int weight = binWeight;
            try {
                bins = sameWeight(weight);
            }catch(Exception e) {
                 throw new  ComponentExecutionException(e);
            }
        }

        BinTransform bt = new BinTransform(tbl, bins, false);

        cc.pushDataComponentToOutput(DATA_OUTPUT, bt);
    }
} // AutoBin

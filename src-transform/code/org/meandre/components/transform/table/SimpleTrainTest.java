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

package org.meandre.components.transform.table;

import java.util.Random;

import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.Table;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/**
 * SimpleTrainTest.java The user to select a percentage of the table to be train
 * and test. This module provides property setting to specify the
 * percent of the data to be used as training data.
 *
 * @author  Tom Redman, revised Xiaolei Li, edited R.Aydt
 * @author  Lily Dong
 * @version $Revision: 3022 $, $Date: 2007-05-18 16:58:36 -0500 (Fri, 18 May 2007) $
 */

@Component(creator="Lily Dong",
           description="This module generates a training table and a testing table from the original table. " +
           "Detailed Description: " +
           "This module presents the user with property setting which allows them to " +
           "specify the percentages of the original table examples that should be used to build "+
           "train and test tables.   The user can specify whether the train and test examples are selected " +
           "at random or sequentially from the beginning (train data) and the end (test data) of the " +
           "original examples.  If the examples are selected randomly, the user can specify the seed used " +
           "by the random number generator. " +
           "If the train and test percentages sum to more than 100 percent, some examples will appear in " +
           "both the train and test tables.   The train and test percentages can be designated " +
           "through the property setting. " +
           "Data Type Restrictions: "+
           "Although this module works with tables containing any type of data, many supervised learning " +
           "algorithms will work only on doubles. If one of these algorithms is to be used, the " +
           "conversion to floating point data should take place prior to this module.   " +
           "Data Handling: " +
           "This module does not change the original data. It creates an instance of an example table " +
           "that manages the data data differently.  " +
           "Scalability: " +
           "This module should scale linearly with the number of rows in the table.  The module needs to " +
           "be able to allocate arrays of integers to hold the indices of the test and train examples.",
           name="SimpleTrainTest",
           tags="train, test",
           baseURL="meandre://seasr.org/components/")

public class SimpleTrainTest implements ExecutableComponent {
    @ComponentInput(description="Read org.seasr.datatypes.table.Table " +
                    "containing the data that will be split into training and testing examples as input.",
                    name= "originalTable")
    public final static String DATA_INPUT = "originalTable";

    @ComponentOutput(description="Output org.seasr.datatypes.table.Table " +
                     "containing the training data",
                     name="trainTable")
    public final static String DATA_OUTPUT_1 = "trainTable";
    @ComponentOutput(description="Output org.seasr.datatypes.table.Table " +
                     "containing the test data",
                     name="testTable")
    public final static String DATA_OUTPUT_2 = "testTable";

    @ComponentProperty(defaultValue="50",
                       description="The percentage of the data to be used for training the model.",
                       name="trainPercent")
    final static String DATA_PROPERTY_1 = "trainPercent";
    @ComponentProperty(defaultValue="50",
                       description="The percentage of the data to be used for testing the model.",
                       name="testPercent")
    final static String DATA_PROPERTY_2 = "testPercent";
    @ComponentProperty(defaultValue="1",
                       description="The method to use when sampling the original examples.  " +
                       "The choices are: " +
                       "Random: Train and test examples are drawn randomly from the original table. " +
                       "Sequential: Training examples are taken sequentially from the beginning of the " +
                       "original table and testing examples are " +
                       "taken sequentially from the end of the original table. ",
                       name="samplingMethod")
    final static String DATA_PROPERTY_3 = "samplingMethod";
    @ComponentProperty(defaultValue="123",
                       description="Seed for random sampling." +
                       "Ignored if Random Sampling is not used.",
                       name="seed")
    final static String DATA_PROPERTY_4 = "seed";
    @ComponentProperty(defaultValue="true",
                       description="control whether debugging information is output to the console",
                       name="verbose")
    final static String DATA_PROPERTY_5 = "verbose";

    private boolean verbose = true;

    //~ Static fields/initializers **********************************************

    /** constant for random sampling. */
    static public final int RANDOM = 0;

    /** constant for sequential sampling. */
    static public final int SEQUENTIAL = 1;

    //~ Instance fields *********************************************************

    /** true if in debug mode */
    private boolean debug = false;

    /** The type of sampling to use: random or sequential. */
    private int samplingMethod;

    /** the seed for the random number generator. */
    private int seed = 123;

    /** percent of dataset to use to test the model. */
    private int testPercent = 50;

    /** percent of dataset to use to train the model. */
    private int trainPercent = 50;

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
        trainPercent = Integer.valueOf(cc.getProperty(DATA_PROPERTY_1));
        if (trainPercent < 0 || trainPercent > 100)
           throw new ComponentExecutionException("Train percentage must be between 0 and 100.");

        testPercent = Integer.valueOf(cc.getProperty(DATA_PROPERTY_2));
        if (testPercent < 0 || testPercent > 100)
          throw new ComponentExecutionException( "Test percentage must be between 0 and 100.");

        samplingMethod = Integer.valueOf(cc.getProperty(DATA_PROPERTY_3));

        seed = Integer.valueOf(cc.getProperty(DATA_PROPERTY_4));
        if (seed < 0)
            throw new ComponentExecutionException(" Value must be >= 0. ");

        verbose = Boolean.valueOf(cc.getProperty(DATA_PROPERTY_5));

        Table orig = (Table)(cc.getDataComponentFromInput(DATA_INPUT));

        // This is the number that will be test
        int nr = orig.getNumRows();
        int numTest = (int) (((long) nr * (long)testPercent) / 100L);
        int numTrain = (int) (((long) nr * (long)trainPercent) / 100L);

        if (numTest < 1 || numTrain < 1) {
            throw new ComponentExecutionException(": The selected table was to small to be practical with the percentages specified.");
        }

        int[] test = new int[numTest];
        int[] train = new int[numTrain];

        // only keep the first N rows
        int[] random = new int[nr];

        for (int i = 0; i < nr; i++) {
            random[i] = i;
        }

        // If we are to select the examples for test and train at random,
        // we need to to shuffle the indices.
        if (samplingMethod == RANDOM) {

            // Shuffle the indices randomly.
            Random r = new Random(seed);

            for (int i = 0; i < nr; i++) {
                int which = (int) (r.nextDouble() * (double) nr);

                if (i != which) {
                    int s = random[which];
                    random[which] = random[i];
                    random[i] = s;
                }
            }
        }

        // do the train assignment, from the start of the array of indices.
        for (int i = 0; i < numTrain; i++) {
            train[i] = random[i];
        }

        // do the test assignment, from the end of the array of indices.
        for (int i = numTest - 1, j = nr - 1; i >= 0; i--, j--) {
            test[i] = random[j];
        }

        random = null;

        ExampleTable et = orig.toExampleTable();
        et.setTestingSet(test);
        et.setTrainingSet(train);

        cc.pushDataComponentToOutput(DATA_OUTPUT_1, et.getTrainTable());
        cc.pushDataComponentToOutput(DATA_OUTPUT_2, et.getTestTable());
    } // end method doit
} // end class SimpleTrainTest

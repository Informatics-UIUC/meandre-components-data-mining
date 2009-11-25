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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.transformations.BinTransform;
import org.seasr.datatypes.table.util.TableUtilities;
import org.seasr.meandre.support.components.transform.binning.BinDescriptor;
import org.seasr.meandre.support.components.transform.binning.BinTree;
import org.seasr.meandre.support.components.transform.binning.NumericBinDescriptor;
import org.seasr.meandre.support.components.transform.binning.TextualBinDescriptor;
/**
 * <p>Overview:
 * Creates an empty BinTree.
 * </p><p>Detailed Description:
 * Given a Binning Transformation containing the definition of the bins,
 * and an Example Table that has the input/ output attribute labels and types,
 * this module builds a Bin Tree that can be later used to classify data.
 * </p><p>A Bin Tree holds information about the number of examples that fall
 * into each bin for each class. The Bin Tree can use only one output feature
 * as a class. If more are selected in the Example Table, only the first one
 * will be used.
 * </p><p> Scalability: a large enough number of features will result in an
 * OutOfMemory error. Use feature selection to reduce the number of features.</p>
 */

@Component(creator = "Lily Dong",
           description = "Given a Binning Transformation containing the definition of the bins, " +
           "and an Example Table that has the input/ output attribute labels and types, " +
           "this module builds a Bin Tree that can be later used to classify data. " +
           "A Bin Tree holds information about the number of examples that fall into each bin " +
           "for each class. The Bin Tree can use only one output " +
           "feature as a class. If more are selected in the Example Table, only the first one will be used." +
           "Scalability: a large enough number of features will result " +
           "in an OutOfMemory error. Use feature selection to reduce the number of features.",
           name = "CreateBinTree",
           tags = "binning, bin tree",
           baseURL="meandre://seasr.org/components/data-mining/")


public class CreateBinTree implements ExecutableComponent {
    @ComponentInput(description = "Input binning transformation containing the bin definitions. " +
                    "It is of type ncsa.d2k.modules.core.datatype.table.transformations.BinTransform",
                    name = "binTransform")
    final static String DATA_INPUT_1 = "binTransform";
    @ComponentInput(description = "Input example table containing the names of the input/output features." +
                    "It is of ncsa.d2k.modules.core.datatype.table.ExampleTable.",
                    name = "exampleTable")
    final static String DATA_INPUT_2 = "exampleTable";

    @ComponentOutput(description = "Output bin tree structure holding counts. " +
                     "It is of type ncsa.d2k.modules.core.transform.binning.BinTree",
                name = "binTree")
    final static String DATA_OUTPUT = "binTree";

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
        BinTransform bt = (BinTransform) cc.getDataComponentFromInput(DATA_INPUT_1);
        ExampleTable et;
        try {
            et = (ExampleTable) cc.getDataComponentFromInput(DATA_INPUT_2);
        } catch (ClassCastException ce) {
            throw new ComponentExecutionException(
                    ": Select input/output features using ChooseAttributes before this module");
        }

        int[] ins = et.getInputFeatures();
        int[] out = et.getOutputFeatures();

        if ((ins == null) || (ins.length == 0))
            throw new ComponentExecutionException(
                ": Please select the input features, they are missing.");

        if (out == null || out.length == 0)
            throw new ComponentExecutionException(
                ": Please select an output feature, it is missing");

        if (bt == null)
            throw new ComponentExecutionException(
                ": Bins must be defined before creating a BinTree");

        // we only support one out variable..
        int classColumn = out[0];

        if (et.isColumnScalar(classColumn))
            throw new ComponentExecutionException(
                ": Output feature must be nominal. Please transform it.");

        BinDescriptor[] bins = bt.getBinDescriptors();

        if (bins.length == 0 || bins.length < ins.length)
            throw new ComponentExecutionException(
                ": Bins must be defined for each input before creating BinTree.");

        BinTree tree = createBinTree(bt, et);

        int numRows = et.getNumRows();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < ins.length; i++) {

            // numeric columns
            if (et.isColumnScalar(ins[i])) {
                for (int j = 0; j < numRows; j++) {
                    if (et.isValueMissing(j, ins[i]))
                        tree.classify(
                                et.getString(j, classColumn),
                                et.getColumnLabel(ins[i]),
                                et.getMissingString());
                    else
                        tree.classify(
                                et.getString(j, classColumn),
                                et.getColumnLabel(ins[i]),
                                et.getDouble(j, ins[i]));
                }
            }

            // everything else is treated as textual columns
            else {
                for (int j = 0; j < numRows; j++)
                    tree.classify(
                            et.getString(j, classColumn),
                            et.getColumnLabel(ins[i]),
                            et.getString(j, ins[i]));
            }
        }
        //	}

        long endTime = System.currentTimeMillis();
        //tree.printAll();
        cc.pushDataComponentToOutput(DATA_OUTPUT, tree);
    }

    /**
     * Create BinTree given bin descriptor, class names, and attribute names
     * @param bins bin descriptors
     * @param cn class names
     * @param an attribute names
     * @return BinTree
     */
    public static BinTree createBinTree(
            BinDescriptor[] bins,
            String[] cn,
            String[] an) {

        BinTree bt = new BinTree(cn, an);

        //System.out.println("bins.length " + bins.length);
        for (int i = 0; i < bins.length; i++) {
            BinDescriptor bd = bins[i];
            String attLabel = bd.label;
            //System.out.println("bin label " + attLabel + " " + bd.name);
            if (bd instanceof NumericBinDescriptor) {
                double max = ((NumericBinDescriptor) bd).max;
                double min = ((NumericBinDescriptor) bd).min;

                try {
                    bt.addNumericBin(attLabel, bd.name, min, false, max, true);
                    //System.out.println("bin min " + min + " max " + max);
                } catch (Exception e) {}

            } else {
                HashSet vals = ((TextualBinDescriptor) bd).vals;
                String[] values = new String[vals.size()];
                Iterator ii = vals.iterator();
                int idx = 0;
                while (ii.hasNext()) {
                    values[idx] = (String) ii.next();
                    //System.out.println(values[idx]);
                    idx++;
                }

                try {
                    bt.addStringBin(attLabel, bd.name, values);
                    //System.out.println("addStringBin in CreateBinTree called");
                } catch (Exception e) {}

            }

        }

        return bt;

    }

    /**
     * Create a BinTree given a BinTransform and ExmapleTable
     * @param bt bin transform
     * @param et example table
     * @return bin tree
     */
    public static BinTree createBinTree(BinTransform bt, ExampleTable et) {

        int[] outputs = et.getOutputFeatures();
        int[] inputs = et.getInputFeatures();

        HashMap used = new HashMap();

        String[] cn = TableUtilities.uniqueValues(et, outputs[0]);
        String[] an = new String[inputs.length];
        for (int i = 0; i < an.length; i++)
            an[i] = et.getColumnLabel(inputs[i]);

        BinDescriptor[] bd = bt.getBinDescriptors();

        return createBinTree(bd, cn, an);
    }
}

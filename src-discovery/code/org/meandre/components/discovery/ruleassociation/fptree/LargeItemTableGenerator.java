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

package org.meandre.components.discovery.ruleassociation.fptree;

import java.util.ArrayList;
import java.util.Collections;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.Sparse;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPPattern;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPProb;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPSparse;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
        description = "<p>Overview: " +
                      "This module transforms a <i>SparseExampleTable</i> containing term frequency values " +
                      "into a data structure, <i>FPProb</i>, that efficiently represents item " +
                      "occurrences within user supplied support constraints.</p>" +
                      "<p> NOTE: All non-zero values for term frequency are treated alike -- they " +
                      "are counted as a positive occurrence for that row.</p>" +
                      "<p>References: " +
                      "N/A." +
                      "</p>" +
                      "<p>Data Type Restrictions: " +
                      "The input table must be a <i>SparseExampleTable</i> containing term frequency information." +
                      "</p>" +
                      "<p>Data Handling: " +
                      "This module does not modify the input <i>SparseExampleTable</i>" +
                      "</p>" +
                      "<p>Scalability: " +
                      "This module makes a constant number of passes over the table data. " +
                      "Memory usage is proportional to the size of the input <i>SparseExampleTable</i>" +
                      "</p>",
        name = "Large Item Table Generator",
        tags = "sparse table, fpprob, transform",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/"
)
public class LargeItemTableGenerator extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        description = "The input data table for pattern mining.",
            name = "sparse_table"
	)
    protected static final String IN_TABLE = "sparse_table";

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        description = "An FPProb object representing.",
	        name = "fp_prob"
	)
    protected static final String OUT_FPPROB = "fp_prob";

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "Query the input table to make sure there are no missing values.",
	        defaultValue = "false",
	        name = "check_missing_values"
	)
    protected static final String PROP_CHECKMV = "check_missing_values";

	@ComponentProperty(
	        description = "The minimum support value for attributes in this data set.",
	        defaultValue = "1",
	        name = "support"
	)
    protected static final String PROP_SUPPORT = "support";

	@ComponentProperty(
	        description = "Remove any attributes that appear in all rows.",
	        defaultValue = "true",
	        name = "remove_saturated_features"
	)
    protected static final String PROP_REMOVE_SAT_FEATS = "remove_saturated_features";

    // Inherited PROP_IGNORE_ERRORS from AbstractExecutableComponent

    //--------------------------------------------------------------------------------------------


	private int _support;
	private boolean _checkMissingValues;
	private boolean _removeSatFeats;

	private int[] _ifeatures = null;


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    _support = Integer.parseInt(ccp.getProperty(PROP_SUPPORT));
        _checkMissingValues = Boolean.parseBoolean(ccp.getProperty(PROP_CHECKMV));
        _removeSatFeats = Boolean.parseBoolean(ccp.getProperty(PROP_REMOVE_SAT_FEATS));

        _ifeatures = null;
	}

    /*
     * In frequency include all occurrences of a term even if it only matches the POS tag criteria
     * for a subset of occurrences.
     */
    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_TABLE);
        if (!(input instanceof Sparse))
            throw new UnsupportedDataTypeException("Input can only be a SparseTable");

        Table in_table = (Table)input;
        if (_checkMissingValues)
            if (in_table.hasMissingValues())
                throw new ComponentExecutionException("Please replace or filter out missing values in your data.");

        if (in_table.getNumRows() < 1)
            throw new ComponentExecutionException("Input table has no rows.");

        if (in_table instanceof ExampleTable) {
            console.fine("Input is an example table.");
            _ifeatures = ((ExampleTable)in_table).getInputFeatures();
        }
        else {
            _ifeatures = new int[in_table.getNumColumns()];
            for (int i = 0, n = in_table.getNumColumns(); i < n; i++)
                _ifeatures[i] = i;
        }

        if (_removeSatFeats) {
            console.fine("Removing saturated features.");

            /**
             * Remove features that saturate the data set.
             */
            ArrayList<Integer> featuresToRemove = new ArrayList<Integer>();
            int rowcnt = in_table.getNumRows();

            for (int i = 0, n = _ifeatures.length; i < n; i++) {
                int col = _ifeatures[i];
                int cnt = ((Sparse)in_table).getColumnNumEntries(col);
                if (cnt == rowcnt) {
                    featuresToRemove.add(col);

                    String featureName = in_table.getColumnLabel(col);
                    console.fine(String.format("Removing feature '%s' (column %d)", featureName, col));
                }
            }

            Collections.sort(featuresToRemove);

            for (int i = 0, n = featuresToRemove.size(); i < n; i++)
                ((MutableTable)in_table).removeColumn(featuresToRemove.get(i) - i);

            if (featuresToRemove.size() > 0) {
                /**
                 * Re-select the input features since we removed columns.
                 */
                if (in_table instanceof ExampleTable)
                    _ifeatures = ((ExampleTable)in_table).getInputFeatures();
                else {
                    _ifeatures = new int[in_table.getNumColumns()];
                    for (int i = 0, n = in_table.getNumColumns(); i < n; i++)
                        _ifeatures[i] = i;
                }
            }
        }

        /**
         * Build new table with integer columns.
         */

        console.fine("Building new table (adding columns).");

        int rcnt = in_table.getNumRows();
        int ccnt = _ifeatures.length;

        FPSparse otab = new FPSparse(ccnt);
        FPPattern.clearElementMapping();

        for (int i = 0, n = ccnt; i < n; i++) {
            FPPattern.addElementMapping(i, in_table.getColumnLabel(_ifeatures[i]));
            otab.addColumn(i);
        }

        console.fine("Copying rows to new table.");

        for (int i = 0, n = rcnt; i < n; i++) {
            int[] rowind = ((Sparse)in_table).getRowIndices(i);
            for (int j = 0, m = rowind.length; j < m; j++) {
                if (!(rowind[j] >= otab.getNumColumns())) {
                    otab.setInt(1, i, rowind[j]);
                }
            }
        }

        /**
         * Gen. feats array. (Don't need this now)
         */
        int[] flist = new int[0];
        FPProb prob = new FPProb(otab, flist, this._support);

        cc.pushDataComponentToOutput(OUT_FPPROB, prob);
    }

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	    _ifeatures = null;
	}
}





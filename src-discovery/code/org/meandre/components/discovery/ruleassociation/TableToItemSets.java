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

package org.meandre.components.discovery.ruleassociation;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.datatypes.table.Table;
import org.seasr.meandre.support.components.discovery.ruleassociation.ItemSets;

/**
 *
 * @author Boris Capitanu
 *
 * BC: Imported from d2k (ncsa.d2k.modules.core.discovery.ruleassociation.TableToItemSets)
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>This module reads a Table and extracts from it items for use "+
        "in mining association rules with the Apriori algorithm. "+
        "</p><p>Detailed Description: "+
        "This module takes as input a Table or an Example Table, and extracts items "+
        "that are used by the Apriori rule association algorithm. "+
        "An item is an [attribute,value] pair that occurs in the input table. "+
        "The module uses information from the original table to determine which "+
        "attributes should be used to form items being considered as possible rule antecedents "+
        "and rule consequents. "+
        "A compact representation is created indicating which items are contained in "+
        "rows in the original table. "+
        "The items and other information used by the Apriori algorithm are written "+
        "to the <i>Item Sets</i> output port. "+

        "</p><p>" +
        "If a Table or an Example Table with no specified input or output attributes is loaded, "+
        "all attributes (columns) will be used to form items being considered as possible antecedents "+
        "and consequents for the association rules. " +
        "If an Example Table with only input attributes or only output attributes is loaded, " +
        "the chosen attributes will be used to form items considered as possible rule antecedents and "+
        "possible rule consequents. " +
        "If an Example Table with both input and output attributes is loaded, the inputs will be " +
        "used to form items considered as possible rule antecedents, "+
        "and the outputs used to form items considered as possible rule consequents. " +

        "</p><p> "+
        "The computational complexity of the Apriori algorithm depends on "+
        "the number of possible antecedents and consequents, so narrowing the search prior to this step is "+
        "highly recommended.   Use the module <i>Choose Attributes</i> to specify the subset of table "+
        "attributes that are of interest. "+
        "If the table has continuous attributes as possible rule antecedents or targets, "+
        "a <i>Binning</i> module should be used prior to this module to reduce the number of possible "+
        "values for those continuous attributes. "+

        "</p><p>" +
        "In a typical itinerary the <i>Item Sets</i> output port from this module is connected to "+
        "a <i>Generate Multiple Outputs</i> " +
        "module and then to an <i>Apriori</i> module which forms frequent itemsets based on "+
        "a minimum support value, and to a <i>Compute Confidence</i> module which forms "+
        "association rules that satisfy a minimum confidence value. "+

        "</p><p>Limitations: "+
        "The <i>Apriori</i> and <i>Compute Confidence</i> modules currently "+
        "build rules with a single item in the consequent.  "+

        "</p><p>Data Handling: " +
        "This module does not modify the input Table in any way. "+

        "</p><p>Scalability: " +
        "A representation of each row of the table is stored in memory. The representation is usually "+
        "smaller than the original data.    </p>",

        name = "Table To Item Sets",
        tags = "rule association, converter, itemsets, table, discovery",
        baseURL="meandre://seasr.org/components/")

public class TableToItemSets implements ExecutableComponent {

    @ComponentInput(description = "The table that items and sets will be extracted from", name = "table")
    final static String DATA_INPUT_TABLE = "table";

    @ComponentOutput(description = "The items of interest that were found in the table and " +
            "a representation of the items that occur together in the table", name = "item_sets")
    final static String DATA_OUTPUT_ITEM_SETS = "item_sets";

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties context) {

	}

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
	public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
	    ItemSets iss = new ItemSets((Table) context.getDataComponentFromInput(DATA_INPUT_TABLE));
	    context.pushDataComponentToOutput(DATA_OUTPUT_ITEM_SETS, iss);
	}

	/*
	 * (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties context) {

    }
}

//Start QA Comments
//2/28/03 Recv from Tom
//3/11/03 Ruth starts QA;
//- Renamed TableToItemSets instead of ConvertTableToItemSets (class)
//and Table To Sets (module name).   Updated documentation.
//3/18/03 Removed Target Attributes output port.  That information is now available
//in ItemSets and all modules that used Target Attributes already get ItemSets.
//3/20/03 Ready for Basic.
//End QA Comments

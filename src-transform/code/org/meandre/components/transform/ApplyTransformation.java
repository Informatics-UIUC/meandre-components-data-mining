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

package org.meandre.components.transform;

import java.util.logging.Logger;

import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Transformation;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

/**
 * <p>
 * Overview: This module applies a Transformation to a Table.
 * </p>
 * <P>
 * Detailed Description: This module applies a Transformation to a MutableTable
 * and outputs the transformed table as a MutableTable.
 * </p>
 * <P>
 * Data Handling: This modules modifies the input Table
 * </P>
 *
 * @author clutter (original)
 * @author Boris Capitanu
 *
 * BC: Imported from d2k (ncsa.d2k.modules.core.transform.ApplyTransformation)
 *
 */

@Component(creator = "Boris Capitanu", description = "<p>This module applies a Transformation to a Table. "
		+ "</p><p>Detailed Description: "
		+ "This module applies a Transformation to a MutableTable and outputs "
		+ "the transformed table as a MutableTable. "
		+ "</p><p>Data Handling: This module modifies the input Table</p>",
		name = "Apply Transformation", tags = "transform, mutabletable",
        baseURL="meandre://seasr.org/components/")

public class ApplyTransformation implements ExecutableComponent {

	@ComponentInput(description = "Transformation to apply to the input table", name = "transformation")
	final static String DATA_INPUT_TRANSFORMATION = "transformation";

	@ComponentInput(description = "The table to apply the transformation to", name = "table")
	final static String DATA_INPUT_TABLE = "table";

	@ComponentOutput(description = "The transformed input table", name = "table")
	final static String DATA_OUTPUT_TABLE = "table";

	private Logger _logger;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties context) {
		_logger = context.getLogger();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
		Transformation t = (Transformation) context.getDataComponentFromInput(DATA_INPUT_TRANSFORMATION);
		MutableTable mt = (MutableTable) context.getDataComponentFromInput(DATA_INPUT_TABLE);

		if (!t.transform(mt)) {
			_logger.severe("Transformation failed");
			throw new ComponentExecutionException("Transformation failed.");
		} else {
			mt.addTransformation(t);
		}

		context.pushDataComponentToOutput(DATA_OUTPUT_TABLE, mt);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties arg0) {
	}
} // end class ApplyTransformation

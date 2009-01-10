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

package org.meandre.components.io.table.sparse;

// ==============
// Java Imports
// ==============

import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

/**
 * TODO: Testing
 *
 * @author D. Searsmith (Conversion to SEASR 6/08)
 */
@Component(creator = "Duane Searsmith",
description = "<p>Overview: Outputs a <i>TableFactory</i> suitable "
		+ "for creating D2K Tables from the "
		+ "org.meandre.components.datatype.table.sparse package.</p>",
name = "Sparse Table Factory Injector", tags = "sparsetable, table, factory",
baseURL="meandre://seasr.org/components/")

public class SparseTableFactoryInjector implements ExecutableComponent {

	static final long serialVersionUID = 1L;

	private static Logger _logger = Logger
			.getLogger("SparseTableFactoryInjector");

	// IO

	@ComponentOutput(description = "Ouputs a new SparseTableFactory instance", name = "sparse_table_factory")
	public final static String DATA_OUTPUT_SPARSE_TABLE_FACTORY = "sparse_table_factory";

	// ================
	// Constructor(s)
	// ================
	public SparseTableFactoryInjector() {
	}

	// ================
	// Public Methods
	// ================

	public void initialize(ComponentContextProperties ccp) {
		_logger.fine("initialize() called");
	}

	public void dispose(ComponentContextProperties ccp) {
		_logger.fine("dispose() called");
	}

	public void execute(ComponentContext ctx)
			throws ComponentExecutionException, ComponentContextException {
		_logger.fine("execute() called");
		try {

		ctx.pushDataComponentToOutput(DATA_OUTPUT_SPARSE_TABLE_FACTORY,
		        new org.meandre.components.datatype.table.sparse.SparseTableFactory());


		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			System.out.println("ERROR: TextFileToDoc.execute()");
			throw new ComponentExecutionException(ex);
		}
	}

}

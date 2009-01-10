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

package org.meandre.components.io.table;

import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.components.datatype.table.basic.BasicTableFactory;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;


/**
 * Instantiates and pushes out a {@link
 * org.meandre.components.datatype.table.basic.BasicTableFactory}.
 *
 * @author  clutter
 * @author  gpape
 * @author  $Author: mcgrath $
 * @author D. Searsmith (Converted to SEASR)
 * @version $Revision: 1.9 $, $Date: 2007/05/18 21:25:08 $
 */
@Component(creator = "Duane Searsmith",
		description = "<p>Overview: Outputs a <i>TableFactory</i> suitable for " +
             "creating D2K Tables from the " +
             "ncsa.d2k.modules.core.datatype.table.basic package. This is " +
             "a standard Table suitable for most applications.</p>",
             name = "Basic Table Factory Injector", tags = "table factory",
             baseURL="meandre://seasr.org/components/")
             
public class BasicTableFactoryInjector implements ExecutableComponent {

	private static Logger _logger = Logger.getLogger("BasicTableFactoryInjector");

	@ComponentOutput(description = "Table factory instance.", name = "table_factory")
	public final static String DATA_OUTPUT_TABLE_FACTORY = "table_factory";

   //~ Constructors ************************************************************

   /**
    * Creates a new BasicTableFactoryInjector object.
    */
   public BasicTableFactoryInjector() { }

   //~ Methods *****************************************************************

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
			ctx.pushDataComponentToOutput(DATA_OUTPUT_TABLE_FACTORY, new BasicTableFactory());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ERROR: BasicTableFactoryInjector.execute(): " + ex.getMessage());
			throw new ComponentExecutionException(ex);
		}
	}

} // end class BasicTableFactoryInjector

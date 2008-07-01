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

package org.meandre.components.io.graph;

// ==============
// Java Imports
// ==============

import java.io.File;
import java.util.logging.Logger;

// ===============
// Other Imports
// ===============

import org.meandre.core.*;
import org.meandre.annotations.*;

import org.meandre.components.io.support.proxy.DataObjectProxy;
import org.meandre.components.io.graph.support.jung_piccolo.PGraphML;


/**
 * Reads a Jung Graph object from a GraphML file or URL.
 * <p>
 * <b>Note:</b> This module is the same as deprecated module
 * <i>XMLToGraphFromURL</i>, extended to access the data through
 * <i>DataObjectProxy</i>.
 * </p>
 * 
 * @author gpape
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:35 $
 */
@Component(creator = "Duane Searsmith", 
		
		description = "<p>Overview: This module creates a Jung Graph object from a "
			+ "GraphML file.  The input may be a local file or remote.</p>"
			+ "<p><b>Note:</b>  This module is the same as deprecated module "
			+ "<i>XMLToGraphFromURL</i>, extended to access the data through a"
			+ "<i>DataObjectProxy</i>.</p>"
			+ "<p>Acknowledgement: This module uses "
			+ "functionality from the JUNG project. See "
			+ "http://jung.sourceforge.net.</p>", 
		
		name = "XMLToGraphFromURL", tags = "io file transform jung xml graphml graph")
public class XMLToGraphFromURL implements ExecutableComponent {

	private static Logger _logger = Logger.getLogger("XMLToGraphFromURL");

	@ComponentInput(description = "File name.", name = "data_proxy")
	public final static String DATA_INPUT_DATA_PROXY = "data_proxy";

	@ComponentOutput(description = "Document object.", name = "jung_graph")
	public final static String DATA_OUTPUT_JUNG_GRAPH = "jung_graph";

	// ~ Methods
	// *****************************************************************

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
			DataObjectProxy dop = (DataObjectProxy) ctx
					.getDataComponentFromInput(DATA_INPUT_DATA_PROXY);
			File f = dop.readFile(null);
			ctx.pushDataComponentToOutput(DATA_OUTPUT_JUNG_GRAPH, PGraphML
					.load(f));
			dop.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			System.out.println("ERROR: XMLToGraphFromURL.execute()");
			throw new ComponentExecutionException(ex);
		}
	}

} // end class XMLToGraphFromURL

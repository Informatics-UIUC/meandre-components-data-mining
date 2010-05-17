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

package org.seasr.meandre.components.io.graph;

//==============
//Java Imports
//==============

import java.io.File;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.components.io.dataproxy.DataObjectProxy;

import prefuse.data.Graph;
import prefuse.data.io.GraphMLReader;

/**
 * Read a graph from a local or remote file using GraphML from prefuse.
 *
 * <p><b>Note:</b>  This module is the same as deprecated module
 * <i>GraphMLToPrefuse</i>, extended to access the data through
 * <i>DataObjectProxy</i>.</p>
 *
 *
 * @author  $Author: dfleming $
 * @author D. Searsmith (Conversion to SEASR 6/2008)
 * @version $Revision: 1.2 $, $Date: 2007/01/23 23:09:35 $
 * @version Revision 1.3, 6/2008
 */
@Component(creator = "Duane Searsmith",

		description = "<p>Overview: <br>"
	+ "Read a graph from a local or remote file using GraphML from prefuse. <br>"
	+ "<b>Note:</b>  This module is the same as deprecated module "
    + "<i>GraphMLToPrefuse</i>, extended to access the data through "
    + "<i>DataObjectProxy</i>.</p>",

	name = "GraphML To Prefuse From URL", tags = "transform, io, file, graphml, prefuse, graph",
    baseURL="meandre://seasr.org/components/data-mining/")

public class GraphMLToPrefuseFromURL implements ExecutableComponent {

	private static Logger _logger = Logger.getLogger("GraphMLToPrefuseFromURL");

	// IO

	@ComponentInput(description = "File name", name = "data_proxy")
	public final static String DATA_INPUT_DATA_PROXY = "data_proxy";

	@ComponentOutput(description = "Document object", name = "prefuse_graph")
	public final static String DATA_OUTPUT_PREFUSE_GRAPH = "prefuse_graph";

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
		      GraphMLReader reader = new GraphMLReader();
		      Graph graph = reader.readGraph(f);
		      dop.close();
			ctx.pushDataComponentToOutput(DATA_OUTPUT_PREFUSE_GRAPH, graph);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			System.out.println("ERROR: GraphMLToPrefuseFromURL.execute()");
			throw new ComponentExecutionException(ex);
		}
	}

}


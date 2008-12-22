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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

// ===============
// Other Imports
// ===============

import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.PredicateUtils;

import prefuse.data.Node;

import org.meandre.core.*;
import org.meandre.annotations.*;

/**
 * <p>
 * Overview: Converts a JUNG graph (directed or not) to a Prefuse graph.
 * </p>
 *
 * <p>
 * Acknowledgement: This module uses functionality from the JUNG project. See
 * http://jung.sourceforge.net.
 * </p>
 *
 * <p>
 * Acknowledgement: This module uses functionality from the Prefuse project. See
 * http://prefuse.org.
 * </p>
 *
 * @author $Author: clutter $
 * @version $Revision: 1.4 $, $Date: 2006/08/02 15:05:47 $
 */
@Component(creator = "Duane Searsmith",

		description = "<p>Overview: Converts a JUNG graph (directed or not) to a Prefuse graph.</p>"
		+ "<p>Acknowledgement: "
		+ "This module uses functionality from the JUNG project. See http://jung.sourceforge.net."
		+ "</p>"
		+ "<p>Acknowledgement: "
		+ "This module uses functionality from the Prefuse project. See http://prefuse.org."
		+ "</p>",

	name = "Jung To Prefuse", tags = "io, transform, file graph, prefuse, jung")
public class JungToPrefuse implements ExecutableComponent {

	// ==============
	// Data Members
	// ==============

	private static Logger _logger = Logger.getLogger("JungToPrefuse");

	// IO

	@ComponentInput(description = "A Jung graph object", name = "jung_graph")
	public final static String DATA_INPUT_JUNG_GRAPH = "jung_graph";

	@ComponentOutput(description = "A Prefuse graph object", name = "prefuse_graph")
	public final static String DATA_OUTPUT_PREFUSE_GRAPH = "prefuse_graph";

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

			edu.uci.ics.jung.graph.Graph jung_graph = (edu.uci.ics.jung.graph.Graph) ctx
					.getDataComponentFromInput(DATA_INPUT_JUNG_GRAPH);

			boolean directed = PredicateUtils.enforcesDirected(jung_graph);

			prefuse.data.Graph prefuse_graph = new prefuse.data.Graph(directed);

			Set jung_vertices = jung_graph.getVertices();
			HashMap jung_to_prefuse_vertex_map = new HashMap(jung_vertices
					.size());

			Iterator jung_vertex_iterator = jung_vertices.iterator();

			while (jung_vertex_iterator.hasNext()) {
				Vertex jung_vertex = (Vertex) jung_vertex_iterator.next();

				// create a new prefuse Node for this vertex
				Node prefuse_node = prefuse_graph.addNode();

				jung_to_prefuse_vertex_map.put(jung_vertex, prefuse_node);

				// now set the user data..
				Iterator user_data_iterator = jung_vertex
						.getUserDatumKeyIterator();

				while (user_data_iterator.hasNext()) {
					String key = (String) user_data_iterator.next();
					Object value = jung_vertex.getUserDatum(key);

					int index = prefuse_node.getColumnIndex(key);

					if (index == -1) {
						Class column_class = Object.class;

						if (value instanceof Integer) {
							column_class = int.class;
						} else if (value instanceof Double) {
							column_class = double.class;
						} else if (value instanceof Long) {
							column_class = long.class;
						} else if (value instanceof String) {
							column_class = String.class;
							// else
							// continue;
						}

						prefuse_node.getTable().addColumn(key, column_class);
						index = prefuse_node.getColumnIndex(key);
					}

					if (value instanceof Integer) {
						prefuse_node.setInt(key, ((Integer) value).intValue());
					} else if (value instanceof Double) {
						prefuse_node.setDouble(key, ((Double) value)
								.doubleValue());
					} else if (value instanceof Long) {
						prefuse_node.setLong(key, ((Long) value).longValue());
					} else if (value instanceof String) {
						prefuse_node.setString(key, (String) value);
					} else {
						prefuse_node.set(key, value);
					}
				} // user data
			} // jung vertex

			Iterator jung_edge_iterator = jung_graph.getEdges().iterator();

			while (jung_edge_iterator.hasNext()) {
				edu.uci.ics.jung.graph.Edge jung_edge = (edu.uci.ics.jung.graph.Edge) jung_edge_iterator
						.next();

				Vertex v1;
				Vertex v2;

				if (!directed) {
					Pair vertices = jung_edge.getEndpoints();
					v1 = (Vertex) vertices.getFirst();
					v2 = (Vertex) vertices.getSecond();
				} else {
					v1 = ((DirectedEdge) jung_edge).getSource();
					v2 = ((DirectedEdge) jung_edge).getDest();
				}

				Node n1 = (Node) jung_to_prefuse_vertex_map.get(v1);
				Node n2 = (Node) jung_to_prefuse_vertex_map.get(v2);

				prefuse.data.Edge prefuse_edge = prefuse_graph.addEdge(n1, n2);

				// now set the user data
				Iterator user_data_iterator = jung_edge
						.getUserDatumKeyIterator();

				while (user_data_iterator.hasNext()) {
					String key = (String) user_data_iterator.next();
					Object value = jung_edge.getUserDatum(key);

					int index = prefuse_edge.getColumnIndex(key);

					if (index == -1) {
						Class column_class = Object.class;

						if (value instanceof Integer) {
							column_class = int.class;
						} else if (value instanceof Double) {
							column_class = double.class;
						} else if (value instanceof Long) {
							column_class = long.class;
						} else if (value instanceof String) {
							column_class = String.class;
							// else
							// continue;
						}

						prefuse_edge.getTable().addColumn(key, column_class);
						index = prefuse_edge.getColumnIndex(key);
					}

					if (value instanceof Integer) {
						prefuse_edge.setInt(key, ((Integer) value).intValue());
					} else if (value instanceof Double) {
						prefuse_edge.setDouble(key, ((Double) value)
								.doubleValue());
					} else if (value instanceof Long) {
						prefuse_edge.setLong(key, ((Long) value).longValue());
					} else if (value instanceof String) {
						prefuse_edge.setString(key, (String) value);
					} else {
						prefuse_edge.set(key, value);
					}
				} // user data

			} // edge iterator

			ctx.pushDataComponentToOutput(DATA_OUTPUT_PREFUSE_GRAPH, prefuse_graph);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			System.out.println("ERROR: JungToPrefuse.doit()");
			throw new ComponentExecutionException(ex);
		}
	}

} // end class JungToPrefuse

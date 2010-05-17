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

package org.seasr.meandre.components.vis.dendrogram.gwt;

// ==============
// Java Imports
// ==============

import gnu.formj.html.A;
import gnu.formj.html.Div;
import gnu.formj.html.Label;
import gnu.formj.html.style.Style;
import gnu.formj.html.table.Table;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.util.Unzipper;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.datatypes.datamining.table.sparse.SparseTable;
import org.seasr.meandre.support.components.discovery.cluster.ClusterModel;
import org.seasr.meandre.support.components.discovery.cluster.TableCluster;

// import org.meandre.tools.components.*;
// import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

/**
 *
 * <p>
 * Title: Dendrogram Visualization
 * </p>
 *
 * <p>
 * Description: A dendrogram visualization of cluster models.
 * </p>
 *
 * <p>
 * Properties: <br>
 * The "use_local_host" means that you intend to run the server and browser
 * client on the same machine. This is set to false by default. If you do run
 * both on the same machine then it is important to set this property to true.
 * The reason is that when Java queries to OS for the local machines IP it often
 * gets internal LAN IP's that often are not reachable as url's in your browser,
 * and therefore an error will result. By choosing to set this property to true
 * you force the client app to use "127.0.0.1" for your local address.<br>
 * The sparse_detail_limit property defaults to 10. For sparse tables it is not
 * practical to display the entire table subset that make up a given cluster
 * because the table may have thousands of columns. This value tells the
 * application how many column values to display in descending order by support.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * <p>
 * Company: Automated Learning Group, NCSA
 * </p>
 *
 * @author D. Searsmith
 * @version 1.0
 */

@Component(creator = "Duane Searsmith",
        description = "<p>Dendrogram visualization of SEASR cluster models.</p>"
            + "<p>"
            + "Properties: <br>"
            + "The 'use_local_host' means that you intend to run the server and browser client "
            + "on the same machine.  This is set to false by default.  If you do run both on the "
            + "same machine then it is important to set this property to true.  The reason is that "
            + "when Java queries to OS for the local machines IP it often gets internal LAN IP's "
            + "that often are not reachable as url's in your browser, and therefore an error will result. "
            + "By choosing to set this property to true you force the client app to use '127.0.0.1' "
            + "for your local address.<br>"
            + "The sparse_detail_limit property defaults to 10.  For sparse tables it is not practical "
            + "to display the entire table subset that make up a given cluster because the table may "
            + "have thousands of columns.  This value tells the application how many column values to "
            + "display in descending order by support." + "</p>",

            name = "Dendrogram Vis",
            tags = "visualization, dendrogram, cluster",
            mode = Mode.webui,
            dependency = { "DendrogramViz_001.jar" },
            baseURL="meandre://seasr.org/components/data-mining/")

public class DendrogramViz implements ExecutableComponent,
		WebUIFragmentCallback {

	// ==============
	// Data Members
	// ==============

	// props

	// @ComponentProperty(defaultValue = "1714", description = "The core
	// repository port.", name = "core_port")
	// public final static String DATA_PROPERTY_CORE_PORT = "core_port";

	@ComponentProperty(defaultValue = "false", description = "Connect to local host.", name = "use_local_host")
	public final static String DATA_PROPERTY_USE_LOCAL_HOST = "use_local_host";

	@ComponentProperty(defaultValue = "10", description = "Detail limit on sparse table summaries.", name = "sparse_detail_limit")
	public final static String DATA_PROPERTY_SPARSE_DETAIL_LIMIT = "sparse_detail_limit";

	// io

	@ComponentInput(description = "D2K Cluster Model", name = "d2k_cluster_Model")
	public final static String DATA_INPUT_CLUSTER_MODEL = "d2k_cluster_Model";

	/** The collection for semaphore */
	private static Vector<Semaphore> sems = new Vector<Semaphore>();

	/** The blocking semaphore */
	private final Semaphore sem = new Semaphore(1, true);

	/** The instance ID */
	private String sInstanceID = null;

	/** The active cluster model. */
	private ClusterModel _cmodel = null;

	private ComponentContext _ctx = null;

	private final Map<Integer, DendrogramViz.ClusterNode> _clustMap = new HashMap<Integer, DendrogramViz.ClusterNode>();

	private final String _resName = "DendrogramViz_001";

	private Map<Integer, Object[]> _centroidMap = null;

	private static Logger _logger = Logger.getLogger("DendrogramViz");

	// ==============
	// Constructors
	// ==============

	public DendrogramViz() {
	}

	// ================
	// Public Methods
	// ================

	static public void main(String args[]) {
		// // get a flow builder instance
		// FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
		// // get a flow object
		// WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
		// // add a component
		// String pushString = wflow
		// .addComponent("org.seasr.meandre.components.io.PushString");
		// // set a component property
		// wflow.setComponentInstanceProp(pushString, "string",
		// "file:///c:/ALG_Projects/alg/wekadata/examples/iris2.arff");
		// // add another component
		// String loadInstances = wflow
		// .addComponent("org.seasr.meandre.components.weka.WekaLoadInstances");
		// // set a component property
		// wflow.setComponentInstanceProp(loadInstances, "printInstances", "Y");
		// // make a connection between two components
		// wflow.connectComponents(pushString, "output_string", loadInstances,
		// "inputURL");
		//
		// // add a third component
		// String instotab = wflow
		// .addComponent("org.seasr.meandre.components.io.d2k.InstancesToTable");
		// // make another connection between two components
		// wflow.connectComponents(loadInstances,
		// WekaLoadInstances.DATA_OUTPUT_INSTANCES, instotab,
		// InstancesToTable.DATA_INPUT);
		//
		// // add a 4th component
		// String clusterer = wflow
		// .addComponent("org.seasr.meandre.components.mining.d2k.unsupervised.cluster.hac.HACModelBuilder");
		// // make another connection between two components
		// wflow.connectComponents(instotab, InstancesToTable.DATA_OUTPUT,
		// clusterer, HACModelBuilder.DATA_INPUT_D2K_TABLE);
		//
		// wflow.setComponentInstanceProp(clusterer,
		// HACModelBuilder.DATA_PROPERTY_VERBOSE, "Y");
		// wflow.setComponentInstanceProp(clusterer,
		// HACModelBuilder.DATA_PROPERTY_NUM_CLUSTERS, "10");
		// wflow.setComponentInstanceProp(clusterer,
		// HACModelBuilder.DATA_PROPERTY_THRESHOLD, ".25");
		// wflow.setComponentInstanceProp(clusterer,
		// HACModelBuilder.DATA_PROPERTY_DISTANCE_METRIC, ""
		// + HACWork.s_Euclidean_DISTANCE);
		// wflow.setComponentInstanceProp(clusterer,
		// HACModelBuilder.DATA_PROPERTY_CLUSTER_METHOD, ""
		// + HACWork.s_UPGMA_CLUSTER);
		//
		// // add a 5th component
		// String dendro = wflow
		// .addComponent("org.seasr.meandre.components.viz.d2k.D2K_DendrogramViz");
		// // make another connection between two components
		// wflow.connectComponents(clusterer,
		// HACModelBuilder.DATA_OUTPUT_CLUSTER_MODEL, dendro,
		// DendrogramViz.DATA_INPUT_CLUSTER_MODEL);
		//
		// wflow.setComponentInstanceProp(dendro,
		// DendrogramViz.DATA_PROPERTY_CORE_PORT, "1715");
		//
		// // execute the flow specifying that we want a web UI displayed
		// flowBuilder.execute(wflow, true);
		//
		// // For some reason the process does not end without a forced exit.
		// System.exit(0);

	}

	// ================
	// Public Methods
	// ================

	public boolean getUseLocalhost(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_USE_LOCAL_HOST);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public int getSparseDetailLimit(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_SPARSE_DETAIL_LIMIT);
		return Integer.parseInt(s);
	}

	// ===========================
	// Interface Implementation: ExecutableComponent
	// ===========================

	/**
	 * Called when a flow is started.
	 */
	public void initialize(ComponentContextProperties ccp) {

		_logger.fine("initialize() called");

		String fname = ((ComponentContext) ccp).getPublicResourcesDirectory();
		if ((!(fname.endsWith("/"))) && (!(fname.endsWith("\\")))) {
			fname += "/";
		}

		try {
			Unzipper.CheckIfZipFileExistsIfNotInstallFromJarThenUnzipIt(fname,
					_resName, fname + _resName, (ComponentContext) ccp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * When ready for execution.
	 *
	 * @param ctx
	 *            The component context.
	 * @throws ComponentExecutionException
	 *             An exeception occurred during execution
	 * @throws ComponentContextException
	 *             Illigal access to context
	 */
	public void execute(ComponentContext ctx)
			throws ComponentExecutionException, ComponentContextException {

		_logger.fine("execute() called");

		_ctx = ctx;

		sems.add(sem);

		try {
			_logger.fine("Firing the web ui component");
			_cmodel = (ClusterModel) ctx
					.getDataComponentFromInput(DendrogramViz.DATA_INPUT_CLUSTER_MODEL);

			if (_cmodel.getRoot().getTable() instanceof SparseTable) {
				int[] centroidInds = _cmodel.getRoot().getSparseCentroidInd();
				double[] centroidVals = _cmodel.getRoot()
						.getSparseCentroidValues();
				SparseTable tab = (SparseTable) _cmodel.getRoot().getTable();
				_centroidMap = new HashMap<Integer, Object[]>();
				for (int i = 0, n = centroidInds.length; i < n; i++) {
					Object[] objarr = new Object[3];
					Integer key = new Integer(centroidInds[i]);
					objarr[0] = key;
					objarr[1] = new Double(centroidVals[i]);
					objarr[2] = tab.getColumnLabel(centroidInds[i]);
					_centroidMap.put(key, objarr);
				}
			}

			sInstanceID = ctx.getExecutionInstanceID();
			sem.acquire();
			ctx.startWebUIFragment(this);
			_logger.fine(">>>STARTED");
			sem.acquire();
			_logger.fine(">>>Done");
			ctx.stopWebUIFragment(this);
		} catch (Exception e) {
			throw new ComponentExecutionException(e);
		}
	}

	/**
	 * Call at the end of an execution flow.
	 */
	public void dispose(ComponentContextProperties ccp) {
		_logger.fine("dispose() called");
		if (_centroidMap != null) {
			_centroidMap.clear();
		}
		_centroidMap = null;
	}

	// ===========================
	// Interface Implementation: WebUIFragmentCallback
	// ===========================

	/**
	 * This method gets call when a request with no parameters is made to a
	 * component webui fragment.
	 *
	 * @param response
	 *            The response object
	 * @throws WebUIException
	 *             Some problem arised during execution and something went wrong
	 */
	public void emptyRequest(HttpServletResponse response)
			throws WebUIException {
		try {
			response.getWriter().println(getViz());
		} catch (IOException e) {
			throw new WebUIException(e);
		}
	}

	/**
	 * This method gets called when a call with parameters is made to a given
	 * component webUI fragment
	 *
	 * @param target
	 *            The target path
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @throws WebUIException
	 *             A problem arised during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws WebUIException {
		String sDone = request.getParameter("done");
		String getmod = request.getParameter("getmod");
		String getrows = request.getParameter("getrows");
		if (sDone != null) {
			for (int i = 0; i < sems.size(); i++) {
				(sems.elementAt(i)).release();
			}
			sems.clear();
		} else if (getmod != null) {
			getModelData(response);
		} else if (getrows != null) {
			getRowsForCluster(response, getrows);
		} else {
			emptyRequest(response);
		}
	}

	// =================
	// Private Methods
	// =================

	private void getRowsForCluster(HttpServletResponse response, String cid)
			throws WebUIException {
		try {
			ClusterNode cn = this._clustMap.get(Integer
					.valueOf(Integer.parseInt(cid)));
			if (cn == null) {
				response.getWriter().println(
						"<div>NO CONTENT FOUND FOR ID:&nbsp;" + cid + "</div>");
			} else {
				if (cn.getRoot().getTable() instanceof SparseTable) {
					response.getWriter().println(getSparseTabViz(cn));
				} else {
					response.getWriter().println(
							getTabViz(cn.getRoot().getTable(), cn.getRoot()
									.getMemberIndices()));
				}
			}

		} catch (IOException e) {
			throw new WebUIException(e);
		}

	}

	private void createClusteringData(HttpServletResponse response,
			ClusterModel cm) throws IOException {
		TableCluster root = cm.getRoot();
		TreeSet<DendrogramViz.ClusterNode> ranked = new TreeSet<DendrogramViz.ClusterNode>(
				new cRank_Comparator());
		clusterWalk(root, ranked);

		Iterator<DendrogramViz.ClusterNode> itty = ranked.iterator();
		while (itty.hasNext()) {
			DendrogramViz.ClusterNode cn = itty.next();
			JSONObject obj = new JSONObject(cn.getMap());
			response.getWriter().print(obj.toString() + "|");
			try {
				_clustMap.put(Integer.valueOf(obj.getInt("rid")), cn);
			} catch (Exception e) {
				_logger.severe("ERROR: " + e);
				throw new RuntimeException(e);
			}
		}
		response.getWriter().println();
	}

	private DendrogramViz.ClusterNode clusterWalk(TableCluster root,
			Set<DendrogramViz.ClusterNode> hold) {
		TableCluster lc = root.getLC();
		TableCluster rc = root.getRC();
		DendrogramViz.ClusterNode lcNode = null;
		DendrogramViz.ClusterNode rcNode = null;
		if (lc.isLeaf()) {
			lcNode = new DendrogramViz.ClusterNode(lc, null, null, lc
					.getChildDistance());
			hold.add(lcNode);
		} else {
			lcNode = clusterWalk(lc, hold);
		}
		if (rc.isLeaf()) {
			rcNode = new DendrogramViz.ClusterNode(rc, null, null, rc
					.getChildDistance());
			hold.add(rcNode);
		} else {
			rcNode = clusterWalk(rc, hold);
		}
		ClusterNode ret = new ClusterNode(root, lcNode, rcNode, root
				.getChildDistance());
		hold.add(ret);
		return ret;
	}

	private void getModelData(HttpServletResponse response)
			throws WebUIException {
		try {
			createClusteringData(response, _cmodel);
		} catch (IOException e) {
			throw new WebUIException(e);
		}
	}

	/**
	 * A simple message.
	 *
	 * @return The html containing the page
	 */
	private String getViz() {
		// Done link

		String s1 = null;
		String port = null;
		int fragPort = -1;
		try {
			if (getUseLocalhost(_ctx)) {
				s1 = "127.0.0.1";
			} else {
				s1 = _ctx.getWebUIUrl(true).getHost();
			}
			// port = _ctx.getProperty(DendrogramViz.DATA_PROPERTY_CORE_PORT);
			fragPort = _ctx.getWebUIUrl(true).getPort();
			port = "" + fragPort;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Div h1 = new Div();
		h1.setValue(sInstanceID);
		h1.setId("fragid");
		Div h2 = new Div();
		h2.setValue(s1);
		h2.setId("fraghost");
		Div h3 = new Div();
		h3.setValue("" + fragPort);
		h3.setId("fragport");

		_logger.info("Port: " + fragPort);
		_logger.info("Core Port: " + port);
		_logger.info("Host: " + s1);
		_logger.info("Instance ID: " + sInstanceID);
		_logger.info("Use local host: " + getUseLocalhost(_ctx));

		A linkDone = new A();
		linkDone.setHref("/" + sInstanceID + "?done=true");
		linkDone
				.setContent(new Label("Done with the dendrogram visualization"));
		linkDone.setTitle("Done with the dendrogram visualization");
		linkDone.setToolTip("Done with the dendrogram visualization");
		Div div = new Div();
		div.add(linkDone);
		div.add(h1);
		div.add(h2);
		div.add(h3);

		String s2 = "<div>" /* + "<title>Dendrogram Visualization</title>" */
				+ "<link rel='stylesheet' href='http://"
				+ s1
				+ ":"
				+ port
				+ "/public/resources/"
				+ _resName
				+ "/Main.css'>"
				+ "<script src='http://"
				+ s1
				+ ":"
				+ port
				+ "/public/resources/"
				+ _resName
				+ "/script/wz_jsgraphics.js' type='text/javascript'></script> "
				+ "<script language='javascript' src='http://"
				+ s1
				+ ":"
				+ port
				+ "/public/resources/"
				+ _resName
				+ "/org.seasr.meandre.components.dendrogram.Main-xs.nocache.js'></script> "
				+ "<script src='http://" + s1 + ":" + port
				+ "/public/resources/" + _resName
				+ "/script/prototype.js' type='text/javascript'></script> "
				+ "<script src='http://" + s1 + ":" + port
				+ "/public/resources/" + _resName
				+ "/script/scriptaculous.js' type='text/javascript'></script>"/* </head><body> */
				+ "<div style='position:relative;' id='g'></div> "
				+ div.toString() + "</div>";
		return s2;
	}

	private String getTabViz(org.seasr.datatypes.datamining.table.Table tab,
			int[] mems) {
		int nr_instances = tab.getNumRows();
		_logger.fine("\nthe number of rows  = " + nr_instances);

		int nr_attributes = tab.getNumColumns();
		_logger.fine("\nthe number of attributes = " + nr_attributes);

		Style s = new Style();
		s.getBox().setBorderStyle("solid");
		s.getBox().setBorder("1px");
		s.getColor().setBackground("#DDDDDD");
		s.getBox().setPadding("1px");
		s.getFont().setFontFamily("Verdana");
		s.getFont().setFontSize("12px");

		// Table table = new Table(1 + nr_instances, nr_attributes);
		Table table = new Table(1 + mems.length, nr_attributes);
		table.setStyle(s);
		table.box.setBorderStyle("solid");
		table.box.setBorder("1px");

		int nr = 0;
		while (nr < nr_attributes) {
			String colname = tab.getColumnLabel(nr);
			table.addNext(colname);
			System.out.print(colname + "\t");
			nr++;
		}
		System.out.println();
		nr = 0;
		while (nr < mems.length) {
			int nr2 = 0;
			while (nr2 < nr_attributes) {
				String token = tab.getString(mems[nr], nr2);
				System.out.print(token + "\t");
				if (token.compareTo("?") != 0) { // ? means empty
					table.addNext(token);
				} else {
					table.addNext("");
				}
				nr2++;
			}
			nr++;
			System.out.println();
		}

		Div div = new Div();
		div.add(table);

		String s2 = div.toString();

		return s2;
	}

	private TreeSet<Object[]> compToCentroid(int[] inds, double[] vals,
			SparseTable spTab) {
		TreeSet<Object[]> cts = new TreeSet<Object[]>(new Centroid_Comparator());

		for (int i = 0, n = inds.length; i < n; i++) {
			Integer key = new Integer(inds[i]);
			Object[] cent = _centroidMap.get(key);
			Object[] objarr = new Object[3];
			objarr[0] = key;
			objarr[1] = new Double(vals[i] - ((Double) cent[1]).doubleValue());
			objarr[2] = cent[2];
			cts.add(objarr);
		}
		return cts;
	}

	private String getSparseTabViz(ClusterNode cn) {

		int[] inds = cn.getRoot().getSparseCentroidInd();
		double[] vals = cn.getRoot().getSparseCentroidValues();

		TreeSet<Object[]> ts = new TreeSet<Object[]>(new Centroid_Comparator());

		for (int i = 0, n = inds.length; i < n; i++) {
			Object[] objarr = new Object[2];
			objarr[0] = new Integer(inds[i]);
			objarr[1] = new Double(vals[i]);
			ts.add(objarr);
		}

		SparseTable spTab = (SparseTable) cn.getRoot().getTable();

		TreeSet<Object[]> cts = compToCentroid(inds, vals, spTab);

		int nr_instances = Integer.parseInt(_ctx
				.getProperty(DATA_PROPERTY_SPARSE_DETAIL_LIMIT));

		// if number of indices is smaller than limit use it as limit.
		nr_instances = (nr_instances > inds.length) ? inds.length
				: nr_instances;

		_logger.fine("\nthe number of rows  = " + nr_instances);

		int nr_attributes = 4;
		_logger.fine("\nthe number of attributes = " + nr_attributes);

		Style s = new Style();
		s.getBox().setBorderStyle("solid");
		s.getBox().setBorder("1px");
		s.getColor().setBackground("#DDDDDD");
		s.getBox().setPadding("1px");
		s.getFont().setFontFamily("Verdana");
		s.getFont().setFontSize("12px");

		Table table = new Table(nr_instances + 1, nr_attributes);
		table.setStyle(s);
		table.box.setBorderStyle("solid");
		table.box.setBorder("1px");

		int nr = 0;
		table.addNext("Attribute");
		System.out.print("Attribute" + "\t");
		table.addNext("Avg. Frequency");
		System.out.print("Avg. Frequency" + "\t");
		table.addNext("Attribute");
		System.out.print("Attribute" + "\t");
		table.addNext("Frequency/Norm");
		System.out.print("Frequency/Norm" + "\t");
		System.out.println();

		nr = 0;
		Iterator<Object[]> itty = ts.iterator();
		Iterator<Object[]> cent = cts.iterator();
		while (nr < nr_instances) {
			Object[] objarr = itty.next();
			Object[] centarr = cent.next();
			String att = spTab.getColumnLabel(((Integer) objarr[0]).intValue());
			System.out.print(att + "\t");
			String freq = String.format("%10.2f", ((Double) objarr[1])
					.doubleValue());
			System.out.print(freq + "\t");
			table.addNext(att);
			table.addNext(freq);
			String catt = (String) centarr[2];
			System.out.print(att + "\t");
			String cfreq = String.format("%10.2f", ((Double) centarr[1])
					.doubleValue());
			System.out.print(freq + "\t");
			table.addNext(catt);
			table.addNext(cfreq);
			nr++;
			System.out.println();
		}

		Div div = new Div();
		div.add(table);

		String s2 = div.toString();

		return s2;
	}

	// ===============
	// Inner Classes
	// ===============

	private class ClusterNode {
		private TableCluster _root = null;

		private ClusterNode _lc = null;

		private ClusterNode _rc = null;

		private double _cdist = 0;

		private Map<String, Object> _map = null;

		public ClusterNode(TableCluster root, ClusterNode lc, ClusterNode rc,
				double cdist) {
			super();
			_map = new HashMap<String, Object>();
			_root = root;
			_map.put("rid", Integer.valueOf(_root.hashCode()));
			_lc = lc;
			if (lc != null) {
				_map.put("lc", Double.valueOf(lc.getRoot().hashCode()));
			}
			_rc = rc;
			if (rc != null) {
				_map.put("rc", Double.valueOf(rc.getRoot().hashCode()));
			}
			_cdist = cdist;
			_map.put("cd", Double.valueOf(_cdist));
		}

		public Map<String, Object> getMap() {
			return _map;
		}

		public boolean isLeaf() {
			return _root.isLeaf();
		}

		public TableCluster getRoot() {
			return _root;
		}

		public double getChildDistance() {
			return _cdist;
		}

		public ClusterNode getLC() {
			return _lc;
		}

		public ClusterNode getRC() {
			return _rc;
		}
	}

	private class Centroid_Comparator implements Comparator<Object[]> {

		/**
		 * The small deviation allowed in double comparisons.
		 */
		public Centroid_Comparator() {
		}

		public int compare(Object[] objarr1, Object[] objarr2) {
			double d1 = ((Double) objarr1[1]).doubleValue();
			double d2 = ((Double) objarr2[1]).doubleValue();

			if (d1 < d2) {
				return 1;
			} else {
				return -1;
			}
		} // end method compare

		@Override
        public boolean equals(Object o) {
			return false;
		}
	} // end class cRank_Comparator

	private class cRank_Comparator implements
			Comparator<DendrogramViz.ClusterNode> {

		/**
		 * The small deviation allowed in double comparisons.
		 */
		public cRank_Comparator() {
		}

		public int compare(DendrogramViz.ClusterNode objarr1,
				DendrogramViz.ClusterNode objarr2) {

			if (objarr1.getChildDistance() > objarr2.getChildDistance()) {
				return 1;
			} else {
				return -1;
			}
		} // end method compare

		@Override
        public boolean equals(Object o) {
			return false;
		}
	} // end class cRank_Comparator

}

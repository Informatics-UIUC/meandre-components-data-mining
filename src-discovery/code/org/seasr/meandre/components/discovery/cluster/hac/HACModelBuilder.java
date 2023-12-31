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

package org.seasr.meandre.components.discovery.cluster.hac;

//==============
// Java Imports
//==============

//===============
// Other Imports
//===============

//import org.meandre.tools.components.*;
//import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.discovery.cluster.hac.HACWork;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@Component(
        creator = "Duane Searsmith",
        description ="Takes a d2k table object and builds a full bottom up cluster tree.",
        name = "HAC Model Builder",
        tags = "cluster, unsupervised, model builder",
        baseURL="meandre://seasr.org/components/data-mining/")

public class HACModelBuilder extends AbstractExecutableComponent {

    //==============
    // Data Members
    //==============

    @ComponentInput(description = "Table", name = "table")
            public final static String IN_D2K_TABLE = "table";

    @ComponentOutput(description = "Cluster Model", name = "cluster_model")
            public final static String OUT_CLUSTER_MODEL = "cluster_model";


    @ComponentProperty(defaultValue="" + HACWork.s_WardsMethod_CLUSTER,
                       description="The clustering method to be used",
                       name="cluster_method")
    public final static String PROP_CLUSTER_METHOD = "cluster_method";
    /** The clustering method to be used. */
    protected int _clusterMethod = HACWork.s_WardsMethod_CLUSTER;

    @ComponentProperty(defaultValue="" + HACWork.s_Euclidean_DISTANCE,
                       description="The distance metric to be used",
                       name="distance_metric")
    public final static String PROP_DISTANCE_METRIC = "distance_metric";
    /** The distance metric to be used. */
    protected int _distanceMetric = HACWork.s_Euclidean_DISTANCE;

    @ComponentProperty(defaultValue="5",
                       description="The number of clusters to create.",
                       name="num_clusters")
    public final static String PROP_NUM_CLUSTERS = "num_clusters";
    /** The number of clusters to create. */
    protected int _numberOfClusters = 5;

    @ComponentProperty(defaultValue="10",
                       description="The percentage of the maximum distance " +
                                   "to use as a cutoff value to halt cluster " +
                                   "agglomeration (0..100)",
                       name="threshold")
    public final static String PROP_THRESHOLD = "threshold";
    /**
     * The percentage of the <i>maximum distance</i> to use as a cutoff value to
     * halt cluster agglomeration.
     */
    protected int _thresh = 0;

    @ComponentProperty(defaultValue="Y",
                       description="Check for missing table values?",
                       name="missing_values")
    public final static String PROP_MISSING_VALUES = "missing_values";
    /**
     * Check missing values flag. If set to true, this component verifies prior
     * to computation, that there are no missing values in the input table.
     * (In the presence of missing values the component throws an Exception.)
     */
    protected boolean _mvCheck = true;

    @ComponentProperty(defaultValue="false",
                       description="Verbose output?",
                       name="verbose")
    public final static String PROP_VERBOSE = "verbose";
    /**
     * Flag for verbose mode - if true then this module outputs verbose info to
     * stdout.
     */
    protected boolean _verbose = false;

    //==============
    // Constructors
    //==============

    public HACModelBuilder() {
    }

    //================
    // Public Methods
    //================


    static public void main(String[] args) {

//            // get a flow builder instance
//            FlowBuilderAPI flowBuilder = new FlowBuilderAPI();
//            // get a flow object
//            WorkingFlow wflow = flowBuilder.newWorkingFlow("test");
//            // add a component
//            String pushString = wflow.addComponent(
//                    "org.seasr.meandre.components.io.PushString");
//            // set a component property
//            wflow.setComponentInstanceProp(pushString,
//                                           "string",
//                                           "file:///c:/ALG_Projects/alg/wekadata/examples/iris.arff");
//            // add another component
//            String loadInstances = wflow.addComponent(
//                    "org.seasr.meandre.components.weka.WekaLoadInstances");
//            // set a component property
//            wflow.setComponentInstanceProp(loadInstances,
//                                           "printInstances",
//                                           "Y");
//            // make a connection between two components
//            wflow.connectComponents(pushString,
//                                    "output_string",
//                                    loadInstances,
//                                    "inputURL");
//
//            // add a third component
//            String instotab = wflow.addComponent("org.seasr.meandre.components.io.d2k.WEKA_InstancesToExampleTable");
//            // make another connection between two components
//            wflow.connectComponents(loadInstances,
//                                    WekaLoadInstances.DATA_OUTPUT_INSTANCES,
//                                    instotab,
//                                    InstancesToTable.DATA_INPUT);
//
//            // add a 4th component
//            String clusterer = wflow.addComponent("org.seasr.meandre.components.mining.d2k.unsupervised.cluster.hac.HACModelBuilder");
//            // make another connection between two components
//            wflow.connectComponents(instotab,
//                                    InstancesToTable.DATA_OUTPUT,
//                                    clusterer,
//                                    DATA_INPUT_D2K_TABLE);
//
//            wflow.setComponentInstanceProp(clusterer,
//                                           DATA_PROPERTY_VERBOSE,
//                                           "Y");
//            wflow.setComponentInstanceProp(clusterer,
//                                           DATA_PROPERTY_NUM_CLUSTERS,
//                                           "10");
//            wflow.setComponentInstanceProp(clusterer,
//                                           DATA_PROPERTY_THRESHOLD,
//                                           ".15");
//
//            // execute the flow specifying that we want a web UI displayed
//            flowBuilder.execute(wflow, true);
//
//            // For some reason the process does not end without a forced exit.
//            System.exit(0);

    }

    //=====================
    // Property Accessors
    //=====================

    /**
     * Return the value if the check missing values flag.
     *
     * @return true if this module was set to handle missing value in a different
     *         way than regular ones
     */
    public boolean getCheckMissingValues() { return _mvCheck; }

    /**
     * Return the value of the verbose flag.
     *
     * @return true if set to work in verbose mode.
     */
    public boolean getVerbose() { return _verbose; }

    /**
     * Sets the check missing values flag.
     *
     * @param b If true then this module treats the missing values in the input
     *          table in a special way. Otherwise treats missing values as if
     *          they were regular ones.
     */
    public void setCheckMissingValues(boolean b) { _mvCheck = b; }

    /**
     * Sets the verbose mode flag.
     *
     * @param b If true then this module outputs verbose info to stdout.
     */
    public void setVerbose(boolean b) { _verbose = b; }

    /**
     * Returns the integer ID of the clustering method of this module. Clustering
     * method IDs are defined in <code>
     * ncsa.d2k.modules.core.discovery.cluster.hac.HAC</code>
     *
     * @return The integer ID of the clustering method of this module.
     */
    public int getClusterMethod() { return _clusterMethod; }

    /**
     * Returns the integer ID of the distance metric of this module. Distance
     * metric IDs are defined in <code>
     * ncsa.d2k.modules.core.discovery.cluster.hac.HAC</code>
     *
     * @return The integer ID of the distance metric.
     */
    public int getDistanceMetric() { return _distanceMetric; }

    /**
     * Returns the threshold value used by this module.
     *
     * @return The value of the threshold used by this module.
     */
    public int getDistanceThreshold() { return _thresh; }

    /**
     * Returns the number of clusters to be formed by this module.
     *
     * @return The number of cluster property's value.
     */
    public int getNumberOfClusters() { return _numberOfClusters; }

    /**
     * Sets the cluster method property.
     *
     * @param noc A cluster method ID. Must be a value in the boundaries of
     *            <code>
     *            ncsa.d2k.modules.core.discovery.cluster.hac.Hac.s_ClusterMethodLabels</code>
     *            array.
     *
     * @see   <code>ncsa.d2k.modules.core.discovery.cluster.hac.Hac</code>
     */
    public void setClusterMethod(int noc) { _clusterMethod = noc; }

    /**
     * Sets the distanc metric property.
     *
     * @param dm A distanc metric ID. Must be a value in the boundaries of <code>
     *           ncsa.d2k.modules.core.discovery.cluster.hac.Hac.s_DistanceMetricLabels</code>
     *           array.
     *
     * @see   <code>ncsa.d2k.modules.core.discovery.cluster.hac.Hac</code>
     */
    public void setDistanceMetric(int dm) { _distanceMetric = dm; }

    /**
     * Sets the distance threshold property.
     *
     * @param noc The new value for the distance threshold property. Must be an
     *            integer between 1 and 100 (represent a percentage).
     */
    public void setDistanceThreshold(int treshold) { _thresh = treshold; }

    /**
     * Sets the number of clusters property.
     *
     * @param noc Number of clusters to be formed by this module.
     */
    public void setNumberOfClusters(int noc) { _numberOfClusters = noc; }

    //===========================
    // Interface Implementation: ExecutableComponent
    //===========================


    @Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	setCheckMissingValues(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_MISSING_VALUES, ccp)));
    	setVerbose(Boolean.parseBoolean(getPropertyOrDieTrying(PROP_VERBOSE, ccp)));
    	setClusterMethod(Integer.parseInt(getPropertyOrDieTrying(PROP_CLUSTER_METHOD, ccp)));
    	setDistanceMetric(Integer.parseInt(getPropertyOrDieTrying(PROP_DISTANCE_METRIC, ccp)));
    	setNumberOfClusters(Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_CLUSTERS, ccp)));
    	setDistanceThreshold(Integer.parseInt(getPropertyOrDieTrying(PROP_THRESHOLD, ccp)));
    }

    @Override
	public void disposeCallBack(ComponentContextProperties context) throws Exception {
    }

    @Override
	public void executeCallBack(ComponentContext context) throws Exception {
    	Table tab = (Table) context.getDataComponentFromInput(IN_D2K_TABLE);

    	HACWork hac =
    		new HACWork(this.getClusterMethod(), this.getDistanceMetric(),
    				this.getNumberOfClusters(),
    				this.getDistanceThreshold(),
    				getVerbose(), this.getCheckMissingValues(),
    				"HACModelBuilder");

    	//            // Push Output
    	context.pushDataComponentToOutput(OUT_CLUSTER_MODEL, hac.buildModel(tab));
    }
}

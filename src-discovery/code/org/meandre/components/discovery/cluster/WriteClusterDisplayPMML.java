package org.meandre.components.discovery.cluster;

/* University of Illinois/NCSA
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

//import org.meandre.components.discovery.ruleassociation.support.FreqItemSet;
import org.meandre.components.discovery.cluster.ClusterPMMLTags;
import org.meandre.components.discovery.cluster.support.ClusterModel;
import org.meandre.components.discovery.cluster.support.TableCluster;

import org.dom4j.*;
import org.dom4j.io.*;

//import org.meandre.tools.components.*;
//import org.meandre.tools.components.FlowBuilderAPI.WorkingFlow;

import org.meandre.core.*;
import org.meandre.annotations.*;

import org.meandre.components.discovery.cluster.hac.support.HACWork;
import org.meandre.components.datatype.table.Column;
import org.meandre.components.datatype.table.Table;
import org.meandre.components.datatype.table.ExampleTable;

/**
* @author Erik Johnson
* SEE http://www.dmg.org/v2-0/ClusteringModel.html#cluster for information on clustering PMML
*/

@Component(creator="Johnson",
          description="Write a ClusterModel out in PMML(Predictive Model Markup Language) format, optimized for Processing Display Algorithm.",
          name="WriteClusterDisplayPMML",
          tags="frequent pattern mining, clustering")

public class WriteClusterDisplayPMML  implements ExecutableComponent, ClusterPMMLTags {
   
	//ClusterModel input
	@ComponentInput(description="A representaiton of clusters to be displayed. " +
           "It is type of org.meandre.components.discovery.cluster.support.ClusterModel)",
                  name= "clusterModel")
   final static String DATA_INPUT = "clusterModel";

	//DOM document output
   @ComponentOutput(description="Document for PMML(org.dom4j.Document)",
                    name="document")
   public final static String DATA_OUTPUT = "document";

   //Property for the distance metric. This is set in the HACModelBuilder when the cluster model is created, this one should match
   @ComponentProperty(defaultValue="" + HACWork.s_Euclidean_DISTANCE,
           description="The distance metric to be used. This should be the same metric used in the HACModelBuilder component",
           name="distance_metric")
           public final static String DATA_PROPERTY_DISTANCE_METRIC = "distance_metric";
   		   /** The distance metric to be used. */
   		   protected static int _distanceMetric = HACWork.s_Euclidean_DISTANCE;
   
   //The Logger
   	private static Logger logger = Logger.getLogger("WriteClusterDisplayPMML");

   //PMML and the ClusterModel use different names to refer to distance metrics. This function converts between the two
   public static String getCompareName()
   {
	   if (_distanceMetric == 1)
	   {
		   return "cityBlock";
	   }
	   else
	   {
		   return "euclidean";
	   }
   }
   			
   public void initialize(ComponentContextProperties ccp) {
	   //get distance metric property
	   String param = ccp.getProperty(WriteClusterDisplayPMML.DATA_PROPERTY_DISTANCE_METRIC);
       int ival = -1;
       try {
    	   //try to parse it 
           ival = Integer.parseInt(param);
       } catch (Exception e){
           System.out.println("WriteClusterPMML invalid value for parameter: "
                             + WriteClusterDisplayPMML.DATA_PROPERTY_DISTANCE_METRIC +
                  ". Value is set to: " + _distanceMetric);
       }
       //set distance metric to value
       if (ival > -1){
           _distanceMetric = ival;
       }
   }
   public void dispose(ComponentContextProperties ccp) {}

   public void execute(ComponentContext cc)
           throws ComponentExecutionException, ComponentContextException {
	   //get cluster model input
       ClusterModel ct = (ClusterModel)cc.getDataComponentFromInput(DATA_INPUT);
       //create pmml output
       cc.pushDataComponentToOutput(DATA_OUTPUT, writePMML(ct));
   }

   public static Document writePMML(ClusterModel ct) {//, String fileName) {
	   //create a new document
       Document document = DocumentHelper.createDocument();
       document.addDocType("PMML", "http://www.dmg.org/v2-0/pmml_v2_0.dtd",
                           "http://www.dmg.org/v2-0/pmml_v2_0.dtd");
       
       int numCols = ct.getNumColumns();
       //add headers
       // Root
       Element root = document.addElement("PMML");
       root.addAttribute("version", "2.0");

       // Header
       Element header = root.addElement("Header");
       header.addAttribute("copyright", "NCSA ALG");
       header.addAttribute("description", "HAC cluster");

       // Data dictionary
       Element dataDictionary = root.addElement(DATA_DICT);
      
       //fill datafield
       for (int i=0; i<numCols; i++)
       {
    	   Column currentCol = ct.getColumn(i);
    	   Element datafield = dataDictionary.addElement(DATA_FIELD);
    	   datafield.addAttribute(NAME, currentCol.getLabel());
    	   if (currentCol.getIsNominal())
    	   {
    		   datafield.addAttribute(OPTYPE, CATEGORICAL);
    	   }
    	   else
    	   {
    		   datafield.addAttribute(OPTYPE, CONTINUOUS);
    	   }
       }
       //fill clustermodel element
       Element clusterModel = root.addElement(CLUSTER_MODEL);
       clusterModel.addAttribute(MODEL_NAME, ct.getLabel());
       clusterModel.addAttribute(FUNCTION_NAME, "clustering");
       clusterModel.addAttribute(NUM_CLUSTERS, new Integer(ct.getClusters().size()).toString());
       clusterModel.addAttribute(MODEL_CLASS, "centerBased");
       
       //these need to be changed to reflect the actual data used for mining! Right now it dumps all the datafields in the table!
       //Mining schema
       ExampleTable ctTable = (ExampleTable)ct.getTable();
       int [] inputs = ctTable.getInputFeatures();
       Element miningSchema = clusterModel.addElement(MINING_SCHEMA);

       for (int i=0; i<inputs.length; i++)
       {
    	   Element miningField = miningSchema.addElement(MINING_FIELD);
    	   miningField.addAttribute(NAME, ctTable.getColumnLabel(inputs[i]));
       }

       //add cluster fields
       for (int i=0; i<inputs.length; i++)
       {
    	   Element clusterField = clusterModel.addElement(CLUSTER_FIELD);
    	   clusterField.addAttribute(FIELD, ctTable.getColumnLabel(inputs[i]));
    	   clusterField.addAttribute(COMPARE_FUNCTION, getCompareName());
       }
       ArrayList clusters = ct.getClusters();
       //add clusters
       TableCluster rootCluster = ct.getRoot();
     
       
       document = addCluster (rootCluster, document, clusterModel);
   
       return document;
   }


	public static Document addCluster (TableCluster clust, Document doc, Element clusterModel)
	{
		
		   Element cluster = clusterModel.addElement(CLUSTER);
		   cluster.addAttribute (NAME, new Integer(clust.getClusterLabel()).toString() );
		   Element array = cluster.addElement(ARRAY);
		   array.addAttribute (NUMBER, new Integer(clust.getCentroid().length).toString());
		   array.addAttribute (TYPE, "real");
		   String centroidString = "";
		   double [] clusterCentroid = clust.getCentroid();
		 
		   for (int j = 0; j<clusterCentroid.length; j++)
		   {
			   centroidString += clusterCentroid[j] + " ";
		   }
		
		   array.setText(centroidString);
		   Element norm = cluster.addElement("centroid_norm");
		   norm.addAttribute ("norm", new Double (clust.getCentroidNorm()).toString());
		   Element dist = cluster.addElement("child_distance");
		   dist.addAttribute ("dist", new Double (clust.getChildDistance()).toString());
		   Element clusterTable = cluster.addElement("table");
	       
		   int [] indexes = clust.getMemberIndices();
		   
		   Table clusttab =  clust.getTable();
		   int numCols =  clusttab.getNumColumns();
	       int numRows  = indexes.length;
	
	       if (numRows>50)
	       {
	    	   //numRows = 100;//only use first 100 rows- save space and memory
	       numRows = 50;
	       }
	       logger.log(Level.INFO, new Integer(numRows).toString());
		   for (int j=0; j<numRows; j++)
		   {
			   Element clusterRow = clusterTable.addElement("row");
			   String rowString = "";
			   for (int i=0; i<numCols; i++)
			   {
				   rowString += clusttab.getObject(indexes[j],i).toString()+" ";
			   }
			   clusterRow.setText(rowString);
		   }
		   logger.log(Level.INFO, "TableFilled");
		   TableCluster lc = clust.getLC();
		   TableCluster rc = clust.getRC();
		   if (lc == null || rc == null)
		   {
			   return doc;
		   }
		   else
		   {
			   Element rchild = cluster.addElement("right_child");
			   Element lchild = cluster.addElement("left_child");
			   rchild.addAttribute("name", new Integer(rc.getClusterLabel()).toString());
			   lchild.addAttribute("name", new Integer(lc.getClusterLabel()).toString());
			   
			   doc = addCluster (lc, doc, clusterModel);
			   return addCluster (rc, doc, clusterModel);
		   }
		   
		   //Element test = array.addElement("", centroidString);
	}
}
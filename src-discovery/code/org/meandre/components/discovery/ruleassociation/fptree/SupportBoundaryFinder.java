package org.meandre.components.discovery.ruleassociation.fptree;

import  java.util.*;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.components.discovery.ruleassociation.fpgrowth.FPProb;
import org.meandre.components.discovery.ruleassociation.fpgrowth.FPSparse;
import org.meandre.components.discovery.ruleassociation.fpgrowth.FPTreeNode;
import org.meandre.components.discovery.ruleassociation.fpgrowth.FeatureTableElement;

@Component(creator="Lily Dong",
        description="<p>Overview: " +
    	"This module will scan the item sets in an <i>FPProb</i> object " +
   		"and determine the minimum and maximum supports. It adds this information " +
    	"to the <i>FPProb</i> object before writing it to output." +
    	"</p>" +
    	"</p><p>References: " +
    	"For more information on the FPGrowth frequent pattern mining algorithm, see &quot;Mining Frequent Patterns " +
    	"without Candidate Generation&quot;Jiawei Han, Jian Pei, and Yiwen Yin, 2000. " +
    	"</p><p>Data Handling: " +
    	"This module modifies the support boundary values in the <i>FPProb</i> object.",
        name="SupportBoundaryFinder",
        tags="support boundary finder",
        baseURL="meandre://seasr.org/components/")
public class SupportBoundaryFinder implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
			   		   defaultValue = "false",
			   		   name = "verbose")
	public final static String DATA_PROPERTY_VERBOSE = "verbose";
	@ComponentProperty(description = "In addition to the minimum support boundary, find the maximum support boundary.",
	   		   		   defaultValue = "false",
	   		   		   name = "discoverMaxSupport")
	public final static String DATA_PROPERTY_DISCOVERMAXSUPPORT = "discoverMaxSupport";

	@ComponentInput(description="The output parameters encapsulated in an FPProb object." +
			"<br>TYPE: org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPProb",
             		name= "FPProb")
    public final static String DATA_INPUT = "FPProb";

	@ComponentOutput(description="The output parameters encapsulated in an FPProb object." +
            "<br>TYPE: org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPProb",
             		 name="FPProb")
    public final static String DATA_OUTPUT = "FPProb";

  //==============
  // Data Members
  //==============
  private boolean DEBUG = false;

  //============
  // Properties
  //============
  private boolean m_verbose = false;
  private boolean _discSupport = false;

  public void initialize(ComponentContextProperties ccp) {
  }

  public void dispose(ComponentContextProperties ccp) {
  }

  public void execute(ComponentContext cc)
  throws ComponentExecutionException, ComponentContextException {
	  m_verbose = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_VERBOSE));
	  _discSupport = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_DISCOVERMAXSUPPORT));

      long start = System.currentTimeMillis();
      try {
    	  FPProb prob = (FPProb)cc.getDataComponentFromInput(DATA_INPUT);;

          prob.setSupport(0);
          prob = FPProcess(prob);

          cc.pushDataComponentToOutput(DATA_OUTPUT, prob);
      } catch (Exception ex) {
          ex.printStackTrace();
          System.out.println(ex.getMessage());
          System.out.println("ERROR: SupportBoundaryFinder.doit()");
          throw new ComponentExecutionException(ex);
      }
  }

  //=================
  // Private Methods
  //=================


  private FPProb FPProcess(FPProb prob) {
    FPTreeNode root = new FPTreeNode( -1, null, -1, -1);
    FPSparse tab = prob.getTable();
    int support = prob.getSupport();
    int maxSupport = prob.getMaxSupport(); //remove Max
    //Build header table
    TreeSet tfeats = new TreeSet(new Feature_Comparator());

    int numrows = tab.getNumRows();
    for (int i = 0, n = tab.getNumColumns(); i < n; i++) {
      int coltot = tab.getColumnTots(i);
      tfeats.add(new FeatureTableElement(tab.getLabel(i), coltot, i));
    }

    //trim the list
    ArrayList headers = new ArrayList();
    boolean b = true;
    for (Iterator it = tfeats.iterator(); it.hasNext(); ) {
      FeatureTableElement fte = (FeatureTableElement) it.next();
      headers.add(fte);
    }

    //build the FPTree
    for (int i = 0, n = tab.getNumRows(); i < n; i++) {
      FPTreeNode current = root;
      for (int j = 0, m = headers.size(); j < m; j++) {
        FeatureTableElement fte = (FeatureTableElement) headers.get(j);
        int val = tab.getInt(i, fte.getPosition());
        if (val > 0) {
          FPTreeNode next = current.getChild(fte.getLabel());
          if (next == null) {
            if (current.isRoot() || (current.getNumChildren() > 0)) {
              //leafcnt++;
            }
            next = new FPTreeNode(fte.getLabel(), current, val, j);
            fte.addPointer(next);
            current.addChild(next);
          }
          else {
            next.inc(val);
          }
          current = next;
        }
      }
      current.setHoldsDocs(true);
    }

    //find min support
    support = discoverMinSupport(headers);
    System.out.println("SupportBoundaryFinder -- Min Support discovered is: " + support);
    if (_discSupport) {
      maxSupport = discoverMaxSupport(headers, support);
      System.out.println("SupportBoundaryFinder -- Max Support discovered is: " + maxSupport);
    }
    prob.setSupport(support);
    prob.setMaxSupport(maxSupport); //remove Max
    return prob;
  }

  private int discoverMinSupport(ArrayList headers){
    for (int a = headers.size() - 1; a >= 0; a--) {
      FeatureTableElement fte = (FeatureTableElement) headers.get(a);
      List ptrs = fte.getPointers();
      for (int i = 0, n = ptrs.size(); i < n; i++){
        FPTreeNode node = (FPTreeNode)ptrs.get(i);
        if (node.getParent().isRoot()){
          return fte.getCnt();
        }
      }
      for (int i = 0, n = ptrs.size(); i < n; i++){
        FPTreeNode node = (FPTreeNode)ptrs.get(i);
        node.getParent().setHoldsDocs(true);
      }
    }
    return 0;
  }

  private int discoverMaxSupport(ArrayList headers, int supp){
    int lastsupp = Integer.MAX_VALUE;
    for (int a = 0, b = headers.size(); a < b; a++) {
      FeatureTableElement fte = (FeatureTableElement) headers.get(a);
      if (fte.getCnt() == supp){
        return lastsupp;
      }
      List ptrs = fte.getPointers();
      for (int i = 0, n = ptrs.size(); i < n; i++){
        FPTreeNode node = (FPTreeNode)ptrs.get(i);
        if (node.getHoldsDocs()){
          return lastsupp;
        }
      }
      if (lastsupp != fte.getCnt()){
        lastsupp = fte.getCnt();
      }
    }
    return lastsupp;
  }


  //=============
  // Inner Class
  //=============
  private class Feature_Comparator
      implements java.util.Comparator {

    /** The small deviation allowed in double comparisons */
    /**
     * put your documentation comment here
     */
    public Feature_Comparator() {
    }

    //======================
    //Interface: Comparator
    //======================
    public int compare(Object o1, Object o2) {
      FeatureTableElement fte1 = (FeatureTableElement) o1;
      FeatureTableElement fte2 = (FeatureTableElement) o2;
      if (fte1.getCnt() == fte2.getCnt()) {
        if (fte1.getLabel() > fte2.getLabel()) {
          return 1;
        }
        else if (fte1.getLabel() < fte2.getLabel()) {
          return -1;
        }
        else {
          System.out.println("ERROR ERROR ERROR: We never want to go here ...");
          return 0;
        }
      }
      else if (fte1.getCnt() > fte2.getCnt()) {
        return -1;
      }
      else {
        return 1;
      }
    }

    /**
     * put your documentation comment here
     * @param o
     * @return
     */
    public boolean equals(Object o) {
      return this.equals(o);
    }
  }


}




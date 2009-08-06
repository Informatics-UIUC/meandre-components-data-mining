package org.meandre.components.discovery.ruleassociation.fptree;

//==============
//Java Imports
//==============
import  java.util.*;

//===============
//Other Imports
//===============
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.components.datatype.table.Table;
import org.meandre.components.datatype.table.Sparse;
import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.datatype.table.MutableTable;

import org.meandre.components.discovery.ruleassociation.fpgrowth.FPPattern;
import org.meandre.components.discovery.ruleassociation.fpgrowth.FPProb;
import org.meandre.components.discovery.ruleassociation.fpgrowth.FPSparse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="<p>Overview: " +
           "This module transforms a <i>SparseExampleTable</i> containing term frequency values " +
           "into a data structure, <i>FPProb</i>, that efficiently represents item " +
           "occurrences within user supplied support constraints.</p>" +
           "<p> NOTE: All non-zero values for term frequency are treated alike -- they " +
           "are counted as a positive occurrence for that row.</p>" +
           "<p>References: " +
           "N/A." +
           "</p>" +
           "<p>Data Type Restrictions: " +
           "The input table must be a <i>SparseExampleTable</i> containing term frequency information." +
           "</p>" +
           "<p>Data Handling: " +
           "This module does not modify the input <i>SparseExampleTable</i>" +
           "</p>" +
           "<p>Scalability: " +
           "This module make a constant number of passes over the tabel data. " +
           "Memory usage is proportional to the size of the input <i>SparseExampleTable</i>" +
           "</p>" +
           "<p>Trigger Criteria: " +
           "Standard." +
           "</p>",
           name="LargeItemTableGenerator",
           tags="large item generator",
           baseURL="meandre://seasr.org/components/")

public class LargeItemTableGenerator implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
					   defaultValue = "false",
					   name = "verbose")
	public final static String DATA_PROPERTY_VERBOSE = "verbose";
	@ComponentProperty(description = "Query the input table to make sure there are no missing values.",
			   		   defaultValue = "false",
			   		   name = "checkMissingValues")
    public final static String DATA_PROPERTY_MVCHECK = "checkMissingValues";
	@ComponentProperty(description = "The minimum support vlaue for attributes in this data set.",
			    	   defaultValue = "1",
			    	   name = "support")
    public final static String DATA_PROPERTY_SUPPORT = "support";
	@ComponentProperty(description = "Remove any attributes that appear in all rows.",
			   		   defaultValue = "true",
			   		   name = "removeSaturatedFeatures")
    public final static String DATA_PROPERTY_REMSATFEATS = "removeSaturatedFeatures";

	@ComponentInput(description="The input data table for pattern mining." +
			"<br>TYPE: org.meandre.components.datatype.table.sparse.SparseTable",
             		name= "sparseTable")
    public final static String DATA_INPUT = "sparseTable";

	@ComponentOutput(description="An FPProb object representing." +
            "<br>TYPE: org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPProb",
             		 name="FPProb")
    public final static String DATA_OUTPUT = "FPProb";

  //==============
  // Data Members
  //==============
  private int[] _ifeatures = null;
  private boolean DEBUG = true;
  long start = 0;
  long stop = 0;
  //============
  // Properties
  //============
  private boolean m_verbose = false;
  private int _support = 1;
  private boolean _mvCheck = false;
  private boolean _remSatFeats = true;

  public void initialize(ComponentContextProperties ccp) {
      _ifeatures = null;
      start = System.currentTimeMillis();
  }

  public void dispose(ComponentContextProperties ccp) {
      _ifeatures = null;
      stop = System.currentTimeMillis();
      System.out.println((stop - start)/1000 + " seconds");
  }

  class Datum {
    int index;
    int count;

    public boolean equals(Object o) {
      Datum other = (Datum)o;
      return this.index == other.index && this.count == other.count;
    }
  }

  /**
   * In frequency include all occurrences of a term even if it only matches the POS tag criteria
   * for a subset of occurrences.
   */
  public void execute(ComponentContext cc)
  throws ComponentExecutionException, ComponentContextException {
	  m_verbose = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_VERBOSE));
	  _support = Integer.parseInt(cc.getProperty(DATA_PROPERTY_SUPPORT));
	  _mvCheck = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_MVCHECK));
	  _remSatFeats = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_REMSATFEATS));

      try {
          Table tab = (Table)cc.getDataComponentFromInput(DATA_INPUT);
          if (!(tab instanceof Sparse)) {
              throw  new ComponentExecutionException(
            		  "LargeItemTableGenerator: Only SpareTable is valid for this module.");
          }
          Table itable = tab;
          if (_mvCheck) {
              if (itable.hasMissingValues()) {
                  throw  new ComponentExecutionException(
                		  "Please replace or filter out missing values in your data.");
              }
          }
          if (itable.getNumRows() < 1) {
              throw  new ComponentExecutionException(
            		  "Input table has no rows.");
          }
          if (itable instanceof ExampleTable) {
        	  System.out.println("It is an example table.");
              _ifeatures = ((ExampleTable)itable).getInputFeatures();
          }
          else {
              _ifeatures = new int[itable.getNumColumns()];
              for (int i = 0, n = itable.getNumColumns(); i < n; i++) {
                  _ifeatures[i] = i;
              }
          }
          ArrayList remove = new ArrayList();
          int[] reminds = null;
          if (_remSatFeats) {
            if (this.DEBUG){
              System.out.println("Removing saturated features.");
            }
              /**
               * Remove features that saturate the data set.
               */
              remove = new ArrayList();
              int rowcnt = itable.getNumRows();
              for (int i = 0, n = _ifeatures.length; i < n; i++) {
                  int cnt = ((Sparse)itable).getColumnNumEntries(_ifeatures[i]);
                  if (cnt == rowcnt) {
                      remove.add(new Integer(_ifeatures[i]));
                  }
              }
              reminds = new int[remove.size()];
              for (int i = 0, n = remove.size(); i < n; i++) {
                  reminds[i] = ((Integer)remove.get(i)).intValue();
                  System.out.println(">>> Column " + reminds[i] + " removed because it covers all rows.");
              }

              Arrays.sort(reminds);
              for (int i = 0, n = reminds.length; i < n; i++){
                ((MutableTable)itable).removeColumn(reminds[i] - i);
              }

              if (remove.size() != 0) {
                  /**
                   * Re-select the input features since we removed columns.
                   */
                  if (itable instanceof ExampleTable) {
                      _ifeatures = ((ExampleTable)itable).getInputFeatures();
                  }
                  else {
                      _ifeatures = new int[itable.getNumColumns()];
                      for (int i = 0, n = itable.getNumColumns(); i < n; i++) {
                          _ifeatures[i] = i;
                      }
                  }
              }
              remove.clear();
          }
          TreeSet feats = new TreeSet(new Feature_Comparator());
          int[] colcnts = new int[itable.getNumColumns()];

          /**
           * Scan the features and get their num entries values.  If num entries is higher
           * than or equal to support add that feature to the TreeSet for sorting, else
           * add it to the remove list for removal.
           */
          if (this.DEBUG){
            System.out.println("Scan features for support.");
          }
          remove = new ArrayList();
          for (int i = 0, n = _ifeatures.length; i < n; i++) {
              //System.out.println(i + " " + _ifeatures[i]);
              int cnt = ((Sparse)itable).getColumnIndices(_ifeatures[i]).length;
              colcnts[_ifeatures[i]] = cnt;
              if (cnt >= _support) {
                  Datum obarr = new Datum();
                  obarr.index = _ifeatures[i];
                  obarr.count = cnt;
                  feats.add(obarr);
              }
              else {
                  //add to remove list
                  remove.add(new Integer(_ifeatures[i]));
              }
          }

          System.out.println("size of feats: "+feats.size());
          /*Iterator iter = feats.iterator();
          while(iter.hasNext()) {
            Object[] arr = (Object[])iter.next();
            Integer index = (Integer)arr[0];
            Integer cnt = (Integer)arr[1];
            System.out.println("feature: "+itable.getColumnLabel(index.intValue())+
                               " cnt: "+cnt.intValue());
          }*/

          int[] rowcnt = new int[itable.getNumRows()];
          int[] suparr = new int[itable.getNumRows()];
          if (m_verbose) {
              /**
               * Scan rows for the max support of their features.  Build and print list
               * of support/coverage values.  We do this here before we trim the features
               * that fall under the support.
               */
              for (int i = 0, n = itable.getNumRows(); i < n; i++) {
                  int[] rowind = ((Sparse)itable).getRowIndices(i);
                  int max = 1;
                  for (int j = 0, m = rowind.length; j < m; j++) {
                      int cnt = colcnts[rowind[j]];
                      if (max < cnt) {
                          max = cnt;
                      }
                  }
                  rowcnt[i] = max;
                  suparr[max - 1]++;
              }
              int scnt = 0;
              int numrows = itable.getNumRows();
              for (int i = 0, n = suparr.length; i < n; i++) {
                  System.out.println("SUPPORT: " + (i + 1) + " / " + "TRANS REMOVED: "
                          + scnt + " / " + "COVERAGE: %" + (((double)numrows
                          - scnt)/((double)numrows)*100));
                  scnt += suparr[i];
              }
          }

          /**
           * Build new table with integer columns.
           */
          if (DEBUG) {
              System.out.println("Building new table (adding columns).");
          }

          int ccnt = _ifeatures.length;
          FPSparse otab = new FPSparse(ccnt);
          int rcnt = itable.getNumRows();
          FPPattern.clearElementMapping();
          for (int i = 0, n = ccnt; i < n; i++) {
              FPPattern.addElementMapping(i, itable.getColumnLabel(_ifeatures[i]));
              otab.addColumn(i);
          }

          if (DEBUG) {
              System.out.println("Copying rows to new table.");
          }
          for (int i = 0, n = rcnt; i < n; i++) {
              int[] rowind = ((Sparse)itable).getRowIndices(i);
              for (int j = 0, m = rowind.length; j < m; j++) {
                  if (!(rowind[j] >= otab.getNumColumns())) {
                      otab.setInt(1, i, rowind[j]);
                  }
              }
          }
          /**
           * Gen. feats array. (Don't need this now)
           */
          int[] flist = new int[0];
          FPProb prob = new FPProb(otab, flist, this._support);

          cc.pushDataComponentToOutput(DATA_OUTPUT, prob);
      } catch (Exception ex) {
          ex.printStackTrace();
          System.out.println(ex.getMessage());
          System.out.println("ERROR: LargeItemTableGenerator.doit()");
          throw new ComponentExecutionException(ex);
      }
  }

  //=================
  // Private Methods
  //=================
  //=============
  // Inner Class
  //=============
  private class Feature_Comparator
          implements java.util.Comparator {

      /** The small deviation allowed in double comparisons */
      /**
       * put your documentation comment here
       */
      public Feature_Comparator () {
      }

      //======================
      //Interface: Comparator
      //======================
      public int compare (Object o1, Object o2) {
          /*Object[] objarr1 = (Object[])o1;
          Object[] objarr2 = (Object[])o2;
          if (((Integer)objarr1[1]).intValue() == ((Integer)objarr2[1]).intValue()) {
              if (((Integer)objarr1[0]).intValue() > ((Integer)objarr2[0]).intValue()) {
                  return  -1;
              }
              else if (((Integer)objarr1[0]).intValue() < ((Integer)objarr2[0]).intValue()) {
                  return  1;
              }
              else {
                  return  0;
              }
          }
          else if (((Integer)objarr1[1]).intValue() > ((Integer)objarr2[1]).intValue()) {
              return  -1;
          }
          else {
              return  1;
          }*/
    //Object[] objarr1 = (Object[])o1;
    //Object[] objarr2 = (Object[])o2;
    Datum objarr1 = (Datum)o1;
    Datum objarr2 = (Datum)o2;
    if (objarr1.count == objarr2.count) {
        if (objarr1.index > objarr2.index) {
            return  -1;
        }
        else if (objarr1.index < objarr2.index) {
            return  1;
        }
        else {
            return  0;
        }
    }
    else if (objarr1.count > objarr2.count) {
        return  -1;
    }
    else {
        return  1;
    }

      }

      /**
       * put your documentation comment here
       * @param o
       * @return
       */
      public boolean equals (Object o) {
          return  this.equals(o);
      }
  }
}





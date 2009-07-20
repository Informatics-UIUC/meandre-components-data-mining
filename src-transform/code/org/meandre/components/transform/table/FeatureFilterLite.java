package org.meandre.components.transform.table;



//==============
// Java Imports
//==============

import java.util.*;
//===============
// Other Imports
//===============

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.components.datatype.table.Table;
import org.meandre.components.datatype.table.Sparse;
import org.meandre.components.datatype.table.ExampleTable;
import org.meandre.components.datatype.table.MutableTable;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import gnu.trove.*;

@Component(creator="Loretta Auvil",
        description="<p>Overview: " +
        "This module scans the input <i>SparseTable</i> and any term (column) whose support "+
        "does not fall within the range specified has its column removed from the table.  This can greatly reduce "+
        "the total number features used for learning -- improving accuracy and performance."+
        "</p>"+
        "<p>Data Type Restrictions: "+
        "The input <i>Table</i> must be an instance of a <i>SparseTable</i>."+
        "</p>"+
        "<p>Data Handling: "+
        "A new <i>SparseTable</i> instance is created and only columns that will be kept "+
        "are copied into it."+
        "</p>"+
        "<p>Scalability: "+
        "Creates a second table on the same order of size as the original.  Columns "+
        "from the first table are inserted into the second table; no copies are made.  Algorithm makes one pass over the "+
        "table columns and one pass over the table data."+
        "</p>",
        name="FeatureFilterLite",
        tags="feature filter",
        baseURL="meandre://seasr.org/components/")
        
public class FeatureFilterLite implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.", defaultValue = "false", name = "verbose")
	public final static String DATA_PROPERTY_VERBOSE = "verbose";
	@ComponentProperty(description = "Remove Columns With Only One Entry: "
			+ "If a columnn in a sparse table has only one entry then remove that column. "
			+ "NOTE: if lower bound is set to a positive value this property is ignored.", defaultValue = "true", name = "removeColumnsWithOnlyOneEntry")
	public final static String DATA_PROPERTY_ONLYONEENTRY = "removeColumnsWithOnlyOneEntry";
	@ComponentProperty(description = "Remove Columns with All Entries Present: "
			+ "If a columnn in a sparse table has every entry possible then remove that column. "
			+ "NOTE: if upper bound is set to a positive value this property is ignored.", defaultValue = "true", name = "removeColumnsWithAllEntries")
	public final static String DATA_PROPERTY_ALLENTRIES = "removeColumnsWithAllEntries";
	@ComponentProperty(description = "Percent Support for Lower Bounds Cutoff: "
			+ "The percent of support below which a given feature (column) will be removed. "
			+ "NOTE: If this value is set to a positive value the \"removeColumnsWithOnlyOneEntry\" property is ignored.", defaultValue = "0", name = "lowerBoundSupport")
	public final static String DATA_PROPERTY_LOWERBOUNDSUPPORT = "lowerBoundSupport";
	@ComponentProperty(description = "Percent Support for Upper Bounds Cutoff: "
			+ "The percent of support above which a given feature (column) will be removed. "
			+ "NOTE: If this value is set to a positive value the \"removeColumnsWithAllEntries\" property is ignored.", defaultValue = "100", name = "upperBoundSupport")
	public final static String DATA_PROPERTY_UPPERBOUNDSUPPORT = "upperBoundSupport";
	@ComponentInput(description = "The input data table for transformation."
			+ "<br>TYPE: org.meandre.components.datatype.table.sparse.SparseTable", name = "sparseTable")
	public final static String DATA_INPUT = "sparseTable";

	@ComponentOutput(description = "The resulting modified table."
			+ "<br>TYPE: org.meandre.components.datatype.table.sparse.SparseTable", name = "sparseTable")
	public final static String DATA_OUTPUT = "sparseTable";


  //==================
  // Option Accessors

  private boolean _removeones = false;
  public boolean getRemoveColumnsWithOnlyOneEntry(){ return _removeones;}
  public void setRemoveColumnsWithOnlyOneEntry(boolean b){_removeones = b;}

  private boolean _removealls = false;
  public boolean getRemoveColumnsWithAllEntries(){ return _removealls;}
  public void setRemoveColumnsWithAllEntries(boolean b){_removealls = b;}

  private double _lowerBoundSupport = 0;
  public double getLowerBoundSupport(){return _lowerBoundSupport;}
  public void setLowerBoundSupport(double d){_lowerBoundSupport = d;}

  private double _upperBoundSupport = 100;
  public double getUpperBoundSupport(){return _upperBoundSupport;}
  public void setUpperBoundSupport(double d){_upperBoundSupport = d;}

  private boolean _verbose = false;
  public boolean getVerbose(){ return _verbose;}
  public void setVerbose(boolean b){_verbose = b;}

  /**
   * In frequency include all occurrences of a term even if it only matches the POS tag criteria
   * for a subset of occurrences.
   */
  public void execute(ComponentContext cc) throws ComponentExecutionException,
	ComponentContextException {
	   
    try{
       ExampleTable table = (ExampleTable)cc.getDataComponentFromInput(DATA_INPUT);
      
      //HashSet colset = new HashSet(); //indices of input features
      int[] iinds = table.getInputFeatures();
      TIntHashSet colset = new TIntHashSet(iinds.length);
      System.out.println("\n\n" + iinds.length + " features input ...");
      if (iinds != null){
        for (int i = 0, n = iinds.length; i < n; i++){
          //colset.add(new Integer(iinds[i]));
          colset.add(iinds[i]);
        }
      }

      //HashSet ocolset = new HashSet(); //indices of output features
      int[] oinds = table.getOutputFeatures();
      TIntHashSet ocolset = new TIntHashSet(oinds.length);
      if (oinds != null){
        for (int i = 0, n = oinds.length; i < n; i++) {
          //ocolset.add(new Integer(oinds[i]));
          ocolset.add(oinds[i]);
        }
      }


      ExampleTable copy = (ExampleTable)table.createTable().toExampleTable();
      //add empty columns for each column.

      // Hash the column names of the output table to indices
      //HashMap colMap2 = new HashMap();
      TObjectIntHashMap colMap2 = new TObjectIntHashMap();

      long minsupp = 0;
      int numrows = table.getNumRows();
      long maxsupp = numrows;

      double lbs = this.getLowerBoundSupport();
      if ((lbs > 0) && (lbs <= 100)){
        minsupp = Math.round((lbs/100)*((double)numrows));
      } else if (this.getRemoveColumnsWithOnlyOneEntry()){
        minsupp = 2;
      }
      System.out.println("Feature Support Filter min support set to: " + minsupp);
      double ubs = this.getUpperBoundSupport();
      if ((ubs > 0) && (ubs <= 100)){
        maxsupp = Math.round((ubs/100)*((double)numrows));
      } else if (this.getRemoveColumnsWithAllEntries()){
        maxsupp = numrows-1;
      }
      System.out.println("Feature Support Filter max support set to: " + maxsupp);

      //ArrayList list = new ArrayList(); //list of indices of in features in the output table
      //ArrayList list2 = new ArrayList();//list of indices of out features in the output table
      TIntArrayList list = new TIntArrayList(colset.size()); //list of indices of in features in the output table
      TIntArrayList list2 = new TIntArrayList(ocolset.size());//list of indices of out features in the output table
      int j = 0; //index of column that is currently added to the output table
      //for each column
      for (int i = 0, n = table.getNumColumns(); i < n; i++) {
        //AbstractSparseColumn col = (AbstractSparseColumn) table.getColumn(i);
        String colstr = table.getColumnLabel(i); //label
        int[] colindices = ((Sparse)table).getColumnIndices(i); //valid rows in column
        //boolean conts = colset.contains(new Integer(i)); //is this an input feature?
        boolean conts = colset.contains(i); //is this an input feature?
        //boolean conts2 = ocolset.contains(new Integer(i)); //is this an output feature
        boolean conts2 = ocolset.contains(i); //is this an output feature

        //if supported by lower or upper bound or not an input feature
        if ( ((colindices.length >= minsupp) && (colindices.length <= maxsupp )) || (!conts)) {
          //add this column to the output table
          //copy.addColumn(((Sparse)copy).getTableFactory().createColumn(table.getColumnType(i)));
          //copy.setColumnLabel(colstr, j);
          copy.addColumn( table.getColumn(i) );
          //add to the map
          //colMap2.put(colstr, new Integer(j));
          colMap2.put(colstr, j);
          if (conts){
            //list.add(new Integer(j));
            list.add(j);
          }
          if (conts2){
            //list2.add(new Integer(j));
            list2.add(j);
          }
          j++;
        }else if(this._verbose){
  //        if ( colindices.length < minsupp)
//          System.out.println("column indexed " + i + " is below the min support. labeled " + colstr);
        if ( colindices.length > maxsupp)
          System.out.println("column indexed " + i + " is above the max support. labeled " + colstr);
        }
      }

      //setting the input features into the output table
      if (iinds != null){
        int[] ifeats = new int[list.size()];
        for (int i = 0, n = ifeats.length; i < n; i++) {
          //ifeats[i] = ( (Integer) list.get(i)).intValue();
          ifeats[i] = list.get(i);
        }
        copy.setInputFeatures(ifeats);
      }
      //setting the output features into the output table
      if (oinds != null){
        int[] ofeats = new int[list2.size()];
        for (int i = 0, n = ofeats.length; i < n; i++) {
          //ofeats[i] = ( (Integer) list2.get(i)).intValue();
          ofeats[i] = list2.get(i);
        }
        copy.setOutputFeatures(ofeats);
      }


//for each column in original table
/*      for (int i = 0, n = table.getNumColumns(); i < n; i++) {
        //get its index in the output table
        //Integer oint = (Integer) colMap2.get(table.getColumnLabel(i));
        if(!colMap2.containsKey(table.getColumnLabel(i))) {
          continue;
        }
        int oint = colMap2.get(table.getColumnLabel(i));
        //if (oint == null) {
        //  continue;
        //}
        int[] spcol = null; //valid rows in current column
        spcol = ( (Sparse) table).getColumnIndices(i);
        //for each entry in current column - copy the value
        for (int jk = 0, m = spcol.length; jk < m; jk++) {
          String newval = table.getString(spcol[jk], i);
          //copy.setString(newval, spcol[jk], oint.intValue());
          copy.setString(newval, spcol[jk], oint);
        }
      }*/


      System.out.println("Feature Support Filter -- " + copy.getInputFeatures().length + " features remain\n\n");

      cc.pushDataComponentToOutput(DATA_OUTPUT, copy);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(ex.getMessage());
      System.out.println("ERROR: FeatureFilter.execute()");
    }
  }


  private class Terms_Comparator implements java.util.Comparator {

    public Terms_Comparator() {
    }

    //======================
    //Interface: Comparator
    //======================
    public int compare(Object o1, Object o2) {
      int i1 = ( (Integer) o1).intValue();
      int i2 = ( (Integer) o2).intValue();

      if (i1 > i2) {
        return 1;
      }
      else if (i1 < i2) {
        return -1;
      }
      else {
        return 0;
      }
    }

    public boolean equals(Object o) {
      return this.equals(o);
    }
  }


public void dispose(ComponentContextProperties arg0)
		throws ComponentExecutionException, ComponentContextException {
	// TODO Auto-generated method stub
	
}

public void initialize(ComponentContextProperties cc)
		throws ComponentExecutionException, ComponentContextException {
	// TODO Auto-generated method stub
	_verbose = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_VERBOSE));
	_removeones = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_ONLYONEENTRY));
	_removealls = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_ALLENTRIES));
	_lowerBoundSupport = Integer.parseInt(cc.getProperty(DATA_PROPERTY_LOWERBOUNDSUPPORT));
	_upperBoundSupport = Integer.parseInt(cc.getProperty(DATA_PROPERTY_UPPERBOUNDSUPPORT));
}

}

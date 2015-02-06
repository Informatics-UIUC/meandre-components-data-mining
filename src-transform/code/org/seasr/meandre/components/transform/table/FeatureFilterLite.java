package org.seasr.meandre.components.transform.table;

//==============
// Java Imports
//==============

import gnu.trove.set.hash.TIntHashSet;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Sparse;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

@Component(creator = "Loretta Auvil", description = "<p>Overview: "
		+ "This module scans the input <i>SparseTable</i> and any term (column) whose support "
		+ "does not fall within the range specified has its column removed from the table.  This can greatly reduce "
		+ "the total number features used for learning -- improving accuracy and performance."
		+ "</p>"
		+ "<p>Data Type Restrictions: "
		+ "The input <i>Table</i> must be an instance of a <i>SparseTable</i>."
		+ "</p>"
		+ "<p>Data Handling: "
		+ "The input <i>SparseTable</i> instance is modified, which is a change from previous version."
		+ "</p>"
		+ "<p>Scalability: "
		+ "Algorithm makes one pass over the "
		+ "table columns and one pass over the table data." + "</p>", name = "FeatureFilterLite", tags = "feature filter", baseURL = "meandre://seasr.org/components/data-mining/")
public class FeatureFilterLite extends AbstractExecutableComponent {

	@ComponentProperty(description = "Verbose output.", defaultValue = "false", name = "verbose")
	public final static String PROP_VERBOSE = "verbose";

	@ComponentProperty(description = "Remove Columns With Only One Entry: "
			+ "If a columnn in a sparse table has only one entry then remove that column. "
			+ "NOTE: if lower bound is set to a positive value this property is ignored.", defaultValue = "true", name = "removeColumnsWithOnlyOneEntry")
	public final static String PROP_ONLYONEENTRY = "removeColumnsWithOnlyOneEntry";

	@ComponentProperty(description = "Remove Columns with All Entries Present: "
			+ "If a columnn in a sparse table has every entry possible then remove that column. "
			+ "NOTE: if upper bound is set to a positive value this property is ignored.", defaultValue = "true", name = "removeColumnsWithAllEntries")
	public final static String PROP_ALLENTRIES = "removeColumnsWithAllEntries";

	@ComponentProperty(description = "Percent Support for Lower Bounds Cutoff: "
			+ "The percent of support below which a given feature (column) will be removed. "
			+ "NOTE: If this value is set to a positive value the \"removeColumnsWithOnlyOneEntry\" property is ignored.", defaultValue = "0", name = "lowerBoundSupport")
	public final static String PROP_LOWERBOUNDSUPPORT = "lowerBoundSupport";

	@ComponentProperty(description = "Percent Support for Upper Bounds Cutoff: "
			+ "The percent of support above which a given feature (column) will be removed. "
			+ "NOTE: If this value is set to a positive value the \"removeColumnsWithAllEntries\" property is ignored.", defaultValue = "100", name = "upperBoundSupport")
	public final static String PROP_UPPERBOUNDSUPPORT = "upperBoundSupport";

	@ComponentInput(description = "The input data table for transformation."
			+ "<br>TYPE: org.seasr.datatypes.datamining.table.sparse.SparseTable", name = "sparseTable")
	public final static String IN_SPARSETABLE = "sparseTable";

	@ComponentOutput(description = "The resulting modified table."
			+ "<br>TYPE: org.seasr.datatypes.datamining.table.sparse.SparseTable", name = "sparseTable")
	public final static String OUT_SPARSETABLE = "sparseTable";

	// ==================
	// Option Accessors

	private boolean _removeones = false;

	public boolean getRemoveColumnsWithOnlyOneEntry() {
		return _removeones;
	}

	public void setRemoveColumnsWithOnlyOneEntry(boolean b) {
		_removeones = b;
	}

	private boolean _removealls = false;

	public boolean getRemoveColumnsWithAllEntries() {
		return _removealls;
	}

	public void setRemoveColumnsWithAllEntries(boolean b) {
		_removealls = b;
	}

	private double _lowerBoundSupport = 0;

	public double getLowerBoundSupport() {
		return _lowerBoundSupport;
	}

	public void setLowerBoundSupport(double d) {
		_lowerBoundSupport = d;
	}

	private double _upperBoundSupport = 100;

	public double getUpperBoundSupport() {
		return _upperBoundSupport;
	}

	public void setUpperBoundSupport(double d) {
		_upperBoundSupport = d;
	}

	private boolean _verbose = false;

	public boolean getVerbose() {
		return _verbose;
	}

	public void setVerbose(boolean b) {
		_verbose = b;
	}

	/**
	 * In frequency include all occurrences of a term even if it only matches
	 * the POS tag criteria for a subset of occurrences.
	 */
	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Object input = cc.getDataComponentFromInput(IN_SPARSETABLE);

		ExampleTable table;

		if (!(input instanceof ExampleTable)) {
			table = ((Table) input).toExampleTable();
			int maxCols = table.getNumColumns();
			int[] iind = new int[maxCols];
			for (int i = 0; i < maxCols; i++)
				iind[i] = i;
			table.setInputFeatures(iind);
		} else
			table = (ExampleTable) input;

		int[] iinds = table.getInputFeatures();
		TIntHashSet colset = new TIntHashSet(iinds.length);
		console.fine("input features: " + iinds.length);
		if (iinds != null) {
			for (int i = 0, n = iinds.length; i < n; i++) {
				colset.add(iinds[i]);
			}
		}

		long minsupp = 0;
		int numrows = table.getNumRows();
		long maxsupp = numrows;

		double lbs = this.getLowerBoundSupport();
		if ((lbs > 0) && (lbs <= 100)) {
			minsupp = Math.round((lbs / 100) * (numrows));
		} else if (this.getRemoveColumnsWithOnlyOneEntry()) {
			minsupp = 2;
		}
		console.fine("Min support set to: " + minsupp);
		double ubs = this.getUpperBoundSupport();
		if ((ubs > 0) && (ubs <= 100)) {
			maxsupp = Math.round((ubs / 100) * (numrows));
		} else if (this.getRemoveColumnsWithAllEntries()) {
			maxsupp = numrows - 1;
		}
		console.fine("Max support set to: " + maxsupp);

		// for each column
		for (int i = table.getNumColumns() - 1; i >= 2; i--) {
			String colstr = table.getColumnLabel(i); 
			// valid rows in column
			int[] colindices = ((Sparse) table).getColumnIndices(i); 
			
			// if supported by lower or upper bound or not an input feature
			if (colindices.length < minsupp || colindices.length > maxsupp) {
				if (colset.contains(i)) {
					table.removeColumn(i); 
					if (this._verbose) {
						if (colindices.length < minsupp)
							console.fine("column indexed " + i
									+ " is below the min support. labeled "
									+ colstr);

						if (colindices.length > maxsupp)
							console.fine("column indexed " + i
									+ " is above the max support. labeled "
									+ colstr);
					}
				}
			}
		}

		console.fine("Features Remain: " + table.getInputFeatures().length);

		cc.pushDataComponentToOutput(OUT_SPARSETABLE, table);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
			throws Exception {
		// TODO Auto-generated method stub
		_verbose = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_VERBOSE,
				ccp));
		_removeones = Boolean.parseBoolean(getPropertyOrDieTrying(
				PROP_ONLYONEENTRY, ccp));
		_removealls = Boolean.parseBoolean(getPropertyOrDieTrying(
				PROP_ALLENTRIES, ccp));
		_lowerBoundSupport = Integer.parseInt(getPropertyOrDieTrying(
				PROP_LOWERBOUNDSUPPORT, ccp));
		_upperBoundSupport = Integer.parseInt(getPropertyOrDieTrying(
				PROP_UPPERBOUNDSUPPORT, ccp));
	}
}

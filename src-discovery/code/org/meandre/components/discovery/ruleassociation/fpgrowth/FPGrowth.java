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

package org.meandre.components.discovery.ruleassociation.fpgrowth;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPPattern;
import org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPProb;
import org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPSparse;
import org.meandre.components.discovery.ruleassociation.fpgrowth.support.FPTreeNode;
import org.meandre.components.discovery.ruleassociation.fpgrowth.support.FeatureTableElement;
import org.meandre.components.discovery.ruleassociation.support.ItemSets;
import org.meandre.components.discovery.ruleassociation.support.ItemSetInterface;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;


/**
 * <p>Title:</p>
 *
 * <p>Description: This component implements the FPGrowth algorithm to generate
 * frequent itemsets consisting of items that occur in a sufficient number of
 * examples to satisfy the minimum support criteria.</p>
 *
 * <p>Detailed Description: This component takes an <i>Item Sets</i> object that
 * has been generated by a <i>Table To Item Sets</i> component and uses the
 * FPGrowth algorithm to find the combinations of items that satisfy a minimum
 * support criteria. An item is an [attribute,value] pair that occurs in the set
 * of examples being mined. The user controls the support criteria via the <i>
 * Minimum Support %</i> property that specifies the percentage of all examples
 * that must contain a given combination of items before that combination is
 * included in the generated output. Each combination of items that satisfies
 * the <i>Minimum Support %</i> is called a <i>Frequent Itemset</i>.</p>
 *
 * <p>The user can restrict the maximum number of items included in any frequent
 * itemset with the <i>Maximum Items Per Rule</i> property. The generation of
 * sets with large number of items can be computationally expensive, so setting
 * this property in conjunction with the <i>Minimum Support %</i> property helps
 * keep the component runtime reasonable.</p>
 *
 * <p>In a typical flow the <i>Frequent Item Sets</i> output port from this
 * component is connected to a <i>Compute Confidence</i> component which forms
 * association rules that satisfy a minimum confidence value.</p>
 *
 * <p>References: For more information on the FPGrowth frequent pattern mining
 * algorithm, see &quot;Mining Frequent Patterns without Candidate
 * Generation&quot;Jiawei Han, Jian Pei, and Yiwen Yin, 2000.</p>
 *
 * <p>Limitations: The <i>FPGrowth</i> and <i>Compute Confidence</i> components
 * currently build rules with a single item in the consequent.</p>
 *
 * <p>Data Type Restrictions: While this component can operate on attributes of any
 * datatype, in practice it is usually infeasible to use it with
 * continuous-valued attributes. The component considers each [attribute,value]
 * pair that occurs in the examples individually when building the frequent
 * itemsets. Continuous attributes (and categorical attributes with a large
 * number of values) are less likely to meet the Minimum Support requirements
 * and can result in unacceptably long execution time. Typically <i>Choose
 * Attributes</i> and <i>Binning</i> components should appear in the itinerary
 * prior to the <i>Table to Item Sets</i> component, whose output produces the <i>
 * Item Sets</i> object used as input by this component. The Choosing/Binning
 * components can reduce the number of distinct [attribute,value] pairs that must
 * be considered in this component to a reasonable number.</p>
 *
 * <p>Data Handling: This component does not modify the input Item Sets in any way.
 * </p>
 *
 * <p>Scalability: This component creates an array of integers to hold the indices
 * of the items in each frequent itemset. The component may be computationally
 * intensive, and scales with the number of Item Sets entries to search. The
 * user can limit the size of the frequent itemsets although this will have
 * little effect on performance for this algorithm. Choosing/Binning components
 * can be included in the itinerary prior to this components to reduce the number
 * of Item Sets entries.</p>
 *
 * @author D. Searsmith (original)
 * @author Boris Capitanu
 * 
 * BC: Imported from d2k (ncsa.d2k.modules.core.discovery.ruleassociation.fpgrowth.FPGrowth)
 * 
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>This component implements the FPGrowth algorithm to generate frequent itemsets consisting of "+
        "items that occur in a sufficient number of examples to satisfy the minimum support criteria. "+

        "</p><p>Detailed Description: "+
        "This component takes an <i>Item Sets</i> object that has been generated by a <i>Table To Item Sets</i> "+
        "component and uses the FPGrowth algorithm to find "+
        "the combinations of items that satisfy a minimum support criteria. "+
        "An item is an [attribute,value] pair that occurs in the set of examples being mined. "+
        "The user controls the support criteria via the <i>Minimum Support %</i> property that specifies the "+
        "percentage of all examples that must contain a given combination of items "+
        "before that combination is included in the generated output. "+
        "Each combination of items that satisfies the <i>Minimum Support %</i> is called "+
        "a <i>Frequent Itemset</i>. "+

        "</p><p> "+
        "The user can restrict the maximum number of items included in any frequent itemset with "+
        "the <i>Maximum Items Per Rule</i> property.  The generation of sets with large number of items "+
        "can be computationally expensive, so setting this property in conjunction with the <i>Minimum Support %</i> "+
        "property helps keep the component runtime reasonable. "+

        "</p><p>"+
        "In a typical flow the <i>Frequent Item Sets</i> output port from this component is connected to "+
        "a <i>Compute Confidence</i> component which forms "+
        "association rules that satisfy a minimum confidence value. "+

        "</p><p>References: "+
        "For more information on the FPGrowth frequent pattern mining algorithm, see &quot;Mining Frequent Patterns "+
        "without Candidate Generation&quot;Jiawei Han, Jian Pei, and Yiwen Yin, 2000. "+

        "</p><p>Limitations: "+
        "The <i>FPGrowth</i> and <i>Compute Confidence</i> components currently "+
        "build rules with a single item in the consequent.  "+

        "</p><p>Data Type Restrictions: "+
        "While this component can operate on attributes of any datatype, in practice it is usually infeasible "+
        "to use it with continuous-valued attributes.   The component considers each [attribute,value] pair that occurs "+
        "in the examples individually when building the frequent itemsets.  Continuous attributes (and categorical "+
        "attributes with a large number of values) are less likely to meet the Minimum Support requirements "+
        "and can result in unacceptably long execution time.  Typically <i>Choose Attributes</i> and <i>Binning</i> "+
        "components should appear in the itinerary prior to the <i>Table to Item Sets</i> component, whose output produces "+
        "the <i>Item Sets</i> object used as input by this component.   The Choosing/Binning components can reduce the "+
        "number of distinct [attribute,value] pairs that must be considered in this component to a reasonable number. "+

        "</p><p>Data Handling: "+
        "This component does not modify the input Item Sets in any way. "+

        "</p><p>Scalability: "+
        "This component creates an array of integers to hold the indices of the items in each frequent itemset. "+
        "The component may be computationally intensive, and scales with the number of Item Sets entries to search. "+
        "The user can limit the size of the frequent itemsets although this will have little effect on performance for "+
        "this algorithm. Choosing/Binning components can be included in the itinerary "+
        "prior to this components to reduce the number of Item Sets entries.  </p>",

        name = "FPGrowth",
        tags = "frequent pattern mining, rule association, discovery"
)
public class FPGrowth implements ExecutableComponent {

    @ComponentInput(description = "An object produced by a <i>Table To Item Sets</i> component " +
            "containing items that will appear in the frequent itemsets. ", name = "item_sets")
    final static String DATA_INPUT_ITEM_SETS = "item_sets";

    @ComponentOutput(description = "A representation of the frequent itemsets found by the component. " +
            "This representation encodes the items used in the sets " +
            "and the number of examples in which each set occurs. This output is typically " +
            "connected to a <i>Compute Confidence</i> component.", name = "freq_item_sets")
    final static String DATA_OUTPUT_FREQ_ITEM_SETS = "freq_item_sets";

    @ComponentProperty(description = "The percent of all examples that must contain a given set of items " +
            "before an association rule will be formed containing those items. " +
            "This value must be greater than 0 and less than or equal to 100.", name = "min_support",
            defaultValue = "20.0")
    final static String DATA_PROPERTY_MIN_SUPPORT = "min_support";

    @ComponentProperty(description = "The maximum number of items to include in any rule. " +
            "Does not impact performance for this algorithm as it does for Apriori." +
            "This value cannot be less than 2.", name = "max_items",
            defaultValue = "6")
    final static String DATA_PROPERTY_MAX_ITEMS = "max_items";

    @ComponentProperty(description = "If this property is true, the component will report " +
            "progress information to the console.", name = "verbose",
            defaultValue = "True")
    final static String DATA_PROPERTY_VERBOSE = "verbose";

    @ComponentProperty(description = "If this property is true, the component will " +
            "write verbose status information to the console.", name = "debug",
            defaultValue = "False")
    final static String DATA_PROPERTY_DEBUG = "debug";

    //~ Static fields/initializers **********************************************

    /** Use serialVersionUID for interoperability. */
    static private final long serialVersionUID = -7565706965121174249L;

    //~ Instance fields *********************************************************

    /** The discovered frequent patterns that meet the support criteria. */
    private ArrayList _patterns;

    /**
     * the verbosity property. If this property is true, the component will report
     * progress information to the console.
     */
    private boolean _verbose;

    /**
     * the Debug property. Is sets to true, this component outpus debug information
     * to stdout.
     */
    private boolean _debug;

    private Logger _logger;
    
    /** the maximum number of attributes that will be included in any rule. */
    private int _maxSize;

    /**
     * this is the number of sets that must contain a given rule for it (the
     * rule) to meet the support.
     */
    int _cutoff;


    /**
     * this property is the min acceptable support, expressed as a percentage.
     */
    double _support;

    //~ Constructors ************************************************************

    /**
     * Creates a new FPGrowth object.
     */
    public FPGrowth() { }

    //~ Methods *****************************************************************

    /**
     * Creates all available pattern combinations using the nodes in <codE>
     * list</codE>. e.g. if list is [A,B,C] then the patterns to be added are AB,
     * BC, AC, ABC (order does not matter)
     *
     * @param list  A List of FPTreeNode objects
     * @param alpha Indices of columns in original table that take part int his
     *              rule.
     */
    private void combos2(List list, int[] alpha) {
        int pattern_len;
        int i;
        int[] ind = new int[list.size() + 1];
        pattern_len = list.size();

        if (pattern_len > 0) {

            // initialize index
            while (ind[pattern_len] == 0) {

                // adjust index
                i = 0;
                ind[i]++;

                while (ind[i] > 1) {
                    ind[i] = 0;
                    ind[++i]++;
                }

                if (ind[pattern_len] == 0) {
                    FPPattern pat = new FPPattern(alpha, 0);
                    int min = Integer.MAX_VALUE;

                    for (i = pattern_len - 1; i >= 0; i--) {

                        if (ind[i] == 1) {
                            FPTreeNode nd = (FPTreeNode) list.get(i);
                            pat.addPatternElt(nd.getLabel());

                            if (nd.getCount() < min) {
                                min = nd.getCount();
                            }
                        }
                    }

                    pat.setSupport(min);
                    _patterns.add(pat);
                }
            } // end while
        } // end if
    } // end method combos2


    /**
     * Builds an FPTree from the input items sets and discovers frequent
     * patterns, that are stored in <codE>_patterns.</code>
     *
     * @param prob Serves as the data for the root of the FPTree to be created by
     *             this method.
     */
    private void FPProcess(FPProb prob) {


        FPTreeNode root = new FPTreeNode(-1, null, -1, -1);

        int[] alpha = prob.getAlpha();
        FPSparse tab = prob.getTable();
        int support = prob.getSupport();

        // Build header table
        TreeSet tfeats = new TreeSet(new Feature_Comparator());

        for (int i = 0, n = tab.getNumColumns(); i < n; i++) {
            int coltot = tab.getColumnTots(i);

            if (coltot >= support) {
                tfeats.add(new FeatureTableElement(tab.getLabel(i), coltot, i));
            }
        }

        // trim the list
        ArrayList headers = new ArrayList();
//      boolean b = true;

        for (Iterator it = tfeats.iterator(); it.hasNext();) {
            FeatureTableElement fte = (FeatureTableElement) it.next();

            headers.add(fte);
        }

        int leafcnt = 0;

        // build the FPTree
        for (int i = 0, n = tab.getNumRows(); i < n; i++) {
            FPTreeNode current = root;

            for (int j = 0, m = headers.size(); j < m; j++) {
                FeatureTableElement fte = (FeatureTableElement) headers.get(j);
                int val = tab.getInt(i, fte.getPosition());

                if (val > 0) {
                    FPTreeNode next = current.getChild(fte.getLabel());

                    if (next == null) {

                        if (current.isRoot() || (current.getNumChildren() > 0)) {
                            leafcnt++;
                        }

                        next = new FPTreeNode(fte.getLabel(), current, val, j);

                        fte.addPointer(next);
                        current.addChild(next);
                    } else {
                        next.inc(val);
                    }

                    current = next;
                }
            }
        } // end for


        // If the tree is null, return
        if (leafcnt == 0) {
            return;
        }


        // If the tree has only one path, ouput all pattern combinations union
        // alpha.
        if (leafcnt == 1) {
            ArrayList path = new ArrayList();
            FPTreeNode cpathnode =
                (FPTreeNode) ((Object[]) root.getChildren().getValues())[0];

            while (true) {
                path.add(cpathnode);

                if (cpathnode.getNumChildren() == 0) {
                    break;
                }

                cpathnode =
                    (FPTreeNode) ((Object[]) cpathnode.getChildren().getValues())[0];
            }

            // now we need to get the combinations.
            ArrayList param = new ArrayList(path);

            // int supp =  ((FPTreeNode)path.get(path.size()-1)).getCount();
            combos2(param, alpha);

            return;
        }


        // else, take each feature from header table (in reverse support order)
        // and output that feature|union alpha as a pattern, create a new patterns
        // DB, create new FPProb, and finally call FPProcess.
        for (int a = headers.size() - 1; a >= 0; a--) {

            FeatureTableElement fte = (FeatureTableElement) headers.get(a);
            List ptrs = fte.getPointers();

            // add the entry in the table union alpha
            FPPattern pat = new FPPattern(alpha, fte.getCnt());
            pat.addPatternElt(fte.getLabel());
            _patterns.add(pat);

            FPSparse otab = new FPSparse(headers.size());
            int[] colmap = new int[headers.size()];
            int cind = 1;

            // create new pattern DB
            int cnter = 0;

            for (int i2 = 0, n2 = ptrs.size(); i2 < n2; i2++) {
                FPTreeNode node = (FPTreeNode) ptrs.get(i2);
                List l = this.getPath(node);

                for (int i3 = 0, n3 = l.size(); i3 < n3; i3++) {
                    FPTreeNode node2 = (FPTreeNode) l.get(i3);

                    if (colmap[node2.getPosition()] == 0) {
                        otab.addColumn(((FeatureTableElement) headers.get(node2
                                .getPosition()))
                                .getLabel());
                        colmap[node2.getPosition()] = cind;
                        cind++;
                    }

                    otab.setInt(node.getCount(), cnter, colmap[node2.getPosition()] -
                            1);
                }

                if (l.size() > 0) {
                    cnter++;
                }
            }

            // build a new prob and submit it for processing
            int[] newalpha = new int[alpha.length + 1];
            System.arraycopy(alpha, 0, newalpha, 0, alpha.length);
            newalpha[newalpha.length - 1] = fte.getLabel();

            FPProb newprob = new FPProb(otab, newalpha, support);
            FPProcess(newprob);
        } // for a

    } // FPProcess


    /**
     * Returns a list of FPTreeNode objects, that is the path from <codE>
     * node</code> to the root of the tree. The returned list won't contain the
     * root or the node itself. Thus if <codE>node</code> is a direct child of
     * the root or the root itself - then the returned value is an empty list.
     *
     * @param  node A data node int he the FPTree created by <code>
     *              FPPRocess</codE> method.
     *
     * @return A List of FPTreeNode objects, that is the path from <codE>
     *         node</code> to the root of the tree. The returned list won't
     *         contain the root or the node itself. Thus if <codE>node</code> is
     *         a direct child of the root or the root itself - then the returned
     *         value is an empty list.
     */
    private List getPath(FPTreeNode node) {
        ArrayList list = new ArrayList();

        if (node.isRoot()) {
            return list;
        }

        node = node.getParent();

        while (true) {

            if (node.isRoot()) {
                return list;
            }

            list.add(node);
            node = node.getParent();
        }
    }

    //private D2KModuleLogger myLogger;
    /**
     * Returns the debug property value.
     *
     * @return boolean The debug property value.
     */
    public boolean getDebug() { return _debug; }

    /**
     * Returns the maximum number of attributes allowed in any rule.
     *
     * @return int The maximum number of attributes allowed in any rule.
     */
    public int getMaxRuleSize() { return _maxSize; }

    /**
     * Returns the value of the minumum support property.
     *
     * @return double The value of the minumum support property
     */
    public double getMinimumSupport() { return _support; }


    /**
     * Returns the value of the verbose property.
     *
     * @return boolean The value of the verbose property.
     */
    public boolean getVerbose() { return _verbose; }

    /**
     * Sets the debug property value.
     *
     * @param enable boolean The value for the debug property.
     */
    public void setDebug(boolean enable) { _debug = enable; }

    /**
     * Sets the maximum number of attributes allowed in any rule.
     *
     * @param  maxItemsPerRule int The maximum number of attributes allowed in any rule.
     *
     * @throws PropertyVetoException if <codE>yy</code> is not greater than 1.
     */
    public void setMaxRuleSize(int maxItemsPerRule) throws PropertyVetoException {

        if (maxItemsPerRule < 2) {
            throw new PropertyVetoException(" Maximum Items per Rule cannot be less than 2.",
                    null);
        }

        _maxSize = maxItemsPerRule;
    }

    /**
     * Sets the value of the minimum support property.
     *
     * @param  minSupport double The value of the minimum support property.
     *
     * @throws PropertyVetoException if <code>d</code> is not in the range
     *                               [0,100]
     */
    public void setMinimumSupport(double minSupport) throws PropertyVetoException {

        if (minSupport <= 0.0 || 100.0 < minSupport) {
            throw new PropertyVetoException(" Minimum Support % must be greater than 0 and less than or equal to 100.",
                    null);
        }

        this._support = minSupport;
    }

    /**
     * Sets the value of the verbose property.
     *
     * @param enable boolean The value for the verbose property.
     */
    public void setVerbose(boolean enable) { this._verbose = enable; }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties context) {
        _logger = context.getLogger();
    	
    	_patterns = null;
        
    	try {
    		_debug = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_DEBUG));
    		_verbose = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_VERBOSE));
    		_maxSize = Integer.parseInt(context.getProperty(DATA_PROPERTY_MAX_ITEMS));
    		_support = Double.parseDouble(context.getProperty(DATA_PROPERTY_MIN_SUPPORT));
    	}
    	catch (Exception e) {
    		_logger.log(Level.SEVERE, "Initialize error: ", e);
    		throw new RuntimeException(e);
    	}
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
       
       // ItemSets iss = (ItemSets) context.getDataComponentFromInput(DATA_INPUT_ITEM_SETS);
       
       ItemSetInterface iss = 
          (ItemSetInterface) context.getDataComponentFromInput(DATA_INPUT_ITEM_SETS);
       
       // now run the algorithm
       int[][] ovals = runFP(context, iss);
       if (ovals != null){
          context.pushDataComponentToOutput(DATA_OUTPUT_FREQ_ITEM_SETS, ovals);
       }
       
    }
       
    
    
    protected int[][] runFP(ComponentContext context,
                             ItemSetInterface iss)
        throws ComponentExecutionException, 
               ComponentContextException 
    {
       
       // HashMap sNames    = iss.getUnique();
       //int[] targetIndices = iss.targetIndices;
       // boolean[][] vals  = iss.getItemFlags();
       // String[] atts     = iss.getTargetNames(); // number of attributes
       
       
       String[] nameAry  = iss.getItemsOrderedByFrequency();
       int numExamples   = iss.getNumExamples();
       
        long start = System.currentTimeMillis();

        try {
            
            _cutoff = (int) ((double) numExamples * (_support / 100.0));

            if (((double) numExamples * (_support / 100.0)) > (double) _cutoff) {
                _cutoff++;
            }

            // BUILD INITIAL PROBLEM
            FPProb prob = null;

            FPSparse tab = new FPSparse(nameAry.length);
            FPPattern.clearElementMapping();

            for (int i = 0, n = nameAry.length; i < n; i++) {
                tab.addColumn(i);
                FPPattern.addElementMapping(i, nameAry[i]);
            }

            int rows = numExamples; // was vals.length
            for (int i = 0, n = rows; i < n; i++) {
                int cols = nameAry.length; // was vals[i].length
                for (int j = 0, m = cols; j < m; j++) {

                    if (iss.getItemFlag(i, j) == true) {
                        tab.setInt(1, i, j);
                    }
                }
            }

            int[] flist = new int[0];
            prob = new FPProb(tab, flist, _cutoff);

            _patterns = new ArrayList();

            FPProcess(prob);

            _logger.fine(_patterns.size() + " patterns discovered.");

            long stop = System.currentTimeMillis();
            _logger.fine((stop - start) / 1000 + " seconds");

            int numpatsout = 0;

            gnu.trove.TIntIntHashMap tiihm = new gnu.trove.TIntIntHashMap();
            // HashMap<Integer, Integer> tiihm = new HashMap<Integer, Integer>();
            
            for (int i = 0, n = _patterns.size(); i < n; i++) {
                FPPattern pat = (FPPattern) _patterns.get(i);
                int sz = pat.getSize();
                int val = tiihm.get(sz);
                val++;
                tiihm.put(sz, val);
            }

            int[] keys = tiihm.keys();
            //Integer[] keys = tiihm.keySet().toArray(new Integer[0]);
            

            for (int i = 0, n = keys.length; i < n; i++) {

                if ((keys[i] < 2) || (keys[i] > this.getMaxRuleSize())) {
                    numpatsout += tiihm.get(keys[i]);

                    continue;
                }

                _logger.info("Number of frequent " + keys[i] + "-patterns: " + tiihm.get(keys[i]));
            }

            // CONVERT TO FORMAT USED BY COMPUTE CONFIDENCE COMPONENT

            int totnum = _patterns.size() - numpatsout;

            int[][] ovals = new int[totnum][];
            int ocnt = 0;

            for (int i = 0, n = _patterns.size(); i < n; i++) {
                FPPattern pat = (FPPattern) _patterns.get(i);

                if (
                        (pat.getSize() < 2) ||
                        (pat.getSize() > this.getMaxRuleSize())) {
                    continue;
                }

                int[] fp = new int[pat.getSize() + 1];
                int cnter = 0;

                for (gnu.trove.TIntIterator it = pat.getPattern(); it.hasNext();) {
                    fp[cnter++] = (int) it.next();
                }

                fp[cnter] = pat.getSupport();
                ovals[ocnt++] = fp;
            }

            if (_patterns.size() > 0) {
                return ovals;
            }
            return null;

        } catch (Exception ex) {
        	_logger.log(Level.SEVERE, "Execution error: ", ex);
            throw new ComponentExecutionException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
     */
    public void dispose(ComponentContextProperties context) {
        _patterns = null;
    }

    //~ Inner Classes ***********************************************************

    /**
     * <p>Title: Feature_Comparator</p>
     *
     * <p>Description: compares 2 FeatureTableElement objects</p>
     *
     */
    private class Feature_Comparator implements java.util.Comparator {

        /**
         * The small deviation allowed in double comparisons.
         */

        /**
         * An empty constructor.
         */
        public Feature_Comparator() { }

        /**
         * Intrerface implementation. Returns 1 if according to the natural order
         * of FeatureTableElement <code>o1</code> is greater than <codE>o1</code>,
         * -1 it <code>o2</code> is greater than <code>o1</code> and 0 if they are
         * equal. The natural order of FeatureTableElement is derived from the
         * natural order of FeatureTableElement's count property (sumof data in
         * the original column): If the count properties are different the
         * returned value is <code>o1</codE>.count - <codE>o2</code>.count If the
         * counts are equals the natural order is derived from the natural order
         * of the FeatureTableElement's labels (The feature's index if all
         * features are ordered by frequency in the original table).
         *
         * @param  o1 Object is expected to be a FeatureTableElement object
         * @param  o2 Object is expected to be a FeatureTableElement object
         *
         * @return int 1 if according to the natural order of FeatureTableElement
         *         <code>o1</code> is greater than <codE>o1</code>, -1 it <code>
         *         o2</code> is greater than <code>o1</code> and 0 if they are
         *         equal.
         */
        public int compare(Object o1, Object o2) {
            FeatureTableElement fte1 = (FeatureTableElement) o1;
            FeatureTableElement fte2 = (FeatureTableElement) o2;

            if (fte1.getCnt() == fte2.getCnt()) {

                if (fte1.getLabel() > fte2.getLabel()) {
                    return 1;
                } else if (fte1.getLabel() < fte2.getLabel()) {
                    return -1;
                } else {
                    System.out.println("ERROR ERROR ERROR: We never want to go here ...");

                    return 0;
                }
            } else if (fte1.getCnt() > fte2.getCnt()) {
                return -1;
            } else {
                return 1;
            }
        }

        /**
         * Interface implementation. Compares <code>o</codE> with this comparator.
         *
         * @param  o an object to be compared with this ocmparator.
         *
         * @return true if <codE>o</code> is this comparator, otherwise returns
         *         false.
         */
        public boolean equals(Object o) { return this.equals(o); }
    } // end class Feature_Comparator


} // end class FPGrowth

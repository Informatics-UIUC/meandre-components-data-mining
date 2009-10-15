package org.meandre.components.discovery.ruleassociation.fptree;

//==============
//Java Imports
//==============
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPPattern;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPProb;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPSparse;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPTreeNode;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FeatureTableElement;

@Component(creator="Lily Dong",
           description="<p>Overview: " +
           "This module implements the CLOSET algorithm to generate closed frequent itemsets consisting of " +
           "items that occur in a sufficient number of examples to satisfy the minimum support criteria. " +
           "</p><p>Detailed Description: " +
           "This module takes an <i>FPProb</i> object that has been generated by a <i>Large Item Table Generator</i> " +
           "module and uses the CLOSET algorithm to find " +
           "the combinations of items that satisfy a minimum support criteria. " +
           "An item is an [attribute,value] pair that occurs in the set of examples being mined. " +
           "The user controls the support criteria via the <iSupport</i> property of LargeItemTableGenerator that specifies the " +
           "number of all examples that must contain a given combination of items " +
           "before that combination is included in the generated output. " +
           "Each combination of items that satisfies the <i>Minimum Support %</i> is called " +
           "a <i>Frequent Itemset</i>. The CLOSET algorithm further restricts the itemsets returned " +
           "by returning only the closed sets. (see Han 2000)" +
           "</p><p>References: " +
           "For more information on the CLOSET frequent pattern mining algorithm, see &quot; CLOSET: An Efficient Algorithm for Mining Frequent Closed Itemsets " +
           "&quot;, Jian Pei, Jiawei Han, Runying Mao, 2000. " +
           "</p><p>Data Type Restrictions: " +
           "While this module can operate on attributes of any datatype, in practice it is usually infeasible " +
           "to use it with continuous-valued attributes.   The module considers each [attribute,value] pair that occurs " +
           "in the examples individually when building the frequent itemsets.  Continuous attributes (and categorical " +
           "attributes with a large number of values) are less likely to meet the Minimum Support requirements " +
           "and can result in unacceptably long execution time.",
           name="CLOSET",
           tags="CLOSET",
           baseURL="meandre://seasr.org/components/")

public class CLOSET implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
	   		   		   defaultValue = "false",
	   		   		   name = "verbose")
    public final static String DATA_PROPERTY_VERBOSE = "verbose";
	@ComponentProperty(description = "Print each closed frequent pattern discovered.",
	   		     	   defaultValue = "false",
	   		     	   name = "printPatterns")
    public final static String DATA_PROPERTY_PRINTPATTERNS = "printPatterns";

	@ComponentInput(description="The input parameters encapsulated in an FPProb object." +
			"<br>TYPE: org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.support.FPProb",
             		name= "FPProb")
    public final static String DATA_INPUT = "FPProb";

	@ComponentOutput(description="List of some patterns found." +
            "<br>TYPE: java.util.List",
             		 name="pattern")
    public final static String DATA_OUTPUT_PATTERN = "pattern";
	@ComponentOutput(description="List of FPProb Objects for parallel execution." +
            "<br>TYPE: java.util.List",
             		 name="FPProb")
    public final static String DATA_OUTPUT_FPPROB = "FPProb";

	@ComponentOutput(description="Report of the patterns found." +
            "<br>TYPE: java.lang.String",
             		 name="Pattern_Report")
    public final static String DATA_OUTPUT_REPORT = "Pattern_Report";

  //==============
  // Data Members
  //==============
  private ArrayList<FPPattern> _patterns = null;
  private ArrayList _problems = null;
  private final boolean DEBUG = true;

  private TIntObjectHashMap _closhash = null;
  private TIntIntHashMap _supports = null;

  private FPTreeNode _csetroot = null;

  //============
  // Properties
  //============
  private boolean m_verbose = false;
  private boolean _printPatts = false;

  public void initialize(ComponentContextProperties ccp) {
      _problems = null;
      _patterns = null;
      _closhash = null;
      _supports = null;
      _csetroot = null;
    }

  /**
   * put your documentation comment here
   */
  public void dispose(ComponentContextProperties ccp) {
      _problems = null;
      _patterns = null;
      _closhash = null;
      _supports = null;
      _csetroot = null;
    }

  /**
   * put your documentation comment here
   * @exception java.lang.Exception
   */
  public void execute(ComponentContext cc)
  throws ComponentExecutionException, ComponentContextException {
	  m_verbose = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_VERBOSE));
	  _printPatts = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_PRINTPATTERNS));

      long start = System.currentTimeMillis();
      try {
          Object input = cc.getDataComponentFromInput(DATA_INPUT);
          if (input instanceof StreamDelimiter) return;

        FPProb prob = (FPProb)input;
          _patterns = new ArrayList<FPPattern>();
          _problems = new ArrayList();
          _closhash = new TIntObjectHashMap();

          //sets up the result tree
          getSupports(prob);

          prob.setConditionalSupport(Integer.MAX_VALUE);
          FPProcess(prob);
          if (m_verbose) {
        	  StringBuffer patternRpt = new StringBuffer();
        	  patternRpt.append(_patterns.size() + " patterns discovered.\n\n");
          	  cc.getOutputConsole().println("\n\n" + _patterns.size() + " patterns discovered.");
              long stop = System.currentTimeMillis();
              cc.getOutputConsole().println((stop - start)/1000 + " seconds");

              if (_printPatts) {
                  Collections.sort(_patterns, new FPPatternComparator());

            	  for (FPPattern pattern : _patterns) {
            	      cc.getOutputConsole().print(pattern.getSupport() + ":");
            	      patternRpt.append(pattern.getSupport() + ":");

            	      TIntIterator iter = pattern.getPattern();
            	      while (iter.hasNext()) {
            	          int fte = iter.next();

            	          cc.getOutputConsole().print(" " + FPPattern.getElementLabel(fte));
                          patternRpt.append(" " + FPPattern.getElementLabel(fte));
            	      }

            	      cc.getOutputConsole().println();
                      patternRpt.append("\n");
            	  }

            	  cc.pushDataComponentToOutput(DATA_OUTPUT_REPORT, patternRpt);
              }

              gnu.trove.TIntIntHashMap tiihm = new gnu.trove.TIntIntHashMap();
              for (int i = 0, n = _patterns.size(); i < n; i++) {
                  FPPattern pat = _patterns.get(i);
                  int sz = pat.getSize();
                  int val = tiihm.get(sz);
                  val++;
                  tiihm.put(sz, val);
              }
              int[] keys = tiihm.keys();
              for (int i = 0, n = keys.length; i < n; i++) {
                  System.out.println("Number of frequent " + keys[i] + "-patterns: "
                          + tiihm.get(keys[i]));
              }
          }
          if (_patterns.size() > 0) {
              cc.pushDataComponentToOutput(DATA_OUTPUT_PATTERN, _patterns);
          }
          cc.pushDataComponentToOutput(DATA_OUTPUT_FPPROB, _problems);
      } catch (Exception ex) {
          ex.printStackTrace();
          cc.getOutputConsole().println(ex.getMessage());
          cc.getOutputConsole().println("ERROR: FPTreeGrowth.doit()");
          throw new ComponentExecutionException(ex);
      }
  }

  //=================
  // Private Methods
  //=================

  private void getSupports(FPProb prob){
    TreeSet tfeats = new TreeSet(new Feature_Comparator());
    //initialize the root for the result tree
    _csetroot = new FPTreeNode(-1, null, -1, -1);
    if (_supports == null){
      _supports = new TIntIntHashMap();
    }
    FPSparse tab = prob.getTable();
    int support = prob.getSupport();
    for (int i = 0, n = tab.getNumColumns(); i < n; i++) {
        int coltot = tab.getColumnTots(i);
        int lbl = tab.getLabel(i);
        if (coltot > support){
          tfeats.add(new FeatureTableElement(tab.getLabel(i), coltot, i));
        }
    }
    int i = 0;
    for (Iterator it = tfeats.iterator(); it.hasNext();i++) {
        FeatureTableElement fte = (FeatureTableElement)it.next();
        _supports.put(fte.getLabel(), i);
    }
  }

  private boolean isSubsetOfClosedItemsetOfSameSupport(FPPattern patt){
    int elt = 0;
    int min = Integer.MAX_VALUE;
    for (gnu.trove.TIntIterator it = patt.getPattern(); it.hasNext(); ){
      int tst = it.next();
      int icmp = _supports.get(tst);
      if (icmp < min){
        min = icmp;
        elt = tst;
      }
    }
    TIntObjectHashMap m = (TIntObjectHashMap)_closhash.get(elt);
    if (m == null) {
      return false;
    }
    ArrayList l = (ArrayList)m.get(patt.getSupport());
    if (l == null){
      return false;
    }
    for (int i = 0, n = l.size(); i < n; i++){
     FPTreeNode node = (FPTreeNode)l.get(i);
     if (node.getPosition() < patt.getSize()){
       continue;
     }
     if (node.getPosition() == patt.getSize()){
       if (node.getNumChildren() == 0){
         continue;
       }
       Object[] children = node.getChildren().getValues();
       boolean okay = false;
       for (int j = 0, k = children.length; j < k; j++){
         if (((FPTreeNode)children[j]).getCount() == patt.getSupport()){
           okay = true;
         }
       }
       if (!okay){
         continue;
       }
     }
     //collect values into array
     node = node.getParent();
     TIntHashSet compset = new TIntHashSet();
     compset.add(node.getLabel());
     while (true) {
       if (node.isRoot()) {
          break;
        }
        compset.add(node.getLabel());
        node = node.getParent();
      }
      if (compset.containsAll(patt.getPatternArray())){
        return true;
      }
   }
   return false;
  }

  private void addPatternToLookup(FPPattern patt) {
    int patsupp = patt.getSupport();
    TreeSet tfeats = new TreeSet(new Feature_Comparator());
    for (gnu.trove.TIntIterator it = patt.getPattern(); it.hasNext(); ) {
      int fte = it.next();
      tfeats.add(new FeatureTableElement(fte, _supports.get(fte), 0));
    }

    FPTreeNode current = _csetroot;
    int j = 0;
    for (Iterator it = tfeats.iterator(); it.hasNext(); j++) {
      FeatureTableElement fte = (FeatureTableElement) it.next();
      FPTreeNode next = current.getChild(fte.getLabel());
      if (next == null) {
        next = new FPTreeNode(fte.getLabel(), current, patsupp, j);
        //update double hash
        TIntObjectHashMap suppmap =
            (TIntObjectHashMap) _closhash.get(fte.getLabel());
        ArrayList ndarr = null;
        if (suppmap == null) {
          suppmap = new TIntObjectHashMap();
          ndarr = new ArrayList();
          ndarr.add(next);
          suppmap.put(patsupp, ndarr);
          _closhash.put(fte.getLabel(), suppmap);
        }
        else {
          ndarr = (ArrayList) suppmap.get(patsupp);
          if (ndarr == null) {
            ndarr = new ArrayList();
            ndarr.add(next);
            suppmap.put(patsupp, ndarr);
          }
          else {
            ndarr.add(next);
          }
        }

        current.addChild(next);
      }
      else {
        if (next.getCount() < patsupp) {
          int oldsupp = next.getCount();
          next.setCount(patsupp);

          //add new support value for this node
          TIntObjectHashMap suppmap =
              (TIntObjectHashMap) _closhash.get(fte.getLabel());
          ArrayList ndarr = null;
          ndarr = (ArrayList) suppmap.get(patsupp);
          if (ndarr == null) {
            ndarr = new ArrayList();
            ndarr.add(next);
            suppmap.put(patsupp, ndarr);
          }
          else {
            ndarr.add(next);
          }
          //remove old support reference
          ndarr = (ArrayList) suppmap.get(oldsupp);
          ndarr.remove(next);
        }
      }
      current = next;
    }

  }


//  private void addPatternToLookup(FPPattern patt){
//    for (gnu.trove.TIntIterator it = patt.getPattern(); it.hasNext(); ){
//      int fte = (int)it.next();
//      ArrayList l = (ArrayList)_closhash.get(fte);
//      if (l == null){
//        l = new ArrayList();
//        l.add(patt);
//        _closhash.put(fte,l);
//      } else {
//        l.add(patt);
//      }
//    }
//  }

  private void FPProcess (FPProb prob) {
//    if (DEBUG) {
//      System.out.println(">>>> EXECUTING FPPpocess ...");
//      System.out.println("Columns: " + prob.getTable().getNumColumns() +
//                         " Rows: " + prob.getTable().getNumRows());
//      System.out.print("Alpha: ");
//      for (int i = 0, n = prob.getAlpha().length; i < n; i++) {
//        System.out.print(FPPattern.getElementLabel(prob.getAlpha()[i]));
//      }
//      System.out.println();
//      System.out.println("Support: " + prob.getSupport());
//      System.out.println();
//    }
      FPTreeNode root = new FPTreeNode(-1, null, -1, -1);
      int[] alpha = prob.getAlpha();
      FPSparse tab = prob.getTable();
      int support = prob.getSupport();
      int maxSupport = prob.getMaxSupport();
      //Build header table
      TreeSet tfeats = new TreeSet(new Feature_Comparator());
      ArrayList tfeats2 = new ArrayList();
     //pattern for Optimization 2 (see CLOSET paper)
      //TIntHashSet everyset = null;
      int numrows = tab.getNumRows();
      FPPattern everytrans = new FPPattern(alpha, prob.getConditionalSupport());;
      for (int i = 0, n = tab.getNumColumns(); i < n; i++) {
          int coltot = tab.getColumnTots(i);
          //      if (DEBUG){
          //        System.out.println("Column " + FPPattern.getElementLabel(tab.getLabel(i)) + " has " + coltot + " sum of entries.");
          //      }
          if ((coltot >= support) && (coltot <= maxSupport)) {
//            int[] colinds = tab.getColumnIndices(i);
//            for (int q = 0, p = colinds.length; q < p; q++){
//              if (tab.getInt(colinds[q], i) == 0){
//                System.out.println("not right ....");
//              }
//            }
            if (coltot == prob.getConditionalSupport()){
            //if (tab.getColumnIndices(i).length == numrows){
              //System.out.println("Saturated attribute .... ");
              //Optimization 2
              everytrans.addPatternElt(tab.getLabel(i));
              tfeats2.add(new FeatureTableElement(tab.getLabel(i), coltot, i));
              //everyset.add(i);
            } else {
              tfeats.add(new FeatureTableElement(tab.getLabel(i), coltot, i));
            }
          }
      }
      //Optimization 2
      if (everytrans.getSize() > 0) {
//        if (tfeats2.size() > 0) {
//          int min = Integer.MAX_VALUE;
//          for (int x = 0, y = tfeats2.size(); x < y; x++){
//            FeatureTableElement fte = (FeatureTableElement)tfeats2.get(x);
//            int tst = fte.getCnt();
//            if (tst < min){
//              min = tst;
//            }
//          }
//          if (min < everytrans.getSupport()){
//            everytrans.setSupport(min);
//          }
//        }
        boolean go = true;
        if ((!isSubsetOfClosedItemsetOfSameSupport(everytrans))){
            if (everytrans.getSupport() >= support){
                _patterns.add(everytrans);
                addPatternToLookup(everytrans);
                go = false;
                //tfeats.removeAll(tfeats2);
            }
        } else {
          if ((tfeats2.size() > 0) && go) {
           tfeats.addAll(tfeats2);
          }
        }
      }
      //    if (DEBUG){
      //      System.out.println("Feature tree set has " + tfeats.size() + " entries.");
      //    }
      //trim the list
      ArrayList headers = new ArrayList();
      boolean b = true;
      for (Iterator it = tfeats.iterator(); it.hasNext();) {
          FeatureTableElement fte = (FeatureTableElement)it.next();
//                if (DEBUG){
//                  System.out.println(">> Header: " + FPPattern.getElementLabel(fte.getLabel()) + " :" + fte.getCnt());
//                }
          headers.add(fte);
      }
//          if (DEBUG){
//            System.out.println("Header table built with " + headers.size() + " entries.");
//          }

      int leafcnt = 0;
      //    int nodecnt = 0;

      //build the FPTree
      for (int i = 0, n = tab.getNumRows(); i < n; i++) {
          FPTreeNode current = root;
          for (int j = 0, m = headers.size(); j < m; j++) {
              FeatureTableElement fte = (FeatureTableElement)headers.get(j);
              int val = tab.getInt(i, fte.getPosition());
              if (val > 0){
                  FPTreeNode next = current.getChild(fte.getLabel());
                  if (next == null) {
                      if (current.isRoot() || (current.getNumChildren() > 0)) {
                          leafcnt++;
                      }
                      next = new FPTreeNode(fte.getLabel(), current, val, j);
                      //            nodecnt++;
                      fte.addPointer(next);
                      current.addChild(next);
                  }
                  else {
                      next.inc(val);
                  }
                  current = next;
              }
          }
//          current.setHoldsDocs(true);
      }
      //    if (DEBUG){
      //      System.out.println("FPTree built with " + nodecnt + " nodes.");
      //      System.out.println("Root has " + root.getNumChildren() + " children.");
      //    }
      //    if (DEBUG) {
      //      int[] keys = root.getChildren().keys();
      //      for (int i = 0, n = keys.length; i < n; i++){
      //        FPTreeNode node = root.getChild(keys[i]);
      //        while ((node != null)){
      //          System.out.print(FPPattern.getElementLabel(node.getLabel()) + ":" + node.getCount() + " ");
      //          //gnu.trove.TIntObjectIterator it = node.getChildren().iterator();
      //          //it.advance();
      //          //node = (FPTreeNode)it.value();
      //          if (node.getChildren().size() > 0){
      //            node = node.getChild(node.getChildren().keys()[0]);
      //          } else {
      //            //System.out.print(FPPattern.getElementLabel(node.getLabel()) + ":" + node.getCount() + " ");
      //            node = null;
      //          }
      //        }
      //        System.out.println();
      //      }
      //    }

      //find min support

//      if (this.getDiscoverMinSupport()){
//        if (!_suppset){
//          support = discoverMinSupport(headers);
//          System.out.println("Min Support discovered is: " + support);
//          if (this.getDiscoverMaxSupport()){
//            maxSupport = discoverMaxSupport(headers, support);
//            System.out.println("Max Support discovered is: " + maxSupport);
//          }
//          _suppset = true;
//          prob.setSupport(support);
//          prob.setMaxSupport(maxSupport);
//          _patterns = new ArrayList();
//          _problems = new ArrayList();
//          _closhash = new TIntObjectHashMap();
//          getSupports(prob);
//          FPProcess(prob);
//          return;
//        }
//      }


      //Optimization 3

      //========================================================================
      // BEGIN FPGROWTH ========================================================
      //========================================================================
      /**
       * If the tree is null, return
       */
      if (leafcnt == 0) {
          return;
      }
      /**
       * If the tree has only one path, extract closed patterns
       * Optimization 3
       */
//      if (leafcnt == 1) {
//          ArrayList path = new ArrayList();
//          FPTreeNode cpathnode = (FPTreeNode)((Object[])root.getChildren().getValues())[0];
//          while (true) {
//              path.add(cpathnode);
//              if (cpathnode.getNumChildren() == 0) {
//                  break;
//              }
//              cpathnode = (FPTreeNode)((Object[])cpathnode.getChildren().getValues())[0];
//          }
//
//          //System.out.println("found path size : " + path.size() + " alpha size: " + alpha.length);
//          for (int i = (path.size() - 1); i >= 0; i--){
//            FPTreeNode nd = (FPTreeNode)path.get(i);
//            if ((nd.getNumChildren() == 0) ||
//                (((FPTreeNode)((Object[])nd.getChildren().getValues())[0]).getCount() < nd.getCount())){
//              if (nd.getCount() >= support){
//                //System.out.println("Building pattern for final test ...");
//                FPPattern newpatt = new FPPattern(alpha, nd.getCount());
//                for (int j = 0, m = i; j <= m; j++){
//                  newpatt.addPatternElt(((FPTreeNode)path.get(j)).getLabel());
//                }
//                if (!isSubsetOfClosedItemsetOfSameSupport(newpatt)){
//                  //System.out.println("Adding pattern ...");
//                  _patterns.add(newpatt);
//                  //System.out.println("Num patterns so far: " + _patterns.size());
//                }
//              }
//            }
//          }
//
//
//
//          //      if (DEBUG){
//          //        System.out.println("\n\nOuput path info for one path -------");
//          //        for (int i = 0, n = path.size(); i < n; i++){
//          //          System.out.print( FPPattern.getElementLabel(((FPTreeNode)path.get(i)).getLabel()) + ":" + ((FPTreeNode)path.get(i)).getCount() + " ");
//          //        }
//          //        System.out.println("\n\n");
//          //      }
//          //now we need to get the combinations.
//
//          //            ArrayList param = new ArrayList(path);
//          //            int supp = ((FPTreeNode)path.get(path.size() - 1)).getCount();
//          //            combos2(param, supp, alpha);
//          return;
//      }



      ArrayList removes = new ArrayList();
      for (int a = headers.size() - 1; a >= 0; a--) {
        FeatureTableElement fte = (FeatureTableElement)headers.get(a);
        List ptrs = fte.getPointers();
        if (ptrs.size() == 1){
          FPTreeNode nd = (FPTreeNode)ptrs.get(0);
          if ((nd.getNumChildren() == 0) || (nd.getNumChildren() > 1) ||
              (((FPTreeNode)(nd.getChildren().getValues())[0]).getCount() < nd.getCount())){
            boolean okay = true;
            FPTreeNode nnd = nd;
            FPPattern newpatt = new FPPattern(alpha, nd.getCount()/*0*/);
            while (true){
              nnd = nnd.getParent();
              if (nnd.getNumChildren() > 1){
                okay = false;
                break;
              }
              if (nnd.isRoot()){
                break;
              }
              newpatt.addPatternElt(nnd.getLabel());
           }
           if (okay){
             newpatt.addPatternElt(nd.getLabel());
             if (nd.getCount() >= support){
               if (!isSubsetOfClosedItemsetOfSameSupport(newpatt)){
                 _patterns.add(newpatt);
                 this.addPatternToLookup(newpatt);
                 //System.out.println("Added pattern .... ");
                 //newpatt.setSupport(nd.getCount());
                 removes.add(fte);
               }
             }
           }
         }
       }
      }
//      if (removes.size() > 0){
//        headers.removeAll(removes);
//      }


      /**
       * else, take each feature from header table (in reverse support order) and
       * output that feature|union alpha as a pattern, create a new patterns DB,
       * create new FPProb, and finally call FPProcess.
       */
      for (int a = headers.size() - 1; a >= 0; a--) {
        //System.out.print(a + " ");

          FeatureTableElement fte = (FeatureTableElement)headers.get(a);
          if (removes.contains(fte)){
            continue;
          }
          List ptrs = fte.getPointers();
          //add the entry in the table union alpha
          FPPattern pat = new FPPattern(alpha, fte.getCnt());
          pat.addPatternElt(fte.getLabel());

          if (isSubsetOfClosedItemsetOfSameSupport(pat)){
            continue;
          }

          //_patterns.add(pat);

//          if (ptrs.size() == 1) {
//              FPTreeNode node = (FPTreeNode)ptrs.get(0);
//              if (node.getParent().isRoot()) {
//                  continue;
//              }
//              List l = this.getPath(node);
//              //now we need to get the combinations.
//              //ArrayList alpha2 = new ArrayList(alpha);
//              //alpha2.add(node.getLabel());
//              //ArrayList combosLst = combos(param);
//              int[] newalpha = new int[alpha.length + 1];
//              System.arraycopy(alpha, 0, newalpha, 0, alpha.length);
//              newalpha[newalpha.length - 1] = node.getLabel();
//              int supp = ((FPTreeNode)l.get(l.size() - 1)).getCount();
//              combos2(l, supp, newalpha);
//              continue;
//          }

          FPSparse otab = new FPSparse(headers.size());
          int[] colmap = new int[headers.size()];
          int cind = 1;
          //create new pattern DB
          int cnter = 0;
          for (int i2 = 0, n2 = ptrs.size(); i2 < n2; i2++) {
              //for (Iterator it2 = fte.getPointersIter(); it2.hasNext();){
              FPTreeNode node = (FPTreeNode)ptrs.get(i2);
              List l = this.getPath(node);
              for (int i3 = 0, n3 = l.size(); i3 < n3; i3++) {
                  //for (Iterator it3 = l.iterator(); it3.hasNext();){
                  FPTreeNode node2 = (FPTreeNode)l.get(i3);
                  if (colmap[node2.getPosition()] == 0) {
                      otab.addColumn(((FeatureTableElement)headers.get(node2.getPosition())).getLabel());
                      colmap[node2.getPosition()] = cind;
                      cind++;
                  }
                  otab.setInt(node.getCount(), cnter, colmap[node2.getPosition()]
                          - 1);
              }
              if (l.size() > 0)
                  cnter++;
          }
          //build a new prob and submit it for processing
          //ArrayList newalpha = new ArrayList(alpha);
          //newalpha.add(fte.getLabel());
          int[] newalpha = new int[alpha.length + 1];
          System.arraycopy(alpha, 0, newalpha, 0, alpha.length);
          newalpha[newalpha.length - 1] = fte.getLabel();
          //      if (DEBUG){
          //        System.out.println("Calling FPProcess on conditional table for: " + fte.getLabel());
          //      }
          FPProb newprob = new FPProb(otab, newalpha, support);
          newprob.setConditionalSupport(fte.getCnt());
          FPProcess(newprob);
      }
      //System.out.println();
  }

//  private int discoverMinSupport(ArrayList headers){
//    for (int a = headers.size() - 1; a >= 0; a--) {
//      FeatureTableElement fte = (FeatureTableElement) headers.get(a);
//      List ptrs = fte.getPointers();
//      for (int i = 0, n = ptrs.size(); i < n; i++){
//        FPTreeNode node = (FPTreeNode)ptrs.get(i);
//        if (node.getParent().isRoot()){
//          return fte.getCnt();
//        }
//      }
//      for (int i = 0, n = ptrs.size(); i < n; i++){
//        FPTreeNode node = (FPTreeNode)ptrs.get(i);
//        node.getParent().setHoldsDocs(true);
//      }
//    }
//    return 0;
//  }
//
//  private int discoverMaxSupport(ArrayList headers, int supp){
//    int lastsupp = Integer.MAX_VALUE;
//    for (int a = 0, b = headers.size(); a < b; a++) {
//      FeatureTableElement fte = (FeatureTableElement) headers.get(a);
//      if (fte.getCnt() == supp){
//        return lastsupp;
//      }
//      List ptrs = fte.getPointers();
//      for (int i = 0, n = ptrs.size(); i < n; i++){
//        FPTreeNode node = (FPTreeNode)ptrs.get(i);
//        if (node.getHoldsDocs()){
//          return lastsupp;
//        }
//      }
//      if (lastsupp != fte.getCnt()){
//        lastsupp = fte.getCnt();
//      }
//    }
//    return lastsupp;
//  }



  /**
   * put your documentation comment here
   * @param list
   * @param support
   * @param alpha
   */
//  private void combos2 (List list, int support, int[] alpha) {
//      int pattern_len, i;
//      int[] ind = new int[list.size() + 1];
//      pattern_len = list.size();
//      if (pattern_len > 0) {
//          //initialize index
//          while (ind[pattern_len] == 0) {
//              //adjust index
//              i = 0;
//              ind[i]++;
//              while (ind[i] > 1) {
//                  ind[i] = 0;
//                  ind[++i]++;
//              }
//              if (ind[pattern_len] == 0) {
//                  FPPattern pat = new FPPattern(alpha, support);
//                  for (i = pattern_len - 1; i >= 0; i--) {
//                      if (ind[i] == 1) {
//                          pat.addPatternElt(((FPTreeNode)list.get(i)).getLabel());
//                      }
//                  }
//                  _patterns.add(pat);
//              }
//          }
//      }
//  }

  /**
   * put your documentation comment here
   * @param node
   * @return
   */
  private List getPath (FPTreeNode node) {
      ArrayList list = new ArrayList();
      if (node.isRoot()) {
          return  list;
      }
      node = node.getParent();
      while (true) {
          if (node.isRoot()) {
              return  list;
          }
          list.add(node);
          node = node.getParent();
      }
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
      public Feature_Comparator () {
      }

      //======================
      //Interface: Comparator
      //======================
      public int compare (Object o1, Object o2) {
          FeatureTableElement fte1 = (FeatureTableElement)o1;
          FeatureTableElement fte2 = (FeatureTableElement)o2;
          if (fte1.getCnt() == fte2.getCnt()) {
              if (fte1.getLabel() > fte2.getLabel()) {
                  return  1;
              }
              else if (fte1.getLabel() < fte2.getLabel()) {
                  return  -1;
              }
              else {
                  System.out.println("ERROR ERROR ERROR: We never want to go here ...");
                  return  0;
              }
          }
          else if (fte1.getCnt() > fte2.getCnt()) {
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
      @Override
    public boolean equals (Object o) {
          return  this.equals(o);
      }
  }

  //compare two patterns based on support
  //add on May 28, 2009
  class FPPatternComparator implements Comparator<FPPattern>{
	  public int compare(FPPattern p1, FPPattern p2) {
		  int s1 = p1.getSupport(),
		  	  s2 = p2.getSupport();
		    return new Integer(s2).compareTo(new Integer(s1));
		  }
  }
}





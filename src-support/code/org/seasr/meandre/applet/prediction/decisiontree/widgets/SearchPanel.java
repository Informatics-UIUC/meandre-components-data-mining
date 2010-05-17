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

package org.seasr.meandre.applet.prediction.decisiontree.widgets;

import org.seasr.meandre.applet.Constrain;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.Viewport;
import org.seasr.meandre.support.components.prediction.decisiontree.NominalViewableDTModel;


import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Panel to input search parameters
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class SearchPanel extends JPanel implements ActionListener {

    //~ Static fields/initializers **********************************************

    /** constant for > */
    static private final String GREATER_THAN = ">";

    /** constant for < */
    static private final String LESS_THAN = "<";

    /** constant for >= */
    static private final String GREATER_THAN_EQUAL_TO = ">=";

    /** constant for <= */
    static private final String LESS_THAN_EQUAL_TO = "<=";

    /** constant for != */
    static private final String NOT_EQUAL_TO = "!=";

    /** constant for == */
    static private final String EQUAL_TO = "==";

    /** constant for && */
    static private final String AND = "&&";

    /** constant for || */
    static private final String OR = "||";

    //~ Instance fields *********************************************************

    /** clear button */
    private JButton clear;

    /** close button */
    private JButton close;

    /** list of the conditions that the filter must satisfy */
    private JList conditionlist;

    /** the frame */
    private JFrame frame;

    /** list model */
    private DefaultListModel listmodel;

    /** decision tree model */
    private NominalViewableDTModel model;

    /** next button */
    private JButton next;

    /** viewport for a node. */
    private Viewport nodeindex;

    /** combo box holding operators for expression building */
    private JComboBox operators;

    /** percent add button */
    private JButton percentadd;

    /** combo box for percent operators. */
    private JComboBox percentoperators;

    /** Percent. */
    private JComboBox percentoutputs;

    /** text field for percent value */
    private JTextField percentvalue;

    /** population add button */
    private JButton populationadd;

    /** population operators combo box */
    private JComboBox populationoperators;

    /** population outputs combo box */
    private JComboBox populationoutputs;

    /** population value combo box */
    private JTextField populationvalue;

    /** previous button */
    private JButton previous;

    /** purity add button */
    private JButton purityadd;

    /** purity operators combo box */
    private JComboBox purityoperators;

    /** purity value text field */
    private JTextField purityvalue;

    /** remove button */
    private JButton remove;

    /** replace button */
    private JButton replace;

    /** result label */
    private JLabel resultlabel;

    /** true if an input is scalar */
    private boolean scalar;

    /** search button */
    private JButton search;

    /** current index into searchList */
    private int searchindex;

    /** list of viewports (nodes) */
    private ArrayList searchlist;

    /** panel  */
    private JPanel searchpanel;

    /** split add button */
    private JButton splitadd;

    /** combo box for split inputs */
    private JComboBox splitinputs;

    /** combo box for split operators */
    private JComboBox splitoperators;

    /** combo box for split values */
    private JComboBox splitvaluebox;

    /** text field to enter split value */
    private JTextField splitvaluefield;

    /** the treescrollpane holds the main view */
    private TreeScrollPane treescrollpane;

    //~ Constructors ************************************************************

    /**
     * Creates a new SearchPanel object.
     *
     * @param scrollpane holds the main view of the decision tree
     * @param parent     frame that holds this component
     */
    public SearchPanel(TreeScrollPane scrollpane, JFrame parent) {
        treescrollpane = scrollpane;
        frame = parent;

        model = (NominalViewableDTModel) treescrollpane.getViewableModel();

        String[] outputs = model.getUniqueOutputValues();
        String[] inputs = model.getInputs();

        searchlist = new ArrayList();

        // Population search
        populationoutputs = new JComboBox(outputs);

        populationoperators = new JComboBox();
        populationoperators.addItem(GREATER_THAN);
        populationoperators.addItem(LESS_THAN);
        populationoperators.addItem(GREATER_THAN_EQUAL_TO);
        populationoperators.addItem(LESS_THAN_EQUAL_TO);
        populationoperators.addItem(EQUAL_TO);
        populationoperators.addItem(NOT_EQUAL_TO);

        populationvalue = new JTextField(5);

        populationadd = new JButton("Add");
        populationadd.addActionListener(this);

        // Percent search
        percentoutputs = new JComboBox(outputs);

        percentoperators = new JComboBox();
        percentoperators.addItem(GREATER_THAN);
        percentoperators.addItem(LESS_THAN);
        percentoperators.addItem(GREATER_THAN_EQUAL_TO);
        percentoperators.addItem(LESS_THAN_EQUAL_TO);
        percentoperators.addItem(EQUAL_TO);
        percentoperators.addItem(NOT_EQUAL_TO);

        percentvalue = new JTextField(5);

        percentadd = new JButton("Add");
        percentadd.addActionListener(this);

        // Purity search
        purityoperators = new JComboBox();
        purityoperators.addItem(GREATER_THAN);
        purityoperators.addItem(LESS_THAN);
        purityoperators.addItem(GREATER_THAN_EQUAL_TO);
        purityoperators.addItem(LESS_THAN_EQUAL_TO);
        purityoperators.addItem(EQUAL_TO);
        purityoperators.addItem(NOT_EQUAL_TO);

        purityvalue = new JTextField(5);

        purityadd = new JButton("Add");
        purityadd.addActionListener(this);

        // Split attributes
        splitinputs = new JComboBox(inputs);
        splitinputs.addActionListener(this);

        splitoperators = new JComboBox();

        int index = splitinputs.getSelectedIndex();

        if (model.scalarInput(index)) {
            scalar = true;

            splitoperators.addItem(GREATER_THAN);
            splitoperators.addItem(LESS_THAN);
            splitoperators.addItem(GREATER_THAN_EQUAL_TO);
            splitoperators.addItem(LESS_THAN_EQUAL_TO);
            splitoperators.addItem(EQUAL_TO);
            splitoperators.addItem(NOT_EQUAL_TO);

            splitvaluefield = new JTextField(5);
        } else {
            scalar = false;

            splitoperators.addItem(EQUAL_TO);
            splitoperators.addItem(NOT_EQUAL_TO);

            splitvaluebox = new JComboBox(model.getUniqueInputValues(index));
        }

        splitadd = new JButton("Add");
        splitadd.addActionListener(this);

        // Search panel
        searchpanel = new JPanel();
        searchpanel.setLayout(new GridBagLayout());

        // Population
        Constrain.setConstraints(searchpanel, new JLabel("Population:"), 0, 0,
                                 1,
                                 1, GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, populationoutputs, 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, populationoperators, 2, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, populationvalue, 3, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, populationadd, 4, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));

        // Percent
        Constrain.setConstraints(searchpanel, new JLabel("Percent:"), 0, 1, 1,
                                 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, percentoutputs, 1, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, percentoperators, 2, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, percentvalue, 3, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, percentadd, 4, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));

        // Purity
        Constrain.setConstraints(searchpanel, new JLabel("Purity:"), 0, 2, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, purityoperators, 2, 2, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, purityvalue, 3, 2, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, purityadd, 4, 2, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));

        // Split
        Constrain.setConstraints(searchpanel, new JLabel("Split:"), 0, 3, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, splitinputs, 1, 3, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(searchpanel, splitoperators, 2, 3, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));

        if (scalar) {
            Constrain.setConstraints(searchpanel, splitvaluefield, 3, 3, 1, 1,
                                     GridBagConstraints.HORIZONTAL,
                                     GridBagConstraints.NORTHWEST, 0, 0,
                                     new Insets(5, 5, 5, 5));
        } else {
            Constrain.setConstraints(searchpanel, splitvaluebox, 3, 3, 1, 1,
                                     GridBagConstraints.HORIZONTAL,
                                     GridBagConstraints.NORTHWEST, 0, 0,
                                     new Insets(5, 5, 5, 5));
        }

        Constrain.setConstraints(searchpanel, splitadd, 4, 3, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));

        JScrollPane searchscroll = new JScrollPane(searchpanel);
        searchscroll.setMinimumSize(searchscroll.getPreferredSize());

        // Conditions
        conditionlist = new JList();
        listmodel = new DefaultListModel();

        JLabel conditionlabel = new JLabel("Current Conditions");
        conditionlist.setModel(listmodel);

        JScrollPane conditionscroll = new JScrollPane(conditionlist);
        JViewport viewport = new JViewport();
        viewport.setView(conditionlabel);
        conditionscroll.setColumnHeader(viewport);

        // Conditions panel
        JPanel conditionpanel = new JPanel();
        conditionpanel.setLayout(new BorderLayout());
        conditionpanel.add(conditionscroll, BorderLayout.CENTER);

        remove = new JButton("Remove");
        remove.addActionListener(this);

        operators = new JComboBox();
        operators.addItem(AND);
        operators.addItem(OR);

        replace = new JButton("Replace");
        replace.addActionListener(this);

        JPanel conditionbuttons = new JPanel();
        conditionbuttons.setLayout(new GridBagLayout());

        Constrain.setConstraints(conditionbuttons, remove, 0, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(conditionbuttons, new JPanel(), 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(conditionbuttons, operators, 2, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(conditionbuttons, replace, 3, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));

        conditionpanel.add(conditionbuttons, BorderLayout.SOUTH);

        JSplitPane splitpane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchscroll,
                               conditionpanel);

        // Button panel
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new GridBagLayout());

        close = new JButton("Close");
        close.addActionListener(this);

        search = new JButton("Search");
        search.addActionListener(this);

        clear = new JButton("Clear");
        clear.addActionListener(this);

        Constrain.setConstraints(buttonpanel, new JPanel(), 0, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(buttonpanel, search, 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(buttonpanel, clear, 2, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(buttonpanel, close, 3, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));

        // Result panel
        JPanel resultpanel = new JPanel();
        resultpanel.setLayout(new GridBagLayout());

        resultlabel = new JLabel("Search Results: ");

        next = new JButton("Next");
        next.addActionListener(this);

        previous = new JButton("Previous");
        previous.addActionListener(this);

        Constrain.setConstraints(resultpanel, resultlabel, 3, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.WEST, 1, 1,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(resultpanel, next, 4, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(resultpanel, previous, 5, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));

        setLayout(new GridBagLayout());
        Constrain.setConstraints(this, splitpane, 0, 0, 1, 1,
                                 GridBagConstraints.BOTH,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(this, buttonpanel, 0, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(this, new JSeparator(SwingConstants.HORIZONTAL),
                                 0, 2, 1, 1, GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
        Constrain.setConstraints(this, resultpanel, 0, 3, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(2, 2, 2, 2));
    }

    //~ Methods *****************************************************************

    /**
     * Recursively search tree.
     *
     * @param condition the condition
     * @param node      viewport that represents a node
     */
    void searchTree(Condition condition, Viewport node) {
        boolean evaluation;

        if (node.isLeaf()) {
            evaluation = condition.evaluate(condition, node);
            node.setSearch(evaluation);

            if (evaluation) {
                searchlist.add(node);
            }

            return;
        }

        for (int index = 0; index < node.getNumChildren(); index++) {
            Viewport child = node.getChild(index);
            searchTree(condition, child);
        }

        evaluation = condition.evaluate(condition, node);
        node.setSearch(evaluation);

        if (evaluation) {
            searchlist.add(node);
        }
    }

    /**
     * Update viewport in tree scroll pane to show node.
     *
     * @param node viewport that represents a node
     */
    void updateViewport(Viewport node) {
        double xnode = node.x;
        double ynode = node.y;

        double nodeheight = node.getViewHeight();
        double nodewidth = node.getViewWidth();

        double scale = treescrollpane.getScale();

        JViewport viewport = treescrollpane.viewport;

        Dimension dimension = viewport.getExtentSize();
        double viewportwidth = dimension.getWidth();
        double viewportheight = dimension.getHeight();

        dimension = viewport.getViewSize();

        double viewwidth = dimension.getWidth();
        double viewheight = dimension.getHeight();

        double xviewport = scale * xnode - viewportwidth / 2;
        double yviewport = scale * (ynode + nodeheight / 2) -
                           viewportheight / 2;

        if (xviewport < 0) {
            xviewport = 0;
        }

        if (xviewport > viewwidth - viewportwidth) {
            xviewport = viewwidth - viewportwidth;
        }

        if (yviewport < 0) {
            yviewport = 0;
        }

        if (yviewport > viewheight - viewportheight) {
            yviewport = viewheight - viewportheight;
        }

        treescrollpane.scroll((int) xviewport, (int) yviewport);
    } // end method updateViewport


    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == populationadd) {
            String attribute = (String) populationoutputs.getSelectedItem();
            String operator = (String) populationoperators.getSelectedItem();
            String svalue = populationvalue.getText();
            double dvalue = 0;

            try {
                dvalue = Double.parseDouble(svalue);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            PopulationCondition condition =
                    new PopulationCondition(attribute, operator, dvalue);
            listmodel.addElement(condition);

            populationvalue.setText("");
        } else if (source == percentadd) {
            String attribute = (String) percentoutputs.getSelectedItem();
            String operator = (String) percentoperators.getSelectedItem();
            String svalue = percentvalue.getText();
            double dvalue = 0;

            try {
                dvalue = Double.parseDouble(svalue);
            } catch (Exception exception) {
            }

            PercentCondition condition =
                    new PercentCondition(attribute, operator, dvalue);
            listmodel.addElement(condition);

            percentvalue.setText("");
        } else if (source == purityadd) {
            String operator = (String) purityoperators.getSelectedItem();
            String svalue = purityvalue.getText();
            double dvalue = 0;

            try {
                dvalue = Double.parseDouble(svalue);
            } catch (Exception exception) {
            }

            PurityCondition condition = new PurityCondition(operator, dvalue);
            listmodel.addElement(condition);

            purityvalue.setText("");
        } else if (source == splitinputs) {
            int index = splitinputs.getSelectedIndex();

            if (model.scalarInput(index) && !scalar) {
                scalar = true;

                splitoperators.removeAllItems();
                splitoperators.addItem(GREATER_THAN);
                splitoperators.addItem(LESS_THAN);
                splitoperators.addItem(GREATER_THAN_EQUAL_TO);
                splitoperators.addItem(LESS_THAN_EQUAL_TO);
                splitoperators.addItem(EQUAL_TO);
                splitoperators.addItem(NOT_EQUAL_TO);

                splitvaluefield = new JTextField(5);

                searchpanel.remove(splitvaluebox);
                Constrain.setConstraints(searchpanel, splitvaluefield, 3, 3, 1,
                                         1,
                                         GridBagConstraints.HORIZONTAL,
                                         GridBagConstraints.NORTHWEST, 0, 0,
                                         new Insets(5, 5, 5, 5));

                revalidate();
                repaint();
            } else if (!model.scalarInput(index) && scalar) {
                scalar = false;

                splitoperators.removeAllItems();
                splitoperators.addItem(EQUAL_TO);
                splitoperators.addItem(NOT_EQUAL_TO);

                splitvaluebox = new JComboBox(model.getUniqueInputValues(index));

                searchpanel.remove(splitvaluefield);
                Constrain.setConstraints(searchpanel, splitvaluebox, 3, 3, 1, 1,
                                         GridBagConstraints.HORIZONTAL,
                                         GridBagConstraints.NORTHWEST, 0, 0,
                                         new Insets(5, 5, 5, 5));

                revalidate();
                repaint();
            }
        } else if (source == splitadd) {
            String attribute = (String) splitinputs.getSelectedItem();
            String operator = (String) splitoperators.getSelectedItem();

            if (scalar) {

                try {
                    String svalue = splitvaluefield.getText();
                    double dvalue = Double.parseDouble(svalue);

                    SplitCondition condition =
                            new SplitCondition(attribute, operator, dvalue);
                    listmodel.addElement(condition);

                    splitvaluefield.setText("");
                } catch (Exception exception) {
                }
            } else {

                try {
                    String svalue = (String) splitvaluebox.getSelectedItem();

                    SplitCondition condition =
                            new SplitCondition(attribute, operator, svalue);
                    listmodel.addElement(condition);
                } catch (Exception exception) {
                }
            }
        } else if (source == remove) {
            int selected = conditionlist.getSelectedIndex();

            if (selected != -1) {
                listmodel.remove(selected);
            }
        } else if (source == replace) {
            String operator = (String) operators.getSelectedItem();

            int[] indices = conditionlist.getSelectedIndices();

            if (indices.length < 2) {
                return;
            }

            Condition first = (Condition) listmodel.getElementAt(indices[0]);
            Condition second = (Condition) listmodel.getElementAt(indices[1]);
            Condition three = new CompoundCondition(first, second, operator);

            listmodel.removeElementAt(indices[0]);
            listmodel.removeElementAt(indices[1] - 1);
            listmodel.add(0, three);
        } else if (source == close) {
            frame.setVisible(false);
            listmodel.removeAllElements();
        } else if (source == search) {
            searchlist.clear();
            searchindex = -1;

            Object[] conditions = listmodel.toArray();

            if (conditions.length == 1) {
                Condition condition = (Condition) conditions[0];
                searchTree(condition, treescrollpane.getViewRoot());

                resultlabel.setText("Search Results: " + searchlist.size() +
                                    " nodes");
                repaint();
                treescrollpane.repaint();
            }
        } else if (source == clear) {
            searchlist.clear();
            searchindex = -1;

            if (nodeindex != null) {
                nodeindex.setSearchBackground(false);
            }

            resultlabel.setText("Search Results: ");

            repaint();
            treescrollpane.clearSearch();
            treescrollpane.repaint();
        } else if (source == next) {

            if (searchlist.size() > 0) {
                searchindex++;

                if (searchindex == searchlist.size()) {
                    searchindex = 0;
                }

                if (nodeindex != null) {
                    nodeindex.setSearchBackground(false);
                }

                Viewport node = (Viewport) searchlist.get(searchindex);

                nodeindex = node;
                nodeindex.setSearchBackground(true);

                updateViewport(node);
            }
        } else if (source == previous) {

            if (searchlist.size() > 0) {
                searchindex--;

                if (searchindex < 0) {
                    searchindex = searchlist.size() - 1;
                }

                if (nodeindex != null) {
                    nodeindex.setSearchBackground(false);
                }

                Viewport node = (Viewport) searchlist.get(searchindex);

                nodeindex = node;
                nodeindex.setSearchBackground(true);

                updateViewport(node);
            }
        }
    } // end method actionPerformed

    //~ Inner Classes ***********************************************************

    /**
     * Combines two conditions to form a boolean expression
     */
    class CompoundCondition extends Condition {
        /**
         * Constructor
         * @param first left hand side
         * @param second right hand side
         * @param operator the operator that joins LHS and RHS
         */
        CompoundCondition(Condition first, Condition second, String operator) {
            this.first = first;
            this.second = second;
            this.operator = operator;
        }

        /**
         * Return the condition as a nicely formatted string.
         * @param condition condtion to format
         * @return a nicely formatted string describing the condition
         */
        String toString(Condition condition) {
            String expression;

            if (condition instanceof CompoundCondition) {
                expression =
                        "(" + toString(condition.first) + " " +
                        condition.operator;
                expression = expression + " " + toString(condition.second) +
                             ")";
            } else {
                return condition.toString();
            }

            return expression;
        }

        /**
         * Return the condition as a nicely formatted string.
         * @return a string representation of the object.
         */
        public String toString() {
            return toString(this);
        }
    }


    /**
     * A Condition that specifies a percent of the examples that must be
     * satisfied, like yes > 50.0%
     */
    class PercentCondition extends Condition {
        /**
         * Constructor
         * @param attribute attribute name
         * @param operator operator
         * @param value percent value
         */
        PercentCondition(String attribute, String operator, double value) {
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;
        }

        /**
         *Return the condition as a nicely formatted string.
         * @return a string representation of the object.
         */
        public String toString() {
            return "Percent: " + attribute + " " + operator + " " + value;
        }
    }


    /**
     * A Condition that specifies the amount of examples of an attribute that
     * must be satisfied, like count > 50.
     */
    class PopulationCondition extends Condition {
        PopulationCondition(String attribute, String operator, double value) {
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;
        }

        /**
         * Return the condition as a nicely formatted string.
         * @return a string representation of the object.
         */
        public String toString() {
            return "Population: " + attribute + " " + operator + " " + value;
        }
    }


    /**
     * A Condition that is a measure of entropy
     */
    class PurityCondition extends Condition {
        /**
         * Constructor
         * @param operator the operator
         * @param value the value
         */
        PurityCondition(String operator, double value) {
            this.attribute = "Purity";
            this.operator = operator;
            this.value = value;
        }

        /**
         * Return the condition as a nicely formatted string.
         * @return a string representation of the object.
         */
        public String toString() {
            return "Purity: " + operator + " " + value;
        }
    }


    /**
     * A split on the input value used to split a node.
     */
    class SplitCondition extends Condition {
        /** true if the split is a scalar value */
        boolean scalar;
        /** used for non-scalar nodes.  this will be the branch label */
        String svalue;

        /**
         * Constructor for scalar splits
         * @param attribute attribute name
         * @param operator operator
         * @param value the value to compare to the split value
         */
        SplitCondition(String attribute, String operator, double value) {
            this.attribute = attribute;
            this.operator = operator;
            this.value = value;

            scalar = true;
        }

        /**
         * Constructor
         * @param attribute the attribute name
         * @param operator operator
         * @param svalue the branch
         */
        SplitCondition(String attribute, String operator, String svalue) {
            this.attribute = attribute;
            this.operator = operator;
            this.svalue = svalue;

            scalar = false;
        }

        /**
         * Return the condition as a nicely formatted string.
         * @return a string representation of the object.
         */
        public String toString() {

            if (scalar) {
                return "Split: " + attribute + " " + operator + " " + value;
            } else {
                return "Split: " + attribute + " " + operator + " " + svalue;
            }
        }
    } // end class SplitCondition


    /**
     * Encapsulates a search criterion
     */
    protected class Condition {

        /** the attribute name */
        String attribute;
        /** LHS */
        Condition first;
        /** RHS */
        Condition second;
        /** operator that joins the Conditions, either AND or OR */
        String operator;
        /** value */
        double value;

        /**
         * Evaluate
         * @param condition condition
         * @param node node
         * @return evaluation
         */
        boolean evaluate(Condition condition, Viewport node) {
            boolean expression;

            if (condition instanceof CompoundCondition) {
                expression = evaluate(condition.first, node);

                if (condition.operator == AND) {
                    expression = expression && evaluate(condition.second, node);
                } else if (condition.operator == OR) {
                    expression = expression || evaluate(condition.second, node);
                }
            } else {
                return node.evaluate(condition);
            }

            return expression;
        }
    }
} // end class SearchPanel

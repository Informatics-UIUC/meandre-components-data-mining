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

package org.meandre.applet.prediction.naivebayes.support;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import java.io.InputStream;
import java.io.BufferedInputStream;

import org.meandre.applet.support.Constrain;
import org.meandre.components.prediction.naivebayes.support.NaiveBayesModel;
import org.meandre.components.prediction.naivebayes.support.NaiveBayesPieChartData;

class NBView extends JPanel implements ActionListener {
    /** the model */
    private transient NaiveBayesModel model;
    /** legend displays the colors */
    private transient Legend legend;

    /** preferred size for the grid */
    private transient Dimension preferredGrid;
    /** preferred size for the header */
    private transient Dimension preferredHeader;

    /** number format */
    private transient NumberFormat nf;

    /** true if zoom in */
    private transient boolean zoomin = false;

    /** menu bar */
    private transient JMenuBar menuBar;
    /** check box to display items as pie charts */
    private transient JCheckBoxMenuItem miPieChart;
    /** check box to display items as bar charts */
    private transient JCheckBoxMenuItem miBarChart;
    /** check box to sort attributes by the best predictor */
    private transient JCheckBoxMenuItem miAttrBest;
    /** check box to sort attributes in alpha order */
    private transient JCheckBoxMenuItem miAttrAlpha;
    /** check box to sort grid by total evidence */
    private transient JCheckBoxMenuItem miEviTot;
    /** check box to sort grid by alpha order (bin name) */
    private transient JCheckBoxMenuItem miEviAlpha;
    /** check box to show predictor values for attributes */
    private transient JCheckBoxMenuItem miShowPredVal;
    /** print menu item */
    private transient JMenuItem miPrint;
    /** menu items to select colors for class names */
    private transient ColorMenuItem[] colorItems;
    /** menu items for attributes */
    private transient AttributeMenuItem[] attributeItems;
    /** checkbox to dipslay percentages */
    private transient JCheckBoxMenuItem miPercentage;
    /** help menu item */
    private transient JMenuItem helpItem;
    /** save as pmml menu item */
    private transient JMenuItem saveAsPmml;

    /** zoom button */
    private transient JToggleButton zoom;
    /** print button */
    private transient JButton printButton;
    /** refresh view */
    private transient JButton refreshView;

    /** the longest attribute name, needed to determine string length */
    private transient String longest_attribute_name;
    /** the longest bin name, needed to determine string length */
    private transient String longest_bin_name;

    /** width of grid */
    private transient int gridwidth;
    /** height of grid */
    private transient int gridheight;
    /** 10% of gridwidth */
    private transient float grid1;
    /** 75% of gridwidth */
    private transient float grid75;
    /** 25% of gridwidth */
    private transient float grid25;
    /** 5% of gridwidth */
    private transient float grid05;
    /** 50% of gridwidth */
    private transient float grid5;
    /** 85% of gridwidth */
    private transient float grid85;
    /** 15% of gridwidth */
    private transient float grid15;
    /** 20% of gridwidth */
    private transient float grid2;
    /** 60% of gridwidth */
    private transient float grid6;

    /** padding is 3% of gridwidth */
    private transient float padding;

    /** grid panel presents the pie charts/bar charts */
    private transient GridPanel gp;
    /** header panel */
    private transient HeaderPanel hp;
    /** composite panel shows a large pie chart that shows probablilties given
     * the evidence selected
     */
    private transient CompositePanel cp;
    /** message area shows details on the selected piece of evidence */
    private transient MessageArea ma;

    /** the selected item in each row */
    private transient int[] selected;

    /** number of rows in the grid */
    private transient int numRows = 0;
    /** number of columns in the grid */
    private transient int numCols = 0;

    /** map class names to a color */
    private transient HashMap color_map;

    /** the names of the classes */
    private transient String[] class_names;
    /** the names of the attributes */
    private transient String[] attribute_names;
    /** the names of the attributes in ranked order */
    private transient String[] all_ranked_attribute_names;
    /** the names of the attributes in alpha order */
    private transient String[] all_alpha_attribute_names;
    /** the data for the pie charts */
    private transient NaiveBayesPieChartData[][] row_data;
    /** predictor values for the attributes */
    private transient double[] predictor_values;
    /** y location of last mouse press */
    private transient int mouse_pos_y;

    /** help window */
    private transient HelpWindow helpWindow;
    /** yellowish color */
    private static final Color yellowish = new Color(255, 255, 240);
    /** grayish color */
    private static final Color grayish = new Color(236, 235, 222);
    /** dark background color */
    private static final Color darkBg = new Color(219, 217, 206);
    /** message color */
    private static final Color messageColor = new Color(64, 64, 64);
    /** label foreground color */
    private static final Color labelFg = new Color(96, 100, 86);
    /** header label color */
    private static final Color headerLabelColor = new Color(0, 0, 0);

    /** button size */
    private static final Dimension buttonsize = new Dimension(25, 25);
    /** label font */
    private static final Font labelFont = new Font("Helvetica", Font.PLAIN, 11);

    /**
     * color wheel
     */
    private static final Color[] colors = {
                                          new Color(71, 74, 98),
                                          new Color(191, 191, 115),
                                          new Color(111, 142, 116),
                                          new Color(178, 198, 181),
                                          new Color(153, 185, 216),
                                          new Color(96, 93, 71),
                                          new Color(146, 205, 163),
                                          new Color(203, 84, 84),
                                          new Color(217, 183, 170),
                                          new Color(140, 54, 57),
                                          new Color(203, 136, 76)
    };

    /** constant for ATTRIBUTES */
    private static final String ATT = "ATTRIBUTES";
    /** constant for EVIDENCE */
    private static final String EVI = "EVIDENCE";
    /** constant for CONCLUSION */
    private static final String CONC = "CONCLUSION";
    /** constant for newline character */
    private static final String NEW_LINE = "\n";
    /** constant for space character */
    private static final String SPACE = " ";
    /** constant for Total : */
    private static final String TOTAL = "  Total : ";
    /** constant for = */
    private static final String EQUALS = "=";
    /** constant for : */
    private static final String COLON = " : ";
    /** constant for % */
    private static final String PERCENT_SIGN = "%";
    /** constant for ( */
    private static final String OPEN_PAREN = " (";
    /** constant for ) */
    private static final String CLOSE_PAREN = ")";
    /** constant for % error */
    private static final String ERR = "% error";
    /** constant for More.. */
    private static final String MORE = "More..";

    /** max menu items */
    private static final int MAX_MENU_ITEMS = 15;
    /** preferred grid size */
    private static final int PREF_GRID = 50;
    /** preferred header size */
    private static final int PREF_HEAD = 100;
    /** maximum length string to describe error */
    private static final String MAX_ATTRIBUTE_ERROR = "100.00% error";

    /** zoom icon location */
    private static final String zoomicon = "/icons/zoom.gif" ;
    /** refresh icon location */
    private static final String refreshicon = "/icons/home.gif" ;

    /**
     *
     * @param loc String
     * @return Image from a local file
     */
    public Image getImage(String loc) {
        Image img = null;
        try {
            MediaTracker m = new MediaTracker(this);
            InputStream is = getClass().getResourceAsStream(loc);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] byBuf = new byte[10000];
            int byteRead = bis.read(byBuf, 0, 10000);
            img = Toolkit.getDefaultToolkit().createImage(byBuf);
            m.addImage(img, 0);
            m.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * Format the number as a percentage with a max of 2 fraction digits
     * @param doub the percentage
     * @return String with the percentage and a percent sign
     */
    private static String percentage(double doub) {
        doub *= 100;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        StringBuffer sb = new StringBuffer(nf.format(doub));
        sb.append(PERCENT_SIGN);
        return sb.toString();
    }

    /**
     * Sort an array of Strings.  Makes a copy and returns a new, sorted array.
     * The original is not modified.
     * @param names unsorted array
     * @return copy of names, in sorted order
     */
    private static String[] sortNames(String[] names) {
        String[] copy = new String[names.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = names[i];
        }
        Arrays.sort(copy);

        return copy;
    }

    /**
     * Called by the D2K Infrastructure to allow the view to perform initialization tasks.
     *
     * @param m The module this view is associated with.
     */
    public void initView() {
        menuBar = new JMenuBar();
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        helpWindow = new HelpWindow();
    }

    /**
     * Called to pass the inputs received by the module to the view.  Set up
     * the UI when the model is received.
     *
     * @param o The object that has been input.
     * @param i The index of the module input that been received.
     * @throws Exception when something goes wrong
     */
    public void setInput(Object o, int i) throws Exception {
        model = (NaiveBayesModel) o;
        if (!model.isReadyForVisualization())
            throw new Exception("NaiveBayesModel has to be processed by PrepareForVisualization module before entering NaiveBayesVisualization");
        all_ranked_attribute_names = model.getAttributeNames();
        all_alpha_attribute_names = sortNames(all_ranked_attribute_names);
        class_names = model.getClassNames();
        color_map = new HashMap();
        // map each class to a color
        for (int j = 0; j < class_names.length; j++) {
            color_map.put(class_names[j], colors[j % colors.length]);

            // when first displayed, we show all attributes by default
        }
        attribute_names = all_ranked_attribute_names;
        numRows = attribute_names.length;
        //System.out.println("numRows "  + numRows);
        selected = new int[attribute_names.length];
        row_data = new NaiveBayesPieChartData[attribute_names.length][];
        predictor_values = new double[attribute_names.length];
        int longest = 0;
        longest_bin_name = "";
        longest_attribute_name = "";

        // get the row data
        for (int j = 0; j < attribute_names.length; j++) {
            row_data[j] = model.getData(attribute_names[j]);
            //	System.out.println("row_data[" +j+ "] " + row_data[j]);
            predictor_values[j] = model.getPredictionValue(attribute_names[j]);
            selected[j] = -1;
            if (attribute_names[j].length() > longest) {
                longest_attribute_name = attribute_names[j];
                longest = attribute_names[j].length();
            }
            if (row_data[j].length > numCols) {
                numCols = row_data[j].length;
            }
            for (int k = 0; k < row_data[j].length; k++) {
                if (row_data[j][k].getBinName().length() >
                    longest_bin_name.length()) {
                    longest_bin_name = row_data[j][k].getBinName();
                }
            }
        }
        if (longest_attribute_name.length() < MAX_ATTRIBUTE_ERROR.length()) {
            longest_attribute_name = MAX_ATTRIBUTE_ERROR;

            // make the grid
        }
        gp = new GridPanel();
        int sq;
        if (numCols > numRows) {
            sq = PREF_GRID * numCols;
        } else {
            sq = PREF_GRID * numRows;
        }
        preferredGrid = new Dimension(sq, sq);
        gp.setPreferredSize(preferredGrid);
        // make the header
        hp = new HeaderPanel();
        preferredHeader = new Dimension(PREF_HEAD, sq);
        hp.setPreferredSize(preferredHeader);

        // make the message area
        if (class_names.length < 4) {
            ma = new MessageArea(class_names.length);
        } else {
            ma = new MessageArea(4);

            // make the legend
        }
        legend = new Legend();

        JScrollPane jsp = new JScrollPane(gp);
        jsp.setViewportBorder(new SPBorder());
        jsp.getViewport().setBackground(yellowish);
        jsp.setPreferredSize(new Dimension(500, 400));
        JViewport jv = new JViewport();
        jv.setView(hp);
        jsp.setRowHeader(jv);
        JLabel att = new AALabel(ATT);
        att.setBorder(new EmptyBorder(10, 3, 10, 0));
        JLabel evi = new AALabel(EVI);
        evi.setBorder(new EmptyBorder(10, 3, 10, 0));
        jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, att);

        JViewport jv1 = new JViewport();
        jv1.setView(evi);
        jsp.setColumnHeader(jv1);

        cp = new CompositePanel();
        cp.setPreferredSize(new Dimension(250, 250));
        JScrollPane jp1 = new JScrollPane(cp);
        jp1.setViewportBorder(new SPBorder2());
        JScrollPane jp2 = new SameSizeSP(ma, ma.getPreferredSize());
        JScrollPane jp3 = new SameSizeSP(legend, ma.getPreferredSize());
        JViewport jch = new JViewport();
        JPanel cnr = new JPanel();
        cnr.setBackground(yellowish);
        jp3.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, cnr);
        jp2.setMaximumSize(ma.getPreferredSize());
        jp3.setMaximumSize(ma.getPreferredSize());
        JPanel pq = new JPanel();
        JPanel pq1 = new JPanel();
        pq1.setLayout(new GridLayout(2, 1));
        pq1.add(jp3);
        pq1.add(jp2);
        pq.setLayout(new BorderLayout());
        pq.add(jp1, BorderLayout.CENTER);
        pq.add(pq1, BorderLayout.SOUTH);
        JViewport jv2 = new JViewport();
        JLabel clLabel = new AALabel(CONC);
        clLabel.setBorder(new EmptyBorder(10, 3, 10, 0));
        JPanel clp = new JPanel();
        JPanel bp = new JPanel();
        bp.setLayout(new GridLayout(1, 3));
        Image im = getImage(refreshicon);
        ImageIcon ri = null;
        if (im != null) {
            ri = new ImageIcon(im);
        }
        if (ri != null) {
            refreshView = new JButton(ri);
        } else {
            refreshView = new JButton("R");
        }
        refreshView.addActionListener(this);
        refreshView.setToolTipText("Reset View");

        im = getImage(zoomicon);
        ImageIcon zi = null;
        if (im != null) {
            zi = new ImageIcon(getImage(zoomicon));
        }
        if (zi != null) {
            zoom = new JToggleButton(zi);
        } else {
            zoom = new JToggleButton("Z");

        }
        zoom.addActionListener(this);
        zoom.setToolTipText("Zoom");

        if (ri != null && zi != null) {
            zoom.setMaximumSize(buttonsize);
            zoom.setPreferredSize(buttonsize);
            refreshView.setMaximumSize(buttonsize);
            refreshView.setPreferredSize(buttonsize);
        }

        bp.add(refreshView);
        //bp.add(printButton);
        bp.add(zoom);

        clp.setLayout(new BorderLayout());
        clp.add(clLabel, BorderLayout.CENTER);
        JPanel bq = new JPanel();
        bq.setLayout(new BoxLayout(bq, BoxLayout.Y_AXIS));
        bq.add(Box.createGlue());
        bq.add(bp);
        bq.add(Box.createGlue());
        clp.add(bq, BorderLayout.EAST);
        jv2.setView(clp);

        jp1.setColumnHeader(jv2);

        pq.setBorder(new EmptyBorder(3, 3, 3, 3));
        JPanel bg = new JPanel();
        bg.setLayout(new BorderLayout());
        bg.add(jsp, BorderLayout.CENTER);
        bg.setBorder(new EmptyBorder(3, 3, 3, 3));
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        p1.add(bg, BorderLayout.CENTER);
        p1.add(pq, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(p1, BorderLayout.CENTER);

        // setup the menus
        JMenu m1 = new JMenu("Options");
        JMenu m2 = new JMenu("Views");
        m2.add(miPieChart = new JCheckBoxMenuItem("Pie Charts", true));
        m2.add(miBarChart = new JCheckBoxMenuItem("Bar Charts", false));
        m1.add(m2);
        menuBar.add(m1);
        miPieChart.addActionListener(this);
        miBarChart.addActionListener(this);

        JMenu m3 = new JMenu("Sort Attributes By");
        m3.add(miAttrBest = new JCheckBoxMenuItem("Best Predictor", true));
        m3.add(miAttrAlpha = new JCheckBoxMenuItem("Alphabetical Order", false));
        miAttrBest.addActionListener(this);
        miAttrAlpha.addActionListener(this);

        JMenu m4 = new JMenu("Sort Evidence By");
        m4.add(miEviTot = new JCheckBoxMenuItem("Bin Weights", true));
        m4.add(miEviAlpha = new JCheckBoxMenuItem("Alphabetical Order", false));
        miEviTot.addActionListener(this);
        miEviAlpha.addActionListener(this);

        JMenu m5 = new JMenu("Show Attributes");
        attributeItems = new AttributeMenuItem[attribute_names.length];
        JMenu curMenu = m5;
        int numItems = 0;
        for (int j = 0; j < attributeItems.length; j++) {
            attributeItems[j] = new AttributeMenuItem(attribute_names[j]);
            attributeItems[j].addActionListener(this);
            if (numItems == MAX_MENU_ITEMS) {
                JMenu nextMenu = new JMenu(MORE);
                curMenu.insert(nextMenu, 0);
                nextMenu.add(attributeItems[j]);
                curMenu = nextMenu;
                numItems = 1;
            } else {
                curMenu.add(attributeItems[j]);
                numItems++;
            }
        }

        JMenu m6 = new JMenu("Set Colors");
        colorItems = new ColorMenuItem[class_names.length];
        curMenu = m6;
        numItems = 0;
        for (int j = 0; j < colorItems.length; j++) {
            colorItems[j] = new ColorMenuItem(class_names[j]);
            colorItems[j].addActionListener(this);
            if (numItems == MAX_MENU_ITEMS) {
                JMenu nextMenu = new JMenu(MORE);
                curMenu.insert(nextMenu, 0);
                //curMenu.add(nextMenu);
                nextMenu.add(colorItems[j]);
                curMenu = nextMenu;
                numItems = 1;
            } else {
                curMenu.add(colorItems[j]);
                numItems++;
            }
        }

        m1.add(m3);
        m1.add(m4);
        m1.add(m5);
        m1.add(m6);
        m1.addSeparator();
        m1.add(miShowPredVal =
                new JCheckBoxMenuItem("Show Predictor Values",
                                      false));
        miShowPredVal.addActionListener(this);
        m1.add(miPercentage =
                new JCheckBoxMenuItem("Show Bin Weight Percentage",
                                      false));
        miPercentage.addActionListener(this);
        m1.addSeparator();
        m1.add(miPrint = new JMenuItem("Print.."));
        miPrint.addActionListener(this);
        m1.add(saveAsPmml = new JMenuItem("Save as PMML..."));
        saveAsPmml.setEnabled(false);
        saveAsPmml.addActionListener(this);

        JMenu helpMenu = new JMenu("Help");
        helpItem = new JMenuItem("About Naive Bayes Vis..");
        helpMenu.add(helpItem);
        helpItem.addActionListener(this);

        menuBar.add(helpMenu);
    }

    /**
     * Supplies the menu bar to be added to the frame that holds this view.
     *
     * @return a menubar specific to this component.
     */
    public Object getMenu() {
        return menuBar;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        // show pie charts (the default)
        if (src == miPieChart) {
            miPieChart.setState(true);
            miBarChart.setState(false);
            repaint();
        }

        // show bar charts
        else if (src == miBarChart) {
            miPieChart.setState(false);
            miBarChart.setState(true);
            repaint();
        }

        // sort the attributes by best predictor
        else if (src == miAttrBest) {
            miAttrBest.setState(true);
            miAttrAlpha.setState(false);
            model.clearEvidence();
            for (int i = 0; i < selected.length; i++) {
                selected[i] = -1;

            }
            HashMap toShow = new HashMap();
            for (int i = 0; i < attributeItems.length; i++) {
                if (attributeItems[i].getState()) {
                    toShow.put(attributeItems[i].getText(),
                               attributeItems[i].getText());
                }
            }

            LinkedList ll = new LinkedList();
            for (int i = 0; i < all_ranked_attribute_names.length; i++) {
                if (toShow.containsKey(all_ranked_attribute_names[i])) {
                    ll.add(all_ranked_attribute_names[i]);
                }
            }
            attribute_names = new String[ll.size()];
            numRows = attribute_names.length;
            Iterator ii = ll.iterator();
            int idx = 0;
            while (ii.hasNext()) {
                attribute_names[idx] = (String) ii.next();
                idx++;
            }

            row_data = new NaiveBayesPieChartData[attribute_names.length][];
            predictor_values = new double[attribute_names.length];
            numCols = 0;
            for (int i = 0; i < attribute_names.length; i++) {
                row_data[i] = model.getData(attribute_names[i]);
                predictor_values[i] = model.getPredictionValue(attribute_names[
                        i]);
                if (row_data[i].length > numCols) {
                    numCols = row_data[i].length;
                }
            }
            repaint();
        }

        // sort the attributes alphabetically
        else if (src == miAttrAlpha) {
            miAttrBest.setState(false);
            miAttrAlpha.setState(true);
            model.clearEvidence();
            for (int i = 0; i < selected.length; i++) {
                selected[i] = -1;

            }
            HashMap toShow = new HashMap();
            for (int i = 0; i < attributeItems.length; i++) {
                if (attributeItems[i].getState()) {
                    toShow.put(attributeItems[i].getText(),
                               attributeItems[i].getText());
                }
            }

            LinkedList ll = new LinkedList();
            for (int i = 0; i < all_alpha_attribute_names.length; i++) {
                if (toShow.containsKey(all_alpha_attribute_names[i])) {
                    ll.add(all_alpha_attribute_names[i]);
                }
            }
            attribute_names = new String[ll.size()];
            numRows = attribute_names.length;
            Iterator ii = ll.iterator();
            int idx = 0;
            while (ii.hasNext()) {
                attribute_names[idx] = (String) ii.next();
                idx++;
            }

            row_data = new NaiveBayesPieChartData[attribute_names.length][];
            predictor_values = new double[attribute_names.length];
            numCols = 0;
            for (int i = 0; i < attribute_names.length; i++) {
                row_data[i] = model.getData(attribute_names[i]);
                predictor_values[i] = model.getPredictionValue(attribute_names[
                        i]);
                if (row_data[i].length > numCols) {
                    numCols = row_data[i].length;
                }
            }
            repaint();
        }

        // change the color associated with a class
        else if (src instanceof ColorMenuItem) {
            ColorMenuItem mi = (ColorMenuItem) src;
            Color oldColor = getColor(mi.getText());
            StringBuffer sb = new StringBuffer("Choose ");
            sb.append(mi.getText());
            sb.append(" Color");
            Color newColor = JColorChooser.showDialog(this, sb.toString(),
                    oldColor);
            if (newColor != null) {
                color_map.put(mi.getText(), newColor);
                repaint();
            }
        }

        // show or hide attributes
        else if (src instanceof AttributeMenuItem) {
            model.clearEvidence();
            for (int i = 0; i < selected.length; i++) {
                selected[i] = -1;
            }
            AttributeMenuItem mi = (AttributeMenuItem) src;

            HashMap toShow = new HashMap();
            for (int i = 0; i < attributeItems.length; i++) {
                if (attributeItems[i].getState()) {
                    toShow.put(attributeItems[i].getText(),
                               attributeItems[i].getText());
                }
            }

            // sort by the best predictor
            if (miAttrBest.getState()) {
                LinkedList ll = new LinkedList();
                for (int i = 0; i < all_ranked_attribute_names.length; i++) {
                    if (toShow.containsKey(all_ranked_attribute_names[i])) {
                        ll.add(all_ranked_attribute_names[i]);
                    }
                }
                attribute_names = new String[ll.size()];
                Iterator ii = ll.iterator();
                int idx = 0;
                while (ii.hasNext()) {
                    attribute_names[idx] = (String) ii.next();
                    idx++;
                }
            }
            // sort in alphabetical order
            else {
                LinkedList ll = new LinkedList();
                for (int i = 0; i < all_alpha_attribute_names.length; i++) {
                    if (toShow.containsKey(all_alpha_attribute_names[i])) {
                        ll.add(all_alpha_attribute_names[i]);
                    }
                }
                attribute_names = new String[ll.size()];
                numRows = attribute_names.length;
                Iterator ii = ll.iterator();
                int idx = 0;
                while (ii.hasNext()) {
                    attribute_names[idx] = (String) ii.next();
                    idx++;
                }
            }

            row_data = new NaiveBayesPieChartData[attribute_names.length][];
            predictor_values = new double[attribute_names.length];
            numCols = 0;
            for (int i = 0; i < attribute_names.length; i++) {
                row_data[i] = model.getData(attribute_names[i]);
                predictor_values[i] = model.getPredictionValue(attribute_names[
                        i]);
                if (row_data[i].length > numCols) {
                    numCols = row_data[i].length;
                }
            }
            setPreferredSize(getPreferredSize());
            repaint();
        }

        // sort the evidence by the bin totals (the default)
        else if (src == miEviTot) {
            miEviTot.setState(true);
            miEviAlpha.setState(false);
            model.clearEvidence();

            // call on model to resort
            model.sortChartDataByRank();
            for (int i = 0; i < attribute_names.length; i++) {
                row_data[i] = model.getData(attribute_names[i]);
            }
            for (int i = 0; i < selected.length; i++) {
                selected[i] = -1;

            }
            repaint();
        }

        // sort the evidence alphabetically by bin name
        else if (src == miEviAlpha) {
            miEviTot.setState(false);
            miEviAlpha.setState(true);

            model.clearEvidence();
            // call on model to resort
            model.sortChartDataAlphabetically();
            for (int i = 0; i < attribute_names.length; i++) {
                row_data[i] = model.getData(attribute_names[i]);
            }
            for (int i = 0; i < selected.length; i++) {
                selected[i] = -1;

            }
            repaint();
        }
        // show the bin weights as percentages
        else if (src == miPercentage) {
            repaint();

            // show the predictor values
        } else if (src == miShowPredVal) {
            hp.repaint();
        }
        // zoom
        else if (src == zoom) {
            if (zoomin) {
                zoomin = false;
            } else {
                zoomin = true;
            }
        }
        // reset the view to the initial size
        else if (src == refreshView) {
            gp.setPreferredSize(preferredGrid);
            hp.setPreferredSize(preferredHeader);
            gp.revalidate();
            hp.revalidate();
        } else if (src == helpItem) {
            helpWindow.setVisible(true);
        }
    }

    /**
     * Lookup the color for a class name
     * @param s class name
     * @return color associated with this class
     */
    private Color getColor(String s) {
        return (Color) color_map.get(s);
    }

    /**
       Scales a graphics context's font so that a given
       string will fit within a given horizontal pixel width.
     @param g2 graphics context
     @param str string
     @param spaceH max horizontal space
     @param spaceV max vertical space
     @return FontMetrics that will size str to fit in the given space
     */
    private final FontMetrics scaleFont(Graphics2D g2, String str, int spaceH,
                                        int spaceV) {
        boolean fits = false;
        Font ft = g2.getFont();
        String nm = ft.getName();
        FontMetrics fm = g2.getFontMetrics();
        int size = ft.getSize();
        int style = ft.getStyle();

        while (!fits) {
            if (fm.getHeight() <= spaceV &&
                fm.stringWidth(str) <= spaceH) {
                fits = true;
            } else {
                if (size <= 4) {
                    fits = true;
                } else {
                    g2.setFont(ft = new Font(nm, style, --size));
                    fm = g2.getFontMetrics();
                }
            }
        }
        return fm;
    }

    /**
       Show the evidence in a grid
     */
    private final class GridPanel extends JPanel implements MouseListener,
            MouseMotionListener {

        /**
         * Constructor
         */
        GridPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);
            setBackground(yellowish);
        }

        /**
         * Paint all the pie charts/bar charts
         * @param g graphics context
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            int xl = 0;
            int yl = 0;

            // draw the name of the chart
            scaleFont(g2, longest_bin_name,
                      gridwidth, (int) grid15);
            for (int i = 0; i < attribute_names.length; i++) {
                NaiveBayesPieChartData[] thisrow =
                        row_data[i];

                for (int j = 0; j < thisrow.length; j++) {
                    double w = (double) thisrow[j].getTotal() /
                               (double) thisrow[j].getRowTotal();

                    if (miPercentage.getState()) {
                        drawWeightPercentage(g2, xl, yl, w);
                    } else {
                        drawWeightBar(g2, xl, yl, w);

                    }
                    boolean is_selected = false;
                    if (selected[i] == j) {
                        is_selected = true;

                        // draw chart
                    }
                    if (miPieChart.getState()) {
                        drawPieChart(g2, xl, yl,
                                     thisrow[j], is_selected);
                    } else {
                        drawBarChart(g2, xl, yl,
                                     thisrow[j], is_selected);

                    }
                    drawName(g2, xl, yl,
                             thisrow[j].getBinName());
                    xl += gridwidth;
                }
                xl = 0;
                yl += gridheight;
            }
        }

        /**
         * Moves and resizes this component. The new location of the top-left
         * corner is specified by <code>x</code> and <code>y</code>, and the
         * new size is specified by <code>width</code> and <code>height</code>.
         *
         * @param x      the new <i>x</i>-coordinate of this component
         * @param y      the new <i>y</i>-coordinate of this component
         * @param w  the new <code>width</code> of this component
         * @param h the new <code>height</code> of this
         *               component
         */
        public void setBounds(int x, int y, int w, int h) {
            if (numCols != 0)
                gridwidth = w / numCols;
            else
                gridwidth = 0;

            if (numRows != 0)
                gridheight = w / numRows;
            else
                gridheight = 0;

            // make gridwidth equal to gridheight
            if (gridwidth < gridheight) {
                gridheight = gridwidth;
            } else {
                gridwidth = gridheight;

                // cache these calculations for later
            }
            grid1 = (float) (.1f * gridwidth);
            grid75 = (float) (.75f * gridwidth);
            grid05 = (float) (.05f * gridwidth);
            grid5 = (float) (.5f * gridwidth);
            grid85 = (float) (.85f * gridwidth);
            grid15 = (float) (.15f * gridwidth);
            grid2 = (float) (.2f * gridwidth);
            grid6 = (float) (.6f * gridwidth);
            grid25 = (float) (.25f * gridwidth);
            padding = (float) (.03 * gridwidth);
            super.setBounds(x, y, w, h);
        }

        /**
           Draw the weight bar above a pie/bar chart.
         @param g2 graphics context
         @param x x location
         @param y y location
         @param weight the weight amount
         */
        private void drawWeightBar(Graphics2D g2, int x, int y,
                                   double weight) {
            g2.setPaint(Color.black);
            g2.draw(new Rectangle2D.Double(x + (int) grid25, y + (int) grid1,
                                           (int) grid5, (int) grid05));
            g2.setPaint(Color.white);
            g2.fill(new Rectangle2D.Double(x + (int) (grid25 + 1),
                                           y + (int) (grid1 + 1),
                                           (int) (grid5 - 1),
                                           (int) (grid05 - 1)));
            g2.setPaint(Color.darkGray);
            g2.fill(new Rectangle2D.Double(x + (int) (grid25 + 1),
                                           y + (int) (grid1 + 1),
                                           (int) (weight * (grid5 - 1)),
                                           (int) (grid05 - 1)));
        }

        /**
         * Draw the bin weight as a percentage.
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         * @param w the weight
         */
        private void drawWeightPercentage(Graphics2D g2, int x, int y,
                                          double w) {
            // draw text
            g2.setPaint(Color.black);
            FontMetrics metrics = g2.getFontMetrics();
            StringBuffer sb = new StringBuffer(nf.format(w * 100));
            sb.append("%");
            String n = sb.toString();
            g2.drawString(n,
                          (int) (x + grid5 - .5 * metrics.stringWidth(n)),
                          (int) (y + grid05 + metrics.getAscent() + 2));
        }

        /**
         * Draw a bar chart for a bin
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         */
        private void drawBarChart(Graphics2D g2, int x, int y,
                                  NaiveBayesPieChartData data,
                                  boolean selected) {

            // draw background if selected
            if (selected) {
                g2.setPaint(grayish);
                g2.fill(new Rectangle2D.Double(x + grid2,
                                               y + grid25 - padding, grid6,
                                               grid5 + 2 * padding));
                g2.setPaint(Color.black);
                g2.draw(new Rectangle2D.Double(x + grid2,
                                               y + grid25 - padding,
                                               grid6, grid5 + 2 * padding));
            }
            g2.setPaint(Color.black);

            // draw the axes
            g2.draw(new Line2D.Double(x + (int) (grid25),
                                      y + (int) (grid75), x + (int) (grid75),
                                      y + (int) (grid75)));
            g2.draw(new Line2D.Double(x + (int) (grid25),
                                      y + (int) (grid25), x + (int) (grid25),
                                      y + (int) (grid75)));

            int barWidth = (int) (grid5 / class_names.length);
            double startX = x + (int) (grid25); // + 1

            // draw the bars
            // start at the top and move down
            for (int count = class_names.length - 1; count >= 0; count--) {
                g2.setColor(getColor(data.getClassName(count)));
                double ratio = data.getClass(class_names[count]);

                g2.fill(new Rectangle2D.Double(startX,
                                               (y + grid75) - (grid5 * ratio),
                                               barWidth,
                                               grid5 * ratio));

                // 5/20/02:
                g2.setColor(Color.black);
                g2.draw(new Rectangle2D.Double(startX,
                                               (y + grid75) - (grid5 * ratio),
                                               barWidth,
                                               grid5 * ratio));

                startX += barWidth;
            }
        }

        /**
         * Draw a bin as a pie chart
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         * @param data table containing the data to draw
         * @param selected true if the pie chart should be highlighted
         */
        private void drawPieChart(Graphics2D g2, int x, int y,
                                  NaiveBayesPieChartData data,
                                  boolean selected) {

            // draw background if selected
            if (selected) {
                g2.setPaint(grayish);
                g2.fill(new Rectangle2D.Double(x + grid2,
                                               y + grid25 - padding, grid6,
                                               grid5 + 2 * padding));
                g2.setPaint(Color.black);
                g2.draw(new Rectangle2D.Double(x + grid2,
                                               y + grid25 - padding,
                                               grid6, grid5 + 2 * padding));
            }

            // draw chart
            int angle = 0;
            if (data.getTotal() == 0) {
                g2.setColor(Color.darkGray);
                g2.fill(new Arc2D.Double(x + (int) (grid25), y + (int) (grid25),
                                         (int) (grid5), (int) (grid5), 0,
                                         360, Arc2D.PIE));
            } else {
                for (int count = class_names.length - 1; count >= 0; count--) {
                    g2.setPaint(getColor(data.getClassName(count)));
                    double ratio = data.getClass(class_names[count]);

                    if (count == class_names.length - 1) {
                        g2.fill(new Arc2D.Double(x + (int) (grid25),
                                                 y + (int) (grid25),
                                                 (int) (grid5), (int) (grid5),
                                                 angle,
                                                 (int) (360 - angle), Arc2D.PIE));
                    } else {
                        g2.fill(new Arc2D.Double(x + (int) (grid25),
                                                 y + (int) (grid25),
                                                 (int) (grid5), (int) (grid5),
                                                 angle,
                                                 (int) (360 * ratio), Arc2D.PIE));

                    }
                    angle += (int) (360 * ratio);
                }
            }
        }

        /**
         * Draw the name of a bin
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         * @param n name to draw
         */
        private void drawName(Graphics2D g2, int x, int y, String n) {
            // draw text
            g2.setPaint(Color.black);
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(n,
                          (int) (x + grid5 - .5 * metrics.stringWidth(n)),
                          (int) (y + grid85 + metrics.getAscent()));
        }

        /**
           Zoom in or out when the mouse is pressed
         @param e mouse event
         */
        public void mousePressed(MouseEvent e) {
            mouse_pos_y = e.getY();
            if (zoomin) {
                if (!e.isMetaDown()) {
                    Dimension d = getPreferredSize();
                    setPreferredSize(new Dimension((int) (d.width * 1.1),
                            (int) (d.height * 1.1)));
                    d = hp.getPreferredSize();
                    hp.setPreferredSize(new Dimension(d.width,
                            (int) (d.height * 1.1)));
                    revalidate();
                    hp.revalidate();
                } else {
                    Dimension d = getPreferredSize();
                    setPreferredSize(new Dimension((int) (d.width * .9),
                            (int) (d.height * .9)));
                    d = hp.getPreferredSize();
                    hp.setPreferredSize(new Dimension(d.width,
                            (int) (d.height * .9)));
                    revalidate();
                    hp.revalidate();
                }
            }
        }

        /**
         * Update the selected item when the mouse is clicked
         * @param e mouse event
         */
        public void mouseClicked(MouseEvent e) {
            if (zoomin) {
                return;
            }
            int cx = e.getX();
            int cy = e.getY();
            int xpos = (int) grid2, ypos = (int) grid2;

            if (cx < xpos || cy < ypos) {
                return;
            } else {
                for (int i = 0; i < attribute_names.length; i++) {
                    int bins_in_row = row_data[i].length;
                    for (int j = 0; j < bins_in_row; j++) {
                        // this is of course unnecessary now, but i may have use for it later
                        if (j < bins_in_row) {

                            if (cx > xpos && cx < xpos + grid6 && cy > ypos &&
                                cy < ypos + grid6) {
                                String bin_name = row_data[i][j].getBinName();

                                if (selected[i] == j) {
                                    model.removeEvidence(attribute_names[i],
                                            bin_name);
                                    selected[i] = -1;
                                } else if (selected[i] == -1) {
                                    model.addEvidence(attribute_names[i],
                                            bin_name);
                                    selected[i] = j;
                                } else {
                                    String old_name =
                                            row_data[i][selected[i]].getBinName();
                                    model.removeEvidence(attribute_names[i],
                                            old_name);
                                    model.addEvidence(attribute_names[i],
                                            bin_name);
                                    selected[i] = j;
                                }

                                repaint();
                                cp.repaint();
                                return;
                            }
                        }
                        xpos += gridwidth;
                    }
                    xpos = (int) grid2;
                    ypos += gridheight;
                }
            }
        }

        /**
         * Do nothing special here
         * @param e mouse event
         */
        public void mouseReleased(MouseEvent e) {}

        /**
         * Do nothing special here
         * @param e mouse event
         */
        public void mouseEntered(MouseEvent e) {}

        /**
         * Do nothing special here
         * @param e mouse event
         */
        public void mouseExited(MouseEvent e) {}

        /**
         * Change the size when the mouse is dragged
         * @param e mouse event
         */
        public void mouseDragged(MouseEvent e) {
            if (e.isMetaDown() && gridwidth > 50) {
                if (mouse_pos_y < e.getY()) {
                    Dimension d = getPreferredSize();
                    setPreferredSize(new Dimension((int) (d.width * 1.03),
                            (int) (d.height * 1.03)));
                    setMinimumSize(new Dimension((int) (d.width * 1.03),
                                                 (int) (d.height * 1.03)));
                    d = hp.getPreferredSize();
                    hp.setPreferredSize(new Dimension(d.width,
                            (int) (d.height * 1.03)));
                    hp.setMinimumSize(new Dimension(d.width,
                            (int) (d.height * 1.03)));
                } else {
                    Dimension d = getPreferredSize();
                    setPreferredSize(new Dimension((int) (d.width * .97),
                            (int) (d.height * .97)));
                    setMinimumSize(new Dimension((int) (d.width * .97),
                                                 (int) (d.height * .97)));
                    d = hp.getPreferredSize();
                    hp.setPreferredSize(new Dimension(d.width,
                            (int) (d.height * .97)));
                    hp.setMinimumSize(new Dimension(d.width,
                            (int) (d.height * .97)));
                }
                revalidate();
                hp.revalidate();
            }
            hp.repaint(); ;
        }

        /**
         * Update the brushing info when the mouse is moved
         * @param e mouse event
         */
        public void mouseMoved(MouseEvent e) {
            int cx = e.getX(), cy = e.getY();
            //int xpos = padding, ypos = padding;
            int xpos = (int) grid2;
            int ypos = (int) grid2;

            if (cx < xpos || cy < ypos) {
                return;
            } else {
                for (int i = 0; i < attribute_names.length; i++) {
                    int bins_in_row = row_data[i].length;
                    for (int j = 0; j < bins_in_row; j++) {
                        if (j < bins_in_row) {
                            if (cx > xpos &&
                                cx < xpos + grid6 &&
                                cy > ypos && cy < ypos + grid6) {
                                ma.update(i, j);
                                return;
                            }
                        }
                        xpos += gridwidth;
                    }
                    xpos = (int) grid2;
                    ypos += gridheight;
                }
            }
        }
    }


    /**
       Show the composite and a legend
     */
    private final class CompositePanel extends JPanel {

        /**
         * Constructor
         */
        CompositePanel() {
            setBackground(yellowish);
        }

        /** 5% of the width */
        float sq05;
        /** 10% of the width */
        float sq1;
        /** 80% of the width */
        float sq8;
        /** 70% of the width */
        float sq7;

        /**
         * Paint the composite as a big pie chart or bar chart.
         * @param g graphics context
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            NaiveBayesPieChartData ev_data = model.getCurrentEvidence();
            legend.updateLegend(ev_data);
            if (miPieChart.getState()) {
                drawPieChart(g2, 0, (int) sq05, ev_data);
            } else {
                drawBarChart(g2, 0, (int) sq05, ev_data);

            }
            g2.setFont(labelFont);
            g2.setPaint(messageColor);
            FontMetrics fm = scaleFont(g2, model.getClassColumn(),
                                       getWidth(), (int) sq1);
            g2.drawString(model.getClassColumn(),
                          getWidth() / 2 -
                          fm.stringWidth(model.getClassColumn()) / 2,
                          sq1);
        }

        /**
         * Draw the composite as a pie chart
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         * @param data the data for the composite view
         */
        private void drawPieChart(Graphics2D g2, int x, int y,
                                  NaiveBayesPieChartData data) {

            // draw chart
            int angle = 0;
            for (int count = class_names.length - 1; count >= 0; count--) {
                g2.setPaint(getColor(data.getClassName(count)));
                double ratio = data.getClass(data.getClassName(count));
                //System.out.println(data.getClassName(count)+" "+ratio);

                if (count == class_names.length - 1) {
                    g2.fill(new Arc2D.Double(x + (int) (sq1), y + (int) (sq1),
                                             (int) (sq8), (int) (sq8), angle,
                                             (int) (360 - angle), Arc2D.PIE));
                } else {
                    g2.fill(new Arc2D.Double(x + (int) (sq1), y + (int) (sq1),
                                             (int) (sq8), (int) (sq8), angle,
                                             (int) (360 * ratio), Arc2D.PIE));

                }
                angle += (int) (360 * ratio);
            }
        }

        /**
         * Draw the composite as a bar chart
         * @param g2 graphics context
         * @param x x location
         * @param y y location
         * @param data the data for the composite view
         */
        private void drawBarChart(Graphics2D g2, int x, int y,
                                  NaiveBayesPieChartData data) {

            g2.setPaint(Color.black);
            // draw the axes
            g2.draw(new Line2D.Double(x + (int) (sq1),
                                      y + (int) (sq8), x + (int) (sq8),
                                      y + (int) (sq8)));
            g2.draw(new Line2D.Double(x + (int) (sq1),
                                      y + (int) (sq1), x + (int) (sq1),
                                      y + (int) (sq8)));

            float barWidth = sq7 / (float) class_names.length;
            double startX = x + (int) (sq1) + 1;

            // draw the bars
            // start at the left and move right
            for (int count = class_names.length - 1; count >= 0; count--) {
                //for(int count = data.getNumRows()-1; count >= 0; count--) {
                g2.setColor(getColor(data.getClassName(count)));
                double ratio = data.getClass(class_names[count]);

                g2.fill(new Rectangle2D.Double(startX,
                                               (y + sq8) - (sq7 * ratio),
                                               barWidth,
                                               sq7 * ratio));
                startX += barWidth;
            }
        }

        /**
         * Moves and resizes this component. The new location of the top-left
         * corner is specified by <code>x</code> and <code>y</code>, and the
         * new size is specified by <code>width</code> and <code>height</code>.
         *
         * @param x      the new <i>x</i>-coordinate of this component
         * @param y      the new <i>y</i>-coordinate of this component
         * @param w  the new <code>width</code> of this component
         * @param h the new <code>height</code> of this
         *               component
         */
        public void setBounds(int x, int y, int w, int h) {
            sq1 = (float) (.1 * w);
            sq8 = (float) (.8 * w);
            sq7 = (float) (.7 * w);
            sq05 = (float) (.05 * w);
            super.setBounds(x, y, w, h);
        }
    }


    /**
       Show the attribute names along the left hand side
     */
    private final class HeaderPanel extends JPanel {
        /** number format */
        NumberFormat nfmt;

        /**
         * Constructor
         */
        HeaderPanel() {
            setBackground(darkBg);
            nfmt = NumberFormat.getInstance();
            nfmt.setMaximumFractionDigits(2);
        }

        /**
         * Paint the attribute names and their predictor values, if the checkbox
         * is checked
         * @param g graphics context
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(headerLabelColor);
            g2.setFont(labelFont);
            int xl = 0;
            FontMetrics fm = scaleFont(g2,
                                       longest_attribute_name, getWidth(),
                                       gridheight);
            int yl = 2 * fm.getAscent();
            int sw;
            int wid = getWidth();
            for (int j = 0; j < attribute_names.length; j++) {
                sw = fm.stringWidth(attribute_names[j]);
                g2.drawString(attribute_names[j],
                              wid / 2 - sw / 2, yl);
                // draw the predictor value
                if (miShowPredVal.getState()) {
                    StringBuffer s =
                            new StringBuffer(nfmt.format(predictor_values[j]));
                    s.append(ERR);
                    String s2 = s.toString();
                    g2.drawString(s2, wid / 2 - fm.stringWidth(s2) / 2,
                                  yl + fm.getHeight() + 1);
                }
                yl += gridheight;
            }
        }
    }


    /**
     * Keep track of the colors with color menu item.
     */
    private final class ColorMenuItem extends JMenuItem {
        ColorMenuItem(String s) {
            super(s);
        }
    }


    /**
     * Keep track of the attributes to show with attribute menu item.
     */
    private final class AttributeMenuItem extends JCheckBoxMenuItem {
        AttributeMenuItem(String s) {
            super(s, true);
        }
    }


    /**
     * Show the colors for each class name and its percentage of
     * the composite
     */
    private final class Legend extends JPanel {

        /**
         * Constructor.  Lay out all the components.
         */
        Legend() {
            setLayout(new GridBagLayout());

            color_comps = new ColorComponent[class_names.length];
            class_labels = new JLabel[class_names.length];
            JLabel leg = new AALabel("LEGEND");
            leg.setBackground(yellowish);
            Constrain.setConstraints(this, leg, 1, 0, 1, 1,
                                     GridBagConstraints.HORIZONTAL,
                                     GridBagConstraints.NORTH, 1.0, 0.0,
                                     new Insets(2, 4, 2, 0));

            Insets ii = new Insets(4, 8, 4, 0);
            Insets i2 = new Insets(4, 8, 4, 0);
            for (int i = 0; i < class_names.length; i++) {
                Color c = getColor(class_names[i]);
                color_comps[i] = new ColorComponent(c);
                Constrain.setConstraints(this, color_comps[i], 0, i + 1, 1, 1,
                                         GridBagConstraints.NONE,
                                         GridBagConstraints.NORTH, 0.0, 0.0, ii);
                class_labels[i] = new AALabel(class_names[i]);
                class_labels[i].setBackground(yellowish);
                class_labels[i].setFont(labelFont);
                class_labels[i].setForeground(messageColor);
                Constrain.setConstraints(this, class_labels[i], 1, i + 1, 1, 1,
                                         GridBagConstraints.HORIZONTAL,
                                         GridBagConstraints.NORTH, 1.0, 0.0, i2);
            }
            setBackground(yellowish);
            updateLegend(null);
        }

        /**
         * Update all the items in the legend
         * @param ev_data current evidence
         */
        private void updateLegend(NaiveBayesPieChartData ev_data) {
            if (ev_data == null) {
                ev_data = model.getCurrentEvidence();

            }
            try {
                ev_data.sortByColumn(2);
            } catch (Exception e) {
            }
            int ct = 0;
            int numRows = ev_data.getNumRows();
            for (int count = numRows - 1; count >= 0; count--) {
                double dr = ev_data.getClass(ev_data.getClassName(count));
                StringBuffer sb = new StringBuffer(ev_data.getClassName(count));
                sb.append(OPEN_PAREN);
                sb.append(percentage(dr));
                sb.append(CLOSE_PAREN);
                color_comps[ct].setBkgrd(getColor(ev_data.getClassName(count)));
                class_labels[ct].setText(sb.toString());
                ct++;
            }
            repaint();
        }
    }


    /**
       Show information on the pie chart that the mouse pointer is over.
       The pie chart represents a bin.
     */
    private final class MessageArea extends JTextArea implements java.io.
            Serializable {

        /**
         * Constructor
         * @param numRow number of rows to display
         */
        MessageArea(int numRow) {
            super(numRow + 3, 0);
            setBackground(yellowish);
            setForeground(messageColor);
            setEditable(false);
            setFont(labelFont);
        }

        /**
         * Make all text anti-aliased for beauty
         * @param g graphics context
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g2);
        }

        /**
         * Update
         * @param row row index of selected bin
         * @param col column index of selected bin
         */
        public void update(int row, int col) {
            setEditable(true);

            StringBuffer sb = new StringBuffer(SPACE);
            sb.append(attribute_names[row]);
            sb.append(EQUALS);
            sb.append(row_data[row][col].getBinName());
            sb.append(NEW_LINE);
            setText(sb.toString());
            NaiveBayesPieChartData data = row_data[row][col];

            sb = new StringBuffer(TOTAL);
            sb.append(data.getTotal());
            sb.append(NEW_LINE);
            append(sb.toString());

            for (int count = class_names.length - 1; count >= 0; count--) {
                int tally = data.getTally(count);
                if (tally > 0) {
                    sb = new StringBuffer(SPACE);
                    sb.append(data.getClassName(count));
                    sb.append(COLON);
                    sb.append(tally);
                    sb.append(COLON);
                    sb.append(percentage(data.getRatio(count)));
                    sb.append(NEW_LINE);
                    append(sb.toString());
                }
            }
            setCaretPosition(0);
            setEditable(false);
        }
    }


    /** JLabels to hold the names of the classes */
    JLabel[] class_labels;
    /** ColorComponents that are associated with the class_labels */
    ColorComponent[] color_comps;

    /**
     * A small square with a black outline.  The color of the
     * square is given in the constructor.
     */
    private final class ColorComponent extends JComponent {
        /** dimension (it is a square) */
        private final int DIM = 12;
        /** background color */
        Color bkgrd;

        /**
         * Constructor
         * @param c color
         */
        ColorComponent(Color c) {
            super();
            setOpaque(true);
            bkgrd = c;
        }

        /**
         * The preferred size is 12x12
         * @return preferred size
         */
        public Dimension getPreferredSize() {
            return new Dimension(DIM, DIM);
        }

        /**
         * The minimum size is 12x12
         * @return minimum size
         */
        public Dimension getMinimumSize() {
            return new Dimension(DIM, DIM);
        }

        /**
         * Fill with the color and then draw a one pixel black border
         * @param g graphics context
         */
        public void paint(Graphics g) {
            g.setColor(bkgrd);
            g.fillRect(0, 0, DIM - 1, DIM - 1);
            g.setColor(Color.black);
            g.drawRect(0, 0, DIM - 1, DIM - 1);
        }

        /**
         * Set the color
         * @param c new color
         */
        void setBkgrd(Color c) {
            bkgrd = c;
        }
    }


    /**
     * A scroll pane that never changes its size.  The preferred, maximum, and
     * minimum sizes are all the same.
     */
    private final class SameSizeSP extends JScrollPane {
        /** preferred size */
        Dimension p;

        /**
         * Constructor
         * @param view Component to show in the scroll pane
         * @param pref preferred size
         */
        SameSizeSP(Component view, Dimension pref) {
            super(view);
            p = pref;
        }

        /**
         * Return preferred size
         * @return preferred size
         */
        public Dimension getPreferredSize() {
            return p;
        }

        /**
         * Return minimum size
         * @return minimum size
         */
        public Dimension getMinimumSize() {
            return p;
        }

        /**
         * Return maximum size
         * @return maximum size
         */
        public Dimension getMaximumSize() {
            return p;
        }
    }


    /**
     * An anti aliased label
     */
    private final class AALabel extends JLabel {

        /**
         * Constructor
         * @param s string to display
         */
        AALabel(String s) {
            super(s);
            setBackground(darkBg);
            setOpaque(true);
            setForeground(labelFg);
        }

        /**
         * Constructor
         * @param s string to display
         * @param i alignment
         */
        AALabel(String s, int i) {
            super(s, i);
        }

        /**
         * Draw text anti-aliased.
         * @param g graphics context
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g2);
        }
    }


    /**
     * A black border on the left and top edges
     */
    private final class SPBorder extends LineBorder {

        /**
         * Constructor
         */
        SPBorder() {
            super(Color.black);
        }


        /**
         * Paints the border for the specified component with the
         * specified position and size.
         *
         * @param c      the component for which this border is being painted
         * @param g      the paint graphics
         * @param x      the x position of the painted border
         * @param y      the y position of the painted border
         * @param width  the width of the painted border
         * @param height the height of the painted border
         */
        public void paintBorder(Component c, Graphics g, int x,
                                int y, int width, int height) {

            Color oldColor = g.getColor();
            g.setColor(lineColor);
            g.drawLine(x, y, x + width, y);
            g.drawLine(x, y, x, y + height);
            g.setColor(oldColor);
        }
    }


    /**
     * A black border on the top edge
     */
    private final class SPBorder2 extends LineBorder {
        /**
         * Constructor
         */
        SPBorder2() {
            super(Color.black);
        }

        /**
         * Paints the border for the specified component with the
         * specified position and size.
         *
         * @param c      the component for which this border is being painted
         * @param g      the paint graphics
         * @param x      the x position of the painted border
         * @param y      the y position of the painted border
         * @param width  the width of the painted border
         * @param height the height of the painted border
         */
        public void paintBorder(Component c, Graphics g, int x,
                                int y, int width, int height) {

            Color oldColor = g.getColor();
            g.setColor(lineColor);
            g.drawLine(x, y, x + width, y);
            g.setColor(oldColor);
        }
    }

    /**
     * A window to hold help text
     */
    private final class HelpWindow extends JFrame {

        /**
         * Constructor
         */
        HelpWindow() {
            super("About Naive Bayes Vis");
            JEditorPane jep = new JEditorPane("text/html", getHelpString());
            jep.setBackground(yellowish);
            getContentPane().add(new JScrollPane(jep));
            setSize(400, 400);
        }
    }


    /**
     * Return a string with useful info for the help window.
     * @return help window text
     */
    private static final String getHelpString() {

        StringBuffer sb = new StringBuffer(
                "<html><h1>Naive Bayes Vis Help</h1>");
        sb.append(
                "<p>Overview: Naive Bayes Vis provides an interactive evidence ");
        sb.append("visualization for a Naive Bayes Model.");
        sb.append(
                "<p>Detailed Description: This evidence visualization shows the data ");
        sb.append(
                "the Naive Bayes Model uses to make its predictions.  The window ");
        sb.append(
                "is split into two panes.  The left pane contains the Attributes and ");
        sb.append(
                "Evidence.  The right side contains the Conclusion.  Evidence items ");
        sb.append(
                "can be selected to update the Conclusion.  The Evidence can be scaled ");
        sb.append("by right-clicking the mouse and dragging up or down.");
        sb.append("<hr> <p>");
        sb.append(
                "Attributes and Evidence: The attributes (inputs) used to train the ");
        sb.append(
                "Naive Bayes Model are displayed on the far left.  The attributes to ");
        sb.append(
                "show and the sorting order can be changed using menu options.  The ");
        sb.append(
                "evidence items for an attribute are listed next to the attribute ");
        sb.append(
                "name.  These are displayed as pie charts by default.  They can be ");
        sb.append(
                "changed to bar charts using a menu option.  Above each pie chart is a ");
        sb.append(
                "line showing the relevance for each pie chart.  This is the ratio of ");
        sb.append(
                "examples that fall into this bin to the number of total examples. ");
        sb.append(
                "The bar can be changed to a percentage using a menu option.  The user ");
        sb.append(
                "can select an evidence item by clicking on it to update the conclusion. ");
        sb.append(
                "<p>Conclusion: The conclusion shows the probabilities of prediction ");
        sb.append(
                "of the outputs given the selected evidence items.  The Legend shows the ");
        sb.append(
                "colors of each of the unique outputs and its percentage of the ");
        sb.append(
                "Conclusion.  The colors can be changed using a menu option.  The lowest ");
        sb.append(
                "portion of the Conclusion shows information about the evidence item ");
        sb.append(
                "currently under the mouse cursor.  This shows the name, the number ");
        sb.append(
                "of records that fall into this bin, and the breakdown of the items ");
        sb.append("by class. ");
        sb.append("<hr> ");
        sb.append("Menu Options: ");
        sb.append("<ul> ");
        sb.append("<li>Options: ");
        sb.append("	<ul> ");
        sb.append(
                "	<li>Views: Toggle the views between pie charts and bar charts. ");
        sb.append(
                "	<li>Sort Attributes By: Sort the attributes by either the best  ");
        sb.append(
                "	predictor or alphabetical order.  The best predictor is the ");
        sb.append("	attribute that induces the largest error when omitted. ");
        sb.append("	<li>Show Attributes: Select which attributes to display. ");
        sb.append("	<li>Set Colors: Select the colors for each of the unique  ");
        sb.append("	outputs. ");
        sb.append(
                "	<li>Show Predictor Values: Display the error induced when the ");
        sb.append("	attribute was omitted from a prediction calculation.  The ");
        sb.append(
                "	attribute with the highest error would be considered to be ");
        sb.append("	the best predictor. ");
        sb.append(
                "	<li>Show Bin Weight Percentage: Show the weights assigned to ");
        sb.append(
                "	each evidence item as a percentage or display a bar with its ");
        sb.append("	weight relative to all other items in its row. ");
        sb.append("	<li>Print: Print the visualization window. ");
        sb.append(
                "     <li>Save as PMML: Saves the model in a PMML format file.");
        sb.append("	</ul> ");
        sb.append("<li>Help: ");
        sb.append("	<ul> ");
        sb.append("	<li>Help: Show this help window. ");
        sb.append("	</ul> ");
        sb.append("</ul> ");
        sb.append("<hr> ");
        sb.append("Toolbar Options: ");
        sb.append("<ul> ");
        sb.append("<li>Reset View: Reset the evidence to the default size. ");
        sb.append("<li>Print: Print this visualization. ");
        sb.append(
                "<li>Zoom: When this button is toggled on, left-click the evidence ");
        sb.append("to zoom in, or right-click the evidence to zoom out. ");
        sb.append("</ul> ");
        sb.append("</html> ");
        return sb.toString();
    }
}

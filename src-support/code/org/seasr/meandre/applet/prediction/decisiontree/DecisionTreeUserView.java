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

package org.seasr.meandre.applet.prediction.decisiontree;

import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;

import org.seasr.meandre.applet.Constrain;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.BrushPanel;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.DecisionTreeScheme;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.NavigatorPanel;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.RectangleBorder;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.SearchPanel;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.TreeScrollPane;
import org.seasr.meandre.applet.prediction.decisiontree.widgets.Viewport;
import org.seasr.meandre.support.components.prediction.decisiontree.NominalViewableDTModel;
import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTModel;



/**
 * <p>Title: Decision Tree Visualization</p>
 *
 * <p>Description: A visualization module of decision tree classification</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Automated Learning Group, NCSA</p>
 *
 * @author Lily Dong
 */

class DecisionTreeUserView extends JPanel implements ActionListener {
    /** zoom icon location. */
    static private final String zoomicon = "/icons/zoom.gif";

    /** search icon location. */
    static private final String searchicon = "/icons/search.gif";

    /** home icon location. */
    static private final String homeicon = "/icons/home.gif";

    /** help icon location. */
    static private final String helpicon = "/icons/help.gif";

    /** size of buttons. */
    static private final Dimension buttonsize = new Dimension(22, 22);

    /** number of menu items in pop-out menus */
    static private final int MENUITEMS = 15;

    //~ Methods *****************************************************************
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
            //
            // if your image is in a subdir in the jar then
            //    InputStream is = getClass().getResourceAsStream("img/image.gif");
            //  for example
            //
            BufferedInputStream bis = new BufferedInputStream(is);
            // a buffer large enough for our image
            //
            // can be
            //   byte[] byBuf = = new byte[is.available()];
            //   is.read(byBuf);  or something like that...
            byte[] byBuf = new byte[10000];

            int byteRead = bis.read(byBuf, 0, 10000);
            img = Toolkit.getDefaultToolkit().createImage(byBuf);
            m.addImage(img, 0);
            m.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Image img = Toolkit.getDefaultToolkit().createImage(loc);
        return img;
    }


    /**
     * Describes the purpose of the module.
     *
     * @return <code>String</code> describing the purpose of the module.
     */
    public String getModuleInfo() {
        String s =
                "<p>Overview: Visualize a decision tree. " +
                "<p>Detailed Description: Given a ViewableDTModel, displays the structure " +
                "and contents of the nodes of the decision tree.  The <i>Navigator</i> " +
                "on the left shows a small view of the entire tree.  The main area " +
                "shows an expanded view of the tree. For more information look up the help " +
                "provided in the UI of the module";

        return s;
    }


    /** brushing panel */
    private BrushPanel brushpanel;
    /** panel for buttons */
    private JPanel buttonpanel;
    /** menu items that represent colors */
    //private ColorMenuItem[] coloritems;

    /** color lookup table */
    private Hashtable colortable;
    /** depth menu item */
    private JMenuItem depth;
    /** frame */
    private
    /*JD2KFrame*/ JFrame depthframe;
    /** */
    //private DepthPanel depthpanel;
    /** help button */
    private JButton helpbutton;
    /** help window */
    private transient HelpWindow helpWindow;

    /** menubar */
    private JMenuBar menubar;
    /** navigator panel */
    private NavigatorPanel navigatorpanel;
    /** order table */
    private Hashtable ordertable;
    /** print button */
    //private JButton printbutton;
    /** print tree menu item */
    private JMenuItem printtree;
    /** print window menu item */
    private JMenuItem printwindow;
    /** reset view button */
    private JButton resetbutton;
    /** save as pmml menu item */
    private JMenuItem saveAsPmml;
    /** color scheme */
    private DecisionTreeScheme scheme;
    /** search menu item */
    private JMenuItem search;
    /** search button */
    private JButton searchbutton;
    /** search frame */
    private
    /*JD2KFrame*/ JFrame searchframe;
    /** search panel */
    private SearchPanel searchpanel;
    /** show branch labels checkbox */
    private JCheckBoxMenuItem showlabels;
    /** side panel */
    private JPanel sidepanel;
    /** scroll pane */
    private JScrollPane sidescrollpane;
    /** spacing menu item */
    private JMenuItem spacing;
    /** spacing frame */
    private
    /*JD2KFrame*/ JFrame spacingframe;
    /** spacing panel */
    private SpacingPanel spacingpanel;

    /** toolbar */
    private JPanel toolpanel;
    /** scroll pane holding tree */
    private TreeScrollPane treescrollpane;
    /** zoom checkbox */
    private JCheckBoxMenuItem zoom;
    /** zoom button */
    private JToggleButton zoombutton;
    /** this is the decision tree model */
    private ViewableDTModel model;

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof ColorMenuItem) {
            ColorMenuItem coloritem = (ColorMenuItem) source;
            Color oldcolor = getColor(coloritem.getText());
            Color newcolor =
                    JColorChooser.showDialog(this, "Choose Color",
                                             oldcolor);

            if (newcolor != null) {
                colortable.put(coloritem.getText(), newcolor);

                Enumeration keys = colortable.keys();
                Color[] colors = new Color[colortable.size()];

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    Integer index = (Integer) ordertable.get(key);
                    colors[index.intValue()] = (Color) colortable.get(key);
                }

                scheme.setColors(colors);
                brushpanel.repaint();
                treescrollpane.repaint();
            }
        } else if (source == showlabels) {
            treescrollpane.toggleLabels();

            /*else if (source == zoom) {
             * if (zoom.isSelected()) zoombutton.setSelected(true); else
             * zoombutton.setSelected(false); treescrollpane.toggleZoom(); }*/

        } else if (source == zoombutton) {
            /*if (zoombutton.isSelected())
             * zoom.setSelected(true);  else zoom.setSelected(false);
             */

            treescrollpane.toggleZoom();
        } else if (source == depth) {
            depthframe.getContentPane().removeAll();
            depthframe.getContentPane().add(new DepthPanel(depthframe,
                    treescrollpane));
            depthframe.pack();
            depthframe.setVisible(true);
        } else if (source == spacing) {
            spacingframe.getContentPane().removeAll();
            spacingframe.getContentPane().add(new SpacingPanel(spacingframe,
                    treescrollpane,
                    navigatorpanel));
            spacingframe.pack();
            spacingframe.setVisible(true);
        } else if (source == resetbutton) {
            treescrollpane.reset();
        } else if (source == search || source == searchbutton) {
            searchframe.getContentPane().add(searchpanel);
            searchframe.pack();
            searchframe.setVisible(true);
        } else if (source == saveAsPmml) {
            JFileChooser jfc = new JFileChooser();

            int returnVal = jfc.showSaveDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                try {

                    // get the selected file
                    File newFile = jfc.getSelectedFile();

                    // TODO add back PMML WriteDecisionTreePMML.writePMML(
                    // (DecisionTreeModel) model,
                    // newFile.getAbsolutePath());
                } catch (Exception e) {
                    /*ncsa.d2k.core.gui.ErrorDialog.showDialog(e,
                            "Error Writing PMML");*/
                }
            }
        } else if (source == helpbutton) {
            helpWindow.setVisible(true);
        }
    } // end method actionPerformed

    /**
     * Get a color from color map
     * @param string
     * @return  color
     */
    public Color getColor(String string) {
        return (Color) colortable.get(string);
    }


    /**
     * Supplies the default behavior for getMenu, which is to return null indicating that
     * no menu specific to this component is to be added.
     *
     * @return a menubar specific to this component.
     */
    public Object getMenu() {
        return menubar;
    }


    /**
     * If the <code>preferredSize</code> has been set to a
     * non-<code>null</code> value just returns it.
     * If the UI delegate's <code>getPreferredSize</code>
     * method returns a non <code>null</code> value then return that;
     * otherwise defer to the component's layout manager.
     *
     * @return the value of the <code>preferredSize</code> property
     * @see #setPreferredSize
     * @see javax.swing.plaf.ComponentUI
     */
    public Dimension getPreferredSize() {
        Dimension top = buttonpanel.getPreferredSize();
        Dimension left = navigatorpanel.getPreferredSize();
        Dimension mainarea = treescrollpane.getPreferredSize();

        int width = (int) left.getWidth() + (int) mainarea.getWidth();
        int height = (int) top.getHeight() + (int) mainarea.getHeight();

        if (width > 800) {
            width = 800;
        }

        if (height > 600) {
            height = 600;
        }

        return new Dimension(width, height);
    }

    /**
     * Called by the Meandre Infrastructure to allow the view to perform initialization tasks.
     *
     * @param module The module this view is associated with.
     */
    public void initView() { //ViewModule module) {
        menubar = new JMenuBar();
        helpWindow = new HelpWindow();
    }

    /**
     * Called to pass the inputs received by the module to the view.
     *
     * @param object The object that has been input.
     * @param index The index of the module input that been received.
     */
    public void setInput(Object object, int index) {
        model = (org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTModel)
                object;

        // Menu
        JMenu optionsmenu = new JMenu("Options");
        JMenu viewsmenu = new JMenu("Views");
        JMenu toolsmenu = new JMenu("Tools");

        menubar.add(optionsmenu);
        menubar.add(viewsmenu);
        menubar.add(toolsmenu);

        printtree = new JMenuItem("Print Tree...");
        printtree.addActionListener(this);

        printwindow = new JMenuItem("Print Window...");
        printwindow.addActionListener(this);

        saveAsPmml = new JMenuItem("Save as PMML...");
        saveAsPmml.addActionListener(this);
        saveAsPmml.setEnabled(false);

        if (!(model instanceof NominalViewableDTModel)) {
            saveAsPmml.setEnabled(false);
        }

        // optionsmenu.add(colorsmenu);
        optionsmenu.addSeparator();
        optionsmenu.add(printtree);
        optionsmenu.add(printwindow);
        optionsmenu.add(saveAsPmml);

        depth = new JMenuItem("Maximum Depth...");
        depth.addActionListener(this);

        spacing = new JMenuItem("Node Spacing...");
        spacing.addActionListener(this);

        // zoom = new JCheckBoxMenuItem("Zoom");
        // zoom.addActionListener(this);

        showlabels = new JCheckBoxMenuItem("Show Labels");
        showlabels.setState(true);
        showlabels.addActionListener(this);

        viewsmenu.add(depth);
        viewsmenu.add(spacing);
        viewsmenu.addSeparator();

        // viewsmenu.add(zoom);
        viewsmenu.add(showlabels);

        search = new JMenuItem("Search...");
        search.addActionListener(this);

        if (!(model instanceof NominalViewableDTModel)) {
            search.setEnabled(false);
        }

        toolsmenu.add(search);

        // Tool panel
        toolpanel = new JPanel();

        Image image = getImage(homeicon);
        ImageIcon icon = null;

        if (image != null) {
            icon = new ImageIcon(image);
        }

        if (icon != null) {
            resetbutton = new JButton(icon);
            resetbutton.setMaximumSize(buttonsize);
            resetbutton.setPreferredSize(buttonsize);
        } else {
            resetbutton = new JButton("Reset");
        }

        resetbutton.addActionListener(this);
        resetbutton.setToolTipText("Reset");

        image = getImage(searchicon);
        icon = null;

        if (image != null) {
            icon = new ImageIcon(image);
        }

        if (icon != null) {
            searchbutton = new JButton(icon);
            searchbutton.setMaximumSize(buttonsize);
            searchbutton.setPreferredSize(buttonsize);
        } else {
            searchbutton = new JButton("Search");
        }

        searchbutton.addActionListener(this);
        searchbutton.setToolTipText("Search");

        if (!(model instanceof NominalViewableDTModel)) {
            searchbutton.setEnabled(false);
        }

        image = getImage(zoomicon);
        icon = null;

        if (image != null) {
            icon = new ImageIcon(image);
        }

        if (icon != null) {
            zoombutton = new JToggleButton(icon);
            zoombutton.setMaximumSize(buttonsize);
            zoombutton.setPreferredSize(buttonsize);
        } else {
            zoombutton = new JToggleButton("Zoom");
        }

        zoombutton.addActionListener(this);
        zoombutton.setToolTipText("Zoom");

        image = getImage(helpicon);

        if (image != null) {
            icon = new ImageIcon(image);
        }

        if (icon != null) {
            helpbutton = new JButton(icon);
            helpbutton.setMaximumSize(buttonsize);
            helpbutton.setPreferredSize(buttonsize);
        } else {
            helpbutton = new JButton("H");
        }

        helpbutton.addActionListener(this);
        helpbutton.setToolTipText("Help");

        toolpanel.setLayout(new GridBagLayout());
        Constrain.setConstraints(toolpanel, new JPanel(), 0, 0, 1, 1,
                                 GridBagConstraints.BOTH,
                                 GridBagConstraints.NORTHWEST, 1, 1);
        buttonpanel = new JPanel();
        buttonpanel.setLayout(new GridLayout(1, 5));
        buttonpanel.add(resetbutton);
        //buttonpanel.add(printbutton);
        buttonpanel.add(zoombutton);
        buttonpanel.add(searchbutton);
        buttonpanel.add(helpbutton);
        Constrain.setConstraints(toolpanel, buttonpanel, 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0);

        // Split pane
        brushpanel = new BrushPanel(model);
        brushpanel.setBorder(new RectangleBorder("Node Info"));

        treescrollpane = new TreeScrollPane(model, brushpanel);

        navigatorpanel = new NavigatorPanel(model, treescrollpane);
        navigatorpanel.setBorder(new RectangleBorder("Navigator"));

        depthframe = new /*JD2KFrame*/ JFrame("Maximum Depth");

        spacingframe = new /*JD2KFrame*/ JFrame("Node Spacing");

        searchframe = new /*JD2KFrame*/ JFrame("Search");

        if (model instanceof NominalViewableDTModel) {
            searchpanel = new SearchPanel(treescrollpane, searchframe);
        }

        sidepanel = new JPanel();
        sidepanel.setMinimumSize(new Dimension(0, 0));

        sidepanel.setBackground(DecisionTreeScheme.backgroundcolor);
        sidepanel.setLayout(new GridBagLayout());
        Constrain.setConstraints(sidepanel, navigatorpanel, 0, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(10, 10, 10, 10));
        Constrain.setConstraints(sidepanel, brushpanel, 0, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(10, 10, 10, 10));
        Constrain.setConstraints(sidepanel, new JPanel(), 0, 2, 1, 1,
                                 GridBagConstraints.NONE,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(10, 10, 10, 10));

        sidescrollpane = new JScrollPane(sidepanel);

        JSplitPane splitpane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               sidescrollpane, treescrollpane);
        splitpane.setOneTouchExpandable(true);
        splitpane.setDividerLocation(260);

        add(toolpanel, BorderLayout.NORTH);
        add(splitpane, BorderLayout.CENTER);
        setBackground(Color.red); //DecisionTreeScheme.backgroundcolor);
    } // end method setInput

    private class ColorMenuItem extends JMenuItem {
        ColorMenuItem(String s) {
            super(s);
        }
    }
} // end class DecisionTreeUserView


/**
 * A window to hold the help text
 */
final class HelpWindow extends /*JD2KFrame*/ JFrame {
    HelpWindow() {
        super("About Decision Tree Vis");

        JEditorPane jep = new JEditorPane("text/html", getHelpString());
        getContentPane().add(new JScrollPane(jep));
        setSize(400, 400);
    }


    /**
     * the help string for help info.
     *
     * @return help string
     */
    private String getHelpString() {
        StringBuffer s = new StringBuffer("<html>");
        s.append("<h1>Decision Tree Vis Help</h1>");
        s.append(
                "<p>Overview: Decision Tree Vis is an interactive display of the ");
        s.append("contents of a decision tree.");
        s.append("<hr>");
        s.append(
                "<p>Detailed Description: Decision Tree Vis is comprised of three main ");
        s.append("components: the Main Area, the Navigator, and the Node Info.");
        s.append(
                "<p>The Main Area shows the decision tree.  When the cursor is ");
        s.append(
                "positioned over a node in the tree, the Node Info is updated to show ");
        s.append(
                "the contents of the node.  The main area also shows the branch labels ");
        s.append(
                "of the decision tree.  The labels are displayed approximately halfway ");
        s.append(
                "between the parent and child.  Subtrees in the Main Area can be ");
        s.append(
                "collapsed using the arrow widget next to a node in the tree.  A ");
        s.append(
                "single-click on a node will show an expanded view of the node.  This ");
        s.append(
                "will show the distributions of the outputs at this particular node.");
        s.append(
                "<p>The Navigator displays a smaller view of the Main Area.  The current ");
        s.append(
                "portion of the tree that is displayed by the Main Area is shown by a ");
        s.append("box in the Navigator.");
        s.append(
                "<p>The Node Info shows the distributions of the classified values at ");
        s.append("the node under the mouse cursor.");
        s.append("<hr>");
        s.append("Menu Options:");
        s.append("<ul>");
        s.append("<li>Options");
        s.append("<ul>");
        s.append(
                "	<li>Set Colors: The color used to display each unique classified");
        s.append("	value can be changed.");
        s.append(
                "	<li>Print Tree: The entire tree can be printed using this option.");
        s.append("	This will be the entire contents of the Main Area.");
        s.append(
                "	<li>Print Window: The Decision Tree Window itself can be printed");
        s.append("	using this option.");
        s.append(
                "  <li>Save as PMML: The Decision Tree Model is saved in a PMML file.");
        s.append("	</ul>");
        s.append("<li>Views");
        s.append("	<ul>");
        s.append(
                "	<li>Maximum Depth: The maximum depth of the tree to be shown can");
        s.append(
                "	be selected using the maximum depth option.  Nodes with a depth");
        s.append("	greater than this number will not be displayed.");
        s.append(
                "	<li>Node Spacing: The space in pixels between nodes in the tree");
        s.append("	can be adjusted using this option.");
        s.append(
                "	<li>Show labels: Toggles the display of branch labels in the Main");
        s.append("	Area.");
        s.append("	</ul>");
        s.append("<li>Tools");
        s.append("	<ul>");
        s.append("	<li>Search: ");
        s.append(
                "Searches for nodes that satisfy the logical expression. The expression is the ");
        s.append(
                "logical AND or logical OR of conditions.<BR>The most basic condition is an ");
        s.append(
                "inequality based on the node population, percent, purity or split value. The ");
        s.append(
                "population is the number of records with the given output value. The percent ");
        s.append(
                "is the population of the given output value relative to the total number of ");
        s.append(
                "records. The purity is a measure of the entropy. The split value is the input ");
        s.append("value used to split the node.<BR>The user may add a series of conditions to the Current ");
        s.append(
                "Conditions list. Pairs of conditions can then be selected and logically ");
        s.append("combined clicking on the 'Replace' button.<br>The single remaining condition is then used to search ");
        s.append(
                "the tree. The search result nodes can be visited  by using Next and Previous.");
        s.append("	</ul>");
        s.append("</ul>");
        s.append("<hr>");
        s.append("Toolbar Options:");
        s.append("<ul>");
        s.append("	<li>Reset: Reset the view to the default viewpoint.");
        s.append("	<li>Print Tree: Print the entire contents of the tree.");
        s.append(
                "	<li>Zoom: When this button is toggled, left-click the mouse ");
        s.append(
                "	button to zoom in and right-click the mouse button to zoom out.");
        s.append("	<li>Search: Display the Search interface.");
        s.append("	<li>Help: Show this help window.");
        s.append("</ul>");
        s.append("<html>");

        return s.toString();
    } // end method getHelpString
}


/**
 * Panel to allow someone to input the maximum depth
 */
class DepthPanel extends JPanel implements ActionListener {
    /** apply button */
    JButton apply;
    /** cancel button */
    JButton cancel;
    /** close button */
    JButton close;

    /** depth */
    int depth;
    /** a frame */
    /*JD2KFrame*/JFrame depthframe;
    /** text field */
    JTextField dfield;

    /** label */
    JLabel dlabel;
    /** the scroll pane holding the tree */
    TreeScrollPane treescrollpane;

    /**
     * Constructor
     * @param frame the frame
     * @param scrollpane scollpane holding the tree
     */
    DepthPanel( /*JD2KFrame*/JFrame frame, TreeScrollPane scrollpane) {
        depthframe = frame;
        treescrollpane = scrollpane;

        depth = treescrollpane.getDepth();

        dlabel = new JLabel("Maximum Depth:");

        dfield = new JTextField(Integer.toString(depth), 5);

        apply = new JButton("Apply");
        apply.addActionListener(this);

        close = new JButton("Close");
        close.addActionListener(this);

        cancel = new JButton("Cancel");
        cancel.addActionListener(this);

        JPanel buttonpanel = new JPanel();
        buttonpanel.add(cancel);
        buttonpanel.add(close);
        buttonpanel.add(apply);

        setLayout(new GridBagLayout());
        Constrain.setConstraints(this, dlabel, 0, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, dfield, 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, buttonpanel, 0, 1, 2, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(5, 5, 5, 5));

    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == close) {
            depthframe.setVisible(false);

        }

        if (source == cancel) {
            treescrollpane.setDepth(depth);

            depthframe.setVisible(false);
        }

        if (source == apply) {
            String svalue = dfield.getText();

            try {
                int ivalue = Integer.parseInt(svalue);

                treescrollpane.setDepth(ivalue);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    } // end method actionPerformed
} // end class DepthPanel


/**
 *  Allow someone to change node spacing
 */
class SpacingPanel extends JPanel implements ActionListener {
    /** apply button */
    JButton apply;
    /** cancel button */
    JButton cancel;
    /** close button */
    JButton close;
    /** text field for horizontal */
    JTextField hfield;
    /** label */
    JLabel hlabel;
    /** navigator */
    NavigatorPanel navigatorpanel;
    /** frame */
    /*JD2KFrame*/JFrame spacingframe;
    /** scroll pane holding the tree */
    TreeScrollPane treescrollpane;
    /** text field for vertical */
    JTextField vfield;
    /** viewport */
    Viewport viewroot;
    /** label */
    JLabel vlabel;

    /** horizontal space */
    double xspace;
    /** vertical space */
    double yspace;

    /**
     * Constructor
     * @param frame the frame
     * @param scrollpane scroll pane holding the tree
     * @param navigator navigator
     */
    SpacingPanel( /*JD2KFrame*/JFrame frame, TreeScrollPane scrollpane,
                               NavigatorPanel navigator) {
        spacingframe = frame;
        treescrollpane = scrollpane;
        navigatorpanel = navigator;

        viewroot = treescrollpane.getViewRoot();
        xspace = viewroot.xspace;
        yspace = viewroot.yspace;

        hlabel = new JLabel("Horizontal Spacing:");
        vlabel = new JLabel("Vertical Spacing:");

        hfield = new JTextField(Double.toString(xspace), 5);
        vfield = new JTextField(Double.toString(yspace), 5);

        apply = new JButton("Apply");
        apply.addActionListener(this);

        close = new JButton("Close");
        close.addActionListener(this);

        cancel = new JButton("Cancel");
        cancel.addActionListener(this);

        JPanel buttonpanel = new JPanel();
        buttonpanel.add(cancel);
        buttonpanel.add(close);
        buttonpanel.add(apply);

        setLayout(new GridBagLayout());
        Constrain.setConstraints(this, hlabel, 0, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, hfield, 1, 0, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, vlabel, 0, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, vfield, 1, 1, 1, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 0, 0,
                                 new Insets(5, 5, 5, 5));
        Constrain.setConstraints(this, buttonpanel, 0, 3, 2, 1,
                                 GridBagConstraints.HORIZONTAL,
                                 GridBagConstraints.NORTHWEST, 1, 1,
                                 new Insets(5, 5, 5, 5));

    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == close) {
            spacingframe.setVisible(false);

        }

        if (source == cancel) {
            viewroot.xspace = xspace;
            viewroot.yspace = yspace;

            treescrollpane.rebuildTree();
            navigatorpanel.rebuildTree();

            spacingframe.setVisible(false);
        }

        if (source == apply) {
            String hsvalue = hfield.getText();
            String vsvalue = vfield.getText();

            try {
                double hdvalue = Double.parseDouble(hsvalue);
                double vdvalue = Double.parseDouble(vsvalue);

                viewroot.xspace = hdvalue;
                viewroot.yspace = vdvalue;

                treescrollpane.rebuildTree();
                navigatorpanel.rebuildTree();

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    } // end method actionPerformed
} // end class SpacingPanel

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

package org.seasr.meandre.applet.ruleassociation;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.ComponentNatures;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

/**
 * <p>Title: Communication Module</p>
 *
 * <p>Description: A component used to communicate between WebUI and the visualization applet</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Automated Learning Group, NCSA</p>
 *
 * @author Lily Dong
 */

@Component(creator="Lily Dong",
           description="This module provides a visual representation of the association rules encapsulated in the " +
           "input Rule Table. " +
           "Detailed Description: " +
           "This module presents a visual representation of association rules identified by " +
           "a discovery algorithm. " +
           "Seasr includes several modules that implement association rule discovery algorithms, " +
           "all of which save their results in a Rule Table structure that can be used as " +
           "input to this module. " +
           "The main region of the display contains a matrix that visually depicts the rules. " +
           "Each numbered column in the matrix corresponds to an association rule  " +
           "that met the minimum support and confidence requirements specified by the user in the " +
           "rule discovery modules. " +
           "Items used in the rules, that is attribute-value pairs, are listed along the left  " +
           "side of the matrix. " +
           "Note that some items in the original data set may not be included in any rule " +
           "because there was insufficient support and/or confidence to consider the item " +
           "significant. " +
           "An icon in the matrix cell corresponding to ( row = item i, column = rule r) " +
           "indicates that item i is included in rule r. " +
           "If the matrix cell icon is a box, then the item is part of the rule antecedent. If " +
           "the icon is a check mark, then the item is part of the rule consequent. " +
           "For example, if the rules being displayed indicate whether or not a mushroom is edible, " +
           "a rule might be odor=none and ring_number=one then edibility=edible. " +
           "This rule would be displayed in a column with a box in the row for the item odor=none " +
           "and a box in the row for ring_number=one, and there would be a check in the " +
           "row for edibility=edible. " +
           "Above the main matrix are two rows of bars labeled Confidence and Support. " +
           "These bars align with the corresponding rule columns in the main matrix.  For any given rule, " +
           "the confidence and support values for that rule are represented by the degree to which the " +
           "bars above the rule column are filled in.   Brushing the mouse on a confidence or support " +
           "bar displays the exact value that is graphically represented by the bar height. " +
           "The rules can be ordered by confidence or by support. " +
           "To sort the rules, click either the support or the confidence label -- " +
           "these labels are clickable radio buttons. " +
           "If support is selected, rules will be sorted using support as the primary key and confidence as the secondary key. " +
           "Conversely, if the confidence button is chosen, confidence is the primary sort key and support is the secondary key.  " +
           "Directly above the confidence and support display is a toolbar that provides additional functionality.  " +
           "On the left side of the toolbar are two buttons that allow the rows of the table to be displayed " +
           "according to different sorting schemes. One of the buttons is active at all times. " +
           "The Alphabetize button sorts the attribute-value pairs alphabetically.  " +
           "The Rank button sorts the rows based on the current Confidence/Support selection, moving the " +
           "consequents and antecedents of the highest ranking rules to the " +
           "top of the attribute-value list. " +
           "On the right side of the toolbar are four additional buttons: " +
           "Restore Original reverts " +
           "back to the original table that was displayed before any sorting was done. " +
           "Filter provides an interface that allows the user to display a subset of the generated rules. " +
           "The user can scroll to different part of the matrix " +
           "to get the full picture of a large matrix. " +
           "Help displays information describing the visualization. " +
           "Scalability: " +
           "While the visualization can display a large number of items and rules, there can be a noticeable delay "  +
           "in opening the visualization when a large number of cells are involved. "  +
           "Also, as the number of cells increases beyond " +
           "a certain point, it is difficult to gain insights from the display.  Advanced features to help " +
           "in these cases are being discussed for a future release.",
           name="WebRuleVis",
           tags="rule association, frequent pattern mining, visualization",
           mode=Mode.webui,
           dependency={"icons.jar","foundry-datatype-datamining.jar", "foundry-datatype-core.jar", "dom4j-1.6.1.jar", "jaxen-1.1.1.jar", "trove-2.0.3.jar"},
           baseURL="meandre://seasr.org/components/data-mining/")

@ComponentNatures( natures={
        @ComponentNature(type="applet",
        extClass=org.seasr.meandre.applet.ruleassociation.WebRuleVisApplet.class
)})

public class WebRuleVis implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentInput(description="Read org.dom4j.Document converted from a representation of associatoion rule to be displayed.",
                    name= "document")
    final static String DATA_INPUT = "document";

    @ComponentProperty(defaultValue="true",
                       description="Control whether debugging information is output to the console",
                       name="verbose")
    final static String DATA_PROPERTY = "verbose";

    /** The blocking semaphore */
    private final Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;
    private String webUIUrl = null;

    private boolean verbose = true;

    //private RuleTable ruleTable;

    private Document document;

    /**
     * Called when a flow is started.
     *
     * @param ccp ComponentContextProperties
     */
    public void initialize(ComponentContextProperties ccp) {}

    /**
    * Called at the end of an execution flow.
    *
    * @param ccp ComponentContextProperties
    */
    public void dispose(ComponentContextProperties ccp) {}

    /** This method gets call when a request with no parameters is made to a
     * component WebUI fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem encountered during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (IOException e) {
            throw new WebUIException(e);
        }
    }

    /** A simple message.
     *
     * @return The HTML containing the page
     */
    private String getViz() {
        StringBuffer sb = new StringBuffer();
        sb.append("<html> ");
        sb.append("<body> ");
        sb.append("<p ALIGN='center'> ");
        sb.append("<APPLET ");
        sb.append("ARCHIVE='org.seasr.meandre.applet.ruleassociation.webrulevisapplet.jar, foundry-datatype-datamining.jar, foundry-datatype-core.jar, dom4j-1.6.1.jar, jaxen-1.1.1.jar, trove-2.0.3.jar, icons.jar' WIDTH='800' HEIGHT='600' ");

        sb.append("CODEBASE='" + webUIUrl + "public/resources/contexts/java/' ");
        sb.append("CODE='org.seasr.meandre.applet.ruleassociation.WebRuleVisApplet.class'> ");
        sb.append("<PARAM name='servletURL' value='" + webUIUrl).append(sInstanceID).append("'> ");
        sb.append("</APPLET> ");
        sb.append("</p> ");
        sb.append("<br/><br/> ");
        sb.append("<div align='center'> ");
        sb.append("<table align='center'><font size='2'> <a id='url' href='" + webUIUrl +
                  sInstanceID + "?done=true'>DONE</a></font></table> ");
        sb.append("</div> ");
        sb.append("</body> ");
        sb.append("</html> ");
        return sb.toString();
    }

    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        String theApplet = request.getParameter("applet");
        if (sDone != null)
            sem.release();
        else if (theApplet != null)
            try {
                ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
                //out.writeObject(ruleTable);
                out.writeObject(document);
                out.flush();
                out.close();
            } catch (Exception ex) {
                throw new WebUIException(ex);
            }
        else
            emptyRequest(response);
    }

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        verbose = Boolean.valueOf(cc.getProperty(DATA_PROPERTY));

        Object theOb = cc.getDataComponentFromInput(DATA_INPUT);

        //ruleTable = (RuleTable) theOb;
        document = (Document)theOb;

        sInstanceID = cc.getExecutionInstanceID();
        webUIUrl = cc.getWebUIUrl(true).toString();
        if (!webUIUrl.endsWith("/")) webUIUrl += "/";

        try {
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            sem.release();
        } catch (InterruptedException iex) {
            throw new ComponentExecutionException(iex);
        }

        cc.stopWebUIFragment(this);
        System.out.flush();
    }

}

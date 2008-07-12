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

package org.meandre.applet.prediction.decisiontree;

import org.meandre.core.ExecutableComponent;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;

import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.ComponentNatures;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.Mode;

import org.meandre.components.prediction.decisiontree.support.ViewableDTModel;

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

/**
 * <p>Overview: Visualize a decision tree.
 * <p>Detailed Description: Given a ViewableDTModel, displays the structure
 * and contents of the nodes of the decision tree.  The <i>Navigator</i>
 * on the left shows a small view of the entire tree.  The main area
 * shows an expanded view of the tree. For more information look up the help
 * provided in the UI of the module
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2886 $, $Date: 2006-08-11 09:39:04 -0500 (Fri, 11 Aug 2006) $
 */

@Component(creator="Lily Dong",
           description= "Given a ViewableDTModel, displays the structure and contents of the nodes of the decision tree. " +
           "The Navigator on the left shows a small view of the entire tree. " +
           "The main area shows an expanded view of the tree. " +
           "For more information look up the help provided in the UI of the module",
           name="WebDecisionTreeVis",
           tags="decision tree, visualization",
           mode=Mode.webui)

@ComponentNatures( natures={
        @ComponentNature(type="applet",
        extClass=org.meandre.applet.prediction.decisiontree.support.WebDecisionTreeVisApplet.class,
        dependency={"icons.jar"}
)})


public final class WebDecisionTreeVis implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentInput(description="Read a decision tree model implementing " +
                    "org.meandre.applet.prediction.decisiontree.ViewableDTModel interface.",
                    name= "vdtModel")
    final static String DATA_INPUT = "vdtModel";

    @ComponentProperty(defaultValue="true",
                       description="Control whether debugging information is output to the console.",
                       name="verbose")
    final static String DATA_PROPERTY= "verbose";

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;

    private boolean verbose = true;

    private ViewableDTModel model;

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
        sb.append("<html>\n");
        sb.append("<body>\n");
        sb.append("<p ALIGN=center >\n");
        sb.append("<APPLET\n");
        sb.append("ARCHIVE=\"org.meandre.applet.prediction.decisiontree.support.webdecisiontreevisapplet.jar, icons.jar\" WIDTH=\"800\"HEIGHT=\"600\"\n");

        sb.append("CODEBASE=\"public/resources/contexts/java\"\n");
        sb.append("CODE=\"org.meandre.applet.prediction.decisiontree.support.WebDecisionTreeVisApplet.class\">\n");
        sb.append("<PARAM name=\"servletURL\" value=\"").append(sInstanceID).
                append("\">\n");
        sb.append("</APPLET>\n");
        sb.append("</p>\n");
        sb.append("<br /><br />\n");
        sb.append("<div align=\"center\">\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                  sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
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
                out.writeObject(model);
                out.flush();
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
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

        Object theOb = (Object) cc.getDataComponentFromInput(DATA_INPUT);
        model = (ViewableDTModel)theOb;

        sInstanceID = cc.getExecutionInstanceID();

        try {
            sem.acquire();
            cc.startWebUIFragment(this);
            sem.acquire();
            sem.release();
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        cc.stopWebUIFragment(this);
        System.out.flush();
    }

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
}

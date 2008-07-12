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

package org.meandre.applet.prediction.naivebayes;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.ComponentNatures;
import org.meandre.annotations.Component.Mode;

import java.util.concurrent.Semaphore;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.meandre.webui.WebUIException;

import javax.servlet.http.HttpServletRequest;
import java.io.ObjectOutputStream;

import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIFragmentCallback;

import org.meandre.components.prediction.naivebayes.support.NaiveBayesModel;

@Component(creator="Lily Dong",
           description="Overview: An evidence visualization for a NaiveBayesModel. " +
           "Detailed Description: This evidence visualization shows pie charts " +
           "that represent the different bins used.  The slices of the pie chart " +
           "represent the ratios of the output classes for that particular bin. " +
           "Selecting a chart in the evidence section will update the conclusion " +
           "pie chart.  The conclusion shows the probability that each output has " +
           "for classification given all the selected evidence charts." +
           "Data Type Restrictions: none" +
           "Data Handling:  This module does not destroy or modify the " +
           "input data." +
           "Scalability: This module keeps data structures to represent the evidence " +
           "charts.  The amount of memory required is proportional to the number of " +
           "bins used in the discretization process.",
           name="NaiveBayesVis",
           tags="naive bayes, visualization",
           mode=Mode.webui)

@ComponentNatures( natures={
        @ComponentNature(type="applet",
        extClass=org.meandre.applet.prediction.naivebayes.support.NBApplet.class,
        dependency={"icons.jar"}
)})

/**
 * An evidence visualization for a NaiveBayesModel.
 *
 * @author  Lily Dong
 */
public final class NaiveBayesVis implements ExecutableComponent ,WebUIFragmentCallback{
    @ComponentInput(description="Read org.meandre.components.prediction.naivebayes.NaiveBayesModel to visualize.",
                       name= "nbModel")
    public final static String DATA_INPUT = "nbModel";

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;

    private NaiveBayesModel model;

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
        sb.append(
                "ARCHIVE=\"org.meandre.applet.prediction.naivebayes.support.nbapplet.jar, icons.jar\" WIDTH=\"800\"HEIGHT=\"600\"\n");

        sb.append("CODEBASE=\"public/resources/contexts/java\"\n");
        sb.append("CODE=\"org.meandre.applet.prediction.naivebayes.support.NBApplet.class\">\n");
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
                ObjectOutputStream out = new ObjectOutputStream(response.
                        getOutputStream());
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
        Object theOb = (Object) cc.getDataComponentFromInput(DATA_INPUT);
        model = (NaiveBayesModel) theOb;

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

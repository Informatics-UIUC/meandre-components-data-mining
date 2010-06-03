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

package org.seasr.meandre.applet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentNature;
import org.meandre.annotations.ComponentNatures;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.datatypes.datamining.table.PredictionTable;

/**
 * Displays statistics about any PredictionTable.  The number of correct
 * predictions and a confusion matrix are included.
 * @author David Clutter
 * @author Lily Dong
*/

@Component(creator="Lily Dong",
           description="Provides a visualization " +
           "to evaluate the performance of a predictive model." +
           "Detailed Description: Given a PredictionTable with both " +
           "the predictions and the actual values, this module will " +
           "provide a simple visualization to evaluate the performance of " +
           "a predictive model on a data set.  The accuracy, defined " +
           "as the number of correct predictions, is displayed.  A pie chart " +
           "depicting the accuracy is also shown.  A confusion matrix for " +
           "the PredictionTable is created.  The confusion matrix displays " +
           "the precision and recall of the predictive model. " +
           "Precision is defined as the number of correct predictions " +
           "within a class divided by the number of predictions within a " +
           "class.  Recall is the number of relevant predictions " +
           "within a class divided by the number that actually exist in " +
           "a class.  The confusion matrix also displays the Type I " +
           "and Type II errors.  Type I error is defined as " +
           "accepting an item as a member of a class when it is actually " +
           "false, known as a false positive.  Type II error is " +
           "defined as rejecting an item as a member of class when it is " +
           "actually true, known as a false negative.  The confusion " +
           "matrix is shown with the unique predictions along the top. " +
           "These are labeled Prediction.  The actual values are " +
           "displayed along the side.  These are labeled Ground Truth." +
           "Data Type Restrictions: A PredictionTable with both the " +
           "predictions and actual values is required.  This module only " +
           "supports classification predictions.  Continuous predictions " +
           "are not supported." +
           "Data Handling: This module does not modify the input data." +
           "Scalability: This module makes one pass over the data to " +
           "count the number of correct and incorrect predictions.",
           name="PredictionTableReport",
           tags="prediction, visualization",
           mode=Mode.webui,
           dependency={"foundry-datatype-datamining.jar","foundry-datatype-core.jar", "trove-2.0.3.jar"},
           baseURL="meandre://seasr.org/components/data-mining/")

@ComponentNatures( natures={
        @ComponentNature(type="applet",
        extClass=org.seasr.meandre.applet.PredApplet.class
)})

public class PredictionTableReport  implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentInput(description="Read org.seasr.datatypes.datamining.table.PredictionTable " +
                    "with both the actual values and predictions as input.",
                    name= "predictionTable")
    public final static String DATA_INPUT = "predictionTable";

    /** The blocking semaphore */
    private final Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;

    private PredictionTable pt;

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
                "ARCHIVE=\"org.seasr.meandre.applet.predapplet.jar, foundry-datatype-datamining.jar, foundry-datatype-core.jar, trove-2.0.3.jar\" WIDTH=\"800\"HEIGHT=\"600\"\n");
        sb.append("CODEBASE=\"public/resources/contexts/java\"\n");
        sb.append(
                "CODE=\"org.seasr.meandre.applet.PredApplet.class\">\n");
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
                out.writeObject(pt);
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
     * @throws ComponentExecutionException An exception occurred during execution
     * @throws ComponentContextException Illegal access to context
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        Object theOb = cc.getDataComponentFromInput(DATA_INPUT);
        pt = (PredictionTable) theOb;

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

/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.javascript;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Mode;

import java.util.Enumeration;
import java.util.StringTokenizer;

import org.seasr.datatypes.datamining.table.Table;

/**
* <p>
* Title: Search Visualization.
* </p>
*
* <p>
* Description: This executable component presents a search box for user to input query.
* </p>
*
* <p>
* Copyright: Copyright (c) 2008
* </p>
*
* <p>
* Company: Automated Learning Group, NCSA
* </p>
*
* @author Lily Dong
* @version 1.0
*/

@Component(creator="Lily Dong",
           description="Present a search box for user to input query.",
           name="SearchBox",
           tags="search, visualization",
           mode=Mode.webui,
           baseURL="meandre://seasr.org/components/data-mining/")

public class SearchBox implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentOutput(description="Output the query passed from the serach box.",
                     name="message")
    public final static String DATA_OUTPUT = "message";

    /*
     * Store the message imported by user.
     */
    private String query = "Hello World!";


   /** The blocking semaphore */
   private Semaphore sem = new Semaphore(1,true);

   /** The instance ID */
   private String sInstanceID = null;

   /** This method gets call when a request with no parameters is made to a
    * component webui fragment.
    *
    * @param response The response object
    * @throws WebUIException Some problem arised during execution and something went wrong
    */
   public void emptyRequest(HttpServletResponse response) throws
           WebUIException {
       try {
           response.getWriter().println(getViz());
       } catch (Exception e) {
           throw new WebUIException(e);
       }
   }

   /** A simple message.
    *
    * @return The html containing the page
    */
   private String getViz() {
       StringBuffer sb = new StringBuffer();

       sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
       sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
       sb.append("<head>\n");
       sb.append("<title>Search</title>\n");
       sb.append("<style type=\"text/css\" media=\"screen\">\n");
       sb.append("body {\n");
       sb.append("font-family: Verdana, sans-serif;\n");
       sb.append("font-size: 1em;\n");
       sb.append("}\n");
       sb.append("input.searchsubmit\n");
       sb.append("{\n");
       sb.append("background-color: #e6EEEE;\n");
       sb.append("}\n");
       sb.append("input {\n");
       sb.append("font-family: Verdana, sans-serif;\n");
       sb.append("font-size: 0.9em;\n");
       sb.append("padding: 5px;\n");
       sb.append("border: 1px solid #666;\n");
       sb.append("}\n");
       sb.append("</style>\n");
       sb.append("</head>\n");

       sb.append("<body>\n");
       sb.append("<br /><br />\n");
       sb.append("<div align=\"center\">\n");
       sb.append("<form name=\"input\" method=\"get\" action=\"/" + sInstanceID + "\">\n");
       sb.append("<input type=\"text\" name=\"user\" value=\"\" size=\"80\"/>\n");
       sb.append("<input type=\"submit\" class=\"searchsubmit\" value=\"Search\" />\n");
       sb.append("</form>\n");
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
    * @throws WebUIException A problem arised during the call back
    */
   public void handle(HttpServletRequest request, HttpServletResponse response) throws
           WebUIException {
       query = request.getParameter("user");
       if(query != null)
           sem.release();
       else
           emptyRequest(response);
   }

   /**
    * Call at the end of an execution flow.
    */
   public void initialize(ComponentContextProperties ccp) {
   }

   /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
   public void execute(ComponentContext cc) throws ComponentExecutionException,
           ComponentContextException {
       try {
           sInstanceID = cc.getExecutionInstanceID();
           sem.acquire();
           cc.startWebUIFragment(this);
           sem.acquire();
           cc.stopWebUIFragment(this);

           cc.pushDataComponentToOutput(DATA_OUTPUT, query);
       } catch (Exception e) {
           throw new ComponentExecutionException(e);
       }
   }

   /**
    * Called when a flow is started.
    */
   public void dispose(ComponentContextProperties ccp) {
   }
}

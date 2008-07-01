/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright © 2008, NCSA.  All rights reserved.
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

package org.meandre.javascript;

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

import java.util.Enumeration;
import java.util.StringTokenizer;

import org.meandre.components.datatype.table.Table;

/**
* <p>
* Title: Table Visualization.
* </p>
*
* <p>
* Description: This executable component visualizes table.
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
          description="Visualize table using jQuery.",
          name="tableViewer",
          tags="table, visualization")

public class TableViewer implements ExecutableComponent, WebUIFragmentCallback {
   @ComponentInput(description="Read org.meandre.components.datatype.table.Table as input.",
                   name= "table")
   final static String DATA_INPUT = "table";

   /** The blocking semaphore */
   private Semaphore sem = new Semaphore(1,true);

   /** The message to print */
   private Table table = null;

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
       
       sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n"); 
       sb.append("\"http://www.w3.org/TR/html4/loose.dtd\">\n");
       sb.append("<html>\n");
       sb.append("<head>\n");
       sb.append("<script src=\"http://code.jquery.com/jquery-latest.js\"></script>\n");
       sb.append("<script>\n");
       sb.append("$(document).ready(function(){\n");
       sb.append("$(\"#myTable\").tablesorter({sortList:[[0,0]],widgets:['zebra']});\n");
       sb.append("})\n");
       sb.append("</script>\n");
       sb.append("</head>\n");
       
       sb.append("<body>\n");
       sb.append("<link rel=\"stylesheet\" href=\"http://dev.jquery.com/view/trunk/themes/flora/flora.all.css\" type=\"text/css\" media=\"screen\" title=\"Flora (Default)\">\n");
       sb.append("<script src=\"http://tablesorter.com/jquery-latest.js\"></script>\n");
       sb.append("<script src=\"http://tablesorter.com/jquery.tablesorter.js\"></script>\n");

       sb.append("<link rel=\"stylesheet\" href=\"http://tablesorter.com/themes/blue/style.css\" type=\"text/css\" media=\"print, projection, screen\" />\n");
       
       sb.append("<table id=\"myTable\" class=\"tablesorter\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\">\n");
       sb.append("<thead>\n");
       sb.append("<tr>\n");
       int nrColumns = table.getNumColumns();
       for(int column=0; column<nrColumns; column++) {
           String theName = table.getColumnLabel(column);
           sb.append("<th>").append(theName).append("</th>\n");
       }
       sb.append("</tr>\n");
       sb.append("</thead>\n");
       
       sb.append("<tbody>\n");
       int nrRows = table.getNumRows();
       for(int row=0; row<nrRows; row++) {
           sb.append("<tr>\n");
           for(int column=0; column<nrColumns; column++)
              sb.append("<td>").append(table.getString(row, column)).append("</td>\n");
           sb.append("</tr>");
       }   
       sb.append("</tbody>\n");
       sb.append("</table>\n"); 
       
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
    * @throws WebUIException A problem arised during the call back
    */
   public void handle(HttpServletRequest request, HttpServletResponse response) throws
           WebUIException {
       String sDone = request.getParameter("done");
       if (sDone != null) {
           sem.release();
       } else
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
           table  = (Table) cc.getDataComponentFromInput(DATA_INPUT);
           sInstanceID = cc.getExecutionInstanceID();
           sem.acquire();
           cc.startWebUIFragment(this);
           sem.acquire();
           cc.stopWebUIFragment(this);
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

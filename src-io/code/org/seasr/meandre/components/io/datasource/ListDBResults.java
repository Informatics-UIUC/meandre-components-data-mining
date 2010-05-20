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

package org.seasr.meandre.components.io.datasource;

//import statements
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.Semaphore;

import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.Mode;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.meandre.webui.WebUIFragmentCallback;
import java.io.IOException;
import org.meandre.webui.WebUIException;

import javax.servlet.http.*;

@Component(creator="Erik Johnson",
        description="<p>Overview:<br>"
        	+"This component accepts a resultset object generated by a query to a database."
        	+" The component launches a webUI and displayes the resultset as a table."
        	+" Each column in the table refers to a column in the resultset and each row to"
        	+" an enrty in the resultset.",
        name="List Database ResultSet",
        tags="database, table",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/data-mining/")

/** This components lists the contents of a resultset
 *
 * @author Erik Johnson
 */
public class ListDBResults implements ExecutableComponent, WebUIFragmentCallback {

	private ResultSet results;

	private Semaphore sem = new Semaphore(1, true);

	    /** The instance ID */
	private String sInstanceID = null;

	private Logger logger;

	@ComponentInput(
		 description = "Resultset to display.",
		 name = "ResultSet")
	final static String DATA_INPUT = "ResultSet";

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

    /** HTML Page.
     *
     * @return The HTML containing the page
     */
    private String getViz() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<html>\n");
        sb.append("<body>\n");

        //display results of a query of all tables
    	try{
    	//get everything from every existing table
    	ResultSetMetaData rsmd = results.getMetaData();
        sb.append("<h1>Resultset Values</h1>\n");
        sb.append("<table border=\"1\" width=\"100%\" cellpadding=\"10\">\n");
        
    	sb.append("<tr>");
    	for (int i=1; i<=rsmd.getColumnCount(); i++)
    	{
			sb.append("<th>"+rsmd.getColumnLabel(i)+"</th> \n");

    	}
    	sb.append("</tr>");
        
//        sb.append("<tr>\n");
    	
        while (results.next())
        {
//        	sb.append("<td width=\"40%\" valign=\"top\">\n");
//        	sb.append("<td>\n");
        	
        	sb.append("<tr>\n");
//        	Object testObj = null;
        	for (int i=1; i<=rsmd.getColumnCount(); i++)
        	{
        		sb.append("<td>\n");
        		//
        		//testObj=results.getObject(i);
        		results.getObject(i);
        		if(results.wasNull())
        			sb.append("NULL");
        		else
        			sb.append(results.getObject(i).toString());
        		//
        		sb.append("</td>\n");
//        		if (testObj != null)
//        			sb.append("<p>"+rsmd.getColumnLabel(i)+" : "+results.getObject(i).toString()+"</p><br />\n");
//        		else
//        			sb.append("<p>"+rsmd.getColumnLabel(i)+" : NULL</p><br /> \n");
        	}
//        	sb.append("</td>\n");
        	sb.append("</tr>\n");
        }
    	}
    	catch(SQLException e)
    	{
    		logger.log(Level.SEVERE,"Error accessing database. "+e);
    		e.printStackTrace();
    	}
    	catch(NullPointerException e)
    	{
    		logger.log(Level.SEVERE,"Error accessing data. "+e);
    		e.printStackTrace();
    	}
        sb.append("</tr>\n");
        sb.append("</table>\n");
        sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
                sInstanceID + "?done=true\">DONE</a></font></table>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }


    /** This method gets called when a call with parameters is done to a given component
     * webUI fragment
     *
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
    	String sDone = request.getParameter("done");
    	if ( sDone!=null ) {
			sem.release();
		}
		else
			emptyRequest(response);
    }

     /** This method is invoked when the Meandre Flow is being prepared for
      * getting run.
      *
      * @param ccp The component context properties
      */
     public void initialize ( ComponentContextProperties ccp ) {
    	 logger = ccp.getLogger();
     }

     /** This components lists the contents of a resultset
      *
      * @throws ComponentExecutionException If a fatal condition arises during
      *         the execution of a component, a ComponentExecutionException
      *         should be thrown to signal termination of execution required.
      * @throws ComponentContextException A violation of the component context
      *         access was detected
      */
     public void execute(ComponentContext cc)
     throws ComponentExecutionException, ComponentContextException {

    		results = (ResultSet) cc.getDataComponentFromInput(DATA_INPUT);

    	 	//start web ui
    		logger.log(Level.INFO,"Firing the web ui component");
    		sInstanceID = cc.getExecutionInstanceID();
    		try {

    			sem.acquire();
    			logger.log(Level.INFO,">>>Rendering...");
    			cc.startWebUIFragment(this);
    			logger.log(Level.INFO,">>>STARTED");
    			sem.acquire();
    			sem.release();
    			logger.log(Level.INFO,">>>Done");
    			
    			cc.requestFlowAbortion();

    		}
    		catch ( Exception e ) {
    			throw new ComponentExecutionException(e);
    		}
    		//pass connection back out to be used again or closed by another component
    		cc.stopWebUIFragment(this);
     }

     /** This method is called when the Meandre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}
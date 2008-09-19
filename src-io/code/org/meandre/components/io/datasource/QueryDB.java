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

package org.meandre.components.io.datasource;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.sql.ResultSet;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Erik Johnson",
        description="Enter a Query for the Database",
        name="QueryDB",
        tags="database")

/** This component allows a user to enter and run an SQL query that produces a result set. The query can be entered as a property string or typed by the user at runtime.
 *
 * @author Erik Johnson
 */
public class QueryDB implements ExecutableComponent, WebUIFragmentCallback  {

	/** The blocking semaphore */
    private Semaphore sem = new Semaphore(1, true);

    /** The instance ID */
    private String sInstanceID = null;

    private String sqlQuery;
    
    private Connection conn;
    
    private Statement stmt;

    private int type = ResultSet.TYPE_FORWARD_ONLY;
    
    private int concurrency = ResultSet.CONCUR_READ_ONLY;
    
    private ResultSet results;
    
    private Logger logger;
    
    //Connection Input
    @ComponentInput(
	 		description = "The input connection",
	 		name = "Connection")
	final static String DATA_INPUT = "Connection";
    
    //Resultset Output
    @ComponentOutput(
	 		description = "The result of the executed query",
	 		name = "Result")
	final static String DATA_OUTPUT1 = "Result";
    
    //Connection output
    @ComponentOutput(
	 		description = "Connection out",
	 		name = "ConnectionOut")
	final static String DATA_OUTPUT2 = "ConnectionOut";
       
    //This component property points to an xml file chosen by the user to store and load JNDI objects
	@ComponentProperty(description="Full path to a text file with an sql query (i.e. C:/myquery.sql). This file will be parsed for an sql query. Lines beginning with // will be ignored as comments. The first semicolon will be treated as the end of the file; this component does not support multiple simultaneous queries. If the value \"none\" is specified, a webUI will be started to allow the user to enter a query manually.",
            	name="Query",
            	defaultValue="none")
    final static String DATA_PROPERTY = "Query";
    
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

        sb.append("<h1>Query Constructor</h1>\n");
        sb.append("<table border=\"0\" width=\"100%\" cellpadding=\"10\">\n");
        sb.append("<tr>\n");

        sb.append("<td width=\"100%\" valign=\"top\">\n");
        
        sb.append("<form action=\"/" +
                sInstanceID+" method=\"get\">\n");
        sb.append("<p>Select Type</p>\n");
        sb.append("<select name=\"type\">\n");
        sb.append("<option value=\""+ResultSet.TYPE_FORWARD_ONLY+"\">Forward Only</option>\n");
        sb.append("<option value=\""+ResultSet.TYPE_SCROLL_INSENSITIVE+"\">Scroll Insensitive</option>\n");
        sb.append("<option value=\""+ResultSet.TYPE_SCROLL_SENSITIVE+"\">Scroll Sensitive</option>\n");
        sb.append("</select>\n");
        sb.append("<p>Select Concurrency</p>\n");
        sb.append("<select name=\"concurrency\">\n");
        sb.append("<option value=\""+ResultSet.CONCUR_READ_ONLY+"\">Read Only</option>\n");
        sb.append("<option value=\""+ResultSet.CONCUR_UPDATABLE+"\">Updatable</option>\n");
        sb.append("</select>\n");
        sb.append("<p> Input your query: </p><input type=\"text\" name=\"Query\" value=\"\" size=\"20\">\n");
        sb.append("<input type=\"submit\" value=\"Execute Query\">\n");
        sb.append("</form>\n");
        
        //sb.append("<table align=center><font size=2><a id=\"url\" href=\"/" +
        //        sInstanceID + "?done=true\">DONE</a></font></table>\n");
        
        sb.append("</td>\n");     
        sb.append("</tr>\n");
        sb.append("</table>\n");
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
    	//done parameter
    	String sDone = request.getParameter("done");
    	//get the type concurrency and query properties
    	String sType = request.getParameter("type");
    	String sConcurrency = request.getParameter("concurrency");
    	String query = request.getParameter("Query");
    	
    	//set type and concurrency if they are not null
    	if (sType != null  && sType != "")
    		type = Integer.parseInt(sType);
    	
    	if (sConcurrency != null && sType != "")
    		concurrency = Integer.parseInt(sConcurrency);
    	
    	//if query is non-null, store and end webUI fragment
    	if (query != null && query !="")
    	{
    		sqlQuery = query;
    		sDone = "done";
    	}
    	
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

     /** This component allows a user to enter and run an SQL query that produces a result set. The query can be entered as a property string or typed by the user at runtime.
      *
      * @throws ComponentExecutionException If a fatal condition arises during
      *         the execution of a component, a ComponentExecutionException
      *         should be thrown to signal termination of execution required.
      * @throws ComponentContextException A violation of the component context
      *         access was detected
      */
     public void execute(ComponentContext cc)
     throws ComponentExecutionException, ComponentContextException {
    	//query from property
     	sqlQuery = cc.getProperty(DATA_PROPERTY);
     	//get input connection
     	conn = (Connection)cc.getDataComponentFromInput(DATA_INPUT);
     	//if the property was blank, start a webUI component to get properties
     	if (sqlQuery ==null || sqlQuery.equalsIgnoreCase("none"))
     	{
     		logger.log(Level.INFO,"Component Query property = none");
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
    		
    		}
    		catch ( Exception e ) {
    			throw new ComponentExecutionException(e);
    		}
     	}
     	else {
     		try{
     			BufferedReader inputQuery
     				= new BufferedReader(new FileReader(sqlQuery));
     			String lineIn = "";
     			boolean go = true;
     			sqlQuery = "";
     			while (go)
     			{
     				try{
     					lineIn = inputQuery.readLine();
     					lineIn.trim();
     				}
     				catch (IOException e){
     					//we have reached the end of file or can't read it- end
     					logger.log(Level.WARNING, "Exception reading query: "+e);
     					go = false;
     				}
     				if (lineIn == null)
     				{
     					go = false;
     				}
     				//if whole line is a comment, ignore it
     				else if (!lineIn.startsWith("//"))
     				{
     					//if line contains a comment, ignore everything after comment
     					if (lineIn.indexOf("//") != -1)
     					{
     						sqlQuery += " "+lineIn.substring(0,lineIn.indexOf("//"));
     					}
     					//if line contains end of query (;), include everything up to it
     					else if (lineIn.indexOf(";") != -1)
     					{
     						sqlQuery += " "+lineIn.substring(0,lineIn.indexOf(";")+1);
     					}
     					else
     					{
     						sqlQuery += " "+lineIn;
     					}
     				}
     			}
     			logger.log(Level.INFO, "Parsed Query is : "+sqlQuery);
     		}
     		catch (Exception e)
     		{
     			logger.log(Level.INFO, "Could not open sql file at: "+sqlQuery);
     		}
     	}
     	try{
     		stmt = conn.createStatement(type, concurrency);
     		logger.log(Level.INFO, "Beginning Query "+sqlQuery);
     		results = stmt.executeQuery(sqlQuery);
     		logger.log(Level.INFO, "Query Complete");
     	}
     	catch (Exception e)
     	{
     		logger.log(Level.SEVERE,"There has been an error executing query " +e);
     	}
     	
     	//outut connection for future queries and result set
		cc.pushDataComponentToOutput(DATA_OUTPUT1, results);
		cc.pushDataComponentToOutput(DATA_OUTPUT2, conn);
		cc.stopWebUIFragment(this);
     }

     /** This method is called when the Menadre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}
					
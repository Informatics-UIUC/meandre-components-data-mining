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

//Import statements
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.io.datasource.support.DataSourceFactory;
import org.meandre.components.io.datasource.support.JNDILookup;
import org.meandre.components.io.datasource.support.JNDINamespaceBuilder;
import org.meandre.components.io.datasource.support.JNDINamespaceWriter;
import org.meandre.components.io.datasource.support.JarXMLLoader;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

@Component(creator="Erik Johnson",
        description="<p>Overview:<br>"
        +"This Component initializes a JNDI context for datasource objects."
        +"These datasource objects can then be used to connect to a database using"
        +"The connect DB component. The web UI allows the user"
        +"to add and configure datasource objects.",
        name="Initialize JNDI with WebUI",
        tags="JNDI, datsources, database, connect, WebUI, UI, html",
        mode=Mode.webui,
        baseURL="meandre://seasr.org/components/")

/** This component will load information from two xml files and construct a JNDI context with datasource objects.
 * To do this, it 
 *
 * @author Erik Johnson
 */
public class InitJNDIWebUI extends InitJNDI implements WebUIFragmentCallback {

	 /** The blocking semaphore for Web UI component*/
    private Semaphore sem = null;

    /** The instance ID for Web UI component*/
    private String sInstanceID = null;
    
    //vector of known datasources
	private Vector <String> existingDS= new Vector<String>();
	
	//User Selected known datasource
	private String selectedExistingDS="";
	
	//Vendors with known driver and datasource class names- these have not necessarily been loaded
	private Vector <String> knownVendors= new Vector<String>();
	
	//selected vendor, driver and datasource
    private String selectedVendor="";
    private String selectedDatasource="";
    private String selectedDriver="";
    private Properties selectedProperties=new Properties();
    
    //control variables
    private boolean configuringDataSource=false;
    private boolean configuringVendor=false;
//    private boolean knownVendor=true;
    private boolean basicProps=true;
    
    //Driver property info to allow user configuration
    private DriverPropertyInfo[] driverProperties;
    
    //Class to access JNDI namespace
    private JNDILookup databaseNamespace= new JNDILookup();
    
    //flag set to true in order to display properties
    private boolean viewProps=false;

    /** This method gets call when a request with no parameters is made to a
     * component WebUI fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem encountered during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
        try {
            response.getWriter().println(getViz());//get visualization in html form
        } catch (IOException e) {
            throw new WebUIException(e);
        }
    }

    /** HTML Page. Page is written into string buffer
     *
     * @return The HTML containing the page in string form
     */
    private String getViz() {

    	//Acess JNDI root context to look for existing datasources
    	existingDS=databaseNamespace.listObjects("");
    	//look in Datasource Factory for known vendors
    	knownVendors=DataSourceFactory.getKnownVendors();
    	String key,value;
    	
    	//check to see if a vendor has been selected
    	if (selectedVendor!= "" && selectedVendor != null)
    	{
    		//find vendor props
    		selectedProperties=DataSourceFactory.discoverProps(selectedVendor);
    	}
    	
        StringBuffer sb = new StringBuffer();
        sb.append("<html>\n");
        sb.append("<body>\n");

        sb.append("<table border=\"0\" width=\"100%\" cellpadding=\"10\">\n");
        sb.append("<tr>\n");
        
        /**Do Not allow the InitJNDI component to generate a connection
        sb.append("<td width=\"40%\" valign=\"top\">\n");
        sb.append("<h1>Select JNDI Datasource to Load</h1>\n");

        form for selection of datasource objects in JNDI namespace
        sb.append("<form action=\"/" +
                sInstanceID+" method=\"post\">\n");//use post over get due to url length
        sb.append("<select name=\"persistentDS\">\n");
        for (int i=0; i<existingDS.size(); i++)
        	sb.append("<option value=\""+existingDS.elementAt(i)+"\">"+existingDS.elementAt(i)+"</option>\n");
        sb.append("</select>\n");
        sb.append("<input type=\"submit\" value=\"Load Datasource\">\n");
        sb.append("</form>\n");
      	*/
        
        /**If the user has selected an existing datasource from the JNDI namespace...
        if (selectedExistingDS != null && selectedExistingDS !="" )
        {
        	//Display info
        	if (driverProperties == null || driverProperties.length==0)
        	{
        		//Properties haven't been set, display name only
        		sb.append("<form action=\"/" +
        				sInstanceID+" method=\"post\">\n");
        		//display options: connect, remove, and viewProps
        		sb.append("<input type=\"radio\" name=\"DSconnection\" value=\"viewProps\"> View Properties \n");
        		sb.append("<input type=\"radio\" name=\"DSconnection\" value=\"Remove\"> Remove   \n");
        		sb.append("<input type=\"radio\" name=\"DSconnection\" value=\"connect\"> Connect <br />\n");
        		sb.append("<input type=\"submit\" value=\"Connect Datasource\">\n");
        		sb.append("</form>\n");
        	}
        	else
        	{
        		//Properties have been set, display all information, required properties, then optional properties
        		sb.append("<form action=\"/" +
        				sInstanceID+" method=\"post\">\n");
        		sb.append("<p>Required Properties:</p><br />\n");
        		for (int i=0; i<driverProperties.length; i++)
        		{
        			if (driverProperties[i].required)
        			{
        				sb.append("<p>"+driverProperties[i].name+" ("+driverProperties[i].description+"): </p><input type=\"text\" name=\"RDB"+driverProperties[i].name+"\" value=\""+driverProperties[i].value+"\" size=\"20\">\n");
                    	sb.append("<br />\n");
                    	if (driverProperties[i].choices!=null)
                    	{
                    		for (int j=0; j<driverProperties[i].choices.length;j++)
                    		{
                    			//list out required properties
                    			sb.append("<p> Choice "+j+": "+driverProperties[i].choices[j]+" </p>\n");
                    		}
                    		sb.append("<br />\n");
                    	}
                    	sb.append("<br />\n");
        			}
        		}
        		sb.append("<p>Optional Properties:</p><br />\n");
        		for (int i=0; i<driverProperties.length; i++)
        		{
        			if (!driverProperties[i].required)
        			{
        				sb.append("<p>"+driverProperties[i].name+" ("+driverProperties[i].description+"): </p><input type=\"text\" name=\"RDB"+driverProperties[i].name+"\" value=\""+driverProperties[i].value+"\" size=\"20\">\n");
                    	sb.append("<br />\n");
                    	if (driverProperties[i].choices!=null)
                    	{
                    		for (int j=0; j<driverProperties[i].choices.length;j++)
                    		{
                    			//list out optional properties
                    			sb.append("<p> Choice "+j+": "+driverProperties[i].choices[j]+" </p>\n");
                    		}
                    		sb.append("<br />\n");
                    	}
        			}
        		}
        		sb.append("<input type=\"submit\" value=\"Reconnect\">\n");
        		sb.append("</form>\n");
        	}
        }
        //sb.append("</td>\n");
        sb.append("</tr>\n");*/
        
        if (configuringDataSource){
        	//Second coloumn for creation of new datasources
        	//present user with known vendors
        	//sb.append("<td width=\"40%\" valign=\"top\">\n");
        	sb.append("<tr>\n");
        	sb.append("<h3>Create New Datasource</h3>\n");
        	sb.append("<form action=\"/" +
        			sInstanceID+" method=\"post\">\n");
        	sb.append("<select name=\"ConnectDS\">\n");
        	for (int i=0; i<knownVendors.size(); i++)
        		sb.append("<option value=\""+knownVendors.elementAt(i)+"\">"+knownVendors.elementAt(i)+"</option>\n");
        	sb.append("</select>\n");
        	sb.append("<input type=\"radio\" name=\"advancedProps\" value=\"advanced\"> Advanced Properties \n");
        	sb.append("<input type=\"radio\" name=\"advancedProps\" value=\"basic\"> Basic Properties   \n");
        	sb.append("<input type=\"submit\" value=\"Use this Vendor\">\n");
        	sb.append("</form>\n");
        
        	//if a vendor has already been selected
        	if (selectedVendor!=null && selectedVendor!="")
        	{
        		//display datasource properties for selection
        		sb.append("<form action=\"/" +
        				sInstanceID+" method=\"post\">\n");
        		sb.append("<p>Configure New Datasource</p><br />\n");
        
        		sb.append("<p>JNDI Location: </p><input type=\"text\" name=\"DBJNDILoc\" value=\"\" size=\"20\">\n");
        		sb.append("<p>The JNDI location is a logical name for this datasource (for example, \"myPostgresDB\"). This name will be used to look up the datasource in the JNDI namespace.</p>\n");
        		
        		if (DataSourceFactory.isCommonVendor(selectedVendor) && basicProps)
        		{
        			sb.append("<p>Vendor Name : </p><input type=\"text\" name=\"DBVendor Name\" value=\""+selectedVendor+"\" size=\"20\">\n");
					sb.append("<br />\n");
					sb.append("<p>Vendor Driver : </p><input type=\"text\" name=\"DBVendor Driver\" value=\""+DataSourceFactory.getCurrentDriver(selectedVendor)+"\" size=\"20\">\n");
					sb.append("<br />\n");
					sb.append("<p>Vendor DataSource : </p><input type=\"text\" name=\"DBVendor DataSource\" value=\""+DataSourceFactory.getCurrentDatasource(selectedVendor)+"\" size=\"20\">\n");
					sb.append("<br />\n");
					sb.append("<p>Connection Pooling : </p><input type=\"text\" name=\"DBConnection Pooling\" value=\""+DataSourceFactory.isPooled(selectedVendor)+"\" size=\"20\">\n");
					sb.append("<br />\n");
        			Vector <?> commonProps= (Vector<?>)( DataSourceFactory.getCommonProps(selectedVendor));
        			for (int i=0; i<commonProps.size(); i+=4)
        			{
        				sb.append("<p>"+commonProps.get(i+1).toString()+" ("+commonProps.get(i+2).toString()+"): </p><input type=\"text\" name=\"DB"+commonProps.get(i).toString()+"\" value=\"\" size=\"20\"> "+commonProps.get(i+3).toString()+"</p>\n");
    					sb.append("<br />\n");
        			}
        		}
        		else{
        			Enumeration<?> propNames = selectedProperties.keys();
        			while (propNames.hasMoreElements())
        			{
        				//list out properties with necessary parameters
        				key= (String) propNames.nextElement();
        				value= (String) selectedProperties.getProperty(key);
        				if (key.equalsIgnoreCase("Vendor Name"))
        				{
        					sb.append("<p>"+key+" ("+value+"): </p><input type=\"text\" name=\"DB"+key+"\" value=\""+selectedVendor+"\" size=\"20\" >\n");
        					sb.append("<br />\n");
        				}
        				else if(key.equalsIgnoreCase("Vendor Driver"))
        				{
        					sb.append("<p>"+key+" ("+value+"): </p><input type=\"text\" name=\"DB"+key+"\" value=\""+DataSourceFactory.getCurrentDriver(selectedVendor)+"\" size=\"20\">\n");
        					sb.append("<br />\n");
        				}
        				else if(key.equalsIgnoreCase("Vendor DataSource"))
        				{
        					sb.append("<p>"+key+" ("+value+"): </p><input type=\"text\" name=\"DB"+key+"\" value=\""+DataSourceFactory.getCurrentDatasource(selectedVendor)+"\" size=\"20\">\n");
        					sb.append("<br />\n");
        				}
        				else if (key.equalsIgnoreCase("Connection Pooling"))
        				{
        					sb.append("<p>"+key+" ("+value+"): </p><input type=\"text\" name=\"DB"+key+"\" value=\""+DataSourceFactory.isPooled(selectedVendor)+"\" size=\"20\">\n");
        					sb.append("<br />\n");
        				}
        				else
        				{
        					sb.append("<p>"+key+" ("+value+"): </p><input type=\"text\" name=\"DB"+key+"\" value=\"\" size=\"20\">\n");
        					sb.append("<br />\n");
        				}
        			}
        		}
        		//check to see if datasource class and driver class are in the classpath
        		if (!DataSourceFactory.isKnownDriver(selectedDriver))
        		{
        			sb.append("<p>WARNING!: Vendor driver cannot be loaded! Check your installation </p><br />");	
        		}
        		if (!DataSourceFactory.isKnownDataSource(selectedDatasource))
        		{
        			sb.append("<p>WARNING!: Vendor Datasource Class cannot be loaded! Check your installation </p><br />");	
        		}
        		sb.append("<input type=\"submit\" value=\"Create Datasource\">\n");
        		sb.append("</form>\n");
        	}
        	sb.append("</tr>\n");
        }

        if (configuringVendor){
        	logger.log(Level.INFO,"Configuring Vendor");
        	//Final column, select new vendor and driver info to load from external jar file
        	Vector<String>commonNames = DataSourceFactory.getCommonVendors();
        	
        	//sb.append("<td width=\"20%\" valign=\"top\">\n");
        	sb.append("<tr>\n");
        	sb.append("<form action=\"/" +
        			sInstanceID+" method=\"post\">\n");
        	sb.append("<p>Add New Database Vendor Information</p><br />\n");
        	//allow user to specify vendor name, driver, datasource
        	
        	sb.append("<select name=\"vendorName\">\n");
        	for (int i=0; i<commonNames.size(); i++)
        		sb.append("<option value=\""+commonNames.elementAt(i)+"\">"+commonNames.elementAt(i)+"</option>\n");
        		sb.append("<option value=\"Other\">Other</option>\n");
        	sb.append("</select>\n");
        	
        	sb.append("<p>If Other, please provide Vendor Name: </p><input type=\"text\" name=\"otherName\" value=\"My Other Vendor\" size=\"20\">\n");
        	sb.append("<br />\n");
        	sb.append("<p>If Other, please provide Vendor Driver: </p><input type=\"text\" name=\"vDriver\" value=\"org.myJDBCpackage.myDriver\" size=\"20\">\n");
        	sb.append("<br />\n");
        	sb.append("<p>If Other, please provide Vendor Datasource Class: </p><input type=\"text\" name=\"vDatasource\" value=\"org.myJDBCpackage.myDatasource\" size=\"20\">\n");
        	sb.append("<br />\n");
        	sb.append("<p>If Other, Is this a pooled connection datasource?</p><br />");
        	sb.append("<input type=\"radio\" name=\"PooledConn\" value=\"true\"> Yes \n");
        	sb.append("<input type=\"radio\" name=\"PooledConn\" value=\"false\"> No   \n");
        	sb.append("<p>Driver Jar File Name: </p><input type=\"text\" name=\"vJarName\" value=\"myjar.jar\" size=\"20\">\n");
        	sb.append("<br />\n");
        	sb.append("<p>External Jar Location: (C:/pathToJar or /pathToJar): </p><input type=\"text\" name=\"vJarLoc\" value=\"\" size=\"20\">\n");
        	sb.append("<br /><p>Location to load JDBC jar containing Datasource and Driver classes if not already loaded</p><br />\n");
        	sb.append("<input type=\"submit\" value=\"Add Vendor\">\n");
        	sb.append("</form>\n");
        	sb.append("</tr>\n");
        }
        else if (!configuringVendor || !configuringDataSource)
        {
        	//sb.append("<td width=\"40%\" valign=\"top\">\n");
        	sb.append("<tr>\n");
        	sb.append("<h3>Configure new Database Vendor or new Datasource Object</h3>\n");
        	sb.append("<form action=\"/" +
        			sInstanceID+" method=\"post\">\n");
        	sb.append("<input type=\"radio\" name=\"configuring\" value=\"DataSource\"> Configure New DataSource");
        	sb.append("<br>");
        	sb.append("<input type=\"radio\" name=\"configuring\" value=\"Vendor\"> Configure New Vendor");
        	sb.append("<br><input type=\"submit\" value=\"Configure\">\n");
        	sb.append("</form>\n");
        	sb.append("</tr>\n");
        }
        //sb.append("</tr>\n");
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
     * @param target The target path
     * @param request The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
    	//get parameters
    	//user has hit done button
    	String sDone = request.getParameter("done"); //done button eliminated, user must create a connection to exit component
    	
    	//user has selected DS from JNDI namespace
    	//String spersistentDS = request.getParameter("persistentDS");
    	
    	//user has requested to connect to existing datasource
    	String snewDS = request.getParameter("ConnectDS");
    	
    	//user has chosen a new vendor to add
    	String snewDriver = request.getParameter("vDriver");
    	String snewDatasource = request.getParameter("vDatasource");
    	String snewName = request.getParameter("vendorName");
    	String snewOtherName = request.getParameter("otherName");
    	String snewJarLoc = request.getParameter("vJarLoc");
    	String snewJarName = request.getParameter("vJarName");
    	//User has requested to connect, remove, or view properties of existing datasource
    	String sDSconnection = request.getParameter("DSconnection");
    	String pooledConnection = request.getParameter("PooledConn");
    	String advancedProperties = request.getParameter("advancedProps");
    	String configString = request.getParameter("configuring");
    	
    	
    	
    	if (configString!=null){
    		if (configString.equalsIgnoreCase("DataSource"))
    		{
    			configuringDataSource=true;
    		}
    		if (configString.equalsIgnoreCase("Vendor"))
    		{
    			configuringVendor=true;
    		}
    	}
    	
    	
    	
    	String dbVendorName = request.getParameter("DBVendor Name");
    	
    	String key,value;
    	//User has given a location to bind a new datasource object in the JNDI namespace
    	String sJNDILoc= request.getParameter("DBJNDILoc");
    	
    	/** //User has chosen to input properties and reconnect to database existing in JNDI namespace
    	if (viewProps)
    	{
    		Properties newProps= new Properties();
    		for (int i=0; i<driverProperties.length;i++)
    		{
    			//get values
    			value = request.getParameter("RDB"+driverProperties[i].name);
    			if (value != null && !value.equalsIgnoreCase("null") && value != "")
    			{
    				//set new properties
    				newProps.setProperty(driverProperties[i].name, value);
    			}
    		}
    		//use reconnect method with exisitng connection, the new properties, and the datasource from the JNDI namespace
    		if (DataSourceFactory.isPooled(selectedExistingDS))
    		{
    			databaseConnection=DataSourceFactory.reConnect(databaseConnection, newProps, (ConnectionPoolDataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    		}
    		else{
    			databaseConnection=DataSourceFactory.reConnect(databaseConnection, newProps, (DataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    		}
    		sDone="Done";
    	}*/
    	
    	/**
    	//User has selected to view properties, connect to, or remove a datasource from the JNDI namespace
    	if (sDSconnection != null && sDSconnection != "")
    	{
    		if (sDSconnection.equalsIgnoreCase("viewProps"))
    		{
    			viewProps=true;
    			//get connection and properties using bound datasource
    			logger.log(Level.INFO, "Attempting to connect to db");
    			try{
    				if (DataSourceFactory.isPooled(selectedExistingDS))
    	    		{
    					databaseConnection=DataSourceFactory.getExistingConnection((ConnectionPoolDataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    	    		}
    	    		else{
    	    			databaseConnection=DataSourceFactory.getExistingConnection((DataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    	    		}
        
        			}
        			catch (Exception e)
        			{
        				logger.log(Level.SEVERE,"Problem connecting to datasource "+databaseConnection.toString()+" :"+e +":"+ e.getMessage());
        		
        			}
    			logger.log(Level.INFO, "Connected to DB");
    			logger.log(Level.INFO, "Reading Properties");
    			if (DataSourceFactory.isPooled(selectedExistingDS))
	    		{
    				driverProperties= DataSourceFactory.getConnectionProperties(databaseConnection, (ConnectionPoolDataSource) databaseNamespace.getExistingObject(selectedExistingDS));
	    		}
	    		else{
	    			driverProperties= DataSourceFactory.getConnectionProperties(databaseConnection, (DataSource) databaseNamespace.getExistingObject(selectedExistingDS));
	    		}
    		
    			logger.log(Level.INFO, "Properties Read");
    		}
    		//User wants to connect to existing datasource without modifying properties
    		if (sDSconnection.equalsIgnoreCase("connect"))
    		{
    			logger.log(Level.INFO, "Attempting to connect to db");
    			try{
    				if (DataSourceFactory.isPooled(selectedExistingDS))
    	    		{
    					logger.log(Level.INFO, "Connection is Pooled for "+selectedExistingDS);
  
    	    			ConnectionPoolDataSource cpds = (ConnectionPoolDataSource) databaseNamespace.getExistingObject(selectedExistingDS);
    	    			logger.log(Level.INFO, "Got DataSource");
    	    		
    	    			Connection conn;
    	    			conn = cpds.getPooledConnection().getConnection();
    	    			logger.log(Level.INFO,"Connected   ");
    	    			logger.log(Level.INFO,conn.getMetaData().getURL());
    	    			databaseConnection = conn;
    	    			//databaseConnection=DataSourceFactory.getExistingConnection((DataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    					//databaseConnection=DataSourceFactory.getExistingConnection((ConnectionPoolDataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    	    		}
    	    		else{
    	    			logger.log(Level.INFO, "Connection is not Pooled "+selectedExistingDS);
    	    			DataSource nopoolds = (DataSource) databaseNamespace.getExistingObject(selectedExistingDS);
    	    			logger.log(Level.INFO, "Got DataSource");
    	    			Connection conn= null;
    	    			conn = nopoolds.getConnection();
    	    			logger.log(Level.INFO,"Connected   ");
    	    			logger.log(Level.INFO,conn.getMetaData().getURL());
    	    			databaseConnection = conn;
    	    			//databaseConnection=DataSourceFactory.getExistingConnection((DataSource) databaseNamespace.getExistingObject(selectedExistingDS));
    	    		}
    				logger.log(Level.INFO, "Connected to DB");
    			}
    			catch (Exception e)
    			{
    				logger.log(Level.SEVERE,"Problem connecting to datasource "+databaseConnection.toString()+" :"+e +":"+ e.getMessage());
    			
    			}
    			sDone="Done";
    		}
    		//user wants to remove datasource from JNDI namespace
    		if (sDSconnection.equalsIgnoreCase("remove"))
    		{
    			databaseNamespace.removeObject(selectedExistingDS);
    		}
    	}*/
    	;
    	//USer has input properties for a new datasource connection and has given a location to bind it to
    	if (sJNDILoc != "" && sJNDILoc != null && !sJNDILoc.equalsIgnoreCase("jdbc") && !sJNDILoc.equalsIgnoreCase("jdbc/"))
        {
        	Enumeration<?> propNames = selectedProperties.keys();
            while (propNames.hasMoreElements())
            {
            	key= (String) propNames.nextElement();
            	value= request.getParameter("DB"+key);
            	if (value == null)
            		value ="";
            	logger.log(Level.INFO, key+value+"\n");
            	selectedProperties.setProperty(key, value);
            }
            //bind new datasource to JNDI namespace
            if (DataSourceFactory.isPooled(dbVendorName))
            {
            	databaseNamespace.bindObject(sJNDILoc, (Object)DataSourceFactory.createPooledDS(selectedProperties));
           }
            else{
            	databaseNamespace.bindObject(sJNDILoc, (Object)DataSourceFactory.createDS(selectedProperties));
           }
           configuringDataSource=false;
        }
    	//user wants to load new vendor and specify an external jar file with classes
    	//if (snewDriver != null && snewDatasource !=null && snewName !=null)
    	logger.log(Level.INFO,"2: "+snewName);
    	if (snewName!=null)
    	{
    		logger.log(Level.INFO,"SNEWNAMENOTNULL");
    		if (DataSourceFactory.isCommonVendor(snewName))
    		{
    			logger.log(Level.INFO, snewName);
    			snewDriver=DataSourceFactory.getCommonDriver(snewName);
    			snewDatasource = DataSourceFactory.getCommonDatasource(snewName);
    			try{
    				pooledConnection = DataSourceFactory.getCommonPooling(snewName);
    			}
    			catch (Exception e)
    			{pooledConnection="false";}
    			logger.log(Level.INFO, snewDriver);
    			logger.log(Level.INFO, snewDatasource);
    			logger.log(Level.INFO, pooledConnection);
    		}
    		else
    		{snewName=snewOtherName;}
    		if (snewJarLoc != "" && snewJarLoc != null && snewDriver != null && snewDatasource !=null)
    		{
    			logger.log(Level.INFO,"AddingJar");
    			//jdbcLoader.loadJarClass(snewDriver, snewJarLoc);
    			try{
    				jarLoader.addJar(pooledConnection, snewName, snewDatasource, snewDriver, snewJarLoc, JarXMLLoader.getPublicResourcesDirectory(), snewJarName);
    				//DataSourceFactory.addJarFile(snewJarLoc);
    				//DataSourceFactory.loadJarClass(snewDriver);
    				//DataSourceFactory.loadJarClass(snewDatasource);
    			}
    			catch (Exception e)
    			{
    				logger.log(Level.SEVERE,"There has been an error loading driver or Datasource"+e);
    			}
    		}
    		//DataSourceFactory.addNewDatabaseVendor(snewName, snewDriver, snewDatasource);
    		configuringVendor=false;
    		try{
//    			String fname = JarXMLLoader.getPublicResourcesURL();
    			String fdir = JarXMLLoader.getPublicResourcesDirectory();
    			if ((!(fdir.endsWith("/"))) && (!(fdir.endsWith("\\")))) {
    				fdir += File.separator;
    			}
    			//use xml location to write out new jar file properties
    			String xmlURL = fdir+jarXMLFile;
    			
    			jarLoader.writePropsFile(xmlURL);
    		}
    		catch (Exception e)
    		{
    			logger.log(Level.INFO, "Failed to write jar xml file");
    		}
    	}
    	/** //User has selected an existing datasource from the JNDI namespace to use
    	if (spersistentDS != null && spersistentDS !="")
    	{
    		selectedExistingDS=spersistentDS;
    	}*/
    	//User has selected a vendor to use to create a new datasource
    	if (snewDS != null && advancedProperties != null)
    	{
    		selectedVendor=snewDS;
    		selectedDriver=DataSourceFactory.getCurrentDriver(selectedVendor);
    		selectedDatasource=DataSourceFactory.getCurrentDatasource(selectedVendor);
//    		pooled = DataSourceFactory.isPooled(selectedVendor);
    		if (advancedProperties.equalsIgnoreCase("advanced"))
				basicProps=false;
			else 
				basicProps=true;
    	}
    	//user has pressed done button or created a connection- release semaphore
    	if ( sDone!=null ) {
			sem.release();
		}
    	//do nothing
		else
			emptyRequest(response);
    }

     /** This Method is invoked when the component is executed. It will initialize and 
      *
      * @throws ComponentExecutionException If a fatal condition arises during
      *         the execution of a component, a ComponentExecutionException
      *         should be thrown to signal termination of execution required.
      * @throws ComponentContextException A violation of the component context
      *         access was detected
      */
     public void execute(ComponentContext cc)
     throws ComponentExecutionException, ComponentContextException {

    	//start the web UI
     	//
     	logger.log(Level.INFO,"Firing the web ui component");
 		sInstanceID = cc.getExecutionInstanceID();
 		try {
 			sem = new Semaphore(1, true);
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


}
/* University of Illinois/NCSA
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

//import statements

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Erik Johnson",
        description="<p>Overview:<br>"
        +"This component opens a connection object to a database."
        +" This component assumes that a JNDI context with datasources is already configured."
        +" It will open a connection to the database specified by the JNDI object at JNDIName.</p>",
        name="Connect to Database",
        tags="database, connect, JNDI, datasource",
        baseURL="meandre://seasr.org/components/data-mining/")

/** A component to close the DB connection passed to it.
 *
 * @author Erik Johnson
 */
public class ConnectDB implements ExecutableComponent {

	private Logger logger;
	private Connection dbConn = null;
	private String jndiName="";
	private Context ctx;
	
	@ComponentProperty(description = "JNDI Datasource URL",
	   		   defaultValue = "",
	   		   name = "JNDI_Name")
public final static String DATA_PROPERTY = "JNDI_Name";
	
	@ComponentOutput(
	 		description = "Connection to Database",
	 		name = "Connection")
	 final static String DATA_OUTPUT = "Connection";
	
     /** This method is invoked when the Meandre Flow is being prepared for
      * getting run.
      *
      * @param ccp The component context properties
      */
	
     public void initialize ( ComponentContextProperties ccp ) {
  
    	logger = ccp.getLogger();
     	
     }

     /** This component searches the JNDI namespace for a Datasource with the given name
      *
      * @throws ComponentExecutionException If a fatal condition arises during
      *         the execution of a component, a ComponentExecutionException
      *         should be thrown to signal termination of execution required.
      * @throws ComponentContextException A violation of the component context
      *         access was detected
      */
     public void execute(ComponentContext ccp)
     throws ComponentExecutionException, ComponentContextException {
      	
     	jndiName = ccp.getProperty(DATA_PROPERTY);
     	logger.log(Level.INFO, "Attempting to connect to db "+jndiName);
     	
     	ctx = null;
       	try {
			Hashtable<String,String> env = new Hashtable<String,String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.mortbay.naming.InitialContextFactory");
			env.put(Context.PROVIDER_URL,"localhost:1099");

			ctx = new InitialContext(env);
			//Assume Context already configured
			NamingEnumeration<NameClassPair> list = ctx.list( "" );

			while (list.hasMore()) {
				NameClassPair nc = (NameClassPair)list.next();
				System.out.println(nc.getName());
				if(nc.getName().equalsIgnoreCase(jndiName)){
					try{
						dbConn = (Connection)( (DataSource)( ctx.lookup(jndiName))).getConnection() ;	
						dbConn.isReadOnly(); //.setAutoCommit(false);
					}
					catch (Exception exc)
					{
						logger.log(Level.WARNING, "Could not open "+jndiName+" as Datasource, trying Pooled Datasource");
						try{  
							dbConn = ((ConnectionPoolDataSource) ctx.lookup(jndiName)).getPooledConnection().getConnection();
							dbConn.isReadOnly(); //.setAutoCommit(false);
						}
						catch (Exception exc2)
						{
							logger.log(Level.SEVERE, "Could not open "+jndiName+": "+exc);
						}
					}
					
				}				
			} 
       	} catch (Exception e){
       		e.printStackTrace();
       	}
       	
       	ccp.pushDataComponentToOutput(DATA_OUTPUT, dbConn);
     }

     /** This method is called when the Menadre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}
		
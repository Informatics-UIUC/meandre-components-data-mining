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
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.sql.Connection;
import java.sql.SQLException;

@Component(creator="Erik Johnson",
        description="<p>Overview:<br>"
        +"This component closes a connection object to a database."
        +" After all components connecting to the databse"
        +" have been executed, the connection should be closed.</p>",
        name="Close Database Connection",
        tags="database, close",
        baseURL="meandre://seasr.org/components/data-mining/")

/** A component to close the DB connection passed to it.
 *
 * @author Erik Johnson
 */
public class CloseDB implements ExecutableComponent {

	private Logger logger;
	
	@ComponentInput(
	 		description = "Connection object to close",
	 		name = "Connection")
	 final static String DATA_INPUT = "Connection";
	
     /** This method is invoked when the Meandre Flow is being prepared for
      * getting run.
      *
      * @param ccp The component context properties
      */
	
     public void initialize ( ComponentContextProperties ccp ) {
    	 logger = ccp.getLogger();
     }

     /** A component to close the DB connection passed to it.
      *
      * @throws ComponentExecutionException If a fatal condition arises during
      *         the execution of a component, a ComponentExecutionException
      *         should be thrown to signal termination of execution required.
      * @throws ComponentContextException A violation of the component context
      *         access was detected
      */
     public void execute(ComponentContext cc)
     throws ComponentExecutionException, ComponentContextException {
    	 try{
    		 //get input connection
    		 Connection conn = (Connection) cc.getDataComponentFromInput(DATA_INPUT);
    		 logger.log(Level.INFO,"Attempting to close connection to Database at "+conn.getMetaData().getURL());
    		 //close input connection
    		 conn.close();
    		 logger.log(Level.INFO,"Database closed");
    	 }
    	 catch (SQLException e)
    	 {
    		 logger.log(Level.SEVERE,"There is a problem closing database connection." +e);
    	 }
    	 catch (Exception e)
    	 {
    		 logger.log(Level.SEVERE,"There is a problem closing database connection." +e);
    	 }
    	 
     }

     /** This method is called when the Meandre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}
								
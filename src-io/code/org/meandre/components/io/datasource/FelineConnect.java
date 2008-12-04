package org.meandre.components.io.datasource;

//import statements
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.ComponentOutput;

import com.mysql.jdbc.jdbc2.optional.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.sql.Connection;
import java.sql.SQLException;

@Component(creator="Erik Johnson",
        description="Open a mysql connection Database using a simple datasource",
        name="FelineConnect",
        tags="database, connect, mysql")

/** A component to close the DB connection passed to it.
 *
 * @author Erik Johnson
 */
public class FelineConnect implements ExecutableComponent {

	private Logger logger;
	
	//@ComponentProperty(description="Location of postgresql driver jar file",
    //    	name="jarlocation",
    //    	defaultValue="C:\\postgresql-8.3-603.jdbc3.jar")
    //final static String PROPERTY_JAR_LOC = "jarlocation";
        /*
    @ComponentProperty(description="Name of database user",
        	name="user",
        	defaultValue="username")
    final static String PROPERTY_USER = "user";
        	
    @ComponentProperty(description="User password",
        	name="password",
        	defaultValue="mypwd")
    final static String PROPERTY_PWD = "password";
        	
   @ComponentProperty(description="Name of database to connect to",
        	name="databasename",
        	defaultValue="mydb")
   final static String PROPERTY_DATABASENAME = "databasename";
    
   @ComponentProperty(description="Name of host server",
        	name="servername",
        	defaultValue="localhost")
   final static String PROPERTY_SERVERNAME = "servername";
   
   @ComponentProperty(description="Host port number",
        	name="port",
        	defaultValue="5432")
   final static String PROPERTY_PORT = "port";      	
        	*/
	@ComponentOutput(
	 		description = "Connection",
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
    		 //get input connection
    		 /*String username = cc.getProperty(PROPERTY_USER);
    		 String pass = cc.getProperty(PROPERTY_PWD);
    		 
    		 String dbname = cc.getProperty(PROPERTY_DATABASENAME);
    		 String hostname = cc.getProperty(PROPERTY_SERVERNAME);
    		 String portnum = cc.getProperty(PROPERTY_PORT);
    		 int port = Integer.parseInt(portnum);
    	*/
    	 
    	 String hostname = "mensa.ncsa.uiuc.edu";
    	 String dbname = "feline";
    	 int port = 13306;
    	 String username = "catscholar";
    	 String pass = "c1tsch1l0r";
    	 
    	 
    	 MysqlDataSource source = new MysqlDataSource();
    			//source.setDataSourceName("A Data Source");
    			source.setServerName(hostname);
    			source.setDatabaseName(dbname);
    			source.setPort(port);
    			source.setUser(username);
    			source.setPassword(pass);
    			logger.log(Level.INFO,"Datasource Configured");
    			//source.setMaxConnections(10);
    			Connection conn = null;
    			logger.log(Level.INFO,source.getUrl());
    			try {
    			    conn = source.getConnection();
    			    logger.log(Level.INFO,"Connected   ");
    			    logger.log(Level.INFO,conn.getMetaData().getURL());
    			    // use connection
    			} catch (SQLException e) {
    			    logger.log(Level.SEVERE,"Could not connect to database");
    			    e.printStackTrace();
    			}
    			cc.pushDataComponentToOutput(DATA_OUTPUT, conn);

    	 
     }

     /** This method is called when the Menadre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}
		
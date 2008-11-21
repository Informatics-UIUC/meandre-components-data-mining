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

import org.meandre.components.datatype.table.Column;
import org.meandre.components.datatype.table.ColumnTypes;
import org.meandre.components.datatype.table.Table;

@Component(creator="Erik Johnson",
        description="This compnent executes a query and returns the resultset. " +
        		"The user has three options. " +
        		"First they may specify a path to a query file on the local filesystem using the Query_Path property. " +
        		"If this property is not blank, the component will attempt to load from the path and execute the query stored in that file. " +
        		"If the Query_Path property is blank, it will check the Query_Statement Property. " +
        		"If this is not blank, it will attempt to execute the text in the Query_Statement property as an sql query. " +
        		"Finally, if both properties are blank, it will present the user with a WebUI to enter a query manually",
        name="QueryDB",
        tags="database")

/** This component allows a user to enter and run an SQL query that produces a result set. The query can be entered as a property string or typed by the user at runtime.
 *
 * @author Erik Johnson
 */
public class WriteTableToDB implements ExecutableComponent {


    /** The instance ID */
    private String sInstanceID = null;

    private String tableString;
    
    private String tableName;
    
    private Connection conn;
    
    private Statement stmt;

    private Table writeTable;
    
    private Logger logger;
    
    public String tableTypetoRSType (int tableType)
    {
 	   if (tableType == ColumnTypes.INTEGER)
 	   {
 		   return "INTEGER";
 	   }
 	   if (tableType == ColumnTypes.BOOLEAN )
 	   {
 		   return "BOOLEAN";
 	   }
 	   if (tableType == ColumnTypes.CHAR)
 	   {
 		   return  "CHAR";
 	   }
 	   if (tableType == ColumnTypes.DOUBLE )
 	   {
 		   return "DOUBLE";
 	   }
 	   if (tableType == ColumnTypes.FLOAT )
 	   {
 		   return "FLOAT";
 	   }
 	   if (tableType == ColumnTypes.OBJECT )
 	   {
 		   return "JAVA_OBJECT";
 	   }
 	   if (tableType == ColumnTypes.SHORT )
 	   {
 		   return "SMALLINT";
 	   }
 	   if (tableType == ColumnTypes.STRING )
 	   {
 		   return "VARCHAR";
 	   }
 	   if (tableType == ColumnTypes.BYTE )
 	   {
 		   return "BINARY";
 	   }
 	   else 
 		   return null;
    }
    
    //Connection Input
    @ComponentInput(
	 		description = "The input databse connection",
	 		name = "Connection")
	final static String DATA_INPUT = "Connection";
    
  //Table Input
    @ComponentInput(
	 		description = "The input D2K table to write",
	 		name = "TableIn")
	final static String DATA_INPUT2 = "Table";
    
    //Connection output
    @ComponentOutput(
	 		description = "Connection out",
	 		name = "ConnectionOut")
	final static String DATA_OUTPUT = "ConnectionOut";

    
	//The name of the table to create in the database
	@ComponentProperty(description="The name of the table to create in the database",
            	name="Table_Name",
            	defaultValue="")
    final static String DATA_PROPERTY = "Table_Name";
	






    
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
     	tableName = cc.getProperty(DATA_PROPERTY);
     	//get input connection
     	conn = (Connection)cc.getDataComponentFromInput(DATA_INPUT);
     	//get table to write
     	writeTable = (Table)cc.getDataComponentFromInput(DATA_INPUT2);
     	
     	int numCols = writeTable.getNumColumns();
     	int numRows = writeTable.getNumRows();
     	
     	//Begin to create SQL statement to create table
     	tableString = "CREATE TABLE "+tableName+" (";
		
     	//Turn table columns into data types for the database
     	for (int i = 0; i<numCols; i++)
     	{
     		if (tableTypetoRSType(writeTable.getColumnType(i)) != null)
     		{
     			tableString +=writeTable.getColumnLabel(i)+" "+tableTypetoRSType(writeTable.getColumnType(i))+", ";
     		}
     	}
     	tableString +=")";

     	//Execute statement to create table in database
     	try {
     			stmt = conn.createStatement();
     			stmt.executeUpdate(tableString);
     			stmt.close();
     	} catch(SQLException ex) {
     		System.err.println("SQLException: " + ex.getMessage());
     	}
     	
     	
     	
     	//Now write all table rows to new table
     	try {
     		for(int i = 0; i<numRows; i++)
     		{
     			tableString = "INSERT INTO "+tableName+" VALUES (";
     			
     			for (int j=0; j<numCols; j++)
     			{
     				if (j!=0)
     				{
     					//add comma to list of values if this is not the first value
     					tableString+=",";
     				}
     				if (tableTypetoRSType(writeTable.getColumnType(j)).equals("VARCHAR"))
     				{
     					tableString+="\'"+writeTable.getString(i,j)+"\'";
     				}
     				else 
     				{
     					tableString+=writeTable.getObject(i,j);
     				}
     			}
     			tableString+=")";
     			stmt = conn.createStatement();
     			stmt.executeUpdate(tableString);
     			stmt.close();
     		}
     	} catch(SQLException ex) {
     		System.err.println("SQLException: " + ex.getMessage());
     	}
     	
     	//outut connection for future queries 
		cc.pushDataComponentToOutput(DATA_OUTPUT, conn);
     }

     /** This method is called when the Menadre Flow execution is completed.
      *
      * @param ccp The component context properties
      */
     public void dispose ( ComponentContextProperties ccp ) {

     }
}

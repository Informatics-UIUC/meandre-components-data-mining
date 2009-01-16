

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

package org.meandre.components.io.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.components.datatype.table.Column;
import org.meandre.components.datatype.table.ColumnTypes;
import org.meandre.components.datatype.table.MutableTable;
import org.meandre.components.datatype.table.Table;
import org.meandre.components.datatype.table.TableFactory;
import org.meandre.components.datatype.table.basic.BasicTableFactory;


import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
/**
* Given a resultset object from an SQL query, create a table for data-mining
*
* @author Erik Johnson
* 
* 
*/

@Component(
       creator = "Erik Johnson",
       description = "<p>Overview:<br>"+
    	   "Given a resultset object from an SQL query,"+
    	   "create a table for data-mining. The table is a D2K Table object."
    	   +" Each column of the table is from a column in the resultset."
    	   +" Each row of the table correspons to a row from the resultset.",
       name = "Database Resultset to Table",
       tags = "io, table, resultset, conversion",
       baseURL="meandre://seasr.org/components/")

public class RStoTable implements ExecutableComponent {

   @ComponentInput(description = "A query resultset to create a table from", name = "ResultSet")
   final static String DATA_INPUT_PARSER = "ResultSet";
   
   @ComponentInput(description = "Connection to Database", name = "Connection")
   final static String DATA_INPUT_CONNECTION = "Connection";
   
   @ComponentOutput(description = "A table created from the resultset data", name = "table")
   final static String DATA_OUTPUT_TABLE = "table";

   @ComponentOutput(description = "Connection to Datbase", name = "Connection")
   final static String DATA_OUTPUT_CONNECTION = "Connection";
   
   @ComponentProperty(description = "If this is set to true, this component will discard incomplete rows from the resultset." +
           "Incomplete rows have one or more blank columns.", name = "Discard_Incomplete_Rows", defaultValue = "false")
   final static String DATA_PROPERTY_USE_BLANKS = "Discard_Incomplete_Rows";

   protected static final char QUESTION = '?';
   protected static final char SPACE = ' ';

   private boolean discard;
   private Logger _logger;

   /**
    * Setter for useBlanks
    * @param b The new value
    */
   public void setDiscard(boolean b) {
       discard = b;
   }

   /**
    * Getter for useBlanks
    * @return The value of useBlanks
    */
   public boolean getDiscard() {
       return discard;
   }

//Can't make table column of Byte, Byte Array, Char Array, Long, Nominal
//Can't convert Array, BigInt, Bianary, Bit, Blob, Clob, DataLink, Date, Decimal, Distinct, LongVarBianary, LongVarChar, Numeric, Other, Real, Ref, Struct, TimeStamp, TinyInt, VarBianry sql datatypes
   public int rsTypetoTableType (int rsType)
   {
	   if (rsType == Types.INTEGER)
	   {
		   return ColumnTypes.INTEGER;
	   }
	   if (rsType == Types.BOOLEAN)
	   {
		   return ColumnTypes.BOOLEAN;
	   }
	   if (rsType == Types.CHAR)
	   {
		   return ColumnTypes.CHAR;
	   }
	   if (rsType == Types.DOUBLE)
	   {
		   return ColumnTypes.DOUBLE;
	   }
	   if (rsType == Types.FLOAT)
	   {
		   return ColumnTypes.FLOAT;
	   }
	   if (rsType == Types.JAVA_OBJECT)
	   {
		   return ColumnTypes.OBJECT;
	   }
	   if (rsType == Types.SMALLINT)
	   {
		   return ColumnTypes.SHORT;
	   }
	   if (rsType == Types.VARCHAR)
	   {
		   return ColumnTypes.STRING;
	   }
	   if (rsType == Types.BIGINT)
	   {
		   return ColumnTypes.DOUBLE;
	   }
	   if (rsType == Types.BINARY)
	   {
		   return ColumnTypes.BYTE;
	   }
	   if (rsType == Types.BIT)
	   {
		   return ColumnTypes.BYTE;
	   }
	   if (rsType == Types.DATE)
	   {
		   return ColumnTypes.STRING;
	   }
	   if (rsType == Types.DECIMAL)
	   {
		   return ColumnTypes.DOUBLE;
	   }
	   if (rsType == Types.LONGVARCHAR)
	   {
		   return ColumnTypes.STRING;
	   }
	   if (rsType == Types.NUMERIC)
	   {
		   return ColumnTypes.DOUBLE;
	   }
	   if (rsType == Types.TINYINT)
	   {
		   return ColumnTypes.INTEGER;
	   }
	   if (rsType == Types.TIMESTAMP)
	   {
		   return ColumnTypes.STRING;
	   }
	   else 
		   return -1;
   }
   
   public Table createTablefromResultSet(ResultSet rs, TableFactory tf) {
       try{
		    
		   ResultSetMetaData rsmd = rs.getMetaData();
		   int numColumns = rsmd.getColumnCount();
	
		   boolean [] hasTypes = new boolean [numColumns];
		   MutableTable ti = (MutableTable)tf.createTable();
		   
		   boolean removeRow = false;
		   
		  int tableColumns = 0;
		   
		   if (numColumns > 0)
		   {
		       
		       
		       //Column[] columns = new Column[numColumns];
		       for(int i = 1; i < numColumns+1; i++) {
		           int type = rsTypetoTableType(rsmd.getColumnType(i));
		           if (type != -1){
		        	   //columns[i] = ColumnUtilities.createColumn(type, numRows);
		        	   tableColumns++;
		        	   Column c = tf.createColumn(type);
		        	   //c.setNumRows(numRows)
		               hasTypes [i-1] = true;
		               String label = rsmd.getColumnLabel(i);
		               if(label != null)
		                   c.setLabel(label);
		               ti.addColumn(c);
		           }
		           else 
		        	   hasTypes [i-1] = false;
		           // set the label
		       }
		
		       //MutableTableImpl ti = new MutableTableImpl(columns);
		
		       
		       int rowcount = 0;
		       int tableColIndex = 0;
		       while (rs.next())
		       {
		    	   tableColIndex = 0;
		    	
		    	   removeRow = false;
		    	   
		    	   Object[] data = new Object [tableColumns];
		    	   for (int i=1; i <numColumns+1; i++)
		    	   {
		    		   if (hasTypes[i-1])
		    		   {
			    		   data [tableColIndex] = rs.getObject(i);
			    		   
			    		   if (data [tableColIndex] == null)
			    		   {
			    			   if (discard)
			    			   {
			    				   _logger.log(Level.INFO, "EMPTY VALUES, DISCARDING ROW"+rowcount);
			    				   removeRow = true;
			    			   }          
			    		   }
			    		   tableColIndex++;
		    		   }
		    	   } 
		    	   if (!removeRow)
		    	   {
		    		   tableColIndex = 0;
		    		   ti.addRows(1);
		    		   
		    		   for (int i=0; i <tableColumns; i++)
			    	   {
		    			   	   
				    		   //data [tableColIndex] = rs.getObject(i);
			    			   if (data [i] == null)
				    		   {
			    				   //_logger.log(Level.INFO, "Adding Null data");
			    				   ti.setValueToEmpty(true, rowcount, i);
			                       switch (rsTypetoTableType (rsmd.getColumnType(i+1))) {
			                       case ColumnTypes.INTEGER:
			                       case ColumnTypes.SHORT:
			                       case ColumnTypes.LONG:
					    			   try{
					    				   
					    				   ti.setInt(ti.getMissingInt(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                       
					                   }
			                           
			                           break;
			                       case ColumnTypes.DOUBLE:
			                       case ColumnTypes.FLOAT:
					    			   try{
					    				   
					    				   ti.setDouble(ti.getMissingDouble(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                     
					                   }
			                    	   
			                           break;
			                       case ColumnTypes.CHAR_ARRAY:
					    			   try{
					    				   
					    				   ti.setChars(ti.getMissingChars(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                 
					                   }
			                           
			                           break;
			                       case ColumnTypes.BYTE_ARRAY:
					    			   try{
					    				   
					    				   ti.setBytes(ti.getMissingBytes(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                     
					                   }
			                           
			                           break;
			                       case ColumnTypes.BYTE:
					    			   try{
					    				   
					    				   ti.setByte(ti.getMissingByte(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                      
					                   }
			                           
			                           break;
			                       case ColumnTypes.CHAR:
					    			   try{
					    				   
					    				   ti.setChar(ti.getMissingChar(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                       
					                   }
			                          
			                           break;
			                       case ColumnTypes.STRING:
					    			   try{
					    				   
					    				   ti.setString(ti.getMissingString(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                       
					                   }
			                           
			                           break;
			                       case ColumnTypes.BOOLEAN:
					    			   try{
					    				   
					    				   ti.setBoolean(ti.getMissingBoolean(), rowcount, i);
					    			   }
					    			   catch(NumberFormatException e) {
					                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
					                    
					                   }
			                           
			                           break;
			                       default: 				    			   
			                    	   try{
			                    		   ti.setString(ti.getMissingString(), rowcount, i);
			                    	   }
			                       	   catch(NumberFormatException e) {
			                       		   ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
			                       		
			                       	   }
			                       	   break;
			                       }
				    		   }
				    		   else
				    		   {
				    			   try{
				    				   //_logger.log(Level.INFO, "Adding data: "+data[i].toString());
				    				   if (rsTypetoTableType (rsmd.getColumnType(i+1)) == ColumnTypes.STRING)
				    				   {
				    					   String temp = (String) data[i];
				    					   temp = temp.trim();
				    					   ti.setObject(temp, rowcount, i);
				    				   }
				    				   else
				    					   ti.setObject(data[i], rowcount, i);
				    			   }
				    			   catch(NumberFormatException e) {
				                       ti.setChars(Integer.toString(0).toCharArray(), rowcount, i);
				                       ti.setValueToMissing(true, rowcount, i);
				                   }
				    		   }
			    		   
			    	   } 
		    	   }
		    	   
		    	   rowcount++;
		       }
		       return ti;
		   }
		   else
			   return ti;
       }
       catch (SQLException e)
       {
    	   _logger.log(Level.WARNING, "SQL Exception: "+e+" Cannot convert resultset to table");
    	   return null;
       }
   }
   
   
   /*
    * (non-Javadoc)
    * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
    */
   public void initialize(ComponentContextProperties context) {
	    _logger = context.getLogger();
	
	    try {
	    	discard = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_USE_BLANKS));
	    }
	    catch (Exception e) {
	    	_logger.log(Level.SEVERE, "Initialize error: ", e);
	    }
	}

   /*
    * (non-Javadoc)
    * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
    */
	public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
	    _logger.log(Level.INFO, "Getting RS for conversion");
		ResultSet rs = (ResultSet) context.getDataComponentFromInput(DATA_INPUT_PARSER);
		Connection conn= (Connection) context.getDataComponentFromInput(DATA_INPUT_CONNECTION);
		_logger.log(Level.INFO, "Creating Table Factory");
	    TableFactory tf = new BasicTableFactory();
	    _logger.log(Level.INFO, "Creating Table");
	    Table table = createTablefromResultSet(rs, tf);
	    _logger.log(Level.INFO, "Table Created Sucessfully");
	    
	    context.pushDataComponentToOutput(DATA_OUTPUT_CONNECTION, conn);
	    context.pushDataComponentToOutput(DATA_OUTPUT_TABLE, table);
	}

	/*
	 * (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties arg0) {

   }
}


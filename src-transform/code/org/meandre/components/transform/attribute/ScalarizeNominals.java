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

package org.meandre.components.transform.attribute;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.basic.ColumnUtilities;
import org.meandre.core.ExecutableComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;


@Component(creator="Lily Dong",
        description="This module examines columns in a MutableTable and, " +
                    "for appropriate columns which contain nominal values, " +
                    "converts these single columns into multiple columns -- one " +
                    "for each possible value of the attribute." +
                    "Detailed Description: " +
                    "If the input MutableTable implements the " +
                    "ExampleTable interface, only columns marked as " +
                    "inputs and outputs will be converted. Otherwise, all " +
                    "columns containing nominal values will be converted. " +
                    "Through a property of the module, the user can select " +
                    "whether the generated columns are double or boolean." +
                    "Data Handling: " +
                    "This module modifies its input data; each relevant nominal " +
                    "column may be replaced with an arbitrary number of new " +
                    "ones. In addition, columns with blank labels are assigned " +
                    "default ones.",
        name="ScalarizeNominals",
        tags="table, conversion",
        baseURL="meandre://seasr.org/components/data-mining/")


public class ScalarizeNominals implements ExecutableComponent {
    @ComponentInput(description="Read a MutableTable, possibly an ExampleTable.",
                    name= "input_table")
    public final static String DATA_INPUT = "input_table";
    
    @ComponentOutput(description="Output the input table with " +
                                 "appropriate nominal columns transformed.",
                     name="output_table")
    public final static String DATA_OUTPUT = "output_table";
    
    @ComponentProperty(defaultValue="true",
                       description="Controls whether converted nominal columns will have scalar type " +
                                   "boolean (true) or type double (false).",
                       name="newTypeBoolean")
    final static String DATA_PROPERTY = "newTypeBoolean";

   //~ Instance fields *********************************************************

   ////////////////////////////////////////////////////////////////////////////////
   // properties
   // //
   ////////////////////////////////////////////////////////////////////////////////

   /**
    * Controls whether converted nominal columns will have scalar type boolean
    * (true) or type double (false).
    */
   private boolean _newTypeBoolean = true;

   //~ Methods *****************************************************************

   /**
    * Called when a flow is started.
    *
    * @param ccp ComponentContextProperties
    */
   public void initialize(ComponentContextProperties ccp) {
   }
   
   /**
    * Called at the end of an execution flow.
    *
    * @param ccp ComponentContextProperties
    */
   public void dispose(ComponentContextProperties ccp) {
       // TODO Auto-generated method stub
   }
   
   /**
    * When ready for execution.
    *
    * @param cc ComponentContext
    * @throws ComponentExecutionException
    * @throws ComponentContextException
    */
   public void  execute(ComponentContext cc)
       throws ComponentExecutionException, ComponentContextException {
      _newTypeBoolean = Boolean.valueOf(cc.getProperty(DATA_PROPERTY));

      MutableTable table = (MutableTable)(cc.getDataComponentFromInput(DATA_INPUT));

      int[] indices;
      int[] origInputs = null;
      int[] origOutputs = null;

      // columns with blank labels need to be assigned default ones

      for (int i = 0; i < table.getNumColumns(); i++) {
         String s = table.getColumnLabel(i);

         if (s == null || s.length() == 0) {
            table.setColumnLabel("column_" + i, i);
         }
      }

      // determine which columns we wish to transform

      boolean tableIsExample = false;

      if (table instanceof ExampleTable) {

         tableIsExample = true;

         ExampleTable et = (ExampleTable) table;

         origInputs = new int[et.getInputFeatures().length];

         for (int i = 0; i < origInputs.length; i++) {
            origInputs[i] = et.getInputFeatures()[i];
         }

         origOutputs = new int[et.getOutputFeatures().length];

         for (int i = 0; i < origOutputs.length; i++) {
            origOutputs[i] = et.getOutputFeatures()[i];
         }

         // ensure unique column indices

         HashMap uniqueIndexMap = new HashMap();

         for (int i = 0; i < origInputs.length; i++) {

            if (et.isColumnNominal(origInputs[i])) {
               uniqueIndexMap.put(new Integer(origInputs[i]), null);
            }
         }

         for (int i = 0; i < origOutputs.length; i++) {

            if (et.isColumnNominal(origOutputs[i])) {
               uniqueIndexMap.put(new Integer(origOutputs[i]), null);
            }
         }

         // retrieve column indices

         indices = new int[uniqueIndexMap.size()];

         int index = 0;

         Iterator iterator = uniqueIndexMap.keySet().iterator();

         while (iterator.hasNext()) {
            indices[index++] = ((Integer) iterator.next()).intValue();
         }

         Arrays.sort(indices);
      } else {

         // simply iterate to find nominal columns

         int numNominalColumns = 0;

         for (int i = 0; i < table.getNumColumns(); i++) {

            if (table.isColumnNominal(i)) {
               numNominalColumns++;
            }
         }

         indices = new int[numNominalColumns];

         int index = 0;

         for (int i = 0; i < table.getNumColumns(); i++) {

            if (table.isColumnNominal(i)) {
               indices[index++] = i;
            }
         }

      }

      // iterate and replace

      int offset = 0; // number of extra columns added to the table. must be

      // added to column indices in order to keep consistent
      int numRows = table.getNumRows();

      for (int count = 0; count < indices.length; count++) {
         int index = indices[count] + offset;

         // find this column's unique values
         HashMap uniqueValuesMap = new HashMap();
         int uniqueValueCount = 0;

         for (int row = 0; row < numRows; row++) {

            if (table.isValueMissing(row, index)) {
               continue;
            }

            String s = table.getString(row, index);

            if (s == null || s.length() == 0) {
               continue;
            }

            if (uniqueValuesMap.containsKey(s)) {
               continue;
            }

            uniqueValuesMap.put(s, new Integer(uniqueValueCount++));
         }

         if (uniqueValuesMap.size() == 0) {

            // nothing (or only missing) here
            continue;
         } else {

            // first, we'd like our string-to-integer mappings as arrays,
            // for efficiency

            String[] uniqueValues = new String[uniqueValuesMap.size()];
            int[] uniqueValueIndices = new int[uniqueValues.length];

            Iterator iterator = uniqueValuesMap.keySet().iterator();
            int iteratorCount = 0;

            while (iterator.hasNext()) {
               uniqueValues[iteratorCount++] = (String) iterator.next();
            }

            for (int i = 0; i < uniqueValues.length; i++) {
               uniqueValueIndices[i] =
                  ((Integer) uniqueValuesMap.get(uniqueValues[i])).intValue();
            }

            // we also want an indirection array so we can act as if these
            // mappings were sorted on the integer value

            int[] indirection = new int[uniqueValueIndices.length];

            for (int i = 0; i < uniqueValueIndices.length; i++) {
               indirection[uniqueValueIndices[i]] = i;
            }

            // now create one array for the entire column specifying which
            // unique value is contained in each row. if the value is missing
            // or empty, set to -1.

            int[] match = new int[numRows];
            boolean[] missing = new boolean[numRows];

            for (int row = 0; row < numRows; row++) {

               if (
                   table.isValueMissing(row, index) ||
                      table.isValueEmpty(row, index)) {
                  match[row] = -1;
                  missing[row] = true;

                  continue;
               }

               String s = table.getString(row, index);
               missing[row] = false;

               for (int j = 0; j < uniqueValues.length; j++) {

                  if (s.equals(uniqueValues[indirection[j]])) {
                     match[row] = indirection[j];

                     break;
                  }
               }

            }

            // !:
            // are we dealing with an ExampleTable? if so, is the old column
            // an input, output, or both?

            boolean isInput = false;
            boolean isOutput = false;

            if (tableIsExample) {

               ExampleTable et = (ExampleTable) table;

               for (int i = 0; i < origInputs.length; i++) {

                  if (origInputs[i] == indices[count]) {
                     isInput = true;

                     break;
                  }
               }

               for (int i = 0; i < origOutputs.length; i++) {

                  if (origOutputs[i] == indices[count]) {
                     isOutput = true;

                     break;
                  }
               }

            }

            // remove the old column

            String columnLabel = table.getColumnLabel(index);
            Column oldColumn = table.getColumn(index);
            table.removeColumn(index);
            offset--;

            // iterate and create the new columns

            for (int k = 0; k < uniqueValues.length; k++) {

               if (_newTypeBoolean) { // create new columns as type boolean

                  boolean[] newColumn = new boolean[numRows];

                  for (int row = 0; row < match.length; row++) {

                     if (missing[row]) {
                        newColumn[row] = table.getMissingBoolean();
                     } else if (match[row] == k) {
                        newColumn[row] = true;
                     } else {
                        newColumn[row] = false;
                     }
                  }

                  // BooleanColumn column =
                  // (BooleanColumn)ColumnUtilities.toBooleanColumn(oldColumn);
                  Column column = ColumnUtilities.toBooleanColumn(oldColumn);
                  int where = index + k;
                  column.setLabel(columnLabel + "=" + uniqueValues[k]);
                  table.insertColumn(column, where);

                  for (int i = 0; i < newColumn.length; i++) {
                     table.setBoolean(newColumn[i], i, where);
                  }
               } else { // create new columns as type int

                  double[] newColumn = new double[numRows];

                  for (int row = 0; row < match.length; row++) {

                     if (missing[row]) {
                        newColumn[row] = table.getMissingDouble();
                     } else if (match[row] == k) {
                        newColumn[row] = 1;
                     } else {
                        newColumn[row] = 0;
                     }
                  }

// VEREd GOREN - column is now of type Column, so that it supports also sparse
// tables. DoubleColumn column = ColumnUtilities.toDoubleColumn(oldColumn);
                  Column column = ColumnUtilities.toDoubleColumn(oldColumn);
                  int where = index + k;
                  column.setLabel(columnLabel + "=" + uniqueValues[k]);
                  table.insertColumn(column, where);

                  for (int i = 0; i < newColumn.length; i++) {
                     table.setDouble(newColumn[i], i, where);
                  }
               } // end if

               offset++;

               // !: we now must add this new column to the list of
               // inputs/outputs if we are dealing with an ExampleTable. this
               // isn't very efficient; maybe we should modify the API to handle
               // this
               if (tableIsExample) {

                  ExampleTable et = (ExampleTable) table;

                  if (isInput) {

                     int[] inputs = et.getInputFeatures();
                     int[] newInputs = new int[inputs.length + 1];

                     for (int i = 0; i < inputs.length; i++) {
                        newInputs[i] = inputs[i];
                     }

                     newInputs[inputs.length] = index + k;

                     Arrays.sort(newInputs);
                     et.setInputFeatures(newInputs);
                  }

                  if (isOutput) {

                     int[] outputs = et.getOutputFeatures();
                     int[] newOutputs = new int[outputs.length + 1];

                     for (int i = 0; i < outputs.length; i++) {
                        newOutputs[i] = outputs[i];
                     }

                     newOutputs[outputs.length] = index + k;

                     Arrays.sort(newOutputs);
                     et.setOutputFeatures(newOutputs);
                  }
               } // end if
            } // end for
         } // end if
      } // end for
      
      cc.pushDataComponentToOutput(DATA_OUTPUT, table);
   } // end method doit
} // end class ScalarizeNominals

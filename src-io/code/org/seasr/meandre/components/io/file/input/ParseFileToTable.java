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

package org.seasr.meandre.components.io.file.input;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.util.FlatFileParser;
import org.seasr.datatypes.datamining.table.util.ParsedLine;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * Given a FlatFileReader, create a TableImpl initialized with its contents.
 *
 * @author Boris Capitanu
 *
 * BC: Imported from d2k (ncsa.d2k.modules.core.io.file.input.ParseFileToTable)
 */

@Component(
        creator = "Boris Capitanu",
        description = "Given a FlatFileParser, this module creates a Table " +
        "initialized with the contents of a flat file from disk.",
        name = "Parse File To Table",
        tags = "io, table",
        baseURL="meandre://seasr.org/components/data-mining/"
)
public class ParseFileToTable extends AbstractExecutableComponent {

    @ComponentInput(description = "A FlatFileParser to read data from", name = "parser")
    final static String IN_PARSER = "parser";
    @ComponentInput(description = "Inputs a new TableFactory instance", name = "table_factory")
	public final static String IN_TABLE_FACTORY = "table_factory";

    @ComponentOutput(description = "A Table with the data from the file reader", name = "table")
    final static String OUT_TABLE = "table";

    @ComponentProperty(description = "When true, any blank entries in the file will be set " +
            "as missing values in the table", name = "use_blanks", defaultValue = "True")
    final static String PROP_USE_BLANKS = "use_blanks";

    protected static final char QUESTION = '?';
    protected static final char SPACE = ' ';

    private boolean useBlanks;

    /**
     * Setter for useBlanks
     * @param b The new value
     */
    public void setUseBlanks(boolean b) {
        useBlanks = b;
    }

    /**
     * Getter for useBlanks
     * @return The value of useBlanks
     */
    public boolean getUseBlanks() {
        return useBlanks;
    }

    /**
     * Creates a table from a file parser
     *
     * @param df The file parser
     * @param tf The table factory
     * @return The table containing the data from the file reader
     */
    public Table createTable(FlatFileParser df, TableFactory tf) {
        int numRows = df.getNumRows();
        int numColumns = df.getNumColumns();

        boolean hasTypes = false;

        MutableTable ti = (MutableTable)tf.createTable();

        //Column[] columns = new Column[numColumns];
        for(int i = 0; i < numColumns; i++) {
            int type = df.getColumnType(i);
            //columns[i] = ColumnUtilities.createColumn(type, numRows);
            Column c = tf.createColumn(type);
            c.setNumRows(numRows);

            if(type != -1)
                hasTypes = true;

            // set the label
            String label = df.getColumnLabel(i);
            if(label != null)
                c.setLabel(label);

            ti.addColumn(c);
        }

        //MutableTableImpl ti = new MutableTableImpl(columns);


        for(int i = 0; i < numRows; i++) {
            ParsedLine pl = df.getRowElements(i);
            char[][] row = pl.elements;
            boolean[] blanks = pl.blanks;
            if(row != null) {
                for(int j = 0; j < numColumns; j++) {
                    boolean isMissing = true;
                    char[] elem = row[j];//(char[])row.get(j);
                    // test to see if this is '?'
                    // if it is, this value is missing.
                    for(int k = 0; k < elem.length; k++) {
                        if(elem[k] != QUESTION && elem[k] != SPACE) {
                            isMissing = false;
                            break;
                        }
                    }

                    // if the value was not missing, just put it in the table
                    if(!isMissing && !blanks[j]) {
                        try {
                            ti.setChars(elem, i, j);
                        }
                        // if there was a number format exception, set the value
                        // to 0 and mark it as missing
                        catch(NumberFormatException e) {
                            ti.setChars(Integer.toString(0).toCharArray(), i, j);
                            ti.setValueToMissing(true, i, j);
                        }
                    }
                    // if the value was missing..
                    else {
                        // put 0 in a numeric column and set the value to missing
                        ti.setValueToMissing(true, i, j);
                        switch (df.getColumnType(j)) {
                        case ColumnTypes.INTEGER:
                        case ColumnTypes.SHORT:
                        case ColumnTypes.LONG:
                            ti.setInt(ti.getMissingInt(), i, j);
                            break;
                        case ColumnTypes.DOUBLE:
                        case ColumnTypes.FLOAT:
                            ti.setDouble(ti.getMissingDouble(), i, j);
                            break;
                        case ColumnTypes.CHAR_ARRAY:
                            ti.setChars(ti.getMissingChars(), i, j);
                            break;
                        case ColumnTypes.BYTE_ARRAY:
                            ti.setBytes(ti.getMissingBytes(), i, j);
                            break;
                        case ColumnTypes.BYTE:
                            ti.setByte(ti.getMissingByte(), i, j);
                            break;
                        case ColumnTypes.CHAR:
                            ti.setChar(ti.getMissingChar(), i, j);
                            break;
                        case ColumnTypes.STRING:
                            ti.setString(ti.getMissingString(), i, j);
                            break;
                        case ColumnTypes.BOOLEAN:
                            ti.setBoolean(ti.getMissingBoolean(), i, j);
                            break;
                        }
                    }
                }
            }
        }

        // if types were not specified, we should try to convert to double columns
        // where appropriate
        if(!hasTypes) {

            // System.out.println("no types");

            // for each column
            for(int i = 0; i < numColumns; i++) {
                boolean isNumeric = true;
                double[] newCol = new double[numRows];

                // for each row
                for(int j = 0; j < numRows; j++) {
                    String s = ti.getString(j, i);

                    if (ti.isValueMissing(j, i) || ti.isValueEmpty(j, i))
                        continue;

                    try {
                        double d = Double.parseDouble(s);
                        newCol[j] = d;
                    }
                    catch(NumberFormatException e) {
                        isNumeric = false;
                    }
                    if(!isNumeric)
                        break;
                }

                if(isNumeric) {
                    //DoubleColumn dc = new DoubleColumn(newCol);
                    Column dc = tf.createColumn(ColumnTypes.DOUBLE);
                    dc.setNumRows(ti.getNumRows());
                    dc.setLabel(ti.getColumnLabel(i));

                    for (int k = 0; k < ti.getNumRows(); k++) {
                        dc.setDouble(newCol[k], k);
                        if (ti.isValueMissing(k, i))
                            dc.setValueToMissing(true, k);
                        if (ti.isValueEmpty(k, i))
                            dc.setValueToEmpty(true, k);
                    }

                    ti.setColumn(dc, i);
                }
            }
        }
        return ti;
    }

    @Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    	useBlanks = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_USE_BLANKS, ccp));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
	    FlatFileParser fle = (FlatFileParser) cc.getDataComponentFromInput(IN_PARSER);
	    TableFactory tf = (TableFactory) cc.getDataComponentFromInput(IN_TABLE_FACTORY);
	    Table table = createTable(fle, tf);

	    cc.pushDataComponentToOutput(OUT_TABLE, table);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

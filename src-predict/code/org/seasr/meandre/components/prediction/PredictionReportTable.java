/**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */

package org.seasr.meandre.components.prediction;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.PredictionTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.basic.BasicTableFactory;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
*
* @author Boris Capitanu
*
*/

@Component(
       name = "Prediction Report Table",
       creator = "Boris Capitanu",
       baseURL = "meandre://seasr.org/components/data-mining/",
       firingPolicy = FiringPolicy.all,
       mode = Mode.compute,
       rights = Licenses.UofINCSA,
       tags = "prediction, report",
       description = "This component creates a table containing the ID and the predicted class for each example in the prediction table." ,
       dependency = { "protobuf-java-2.2.0.jar" }
)
public class PredictionReportTable extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TABLE,
            description = "The prediction table" +
                    "<br>TYPE: org.seasr.datatypes.datamining.table.PredictionTable"
    )
    protected static final String IN_PRED_TABLE = Names.PORT_TABLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TABLE,
            description = "The prediction result table" +
                    "<br>TYPE: org.seasr.datatypes.table.Table"
    )
    protected static final String OUT_RESULT_TABLE = Names.PORT_TABLE;

    //--------------------------------------------------------------------------------------------


    protected static final TableFactory TABLE_FACTORY = new BasicTableFactory();

    // Note: By convention the ID is stored in column 1 while the label (possibly empty for unlabeled data) in column 0
    // @see org.seasr.meandre.components.transform.text.TokenCountsToFeatureTable
    protected static final int ID_COLUMN_IDX = 1;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        PredictionTable predictionTable = (PredictionTable) cc.getDataComponentFromInput(IN_PRED_TABLE);
        int numRows = predictionTable.getNumRows();
        int[] predictionSet = predictionTable.getPredictionSet();
        int[] outputSet = predictionTable.getOutputFeatures();

        console.fine(String.format("The prediction table has %,d row(s), with %,d prediction column(s)", numRows, predictionSet.length));

        MutableTable predictionResultTable = (MutableTable) TABLE_FACTORY.createTable();
        Column colId = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
        colId.setLabel(predictionTable.getColumnLabel(ID_COLUMN_IDX));  // ID stored in column 1 by convention

        Column[] outputColumns = new Column[outputSet.length];
        for (int i = 0; i < outputColumns.length; i++) {
            Column colOutput = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
            colOutput.setLabel(predictionTable.getColumnLabel(outputSet[i]));
            outputColumns[i] = colOutput;
        }

        Column[] predictionColumns = new Column[predictionSet.length];
        for (int i = 0; i < predictionColumns.length; i++) {
            Column colPrediction = TABLE_FACTORY.createColumn(ColumnTypes.STRING);
            colPrediction.setLabel(predictionTable.getColumnLabel(predictionSet[i]));
            predictionColumns[i] = colPrediction;
        }

        predictionResultTable.addColumn(colId);
        predictionResultTable.addColumns(outputColumns);
        predictionResultTable.addColumns(predictionColumns);
        predictionResultTable.addRows(numRows);

        for (int row = 0; row < numRows; row++) {
            predictionResultTable.setString(predictionTable.getString(row, ID_COLUMN_IDX), row, 0);
            int offset = 1;
            for (int col = 0; col < outputColumns.length; col++)
                predictionResultTable.setString(predictionTable.getObject(row, outputSet[col]).toString(), row, col + offset);
            offset += outputColumns.length;
            for (int col = 0; col < predictionColumns.length; col++)
                predictionResultTable.setString(predictionTable.getObject(row, predictionSet[col]).toString(), row, col + offset);
        }

        cc.pushDataComponentToOutput(OUT_RESULT_TABLE, predictionResultTable);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

}

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.PredictionTable;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.basic.BasicTableFactory;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
*
* @author Boris Capitanu
*
*/

@Component(
       name = "Confusion Matrix",
       creator = "Boris Capitanu",
       baseURL = "meandre://seasr.org/components/data-mining/",
       firingPolicy = FiringPolicy.all,
       mode = Mode.compute,
       rights = Licenses.UofINCSA,
       tags = "confusion matrix, contingency table",
       description = "This component creates the confusion matrix for a prediction result. " +
               "If multiple predicted classes exist in the prediction table, this component will output " +
               "one confusion matrix for each predicted class." ,
       dependency = { "protobuf-java-2.2.0.jar" }
)
public class ConfusionMatrix extends AbstractStreamingExecutableComponent {

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
            description = "The confusion matrix (aka contingency table)" +
                    "<br>TYPE: org.seasr.datatypes.table.Table"
    )
    protected static final String OUT_CONTINGENCY_TABLE = Names.PORT_TABLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected static final TableFactory TABLE_FACTORY = new BasicTableFactory();
    protected boolean _wrapStream;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        PredictionTable predictionTable = (PredictionTable) cc.getDataComponentFromInput(IN_PRED_TABLE);

        int[] outputFeatures = predictionTable.getOutputFeatures();
        int[] predictionSet = predictionTable.getPredictionSet();

        console.fine(String.format("Prediction table has %,d output feature column(s) and %,d prediction column(s)",
                outputFeatures.length, predictionSet.length));

        assert (outputFeatures.length == predictionSet.length);

        if (_wrapStream) {
            StreamDelimiter sd = new StreamInitiator(streamId);
            cc.pushDataComponentToOutput(OUT_CONTINGENCY_TABLE, sd);
        }

        for (int i = 0; i < outputFeatures.length; i++) {
            int actualCol = outputFeatures[i];
            int predictionCol = predictionSet[i];

            Set<String> classNames = new TreeSet<String>();
            for (int row = 0, rowMax = predictionTable.getNumRows(); row < rowMax; row++) {
                classNames.add(predictionTable.getObject(row, actualCol).toString());
                classNames.add(predictionTable.getObject(row, predictionCol).toString());
            }

            int numClasses = classNames.size();

            // Note: Confusion Matrix and Contingency Table are interchangeable terms that mean the same thing
            MutableTable contingencyTable = (MutableTable) TABLE_FACTORY.createTable();

            // Set up the columns of the contingency table
            Column[] predictionColumns = new Column[numClasses + 1];
            predictionColumns[0] = TABLE_FACTORY.createColumn(ColumnTypes.STRING);  // the column holding the actual class names
            predictionColumns[0].setLabel("class");

            Iterator<String> classNameIterator = classNames.iterator();
            for (int col = 1, colMax = predictionColumns.length; col < colMax; col++) {
                Column predictionColumn = TABLE_FACTORY.createColumn(ColumnTypes.INTEGER);
                predictionColumn.setLabel(classNameIterator.next());
                predictionColumns[col] = predictionColumn;
            }

            contingencyTable.addColumns(predictionColumns);
            contingencyTable.addRows(numClasses);

            Map<String, Integer> classIdxMap = new HashMap<String, Integer>();
            int index = 0;
            for (String className : classNames)
                classIdxMap.put(className, index++);

            // Calculate the confusion matrix
            int[][] confusionMatrix = new int[numClasses][numClasses];
            for (int row = 0, rowMax = predictionTable.getNumRows(); row < rowMax; row++) {
                Object actual = predictionTable.getObject(row, actualCol);
                Object prediction = predictionTable.getObject(row, predictionCol);

                int actualIdx = classIdxMap.get(actual);
                int predictionIdx = classIdxMap.get(prediction);

                confusionMatrix[actualIdx][predictionIdx]++;
            }

            if (console.isLoggable(Level.FINE)) {
                // Dump out the confusion matrix
                StringBuilder sb = new StringBuilder();
                for (int row = 0; row < numClasses; row++) {
                    for (int col = 0; col < numClasses; col++)
                        sb.append(confusionMatrix[row][col]).append("  ");
                    sb.append("\n");
                }
                console.fine(String.format("Confusion matrix with size [%d, %d] and classes %s:\n%s",
                        numClasses, numClasses, classNames, sb.toString()));
            }

            classNameIterator = classNames.iterator();
            for (int row = 0; row < numClasses; row++) {
                contingencyTable.setString(classNameIterator.next(), row, 0);
                for (int col = 1, colMax = predictionColumns.length; col < colMax; col++)
                    contingencyTable.setInt(confusionMatrix[row][col-1], row, col);
            }

            cc.pushDataComponentToOutput(OUT_CONTINGENCY_TABLE, contingencyTable);
        }

        if (_wrapStream) {
            StreamDelimiter sd = new StreamTerminator(streamId);
            cc.pushDataComponentToOutput(OUT_CONTINGENCY_TABLE, sd);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }

}

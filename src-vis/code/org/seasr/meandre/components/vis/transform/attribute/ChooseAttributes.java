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

package org.seasr.meandre.components.vis.transform.attribute;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;


/**
 * ChooseAttributes.java (previously ChooseFields) Allows the user to choose
 * which columns of a table are inputs and outputs. Then assigns them in an
 * ExampleTable.
 *
 * @author  $Author: mcgrath $
 * @version $Revision: 3020 $, $Date: 2007-05-18 16:37:56 -0500 (Fri, 18 May 2007) $
 */

/**
 * Imported from d2k (ncsa.d2k.modules.core.transform.attribute.ChooseAttributes)
 *
 * @author Boris Capitanu
 * @author Lily Dong
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>This module allows the user to choose which columns of a table are inputs and outputs." +
                      "</p><p>Detailed Description: " +
                      "This module outputs an <i>Example Table</i> with the input and output features assigned. " +
                      "Inputs and outputs do not have to be selected, nor do they have to be mutually exclusive. " +
                      "</p><p>Data Handling: " +
                      "This module does not modify the data in the table. It only sets the input and output features.",
        name = "Choose Attributes",
        tags = "transform",
        rights = Licenses.UofINCSA,
        mode = Mode.webui,
        baseURL = "meandre://seasr.org/components/data-mining/"
)
public class ChooseAttributes extends AbstractExecutableComponent implements WebUIFragmentCallback {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            description = "The Table to choose inputs and outputs from" +
            		      "<br>TYPE: org.seasr.datatypes.datamining.table.Table",
            name = "table"
    )
    protected final static String DATA_INPUT_TABLE = "table";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The Example Table with input and output features assigned" +
            		      "<br>TYPE: org.seasr.datatypes.datamining.table.ExampleTable",
            name = "example_table"
    )
    protected final static String DATA_OUTPUT_EXAMPLE_TABLE = "example_table";

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Set to 'true' if selection of an output attribute is required",
            name = "require_output_selection",
            defaultValue = "true"
    )
    protected final static String DATA_PROPERTY_REQUIRE_OUTPUT = "require_output_selection";

    //--------------------------------------------------------------------------------------------


    private String[] attributeLabels;
    private ArrayList<String> selectedInputs;
    private ArrayList<String> selectedOutputs;
    private boolean requireOutputSelection;

    private String executionInstanceId;
    private boolean _done;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        requireOutputSelection = Boolean.parseBoolean(getPropertyOrDieTrying(DATA_PROPERTY_REQUIRE_OUTPUT, true, true, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        executionInstanceId = cc.getExecutionInstanceID();

        selectedInputs = new ArrayList<String>();
        selectedOutputs = new ArrayList<String>();

        // get the input Table (or ExampleTable)
        Table table = (Table) cc.getDataComponentFromInput(DATA_INPUT_TABLE);
        console.fine("Input table is of type: " + table.getClass().getName());

        attributeLabels = new String[table.getNumColumns()];
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>(attributeLabels.length);

        for (int i = 0, iMax = attributeLabels.length; i < iMax; i++) {
            String columnLabel = table.getColumnLabel(i);

            if (columnLabel.equals(""))
                columnLabel = String.format("Column %d", i);

            attributeLabels[i] = columnLabel;
            indexMap.put(columnLabel, i);
        }

        if (table instanceof ExampleTable) {
            ExampleTable et = (ExampleTable)table;
            int[] inputFeatures = et.getInputFeatures();
            int[] outputFeatures = et.getOutputFeatures();

            if (inputFeatures != null)
                for (int i = 0, iMax = inputFeatures.length; i < iMax; i++)
                    selectedInputs.add(et.getColumnLabel(inputFeatures[i]));

            if (outputFeatures != null)
                for (int i = 0, iMax = outputFeatures.length; i < iMax; i++)
                    selectedOutputs.add(et.getColumnLabel(outputFeatures[i]));
        }

        _done = false;

        cc.startWebUIFragment(this);

        while (!cc.isFlowAborting() && !_done)
            Thread.sleep(1000);

        if (cc.isFlowAborting())
            console.info("Flow abort requested - terminating component execution...");
        else {
            if (selectedInputs.size() == 0 && selectedOutputs.size() == 0)
                throw new ComponentExecutionException(executionInstanceId +
                        ": No inputs or outputs were selected - cannot continue.");

            ExampleTable exampleTable = table.toExampleTable();

            // Set the input features
            int[] inputFeatures = new int[selectedInputs.size()];
            for (int i = 0, iMax = selectedInputs.size(); i < iMax; i++)
                inputFeatures[i] = indexMap.get(selectedInputs.get(i));
            exampleTable.setInputFeatures(inputFeatures);

            // Set the output features
            if (selectedOutputs.size() > 0) {
                int[] outputFeatures = new int[selectedOutputs.size()];
                for (int i = 0, iMax = selectedOutputs.size(); i < iMax; i++)
                    outputFeatures[i] = indexMap.get(selectedOutputs.get(i));
                exampleTable.setOutputFeatures(outputFeatures);
            }

            // Send the result
            cc.pushDataComponentToOutput(DATA_OUTPUT_EXAMPLE_TABLE, exampleTable);
        }

        cc.stopWebUIFragment(this);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "emptyRequest", response);

        try {
            response.getWriter().println(getViz());
        }
        catch (Exception e) {
            throw new WebUIException(e);
        }

        console.exiting(getClass().getName(), "emptyRequest");
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
        console.entering(getClass().getName(), "handle", response);

        if (request.getParameterMap().isEmpty() && request.getMethod().equals("GET"))
            emptyRequest(response);

        else

        if (request.getMethod().equals("POST")) {
            String[] inputs = request.getParameterValues("inputs");
            String[] outputs = request.getParameterValues("outputs");

            boolean missingOutputs = requireOutputSelection && (outputs == null || outputs.length == 0);
            if (inputs == null || inputs.length == 0 || missingOutputs) {
                // return error message
                try {
                    PrintWriter writer = response.getWriter();
                    writer.println("<html><head><title>Choose Attributes</title></head><body>");
                    writer.print("<h3 style='color: red;'>You need to select at least one input");
                    if (requireOutputSelection)
                        writer.print(" and one output");
                    writer.println(" attribute.</h3><p/>");

                    writer.println("<form><input type='button' value='Go back' onClick='history.back()'/></form>");
                    writer.println("</body></html>");
                }
                catch (IOException e) {
                    throw new WebUIException(e);
                }
            } else { //assuming 'Submit' has been pressed

                selectedInputs.clear();
                selectedOutputs.clear();

                selectedInputs.addAll(Arrays.asList(inputs));
                if (outputs != null && outputs.length > 0)
                    selectedOutputs.addAll(Arrays.asList(outputs));

                _done = true;

                //redirect the browser back to the webui's main url so any
                //subsequent visualizations will appear automatically
                try {
                    response.getWriter().println("<html><head><meta http-equiv='REFRESH' content='1;url=/'></head><body></body></html>");
                }
                catch (IOException e) {
                    throw new WebUIException(e);
                }
            }
        }

        console.exiting(getClass().getName(), "handle");
    }

    //--------------------------------------------------------------------------------------------

    private String getViz() {
        int maxEntriesToDisplay = Math.min(attributeLabels.length, 20);

        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<head><title>Choose Attributes</title></head>");
        sb.append("<body>");
        sb.append("<script language='JavaScript' type='text/javascript'>");
        sb.append("   function checkSelection() {" +
                "         var submit = document.frmSelectAttributes.submitButton; " +
                "         var inputs = document.frmSelectAttributes.inputs; " +
                "         var outputs = document.frmSelectAttributes.outputs; " +

                ((requireOutputSelection) ?
        		"         submit.disabled = ((inputs.selectedIndex == -1) || (outputs.selectedIndex == -1)); " :
        		"         submit.disabled = (inputs.selectedIndex == -1); ") +
        		"     } " +
        		"" +
        		"     function checkDuplicates(source, dest) {" +
        		"         for (var i = 0; i < source.options.length; i++) {" +
        		"             var srcOpt = source.options[i];" +
        		"             var destOpt = document.getElementById(dest.name + ':' + srcOpt.value);" +
        		"             if (destOpt != null) {" +
        		"                 if (srcOpt.selected)" +
        		"                     destOpt.selected = false;" +
        		"                 destOpt.disabled = srcOpt.selected;" +
        		"             }" +
        		"         }" +
        		"     } ");
        sb.append("</script>");
        sb.append("<form name='frmSelectAttributes' method='POST' action='/" + executionInstanceId + "'>");
        sb.append("<table border='0'><tr>");
        sb.append("<td><fieldset><legend>Input Attributes</legend>");
        sb.append("<select name='inputs' size='" + maxEntriesToDisplay + "' style='width: 100%' multiple " +
        		"onChange='checkDuplicates(document.frmSelectAttributes.inputs, document.frmSelectAttributes.outputs); " +
        		"checkSelection();'>");

        for (String inputLabel : attributeLabels) {
            sb.append("<option id='inputs:" + inputLabel + "' value='" + inputLabel + "'" +
                    (selectedInputs.contains(inputLabel) ? " selected" : "") + ">" + inputLabel + "</option>");
        }

        sb.append("</select></fieldset></td>");
        sb.append("<td><fieldset><legend>Output Attributes</legend>");
        sb.append("<select name='outputs' size='" + maxEntriesToDisplay + "' style='width: 100%' multiple " +
                "onChange='checkDuplicates(document.frmSelectAttributes.outputs, document.frmSelectAttributes.inputs); " +
                "checkSelection();'>");

        for (String outputLabel : attributeLabels) {
            sb.append("<option id='outputs:" + outputLabel + "' value='" + outputLabel + "'" +
                    (selectedOutputs.contains(outputLabel) ? " selected" : "") + ">" + outputLabel + "</option>");
        }

        sb.append("</select></fieldset></td>");
        sb.append("</tr></table>");
        sb.append("<input type='HIDDEN' name='done' value='true'/>");
        sb.append("<p><input type='SUBMIT' id='submitButton' value='Submit'/></p>");
        sb.append("</form>");
        sb.append("<script language='JavaScript' type='text/javascript'>");
        sb.append("checkSelection();");
        sb.append("</script>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }
}

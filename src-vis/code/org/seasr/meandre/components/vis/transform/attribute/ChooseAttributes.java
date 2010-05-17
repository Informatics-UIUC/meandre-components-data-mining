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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Table;


import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Mode;


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
        mode = Mode.webui,
        baseURL="meandre://seasr.org/components/data-mining/")
        
public class ChooseAttributes implements ExecutableComponent, WebUIFragmentCallback {

    @ComponentInput(description = "The Table to choose inputs and outputs from", name = "table")
    final static String DATA_INPUT_TABLE = "table";

    @ComponentOutput(description = "The Example Table with input and output features assigned", name = "example_table")
    final static String DATA_OUTPUT_EXAMPLE_TABLE = "example_table";

    @ComponentProperty(description = "Set to 'true' if selection of an output attribute is required",
                       name = "require_output_selection",
                       defaultValue = "true")
    final static String DATA_PROPERTY_REQUIRE_OUTPUT = "require_output_selection";



    //~ Instance fields *********************************************************

    /** The WebUI fragment semaphore */
    private Semaphore semaphore = new Semaphore(1, true);

    private String[] attributeLabels;
    private ArrayList<String> selectedInputs;
    private ArrayList<String> selectedOutputs;
    private boolean requireOutputSelection;

    private String executionInstanceId;

    //url of the webui, to redirect to when done
    private String webUIUrl;

    public void initialize(ComponentContextProperties context) {
        try {
            requireOutputSelection = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_REQUIRE_OUTPUT));
        }
        catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Initialize error: ", e);
            throw new RuntimeException(e);
        }
    }

    public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
        executionInstanceId = context.getExecutionInstanceID();
        webUIUrl = context.getWebUIUrl(true).toString();

        selectedInputs = new ArrayList<String>();
        selectedOutputs = new ArrayList<String>();

        // get the input Table (or ExampleTable)
        Table table = (Table) context.getDataComponentFromInput(DATA_INPUT_TABLE);

        attributeLabels = new String[table.getNumColumns()];
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>(attributeLabels.length);

        for (int i = 0; i < attributeLabels.length; i++) {
            String columnLabel = table.getColumnLabel(i);

            if (columnLabel.equals(""))
                columnLabel = new String("Column " + Integer.toString(i));

            attributeLabels[i] = columnLabel;
            indexMap.put(columnLabel, i);
        }

        if (table instanceof ExampleTable) {
            ExampleTable et = (ExampleTable)table;
            int[] inputFeatures = et.getInputFeatures();
            int[] outputFeatures = et.getOutputFeatures();

            if (inputFeatures != null) {
                for (int i = 0; i < inputFeatures.length; i++)
                    selectedInputs.add(et.getColumnLabel(inputFeatures[i]));
            }

            if (outputFeatures != null) {
                for (int i = 0; i < outputFeatures.length; i++)
                    selectedOutputs.add(et.getColumnLabel(outputFeatures[i]));
            }
        }

        try {
            semaphore.acquire();
            context.startWebUIFragment(this);
            semaphore.acquire();
            semaphore.release();

            context.stopWebUIFragment(this);
        } catch (InterruptedException e) {
            throw new ComponentExecutionException(e);
        }

        if (selectedInputs.size() == 0 || selectedOutputs.size() == 0)
            throw new ComponentExecutionException(executionInstanceId +
                    ": No inputs or outputs were selected - cannot continue.");

        ExampleTable exampleTable = table.toExampleTable();

        // Set the input features
        int[] inputFeatures = new int[selectedInputs.size()];
        for (int i = 0; i < selectedInputs.size(); i++)
            inputFeatures[i] = indexMap.get(selectedInputs.get(i));
        exampleTable.setInputFeatures(inputFeatures);

        // Set the output features
        int[] outputFeatures = new int[selectedOutputs.size()];
        for (int i = 0; i < selectedOutputs.size(); i++)
            outputFeatures[i] = indexMap.get(selectedOutputs.get(i));
        exampleTable.setOutputFeatures(outputFeatures);

        // Send the result
        context.pushDataComponentToOutput(DATA_OUTPUT_EXAMPLE_TABLE, exampleTable);
    }

    public void dispose(ComponentContextProperties context) { }

    public void emptyRequest(HttpServletResponse response) throws WebUIException {
        try {
            response.getWriter().println(getViz());
        } catch (IOException e) {
            throw new WebUIException(e);
        }
    }


    public void handle(HttpServletRequest request, HttpServletResponse response)
    throws WebUIException {

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { //assuming 'done' has been pressed, releasing webui

            selectedInputs.clear();
            selectedOutputs.clear();

            selectedInputs.addAll(Arrays.asList(inputs));
            if (outputs != null)
                selectedOutputs.addAll(Arrays.asList(outputs));

             //redirect the browser back to the webui's main url so any
            //subsequent visualizations will appear automatically
            try{
                PrintWriter writer = response.getWriter();
                writer.println("<html><head><title>Choose Attributes</title>");
                writer.println("<meta http-equiv='REFRESH' content='0;url=/'></HEAD>");
                writer.println("<body>Choose Attributes Releasing Display</body></html>");
            }catch (IOException e) {
                e.printStackTrace();
            }
            semaphore.release();
        }
    }

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
        sb.append("<p><input type='SUBMIT' id='submitButton' value='Submit' disabled/></p>");
        sb.append("</form>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

} // end class ChooseAttributes

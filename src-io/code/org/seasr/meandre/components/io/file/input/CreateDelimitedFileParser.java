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
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.io.file.input.DelimitedFileParserFromURL;
import org.seasr.meandre.support.generic.io.webdav.WebdavClient;


/**
 * Create a DelimitedFileReader for a file

 * @author mcgrath (original)
 * @author Boris Capitanu
 * @author Lily Dong
 *
 * BC: Imported from d2k (ncsa.d2k.modules.core.io.file.input.CreateDelimitedParserFromURL)
 */

@Component(
        creator = "Boris Capitanu",
        description = "This module creates a parser for the specified WevdavClient. " +
        "The file is expected to have a consistent delimiter character.</p>" +

        "<p>Detailed Description: <br/>" +
        "This module creates a parser that can be used to read data from a file that uses a single delimiter " +
        "character to separate the data into fields. The delimiter can be found automatically, or it can be " +
        "input in the properties editor.  If the delimiter is to be found automatically, the file must " +
        "contain at least 2 rows. The file can contain a row of labels, and a row of data " +
        "types.  These are also specified via the properties editor." +
        "Properties are used to specify the delimiter, the labels row number, " +
        "and the types row number. The row numbers are indexed from zero." +
        "Typically the File Parser output port of this " +
        "module is connected to the File Parser input port of " +
        "a module whose name begins with 'Parse File', for example, " +
        "Parse File To Table or  Parse File To Paging Table." +
        "Data Type Restrictions: " +
        "The input to this module must be a delimited file. If the file is " +
        "large a java OutOfMemory error might occur. <p>Data Handling: " +
        "The module does not destroy or modify the input data.",
        name = "Create Delimited File Parser",
        tags = "file parser",
        baseURL="meandre://seasr.org/components/data-mining/")

public class CreateDelimitedFileParser extends AbstractExecutableComponent {

    @ComponentInput(description = "WebdavClient pointing to a resource",
                    name = "webdavClient")
    final static String IN_CLIENT= "webdavClient";

    @ComponentInput(description = "URL pointing to a resource location.",
                    name = "url")
    final static String IN_URL = "url";

    @ComponentOutput(description = "A Delimited File Parser for the specified file", name = "parser")
    final static String OUT_PARSER = "parser";

    @ComponentProperty(description = "This is the index of the labels row in the file, " +
            "or -1 if there is no labels row", name = "labelsRowIndex", defaultValue = "0")
    final static String PROP_LABELSROWINDEX = "labelsRowIndex";

    @ComponentProperty(description = "This is the index of the types row in the file, " +
            "or -1 if there is no types row", name = "typesRowIndex", defaultValue = "1")
    final static String PROP_TYPESROWINDEX = "typesRowIndex";

    @ComponentProperty(description = "The delimiter of this file " +
            "if it is different than space, tab '|' or '='", name = "delimiter", defaultValue = "default")
    final static String PROP_DELIMITER = "delimiter";

    //~ Instance fields *********************************************************

    /** Description of field hasLabels. */
    private boolean hasLabels = true;

    /** Description of field hasSpecDelim. */
    private boolean hasSpecDelim = false;

    /** Description of field hasTypes. */
    private boolean hasTypes = true;

    /** Description of field labelsRow. */
    private int labelsRow;

    /** Description of field specDelim. */
    private String specDelim = null;

    /** Description of field typesRow. */
    private int typesRow;

    //~ Methods *****************************************************************

    /**
     * Description of method getHasLabels.
     *
     * @return Description of return value.
     */
    public boolean getHasLabels() { return hasLabels; }

    /**
     * Description of method getHasSpecDelim.
     *
     * @return Description of return value.
     */
    public boolean getHasSpecDelim() { return hasSpecDelim; }

    /**
     * Description of method getHasTypes.
     *
     * @return Description of return value.
     */
    public boolean getHasTypes() { return hasTypes; }

    /**
     * Description of method getLabelsRow.
     *
     * @return Description of return value.
     */
    public int getLabelsRow() { return labelsRow; }

    /**
     * Description of method getSpecDelim.
     *
     * @return Description of return value.
     */
    public String getSpecDelim() { return specDelim; }

    /**
     * Description of method getTypesRow.
     *
     * @return Description of return value.
     */
    public int getTypesRow() { return typesRow; }

    /**
     * Description of method setHasLabels.
     *
     * @param b Description of parameter b.
     */
    public void setHasLabels(boolean b) { hasLabels = b; }

    /**
     * Description of method setHasSpecDelim.
     *
     * @param b Description of parameter b.
     */
    public void setHasSpecDelim(boolean b) { hasSpecDelim = b; }

    /**
     * Description of method setHasTypes.
     *
     * @param b Description of parameter b.
     */
    public void setHasTypes(boolean b) { hasTypes = b; }

    /**
     * Description of method setLabelsRow.
     *
     * @param i Description of parameter i.
     */
    public void setLabelsRow(int i) { labelsRow = i; }

    /**
     * Description of method setSpecDelim.
     *
     * @param s Description of parameter s.
     */
    public void setSpecDelim(String s) { specDelim = s; }

    /**
	 * Description of method setTypesRow.
	 *
	 * @param i Description of parameter i.
	 */
	public void setTypesRow(int i) { typesRow = i; }

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		labelsRow = Integer.parseInt(getPropertyOrDieTrying(PROP_LABELSROWINDEX, ccp));
		typesRow = Integer.parseInt(getPropertyOrDieTrying(PROP_TYPESROWINDEX, ccp));

		String strDelim = getPropertyOrDieTrying(PROP_DELIMITER, ccp);
		if (strDelim.equals("default")) {
			setHasSpecDelim(false);
			setSpecDelim(null);
		} else {
			setSpecDelim(strDelim);
			setHasSpecDelim(true);
		}
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String url = (String)cc.getDataComponentFromInput(IN_URL);
		WebdavClient client = (WebdavClient)cc.getDataComponentFromInput(IN_CLIENT);
	    DelimitedFileParserFromURL df = null;

	    int lbl = -1;

	    if (getHasLabels()) {
	        lbl = getLabelsRow();
	    }

	    int typ = -1;

	    if (getHasTypes()) {
	        typ = getTypesRow();
	    }

	    if (!getHasSpecDelim()) {
	        try {
	            df = new DelimitedFileParserFromURL(client, url, lbl, typ);
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new ComponentExecutionException(e);
	        }
	    } else {
	        String s = getSpecDelim();
	        char[] del = s.toCharArray();
	        System.out.println("delimiter is: " + del[0]);

	        if (del.length == 0) {
	            throw new ComponentContextException("User specified delimiter has not been set");
	        }

	        try {
	            df = new DelimitedFileParserFromURL(client, url, lbl, typ, del[0]);
	        } catch (Exception e) {
	            throw new ComponentExecutionException(e);
	        }
	    }

	    cc.pushDataComponentToOutput(OUT_PARSER, df);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}

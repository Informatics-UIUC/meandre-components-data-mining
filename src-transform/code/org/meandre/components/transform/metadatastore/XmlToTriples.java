/**
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
 */

package org.meandre.components.transform.metadatastore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.components.transform.metadatastore.IRdfStatementProcessor;
import org.seasr.meandre.support.components.transform.metadatastore.XmlException;
import org.seasr.meandre.support.components.transform.metadatastore.XmlToRdfConverter;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This component takes as input a file path or URL specifying a TEI-encoded XML data source,
 * parses the header (identified by 'teiHeader') and body (identified by 'text') and
 * converts the XML data to RDF triples, pushing them to their respective outputs as they are produced.
 *
 * @author Boris Capitanu
 */
@Component(
        creator = "Boris Capitanu",
        description = "Converts TEI-encoded XML data to RDF triples. " +
        		"The TEI-XML format contains two sections ('teiHeader' and 'text') which " +
        		"this component processes separately to generate triples for each section.<br/>" +
        		"<u>Note:</u> The end-of-section processing is indicated by pushing out the string: 'EOF'",
        name = "XML To Triples",
        tags = "rdf, xml, triples, tei, converter",
        baseURL="meandre://seasr.org/components/data-mining/")

public class XmlToTriples implements ExecutableComponent, IRdfStatementProcessor {
    @ComponentInput(description = "Location of XML data", name = "data_url")
    final static String DATA_INPUT_URL = "data_url";

    @ComponentOutput(description = "The triples corresponding to the 'teiHeader' section",
    		name = "header_triples")
    final static String DATA_OUTPUT_HEADER_TRIPLES = "header_triples";

    @ComponentOutput(description = "The triples corresponding to the 'text' section", name = "body_triples")
    final static String DATA_OUTPUT_BODY_TRIPLES = "body_triples";

    @ComponentOutput(description = "The URL for the document currently being processed", name = "doc_url")
    final static String DATA_OUTPUT_DOC_URL = "doc_url";

    @ComponentProperty(description = "Normalize whitespaces? (true | false)",
                       name = "normalizeWhitespaces", defaultValue = "true")
    final static String DATA_PROPERTY_NORMALIZEWHITESPACES = "normalizeWhitespaces";

    @ComponentProperty(description = "Create attribute sequences? (true | false)",
                       name = "createAttrSeq", defaultValue = "true")
    final static String DATA_PROPERTY_CREATEATTRSEQ = "createAttrSeq";

    @ComponentProperty(description = "Stop flow on error? (true | false)",
                       name = "stopOnError", defaultValue = "true")
    final static String DATA_PROPERTY_STOPONERROR = "stopOnError";

    private Logger _logger;
    private ComponentContext _context;
    private URL _url;

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties context) {
        _logger = context.getLogger();
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
        _logger.entering(this.getClass().getName(), "execute");

        _context = context;

        // Create a URL for the source XML data
        _url = getURL((String) context.getDataComponentFromInput(DATA_INPUT_URL));
        if (_url == null)
            throw new ComponentContextException("Invalid input URL specified");

        try {
            _context.pushDataComponentToOutput(DATA_OUTPUT_DOC_URL, _url.toString());
        }
        catch (Exception e) {
            _logger.log(Level.SEVERE, "Execution error: ", e);
        }

        // Get the component configuration values from the supplied properties
        boolean bNormalizeWhitespaces = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_NORMALIZEWHITESPACES));
        boolean bCreateAttrSeq = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_CREATEATTRSEQ));
        boolean bStopOnError = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_STOPONERROR));

        // Perform the actual conversion
        try {
            new XmlToRdfConverter(_url, bCreateAttrSeq, bNormalizeWhitespaces, this).start();
        }
        catch (IOException e) {
            String strError = "Processing error - '" + _url.toString() + "': ";
            _logger.log(Level.SEVERE, strError, e);
            if (bStopOnError) throw new ComponentExecutionException(strError);
        }
        catch (XmlException e) {
            String strError = "Processing error - '" + _url.toString() + "': ";
            _logger.log(Level.SEVERE, strError, e);
            if (bStopOnError) throw new ComponentExecutionException(strError);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
     */
    public void dispose(ComponentContextProperties context) {
    }

    /**
     * Converts a file path or file _url to a URL
     *
     * @param location the file path or URL specifying the location
     * @return the URL describing that location
     */
    private URL getURL(String location) {
        URL url = null;

        try {
            url = new URL(location);
        }
        catch (MalformedURLException e) {
            File file = new File(location);
            try {
                URI uri = file.toURI();
                url = uri.toURL();
            }
            catch (MalformedURLException e1) {
                _logger.log(Level.SEVERE, "Cannot convert '" + location + "' to a URL: ", e1);
            }
        }

        return url;
    }

    /**
     * Called before the processing of the 'teiHeader' section
     */
    public void startHeaderProcessing() {
    	_logger.fine(_url.toString());
    }

    /**
     * Called after the processing of the 'teiHeader' section is complete
     */
    public void endHeaderProcessing() {
        try {
        	// Push out the custom EOF marker to indicate the completion of processing
            _context.pushDataComponentToOutput(DATA_OUTPUT_HEADER_TRIPLES, "EOF");
            _logger.fine(_url.toString());
        }
        catch (ComponentContextException e) {
            _logger.log(Level.SEVERE, "Processing error: ", e);
        }
    }

    /**
     * Called whenever a header triple has been generated
     *
     * @param stmt the newly-generated header triple
     */
    public void processHeaderStatement(Statement stmt) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ModelFactory.createDefaultModel().add(stmt).write(outStream, "N-TRIPLE", null);
            _context.pushDataComponentToOutput(DATA_OUTPUT_HEADER_TRIPLES, outStream.toString());
        }
        catch (ComponentContextException e) {
            _logger.log(Level.SEVERE, "Processing error: ", e);
        }
    }

    /**
     * Called before the body ('text') section is processed
     */
    public void startBodyProcessing() {
    	_logger.fine(_url.toString());
    }

    /**
     * Called after the body ('text') section has been processed
     */
    public void endBodyProcessing() {
        try {
        	// Push out the custom EOF marker to indicate the completion of processing
            _context.pushDataComponentToOutput(DATA_OUTPUT_BODY_TRIPLES, "EOF");
            _logger.fine(_url.toString());
        }
        catch (ComponentContextException e) {
            _logger.log(Level.SEVERE, "Processing error: ", e);
        }
    }

    /**
     * Called whenever a body triple has been generated
     *
     * @param stmt the newly-generated body triple
     */
    public void processBodyStatement(Statement stmt) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ModelFactory.createDefaultModel().add(stmt).write(outStream, "N-TRIPLE", null);
            _context.pushDataComponentToOutput(DATA_OUTPUT_BODY_TRIPLES, outStream.toString());
        }
        catch (ComponentContextException e) {
            _logger.log(Level.SEVERE, "Processing error: ", e);
        }
    }
}

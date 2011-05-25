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

package org.seasr.meandre.components.transform.metadatastore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.generic.io.webdav.WebdavClient;
import org.seasr.meandre.support.generic.io.webdav.WebdavClientFactory;

/**
 * This component reads RDF triples from the input and writes them to a file.
 * This has been created as a complement to the XmlToTriples component and uses
 * that component's outputs to feed this component's inputs.
 *
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Saves RDF triples to file",
        name = "Triples To File",
        tags = "rdf, triples",
        firingPolicy = Component.FiringPolicy.any,
        baseURL="meandre://seasr.org/components/data-mining/")

public class TriplesToFile implements ExecutableComponent {
    @ComponentInput(description = "Source base URL", name = "src_base_url")
    final static String DATA_INPUT_SRC_BASE_URL = "src_base_url";

    @ComponentInput(description = "Destination base URL", name = "dest_base_url")
    final static String DATA_INPUT_DEST_BASE_URL = "dest_base_url";

    @ComponentInput(description = "Document URL", name = "doc_url")
    final static String DATA_INPUT_DOC_URL = "doc_url";

    @ComponentInput(description = "Triples", name = "triples")
    final static String DATA_INPUT_TRIPLES = "triples";

    @ComponentProperty(description = "Processing method (header | body)",
                       name = "processing_method", defaultValue = "body")
    final static String DATA_PROPERTY_PROCESSING_METHOD = "processing_method";

    private Logger _logger;

    private File _tempFile;
    private URL _urlBaseSrc;
    private URL _urlBaseDest;
    private URL _urlDoc;
    private Queue<String> _queueTriples;
    private boolean _bDone;
    private boolean _bIsProcessingHeader;

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties contextProperties) {
    	_logger = contextProperties.getLogger();

        _urlBaseSrc = _urlBaseDest = _urlDoc = null;
        _queueTriples = new LinkedList<String>();
        _bDone = false;

        String strProcessingMethod = contextProperties.getProperty(DATA_PROPERTY_PROCESSING_METHOD);
        _bIsProcessingHeader = strProcessingMethod.toLowerCase().equals("header");
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext context) throws ComponentExecutionException, ComponentContextException {
        _logger.entering(this.getClass().getName(), "execute");

        // Get the source base URL, if available
        if (context.isInputAvailable(DATA_INPUT_SRC_BASE_URL) && _urlBaseSrc == null)
            _urlBaseSrc = getURL((String) context.getDataComponentFromInput(DATA_INPUT_SRC_BASE_URL));

        // Get the destination base URL, if available
        if (context.isInputAvailable(DATA_INPUT_DEST_BASE_URL) && _urlBaseDest == null)
            _urlBaseDest = getURL(context.getDataComponentFromInput(DATA_INPUT_DEST_BASE_URL) + "/");

        // Get the input document URL, if available
        if (context.isInputAvailable(DATA_INPUT_DOC_URL) && _urlDoc == null)
            _urlDoc = getURL((String) context.getDataComponentFromInput(DATA_INPUT_DOC_URL));

        // Queue any new triples that arrived on the input port
        if (context.isInputAvailable(DATA_INPUT_TRIPLES))
            if (!_queueTriples.offer((String) context.getDataComponentFromInput(DATA_INPUT_TRIPLES)))
                throw new ComponentExecutionException("Cannot add triple to internal queue");

        if (_urlDoc != null && _tempFile == null) {
            // Create a temporary file to store the triples
            String strTempFileName = _urlDoc.getPath().substring(_urlDoc.getPath().lastIndexOf("/") + 1);
            try {
                _tempFile = File.createTempFile(strTempFileName + "_", null);
            }
            catch (IOException e) {
                String strError = "Cannot create temporary file";
                _logger.log(Level.SEVERE, strError, e);
                throw new ComponentExecutionException(strError);
            }
        }

        if (_tempFile != null) {
            while (!_queueTriples.isEmpty() && !_bDone) {
                /*
                 * If there are triples in the queue and we aren't yet done with the current document
                 * then get the triples from the queue and append them to the temporary file
                 */
                String strTriple = _queueTriples.remove();
                if (strTriple.trim().equals("EOF")) {
                    _logger.info("Finished processing " + _urlDoc.toString());
                    _bDone = true;
                }
                else {
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(_tempFile, true));
                        writer.write(strTriple);
                        writer.close();
                    }
                    catch (IOException e) {
                        String strError = "Cannot write to file";
                        _logger.log(Level.SEVERE, strError, e);
                        throw new ComponentExecutionException(strError);
                    }
                }
            }
        }

        if (_urlBaseSrc != null && _urlBaseDest != null && _urlDoc != null && _bDone) {
            /*
             * If we know the base URL, the destination URL, and document URL, and we're done
             * with the current file, then we can upload the teporary file to WebDAV
             */

            // Construct the output document name
            String strDocSaveName = _urlDoc.getPath().substring(_urlDoc.getPath().lastIndexOf("/") + 1);
            strDocSaveName += (_bIsProcessingHeader ? "_header" : "_body") + ".n3";

            try {
                // Construct the output URI for the document
                URI uriDocRelPath = _urlBaseSrc.toURI().relativize(_urlDoc.toURI()).resolve(strDocSaveName);
                URI uriOut = _urlBaseDest.toURI().resolve(uriDocRelPath);
                _logger.info("Destination store is '" + uriOut.toString() + "'");

                // Write the file to webdav now
                String sDirectory = uriOut.toString();
                sDirectory = sDirectory.substring(0, sDirectory.lastIndexOf("/"));

                try {
                    WebdavClient webdav = WebdavClientFactory.begin(new HttpHost(uriOut.getHost(), uriOut.getPort()));
                    // Create the necessary webdav folders
                    if (webdav.mkdirs(sDirectory)) {
                        // ... and upload the file
                        webdav.put(uriOut.toString(), _tempFile, "text/plain");
                    }
                    else
                        _logger.severe("Could not create all subpaths for " + sDirectory);

                    _logger.info("Finished uploading " + uriOut.toString());
                }
                catch (IOException e) {
                    String strError = "Error uploading '" + uriOut.toString() + "' to WebDAV: ";
                    _logger.log(Level.SEVERE, strError, e);
                    throw new ComponentExecutionException(strError);
                }
                finally {
                    _tempFile.delete();
                }

                // Reset state for next document
                _bDone = false;
                _urlDoc = null;
                _tempFile = null;

            }
            catch (URISyntaxException e) {
            	_logger.log(Level.SEVERE, "Execution error: ", e);
                throw new ComponentExecutionException(e.getMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
     */
    public void dispose(ComponentContextProperties ccp) {
    }

    /**
     * Converts a file path or file url to a URL
     *
     * @param location the file path or URL specifying the location
     * @return the URL describing that location
     * @throws ComponentContextException Thrown if the location cannot be converted to a URL
     */
    private URL getURL(String location) throws ComponentContextException {
        URL url;
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
                String strError = "Cannot convert '" + location + "' to a URL: ";
                _logger.log(Level.SEVERE, strError, e1);
                throw new ComponentContextException(strError);
            }
        }

        return url;
    }
}

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

package org.meandre.components.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.tools.webdav.IResourceInfo;
import org.meandre.tools.webdav.WebdavClient;

/**
 * Outputs the list of files in a WebDAV tree matching the specified criteria
 *
 * @author Boris Capitanu
 * @jira COMPONENTS-1 XmlToTriples
 */

@Component(
        creator = "Boris Capitanu",
        description = "Outputs the list of files in a WebDAV tree matching the specified criteria.",
        name = "Webdav Browser",
        tags = "io, webdav, input",
        baseURL="meandre://seasr.org/components/data-mining/")
        
public class WebdavBrowser implements ExecutableComponent {

    @ComponentProperty(description = "The URL where to start looking for files",
    				   name = "start_url",
                       defaultValue = "null")
    final static String DATA_PROPERTY_START_URL = "start_url";

	@ComponentProperty(description = "Webdav username (use 'null' to indicate that " +
			"no authentication needs to be performed", name = "username", defaultValue = "null")
	final static String DATA_PROPERTY_USERNAME = "username";

	@ComponentProperty(description = "Webdav password", name = "password", defaultValue = "null")
	final static String DATA_PROPERTY_PASSWORD = "password";

    @ComponentProperty(description = "The regular expression used to filter files",
    		name = "regex_filter", defaultValue = "*")
    final static String DATA_PROPERTY_REGEX_FILTER = "regex_filter";

    @ComponentProperty(description = "Recurse subdirectories? (true/false)",
    		name = "recursive", defaultValue = "false")
    final static String DATA_PROPERTY_RECURSIVE = "recursive";

    @ComponentOutput(description = "The base URL that was used when searching was performed", name = "start_url")
    final static String DATA_OUTPUT_START_URL = "start_url";

    @ComponentOutput(description = "The URL of the file", name = "file_url")
    final static String DATA_OUTPUT_FILE_URL = "file_url";


    private static Logger _logger;

    private String _sStartUrl;
    private String _sRegexFilter;
    private String _sUsername;
    private String _sPassword;
    private boolean _bRecurseSubdirs;
    private WebdavClient _webdav;

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties context) {
    	_logger = context.getLogger();
    	_logger.entering(this.getClass().getName(), "initialize");

    	try {
    		_sStartUrl = context.getProperty(DATA_PROPERTY_START_URL);
    		_sRegexFilter = context.getProperty(DATA_PROPERTY_REGEX_FILTER);
    		_sUsername = context.getProperty(DATA_PROPERTY_USERNAME);
    		_sPassword = context.getProperty(DATA_PROPERTY_PASSWORD);
    		_bRecurseSubdirs = Boolean.parseBoolean(context.getProperty(DATA_PROPERTY_RECURSIVE));

    		boolean useAuthentication = _sUsername.trim().equalsIgnoreCase("null");
    		_logger.fine("startUrl=" + _sStartUrl);
    		_logger.fine("regexFilter=" + _sRegexFilter);
    		_logger.fine("recurseSubdirectories=" + _bRecurseSubdirs);
    		_logger.fine("Using authentication: " + useAuthentication);

        	if (useAuthentication)
        		_webdav = new WebdavClient(_sStartUrl,
        				new UsernamePasswordCredentials(_sUsername, _sPassword));
        	else
        		_webdav = new WebdavClient(_sStartUrl);
        }
        catch (Exception e) {
    		_logger.log(Level.SEVERE, "Initialize error", e);
    		throw new RuntimeException(e);
    	}

        _logger.exiting(this.getClass().getName(), "initialize");
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
    public void execute(ComponentContext context)
            throws ComponentExecutionException, ComponentContextException {
        _logger.entering("WebdavBrowser", "execute");

        try {
            // Get the list of files matching the specified criteria
            IResourceInfo[] files = _webdav.listFiles(_sStartUrl, _bRecurseSubdirs, new FilenameFilter() {
                public boolean accept(File dir, String name) {
                	_logger.finest("Matching: dir=" + dir.toString() + " name=" + name);
                    return Pattern.matches(_sRegexFilter, name);
                }
            });

            if (files != null) {
                // Push out the start URL
                context.pushDataComponentToOutput(DATA_OUTPUT_START_URL, _sStartUrl);

                // Push out all the file URLs found matching the given criteria
                for (IResourceInfo fileInfo : files) {
                    _logger.info("Found file " + fileInfo.getURL().toString());
                    context.pushDataComponentToOutput(DATA_OUTPUT_FILE_URL, fileInfo.getURL().toString());
                }
            }
            else
                _logger.warning(_sStartUrl + " does not exist or is not a directory!");

        }
        catch (IOException e) {
            _logger.log(Level.SEVERE, "Execution exception: ", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
     */
    public void dispose(ComponentContextProperties context) {
    }
}

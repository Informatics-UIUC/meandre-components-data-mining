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

package org.meandre.components.io.file.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import org.meandre.tools.webdav.WebdavClient;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.Credentials;

import java.io.*;

/**
 * InputFileURL allows the user to input the url to a local or remote resource.
 * The url can be set in the properties editor.
 *
 * <p>This module creates a WevdavClient for the specified file or URL. The
 * <i>WevdavClient</i> is pushed to the output.</p>
 *
 * @author  unascribed (original)
 * @author Boris Capitanu
 * @author Lily Dong
 *
 * BC: Imported from d2k (ncsa.d2k.modules.core.io.file.input.Input1FileURL)
 *
 * TODO: testing
 * @author D. Searsmith (conversion to SEASR 6/08)
 */

@Component(
        creator = "Boris Capitanu and Lily Dong",
        description = "This module is used to enter the url to a local or remote resource. " +
        "Detailed Description: " +
        "Collect a URL or local path, and create a WebdavClient to access it. " +
        "The module provides a properties editor that can be used to " +
        "enter a url to a local or remote resource.  If the url points " +
        "to a local file, the user can enter the name directly into " +
        "a text area." +
        "If the url points to a remote file, the user " +
        "has to type in the host url, which include protocol, path and port " +
        "in the text area for host url, and the relative path of " +
        "the resource to the server in the text area for file name." +
        "This module does not perform any checks to verify that " +
        "the url exists and is accessible with the username and password " +
        "given by the user. A check is performed to " +
        "make sure that a file name has been entered and an exception is " +
        "thrown if the editor text area is blank. " +
        "The WebdavClient is made available on the WebdavClient output " +
        "port.  For local url, a path may or may not be included " +
        "in the file name string.",
        name = "Input URL Or Path",
        tags = "io, input",
        dependency = {"jackrabbit-webdav-1.4.jar", "slf4j-api-1.5.2.jar", "slf4j-jcl-1.5.2.jar", "meandre-webdav-1.4.0.jar" },
        baseURL="meandre://seasr.org/components/")

public class InputFileUrl implements ExecutableComponent {

    @ComponentOutput(description = "WebdavClient pointing to a resource.",
                     name = "webdavClient")
    final static String DATA_OUTPUT_CLIENT = "webdavClient";

    @ComponentOutput(description = "URL pointing to a resource location.",
                     name = "url")
    final static String DATA_OUTPUT_URL = "url";

    @ComponentProperty(description = "The input file URL",
                       name = "file_url",
                       defaultValue = " ")
    final static String DATA_PROPERTY_FILE_URL = "file_url";

    @ComponentProperty(description = "The user login name to access the object. " +
                                     "if needed, use null to indicate no authentication " +
                                     "is to be performed.",
                       name = "username",
                       defaultValue = "null")
    final static String DATA_PROPERTY_USERNAME = "username";

    @ComponentProperty(description = "The password to access the object, if needed",
                       name = "password",
                       defaultValue = "null")
    final static String DATA_PROPERTY_PASSWORD = "password";

    //~ Instance fields *********************************************************
    private WebdavClient client;

    private URL fileUrl;

    /** The password property. */
    private String password;

    /** The username property. */
    private String username;

    private Logger _logger;

    //~ Methods *****************************************************************

    /**
     * Get the password.
     *
     * @return password.
     */
    protected String getPassword() { return password; }

    /**
     * Get UserName.
     *
     * @return The user name.
     */
    public String getUserName() { return username; }

    /**
     * Set Password.
     *
     * @param s the new value.
     */
    public void setPassword(String s) { password = s; }

    /**
     * Set UserName.
     *
     * @param s new value.
     */
    public void setUserName(String s) { username = s; }

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
     */
    public void initialize(ComponentContextProperties context) {
	    _logger = context.getLogger();

	    username = context.getProperty(DATA_PROPERTY_USERNAME);
	    password = context.getProperty(DATA_PROPERTY_PASSWORD);

	    if (username.equalsIgnoreCase("null")) username = "";
	    if (password.equalsIgnoreCase("null")) password = "";

	    setUserName(username);
	    setPassword(password);

	    try {
	        fileUrl = new URL(context.getProperty(DATA_PROPERTY_FILE_URL));
	    } catch (MalformedURLException e) {
	    	_logger.log(Level.SEVERE, "Initialize error: ", e);
	    	throw new RuntimeException(e);
	    }

	    client = null;
	}

    /*
     * (non-Javadoc)
     * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
     */
	public void execute(ComponentContext context)
	    throws ComponentExecutionException, ComponentContextException {

	    /*DataObjectProxy dataobj;
	    try {
	        dataobj = DataObjectProxyFactory.getDataObjectProxy(fileUrl, username, password);
	    } catch (DataObjectProxyException e) {
	    	_logger.log(Level.SEVERE, "Execution exception: ", e);
	        throw new ComponentExecutionException(e);
	    }
	    context.pushDataComponentToOutput(DATA_OUTPUT_DATAOBJECTPROXY, dataobj);*/

	    Credentials credentials = null;
        if(username.length() != 0 && password.length() != 0)
            credentials = new UsernamePasswordCredentials(username, password);

        try {
            client = new WebdavClient(context.getProperty(DATA_PROPERTY_FILE_URL),
                                      credentials);
        }catch (MalformedURLException e) {
            e.printStackTrace();
            _logger.log(Level.SEVERE, "Execution exception: ", e);
            throw new ComponentExecutionException(e);
        }

        context.pushDataComponentToOutput(
                DATA_OUTPUT_URL, context.getProperty(DATA_PROPERTY_FILE_URL));
        context.pushDataComponentToOutput(DATA_OUTPUT_CLIENT, client);

        /*File file = new File("/birdy_project/iris.arff");
        try {
            client.put("http://norma.ncsa.uiuc.edu/alg-dav/iris.arff", file, "text/plain");
        }catch(IOException e) {
            e.printStackTrace();
        }*/
	}

	/*
	 * (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties context) {
    }

} // end class InputFileURL

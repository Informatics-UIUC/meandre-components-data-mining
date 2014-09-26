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

import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.webdav.WebdavClient;
import org.seasr.meandre.support.generic.io.webdav.WebdavClientFactory;

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
        creator = "Boris Capitanu",
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
        baseURL="meandre://seasr.org/components/data-mining/")

public class InputFileUrl extends AbstractExecutableComponent {

    @ComponentOutput(description = "WebdavClient pointing to a resource.",
                     name = "webdavClient")
    final static String OUT_CLIENT = "webdavClient";

    @ComponentOutput(description = "URL pointing to a resource location.",
                     name = "url")
    final static String OUT_URL = "url";

    @ComponentProperty(description = "The input file URL",
                       name = "file_url",
                       defaultValue = " ")
    final static String PROP_FILE_URL = "file_url";

    @ComponentProperty(description = "The user login name to access the object. " +
                                     "if needed, use null to indicate no authentication " +
                                     "is to be performed.",
                       name = "username",
                       defaultValue = "null")
    final static String PROP_USERNAME = "username";

    @ComponentProperty(description = "The password to access the object, if needed",
                       name = "password",
                       defaultValue = "null")
    final static String PROP_PASSWORD = "password";

    //~ Instance fields *********************************************************
    private WebdavClient client;

    private URL fileUrl;

    /** The password property. */
    private String password;

    /** The username property. */
    private String username;

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


    @Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    username = getPropertyOrDieTrying(PROP_USERNAME, true, false, ccp);
	    password = getPropertyOrDieTrying(PROP_PASSWORD, false, false, ccp);

	    setUserName(username);
	    setPassword(password);

	    String sFileUrl = getPropertyOrDieTrying(PROP_FILE_URL, ccp);
	    fileUrl = new URL(sFileUrl);

	    client = null;
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
	    Credentials credentials = null;
        if(username.length() != 0 && password.length() != 0)
            credentials = new UsernamePasswordCredentials(username, password);

		HttpHost host = new HttpHost(fileUrl.getHost(), fileUrl.getPort(), fileUrl.getProtocol());
		client = WebdavClientFactory.begin(host, credentials);

        cc.pushDataComponentToOutput(OUT_URL, fileUrl.toString());
        cc.pushDataComponentToOutput(OUT_CLIENT, client);
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

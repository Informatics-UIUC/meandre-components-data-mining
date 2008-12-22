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

//==============
// Java Imports
//==============

import java.util.*;
import java.util.logging.Logger;

//===============
// Other Imports
//===============

import org.meandre.core.*;
import org.meandre.annotations.*;

import org.meandre.tools.webdav.WebdavClient;
import org.meandre.tools.webdav.IResourceInfo;

/**
 * TODO: testing
 * @author D. Searsmith and Lily Dong(conversion to SEASR 6/08)
 */
@Component(creator = "Duane Searsmith and Lily Dong",

		description = "<p>Overview:<br>"
		+ "This module reads URLs residing underneath the URL that is pointed to "
		+ "by the input WebDAV client.</p>"
		, name = "Get URLs", tags = "io, url")
public class GetURLs implements ExecutableComponent {
	// ==============
	// Data Members
	// ==============
	// Options
	private int _docsProcessed = 0;
	private int _urlsPushedCount = 0;
	private long _start = 0;
	private Set<String> _extenssions = null;
	private static Logger _logger = Logger.getLogger("GetURLs");

	// props

	@ComponentProperty(description = "Verbose output?", name = "verbose", defaultValue = "false")
	public final static String DATA_PROPERTY_VERBOSE = "verbose";

	@ComponentProperty(description = "Comma delimeted list of file extensions.  Empty list means all.", name = "file_ext", defaultValue = "")
	public final static String DATA_PROPERTY_FILE_EXTS = "file_ext";

	@ComponentProperty(description = "Recurse subdirectories?", name = "recurse_subs", defaultValue = "false")
	public final static String DATA_PROPERTY_RECURSE_SUBS = "recurse_subs";

	@ComponentProperty(description = "Search depth.", name = "search_depth", defaultValue = "0")
	public final static String DATA_PROPERTY_SEARCH_DEPTH = "search_depth";

	// io
	@ComponentInput(description = "WevdavClient pointing to a resource",
                    name = "webdavClient")
    final static String DATA_INPUT_CLIENT= "webdavClient";

	@ComponentInput(description = "URL pointing to a resource location.",
                    name = "url")
    final static String DATA_INPUT_URL = "url";

	@ComponentOutput(description = "URL", name = "url")
	public final static String DATA_OUTPUT_URL = "url";

	@ComponentOutput(description = "Count of URLs output.", name = "url_cnt")
	public final static String DATA_OUTPUT_URL_COUNT = "url_cnt";

	// ================
	// Constructor(s)
	// ================
	public GetURLs() {
	}

	// ================
	// Public Methods
	// ================

	public boolean getVerbose(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_VERBOSE);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public String getFileExts(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_VERBOSE);
		return s;
	}

	public boolean getRecurseSubs(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_RECURSE_SUBS);
		return Boolean.parseBoolean(s.toLowerCase());
	}

	public int getSearchDepth(ComponentContextProperties ccp) {
		String s = ccp.getProperty(DATA_PROPERTY_SEARCH_DEPTH);
		int d = Integer.parseInt(s.toLowerCase());
		if (d != 0 && d != 1) {
			d = 2147483647; //DataObjectProxy.DEPTH_INFINITY;
		}
		return d;
	}

	/**
	 * Return information about the module.
	 *
	 * @return A detailed description of the module.
	 */
	public String getModuleInfo() {
		String s = "<p>Overview: ";
		s += "This module reads URLs residing underneith the URL that is pointed to by the input WebDAV client.";
		s += "</p>";
		return s;
	}

	public void initialize(ComponentContextProperties ccp) {
		_logger.fine("initialize() called");
		_docsProcessed = 0;
		_urlsPushedCount = 0;
		_start = System.currentTimeMillis();

		String[] temp = getFileExts(ccp).split(",", 0);
		_extenssions = new HashSet<String>();
		for (int i = 0; i < temp.length; i++) {
			String strTmp = temp[i].trim();
			if (strTmp.length() > 0)
				_extenssions.add(strTmp);
		}

	}

	public void dispose(ComponentContextProperties ccp) {
		_logger.fine("dispose() called");
		long end = System.currentTimeMillis();
		if (getVerbose(ccp)) {
			System.out.println("GetURLs: END EXEC -- URLs Processed: "
					+ _docsProcessed + " and URLs pushed out: "
					+ this._urlsPushedCount + " in " + (end - _start) / 1000
					+ " seconds\n");
		}
		_docsProcessed = 0;
		_urlsPushedCount = 0;
	}


	@SuppressWarnings("unchecked")
	public void execute(ComponentContext ctx)
			throws ComponentExecutionException, ComponentContextException {
		_logger.fine("execute() called");

		Vector urlsVector = new Vector(); //null;

		try {
			_urlsPushedCount = 0;

			String url = (String)ctx.getDataComponentFromInput(DATA_INPUT_URL);
		    WebdavClient client = (WebdavClient)ctx.getDataComponentFromInput(DATA_INPUT_CLIENT);

		    if(!url.endsWith("/")) //make sure url ending with /
		        url += "/";
		    String str1 = url.substring(0, url.length()-1); //str1 equals the url without the last /
		    str1 = str1.substring(str1.lastIndexOf("/")+1);
		    IResourceInfo[] info = client.listContents(url);
			for(int i=0; i<info.length; i++) {
			    String path = info[i].getPath();
			    if(!path.startsWith("http://")) {//ensure that the path is relative
			        if(path.startsWith("/")) //make sure the path beginning without /
			            path = path.substring(1);
			        int index = path.indexOf(str1); //look for the first /
			        String str2 = null;
			        if(index != -1)
			            str2 = path.substring(index + str1.length());
			        if(str2 != null && str2.startsWith("/"))
			            str2 = str2.substring(1);
			        if(str2 != null) {//overlap exists
			            path = url + str2;
			        } else { //no overlap
			            path = url + path;
			        }
			    }
			    urlsVector.add(path);
			}

			/*DataObjectProxy dop = (DataObjectProxy) ctx
					.getDataComponentFromInput(DATA_INPUT_DATA_PROXY);
			urlsVector = dop.getChildrenURLs(getSearchDepth(ctx), true);*/

			if (getVerbose(ctx)) {
				System.out.println("GetURLs: the returned urls --\n"
						+ urlsVector.toString());
			}

			while (urlsVector != null && urlsVector.size() > 0) {
				// popping a url ...
				Object obj = urlsVector.remove(urlsVector.size() - 1);
				String tmp = null; // this will be the url string to be
				// observed
				// if we have extenssions to filter on...
				if (_extenssions.size() > 0) {
					// get the string and get its extenssion
					String urlStr = obj.toString();
					int fsIndex = urlStr.lastIndexOf("/");
					if (fsIndex == -1)
						fsIndex = 0;

					// String tmpUrlStr = urlStr.substring(fsIndex);
					int index = urlStr.substring(fsIndex).lastIndexOf(".");
					if (index != -1) {
						tmp = urlStr.substring(index + fsIndex);
						if (tmp.length() > 4) {
							_logger.info("found suffix " + tmp
									+ " probably isn't the suffix... ");
						}
					}
				}// if there are extenssions
				_docsProcessed++;
				// if the suffix in valid or if we have no xtenssions to
				// validate
				// upon
				if (_extenssions.size() == 0
						|| (tmp != null && _extenssions.contains(tmp))) {
					_urlsPushedCount++;
					// push out this url
					ctx.pushDataComponentToOutput(DATA_OUTPUT_URL, obj);
					if (getVerbose(ctx)) {
						_logger.info("GetURLs: url = " + obj.toString());
					}
				} else {
					if (getVerbose(ctx)) {
						_logger.info("GetURLs: url = " + obj.toString()
								+ " is filtered out.");
					}
				}

			}// if there are urls in the vector
			urlsVector = null;
			ctx.pushDataComponentToOutput(DATA_OUTPUT_URL_COUNT, _urlsPushedCount);
		} catch (Exception ex) {
			ex.printStackTrace();
			_logger.severe(ex.getMessage());
			_logger.severe("ERROR: TextFileToDoc.execute()");
			throw new ComponentExecutionException(ex);
		}

	}

}

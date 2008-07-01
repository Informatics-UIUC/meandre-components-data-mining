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

package org.meandre.components.util;

//==============
// Java Imports
//==============

import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.*;

//===============
// Other Imports
//===============

import org.meandre.core.ComponentContext;

/**
 * This class will provide functions to retrieve file resources from jars and
 * install them as readable files in the public resources directory of the
 * meandre server.
 * 
 * @author D. Searsmith
 * 
 */

public class MeandreJarFileReaderUtil {
	
	// ==============
	// Data Members
	// ==============
	
	private static Logger _logger = Logger.getLogger("MeandreJarFileReaderUtil");

	
	/**
	 * If resource cannot be located a null is returned, otherwise the
	 * <code>File</code> object is returned.
	 * 
	 * NOTE: Both resource and filename parameters are expected to use forward
	 * slashes as path delimiters.
	 * 
	 * @param resource
	 *            String denoting the resource location and name.
	 * @param filename
	 *            String denoting the file location and name.
	 * @return File object or null if resource is not found.
	 */
	static public File findAndInstallFileResource(String resource, String filename, ComponentContext ctx)
		throws IOException {
		
		if ((resource == null) || (filename == null)){
			_logger.severe("findAndInstallFileResource params may not be null");
			return null;
		}
		if (resource.indexOf("\\") > -1){
			_logger.severe("findAndInstallFileResource resource param may include only forward slashes.");
			return null;
		}
		if (filename.indexOf("\\") > -1){
			_logger.severe("findAndInstallFileResource filename param may include only forward slashes.");
			return null;
		}
		while (resource.startsWith("/")){
			resource = resource.substring(1);
		}
		if (!filename.startsWith("/")){
			filename = "/" + filename;
		}

		String tmpdir = ctx.getExecutionInstanceID();
		File pub = new File(tmpdir);
		String pubdir = pub.getCanonicalPath();
		File resFile = new File(pubdir+filename);
		if (!resFile.exists()) {
			BufferedOutputStream bos = null;
			BufferedInputStream bis = null;
			
			String tmp = filename.substring(1);
			
			StringTokenizer toker = new StringTokenizer(tmp, "/");
			
			int cnt = toker.countTokens();
			//need to account for extra loop for creating pubdir proper.
			cnt++;
			int i = 0;
			StringBuffer work = new StringBuffer(pubdir);
			File f = null;
			try {
		
			while (toker.hasMoreTokens()){
				i++;
				String str = null;
				if (i > 1){
					str = "/" + toker.nextToken();
				} else {
					str = "";
				}
				if (i == cnt){
					resFile.createNewFile();
					bis = new BufferedInputStream(MeandreJarFileReaderUtil.class.getClassLoader()
							.getResourceAsStream(
									resource));
					if (bis == null){
						_logger.severe("findAndInstallFileResource :: Unable to open resource: " + resource);
						return null;												
					}
					bos = new BufferedOutputStream(new FileOutputStream(resFile));
					if (bos == null){
						_logger.severe("findAndInstallFileResource :: Unable to open file: " + resFile.getName());
						return null;																		
					}
					byte[] barr = new byte[1024];
					int len = 0;
					while ((len = bis.read(barr)) != -1) {
						bos.write(barr, 0, len);
					}					
				} else {
					work.append(str);
					f = new File(work.toString());
					if (!f.exists()){
						if (!f.mkdir()){
							_logger.severe("findAndInstallFileResource :: Unable to create directory: " + f.getPath() + "  " + f.getName());
							return null;
						}
					}
				}
			}
			
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} finally {
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
					if (bis != null) {
						bis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
		}	
		return resFile;
	}
}

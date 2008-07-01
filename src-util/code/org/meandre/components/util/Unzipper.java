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

// ==============
// Java Imports
// ==============

import java.io.*;
import java.util.zip.*;
import java.util.logging.*;

// ===============
// Other Imports
// ===============

/**
 * A utility class for unzipping archive files.
 * 
 * @author D. Searsmith 
 * 
 * TODO: Testing, Unit Tests
 * 
 */
public class Unzipper {

	// ==============
	// Data Members
	// ==============

	private static Logger _logger = Logger.getLogger("Unzipper");

	// ================
	// Static Methods
	// ================

	/**
	 * Given an archive file, unzip its contents to the path denoted by the
	 * destination parameter.
	 */
	public static void unzip(File zipFile, String dest) {
		if (zipFile == null) {
			_logger
					.severe("Cannot pass a null file parameter to unzip! Do nothing and return.");
			return;
		}
		if (!zipFile.exists()) {
			_logger
					.severe("The file passed does not exists! Do nothing and return.");
			return;
		}
		/*
		 * If no destination path is specified then use the parent directory of
		 * the zip file as the destination for the file contents.
		 */
		if (dest == null) {
			dest = zipFile.getParent();
		}
		if ((!dest.endsWith("/"))) {
			dest += "/";
		} else {
			dest = "Using paths in zip file for each entry.";
		}
		_logger.info("File to unzip: " + zipFile);
		_logger.info("Zip contents destination: " + dest);
		try {
			byte[] buf = new byte[4096];
			ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry == null) {
					break;
				}
				if (entry.isDirectory()) {
					File mkdir = new File(dest + entry.getName());
					mkdir.mkdir();
					continue;
				}
				_logger.info(entry.getName() + " (" + entry.getCompressedSize()
						+ "/" + entry.getSize() + ")");
				FileOutputStream out = new FileOutputStream(dest
						+ entry.getName());
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				in.closeEntry();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			_logger.severe(e.toString());
		}
	}
}

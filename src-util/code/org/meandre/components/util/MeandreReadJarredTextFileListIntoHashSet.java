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

package  org.meandre.components.util;

//==============
// Java Imports
//==============
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
//===============
// Other Imports
//===============

import org.meandre.components.util.MeandreJarFileReaderUtil;
import org.meandre.core.*;

/**
 * <p>Overview: <br>
 * This class takes a resource name and a target filename and a component context
 * object.  It uses the MeandreJarFileReaderUtil to unjar the resource into a file 
 * at the target file location.  It then reads the text list putting each string
 * into a <i>HashSet</i> which is returned.
 * 
 * </p>
 * 
 * TODO: Testing, Unit Tests
 * @author D. Searsmith 
 */
public class MeandreReadJarredTextFileListIntoHashSet {

	//==============
    // Data Members
    //==============
    
    private static Logger _logger = Logger.getLogger("MeandreReadJarredTextFileListIntoHashSet");
    
    //================
    // Constructor(s)
    //================
    public MeandreReadJarredTextFileListIntoHashSet () {
    }

    //================
    // Public Methods
    //================
    
    static public Set<String> getSet(String resourceName, String targetFilename, ComponentContext ctx)
    throws Exception {
		try {
			return getSet(resourceName, targetFilename, 0, ctx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
    }

    static public Set<String> getSet(String resourceName, String targetFilename, int expectedSetSize, ComponentContext ctx)
    throws Exception {
		try {
			File f = MeandreJarFileReaderUtil.findAndInstallFileResource(
					resourceName, targetFilename, ctx);
			return readList(f, expectedSetSize);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
    }
    
    //=================
    // Private Methods
    //=================
    
    static private Set<String> readList(File f, int expectedSetSize) throws Exception {
        try {
        	HashSet<String> ret = new HashSet<String>();
            if (expectedSetSize > 0){
            	ret = new HashSet<String>(expectedSetSize);
            } else {
            	ret = new HashSet<String>();
            }
        	
        	String line = null;
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    ret.add(line.trim());
                }
            }
            return ret;
        } catch (Exception e) {
            _logger.severe("ERROR in list reading process: " + e);
            throw e;
        }
    }
}




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

package org.seasr.meandre.applet;

/**
* <p>Title: Prediction Visualization</p>
*
* <p>Description: A visualization applet for prediction table report</p>
*
* <p>Copyright: Copyright (c) 2008</p>
*
* <p>Company: Automated Learning Group, NCSA</p>
*
* @author Lily Dong
*/

import org.seasr.datatypes.datamining.table.PredictionTable;

import javax.swing.JApplet;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.ObjectInputStream;


public class PredApplet extends JApplet {
    PredictionTable pt;

    /**
     * Executed each time the applet is loaded or reloaded.
     */
    public void init() {
        String location = getDocumentBase().toString() +
                          getParameter("servletURL") + "?applet=true";

       try {
           URL testServlet = new URL(location);
           URLConnection servletConnection = testServlet.openConnection();
           servletConnection.setDoInput(true);
           InputStream inputStreamFromServlet = servletConnection.getInputStream();
           ObjectInputStream ois = new ObjectInputStream(inputStreamFromServlet);
           pt = (PredictionTable)ois.readObject();
       } catch (Exception ex) {
           ex.printStackTrace();
       }

       PredView pv = new PredView();
       try {
           pv.setInput((Object)pt, 0);
       } catch (Exception ex) {
           ex.printStackTrace();
       }

       getContentPane().add(pv);
    }

    /**
     * Executed when the applet is loaded or revisited.
     */
    public void start() {}

    /**
     * Executed when the user leaves the applet's page.
     */
    public void stop() {}

    /**
     * Clean up the applet.
     */
    public void destroy() {}
}

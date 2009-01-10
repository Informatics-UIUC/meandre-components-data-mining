/**
*
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
*
*/
package org.meandre.components.io.webservice;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.rpc.ParameterMode;

import org.meandre.annotations.Component;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
            description="Consume web service written in Java.",
            name="AxisClient",
            tags="web service",
            baseURL="meandre://seasr.org/components/")

public class AxisClient implements ExecutableComponent{
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
   public void execute(ComponentContext cc) throws ComponentExecutionException,
           ComponentContextException {
       /*try {
       String endpoint = "http://www.claudehussenet.com:80/ws/services/Anagram"; //web service is written in Glue.
       Service  service = new Service();
       Call     call    = (Call) service.createCall();
       call.setTargetEndpointAddress( new java.net.URL(endpoint) );
       call.setOperationName("getAnagram");
       call.addParameter("word", XMLType.XSD_STRING, ParameterMode.IN);
       call.setReturnType(XMLType.XSD_ANY);    
       String[] ret = (String[])call.invoke(new Object[] {"Elvis"});
       for(int i=0; i<ret.length; i++)
           System.out.println(ret[i]);
       }catch(Exception e) {
           e.printStackTrace();
       }*/
       
       try {
           String endpoint = "http://projekt.wifo.uni-mannheim.de/elmar/api/ElmarSearchServices"; //web service is written in Axis.
           Service  service = new Service();
           Call     call    = (Call) service.createCall();
           call.setTargetEndpointAddress( new java.net.URL(endpoint) );
           call.setOperationName("hello");
           call.addParameter("s", XMLType.XSD_STRING, ParameterMode.IN);
           call.setReturnType(XMLType.XSD_ANY);    
           String ret = (String)call.invoke(new Object[] {"Lily"});
           System.out.println(ret); 
       }catch(Exception e) {
           e.printStackTrace();
       }
   }
   
    /**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    }
    
    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}

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
package org.seasr.meandre.components.io.webservice;

import java.io.*;
import java.net.*;

import javax.activation.*;

import org.apache.soap.*;
import org.apache.soap.util.*;
import org.apache.soap.util.xml.*;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.messaging.*;

import org.meandre.annotations.Component;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Consume web service written in MS .NET.",
           name="SOAPClient",
           tags="web service",
           baseURL="meandre://seasr.org/components/data-mining/")

public class SOAPClient implements ExecutableComponent {
    /** When ready for execution.
    *
    * @param cc The component context
    * @throws ComponentExecutionException An exeception occurred during execution
    * @throws ComponentContextException Illigal access to context
    */
   public void execute(ComponentContext cc) throws ComponentExecutionException,
           ComponentContextException {
       try {
           // Create a proxy
           ApacheSoapProxy proxy = new ApacheSoapProxy ();
           
           // Invoke getResponse over SOAP
           String result = proxy.getResponse();
           
           System.out.println(result);
         }
         catch (java.net.MalformedURLException exception) {
           exception.printStackTrace ();
         }
         catch (org.apache.soap.SOAPException exception) {
           exception.printStackTrace ();
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
    
    class ApacheMessageBody extends Body { 
        
        /** potential argument to the web method. */
        public String value;
        
        /**
         * Override the Apache default marshall method 
         * and change how the SOAP Body element
         * is serialized.
         */
        public void marshall (String inScopeEncodingStyle, 
                              Writer sink, 
                              NSStack nameSpaceStack,
                              XMLJavaMappingRegistry registry,
                              SOAPContext context) throws IllegalArgumentException, IOException {
          // Set the Body element
          String soapEnvironmentNamespacePrefix = "SOAP-ENV";
          sink.write ('<'+soapEnvironmentNamespacePrefix+':'+
                      Constants.ELEM_BODY+'>'+
                      StringUtils.lineSeparator);
          
          // Write out the method name and related argument (s)
          /*sink.write ("<GetWeatherByZipCode xmlns=\"http://www.webservicex.net\">"+
                      "<ZipCode>"+value+"</ZipCode>"+
                      "</GetWeatherByZipCode>");*/

          sink.write ("<PhoneVerify xmlns=\"http://webservicemart.com/ws/\">"+
                      "<PhoneNumber>"+value+"</PhoneNumber>"+
                      "</PhoneVerify>");
          
          // Close the Body element
          sink.write ("</" + soapEnvironmentNamespacePrefix+':'+
                      Constants.ELEM_BODY+'>'+
                      StringUtils.lineSeparator);
          
          nameSpaceStack.popScope ();
        }
    }
    
    class ApacheSoapProxy { 
        private URL url = null;
        private String soapActionUri = "";
        private Message message = new Message ();
        private Envelope envelope = new Envelope ();
        DataHandler soapMessage = null;
        
        public ApacheSoapProxy () throws MalformedURLException {
          url = new URL ("http://www.webservicemart.com/phone3t.asmx");
          //url = new URL ("http://www.webservicex.net/WeatherForecast.asmx"); //ending point
        }
        
        /**
         * Apache 2.2 classes encode messages differently than .NET does.
         * Therefore we have to override the piece that builds the body and
         * the pieces that interpret the response.
         */
        public synchronized String getResponse () throws SOAPException {
          String returnValue = "";
          
          if (url == null) {
            throw new SOAPException (Constants.FAULT_CODE_CLIENT,
                                     "An ending point must be specified.");
          }
          // Get this from the soapAction attribute on the
          // soap:operation element that is found within the SOAP
          // binding information in the WSDL
          soapActionUri = "http://webservicemart.com/ws/PhoneVerify";
          //soapActionUri = "http://www.webservicex.net/GetWeatherByZipCode";
          ApacheMessageBody ourBody = new ApacheMessageBody ();
          
          // Set the argument
          //ourBody.value = "61801";
          ourBody.value = "2172440901";
          
          // Replace the default body with our own
          envelope.setBody (ourBody);
          message.send (url, soapActionUri, envelope);
          
          try {
            soapMessage = message.receive();
            returnValue = soapMessage.getContent().toString();
          }
          catch (Exception exception) {
              exception.printStackTrace ();
          }
          return returnValue;
        }
    }
}

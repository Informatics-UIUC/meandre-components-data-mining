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

/**
 * TODO: Fix licensing for this file.
 */

package org.seasr.meandre.components.io.file.output;

import org.meandre.core.*;
import org.meandre.annotations.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.meandre.core.ComponentContextProperties;

/**This component takes an object and returns the JSON or XML representation
 * of the object. It uses XSTREAM library along with xpp3 library to create
 * the XML representation. The limitations of this component are
 * enumerated here <http://xstream.codehaus.org/faq.html#Compatibility/>
 * Please refer to the XSTREAM library for more information
 *
 * @author Amit Kumar
 * Created on Feb 6, 2008 3:57:15 PM
 *
 */
@Component(creator="Amit Kumar", description="Serializes an object to a string using xstream", 
		tags="serialize output io", name="ObjectSerializer",
        baseURL="meandre://seasr.org/components/data-mining/")
        
public class ObjectSerializer implements ExecutableComponent {


	@ComponentProperty(description="Format objects", name="format", defaultValue = "json")
	public static final String DATA_PROPERTY_1 ="format";


	@ComponentInput(description="Java Object", name="object")
	public static final String DATA_INPUT_1 ="object";


	@ComponentOutput(description="String serialized representation of the object", name="stringVal")
	public static final String DATA_OUTPUT_1 ="stringVal";


	@ComponentOutput(description="format json or xml", name="format")
	public static final String DATA_OUTPUT_2 ="format";


	public void initialize(ComponentContextProperties ccp) {
	}

	@SuppressWarnings("unchecked")
	public void execute(ComponentContext context)
			throws ComponentExecutionException, ComponentContextException {
		Object object = context.getDataComponentFromInput( DATA_INPUT_1);
		String format = context.getProperty(DATA_PROPERTY_1);

		Class clazz=null;
		try {
			clazz = Class.forName(object.getClass().getName());
		} catch (ClassNotFoundException e) {
			throw new ComponentExecutionException("Class: " + object.getClass().getName() + " not found...");
		}
		String output=null;
		if(format.equals("json")){
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(clazz.getSimpleName(), clazz);
		output=xstream.toXML(object);
		}else if(format.equals("xml")){
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(clazz.getSimpleName(), clazz);
		output=xstream.toXML(object);
		}else{
			throw new ComponentExecutionException("Invalid format: expected json or xml");
		}

		context.pushDataComponentToOutput(DATA_OUTPUT_1, output);
		context.pushDataComponentToOutput(DATA_OUTPUT_2, format);
	}

	public void dispose(ComponentContextProperties ccp) {
	}

}

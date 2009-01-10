package org.meandre.components.io.webservice;


import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

/**
 * @author Xavier
 * Modified by Loretta
 *
 */

	@Component(creator = "Xavier Llora", 
		description = "service call for output from webUI fragment", 
		name = "ServiceTail", 
		tags = "output, webUI", 
		firingPolicy = Component.FiringPolicy.all,
        baseURL="meandre://seasr.org/components/")

	public class ServiceTail implements ExecutableComponent {

	public final static String INPUT_0_StringValue = "stringValue";
	@ComponentInput(description = "String value of the selection", 
			name = INPUT_0_StringValue)

			public final static String INPUT_1_Response = "response";
	@ComponentInput(description = "The response to be sent to the Service Tail.", 
			name = INPUT_1_Response)

			final static String INPUT_2_Semaphore = "semaphore";
	@ComponentInput(description = "The semaphore to signal response done.", 
			name = INPUT_2_Semaphore)
			
			final static String INPUT_3_Semaphore = "semaphore";
	

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {

		System.out.println("---- Tail fired!!!");
		Object obj = cc.getDataComponentFromInput(INPUT_0_StringValue);
		Semaphore sem = (Semaphore) cc.getDataComponentFromInput(INPUT_2_Semaphore);
		HttpServletResponse response = (HttpServletResponse) cc.getDataComponentFromInput(INPUT_1_Response);

		StringBuffer xmltest = new StringBuffer ("<root>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Bluegrass'>1</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Blues'>2</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Classical'>3</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Country'>4</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='asy Listening'>5</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Electronic'>6</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='HipHop'>7</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Jazz'>8</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Reggae'>9</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Rock'>10</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Bluegrass'>11</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Blues'>12</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Classical'>13</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Country'>9</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Easy Listening'>15</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Electronic'>8</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='HipHop'>5</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Jazz'>3</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Reggae'>2</data>\n");
xmltest.append("<data songid='song1.mp3' frameid='0' modelid='NBayes' fieldid='Rock'>2</data>\n");
xmltest.append("</root>");

		//response.setContentType("text/plain");
		response.setContentType("application/xml");
		try {
			response.getWriter().println(obj.toString());
			response.getWriter().flush();
			System.out.println("---- Tail printed StringValue"+obj.toString());
			//System.out.println("---- Tail printed xmltest"+xmltest.toString());
			sem.release();
			System.out.println("---- Tail released");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.flush();
	}

	/* (non-Javadoc)
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp) {
	}
}


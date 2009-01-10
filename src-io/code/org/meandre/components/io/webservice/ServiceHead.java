package org.meandre.components.io.webservice;

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
/**
 * @author Xavier
 * modified by Loretta
 *
 */
import org.meandre.annotations.Component.Mode;
	@Component(creator = "Xavier Llora",
		description = "service call for input to webUI fragment",
		name = "ServiceHead",
		tags = "input, webUI",
		mode = Mode.webui,
		firingPolicy = Component.FiringPolicy.all,
        baseURL="meandre://seasr.org/components/")

	public class ServiceHead implements ExecutableComponent, WebUIFragmentCallback {

	public final static String OUTPUT_0_StringValue = "stringValue";
	@ComponentOutput(description = "String value of the selection",
			name = OUTPUT_0_StringValue)

			public final static String OUTPUT_1_Response = "response";
	@ComponentOutput(description = "The response to be sent to the Service Tail.",
			name = OUTPUT_1_Response)

			public final static String OUTPUT_2_Semaphore = "semaphore";
	@ComponentOutput(description = "The semaphore to signal response done.",
			name = OUTPUT_2_Semaphore)

	private String sInstanceID;
	private Semaphore sem;
	private ComponentContext ccHandle;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
	 */
	public void dispose(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
	 */
	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		try {
			sInstanceID = cc.getExecutionInstanceID();
			ccHandle = cc;
			// sem.acquire();
			cc.startWebUIFragment(this);
			// sem.acquire();
			// sem.release();
			while ( !cc.isFlowAborting() ) {
				Thread.sleep(1000);
			}
			System.out.println(">>>Done");
			cc.stopWebUIFragment(this);
		} catch (Exception e) {
			throw new ComponentExecutionException(e);
		}
	}
	/*
	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		try {

			sInstanceID = cc.getExecutionInstanceID();
			ccHandle = cc;
			sem.acquire();
			cc.startWebUIFragment(this);
			sem.acquire();
			sem.release();

			System.out.println(">>>Done");
			cc.stopWebUIFragment(this);
		} catch (Exception e) {
			throw new ComponentExecutionException(e);
		}
	}
*/
	/*
	 * (non-Javadoc)
	 *
	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
	 */
	public void initialize(ComponentContextProperties ccp) {
		sem = new Semaphore(1, true);
	}

	/**
	 * This method gets call when a request with no parameters is made to a
	 * component webui fragment.
	 *
	 * @param response
	 *            The response object
	 * @throws WebUIException
	 *             Some problem arose during execution and something went wrong
	 */
	public void emptyRequest(HttpServletResponse response)
	throws WebUIException {

	}

	/**
	 * This method gets called when a call with parameters is done to a given
	 * component webUI fragment
	 *
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @throws WebUIException
	 *             A problem arose during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws WebUIException {
		String sParameter = request.getParameter("stringValue");
		Semaphore sem  = new Semaphore(1,true);
		if (sParameter != null && sParameter.length() > 0) {
			try {
				System.out.println("++++ Head webui called!!!");
				ccHandle.pushDataComponentToOutput(OUTPUT_0_StringValue, sParameter);
				ccHandle.pushDataComponentToOutput(OUTPUT_1_Response, response);
				ccHandle.pushDataComponentToOutput(OUTPUT_2_Semaphore, sem);
				System.out.println("++++ Head webui pushed "+sParameter);
				sem.acquire();
				System.out.println("++++ Head ready to block "+sParameter);
				sem.acquire();
				System.out.println("++++ Head released");
				sem.release();
			} catch (ComponentContextException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.flush();
		}
	}
}
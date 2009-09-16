package org.meandre.components.discovery.ruleassociation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.components.discovery.ruleassociation.ItemSetInterface;
import org.seasr.meandre.support.components.discovery.ruleassociation.ItemSetParser;
import org.seasr.meandre.support.components.discovery.ruleassociation.ItemSetTool;



/** This executable component takes an input stream and reads it in
 * as an item set (.is format)
 *
 * @author mike haberman
 *
 */

@Component(creator="mikeh", description="Creates Item sets from .is formatted files",
		name="InputStreamToItemSets",
		tags="io, input, itemsets",
        baseURL="meandre://seasr.org/components/")

public class InputStreamToItemSets implements ExecutableComponent {


	/*
	 * this should be made into a component WebDavClient to InputStream
	 * // import org.meandre.tools.webdav.WebdavClient;
	final static String DATA_INPUT_URL = "url";
	@ComponentInput(description = "url for webdavClient",
			               name = DATA_INPUT_URL)

    final static String DATA_INPUT_CLIENT = "webdavClient";
	@ComponentInput(description = "the webdavClient",
			               name = DATA_INPUT_CLIENT)

		WebdavClient client;
		String url;
		client = (WebdavClient) cc.getDataComponentFromInput(DATA_INPUT_CLIENT);
		url    = (String) cc.getDataComponentFromInput(DATA_INPUT_URL);
		InputStreamReader reader = new InputStreamReader(client.getResourceAsStream(url));
	*/

	@ComponentProperty(description="number of itemsets per group",
        	                  name="itemsetsPerGroup",
        	           defaultValue="1")
    final static String DATA_PROPERTY = "itemsetsPerGroup";


	@ComponentInput(description = "java.io.InputStream",
			               name = "inputStream")
    final static String DATA_INPUT_STREAM = "inputStream";



	@ComponentOutput(description="ItemSetInterface", name = "itemset")
	final static String DATA_OUTPUT_OBJECT = "itemset";


	/** This method is invoked when the Meandre Flow is being prepared for
	 * getting run.
	 *
	 * @param ccp The properties associated to a component context
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 */

	public void initialize ( ComponentContextProperties ccp )
	throws ComponentExecutionException, ComponentContextException {




	}

	/** This method just pushes a concatenated version of the entry to the
	 * output.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected

	 */
	public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {


		try {
			int itemsPerGroup = Integer.parseInt(cc.getProperty(DATA_PROPERTY));

			InputStream is   = (InputStream) cc.getDataComponentFromInput(DATA_INPUT_STREAM);

			ItemSetParser parser = new ItemSetParser();
			ItemSetInterface itemSets = parser.getItemSets(new InputStreamReader(is), itemsPerGroup);

			ItemSetTool.print(itemSets);

			cc.pushDataComponentToOutput(DATA_OUTPUT_OBJECT, itemSets);
		} catch (IOException e) {
			throw new ComponentExecutionException(e);
		}

	}

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 * @param ccp The properties associated to a component context
	 */
	public void dispose ( ComponentContextProperties ccp )
	throws ComponentExecutionException, ComponentContextException {
	}

}
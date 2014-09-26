package org.seasr.meandre.components.io.datasource.featurelens;

import gnu.trove.iterator.TIntIterator;

import java.sql.Connection;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth.FPPattern;

@Component(creator="Lily Dong",
        	description="Writes the Pattern table of FeatureLens to a database.",
        	name="FeatureLensPattern",
        	tags="FeatureLens",
        	baseURL="meandre://seasr.org/components/data-mining/")

public class FeatureLensPattern implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
	   		   		   defaultValue = "false",
	   		   		   name = "debug")
    public final static String DATA_PROPERTY = "debug";

	@ComponentInput(description="List of Patterns." +
			"<br>TYPE: java.util.List",
             		name= "patternList")
    public final static String DATA_INPUT_PATTERN_LIST = "patternList";
	@ComponentInput(description="JDBC database connection." +
			"<br>TYPE: java.sql.Connection;",
             		name= "connection")
    public final static String DATA_INPUT_CONNECTION = "connection";

	public void initialize(ComponentContextProperties ccp) {}
	public void dispose(ComponentContextProperties ccp) {}

	private boolean debug = false;

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		debug = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY));

		List patterns =
			(List)cc.getDataComponentFromInput(DATA_INPUT_PATTERN_LIST);
		Connection cw =
			(Connection)cc.getDataComponentFromInput(DATA_INPUT_CONNECTION);
		String todo;
		System.out.println("NumPatterns = "+patterns.size());
		for (int i = 0, n = patterns.size(); i < n; i++) {
			FPPattern p = (FPPattern) patterns.get(i);
			todo = ("INSERT into pattern " +
					"(size,text,support,`delete`,collection_id) VALUES(" +
					"'"+p.getSize()+"','"+patternToString(p)+"','"+p.getSupport()+"','0','1');");
			System.out.println(todo);

			try {
				java.sql.Statement s = cw.createStatement();
				int r = s.executeUpdate (todo);
			}
			catch (Exception e) {
				throw new ComponentExecutionException(
						"Error loading pattern " +
						i +
						" "	+
						e.getMessage());
			}
		}
	}

	// copied code below from the PatternDocumentResolver
	private String patternToString(FPPattern p) {
		// Nice vertical-bar-delimited string version of a pattern's terms.
		String s = new String();
		for (TIntIterator pi = p.getPattern(); pi.hasNext();) {
			s += FPPattern.getElementLabel((int) pi.next()) + "|";
		}
		s = s.substring(0, s.length() - 1);
		return s;
	}
}

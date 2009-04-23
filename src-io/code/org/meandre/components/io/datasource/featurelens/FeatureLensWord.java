package org.meandre.components.io.datasource.featurelens;

import org.meandre.components.datatype.table.ExampleTable;

import java.sql.Connection;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
        description="Adds the individual words to the " +
        "Pattern database table of FeatureLens.",
        name="FeatureLensWord",
        tags="FeatureLens",
        baseURL="meandre://seasr.org/components/")

public class FeatureLensWord implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
					   defaultValue = "false",
					   name = "debug")
    public final static String DATA_PROPERTY = "debug";

	@ComponentInput(description="Sparse Table." +
			"<br>TYPE: org.meandre.components.datatype.table.Table",
             		name= "table")
    public final static String DATA_INPUT_TABLE = "table";
	@ComponentInput(description="JDBC database connection." +
			"<br>TYPE: java.sql.Connection",
             		name= "connection")
    public final static String DATA_INPUT_CONNECTION = "connection";

	private boolean debug = false;

	public void initialize(ComponentContextProperties ccp) {}
	public void dispose(ComponentContextProperties ccp) {}

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		debug = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY));

		ExampleTable table =
			(ExampleTable)cc.getDataComponentFromInput(DATA_INPUT_TABLE);
		Connection cw =
			(Connection)cc.getDataComponentFromInput(DATA_INPUT_CONNECTION);

		//skip attributes that have _DOCPROP at the end of attribute label
		// total is used to find the support for the attribute across all documents
		for (int i = 0, n=table.getNumColumns(); i<n; i++) {
			int total = 0;
			if (((String)(table.getColumnLabel(i))).endsWith("_DOCPROP") == false){
				for (int j = 0, m=table.getNumRows(); j<m; j++) {
					if (table.getInt(j, i) > 0)
						total++;
				}
				String todo = ("INSERT into pattern " +
						"(size,text,support,`delete`,collection_id) VALUES(" +
						"'0','"+table.getColumnLabel(i)+"','"+total+"','0','1');");
				try {
					java.sql.Statement s = cw.createStatement();
					int r = s.executeUpdate (todo);
				}
				catch (Exception e) {
					throw new ComponentExecutionException(
							"Error loading word " + i + " " + e.getMessage());
				}
			}
		}
	}
}


package org.meandre.components.io.datasource.featurelens;

import java.sql.Connection;

import java.io.*;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

@Component(creator="Lily Dong",
           description="Writes the Collection table of FeatureLens to a database. "+
           "Specifically setup to take a directory name and create an row in "+
           "the database for the top level and then create a row for each "+
           "subdirectory with a link to the 1st row as parent.",
           name="FeatureLensCollection",
           tags="FeatureLens",
           baseURL="meandre://seasr.org/components/")
public class FeatureLensCollection implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
			   		   defaultValue = "false",
			   		   name = "debug")
    public final static String DATA_PROPERTY = "debug";

	@ComponentInput(description="Directory Name." +
			"<br>TYPE:java.lang.String",
             		name= "directory")
    public final static String DATA_INPUT_DIRECTORY = "directory";
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

		String dirname =
			(String)cc.getDataComponentFromInput(DATA_INPUT_DIRECTORY);
		Connection cw =
			(Connection)cc.getDataComponentFromInput(DATA_INPUT_CONNECTION);
		//Todo: this could be made more generic by creating a property for the name of this collection.
		String todo = ("INSERT into collection " +
				"(type,title,parent_id) VALUES(" +
		   		"'news','VASTnews2007','0');");
		System.out.println(todo);
		try {
			java.sql.Statement s = cw.createStatement();
			int r = s.executeUpdate (todo);
		}
		catch (Exception e) {
			throw new ComponentExecutionException(
					"Error loading Collection: VASTnews2007 "+e.getMessage());
		}

		//for each directory create a new record pointing to collection_id 1 as the parent
		File dir=new File(dirname);
		if (dir.isDirectory()){
			File[] files = dir.listFiles();
			for (int i = 0, n = files.length; i < n; i++) {
				if (files[i].isDirectory()) {
					todo = ("INSERT into collection " +
							"(type,title,parent_id) VALUES(" +
							"'news','"+files[i].getName()+"','1');");
					//System.out.println(todo);
					try {
						java.sql.Statement s = cw.createStatement();
						int r = s.executeUpdate (todo);
					}
					catch (Exception e) {
						throw new ComponentExecutionException(
								"Error loading Collection: news " +
								files[i].getName() +
								" " +
								e.getMessage());
					}
				}
			}
		}
	}
}


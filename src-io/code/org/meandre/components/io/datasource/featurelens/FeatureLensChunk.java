package org.meandre.components.io.datasource.featurelens;

import java.sql.Connection;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.*;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.seasr.components.text.datatype.corpora.Document;

@Component(creator="Lily Dong",
    	   description="Writes the Chunk table of FeatureLens to a database.",
    	   name="FeatureLensChunk",
    	   tags="FeatureLens",
    	   baseURL="meandre://seasr.org/components/")

public class FeatureLensChunk implements ExecutableComponent {
	@ComponentProperty(description = "Verbose output.",
	   		   		   defaultValue = "false",
	   		   		   name = "debug")
	public final static String DATA_PROPERTY_DEBUG = "debug";
	@ComponentProperty(description = "The Collection Label is the name " +
					   "stored in the database " +
                	   "for the collection being processed.",
	   		   		   defaultValue = "MyNewCollection",
	   		   		   name = "label")
	public final static String DATA_PROPERTY_LABEL = "label";

	@ComponentInput(description="Document." +
			"<br>TYPE: org.seasr.components.text.datatype.corpora.Document",
             		name= "document")
    public final static String DATA_INPUT_DOCUMENT = "document";
	@ComponentInput(description="JDBC database connection." +
			"<br>TYPE: java.sql.Connection;",
             		name= "connection")
    public final static String DATA_INPUT_CONNECTION = "connection";

	@ComponentOutput(description="Document." +
            "<br>TYPE: org.seasr.components.text.datatype.corpora.Document",
             		 name="document")
    public final static String DATA_OUTPUT = "docuemnt";


    private int m_docsProcessed = 1;
    private Connection cw = null;
    private boolean debug = false;
    private String collection = "MyNewCollection";

    public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
    	debug = Boolean.parseBoolean(cc.getProperty(DATA_PROPERTY_DEBUG));
    	collection = cc.getProperty(DATA_PROPERTY_LABEL);

    	cw = (Connection)cc.getDataComponentFromInput(
    			DATA_INPUT_CONNECTION);
    	// Inserting the Collection information into the collection database.
        // This needs to be done once at the beginning.
        String todo = ("INSERT into collection " +
        		"(type,title,parent_id) VALUES(" +
         		"'news','"+collection+"','0');");
        //System.out.println(todo);
        try {
        	java.sql.Statement s = cw.createStatement();
            int r = s.executeUpdate (todo);
        } catch (Exception e) {
        	throw new ComponentExecutionException(
        			"Error loading Collection" +
        			e.getMessage());
        }

        Document doc = (Document)cc.getDataComponentFromInput(
        		DATA_INPUT_DOCUMENT);

        //parent_id points to the collection_id in the collection table

        String source = (String) doc.getFeatures().get("gate.SourceURL");

        //Find the directory one up from the file for use as collection label
        File dir=new File(source.replaceFirst("file:", ""));
        String s=dir.getParent();
        String t=dir.getParentFile().getParent();

        String tst =  s.replaceFirst(t, "");
        tst = tst.replaceFirst("/", "");

        //Insert into collection the directory - the db will only allow unique entries
        todo = ("INSERT into collection " +
        		"(type,title,parent_id) VALUES(" +
	    		"'news','"+tst+"','1');");
        //System.out.println(todo);
        java.sql.Statement stmt;
        try {
        	stmt = cw.createStatement();

            int r = stmt.executeUpdate(todo);
            if (r==1)
            	m_docsProcessed=1;
        } catch (Exception e) {
        	throw new ComponentExecutionException(
        			"Error loading collection " +
        			tst +
        			" " +
        			e.getMessage());
        }
        //Insert into chunk use db function to find the collection_id from collection table
        todo = ("INSERT into chunk (n,type,text,parent_id)" +
        		" SELECT '"+ m_docsProcessed +
        		"'," + "'newsItem','" + doc.getContent().replaceAll("'", "''") +
        		"'," +"collection.collection_id FROM collection WHERE collection.title='"+tst+"';");

        //System.out.println(todo);

        try {
        	stmt = cw.createStatement();

            int r = stmt.executeUpdate(todo);
            if (r==1)
            	m_docsProcessed++;
        } catch (Exception e) {
        	System.err.println("Error loading chunk " + m_docsProcessed +
                    " " + e);
        }

        cc.pushDataComponentToOutput(DATA_OUTPUT, doc);
    }

    public void initialize(ComponentContextProperties ccp) {
        m_docsProcessed = 1;
        cw = null;
    }

    public void dispose(ComponentContextProperties ccp) {
        cw = null;
    }
}


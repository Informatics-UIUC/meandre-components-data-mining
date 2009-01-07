package org.meandre.components.discovery.ruleassociation.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class ItemSetParser {
	
    
    public ItemSetInterface getItemSets(String filename)
       throws FileNotFoundException, IOException
    {
       File file = new File(filename);
       FileInputStream fis = new FileInputStream(file);
       return getItemSets(new InputStreamReader(fis));
    }
    
    public ItemSetInterface getItemSets(Reader r)
       throws IOException
    {
       BufferedReader reader = new BufferedReader(r);
          
     
       String line = null;
       SimpleItemSet itemSet = new SimpleItemSet();
       
       while ( (line = reader.readLine()) != null) {
          
          line = line.replaceAll("[{}]", "");
          line = line.trim();
          StringTokenizer tokens = new StringTokenizer(line, ",");
          
          Set<String> set = new HashSet<String>();
          while(tokens.hasMoreTokens()){
             String item = tokens.nextToken();
             set.add(item);
          }
          itemSet.addSet(set); 
       }
       
       itemSet.compute();
       
       return itemSet;
       
    }
}
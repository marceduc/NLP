package service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PubMedSearchEngineImpl implements PubMedSearchEngine {
	
	public static HashMap<String, String> titleWL = new HashMap<String, String>();
	public static HashMap<String, String> abstractWL = new HashMap<String, String>();
	public static HashMap<String, String> yearL = new HashMap<String, String>();
	public static HashMap<String, String> meshL = new HashMap<String, String>();
	
    public PubMedSearchEngineImpl() {
        //Don't change the constructor!
    }

    @Override
    public void buildIndex(Path pubmedDirectory) {
      
    	//Indiziert alle Informationen
    	index_parser ip = new index_parser(pubmedDirectory);
    	ip.run();
    	titleWL = index_parser.titleWL;
    	abstractWL = index_parser.abstractWL;
    	yearL = index_parser.yearL;
    	meshL = index_parser.meshL;
    	
    }

    @Override
    public Set<Integer> query(String query) {
        
    	
        return new HashSet<>();
    }
}

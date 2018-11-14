package service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PubMedSearchEngineImpl implements PubMedSearchEngine {

    public PubMedSearchEngineImpl() {
        //Don't change the constructor!
    }

    @Override
    public void buildIndex(Path pubmedDirectory) {
        //TODO: Implement creation of indices!
    }

    @Override
    public Set<Integer> query(String query) {
        // TODO: Implement the query logic!
        return new HashSet<>();
    }
}

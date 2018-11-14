package service;

import java.nio.file.Path;
import java.util.Set;

public interface PubMedSearchEngine {

    /**
     * Builds the indices necessary to support the search / query engine.
     *
     * @param pubmedDirectory Path to a directory containing PubMed xml files.
     */
    void buildIndex(Path pubmedDirectory) throws Exception;

    /**
     * Retrieves all matching documents for the given query. The query can be composed
     * of term queries (e.g. title:disease), phrase queries (e.g. abstract:"fish kidney")
     * and (AND) conjunctions of them (e.g. title:disease AND abstract:"fish kidney").
     *
     * @param query The query describing the user search interest.
     *
     * @return Set of pubmed ids of the documents matching the given query.
     */
    Set<Integer> query(String query) throws Exception;

}

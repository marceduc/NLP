import service.PubMedSearchEngine;
import service.PubMedSearchEngineImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class PubMedSearchApplication {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: java -jar PubMedSearchApplication.jar " +
                    "<pubMed-Directory> <queries-file> <results-file>");
            System.exit(-1);
        }

        final Path pubmedDirectory = Paths.get(args[0]);
        if (!Files.isDirectory(pubmedDirectory)) {
            System.err.println(format("%s isn't a valid directory!", pubmedDirectory));
            System.exit(-1);
        }

        final Path queryFile = Paths.get(args[1]);
        if (!Files.exists(queryFile)) {
            System.err.println(format("Can't find query file %s!", queryFile));
            System.exit(-1);
        }

        final Path resultsFile = Paths.get(args[2]);
        if (!Files.exists(resultsFile)) {
            System.err.println(format("Can't find results file %s!", resultsFile));
            System.exit(-1);
        }

        final PubMedSearchApplication searchApplication = new PubMedSearchApplication();
        searchApplication.run(pubmedDirectory, queryFile, resultsFile);
    }

    public void run(Path pubmedDirectory, Path queryFile, Path resultFile) throws Exception {
        PubMedSearchEngine searchEngine = new PubMedSearchEngineImpl();

        System.out.println("Start building index");
        final long indexStart = System.nanoTime();
        searchEngine.buildIndex(pubmedDirectory);
        final long indexEnd = System.nanoTime();

        System.out.println(format("Finished index creation after %s ns", indexEnd-indexStart));

        final List<String> queries = readQueries(queryFile);
        final List<Set<Integer>> results = readQueryResults(resultFile);

        for (int i = 0; i < queries.size(); i++) {
            final String query = queries.get(i);
            final Set<Integer> expectedResult = results.get(i);

            System.out.println(format("Query          : %s", query));
            final long queryStart = System.nanoTime();
            final Set<Integer> actualResult = searchEngine.query(query);
            final long queryEnd = System.nanoTime();

            final String expectedResultStr = expectedResult.stream()
                    .sorted().map(Objects::toString).collect(joining(","));

            final String actualResultStr = actualResult.stream()
                    .sorted().map(Objects::toString).collect(joining(","));

            System.out.println(format("Runtime        : %s nanoseconds", queryEnd-queryStart));
            System.out.println(format("Expected result: %s", expectedResultStr));
            System.out.println(format("Actual result  : %s", actualResultStr));
            System.out.println(expectedResult.equals(actualResult) ? "SUCCESS\n" : "FAILURE\n");
        }
    }

    private List<String> readQueries(Path queryFile) {
        List<String> queries = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(queryFile, UTF_8)) {
            String line = null;
            while((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    queries.add(line);
                }
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        return queries;
    }

    private List<Set<Integer>> readQueryResults(Path resultFile) {
        List<Set<Integer>> results = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(resultFile, UTF_8)) {
            String line = null;
            while((line = reader.readLine()) != null) {
                String[] result = line.split(",");
                final Set<Integer> resultIds = Arrays.stream(result)
                        .map(String::trim).map(Integer::valueOf).collect(toSet());
                results.add(resultIds);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        return results;
    }
}

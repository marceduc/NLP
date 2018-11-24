package service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

public class PubMedSearchEngineImpl implements PubMedSearchEngine {

	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> titleWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> abstractWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	public static HashMap<String, ArrayList<Integer>> yearL = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, HashMap<Integer, ArrayList<Integer>>> meshWL = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();

	public PubMedSearchEngineImpl() {
		// Don't change the constructor!
	}

	@Override
	public void buildIndex(Path pubmedDirectory) {

		// Indiziert alle Informationen
		index_parser ip = new index_parser(pubmedDirectory);
		ip.run();
		titleWL = index_parser.titleWL;
		abstractWL = index_parser.abstractWL;
		yearL = index_parser.yearL;
		meshWL = index_parser.meshWL;
		ip = null;

	}

	@Override
	public Set<Integer> query(String query) {

		// Ergebnissliste
		HashSet<Integer> results = new HashSet<Integer>();

		// Splitten der Anfrage in Subanfragen
		if (query.contains("AND")) {

			String[] sub_queries = query.split(" AND ");

			// Für jede Subanfrage do....
			for (String sub_query : sub_queries) {

				// Entscheide ob Subanfrage Term- oder Phrasenanfrage ist
				if (!sub_query.contains("\"")) {

					String[] split = sub_query.toLowerCase().split(":");
					String field = split[0];
					String term = split[1];

					if (results.isEmpty()) {

						results = getIDSforTerm(field, term);

					} else {

						results = mergeSets(results, getIDSforTerm(field, term));
					}

				} else {

					String[] split = sub_query.toLowerCase().split(":");
					String field = split[0];
					String phrase = split[1];

					if (results.isEmpty()) {

						results = checkPhrase(field, phrase);

					} else {

						results = mergeSets(results, checkPhrase(field, phrase));

					}

				}

			}

			return results;

		} else {

			String[] split = query.toLowerCase().split(":");
			String field = split[0];
			String term = split[1];

			if (!query.contains("\"")) {

				results = getIDSforTerm(field, term);
				return results;
			} else {

				results = checkPhrase(field, term);
				return results;
			}

		}

	}

	//Gibt die IDS als HashSet aus, die die Termanfrage erfüllen
	public HashSet<Integer> getIDSforTerm(String field, String term) {

		HashSet<Integer> resultIDS = new HashSet<Integer>();

		if (field.equals("title")) {

			if (titleWL.containsKey(term)) {

				HashMap<Integer, ArrayList<Integer>> pmids = titleWL.get(term);

				for (Map.Entry<Integer, ArrayList<Integer>> entry : pmids.entrySet()) {

					resultIDS.add(entry.getKey());

				}

			}

		}

		if (field.equals("abstract")) {

			if (abstractWL.containsKey(term)) {

				HashMap<Integer, ArrayList<Integer>> pmids = abstractWL.get(term);

				for (Map.Entry<Integer, ArrayList<Integer>> entry : pmids.entrySet()) {

					resultIDS.add(entry.getKey());

				}

			}

		}

		if (field.equals("year")) {

			if (yearL.containsKey(term)) {

				ArrayList<Integer> pmids = yearL.get(term);

				for (int pmid : pmids) {

					resultIDS.add(pmid);

				}

			}

		}

		if (field.equals("mesh")) {

			if (meshWL.containsKey(term)) {

				HashMap<Integer, ArrayList<Integer>> pmids = meshWL.get(term);

				for (Map.Entry<Integer, ArrayList<Integer>> entry : pmids.entrySet()) {

					resultIDS.add(entry.getKey());

				}

			}

		}

		return resultIDS;

	}

	//Gibt die Schnittmenge zwischen zwei HashSets als HashSet wieder
	public HashSet<Integer> mergeSets(HashSet<Integer> set1, HashSet<Integer> set2) {

		HashSet<Integer> mergedSet = new HashSet<Integer>();

		if (set1.isEmpty())
			return mergedSet;
		if (set2.isEmpty())
			return mergedSet;

		for (int pmid1 : set1) {

			for (int pmid2 : set2) {

				if (pmid1 == pmid2) {

					mergedSet.add(pmid2);

				}

			}

		}

		return mergedSet;

	}

	//Schaut nach ob 2 Wörter hintereinander folgen in einem Document
	public HashSet<Integer> checkWords(String field, String word1, String word2) {

		HashSet<Integer> results = new HashSet<Integer>();

		// Entscheide ob in Titel, Abstracts oder MesH gesucht werden soll
		if (field.equals("title")) {

			if (titleWL.containsKey(word1) && titleWL.containsKey(word2)) {

				HashMap<Integer, ArrayList<Integer>> hm1 = titleWL.get(word1);
				HashMap<Integer, ArrayList<Integer>> hm2 = titleWL.get(word2);

				for (Map.Entry<Integer, ArrayList<Integer>> entry1 : hm1.entrySet()) {

					for (Map.Entry<Integer, ArrayList<Integer>> entry2 : hm2.entrySet()) {

						int pmid1 = entry1.getKey();
						int pmid2 = entry2.getKey();

						if (pmid1 == pmid2) {

							ArrayList<Integer> posArray1 = hm1.get(pmid1);
							ArrayList<Integer> posArray2 = hm2.get(pmid2);

							for (int pos1 : posArray1) {

								for (int pos2 : posArray2) {

									if (Math.abs(pos1 - pos2) == 1) {

										results.add(pmid1);

									}

								}

							}

						}
						
					}

				}

			} else {

				return results;

			}

		}

		if (field.equals("abstract")) {

			if (abstractWL.containsKey(word1) && abstractWL.containsKey(word2)) {

				HashMap<Integer, ArrayList<Integer>> hm1 = abstractWL.get(word1);
				HashMap<Integer, ArrayList<Integer>> hm2 = abstractWL.get(word2);

				for (Map.Entry<Integer, ArrayList<Integer>> entry1 : hm1.entrySet()) {

					for (Map.Entry<Integer, ArrayList<Integer>> entry2 : hm2.entrySet()) {

						int pmid1 = entry1.getKey();
						int pmid2 = entry2.getKey();

						if (pmid1 == pmid2) {

							ArrayList<Integer> posArray1 = hm1.get(pmid1);
							ArrayList<Integer> posArray2 = hm2.get(pmid2);

							for (int pos1 : posArray1) {

								for (int pos2 : posArray2) {

									if (Math.abs(pos1 - pos2) == 1) {

										results.add(pmid1);

									}

								}

							}

						}
						
					}

				}

			} else {

				return results;

			}

		}

		if (field.equals("mesh")) {

			if (meshWL.containsKey(word1) && meshWL.containsKey(word2)) {

				HashMap<Integer, ArrayList<Integer>> hm1 = meshWL.get(word1);
				HashMap<Integer, ArrayList<Integer>> hm2 = meshWL.get(word2);

				for (Map.Entry<Integer, ArrayList<Integer>> entry1 : hm1.entrySet()) {

					for (Map.Entry<Integer, ArrayList<Integer>> entry2 : hm2.entrySet()) {

						int pmid1 = entry1.getKey();
						int pmid2 = entry2.getKey();

						if (pmid1 == pmid2) {

							ArrayList<Integer> posArray1 = hm1.get(pmid1);
							ArrayList<Integer> posArray2 = hm2.get(pmid2);

							for (int pos1 : posArray1) {

								for (int pos2 : posArray2) {

									if (Math.abs(pos1 - pos2) == 1) {

										results.add(pmid1);

									}

								}

							}

						}
						
					}

				}

			} else {

				return results;

			}

		}

		return results;
	}

	//Gibt die IDS als HashSet aus, die die Phrasenanfrage erfüllen
	public HashSet<Integer> checkPhrase(String field, String phrase) {
		
		phrase = phrase.replace("\"", "");
		String[] words = phrase.split("[\\s+.,:!?]");
		
		HashSet<Integer> results = new HashSet<Integer>();
		
		if (words.length == 0) {
			return results;
		}
		
		if (words.length == 1) {
			
			results = getIDSforTerm(field, words[0]);
			return results;
		}
		
		for (int i = 0; i < words.length - 1; i++) {
			
			if (results.isEmpty()) {
				
				results = checkWords(field, words[i], words[i+1]);
				
			}
			else {
				HashSet<Integer> tmp = checkWords(field, words[i], words[i+1]);
				results = mergeSets(results, tmp);
			}
			
		}
		
		return results;
		
		
	}
}

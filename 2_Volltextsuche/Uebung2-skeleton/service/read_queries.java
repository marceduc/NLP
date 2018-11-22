import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

//reads in queries, returns list of HashMaps = {tag:[word1,word2,...]}
public class read_queries {

	public static void main(String[] args) {

		ArrayList<String> lines = new ArrayList<String>();

		// The name of the file to open.
      String fileName = "../../Datensatz/queries.txt";

      // This will reference one line at a time
      String line = null;

      try {
          // FileReader reads text files in the default encoding.
          FileReader fileReader =
              new FileReader(fileName);

          // Always wrap FileReader in BufferedReader.
          BufferedReader bufferedReader =
              new BufferedReader(fileReader);

          while((line = bufferedReader.readLine()) != null) {
              //System.out.println(line);
							lines.add(line);
          }

					// Always close files.
					bufferedReader.close();

				}
				catch(FileNotFoundException ex) {
						System.out.println(
								"Unable to open file '" +
								fileName + "'");
				}
				catch(IOException ex) {
						System.out.println(
								"Error reading file '"
								+ fileName + "'");
						// Or we could just do this:
						// ex.printStackTrace();
				}

				//String query = lines.get(4);
				line = lines.get(4);
				System.out.println("testetestetetstses");
				System.out.println(line);
				Map<String, ArrayList<String>> query = line_to_query(line);


				for (String name: query.keySet()){

				 String key =name.toString();
				 String value = query.get(name).toString();
				 System.out.println(key + " " + value);
			 }

			 //for testing remove when merging with Jermeies code
			 // Key = Word, Jahr, Descriptor || Value = PMID,PMID,PMID, ....
			 public static HashMap<String, String> titleWL = new HashMap<String, String>();
			 public static HashMap<String, String> abstractWL = new HashMap<String, String>();
			 titleWL.put('men', '1,2,3,4,5');
			 titleWL.put('therapy', '1,2,3,4,5');
			 titleWL.put('cancer', '1,2,3,4,5');

				/*
				public static HashMap<String, String> currentwL = new HashMap<String, String>();
				String tag = query.getkey();

				//initializ cadidates with all existing IDs, and empty new_cadidates
				Set candidates = new HashSet();
				Set new_candidates = new HashSet();

				for (String key: titletWL.keySet()) {
			    for(String entry:titletWL.get(key).split(',')){
			    	candidates.add();
					}
			}
			//toDo iterate throw tags as keys of query
			// select dict based on tag
				if(tag == 'title'){
					currentwL = titletWL;
			} elseif(tag == 'abstract'){
					currentwL = abstractWL;
			} elseif(initialkey == 'year'){
					currentwL = yearL;
		  } elseif(tag == 'year'){
					currentwL = meshL;
		  } else{
					System.out.println("Error " + initialkey + " does not exist!");
			}
			for(String query_term:query.get(tag)){
			//for every queryterm get
				for(String id: currentwL.get(query_term)){
					new_candidates.add(ID);
				}
				candidates.retainAll(new_candidates);
				//clear new_candidates
				new_candidates.clear();
			}

				*/
      }

			public static Map<String, ArrayList<String>> line_to_query(String line){
				//query_dic = {abstract:[word1,word2,word3,...],title:[word1,word2,],year:[1999,2000,...]}
				HashMap<String, ArrayList<String>> query_dic = new HashMap<String, ArrayList<String>>();
				ArrayList<String> searchTerms = new ArrayList<String>();


				//ArrayList current = dictMap.get(dictCode);

				for (String retval: line.split(" AND ")) {
					String tag = retval.split(":")[0];
					String phrase = retval.split(":")[1];

					System.out.println("search tag:" + tag);

					if (query_dic.containsKey(tag)) {
						System.out.println(tag + "exists");
						//searchTerms = query_dic[tag];
						for(String word:phrase.split(" ")){
							searchTerms.add(word);
							query_dic.get(tag).add(word);
						}
					} else{
						 System.out.println(tag + "does not exists");
						 searchTerms.clear();
						 for(String word:phrase.split(" ")){
							 searchTerms.add(word);
						 }
						 query_dic.put(tag,searchTerms);
					 }

				}
				return query_dic;

			}


  }

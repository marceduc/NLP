import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

      }

			public static Map<String, ArrayList<String>> line_to_query(String line){

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

					 for (String name: query_dic.keySet()){

						String key =name.toString();
						String value = query_dic.get(name).toString();
						System.out.println(key + " " + value);
					}
				}
				return query_dic;

			}


  }

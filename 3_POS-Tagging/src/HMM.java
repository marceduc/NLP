import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Value;

public class HMM {

	public static void main(String[] args) {
		// TODO read files
		System.out.println(System.getProperty("user.dir"));
		List<String> results = new ArrayList<String>();

		String PATH = "./brown_training/";
		
		File[] files = new File(PATH).listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		for (File file : files) {
		    if (file.isFile()) {
		        results.add(file.getName());
		    }
		}
		
		

		
		System.out.println(results.get(0));
		
		List<String> sentences = new ArrayList<String>();
		for(String filename : results) {
		    try {
		    	
		    	String content = readFile(PATH + filename , StandardCharsets.UTF_8);
		    	
		    	List<String> tmp = Arrays.asList(content.split("\\r?\\n"));
		    	for(String s:tmp) {
		    		s = s.trim();
		    		if(s.length()>2) {
		    			sentences.add(s);
		    		}
		    	}

		    }
	
		    	
	
			catch(FileNotFoundException ex) {
					System.out.println(
							"Unable to open file '" +
									"./brown_training/"+ results.get(0) + "'");
			}
			catch(IOException ex) {
					System.out.println(
							"Error reading file '"
							+ "./brown_training/"+ results.get(0) + "'");
					// Or we could just do this:
					// ex.printStackTrace();
			}
		}
		System.out.println(sentences.get(0));
		System.out.println(sentences.get(1));
		System.out.println(sentences.get(2));
		System.out.println(sentences.size());
		System.out.println(sentences.get(1).split(" ")[0].split("/")[1]);
		System.out.println(sentences.get(1).split("\\s++")[0]);
		
		
		//calculate Initial Probabilities
		HashMap<String, Double> start_prob = new HashMap<String, Double>();
		HashMap<String, HashMap<String, Double>> emission_mat = new HashMap<String, HashMap<String, Double>>();
	 	HashMap<String, Double> word_counts = new HashMap<String, Double>();
	 	
	 	List<String> words = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		
		
		
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			//List<String> pairs = Arrays.asList(sentence.split(" "));
			
			//get start probabilities
			String start_tag = pairs[0].split("/")[1]; 
			//String start_tag = sentence.split(" ")[0].split("/")[1];
			
			System.out.println(start_tag);
			
			if(start_prob.containsKey(start_tag)) {
				
				double val = start_prob.get(start_tag)+ (double) 1.0;
				start_prob.put(start_tag, val);
				
			}else {
				
				start_prob.put(start_tag, (double) 1);
				
			}
			
			//calculate probability by dividing through sentence sum
			for(String key:start_prob.keySet() ) {
				double val = start_prob.get(key) / (double) sentences.size();
				start_prob.put(key, val);
				//System.out.println(key + start_prob.get(key) );
				
			}
			
			// get emission matrix
			for(String pair:pairs) {
				
				String word = pair.split("/")[0];
				String tag = pair.split("/")[1];
				
				//count all word occurences for normalization
				if(word_counts.containsKey(word)) {
					double count = word_counts.get(word) + (double) 1;
					word_counts.put(word, count);
				}else {
					word_counts.put(word, (double) 1);
				}
				
				if(emission_mat.containsKey(tag)) {
					if(emission_mat.get(tag).containsKey(word)) {
						//get tag emission matrix
						HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
						//get value of word
						double val = emission_mat.get(tag).get(word) + (double) 1;
						//update value
						tag_emission_prob.put(word, val);
						//update tag emission matrix
						emission_mat.put(tag, tag_emission_prob);						
						
					}else {
						//get tag emission matrix
						HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
						//create word, set to 1 
						tag_emission_prob.put(word, (double) 1);
						//update tag emission matrix
						emission_mat.put(tag, tag_emission_prob);	
					}
				}else {
					//create new tag emission matrix
					HashMap<String, Double> tag_emission_prob  = new HashMap<String, Double>();
					//create word, set to 1 
					tag_emission_prob.put(word, (double) 1);
					//update tag emission matrix
					emission_mat.put(tag, tag_emission_prob);
				}
				
			}
			/*
			for(String word : word_counts.keySet()) {
				for(String tag : emission_mat.keySet()) {
					if(emission_mat.get(tag).get(word) != null) {
						//get tag emission matrix
						HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
						//get value of word
						double val = emission_mat.get(tag).get(word) / word_counts.get(word);
						//update value
						tag_emission_prob.put(word, val);
						//update tag emission matrix
						emission_mat.put(tag, tag_emission_prob);	
					}
				}
			}
			*/

			
		}
		
		System.out.println("-------------");
		System.out.print(emission_mat.entrySet());
		System.out.print(start_prob.entrySet());
		
	
		
		
		
	



		
	}
		

	
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}

}

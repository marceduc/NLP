import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

public class ApplyModel {

	public static void main(String[] args) {
		// TODO read files
		System.out.println(System.getProperty("user.dir"));

		String train_folder = "./brown_training/";
		String input_folder = "./input/";
		String output_folder = "./output/";
		
		List<String> sentences = sentences_from_path(train_folder);
		
		HMM myHmm = new HMM("awesome");
		System.out.println(myHmm.name);
		
		
		myHmm.get_start_prob(sentences);
		System.out.println(myHmm.start_prob);
		myHmm.get_emission(sentences);
		System.out.println(myHmm.emission_mat.keySet());
		System.out.println(myHmm.emission_mat.get("vb"));
		System.out.println("-----");
		myHmm.get_transition(sentences);
		System.out.println(myHmm.transition_mat.get("vb"));
		myHmm.save_matrix(myHmm.emission_mat, "emission.txt");
		myHmm.save_matrix(myHmm.transition_mat, "transition_mat.txt");
		myHmm.save_start_prob(myHmm.start_prob, "start_probabilities.txt");
		
		//myHmm.train(sentences);
		//System.out.println(myHmm.emission_mat);
		//System.out.println(myHmm.transition_mat);
		
		
		
		myHmm.emission_mat = myHmm.load_matrix("emission.txt");
		myHmm.transition_mat = myHmm.load_matrix("transition_mat.txt");
		myHmm.start_prob = myHmm.load_start_prob("start_probabilities.txt");
		System.out.println(myHmm.emission_mat.get("vb"));
		
	
		sentences = sentences_from_path(input_folder);
		
		
		HashMap<String, Double> word_state_prob = new HashMap<String, Double>();
		
		
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			List<String> words = new ArrayList<>();
			
			for(String pair:pairs) {
				words.add(pair.split("/")[0]);				
			}
			System.out.println(words);
			
			System.out.println("______");
			System.out.println(myHmm.known_words.contains("defendants"));
			System.out.println(myHmm.known_words);
			
			double em_prob;
			
			//initialize frist word
			String first_word = words.get(0);
			for(String state : myHmm.emission_mat.keySet()){
				double start_p = myHmm.start_prob.get(state);
				if(myHmm.known_words.contains(first_word)) {
					em_prob = myHmm.emission_mat.get(state).get(first_word);
				} else { //dealing with unknown words
					em_prob = (double) 1 / (double) myHmm.emission_mat.size();
				}
				start_p = Math.log(start_p) + Math.log(em_prob);
				word_state_prob.put(state, start_p);
				myHmm.LLH_mat.put(first_word, word_state_prob);				
			}
			
			
		
	}
		
		
	}
		
	static List<String> sentences_from_path(String PATH){
		
		List<String> results = new ArrayList<String>();
		
		File[] files = new File(PATH).listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		for (File file : files) {
		    if (file.isFile()) {
		        results.add(file.getName());
		    }
		}
		
		

		
		List<String> sentences = new ArrayList<String>();
		for(String filename : results) {
			
			if(filename == ".DS_Store") {
				continue;
			}
			
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
		
		return(sentences) ;
	}
	
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}

}

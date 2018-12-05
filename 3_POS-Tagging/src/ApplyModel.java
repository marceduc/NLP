import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
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
		
		
		Model myMod = new Model("HMM");
		System.out.println(myMod.name);
		myMod.get_knowns(sentences);
		System.out.println(myMod.known_tags);
		myMod.get_start_p(sentences);
		System.out.println(myMod.start_p.keySet());
		System.out.println(myMod.start_p);
		
		myMod.get_emission(sentences);
		System.out.println(myMod.em_mat.keySet());
		System.exit(0);
		System.out.println(myMod.em_mat.get("vbn"));
		
		myMod.get_transition(sentences);
		System.out.println(myMod.tr_mat.keySet());
		System.out.println(myMod.tr_mat.get("vbn"));
		
		System.out.println("Trainging finished");
		
		
		sentences = sentences_from_path(input_folder);
		List<String> subset = new ArrayList<>();
		subset.add(sentences.get(0));
		
		myMod.annotate(subset);

		
		
		System.exit(0);
		
		HMM myHmm = new HMM("myHmm");
		System.out.println(myHmm.name);
			
		

		
		myHmm.get_emission(sentences);
		myHmm.get_start_prob(sentences);
		myHmm.get_transition(sentences);
		
		System.out.println(myHmm.start_prob.keySet());
		System.out.println(myHmm.emission_mat.keySet());

		System.out.println(myHmm.transition_mat.keySet());
		
		for(String key :myHmm.transition_mat.keySet() ) {
			System.out.println(myHmm.transition_mat.get(key));
		}
		

		
		System.out.println("Trainging finished");
		//System.exit(0);
		/*
		System.out.println(myHmm.start_prob);
		System.out.println(myHmm.emission_mat.keySet());
		System.out.println(myHmm.emission_mat.get("vb"));
		System.out.println("-----");
		System.out.println(myHmm.transition_mat.get("vb"));
		/*
		//save model
	      try {
	          FileOutputStream fileOut =
	          new FileOutputStream("bla.txt");
	          ObjectOutputStream out = new ObjectOutputStream(fileOut);
	          out.writeObject(myHmm);
	          out.close();
	          fileOut.close();
	          System.out.printf(myHmm.name + "is saved in myHmm.txt");
	       } catch (IOException i) {
	          i.printStackTrace();
	       }
		
			/*load model
	      myHmm = null;
	      try {
	          FileInputStream fileIn = new FileInputStream("myHmm.txt");
	          ObjectInputStream in = new ObjectInputStream(fileIn);
	          myHmm = (HMM) in.readObject();
	          in.close();
	          fileIn.close();
	       } catch (IOException i) {
	          i.printStackTrace();
	          return;
	       } catch (ClassNotFoundException c) {
	          System.out.println("Employee class not found");
	          c.printStackTrace();
	          return;
	       }
		*/
		
		
		/*
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
		*/
	
		sentences = sentences_from_path(input_folder);
		subset = new ArrayList<>();
		subset.add(sentences.get(0));
		
		myHmm.annotate(subset);

		
		
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

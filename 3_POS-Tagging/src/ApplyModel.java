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
		
		
		//sentences = subset;
		
		
		
		//System.out.println(myMod.known_tags);
		
		
		myMod.get_start_p(sentences);		
		System.out.println(myMod.start_p.get("at"));
		System.out.println(myMod.start_p.get("be"));
		System.out.println(myMod.start_p.get("nil"));
		//counts are correct
		
		myMod.get_emission(sentences);
		//myMod.get_emission_from_csv("em_short.csv");		
		System.out.println("Emission_stats");
		//System.out.println(myMod.em_mat);
		
		System.out.println(myMod.em_mat.get("to").get("to"));
		System.out.println(myMod.em_mat.get("at").get("the"));
		System.out.println(myMod.em_mat.get("in").get("of"));
		System.out.println(myMod.em_mat.get("in").get("through"));
		//Error in counting, values should be 14137, 59668, 34289,843 Validated with R-Script
		
		
		HMM myHmm = new HMM("myHmm");
		System.out.println(myHmm.name);
			
		

		
		myHmm.get_emission(sentences);
		System.out.println(myHmm.emission_mat.get("to").get("to"));
		System.out.println(myHmm.emission_mat.get("at").get("the"));
		System.out.println(myHmm.emission_mat.get("in").get("of"));
		System.out.println(myHmm.emission_mat.get("in").get("through"));
		
		myMod.em_mat = myHmm.emission_mat;
		myMod.p_from_em_count();
		System.out.println(myHmm.emission_mat.get("to").get("to"));
		System.out.println(myHmm.emission_mat.get("at").get("the"));
		System.out.println(myHmm.emission_mat.get("in").get("of"));
		System.out.println(myHmm.emission_mat.get("in").get("through"));
		//in/ throught freq, p correct, log_p incorrect?!
		
		
		
		myMod.get_transition(sentences);
		//System.out.println(myMod.tr_mat.get(" ").get("at"));
		System.out.println("transition_stats");
		System.out.println(myMod.tr_mat.get("at").get("nn"));
		System.out.println(myMod.tr_mat.get("nil").get("nil"));
		System.out.println(myMod.tr_mat.get("pn").get("pn"));
		//counts are correct
		
		
		
		
		

		/*
		System.out.println(myMod.em_mat.get("nn"));
		System.out.println(myMod.em_mat.get("jj"));
		
		System.out.println(myMod.em_mat.get("nil").get("Reserve"));
		System.out.println("_________");
		System.out.println(myMod.em_mat.get("nn").get("Reserve"));
		
		
		
		for(String preceder : myMod.tr_mat.keySet()){
			System.out.println(preceder);
			System.out.println("nil prob " +  myMod.tr_mat.get(preceder).get("nil"));
			System.out.println("jj prob " +  myMod.tr_mat.get(preceder).get("jj"));
			System.out.println(myMod.tr_mat.get(preceder).keySet().size());
			
		}
		

	
		System.out.println(myMod.tr_mat.get("bed").keySet());
		
		
		/*
		System.out.println(myMod.start_p.keySet());
		System.out.println(myMod.start_p);
		
		
		
		
		//System.out.println(myMod.em_mat.keySet());
		//System.out.println(myMod.em_mat.get("nil"));
		System.out.println(myMod.tr_mat.get("nil"));
		
		
		//System.exit(0);
		System.out.println(myMod.em_mat.get("vbn"));
		
		
		System.out.println(myMod.tr_mat.keySet());
		System.out.println(myMod.tr_mat.get("vbn"));
		
		System.out.println("Trainging finished");
		
		*/
		
		
		
		
		sentences = sentences_from_path(input_folder);
		List<String> subset = new ArrayList<>();
		subset.add(sentences.get(0));
		
		
		
		
		myMod.annotate(subset);

		
		
		System.exit(0);
		
		//HMM myHmm = new HMM("myHmm");
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

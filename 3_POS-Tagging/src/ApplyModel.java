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

public class ApplyModel {

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

		
		HMM myHmm = new HMM("awesome");
		System.out.println(myHmm.name);
		myHmm.train(sentences);
		System.out.println(myHmm.emission_mat);
		System.out.println(myHmm.transition_mat);
	
		
	}
		

	
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}

}

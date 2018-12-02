import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HMM {
	
	String name;
	public HashMap<String, Double> start_prob;
	public HashMap<String, HashMap<String, Double>> emission_mat;
	public HashMap<String, Double> word_counts;
	
	public HashMap<String, HashMap<String,Double>> transition_mat;
	public HashMap<String, Double> tag_transition_prob;
	public HashMap<String, Double> follower_counts;
	
	public Set<String> known_words; 
	
	public HashMap<String, HashMap<String,Double>> LLH_mat;

	
	public HMM(String name){
		
		this.name = name;
		this.start_prob = new HashMap<String, Double>();
		this.emission_mat = new HashMap<String, HashMap<String, Double>>();
		this.word_counts = new HashMap<String, Double>();
		
		this.transition_mat = new HashMap<String, HashMap<String,Double>>();
		this.tag_transition_prob = new HashMap<String, Double>();
		this.follower_counts = new HashMap<String, Double>();
		this.LLH_mat = new HashMap<String, HashMap<String,Double>>();
		this.known_words = new HashSet<String>();
		
		
	}
	
	
	public void get_start_prob(List<String> sentences) {
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			//get start probabilities
			String start_tag = pairs[0].split("/")[1];		
			
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
		}
		
	}
	
	public void get_emission(List<String> sentences) {
		
		int i = 1;
		//count emissions
		for(String sentence : sentences) {
			//System.out.println("Sentence " + i + "/" + sentences.size());
			i = i + 1;
			
			String[] pairs = sentence.split("\\s++");
			
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
				//update emission matrix for every word within every tag
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
		
		}
		known_words = word_counts.keySet();
		
		for(String tag: emission_mat.keySet()) {
			if(start_prob.get(tag) == null) {
				start_prob.put(tag, (double) 0);
			}
		}
		
		
		//Count unseen words for smoothing
		HashMap<String, Double> unseenWords = new HashMap<String, Double>();
		double lambda = 0.5;

		for(String word : word_counts.keySet()) {
		  for(String tag : emission_mat.keySet()) {
		    if(emission_mat.get(tag).containsKey(word)) {
		    	
		      //add lambda to tag-word count 
		      HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
		      
		      double val = emission_mat.get(tag).get(word) + lambda;
		      
		      tag_emission_prob.put(word, val);
		      
		      emission_mat.put(tag, tag_emission_prob);	
		      
		    } else {
		      //add word to tag_emmission_prob, add unseen count
		      HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
		      //add unseen word probability
		      tag_emission_prob.put(word, lambda);
		      //update tag emission matrix
		      emission_mat.put(tag, tag_emission_prob);
		      //add count for unseen words
		      if(unseenWords.containsKey(word)) {
		        double val = unseenWords.get(word) + (double) 1.0;
		        unseenWords.put(word, val);
		      } else {
		    	  unseenWords.put(word, (double) 1);
		      }
		    }
		  }
		}
		
		double unseen_count; 
		
		//normalize emission matrix
		for(String word : word_counts.keySet()) {
			for(String tag : emission_mat.keySet()) {
				if(emission_mat.get(tag).get(word) != null) {
					//get tag emission matrix
					HashMap<String, Double> tag_emission_prob  = emission_mat.get(tag);
					if(unseenWords.get(word)== null){
						unseen_count = 0;
					} else {
						unseen_count = unseenWords.get(word);
					}
					
					//get value of word
					double val = emission_mat.get(tag).get(word) / (word_counts.get(word) + lambda * unseen_count) ;
					//update value
					tag_emission_prob.put(word, val);
					//update tag emission matrix
					emission_mat.put(tag, tag_emission_prob);	
				}
			}
		}
			
	}
	
	
	
	
	public void get_transition(List<String> sentences ){		
		
		//get transition matrix
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			//get start probabilities
			String preceder = pairs[0].split("/")[1];
			
			for(int i=1;i <pairs.length; i++) {
				String follower = pairs[i].split("/")[1];
				
				//update counts
				if(follower_counts.containsKey(follower)) {
					double count = follower_counts.get(follower) + (double) 1;
					follower_counts.put(follower, count);					
				} else {
					follower_counts.put(follower, (double) 1);
				}
				
				if(transition_mat.containsKey(preceder)) {
					if(transition_mat.get(preceder).containsKey(follower)) {
						//get transition row
						tag_transition_prob = transition_mat.get(preceder);
						//update value for given follower
						double val = tag_transition_prob.get(follower) + (double) 1;
						tag_transition_prob.put(follower, val);
						transition_mat.put(preceder, tag_transition_prob);
					} else {
						tag_transition_prob = transition_mat.get(preceder);
						tag_transition_prob.put(follower, (double) 1);
						transition_mat.put(preceder, tag_transition_prob);
					}					
				} else {
					tag_transition_prob = new HashMap<String, Double>();
					tag_transition_prob.put(follower, (double) 1);
					transition_mat.put(preceder, tag_transition_prob);
				}
				//update preceder
				preceder = follower;
			}
			
			
		}
		
		
		//count unseen transitions
		HashMap<String, Double> unseen_transitions = new HashMap<String, Double>();
		double lambda = 0.5;
		double unseen_count = 0;
		
		for(String preceder : transition_mat.keySet()) {
			for(String follower : transition_mat.keySet()) {
				tag_transition_prob = transition_mat.get(preceder);
				if(tag_transition_prob.containsKey(follower)) {
					double val = tag_transition_prob.get(follower) + lambda;
					tag_transition_prob.put(follower, val);
					transition_mat.put(preceder, tag_transition_prob);
				} else {
					tag_transition_prob.put(follower, lambda);
					transition_mat.put(preceder, tag_transition_prob);
					
					if(unseen_transitions.containsKey(follower)) {
					unseen_count  = unseen_transitions.get(follower) + (double) 1.0;
					unseen_transitions.put(follower, unseen_count);
					} else {
						unseen_transitions.put(follower, (double) 1.0);
					}					
					
				}				
			}
		}
		
		//normalize transmisson matrix
		for(String follower : follower_counts.keySet()) {
			for(String preceder : transition_mat.keySet()) {
				if(transition_mat.get(preceder).get(follower) != null) {
					//get preceder transition matrix
					tag_transition_prob  = transition_mat.get(preceder);
					if(unseen_transitions.get(follower)== null){
						unseen_count = 0;
					} else {
						unseen_count = unseen_transitions.get(follower);
					}
					
					
					//get value of word
					double val = tag_transition_prob.get(follower) / (follower_counts.get(follower) + lambda * unseen_count) ;
					//update value
					tag_transition_prob.put(follower, val);
					//update tag transition matrix
					transition_mat.put(preceder, tag_transition_prob);	
				}
			}
		}	
		
	}

	public void save_matrix(HashMap<String, HashMap<String,Double>>  matrix, String filename) {
		try (ObjectOutput objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename, false)))) {
		    objectOutputStream.writeObject(matrix);
		} catch (Throwable cause) {
		    cause.printStackTrace();
		}
		System.out.println(filename + " has been saved.");
	}
	public void save_start_prob(HashMap<String,Double>  matrix, String filename) {
		try (ObjectOutput objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename, false)))) {
		    objectOutputStream.writeObject(matrix);
		} catch (Throwable cause) {
		    cause.printStackTrace();
		}
		System.out.println(filename + " has been saved.");
	}
	
	public HashMap<String, HashMap<String,Double>> load_matrix(String filename){
		System.out.println("loading " + filename);
		HashMap<String, HashMap<String, Double>> outerMap;
		try (ObjectInput objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
		    outerMap = (HashMap<String, HashMap<String, Double>>) objectInputStream.readObject();
		} catch (Throwable cause) {
		    cause.printStackTrace();
		    outerMap =  new HashMap<String, HashMap<String, Double>>();
		}
		return(outerMap);

	}
	public HashMap<String,Double> load_start_prob(String filename){
		System.out.println("loading " + filename);
		HashMap<String, Double> outerMap;
		try (ObjectInput objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
		    outerMap = (HashMap<String, Double>) objectInputStream.readObject();
		} catch (Throwable cause) {
		    cause.printStackTrace();
		    outerMap =  new HashMap<String, Double>();
		}
		return(outerMap);

	}
	
	public void annotate(List<String> sentences) {
		
		HashMap<String, Double> word_state_prob = new HashMap<String, Double>();
		
		
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			List<String> words = new ArrayList<>();
			
			for(String pair:pairs) {
				words.add(pair.split("/")[0]);				
			}
			System.out.println(words);
			System.out.println("______");
			
			//initialize frist word
			String first_word = words.get(0);
			for(String state : emission_mat.keySet()){
				double start_p = start_prob.get(state);
				double em_prob = emission_mat.get(state).get(words);
				start_p = Math.log(start_p) + Math.log(em_prob);
				word_state_prob.put(state, start_p);
				LLH_mat.put(first_word, word_state_prob);				
			}
			
			
		
	}
}


	
	
	


}

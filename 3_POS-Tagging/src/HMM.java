import java.util.HashMap;
import java.util.List;

public class HMM {
	
	String name;
	public HashMap<String, Double> start_prob;
	public HashMap<String, HashMap<String, Double>> emission_mat;
	public HashMap<String, Double> word_counts;
	
	public HashMap<String, HashMap<String,Double>> transition_mat;
	public HashMap<String, Double> tag_transition_prob;
	public HashMap<String, Double> follower_counts;

	
	public HMM(String name){
		
		this.name = name;
		this.start_prob = new HashMap<String, Double>();
		this.emission_mat = new HashMap<String, HashMap<String, Double>>();
		this.word_counts = new HashMap<String, Double>();
		
		this.transition_mat = new HashMap<String, HashMap<String,Double>>();
		this.tag_transition_prob = new HashMap<String, Double>();
		this.follower_counts = new HashMap<String, Double>();
		
		
	}
	
	public void train(List<String> sentences ){
		
		//get start_probs and emmision matrix
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
			
			// train emission matrix
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
		
		//normalize emission matrix
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
		
		
		//get transition matrix
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			//List<String> pairs = Arrays.asList(sentence.split(" "));
			
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
		
		//normalize transmisson matrix
		for(String follower : follower_counts.keySet()) {
			for(String preceder : transition_mat.keySet()) {
				if(transition_mat.get(preceder).get(follower) != null) {
					//get preceder transition matrix
					tag_transition_prob  = transition_mat.get(preceder);
					//get value of word
					double val = tag_transition_prob.get(follower) / follower_counts.get(follower);
					//update value
					tag_transition_prob.put(follower, val);
					//update tag transition matrix
					transition_mat.put(preceder, tag_transition_prob);	
				}
			}
		}	
		
	}

	
	

	
	
	


}

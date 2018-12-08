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
	public Set<String> known_tags; 
	
	double lambda = 0.0; //smoothening constant
	
	public HMM(String name){
		
		this.name = name;
		this.start_prob = new HashMap<String, Double>();
		this.emission_mat = new HashMap<String, HashMap<String, Double>>();
		this.word_counts = new HashMap<String, Double>();
		
		this.transition_mat = new HashMap<String, HashMap<String,Double>>();
		this.tag_transition_prob = new HashMap<String, Double>();
		this.follower_counts = new HashMap<String, Double>();
		//this.LLH_mat = new HashMap<Integer, HashMap<String,Double>>();
		this.known_words = new HashSet<String>();
		this.known_tags = new HashSet<String>();
		
	}
	
	
	public void get_start_prob(List<String> sentences) {
		
		double val;
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			int idx = pairs[0].lastIndexOf("/");
			String start_tag = pairs[0].substring(idx + 1);

			
			if(start_prob.containsKey(start_tag)) {				
				val = start_prob.get(start_tag)+ (double) 1.0;
				start_prob.put(start_tag, val);				
			}else {				
				start_prob.put(start_tag, (double) 1);				
			}
			
			//smoothing start_prob
			double unseen_counter = 0 ;
			
			for(String tag : known_tags) {
				if(start_prob.containsKey(tag)) {				
					val = start_prob.get(tag)+ lambda;
					start_prob.put(tag, val);				
				}else {				
					start_prob.put(tag, lambda);
					unseen_counter ++;
				}
			}
			
			//calculate probability by dividing through sentence sum
			for(String key:start_prob.keySet() ) {
				val = start_prob.get(key) / ((double) sentences.size() + lambda * unseen_counter);
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

				
				int idx = pair.lastIndexOf("/");
				
				String word = pair.substring(0,idx);
				String tag = pair.substring(idx + 1);
				
				
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
		known_tags = emission_mat.keySet();
		
		for(String tag: known_tags) {
			if(start_prob.get(tag) == null) {
				start_prob.put(tag, (double) 0);
			}
		}
		
		
		//Count unseen words for smoothing
		HashMap<String, Double> unseenWords = new HashMap<String, Double>();
		//double lambda = 0.5;

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
		/*
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
		*/
			
	}
	
	
	
	
	public void get_transition(List<String> sentences ){		
		
		//get transition matrix
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			
			//String preceder = pairs[0].split("/")[1];
			int idx = pairs[0].lastIndexOf("/");
			String preceder = pairs[0].substring(idx + 1);
			
			
			for(int i=1;i <pairs.length; i++) {
				idx = pairs[0].lastIndexOf("/");
				String follower = pairs[0].substring(idx + 1);
				
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
		//double lambda = 0.5;
		double unseen_count = 0;
		
		for(String preceder : known_tags) {
			for(String follower : known_tags) {
				if(transition_mat.containsKey(preceder)) {
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
					//case preceder not yet in transition_mat add new hashmap
				} else {
					tag_transition_prob.clear();
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
		
		//keeps track of the likelihood of the sequence at each Position for each state
		HashMap<Integer, HashMap<String,Double>> LLH_mat = new HashMap<Integer, HashMap<String,Double>>();
		//keeps track of the mml preceder for eacht position and each state
		HashMap<Integer, HashMap<String,String>> state_seq_mat = new HashMap<Integer, HashMap<String,String>>();
		
		HashMap<String, Double> pos_state_prob = new HashMap<String, Double>();
		HashMap<String, Double> former_pos_prob = new HashMap<String, Double>();
		
		HashMap<String, String> state_preceder = new HashMap<String, String>();
		
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			List<String> words = new ArrayList<>();
			List<String> True_states = new ArrayList<>();
			List<String> reversed_states = new ArrayList<>();
			
			List<String> maxLL_states = new ArrayList<>();
			
			for(String pair:pairs) {
				words.add(pair.split("/")[0]);
				True_states.add(pair.split("/")[1]);
			}
			
			System.out.println("Words:");
			System.out.println(words);
			System.out.println("True States:");
			System.out.println(True_states);
			
			
			
			System.out.println("______");
			System.out.println(known_words.contains("defendants"));
			//System.out.println(known_words);
			
			double em_prob;
			double trans_prob;
			double maxLL = 0;
			String maxLL_state;
			
			
			//initialize frist word
			String first_word = words.get(0);
			int pos_zero = 0;
			
			for(String state : emission_mat.keySet()){
				//System.out.println("State: " + state);
				double start_p = start_prob.get(state);
				//System.out.println("start_p:" + start_p);
				start_p = Math.log(start_p);
				
				if(known_words.contains(first_word)) {
					em_prob = emission_mat.get(state).get(first_word);
				} else { //default prob for unknown words
					em_prob = (double) 1 / (double) emission_mat.size();
				}
				em_prob = Math.log(em_prob);
				
				//System.out.println("start_p:" + start_p + " em_p: " + em_prob );
				
				start_p = start_p + em_prob;				
				pos_state_prob.put(state, start_p);
				LLH_mat.put(pos_zero, pos_state_prob);
			}
			former_pos_prob = pos_state_prob;
			
			System.out.println(former_pos_prob);
			System.out.println(LLH_mat.get(1-1)); //both correct
			//fill up table by word position
			for(int pos =1; pos < 2; pos++) {
				//add column for every position in sentence
				former_pos_prob = LLH_mat.get(pos-1);
				System.out.println(LLH_mat.get(pos-1).keySet()); //correct
				//pos_state_prob.clear(); 
				double prev_p;
				double p; 

				
				//get total prob for every tag at pos
				for(String state : emission_mat.keySet()){
					// get em prob of given state for word at pos
					if(known_words.contains(words.get(pos))) {
						em_prob = emission_mat.get(state).get(words.get(pos));
					} else { //default prob for unknown words
						em_prob = (double) 1 / (double) emission_mat.size();
					}
					em_prob = Math.log(em_prob);
					maxLL_state = "Overwrite";
					maxLL = Double.NEGATIVE_INFINITY ;

					former_pos_prob = LLH_mat.get(pos-1);
					System.out.println(LLH_mat.get(pos-1).keySet()); //empty// clears this
					//get mLL from all preceders
					for(String prev_state : former_pos_prob.keySet()) {
						
						prev_p = former_pos_prob.get(prev_state);
						//System.out.println("Prev log prob: "+ prev_p);

						System.out.println("Prevstate: "+prev_state + " State: " + state );
						trans_prob = transition_mat.get(prev_state).get(state);
						trans_prob = Math.log(trans_prob);
						//System.out.println("Trans log prob: "+ trans_prob);
						p = prev_p + trans_prob + em_prob;
						//System.out.println("probs: "+ prev_p + "/" + trans_prob +  "/" + em_prob + "/" +  p);
						if(pos == 2) {
							System.exit(0);
						}
						
						
						if(p >= maxLL) {
							maxLL = p;
							maxLL_state = prev_state;
						}

					}

					pos_state_prob.put(state, maxLL);
					state_preceder.put(state, maxLL_state);
				}
				LLH_mat.put(pos, pos_state_prob);
				state_seq_mat.put(pos, state_preceder);

			}
			for(int pos =words.size()-1; pos > 0; pos--) {
				System.out.println(pos + "/" + words.size());
				System.out.println(state_seq_mat.get(pos));
				String mLLstate = "";				
				double old_p = Double.NEGATIVE_INFINITY;

				for(String state:LLH_mat.get(pos).keySet()) {
					double p = LLH_mat.get(pos).get(state);
					if(p >= old_p) {
						old_p = p;
						mLLstate = state;
					}
				}
				
				reversed_states.add(mLLstate);
				
			}
			System.out.println("reversed states: ");
			System.out.println(reversed_states);
		}
	}
		
}


	
	
	




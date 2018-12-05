import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Model {
	
	double lambda = 0.5; //smoothening constant
	String name;
	public Set<String> known_words; 
	public Set<String> known_tags;
	public ArrayList<String> start_tags;
	
	public HashMap<String, Double> start_p;
	public HashMap<String, HashMap<String, Double>> em_mat;
	public HashMap<String, HashMap<String, Double>> tr_mat;
	public HashMap<Integer, HashMap<String, Double>> LLH_mat;
	public HashMap<Integer, HashMap<String, String>> LLH_preceder;
	
	public Model(String name) {
		this.name = name;
		this.known_words = new HashSet<String>();
		this.known_tags = new HashSet<String>();
		this.start_tags = new ArrayList<String>();
		
		this.start_p = new HashMap<String, Double>();
		this.em_mat = new HashMap<String, HashMap<String, Double>>();
		this.tr_mat = new HashMap<String, HashMap<String, Double>>();
		this.LLH_mat = new HashMap<Integer, HashMap<String, Double>>();
		this.LLH_preceder = new HashMap<Integer, HashMap<String, String>>();
		
	}
	
	public void get_knowns(List<String> sentences) {
		for(String sentence: sentences) {
			String[] pairs = sentence.split("\\s++");
			int i = 0;
			for(String pair:pairs) {	
				int idx = pair.lastIndexOf("/");				
				String word = pair.substring(0,idx);
				String tag = pair.substring(idx + 1);
			
				if(i == 0) {
					start_tags.add(tag);
				}
				
				known_words.add(word);
				known_tags.add(tag);
				i ++;
			}
		}
	}
	
	public void get_start_p(List<String> sentences) {
		//initialize start_p
		double tag_count;
		
		for(String tag: known_tags) {
			start_p.put(tag, lambda);
		}
		double B = (double) known_tags.size();
		double N = (double) sentences.size();
		double p;

		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			String pair = pairs[0];
			int idx = pair.lastIndexOf("/");			
			String start_tag = pair.substring(idx + 1);
			
			tag_count = start_p.get(start_tag);
			tag_count ++;
			start_p.put(start_tag, tag_count);			
		}
		//normalize
		for(String tag: known_tags) {
			tag_count = start_p.get(tag);
			p = tag_count/ (N + B * lambda);
			p = Math.log(p);
			start_p.put(tag, p);
		}		
	}
	
	public void get_emission(List<String> sentences) {
		
		
		//initialize em_mat with lambda values
		for(String tag : known_tags) {
			HashMap<String, Double> word_prob = new HashMap<String, Double>();
			double unknown_prob = 1/ (double) known_tags.size();
			word_prob.put("un--known-ident", lambda);
			em_mat.put(tag, word_prob);
			for(String word : known_words) {
				//word_prob.clear();
				word_prob.put(word, lambda);
			}
		}
		
		//count words per tag
		HashMap<String, Double> tag_mat = new HashMap<String, Double>();
		HashMap<String, Double> word_counts = new HashMap<String, Double>();
		for(String word: known_words) {
			word_counts.put(word, (double)0 ); 
		}
		double tag_word_count;
		double word_count;
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			int i = 0;
			for(String pair:pairs) {	
				int idx = pair.lastIndexOf("/");				
				String word = pair.substring(0,idx);
				String tag = pair.substring(idx + 1);
				
				tag_mat = em_mat.get(tag);
				tag_word_count = tag_mat.get(word);
				tag_word_count ++;
				tag_mat.put(tag, tag_word_count);
				
				word_count = word_counts.get(word);
				word_count ++;
				word_counts.put(word, word_count);
			}
			
		}
		//normalize
		double N = 0.0;
		double B = (double) known_tags.size();;
		double p = 0.0;
		
		for(String tag: known_tags) {
			tag_mat = em_mat.get(tag);
			for(String word : known_words) {
				tag_word_count = tag_mat.get(word);
				N = word_counts.get(word);
				p = tag_word_count / (N + B * lambda);
				p = Math.log(p);
				tag_mat.put(word, p);
			}
			em_mat.put(tag, tag_mat);
		}		
	}
	
	public void get_transition(List<String> sentences) {
		
		//initialize etr_mat with lambda values
		for(String preceder: known_tags) {
			HashMap<String, Double> preceder_mat = new HashMap<String, Double>();
			for(String follower: known_tags) {
				preceder_mat.put(follower, lambda);
			}
			tr_mat.put(preceder, preceder_mat);
		}
		
		
		//count transitions
		HashMap<String, Double> preceder_mat = new HashMap<String, Double>();
		HashMap<String, Double> follower_counts = new HashMap<String, Double>();
		for(String follower: known_tags) {
			follower_counts.put(follower, (double) 0.0);
		}
		
		
		
		double follower_count;
		double preceder_follower_count;
		
		
		int idx;
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			idx = pairs[0].lastIndexOf("/");
			String preceder = pairs[0].substring(idx + 1);
			String follower;

			for(int i=1;i <pairs.length; i++) {
				
				if(!known_tags.contains(preceder)) {
					System.out.println(preceder + "unknown, abort!");
					System.exit(0);
				}
				
				idx = pairs[i].lastIndexOf("/");				
				follower = pairs[i].substring(idx + 1);
				
				preceder_mat = tr_mat.get(preceder);
				preceder_follower_count = preceder_mat.get(follower);
				preceder_follower_count ++;
				preceder_mat.put(follower, preceder_follower_count);
				tr_mat.put(preceder, preceder_mat);
				
				follower_count = follower_counts.get(follower);
				follower_count ++;
				follower_counts.put(follower, follower_count);
				
				preceder = follower;
			}
		}
		//System.out.println("Follower_counts: ");
		//System.out.println(follower_counts);
		//normalize
		double N = 0.0;
		double B = (double) known_tags.size();
		double p = 0.0;
		
		for(String preceder: known_tags) {
			preceder_mat = tr_mat.get(preceder);
			
			for(String follower : known_tags) {
				N = follower_counts.get(follower);
				preceder_follower_count = preceder_mat.get(follower);
				p = preceder_follower_count / (N + B * lambda);
				p = Math.log(p);
				preceder_mat.put(follower, p);				
			}
			tr_mat.put(preceder, preceder_mat);
		}
		
		
	}

	public void annotate(List<String> sentences) {
		HashMap<String, Double> pos_state_prob = new HashMap<String, Double>();
		HashMap<String, Double> state_MLL = new HashMap<String, Double>();
		HashMap<String, String> MLL_state_preceder = new HashMap<String, String>();
		
		List<String> True_states = new ArrayList<>();
		List<String> words = new ArrayList<>();
		double p0;
		double p;
		double em_p;
		double p_prev;
		double p_tr;
		//int pos;
		int pos_prev;
		double maxLL;
		String mLL_preceder;
		String word;
		
		for(String sentence : sentences) {
			String[] pairs = sentence.split("\\s++");
			
			for(String pair:pairs) {
				words.add(pair.split("/")[0]);
				True_states.add(pair.split("/")[1]);
			}
			String first_word = words.get(0);
			//initialize first position
			for(String state: known_tags) {
				p0 = start_p.get(state);
				em_p = em_mat.get(state).get(first_word);
				p = p0 + em_p;
				pos_state_prob.put(state, p);
			}
			LLH_mat.put(0, pos_state_prob);
			System.out.println(LLH_mat.get(0));
			
			//pos = 1;
			for(int pos =1; pos < words.size(); pos++) {
				word = words.get(pos);
				
				for(String state : known_tags) {
					//for each state get maximum LLH(from previous states LLHs and transition prob)
					maxLL = Double.NEGATIVE_INFINITY;
					mLL_preceder = "---";
					
					for(String prev_state : known_tags) {
						//get max LLH from all previous states
						pos_prev = pos - 1;
						p_prev = LLH_mat.get(pos_prev).get(prev_state);
						p_tr = tr_mat.get(prev_state).get(state);
						p = p_prev + p_tr;
						if(p >= maxLL) {
							maxLL = p;
							mLL_preceder = prev_state;
						}					
					}
					//System.out.println(maxLL);
					//add MLL of the current state to the temp map of state MLLs
					maxLL = maxLL + em_mat.get(state).get(word);
					state_MLL.put(state,maxLL);	
					MLL_state_preceder.put(state, mLL_preceder);
				}
				//add all state MLLs at the given pos to the LLH mat.
				LLH_mat.put(pos, state_MLL);
				LLH_preceder.put(pos, MLL_state_preceder);
				//System.out.print(LLH_mat.get(pos));
				System.out.println("Position:" + pos);
				//System.out.print(LLH_preceder.get(pos));
			}
			
			/*
			String expected_last_state = "--";
			int lastpos = (int) words.size() - 1;
			maxLL = Double.NEGATIVE_INFINITY;
			
			for(String state : LLH_mat.get(lastpos).keySet()) {
				p = LLH_mat.get(lastpos).get(state);
				if(p > maxLL) {
					maxLL = p;
					expected_last_state = state;
				}
			}
			
			System.out.println("-----------");
			for(int pos = words.size()-1; pos > 0; pos--) {
				System.out.print(pos);
				System.out.println(expected_last_state);
				expected_last_state = LLH_preceder.get(pos).get(expected_last_state);
				
			}
			*/
			
		}
		
		
	}


}

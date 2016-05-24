package no.ntnu.item.smash.sim.structure;

import java.util.HashMap;

public class FeedbackList {

	private HashMap<String,Feedback> feedbacks = new HashMap<String,Feedback>();
	
	public Feedback getFeedback(String id) {
		return feedbacks.get(id);
	}
	
	public Feedback getAndRemoveFeedback(String id) {
		return feedbacks.remove(id);
	}
	
	public void removeFeedback(String id) {
		feedbacks.remove(id);
	}
	
	public HashMap<String,Feedback> getFeedbacks() {
		return feedbacks;
	}
	
	public void addFeedback(String id, HashMap<String, Object> feedback) {
		feedbacks.put(id, new Feedback(feedback));
	}
	
	public class Feedback {
		private HashMap<String, Object> feedback = new HashMap<String, Object>();
		
		public Feedback(HashMap<String,Object> feedback) {
			this.feedback = feedback;
		}
		
		public HashMap<String,Object> getFeedback() {
			return feedback;
		}
		
		public Object getSpecificFeedback(String type) {
			return feedback.get(type);
		}
	}
	
}

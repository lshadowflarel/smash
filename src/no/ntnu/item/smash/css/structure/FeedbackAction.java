package no.ntnu.item.smash.css.structure;

import java.util.HashMap;

import no.ntnu.item.smash.css.core.SystemContext;

public class FeedbackAction extends DecidedAction {

	private String id;
	private HashMap<String, Object> data = new HashMap<String, Object>();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void addFeedback(String type, Object value) {
		data.put(type, value);
	}
	
	//TODO only placeholder here - do it properly later
	@Override
	public void execute(SystemContext context) {
		context.getHouse().getFeedbacks().addFeedback(id, data);
		context.getHouse().taskDoneNotification(2);
	}

}

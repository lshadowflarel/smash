package no.ntnu.item.smash.css.structure;

import java.util.HashMap;

import no.ntnu.item.smash.css.core.SystemContext;

public class TriggerPolicyAction extends DecidedAction {
	private int triggerType;
	private HashMap<String, Object> data = new HashMap<String, Object>();
	
	public TriggerPolicyAction() {}
	
	public void setTriggerType(int type) {
		triggerType = type;
	}
	
	public void setData(HashMap<String,Object> data) {
		this.data = data;
	}
	
	public int getTriggerType() {
		return triggerType;
	}
	
	public HashMap<String,Object> getData() {
		return data;
	}
	
	@Override
	public void execute(SystemContext context) { 
		data.put("force" , true);
		context.getBsc().getNotified(data, triggerType);
	}
	
	
}

package no.ntnu.item.smash.css.structure;

public class ExternalEventTrigger extends Trigger{
	
	public ExternalEventTrigger(int activeTriggerType, int conditionOP, Object conditionValue) {
		super(activeTriggerType, conditionOP, conditionValue);
	}

	public int[] getTriggerType() {
		return new int[]{Trigger.TYPE_EXTERNAL_EVENT};
	}
	
	public boolean conditionFulfilled(Object compareWith) {
		String val = (String)conditionValue;
		String compare = (String)compareWith;
		
		switch(conditionOP) {
		case Trigger.COND_EQUALTO:
			if(compare.equals(val)) return true;
			break;
		case Trigger.COND_NOTEQUALTO:
			if(!compare.equals(val)) return true;
			break;
		default:
			break;
		}
		
		return false;
	}
}

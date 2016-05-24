package no.ntnu.item.smash.css.structure;

public class DeviceStartTrigger extends Trigger {

	public DeviceStartTrigger(int activeTriggerType, int conditionOP,
			Object conditionValue) {
		super(activeTriggerType, conditionOP, conditionValue);
	}
	
	public int[] getTriggerType() {
		return new int[]{Trigger.TYPE_DEVICE_START};
	}
	
	public boolean conditionFulfilled(Object compareWith) {
		int val = (int)conditionValue;
		int compare = compareWith instanceof String?Integer.parseInt((String)compareWith):(int)compareWith;
		
		switch(conditionOP) {
		case Trigger.COND_EQUALTO:
			if(compare==val) return true;
			break;
		default:
			break;
		}
		
		return false;
	}

}

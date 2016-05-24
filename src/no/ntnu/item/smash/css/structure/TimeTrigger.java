package no.ntnu.item.smash.css.structure;

public class TimeTrigger extends Trigger{

	public TimeTrigger(int activeTriggerType, int conditionOP, Object conditionValue) {
		super(activeTriggerType, conditionOP, conditionValue);
	}

	public int[] getTriggerType() {
		return new int[]{Trigger.TYPE_TIME};
	}
	
	public boolean conditionFulfilled(Object compareWith) {
		int val = (int)conditionValue;
		int compare = (int)compareWith;
		
		switch(conditionOP) {
		case Trigger.COND_EQUALTO:
			if(compare==val) return true;
			break;
		case Trigger.COND_GREATERTHAN:
			if(compare>val) return true;
			break;
		case Trigger.COND_GREATERTHANEQ:
			if(compare>=val) return true;
			break;
		case Trigger.COND_LESSTHAN:
			if(compare<val) return true;
			break;
		case Trigger.COND_LESSTHANEQ:
			if(compare<=val) return true;
			break;
		case Trigger.COND_NOTEQUALTO:
			if(compare!=val) return true;
			break;
		default:
			break;
		}
		
		return false;
	}
}

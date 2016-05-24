package no.ntnu.item.smash.css.structure;

public abstract class Trigger {

	public static final int TYPE_DEF = -1;
	public static final int TYPE_PRICE_ESP = 0;
	public static final int TYPE_PRICE_DSO = 1;
	public static final int TYPE_PRICE = 2;
	public static final int TYPE_TIME = 3;
	public static final int TYPE_EXTERNAL_EVENT = 4;
	public static final int TYPE_DEVICE_START = 5;
	public static final int TYPE_PRICE_ESP_NEXTINT = 6;
	public static final int TYPE_PRICE_DSO_NEXTINT = 7;
	public static final int TYPE_PRICE_NEXTINT = 8;
	public static final int TYPE_POWERLIMIT = 9;
	
	public static final int COND_GREATERTHAN = 0;
	public static final int COND_GREATERTHANEQ = 1;
	public static final int COND_LESSTHAN = 2;
	public static final int COND_LESSTHANEQ = 3;
	public static final int COND_EQUALTO = 4;
	public static final int COND_NOTEQUALTO = 5;
	
	protected int activeTriggerType;
	protected int conditionOP;
	protected Object conditionValue;
	
	public Trigger(int activeTriggerType, int conditionOP, Object conditionValue) {
		setActiveTriggerType(activeTriggerType);
		setCondition(conditionOP, conditionValue);
	}
	
	public int[] getSupportedTriggerType() {
		return null;
	}
	
	public int getActiveTriggerType() {
		return activeTriggerType;
	}
	
	public void setActiveTriggerType(int activeTriggerType) {
		this.activeTriggerType = activeTriggerType;
	}
	
	public void setCondition(int conditionOP, Object conditionValue) {
		this.conditionOP = conditionOP;
		this.conditionValue = conditionValue;
	}
	
	public boolean conditionFulfilled(Object compareWith) {
		return false;
	}
	
	public int getConditionOP() {
		return conditionOP;
	}
	
	public void setConditionOP(int conditionOP) {
		this.conditionOP = conditionOP;
	}
	
	public Object getConditionValue() {
		return conditionValue;
	}
	
	public void setConditionValue(Object conditionValue) {
		this.conditionValue = conditionValue;
	}
}

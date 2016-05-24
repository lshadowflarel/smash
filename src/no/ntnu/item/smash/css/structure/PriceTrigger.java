package no.ntnu.item.smash.css.structure;

public class PriceTrigger extends Trigger {

	public PriceTrigger(int activeTriggerType, int conditionOP,
			Object conditionValue) {
		super(activeTriggerType, conditionOP, conditionValue);
	}

	public int[] getSupportedTriggerType() {
		return new int[] { Trigger.TYPE_PRICE_ESP, Trigger.TYPE_PRICE_DSO,
				Trigger.TYPE_PRICE, Trigger.TYPE_PRICE_ESP_NEXTINT,
				Trigger.TYPE_PRICE_DSO_NEXTINT, Trigger.TYPE_PRICE_DSO_NEXTINT };
	}

	public boolean conditionFulfilled(Object compareWith) {
		double val = (double) conditionValue;
		double compare = (double) compareWith;

		switch (conditionOP) {
		case Trigger.COND_EQUALTO:
			if (compare == val)
				return true;
			break;
		case Trigger.COND_GREATERTHAN:
			if (compare > val)
				return true;
			break;
		case Trigger.COND_GREATERTHANEQ:
			if (compare >= val)
				return true;
			break;
		case Trigger.COND_LESSTHAN:
			if (compare < val)
				return true;
			break;
		case Trigger.COND_LESSTHANEQ:
			if (compare <= val)
				return true;
			break;
		case Trigger.COND_NOTEQUALTO:
			if (compare != val)
				return true;
			break;
		default:
			break;
		}

		return false;
	}
}

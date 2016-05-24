package no.ntnu.item.smash.css.structure;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.css.core.MONMachine;
import no.ntnu.item.smash.css.role.Subscriber;

public class SubscriptionList {

	private HashMap<Integer, ArrayList<Subscriber>> list; // data type - subscribers
	
	public SubscriptionList() {
		list = new HashMap<Integer, ArrayList<Subscriber>>();
		list.put(MONMachine.SUBSCRIBE_DATA_ESP_PRICE, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_DSO_PRICE, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_PRICE, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_TIME, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_ESP_PRICE_NEXTINT, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_DSO_PRICE_NEXTINT, new ArrayList<Subscriber>());
		list.put(MONMachine.SUBSCRIBE_DATA_PRICE_NEXTINT, new ArrayList<Subscriber>());
	}
	
	public void addSubscription(Subscriber subscriber, int dataType) { 
		ArrayList<Subscriber> subscribers = list.get(dataType);
		
		subscribers.add(subscriber);
		list.put(dataType, subscribers);
	}
	
	public ArrayList<Subscriber> getSubscribers(int dataType) {
		return list.get(dataType);
	}
}

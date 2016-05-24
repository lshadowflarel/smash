package no.ntnu.item.smash.css.core;

import java.util.ArrayList;

import no.ntnu.item.smash.css.core.MONMachine.Event;
import no.ntnu.item.smash.css.role.Subscriber;
import no.ntnu.item.smash.css.structure.SubscriptionList;

public class SDCMachine {

	private SystemContext context;
	
	protected enum State {
		IDLE {
			void transition(SDCMachine machine, Event event, Object data) {
				switch (event) {
				case SUBSCRIBE:
					machine.currentState = State.PROCESSINGSUBSCRIPTION;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(SDCMachine machine, Object data) {
			}
		},
		PROCESSINGSUBSCRIPTION {
			void transition(SDCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			@SuppressWarnings("unchecked")
			void doAction(SDCMachine machine, Object data) {
				ArrayList<Object> info = (ArrayList<Object>)data;
				Subscriber subscriber = (Subscriber)info.get(0);
				int dataType = (Integer)info.get(1);
				
				if(dataType!=MONMachine.SUBSCRIBE_EVENT_DEVSTART && dataType!=MONMachine.SUBSCRIBE_EVENT_POWERLIMIT) {
					machine.subscriptionList.addSubscription(subscriber, dataType);
					if(!machine.subscriptionTypes.contains(dataType)) machine.subscriptionTypes.add(dataType);
				}

				machine.context.getMon().monitor(subscriber, dataType);
				
				machine.currentState.transition(machine, Event.WAIT, null);
			}
		};
		
		abstract void transition(SDCMachine machine, Event event, Object data);

		abstract void doAction(SDCMachine machine, Object data);
	}
	
	protected enum Event {
		WAIT, SUBSCRIBE;
	}
	
	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private SubscriptionList subscriptionList = new SubscriptionList();;
	private ArrayList<Integer> subscriptionTypes = new ArrayList<Integer>();
	
	public SDCMachine(SystemContext context) {
		this.context = context;
	}
	
	public SystemContext getContext() {
		return context;
	}
	
	public void subscribe(Subscriber subscriber, int dataType) {
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(subscriber);
		data.add(dataType);
		
		currentState.transition(this, Event.SUBSCRIBE, data);
	}
	
	public SubscriptionList getSubscriptionList() {
		return subscriptionList;
	}
	
}

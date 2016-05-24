package no.ntnu.item.smash.css.core;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.css.structure.Trigger;

/*
 * Policy/Goal/Constraint EFSM
 * 
 */
public class PGCMachine {

	protected enum State {
		IDLE {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case DEACTIVATE:
					machine.currentState = State.DEACTIVATING;
					break;
				case ACTIVATE:
					machine.currentState = State.ACTIVATING;
					break;
				case REGISTER:
					machine.currentState = State.REGISTERING;
					break;
				case ASSIGNTRIGGER:
					machine.currentState = State.ASSIGNINGTRIGGER;
					break;
				case UPDATE:
					machine.currentState = State.UPDATING;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
			}
		},
		REGISTERING {
			void transition(PGCMachine machine, Event event, Object data) {
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
			void doAction(PGCMachine machine, Object data) {
				// the data for REGISTERING event contains 1) id 2) file path
				ArrayList<Object> d = (ArrayList<Object>)data;
				String id = (String)d.get(0);
				String filePath = (String)d.get(1);
				machine.idFileMap.put(id, filePath);
				machine.opSuccess = true;
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		DEACTIVATING {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				String id = (String)data;
				if(!machine.idFileMap.containsKey(id)) {
					machine.opSuccess = false; // invalid ID - the supplied policy does not exist in the system
				} else {			
					if(!machine.inactivePolicies.contains(id)) {
						machine.inactivePolicies.add(id);
					}
					
					machine.activePolicies.remove(id);
					machine.opSuccess = true;
				}
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		ACTIVATING {
			void transition(PGCMachine machine, Event event, Object data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(PGCMachine machine, Object data) {
				String id = (String)data;
				if(!machine.idFileMap.containsKey(id)) { 
					machine.opSuccess = false; // invalid ID - the supplied policy does not exist in the system
				} else {			
					if(!machine.activePolicies.contains(id)) {
						machine.activePolicies.add(id);
					}
					
					machine.inactivePolicies.remove(id);
					machine.opSuccess = true;
				}
				
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		ASSIGNINGTRIGGER {
			void transition(PGCMachine machine, Event event, Object data) {
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
			void doAction(PGCMachine machine, Object data) { 
				ArrayList<Object> d = (ArrayList<Object>)data;
				String id = (String)d.get(0);
				machine.idTriggerMap.put(id, (Trigger)d.get(1));
				
				// the policy to be activated needs to have been assigned a trigger
				int activeTriggerType = machine.idTriggerMap.get(id).getActiveTriggerType();
				
				int dataType = Trigger.TYPE_DEF;
				switch(activeTriggerType) {
				case Trigger.TYPE_PRICE_ESP:
					dataType = MONMachine.SUBSCRIBE_DATA_ESP_PRICE;
					break;
				case Trigger.TYPE_PRICE_DSO:
					dataType = MONMachine.SUBSCRIBE_DATA_DSO_PRICE;
					break;
				case Trigger.TYPE_PRICE:
					dataType = MONMachine.SUBSCRIBE_DATA_PRICE;
					break;
				case Trigger.TYPE_TIME:
					dataType = MONMachine.SUBSCRIBE_DATA_TIME;
					break;
				case Trigger.TYPE_EXTERNAL_EVENT:
					dataType = MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT;
					break;
				case Trigger.TYPE_DEVICE_START:
					dataType = MONMachine.SUBSCRIBE_EVENT_DEVSTART;
					break;
				case Trigger.TYPE_PRICE_ESP_NEXTINT:
					dataType = MONMachine.SUBSCRIBE_DATA_ESP_PRICE_NEXTINT;
					break;
				case Trigger.TYPE_PRICE_DSO_NEXTINT:
					dataType = MONMachine.SUBSCRIBE_DATA_DSO_PRICE_NEXTINT;
					break;
				case Trigger.TYPE_PRICE_NEXTINT:
					dataType = MONMachine.SUBSCRIBE_DATA_PRICE_NEXTINT;
					break;
				case Trigger.TYPE_POWERLIMIT:
					dataType = MONMachine.SUBSCRIBE_EVENT_POWERLIMIT;
					break;
				default:
					// TODO - error then
					break;
				}
				
				machine.context.getSdc().subscribe(machine.context.getBsc(), dataType);
				
				machine.opSuccess = true;
				machine.currentState.transition(machine, Event.WAIT, data);
			}
		},
		UPDATING {
			void transition(PGCMachine machine, Event event, Object data) {
			}

			void doAction(PGCMachine machine, Object data) {
			}
		};

		abstract void transition(PGCMachine machine, Event event, Object data);

		abstract void doAction(PGCMachine machine, Object data);
	}
	
	protected enum Event {
		DEACTIVATE, ACTIVATE, REGISTER, ASSIGNTRIGGER, UPDATE, WAIT
	}
	
	private SystemContext context;
	
	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private HashMap<String, String> idFileMap = new HashMap<String, String>();
	private HashMap<String, Trigger> idTriggerMap = new HashMap<String, Trigger>();
	private HashMap<String, String> idGoalMap = new HashMap<String, String>();
	private ArrayList<String> activePolicies = new ArrayList<String>();
	private ArrayList<String> inactivePolicies = new ArrayList<String>();
	private boolean opSuccess = true;
	
	public PGCMachine(SystemContext context) {
		this.context = context;
	}
	
	public void registerPolicy(String filePath, String goal, String id) {
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(id);
		data.add(filePath);
		this.currentState.transition(this, Event.REGISTER, data);
		idGoalMap.put(id, goal);
	}
	
	public boolean activatePolicy(String id) {
		this.currentState.transition(this, Event.ACTIVATE, id);
		
		return opSuccess;
	}
	
	public boolean deactivatePolicy(String id) {
		this.currentState.transition(this, Event.DEACTIVATE, id);
		
		return opSuccess;
	}
	
	public boolean registerPolicyTrigger(String id, Trigger trigger) { 
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(id);
		data.add(trigger);
		this.currentState.transition(this, Event.ASSIGNTRIGGER, data);
		
		return opSuccess;
	}
	
	/* TODO: All these will be removed once the proper datastores have been implemented */
	public HashMap<String, String> getIDFileMap() {
		return idFileMap;
	}
	
	public HashMap<String, Trigger> getIDTriggerMap() {
		return idTriggerMap;
	}
	
	public ArrayList<String> getActivePolicies() {
		return activePolicies;
	}
	
	public HashMap<String, String> getIDGoalMap() {
		return idGoalMap;
	}
	
}

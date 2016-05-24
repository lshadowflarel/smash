package no.ntnu.item.smash.css.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import no.ntnu.item.smash.css.role.Subscriber;
import no.ntnu.item.smash.css.structure.ActionSet;
import no.ntnu.item.smash.css.structure.DecidedAction;
import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.externalsp.DSO;

public class BSCMachine extends Observable implements Subscriber, Observer {

	private NxETReasoner rm;
	private SystemContext context;
	
	public BSCMachine(SystemContext context) {
		this.context = context;
		rm = new NxETReasoner(context);
	}
	
	protected enum State {
		IDLE {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case DEACTIVATE:
					machine.currentState = State.SUSPENDED;
					break;
				case STARTPROCESSING:
					machine.currentState = State.PROCESSPOLICY;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) {
			}
		},
		PROCESSPOLICY {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case WAIT:
					machine.currentState = State.IDLE;
					break;
				case DEACTIVATE:
					machine.currentState = State.SUSPENDED;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) { 
				if(!machine.processingQueue.isEmpty()) { 
					String policyToProcess = machine.processingQueue.remove(0);
					String policyFilePath = machine.context.getPgc().getIDFileMap().get(policyToProcess);
					
					ActionSet actions = machine.rm.processPolicy(policyToProcess, policyFilePath, machine.dataQueue.get(0));
					ArrayList<DecidedAction> actionList = actions.getActions();
					for(DecidedAction action:actionList) { 
						action.execute(machine.context);
					}
					
					machine.update(machine, machine.dataQueue.remove(0).get("dataType"));
				} else {
					machine.update(machine, null);
				}
				//machine.currentState.transition(machine, Event.WAIT, null);
			}
		},
		SUSPENDED {
			void transition(BSCMachine machine, Event event, HashMap<String,Object> data) {
				switch (event) {
				case ACTIVATE:
					machine.currentState = State.IDLE;
					break;
				default:
					break;
				}
				machine.currentState.doAction(machine, data);
			}

			void doAction(BSCMachine machine, HashMap<String,Object> data) {
			}
		};

		abstract void transition(BSCMachine machine, Event event, HashMap<String,Object> data);

		abstract void doAction(BSCMachine machine, HashMap<String,Object> data);
	}

	protected enum Event {
		DEACTIVATE, ACTIVATE, STARTPROCESSING, WAIT
	}

	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	private ArrayList<String> processingQueue = new ArrayList<String>();
	private ArrayList<HashMap<String,Object>> dataQueue = new ArrayList<HashMap<String,Object>>();
	
	public State getState() {
		return currentState;
	}

	@Override
	public void subscribe(int dataType) {
				
	}

	/*
	 * Receive the subscribed data
	 */
	@Override
	public synchronized void getNotified(HashMap<String,Object> data, int dataType) { 
		if(data!=null) {
			//System.out.println("BSC receives data: " + data.get("mdata-"+dataType) + " of type " + dataType);
			
			// check policies with the required data type
			HashMap<String, Trigger> idTriggerMap = context.getPgc().getIDTriggerMap();
			Set<String> ids = idTriggerMap.keySet();
			
			for(String id:ids) {
				Trigger trigger = idTriggerMap.get(id);
				if(dataType==trigger.getActiveTriggerType() && trigger.conditionFulfilled(data.get("mdata-"+dataType)) && 
						(context.getPgc().getActivePolicies().contains(id) || (data.get("force")!=null && (Boolean)data.get("force")))) {
					data.put("goal", context.getPgc().getIDGoalMap().get(id));
					data.put("dataType", dataType);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, context.getTime().hour());
					cal.set(Calendar.MINUTE, context.getTime().min());
					cal.set(Calendar.DAY_OF_MONTH, context.getTime().day());
					cal.set(Calendar.MONTH, context.getTime().month()-1);
					cal.set(Calendar.YEAR, context.getTime().year());
					data.put("timestamp", cal.getTime().toString());
					data.put("houseID", context.getHouse().getModelID());
					data.put("powerLimit", (context.getHouse().getContract()==DSO.CONTRACT_RTPCPP || context.getHouse().getContract()==DSO.CONTRACT_RTPCPPEV)?15000:99999);
					data.put("cemsAlgorithm", context.getHouse().getCEMS()==null?0:context.getHouse().getCEMS().getEVChargeAlgorithm());
					data.put("isEVContract", (DSO.isEVContract(context.getHouse().getContract()))?1:0);
					// put the policy in the processing queue
					processingQueue.add(id);
					dataQueue.add(data);
				}
			}
		}
		
		if(currentState!=State.PROCESSPOLICY) currentState.transition(this, Event.STARTPROCESSING, null);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(processingQueue.isEmpty()) {			
			this.currentState.transition(this, Event.WAIT, null);
			if(arg!=null && ((Integer)arg==-1)) {
				context.getHouse().taskDoneNotification(1);
			}
		} else {
			this.currentState.doAction(this, null);
		}
	}

}

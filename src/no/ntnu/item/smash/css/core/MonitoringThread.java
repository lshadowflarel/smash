package no.ntnu.item.smash.css.core;
public abstract class MonitoringThread extends Thread{
	
	protected long interval;
	protected int id;
	protected boolean running = true;
	
	protected enum State {
		IDLE {
			void transition(MonitoringThread thread, Event event, Object data) {
				switch (event) {
				case MONITOR:
					thread.currentState = State.MONITORING;
					break;
				default:
					break;
				}
				thread.currentState.doAction(thread, data);
			}

			void doAction(MonitoringThread machine, Object data) {
				machine.running = false;
			}
		},
		MONITORING {
			void transition(MonitoringThread thread, Event event, Object data) {
				switch (event) {
				case WAIT:
					thread.currentState = State.IDLE;
					break;
				default:
					break;
				}
				thread.currentState.doAction(thread, data);
			}

			void doAction(MonitoringThread thread, Object data) {
				thread.running = true;
				if(!thread.isAlive()) thread.start();
			}
		};
		
		abstract void transition(MonitoringThread thread, Event event, Object data);

		abstract void doAction(MonitoringThread machine, Object data);
	}
	
	public static enum Event {
		WAIT, MONITOR
	}
	
	private State initialState = State.IDLE;
	private State currentState = initialState;
	
	protected MONMachine parent;
	
	public MonitoringThread(MONMachine parent) {
		super();
		this.parent = parent;
	}
	
	public void setMonitoringInterval(long interval) {
		this.interval = interval;
	}
	
	public long getMonitoringInterval() {
		return interval;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public State getCurrentState() {
		return currentState;
	}
	
	public MONMachine getParent() {
		return parent;
	}
}

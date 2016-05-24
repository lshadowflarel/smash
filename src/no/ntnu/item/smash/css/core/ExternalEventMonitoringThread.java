package no.ntnu.item.smash.css.core;

import java.util.ArrayList;
import java.util.HashMap;

public class ExternalEventMonitoringThread extends MonitoringThread{
	
	@SuppressWarnings("unchecked")
	private ArrayList<HashMap<String,Object>>[] eventList = new ArrayList[24];
	
	public ExternalEventMonitoringThread(MONMachine parent) {
		super(parent);
		
		initEventList();
	}

	@Override
	public void run() {
		int hour = -1;
		
		while(running) {
			if(parent.getContext().getTime().hour()>hour) {
				hour = parent.getContext().getTime().hour();
				
				if(eventList[hour%24].size()==0) parent.reportData(null, new int[]{MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT});
				
				// send events to subscriber
				for(int i=0; i<eventList[hour%24].size(); i++) {
					HashMap<String,Object> dataMap = eventList[hour%24].remove(i); 
					parent.reportData(dataMap, new int[]{MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT});
				}
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addEvent(HashMap<String,Object> eventData, int time) {
		eventList[time].add(eventData);
	}
	
	private void initEventList() {
		for(int i=0; i<24; i++) {
			eventList[i] = new ArrayList<HashMap<String,Object>>();
		}
	}
}

package no.ntnu.item.smash.css.core;

import java.util.HashMap;

import no.ntnu.item.smash.css.structure.Trigger;

public class TimeMonitoringThread extends MonitoringThread{
	
	public TimeMonitoringThread(MONMachine parent) {
		super(parent);
	}

	@Override
	public void run() {
		int hour = -1;
		
		while(running) {
			if(parent.getContext().getTime().hour()>hour) {
				hour = parent.getContext().getTime().hour(); 
				
				// send time back to the subscriber
				HashMap<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("mdata-"+Trigger.TYPE_TIME, hour%24);
				parent.reportData(dataMap, new int[]{MONMachine.SUBSCRIBE_DATA_TIME});
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}

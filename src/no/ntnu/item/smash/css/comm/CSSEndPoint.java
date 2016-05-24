package no.ntnu.item.smash.css.comm;

import java.util.Calendar;
import java.util.HashMap;

import no.ntnu.item.smash.css.core.MONMachine;
import no.ntnu.item.smash.css.core.SystemContext;
import no.ntnu.item.smash.css.structure.Trigger;

public class CSSEndPoint {

	private SystemContext context;
	
	public CSSEndPoint(SystemContext context) {
		this.context = context;
	}


	public void syncTime(int min, int hour, int day, int month, int year) {
		context.getTime().setTime(min, hour, day, month, year);
		context.getMon().getPriceMonitor().syncPrice();
		context.getBsc().update(context.getBsc(), new Integer(-1));
	}

	public void notifyPowerLimitReached(double powerToReduce, double currentPeak, String timeString) { 	//System.out.println("######## " + powerToReduce + " " + currentPeak);
		HashMap<String,Object> data = new HashMap<String,Object>();
		data.put("mdata-9", currentPeak);
		data.put("powerToReduce", powerToReduce);
		data.put("exactTimeString", timeString);
		context.getBsc().getNotified(data, 9);
	}
	
	public void requestStartDevice(int devType, String devName, double[] plan, int taskID, Object[] additionalInfo) {
		HashMap<String,Object> data = new HashMap<String,Object>();
		 
		/*
		 	DEVTYPE_HEATER = 0;
			DEVTYPE_EWATERHEATER = 1;
			DEVTYPE_LIGHT = 2;
			DEVTYPE_WASHINGMACHINEDRYER = 3;
			DEVTYPE_EVCHARGER = 4;
		*/
		if(devType==3) {
			Calendar cal = Calendar.getInstance();
			cal.set((int)plan[7], (int)plan[6]-1, (int)plan[5], (int)plan[2], 0);
			data.put("mdata-5", devType);
			data.put("devName", devName);
			data.put("washProg", (int)plan[0]);
			data.put("dryProg", (int)plan[1]);
			data.put("startTime", cal.getTime().toString());
			data.put("pauseTime", plan[3]);
			cal.set(Calendar.HOUR_OF_DAY, (int)plan[4]);
			data.put("latestFinish", cal.getTime().toString());
			data.put("taskID", taskID);
		} else if(devType==4) {
			Calendar cal = Calendar.getInstance();
			cal.set((int)plan[6], (int)plan[5]-1, (int)plan[4], (int)plan[2]/60, (int)plan[2]%60);
			data.put("mdata-5",  devType);
			data.put("devName", devName);
			data.put("startTime", cal.getTime().toString());
			cal.add(Calendar.MINUTE, (int)plan[3]);
			data.put("latestFinish", cal.getTime().toString());
			data.put("stateOfCharge", plan[1]);
			data.put("chargePower", plan[7]);
			data.put("nonPossibleStartTime", additionalInfo[0]);
			data.put("batterySize", additionalInfo[1]);
		}
		
		context.getBsc().getNotified(data, 5);
	}

	public void remoteEvent(String source, int eventTime, HashMap<String,Object> data) { 		
		data.put("mdata-"+Trigger.TYPE_EXTERNAL_EVENT, source);
		
		// create an ExternalEvent
		context.getMon().reportData(data, new int[]{MONMachine.SUBSCRIBE_DATA_EXTERNALEVENT});
	}

	
}

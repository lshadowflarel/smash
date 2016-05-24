package no.ntnu.item.smash.css.core;

import java.util.Calendar;
import java.util.HashMap;

import no.ntnu.item.smash.css.structure.Trigger;

public class PriceMonitoringThread extends MonitoringThread{
	
	private int hour = -1, min = 0;
	
	public PriceMonitoringThread(MONMachine parent) {
		super(parent);
	}

	public void syncPrice() {
		if(parent.getContext().getTime().hour()>hour || (parent.getContext().getTime().hour()==hour && parent.getContext().getTime().min()>min)) {  
			hour = parent.getContext().getTime().hour();
			min = parent.getContext().getTime().min();
			
			// get the immediate interval price from the DSO
			HashMap<String,Object> parameters = new HashMap<String,Object>();
			parameters.put("time", ""+((3600*(hour%24))+(min*60)));
			parameters.put("day", ""+parent.getContext().getTime().day());
			parameters.put("month", ""+parent.getContext().getTime().month());
			parameters.put("year", ""+parent.getContext().getTime().year());
			double dsoPrice = (Double)parent.getDataRequester().getRemoteData("DSO", "getEstElectricityPrice", parameters).get("dso-price");
			double espPrice = (Double)parent.getDataRequester().getRemoteData("DSO", "getEstElectricityPrice", parameters).get("esp-price");
							
			// get also the next interval price
			HashMap<String,Object> parametersNextInt = new HashMap<String,Object>();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, min);
			cal.set(Calendar.DAY_OF_MONTH, parent.getContext().getTime().day());
			cal.set(Calendar.MONTH, parent.getContext().getTime().month());
			cal.set(Calendar.YEAR, parent.getContext().getTime().year());
			cal.add(Calendar.MINUTE, 30);
			parametersNextInt.put("time", ""+((3600*(cal.get(Calendar.HOUR_OF_DAY)%24))+(cal.get(Calendar.MINUTE)*60)));
			parametersNextInt.put("day", ""+cal.get(Calendar.DAY_OF_MONTH));
			parametersNextInt.put("month", ""+cal.get(Calendar.MONTH));
			parametersNextInt.put("year", ""+cal.get(Calendar.YEAR));
			double dsoPriceNextInt = (Double)parent.getDataRequester().getRemoteData("DSO", "getEstElectricityPrice", parametersNextInt).get("dso-price");
			double espPriceNextInt = (Double)parent.getDataRequester().getRemoteData("DSO", "getEstElectricityPrice", parametersNextInt).get("esp-price");
			
			// send price back to the subscriber
			HashMap<String,Object> dataMap = new HashMap<String,Object>();
			dataMap.put("mdata-"+Trigger.TYPE_PRICE_DSO, dsoPrice);
			dataMap.put("mdata-"+Trigger.TYPE_PRICE_ESP, espPrice);
			dataMap.put("mdata-"+Trigger.TYPE_PRICE, dsoPrice+espPrice);
			dataMap.put("mdata-"+Trigger.TYPE_PRICE_DSO_NEXTINT, dsoPriceNextInt);
			dataMap.put("mdata-"+Trigger.TYPE_PRICE_ESP_NEXTINT, espPriceNextInt);
			dataMap.put("mdata-"+Trigger.TYPE_PRICE_NEXTINT, dsoPriceNextInt+espPriceNextInt);
			dataMap.put("nextIntTimestamp", cal.getTime().toString());
			parent.reportData(dataMap, new int[]{MONMachine.SUBSCRIBE_DATA_ESP_PRICE, MONMachine.SUBSCRIBE_DATA_DSO_PRICE, MONMachine.SUBSCRIBE_DATA_PRICE, 
					MONMachine.SUBSCRIBE_DATA_ESP_PRICE_NEXTINT, MONMachine.SUBSCRIBE_DATA_DSO_PRICE_NEXTINT, MONMachine.SUBSCRIBE_DATA_PRICE_NEXTINT});
		}
	}
	
	@Override
	public void run() {
	}
	
}

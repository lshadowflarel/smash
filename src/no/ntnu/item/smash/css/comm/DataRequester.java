package no.ntnu.item.smash.css.comm;

import java.util.HashMap;

import no.ntnu.item.smash.css.core.SystemContext;

public class DataRequester {
	private SystemContext context;
	
	public DataRequester(SystemContext context) {
		this.context = context;
	}
	
	public synchronized HashMap<String,Object> getRemoteData(String provider, String operation, HashMap<String,Object> parameters) {		
		HashMap<String,Object> results = context.getHouse().requestRemoteData(provider, operation, parameters);
		
		return results;
	}
	
}

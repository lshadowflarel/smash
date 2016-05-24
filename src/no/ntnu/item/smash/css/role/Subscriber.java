package no.ntnu.item.smash.css.role;

import java.util.HashMap;

public interface Subscriber {

	public void subscribe(int dataType);
	
	public void getNotified(HashMap<String,Object> data, int dataType);
	
}

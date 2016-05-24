package net.sf.xet.nxet.data;

import java.util.HashMap;

public class InMemoryObjects {

	private HashMap<String,String> XETMEMORYOBJECT = new HashMap<String,String>();
	
	public InMemoryObjects() {}
	
	public HashMap<String,String> getInMemoryObject() {
		return XETMEMORYOBJECT;
	}
	
}

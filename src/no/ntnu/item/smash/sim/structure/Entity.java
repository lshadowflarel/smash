package no.ntnu.item.smash.sim.structure;

public abstract class Entity {

	protected String name;
	protected String resourceURI;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	
	
}

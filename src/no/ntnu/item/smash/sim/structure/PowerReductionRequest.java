package no.ntnu.item.smash.sim.structure;

public abstract class PowerReductionRequest {

	private String id;
	private int startInterval;
	private int endInterval;
	
	public PowerReductionRequest() {
		
	}
	
	public PowerReductionRequest(int startInterval, int endInterval) {
		this.startInterval = startInterval;
		this.endInterval = endInterval;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getStartInterval() {
		return startInterval;
	}

	public void setStartInterval(int startInterval) {
		this.startInterval = startInterval;
	}

	public int getEndInterval() {
		return endInterval;
	}

	public void setEndInterval(int endInterval) {
		this.endInterval = endInterval;
	}
}

package no.ntnu.item.smash.sim.structure;

public class CLCRequest extends PowerReductionRequest {

	// interval length is 5 minutes
	private double kW;
	
	public CLCRequest() {
		
	}
	
	public CLCRequest(int start, int end, double kW) {
		super(start, end);
		this.kW = kW;
	}

	public double getKW() {
		return kW;
	}
	
	public void setKW(int kW) {
		this.kW = kW;
	}	
	
}

package no.ntnu.item.smash.sim.structure;

public class ULCRequest extends PowerReductionRequest {

	private int startHour;
	private int endHour;
	private int kW;
	
	public ULCRequest() {
		
	}
	
	public ULCRequest(int start, int end, int kW) {
		this.startHour = start;
		this.endHour = end;
		this.kW = kW;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getKW() {
		return kW;
	}
	
	public void setKW(int kW) {
		this.kW = kW;
	}
	
	public int getDuration() {
		return 60*(endHour - startHour);
	}
	
	
}

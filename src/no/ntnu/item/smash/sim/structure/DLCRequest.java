package no.ntnu.item.smash.sim.structure;

public class DLCRequest extends PowerReductionRequest {

	private int deviceType;
	private int cycleDuration; // min
	private int controlDuration; // min
	
	public DLCRequest() {
		
	}
	
	public DLCRequest(int deviceType, int startTime, int endTime, int cycleDuration, int controlDuration) {
		super(startTime, endTime);
		this.deviceType = deviceType;
		this.cycleDuration = cycleDuration;
		this.controlDuration = controlDuration;
	}
	
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public int getCycleDuration() {
		return cycleDuration;
	}
	public void setCycleDuration(int cycleDuration) {
		this.cycleDuration = cycleDuration;
	}
	public int getControlDuration() {
		return controlDuration;
	}
	public void setControlDuration(int controlDuration) {
		this.controlDuration = controlDuration;
	}
	
	
}

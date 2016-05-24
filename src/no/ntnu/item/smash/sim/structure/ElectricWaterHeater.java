package no.ntnu.item.smash.sim.structure;

public class ElectricWaterHeater extends Device {

	private double tankHeight;
	private double tankDiameter;
	private double tankVolume; // litre
	private double rValue;
	
	public double getTankHeight() {
		return tankHeight;
	}
	public void setTankHeight(double tankHeight) {
		this.tankHeight = tankHeight;
	}
	public double getTankDiameter() {
		return tankDiameter;
	}
	public void setTankDiameter(double tankDiameter) {
		this.tankDiameter = tankDiameter;
	}
	public double getTankVolume() {
		return tankVolume;
	}
	public void setTankVolume(double tankVolume) {
		this.tankVolume = tankVolume;
	}
	public double getrValue() {
		return rValue;
	}
	public void setrValue(double rValue) {
		this.rValue = rValue;
	}
	
	
}

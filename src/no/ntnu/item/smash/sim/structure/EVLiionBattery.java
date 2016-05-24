package no.ntnu.item.smash.sim.structure;

public class EVLiionBattery extends Device {
	
	private double kWh;
	private double maxRange;
	private boolean lv2Charging;
	
	public EVLiionBattery() {
		
	}

	public double getkWh() {
		return kWh;
	}

	public void setkWh(double kWh) {
		this.kWh = kWh;
	}

	public double getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(double maxRange) {
		this.maxRange = maxRange;
	}

	public static double getReducedSoCAfterDistance(double kWh, double maxRange, double distance, double WhRecovered) {
		double kWhPerkm = kWh/maxRange;
		double reducedKWh = (kWhPerkm * distance) + (WhRecovered*0.001);
		
		return (reducedKWh/kWh)*100;
	}
	
	public void setCanChargeLV2(boolean ability) {
		lv2Charging = ability;
	}
	
	public boolean canChargeLV2() {
		return lv2Charging;
	}
	
	@Override
	public double getWattByProgram(int program) {
		if(program==1) return 1100;
		else return watt;
	}
}

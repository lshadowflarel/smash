package no.ntnu.item.smash.sim.structure;

public class Plan {

	private double[] hourlyPlan;

	public Plan() {
		
	}
	
	public Plan(double[] plan) {
		hourlyPlan = plan;
	}
	
	public double[] getHourlyPlan() {
		return hourlyPlan;
	}

	public void setHourlyPlan(double[] hourlyPlan) {
		this.hourlyPlan = hourlyPlan;
	}
	
	public void setValueAtHour(int hour, double value) {
		hourlyPlan[hour] = value;
	}
	
}

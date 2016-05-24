package no.ntnu.item.smash.sim.structure;

public class Schedule {

	private double[] extendedSchedule;

	public Schedule() {
		
	}
	
	public Schedule(double[] schedule) {
		extendedSchedule = schedule;
	}
	
	public double[] getSchedule() {
		return extendedSchedule;
	}

	public void setSchedule(double[] schedule) {
		this.extendedSchedule = schedule;
	}
	
	public void setValueAtTime(int hour, int min, int second, double value) {
		int sHour = 12 * hour;
		if(second>30) min += 1;
		int sMin = min/5;
		extendedSchedule[sHour+sMin] = value;
	}
	
	public double getValueAtTime(int hour, int min, int second) {
		int sHour = 12 * hour;
		if(second>30) min += 1;
		int sMin = min/5;
		
		return extendedSchedule[sHour+sMin];
	}
	
	/**
	 * Create a schedule with 5 min resolution from a schedule with 1 hour resolution
	 * @param schedule
	 * @return another schedule with 5 min resolution
	 */
	public static double[] createHighResolutionSchedule(double[] schedule) {
		double[] newSchedule = new double[288];
		
		for(int i=0; i<24; i++) {
			for(int j=0; j<12; j++) {
				newSchedule[(12*i)+j] = schedule[i];
			}
		}
		
		return newSchedule;
	}
	
	public static void printSchedule(double[] schedule) {
		for(int i=0; i<schedule.length; i++)
			System.out.print(schedule[i] + ",");
		System.out.println();
	}
}

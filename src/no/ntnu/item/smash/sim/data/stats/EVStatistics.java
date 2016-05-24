package no.ntnu.item.smash.sim.data.stats;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;

public class EVStatistics extends EntityStatistics {

	private ArrayList<double[]> SoCs = new ArrayList<double[]>();
	
	public EVStatistics(String path, String entityName) {
		super(path, entityName);
	}

	public void addSoCData(double[] data) { 
		SoCs.add(Arrays.copyOf(data, data.length));
	}
	
	public ArrayList<double[]> getSoCData() {
		return SoCs;
	}
	
	protected void initializeAll() {
		initializeStat(monthlyEnergy);
		initializeStat(monthlyPlan);
		initializeStat(monthlySchedule);
		initializeStat(SoCs);
	}
	
	public void writeDataToFiles(int month) {
		if(monthlyEnergy.isEmpty()) return;
		
		writeDataToFile(month, "energy", monthlyEnergy);
		writeDataToFile(month, "plan", monthlyPlan);
		writeDataToFile(month, "schedule", monthlySchedule);
		writeDataToFile(month, "peakload", monthlyPeakLoad);
		writeSoCDataToFile(month, "soc", SoCs);
		
		initializeAll();
	}
	
	public void writeYearHeadingToFiles(int year) {
		writeYearHeadingToFile(year, "energy");
		writeYearHeadingToFile(year, "plan");
		writeYearHeadingToFile(year, "schedule");
		writeYearHeadingToFile(year, "peakload");
		writeYearHeadingToFile(year, "soc");
	}
	
	public void writeYearHeadingToFile(int year, String dataType) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + dataType + ".txt");
	}
	
	public void writeDataToFile(int month, String dataType, ArrayList<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + dataType + ".txt");
	}
	
	public void writeSoCDataToFile(int month, String dataType, ArrayList<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += d[0] + " " + d[1] + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + dataType + ".txt");
	}
}

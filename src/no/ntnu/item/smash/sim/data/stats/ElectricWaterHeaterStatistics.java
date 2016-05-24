package no.ntnu.item.smash.sim.data.stats;

import java.util.ArrayList;
import java.util.Arrays;

import no.ntnu.item.smash.sim.data.Constants;

public class ElectricWaterHeaterStatistics extends EntityStatistics {

	private ArrayList<double[]> monthlyWaterDemand = new ArrayList<double[]>();
	private ArrayList<double[]> monthlyWaterTemp = new ArrayList<double[]>();
	
	public ElectricWaterHeaterStatistics(String path, String entityName) {
		super(path, entityName);
	}

	public void addWaterDemandData(double[] data) { 
		monthlyWaterDemand.add(Arrays.copyOf(data, data.length));
	}
	
	public void addWaterTempData(double[] data) { 
		monthlyWaterTemp.add(Arrays.copyOf(data, data.length));
	}
	
	protected void initializeAll() {
		initializeStat(monthlyEnergy);
		initializeStat(monthlyPlan);
		initializeStat(monthlySchedule);
		initializeStat(monthlyWaterDemand);
		initializeStat(monthlyWaterTemp);
		initializeStat(monthlyPeakLoad);
	}
	
	public void writeDataToFiles(int month) {
		if(monthlyEnergy.isEmpty()) return;
		
		writeDataToFile(month, "energy", monthlyEnergy);
		writeDataToFile(month, "wtemp", monthlyWaterTemp);
		writeDataToFile(month, "plan", monthlyPlan);
		writeDataToFile(month, "schedule", monthlySchedule);
		writeDataToFile(month, "demand", monthlyWaterDemand);
		writeDataToFile(month, "peakload", monthlyPeakLoad);
		
		initializeAll();
	}
	
	public void writeYearHeadingToFiles(int year) {
		writeYearHeadingToFile(year, "energy");
		writeYearHeadingToFile(year, "wtemp");
		writeYearHeadingToFile(year, "plan");
		writeYearHeadingToFile(year, "schedule");
		writeYearHeadingToFile(year, "demand");
		writeYearHeadingToFile(year, "peakload");
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
}

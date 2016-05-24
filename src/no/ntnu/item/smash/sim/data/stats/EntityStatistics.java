package no.ntnu.item.smash.sim.data.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.structure.Schedule;

public class EntityStatistics {

	protected String resultDirectory = "results";
	protected String entityName;
	protected ArrayList<double[]> monthlyEnergy = new ArrayList<double[]>();
	protected ArrayList<double[]> monthlyRTemp = new ArrayList<double[]>();
	protected ArrayList<double[]> monthlyPlan = new ArrayList<double[]>();
	protected ArrayList<double[]> monthlySchedule = new ArrayList<double[]>();
	protected ArrayList<double[]> monthlyPeakLoad = new ArrayList<double[]>();
	
	public EntityStatistics(String path, String entityName) {
		resultDirectory = path;
		this.entityName = entityName;

		File dir = new File(resultDirectory);
		dir.mkdirs();
	}
	
	public void addEnergyData(double[] data) { 
		monthlyEnergy.add(Arrays.copyOf(data, data.length));
	}
	
	public void addRTempData(double[] data) { 
		monthlyRTemp.add(Arrays.copyOf(data, data.length));
	}
	
	public void addPlanData(double[] data, boolean highRes) { // true=create high resolution, false=already high resolution
		if(highRes) monthlyPlan.add(Schedule.createHighResolutionSchedule(Arrays.copyOf(data, data.length)));
		else monthlyPlan.add(Arrays.copyOf(data, data.length));
	}
	
	public void addScheduleData(double[] data) {
		monthlySchedule.add(Arrays.copyOf(data, data.length));
	}
	
	public void addPeakLoadData(double[] data) {
		monthlyPeakLoad.add(Arrays.copyOf(data, data.length));
	}
	
	protected void initializeAll() {
		initializeStat(monthlyEnergy);
		initializeStat(monthlyRTemp);
		initializeStat(monthlyPlan);
		initializeStat(monthlySchedule);
		initializeStat(monthlyPeakLoad);
	}
	
	protected void initializeStat(ArrayList<double[]> stat) {
		stat.clear();
	}
	
	public void writeDataToFiles(int month) {
		if(monthlyEnergy.isEmpty()) return;
		
		writeDataToFile(month, "energy", monthlyEnergy);
		writeDataToFile(month, "rtemp", monthlyRTemp);
		writeDataToFile(month, "plan", monthlyPlan);
		writeDataToFile(month, "schedule", monthlySchedule);
		writeDataToFile(month, "peakload", monthlyPeakLoad);
		
		initializeAll();
	}	
	
	public void writeDataToFile(int month, String dataType, ArrayList<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + dataType + ".txt");
	}
	
	protected String getDailyDataString(double[] dailyData) {
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.0000");
		
		String toReturn = "" + (dailyData[0]==-9999?"null":df.format(dailyData[0]));
		
		for(int i=1; i<dailyData.length; i++) {
			toReturn += ", " + (dailyData[i]==-9999?"null":df.format(dailyData[i]));
		}
		
		return toReturn;
	}
	
	public void writeYearHeadingToFiles(int year) {
		writeYearHeadingToFile(year, "energy");
		writeYearHeadingToFile(year, "rtemp");
		writeYearHeadingToFile(year, "plan");
		writeYearHeadingToFile(year, "schedule");
		writeYearHeadingToFile(year, "peakload");
	}
	
	public void writeYearHeadingToFile(int year, String dataType) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + dataType + ".txt");
	}
	
	 public static void writeStringToFile(String text, String file) {
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(file);
			// if file doesnt exists, then create it
			if (!f.exists()) { 
				f.createNewFile();
			}
			
			fop = new FileOutputStream(f, true);			

			// get the content in bytes
			fop.write(text.getBytes());
			
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void emptyDirectory() {
		File folder = new File(resultDirectory);
		File[] files = folder.listFiles();
		for (File f: files) f.delete();
	}
	
}

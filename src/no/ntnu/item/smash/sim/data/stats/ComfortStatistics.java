package no.ntnu.item.smash.sim.data.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.structure.ComfortMeasure;

public class ComfortStatistics {

	private HashMap<ComfortMeasure, ArrayList<double[]>> dodStats = new HashMap<ComfortMeasure, ArrayList<double[]>>();
	private HashMap<ComfortMeasure, ArrayList<String[]>> comfortStats = new HashMap<ComfortMeasure, ArrayList<String[]>>();
	protected String resultDirectory = "results";
	protected String entityName;
	
	public ComfortStatistics(String path, String entityName) {
		resultDirectory = path;
		this.entityName = entityName;
		
		File dir = new File(resultDirectory);
		dir.mkdirs();
	}
	
	public void addComfortStat(ComfortMeasure measure, double[] data, String[] data2) {
		if(dodStats.get(measure)==null) {
			dodStats.put(measure, new ArrayList<double[]>());
			comfortStats.put(measure, new ArrayList<String[]>());
		}
		
		dodStats.get(measure).add(Arrays.copyOf(data, data.length));
		comfortStats.get(measure).add(Arrays.copyOf(data2, data2.length));
	}
	
	public HashMap<ComfortMeasure, ArrayList<double[]>> getDODStats() {
		return dodStats;
	}
	
	public HashMap<ComfortMeasure, ArrayList<String[]>> getComfortLVStats() {
		return comfortStats;
	}
	
	public ArrayList<double[]> getDODStat(ComfortMeasure measure) {
		return dodStats.get(measure);
	}
	
	public ArrayList<String[]> getComfortLVStat(ComfortMeasure measure) {
		return comfortStats.get(measure);
	}
	
	public void writeDataToFiles(int month) {
		Set<ComfortMeasure> keys = dodStats.keySet();
		for(ComfortMeasure measure:keys) {
			writeDataToFile(month, dodStats.get(measure), "DoD_" + measure.getName());
			writeStringDataToFile(month, comfortStats.get(measure), "COM_" + measure.getName());
		}
		
//		dodStats.clear();
//		comfortStats.clear();
	}
	
	private void writeDataToFile(int month, ArrayList<double[]> data, String comfortMeasureName) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + comfortMeasureName + "DoD.txt");
	}
	
	private void writeStringDataToFile(int month, ArrayList<String[]> data, String comfortMeasureName) { 
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(String[] d:data) { 
			for(int i=0; i<d.length; i++) {
				toWrite += d[i]; 
				if(i<d.length-1) toWrite += ",";
			}
			toWrite += "\n"; 
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + comfortMeasureName + "DoD.txt");
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
		Set<ComfortMeasure> keys = dodStats.keySet();
		for(ComfortMeasure measure:keys) {
			writeYearHeadingToFile(year, "DoD_" + measure.getName());
			writeYearHeadingToFile(year, "COM_" + measure.getName());
		}
	}
	
	public void writeYearHeadingToFile(int year, String comfortMeasureName) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/" + entityName + "_" + comfortMeasureName + "DoD.txt");
	}
	
	protected void writeStringToFile(String text, String file) {
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
}

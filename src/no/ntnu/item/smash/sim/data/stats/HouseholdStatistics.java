package no.ntnu.item.smash.sim.data.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import no.ntnu.item.smash.sim.data.Constants;


public class HouseholdStatistics {
	
	private int startDay = 1;
	
	// Energy consumption HEURISTICS - estimated household energy consumption 
	// with regards to average outdoor temperature and average comfort level
	public static final HashMap<Integer, Double> reducedEnergyConsumptionStat = new HashMap<Integer,Double>();
	public static final HashMap<Integer, Double> normalEnergyConsumptionStat = new HashMap<Integer,Double>();
	
	static {
		normalEnergyConsumptionStat.put(-20, 65.0);
		normalEnergyConsumptionStat.put(-15, 60.0);
		normalEnergyConsumptionStat.put(-10, 55.0);
		normalEnergyConsumptionStat.put(-5, 40.0);
		normalEnergyConsumptionStat.put(0, 35.0);
		normalEnergyConsumptionStat.put(5, 25.0);
		normalEnergyConsumptionStat.put(10, 18.0);
		normalEnergyConsumptionStat.put(15, 12.0);
		normalEnergyConsumptionStat.put(20, 5.0);
		
		reducedEnergyConsumptionStat.put(-20, 62.0);
		reducedEnergyConsumptionStat.put(-15, 56.0);
		reducedEnergyConsumptionStat.put(-10, 47.0);
		reducedEnergyConsumptionStat.put(-5, 35.0);
		reducedEnergyConsumptionStat.put(-0, 32.0);
		reducedEnergyConsumptionStat.put(5, 28.0);
		reducedEnergyConsumptionStat.put(10, 16.0);
		reducedEnergyConsumptionStat.put(15, 11.0);
		reducedEnergyConsumptionStat.put(20, 4.0);
    }
	
	private String resultDirectory = "results";
	private List<double[]> monthlyEnergy = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyCost = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyOTemp = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyPrice = Collections.synchronizedList(new ArrayList<double[]>());
	private List<double[]> monthlyPeakLoad = Collections.synchronizedList(new ArrayList<double[]>());
	
	public HouseholdStatistics(String path) {
		resultDirectory = path;
		
		new File(resultDirectory).mkdirs();
		emptyDirectory(resultDirectory);	
		
		InputStream inStream = null;
		OutputStream outStream = null;

    	try{
    		File res = new File("./misc/results.html");
    	    File copied = new File(resultDirectory + "/results.html");
    	    
    	    inStream = new FileInputStream(res);
    	    outStream = new FileOutputStream(copied);
 
    	    byte[] buffer = new byte[1024];
 
    	    int length;
    	    while ((length = inStream.read(buffer)) > 0){
     	    	outStream.write(buffer, 0, length);
     	    }
 
    	    inStream.close();
    	    outStream.close(); 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
	}
	
	public void setStartDay(int day) {
		startDay = day;
	}
	
	public synchronized void addEnergyDataAtDay(int d, double[] data) {
		int day = d - startDay + 1;
		
		if(day>monthlyEnergy.size())
			monthlyEnergy.add(Arrays.copyOf(data, data.length));
		else
			monthlyEnergy.set(day-1, mergeData(monthlyEnergy.get(day-1), data));
	}
	
	public synchronized void addCostDataAtDay(int d, double[] data) {
		int day = d - startDay + 1;

		if(day>monthlyCost.size())
			monthlyCost.add(Arrays.copyOf(data, data.length));
		else
			monthlyCost.set(day-1, mergeData(monthlyCost.get(day-1), data));
	}
	
	public synchronized void addOTempDataAtDay(int d, double[] data) {
		int day = d - startDay + 1;
		
		if(day>monthlyOTemp.size())
			monthlyOTemp.add(Arrays.copyOf(data, data.length));
	}
	
	public synchronized void addPriceDataAtDay(int d, double[] data) {
		int day = d - startDay + 1;
		
		if(day>monthlyPrice.size())
			monthlyPrice.add(Arrays.copyOf(data, data.length));
	}
	
	public synchronized void addPeakLoadDataDay(int d, double[] data) { // this one is 288 instead of 24 like the others
		int day = d - startDay + 1;
		
		if(day>monthlyPeakLoad.size())
			monthlyPeakLoad.add(Arrays.copyOf(data, data.length));
		else 
			monthlyPeakLoad.set(day-1, mergeData(monthlyPeakLoad.get(day-1), data));
	}
	
	public synchronized void addCostData(double[] cost) {
		monthlyCost.add(cost);
	}
	
	public List<double[]> getMonthlyEnergy() {
		return monthlyEnergy;
	}
	
	public List<double[]> getMonthlyCost() {
		return monthlyCost;
	}
	
	public List<double[]> getMonthlyPrice() {
		return monthlyPrice;
	}
	
	public List<double[]> getMonthlyPeakLoad() {
		return monthlyPeakLoad;
	}
	
	private void initializeAll() {
		initializeStat(monthlyEnergy);
		initializeStat(monthlyCost);
		initializeStat(monthlyOTemp);
		initializeStat(monthlyPrice);
		initializeStat(monthlyPeakLoad);
	}
	
	private void initializeStat(List<double[]> stat) {
		stat.clear();
	}
	
	public void writeDataToFiles(int month) {
		if(monthlyEnergy.isEmpty()) return;
		
		writeDataToFile(month, "energy", monthlyEnergy);
		writeDataToFile(month, "cost", monthlyCost);
		writeDataToFile(month, "otemp", monthlyOTemp);
		writeDataToFile(month, "price", monthlyPrice);
		writePeakLoadDataTofile(month, monthlyPeakLoad);
		
		initializeAll();
	}	
	
	public void writePeakLoadDataTofile(int month, List<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		for(double[] d:data) {
			// make a 24-hour version from 288-slot version
			double[] d24 = new double[24];
			for(int i=0; i<288; i=i+12) {
				double max = 0;
				for(int j=0; j<12; j++) {
					if(d[i+j]>max) max = d[i+j];
				}
				d24[((i+12)/12)-1] = max;
			}
			toWrite += getDailyDataString(d24) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/Household_peakload.txt");
	}
	
	public void writeDataToFile(int month, String dataType, List<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/Household_" + dataType + ".txt");
	}
	
	private String getDailyDataString(double[] dailyData) {
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		
		String toReturn = "" + (dailyData[0]==-9999?"null":df.format(dailyData[0]));
		
		for(int i=1; i<dailyData.length; i++) {
			toReturn += ", " + (dailyData[i]==-9999?"null":df.format(dailyData[i]));
		}
		
		return toReturn;
	}
	
	public void writeYearHeadingToFiles(int year) {
		writeYearHeadingToFile(year, "energy");
		writeYearHeadingToFile(year, "cost");
		writeYearHeadingToFile(year, "otemp");
		writeYearHeadingToFile(year, "price");
		writeYearHeadingToFile(year, "peakload");
	}
	
	public void writeYearHeadingToFile(int year, String dataType) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/Household_" + dataType + ".txt");
	}
	
	private void writeStringToFile(String text, String file) {
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

	public static void emptyDirectory(String resultDirectory) {
		File folder = new File(resultDirectory);
		if(folder.exists()) {
			File[] files = folder.listFiles();
			for (File f: files) {
				if(f.isDirectory()) {
					emptyDirectory(f.getAbsolutePath());
				}
				f.delete();
			}
		}
	}
	
	private double[] mergeData(double[] data1, double[] data2) {
		double[] merged = new double[Math.min(data1.length, data2.length)];
		
		for(int i=0; i<Math.min(data1.length, data2.length); i++) {
			merged[i] = data1[i] + data2[i];
		}
		
		return merged;
	}
	
	public static double getEstEnergyConsumption(int comfortLevel, int temperature) {
		double kwh = 0;
		
		HashMap<Integer,Double> refMap = normalEnergyConsumptionStat;
		if(comfortLevel<2) refMap = reducedEnergyConsumptionStat;
		
		int start = (temperature/5)*5; 
		int next = temperature<0?start-5:start+5;
		
		double kwhStart = refMap.get(start);
		double kwhNext = refMap.get(next);
		
		double step = (kwhNext-kwhStart)/5;
		
		kwh = kwhStart + ((temperature%5)*step);
		
		return kwh;
	}
	
	/*
	 * This method creates a new array of 24-hour time series data from
	 * an input array of wider range time series data
	 */
	public static double[] aggregateData(double[] data, int step, boolean avg) {
		double[] newData = new double[data.length/step];
		
		for(int i=0; i<data.length; i=i+step) {
			double total = 0;
			for(int j=i; j<i+step; j++) {
				total += data[j];
			}
			
			if(avg) newData[i/step] = total/step;
			else newData[i/step] = total;
		}
	
		return newData;
	}
	
	public static double[] aggregateDataMax(double[] data, int step) {
		double[] newData = new double[data.length/step]; 
		
		for(int i=0; i<data.length; i=i+step) {
			double max = 0;
			for(int j=i; j<i+step; j++) {
				if(data[j]>max) max = data[j];
			}
			
			newData[i/step] = max;
		}
	
		return newData;
	}
	
	public static ArrayList<double[]> readStat(String file, int size) {
		ArrayList<double[]> stat = new ArrayList<double[]>();
		
		BufferedReader br = null;
		
		double[] daily = new double[size];
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(file));
 
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				if(sCurrentLine.startsWith("#") || sCurrentLine.isEmpty()) continue;
				
				String[] d = sCurrentLine.split(", ");
				
				for(int i=0; i<d.length; i++) {
					daily[i] = Double.parseDouble(d[i]);
				}
				
				stat.add(Arrays.copyOf(daily, daily.length));
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return stat;
	}
	
	public void writeCompentation(int houseID, double amount, String dir, String code) {
		File fileToWrite = new File(dir + "/full_compensation.txt");
		
		if(!fileToWrite.exists()){
			try {
				fileToWrite.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileToWrite,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		pw.println(houseID + "," + amount + "," + code);
		
		pw.close();
	}
}

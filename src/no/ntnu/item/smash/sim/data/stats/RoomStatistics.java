package no.ntnu.item.smash.sim.data.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.structure.Room;


public class RoomStatistics {
	
	private int startDay = 1;
	
	private String resultDirectory = "results";
	
	private HashMap<Room,List<double[]>> roomEnergyMap = new HashMap<Room,List<double[]>>();
	private HashMap<Room,List<double[]>> roomCostMap = new HashMap<Room,List<double[]>>();
	private HashMap<Room,List<double[]>> roomPeakLoadMap = new HashMap<Room,List<double[]>>();
	
	public RoomStatistics(String path) {
		resultDirectory = path;
	}
	
	public void addRoom(Room room) {
		roomEnergyMap.put(room, Collections.synchronizedList(new ArrayList<double[]>()));
		roomCostMap.put(room, Collections.synchronizedList(new ArrayList<double[]>()));
		roomPeakLoadMap.put(room, Collections.synchronizedList(new ArrayList<double[]>()));
	}
	
	public void setStartDay(int day) {
		startDay = day;
	}
	
	public synchronized void addEnergyDataAtDay(Room room, int d, double[] data) {
		int day = d - startDay + 1;
		
		List<double[]> monthlyEnergy = roomEnergyMap.get(room);
		
		if(day>monthlyEnergy.size()) 
			monthlyEnergy.add(Arrays.copyOf(data, data.length));
		else
			monthlyEnergy.set(day-1, mergeData(monthlyEnergy.get(day-1), data));
		
		roomEnergyMap.put(room, monthlyEnergy);
	}
	
	public synchronized void addCostDataAtDay(Room room, int d, double[] data) {
		int day = d - startDay + 1;
		
		List<double[]> monthlyCost = roomCostMap.get(room);
		
		if(day>monthlyCost.size())
			monthlyCost.add(Arrays.copyOf(data, data.length));
		else
			monthlyCost.set(day-1, mergeData(monthlyCost.get(day-1), data));
		
		roomCostMap.put(room, monthlyCost);
	}

	
	public synchronized void addPeakLoadDataDay(Room room, int d, double[] data) {
		int day = d - startDay + 1;
		
		List<double[]> monthlyPeakLoad = roomPeakLoadMap.get(room);
		
		if(day>monthlyPeakLoad.size())
			monthlyPeakLoad.add(Arrays.copyOf(data, data.length));
		else
			monthlyPeakLoad.set(day-1, mergeData(monthlyPeakLoad.get(day-1), data));
		
		roomPeakLoadMap.put(room, monthlyPeakLoad);
	}
	
	private void initializeAll() {
		Set<Room> keys = roomEnergyMap.keySet();
		for(Room r:keys) {
			initializeStat(roomEnergyMap.get(r));
			initializeStat(roomCostMap.get(r));
			initializeStat(roomPeakLoadMap.get(r));
		}
	}
	
	private void initializeStat(List<double[]> stat) {
		stat.clear();
	}
	
	public void writeDataToFiles(int month) {
		Set<Room> keys = roomEnergyMap.keySet();
		for(Room r:keys) {
			if(roomEnergyMap.get(r).isEmpty()) continue;
			
			writeDataToFile(r, month, "energy", roomEnergyMap.get(r));
			writeDataToFile(r, month, "cost", roomCostMap.get(r));
			writeDataToFile(r, month, "peakload", roomPeakLoadMap.get(r));
		}
		
		initializeAll();
	}	
	
	public void writeDataToFile(Room room, int month, String dataType, List<double[]> data) {
		String toWrite = "\n#M-" + Constants.getMonthName(month) + "\n";
		
		// get the content in bytes
		for(double[] d:data) { 
			toWrite += getDailyDataString(d) + "\n";
		}
		
		writeStringToFile(toWrite, resultDirectory + "/" + room.getName() + "_" + dataType + ".txt");
	}
	
	private String getDailyDataString(double[] dailyData) {
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
		Set<Room> keys = roomEnergyMap.keySet();
		for(Room r:keys) {
			writeYearHeadingToFile(r, year, "energy");
			writeYearHeadingToFile(r, year, "cost");
			writeYearHeadingToFile(r, year, "peakload");
		}
	}
	
	public void writeYearHeadingToFile(Room room, int year, String dataType) {
		String toWrite = "#Y-" + year;
		
		writeStringToFile(toWrite, resultDirectory + "/" + room.getName() + "_" + dataType + ".txt");
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
	
	private double[] mergeData(double[] data1, double[] data2) {
		double[] merged = new double[Math.min(data1.length, data2.length)];
		
		for(int i=0; i<Math.min(data1.length, data2.length); i++) {
			merged[i] = data1[i] + data2[i];
		}
		
		return merged;
	}

	
}

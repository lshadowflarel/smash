package no.ntnu.item.smash.sim.data.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


public class FullSimulationStatistics {
	
	public static void aggregateStats(String dir, int numHouse) {
		
		ArrayList<double[]> costData = new ArrayList<double[]>();
		ArrayList<double[]> energyData = new ArrayList<double[]>();
		
		for(int i=0; i<numHouse; i++) {
			String dirPath = dir + "/H" + (i+1);
			
			// aggregate cost
			costData = mergeData(costData, HouseholdStatistics.readStat(dirPath + "/Household_cost.txt", 24));
			
			// aggregate energy
			energyData = mergeData(energyData, HouseholdStatistics.readStat(dirPath + "/Household_energy.txt", 24));
		}
		
		writeStat(dir, "full_cost.txt", costData);
		writeStat(dir, "full_energy.txt", energyData);
	}
	
	private static ArrayList<double[]> mergeData(ArrayList<double[]> data1, ArrayList<double[]> data2) {
		for(int i=0; i<data2.size(); i++) {
			if(data1.size()<i+1) {
				data1.add(data2.get(i));
			}
			else {
				double[] d1 = data1.get(i);
				double[] d2 = data2.get(i);
				double[] newDaily = new double[24];
				
				for(int j=0; j<24; j++) {
					newDaily[j] = d1[j]+d2[j];
				}
				
				data1.set(i, newDaily); 
			}
		}
		
		return data1;
	}
	
	private static void writeStat(String dir, String fileName, ArrayList<double[]> data) {
		// write to file
		File file = new File(dir + "/" + fileName);
		//if file doesnt exists, then create it
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir + "/H1/Household_cost.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String daily = "";
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.000");
		try {
			String sCurrentLine;
			int days = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				if(sCurrentLine.startsWith("#") || sCurrentLine.isEmpty()) {
					pw.println(sCurrentLine);
				} else {
					double[] d = data.get(days++);
					for(int j=0; j<d.length-1; j++)
						daily += df.format(d[j]) + ",";
					daily += df.format(d[d.length-1]);
					
					pw.println(daily);
					daily = "";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
	
	public static void writeComfortStat(String dir, int houseID, ArrayList<double[]> data) {
		File fileToWrite = new File(dir + "/full_comfort.txt");

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
		
		int day = 1;
		for(double[] d:data) {
			pw.println(houseID + "," + (day++) + "," + (int)d[0] + "," + (int)d[1]);
		}
		pw.close();
	}
}

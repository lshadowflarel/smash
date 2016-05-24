package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import no.ntnu.item.smash.sim.data.Constants;

public class PeakloadDataUtility {
	
	public static void main(String[] args) {
		PeakloadDataUtility.writeForecastLoad("Final_Base50", 1, 2010);
	}
	
	public static void writeForecastLoad(String folder, int month, int year) {
		String path = folder + "/full_peakload.txt";

		BufferedReader br = null;
		File file = new File("./data/forecastload/FL"
				+ (month < 10 ? "0" + month : month) + year + ".txt");
		// if file doesnt exists, then create it
		if (file.exists()) {
			file.delete();
		}
		try {
			file.delete();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		Random generator = new Random(54822);
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			int line = 1;
			String dailyLoad = "";
			
			double halfHourLoad = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {				
				halfHourLoad += Double.parseDouble(sCurrentLine);
				
				if(line%6==0) {
					dailyLoad += df.format((halfHourLoad/5)+((generator.nextDouble()>0.5?-1:1)*generator.nextDouble()*20000)) + " ";
					halfHourLoad = 0;
				} 
				if(line%288==0) {
					pw.println(dailyLoad.trim());
					dailyLoad = "";
				}
				
				line++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				pw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static double[] readDailyForecastPeakLoad(int day, int month, int year) {
		double[] data = new double[48];
		
		String path = "./data/forecastload/FL" + (month < 10 ? "0" + month : month) + year + ".txt";
		BufferedReader br = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			int line = 1;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				if(line==day) {
					String[] d = sCurrentLine.split(" ");
					for(int i=0; i<d.length; i++) {
						data[i] = Double.parseDouble(d[i]);
					}
					
					return data;
				}
				line++;
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
		
		return data;
	}
	
	public static double[] readHistoricPeakLoad(int month, int year, String path) {
		double[] data = new double[Constants.getNumDaysInMonth(month, year)*288];
		BufferedReader br = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			int interval = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				data[interval] = Double.parseDouble(sCurrentLine);
				interval++;
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
		
		return data;
	}
}

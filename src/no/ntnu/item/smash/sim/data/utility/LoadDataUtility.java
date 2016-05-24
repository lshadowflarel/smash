package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class LoadDataUtility {

	public static void main(String[] args) {
		//LoadDataUtility.writeBaseLoad("data/load/H1/L012006.txt", "data/load/RD.txt", "data/load/H1/L012010.txt");
		//LoadDataUtility.writeForecastEnergy("TEMP", 1, 2010);
		LoadDataUtility.rewriteBaseLoad();
	}
	
	public static double[] readLoadDataForDay(int houseID, int day, int month, int year) {
		double[] data = new double[288];
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("./data/load/H" + (houseID+1) + "/L" + (month<10?"0"+month:month) + year + ".txt"));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				line++;
				
				if(line==day) {					
					String[] load = sCurrentLine.split(",");
					for(int s=0; s<load.length; s++) { 
						data[s] = Double.parseDouble(load[s])*1000;
					}
					break;
				}
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
	
	/*
	 * For all houses
	 */
	public static double[] readDailyForecastEnergy(int day, int month, int year, int updateInterval) {
		double[] data = new double[24*60/updateInterval];
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("./data/forecastload/FE" + (month<10?"0"+month:month) + year + ".txt"));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				line++;
				
				if(line==day) {					
					String[] load = sCurrentLine.split(" ");
					if(data.length>=load.length) {
						int repeat = data.length/load.length;
						for(int i=0; i<load.length; i++) {
							for(int j=0; j<repeat; j++) {
								data[i+j] = Double.parseDouble(load[i]);
							}
						}
					} else {
						//TODO: complete this later
					}
					
					break;
				}
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
	
	public static void writeForecastEnergy(String folder, int month, int year) {
		String path = folder + "/full_energy.txt";

		BufferedReader br = null;
		File file = new File("./data/forecastload/FE"
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
 
			String dailyLoad = "";
			br.readLine();
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {				
				String[] daily = sCurrentLine.split(",");
				
				for(int i=0; i<daily.length; i++) {
					for(int repeat=0; repeat<2; repeat++) {
						dailyLoad += df.format((Double.parseDouble(daily[i])/2) + ((generator.nextDouble()>0.5?-1:1)*generator.nextDouble()*2)) + " ";
					}
				}
				
				pw.println(dailyLoad.trim());
				dailyLoad = "";
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
	
	/*
	 * NOT USED - THIS WAS A BAD IDEA
	 */
	public static void writeBaseLoad(String path_totalLoad, String path_toSubtract, String path_toCreate) {
		BufferedReader br = null;
		BufferedReader br2 = null;
		PrintWriter pw = null;
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		
		try {
			File fileTarget = new File(path_toCreate);
			if (fileTarget.exists())
				fileTarget.delete();
			fileTarget.createNewFile();

			br = new BufferedReader(new FileReader(path_totalLoad));
			br2 = new BufferedReader(new FileReader(path_toSubtract));
			FileWriter fw = new FileWriter(fileTarget, true);
			pw = new PrintWriter(fw);
			String tLoad, sLoad, toWrite = "";
			while ((tLoad = br.readLine()) != null
					&& !tLoad.isEmpty()) {
				sLoad = br2.readLine();
				String[] totalLoad = tLoad.split(",");
				String[] subtractLoad = sLoad.split(",");
				
				double max = Double.parseDouble(totalLoad[0]); int index = 0; 
				for(int i=0; i<totalLoad.length; i++) {
					if(Double.parseDouble(totalLoad[i])>max) {
						max = Double.parseDouble(totalLoad[i]);
						index = i; 
					}
				}
				
				double excessLoad = max-3; 
				double excessUnit = excessLoad/Double.parseDouble(subtractLoad[index]);
				for(int i=0; i<totalLoad.length; i++) {
					toWrite += df.format(Math.max(1,Double.parseDouble(totalLoad[i]) - (excessUnit * Double.parseDouble(subtractLoad[i]))));
					if(i<totalLoad.length-1) toWrite += ",";
				}
				
				pw.println(toWrite); 
				toWrite = "";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (br2 != null)
					br2.close();
				if (pw != null)
					pw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void rewriteBaseLoad() {
		for(int template=1; template<=5; template++) {
			String random = "./data/load/temp/" + template +  "/L012009-" + System.currentTimeMillis() + ".txt";
			
			String path = "./data/load/temp/" + template + "/L012009.txt";
			File oldfile = new File(path);
			File newfile = new File(random);
			
			oldfile.renameTo(newfile);
			
			BufferedReader br = null;
			
			FileWriter fw = null;
			PrintWriter pw = null;
			try {
				fw = new FileWriter(path,true);
				pw = new PrintWriter(fw);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			DecimalFormat df = (DecimalFormat)
			        NumberFormat.getNumberInstance(new Locale("en", "US"));
			df.applyPattern("0.00");
			
			try {
				String sCurrentLine;
				br = new BufferedReader(new FileReader(random));
	 
				while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
					String[] split = sCurrentLine.split(",");
					String newString = "";
					for(int i=0; i<split.length; i++) {
						newString += "" + df.format(Math.max(0,Double.parseDouble(split[i]) - 3));
						if(i<split.length-1)newString += ", "; 
					}
					
					pw.println(newString);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) br.close();
					pw.close();
					newfile.delete();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}

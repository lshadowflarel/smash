package no.ntnu.item.smash.sim.data.formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import no.ntnu.item.smash.sim.data.Constants;

public class RawPriceDataFormatter {

	public static final String SRC_PATH = "rawdata/prices/";
	public static final String DEST_PATH = "data/prices/";
	
	public static void parseAndWrite(int month, int year, String city) {
		String fileName = "Energi_" + Constants.getMonthName(month).toUpperCase() + year + "_" + city + ".csv";
		
		File file = new File(DEST_PATH + "E" + (month<10?"0"+month:month) + year + ".txt");
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
			String sCurrentLine;
			br = new BufferedReader(new FileReader(SRC_PATH + fileName));
 
			int line = 0; 
			int day = 1;
			String dailyPrice = "";
			
			br.readLine(); // 1st line is only the heading
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] hourly = sCurrentLine.split(";");
				
				if(line==0) {
					dailyPrice = hourly[1];
					
					line++;
				} else if(line<23) {
					dailyPrice = dailyPrice + "," + hourly[1];
					
					line++;
				} else if(line==23) {
					if(hourly[0].equals((day<10?"0"+day:""+day))) {
						dailyPrice = dailyPrice + "," + hourly[1];
						
						pw.println(dailyPrice);
						line = 0;
						day++;
					} else {
						dailyPrice = dailyPrice + "," + hourly[1];
						
						pw.println(dailyPrice);
						dailyPrice = hourly[1];
						line = 1;
						day++;
					}					
				}
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
	
	
}

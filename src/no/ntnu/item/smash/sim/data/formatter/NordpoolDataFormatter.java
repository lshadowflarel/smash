package no.ntnu.item.smash.sim.data.formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import no.ntnu.item.smash.sim.data.Constants;

public class NordpoolDataFormatter {

	public static final String SRC_PATH = "rawdata/prices/";
	public static final String DEST_PATH = "data/prices/";
	
	public static void main(String[] args) {
		if(args.length<3) System.out.println("Needs 3 arguments: month (integer), year (integer), and city name");
		
		try {
			int month = Integer.parseInt(args[0]);
			int year = Integer.parseInt(args[1]);
			String city = args[2];
			NordpoolDataFormatter.parseAndWrite(month, year, city);
		} catch(Exception e) {
			System.out.println("Failed to parse arguments. Check the formats.");
		}
	}
	
	public static void parseAndWrite(int month, int year, String city) {
		String fileName = "Nordpool_" + Constants.getMonthName(month).toUpperCase() + year + "_" + city + ".csv";
		
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
		String daily = "";
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.000");
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(SRC_PATH + fileName));
			
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] splitted = sCurrentLine.split(";");
				for(int i=1; i<splitted.length-1; i++) {
					daily += df.format(Double.parseDouble(splitted[i].replace(",", "."))/1000) + ",";
				}
				daily += df.format(Double.parseDouble(splitted[splitted.length-1].replace(",", "."))/1000);
				
				pw.println(daily);
				daily = "";
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

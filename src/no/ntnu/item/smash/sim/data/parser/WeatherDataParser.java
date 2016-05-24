package no.ntnu.item.smash.sim.data.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import no.ntnu.item.smash.sim.data.Constants;

public class WeatherDataParser {

	public static final String WEATHER_TEMP_PATH = "data/weather/";
	
	public static double[][] readTemperatureForMonth(int month, int year, String type) {
		String filePath = WEATHER_TEMP_PATH + "W" + (month<10?"0"+month:month) + year + type + ".txt";
		BufferedReader br = null;
		
		double[][] data = new double[Constants.getNumDaysInMonth(month, year)][24];
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filePath));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] daily = sCurrentLine.split(",");
				
				for(int i=0; i<daily.length; i++) {
					data[line][i] = Double.parseDouble(daily[i]);
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
	
}

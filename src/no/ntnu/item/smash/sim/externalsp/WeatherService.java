package no.ntnu.item.smash.sim.externalsp;

import no.ntnu.item.smash.sim.data.parser.WeatherDataParser;

public class WeatherService {

	private static final double[] WEATHER_FORECAST = { 9, 9.5, 9.5, 10, 10, 10,
			10.5, 10.5, 11, 11, 12, 13, 13.5, 14, 14, 15, 14, 13.5, 12, 12, 12,
			11, 11, 10 };
	
	private static double[][] WEATHER_ACTUAL;
	private static double[][] LAKE_TEMP_ACTUAL;

	private static int dataMonth = -1;
	private static int dataYear = -1;
	
	private static int lakeMonth = -1;
	private static int lakeYear = -1;
	
	public static double getForecastTempAtHour(int hourOfDay) {
		if (hourOfDay > WEATHER_FORECAST.length - 1)
			hourOfDay = 0;
		return WEATHER_FORECAST[hourOfDay];
	}

	public static double getForecastTempAtTime(int time) { // time means time
															// step which is in
															// seconds
		int hourOfDay = time / 3600;
		if (hourOfDay > WEATHER_FORECAST.length - 1)
			hourOfDay -= 1;
		return WEATHER_FORECAST[hourOfDay];
	}
	
	public static double getActualTemp(int time, int day, int month, int year) {
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==dataMonth && year==dataYear) {
			if(WEATHER_ACTUAL[day-1][hourOfDay]!=9999) {
				return WEATHER_ACTUAL[day-1][hourOfDay];
			} else {
				// TODO: not sure what to do
			}
		} else { 
			// read from file
			WEATHER_ACTUAL = WeatherDataParser.readTemperatureForMonth(month, year, "");
			dataMonth = month;
			dataYear = year;
		
			return WEATHER_ACTUAL[day-1][hourOfDay];
		}
		
		return 9999;
	}
	
	public static double getActualLakeTemp(int time, int day, int month, int year) {
		int hourOfDay = 0;
		
		if((time/3600)%24==0) hourOfDay = 0;
		else hourOfDay = (time/3600)%24;
		
		if(month==lakeMonth && year==lakeYear) {
			if(LAKE_TEMP_ACTUAL[day-1][hourOfDay]!=9999) { 
				return LAKE_TEMP_ACTUAL[day-1][hourOfDay];
			} else {
				// TODO: not sure what to do
			}
		} else { 
			// read from file
			LAKE_TEMP_ACTUAL = WeatherDataParser.readTemperatureForMonth(month, year, "_WATER");
			lakeMonth = month;
			lakeYear = year;
		
			return LAKE_TEMP_ACTUAL[day-1][hourOfDay];
		}
		
		return 9999;
	}
	
	public static double getActualTempAtTime(int time, int day) {
		int hourOfDay = time / 3600;
		
		return WEATHER_ACTUAL[day-1][hourOfDay];
	}
	
	public static double getForecastedAvgTemp() {
		double avg = 0;
		
		for(int i=0; i<WEATHER_FORECAST.length; i++) {
			avg += WEATHER_FORECAST[i];
		}
		
		return avg/WEATHER_FORECAST.length;
	}
	
	public static void setDataMonth(int month) {
		dataMonth = month;
	}
	
	public static void setDataYear(int year) {
		dataYear = year;
	}
	
}

package no.ntnu.item.smash.sim.data.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import no.ntnu.item.smash.sim.structure.BuildingThermalIntegrity;

public class EnergyUsageStatistics {
	
	public static double[] thermalProp = BuildingThermalIntegrity.NORWAY_MEDINSULATED;
	
	public static double getEstEnergyToMaintain(int houseID, double otempD, double tempD, String entityID) {		
		int otemp = (int)otempD;
		int temp = (int)tempD;
		int startOtemp = (int)((otemp/5)*5);
		int refTemp = (int)(temp%2==1?temp:temp+1);
		
		if(startOtemp<-35) startOtemp = -35;
		if(startOtemp>20) startOtemp = 20;
		
		if(refTemp<17) refTemp = 17;
		if(refTemp>27) refTemp = 27;
		
		double kwhStart = readEnergyData(houseID, startOtemp, refTemp, entityID);
		double kwhNext = readEnergyData(houseID, startOtemp-5, refTemp, entityID);
		
		double step = (kwhNext-kwhStart)/5.0;
		
		double estimatedE = kwhStart + ((otemp%5)*step);
		
		return thermalProp==BuildingThermalIntegrity.NORWAY_HEAVYINSULATED?estimatedE:estimatedE*1.15; // 1.15
	}	
	
	private static double readEnergyData(int houseID, int otemp, int setpoint, String room) {
		String toCheck = room + "," + setpoint + "," + otemp + "=";
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("data/histEnergy/H" + (houseID+1) + ".txt"));
		
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.startsWith(toCheck)) {
					return Double.parseDouble(sCurrentLine.substring(sCurrentLine.indexOf("=")+1, sCurrentLine.length()));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		System.out.println("ERROR! ENERGY USAGE STAT NOT FOUND");
		
		return 0;
	}
	
}

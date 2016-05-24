package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class RecordHistoricalEnergyData {

	/*
	 * Class written to parse energy consumption of result files
	 */	
	public static void main(String[] args) {
		int[] houseTemplateIDs = {1,2,3,4,5};
		int[] setpoints = {17, 19, 21, 23, 25, 27};
		
		for(int i=houseTemplateIDs[0]; i<=houseTemplateIDs[houseTemplateIDs.length]; i++) {
			for(int j=setpoints[0]; j<=setpoints[setpoints.length]; j++) {
				clearFile(i, j);
				
				RecordHistoricalEnergyData.writeResults("LivingRoom", i, j);
				RecordHistoricalEnergyData.writeResults("Kitchen", i, j);
				RecordHistoricalEnergyData.writeResults("Bathroom1", i, j);
				RecordHistoricalEnergyData.writeResults("Bathroom2", i, j);
				RecordHistoricalEnergyData.writeResults("Bathroom3", i, j);
				RecordHistoricalEnergyData.writeResults("Bedroom1", i, j);
				RecordHistoricalEnergyData.writeResults("Bedroom2", i, j);
				RecordHistoricalEnergyData.writeResults("Bedroom3", i, j);
				RecordHistoricalEnergyData.writeResults("Work", i, j);
				RecordHistoricalEnergyData.writeResults("Guest", i, j);
			}
		}
		
		for(int i=0; i<houseTemplateIDs.length; i++) {
			RecordHistoricalEnergyData.mergeRecords(houseTemplateIDs[i]);
		}
	}
	
	public static void clearFile(int houseTemplateID, int setpoint) {
		File f = new File("data/houses/histEnergy/P" + houseTemplateID + "/" + setpoint + ".txt");
		if(f.exists()) f.delete();
		
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeResults(String room, int houseTemplateID, int setpoint) {		
		//check if this house type has the room
		File fTest = new File("data/histEnergy/H1/" + room + "_energy.txt");
		if(!fTest.exists()) return;
		
		FileOutputStream fop = null;
		File f;
		BufferedReader br = null;
		
		try {
			f = new File("data/houses/histEnergy/P" + houseTemplateID + "/" + setpoint + ".txt");	
			fop = new FileOutputStream(f, true);
			
			fop.write(("#"+room).getBytes());
			
			String sCurrentLine;
			br = new BufferedReader(new FileReader("data/histEnergy/H1/" + room + "_energy.txt"));
 
			sCurrentLine = br.readLine();
			sCurrentLine = br.readLine();
			
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] daily = sCurrentLine.split(", ");
				double print = 0;
				boolean foundOver0 = false;
				boolean found = false;
				for(int i=12;i<daily.length;i++) {
					if(Double.parseDouble(daily[i])>0) foundOver0 = true;
					else if(foundOver0) {
						found = true;
						for(int j=i; j<i+12 && j<daily.length; j++) {
							print+=Double.parseDouble(daily[j]);
						}
						break;
					}
				}
				
				if(!found) print += Double.parseDouble(daily[12])*12; 
				String toPrint = "\n" + print;
				fop.write(toPrint.getBytes());
				
				line++;
				if(line==12) {
					toPrint = "\n\n";
					fop.write(toPrint.getBytes());
					break;
				}
			}
 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				fop.flush();
				fop.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void mergeRecords(int housePatternID) {
		int[] setpoints = {17, 19, 21, 23, 25, 27};
		int[] otemp = {-35, -30, -25, -20, -15, -10, -5, 0, 5, 10, 15, 20};
		
		FileOutputStream fop = null;
		BufferedReader br = null;
		File write = new File("data/houses/histEnergy/P" + housePatternID + "/HE-M.txt");
		if(write.exists()) write.delete();
		try {
			write.createNewFile();
			fop = new FileOutputStream(write, true);
			
			for(int i=0; i<setpoints.length; i++) {				
				String sCurrentLine;
				br = new BufferedReader(new FileReader("data/houses/histEnergy/P" + housePatternID + "/" + setpoints[i] + "-M.txt"));
								
				int line = 0;
				String room = "";
				while ((sCurrentLine = br.readLine()) != null) {
					String toPrint = "";
					if(sCurrentLine.startsWith("#")) {
						room = sCurrentLine.substring(1, sCurrentLine.length());
						line = 0;
					} else if(!sCurrentLine.isEmpty()){
						toPrint += room + "," + setpoints[i] + "," + otemp[line-1] + "=" + sCurrentLine + "\n";
					}
					
					fop.write(toPrint.getBytes());
					
					line++;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
				fop.flush();
				fop.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
}

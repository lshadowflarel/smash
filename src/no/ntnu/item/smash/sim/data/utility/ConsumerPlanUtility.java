package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConsumerPlanUtility {

	public static ArrayList<double[]> readPlan(String fileName, int day) {
		ArrayList<double[]> schedule = new ArrayList<double[]>();
		
		String path = "./data/plan/" + fileName;
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				line++;
				
				if(line==day) {
					if(sCurrentLine.equals("-")) break;
					
					String[] activity = sCurrentLine.split(";");
					for(int s=0; s<activity.length; s++) { 
						activity[s] = activity[s].replace("[", "");
						activity[s] = activity[s].replace("]", "");
						String[] infoS = activity[s].split(",");
						double[] info = new double[infoS.length];
						
						for(int l=0; l<infoS.length; l++) {
							info[l] = Double.parseDouble(infoS[l]);
						}
						
						schedule.add(info);
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
		
		return schedule;
	}
	
	public static HashMap<String,double[]> readRoomTempPlans(String path) {
		HashMap<String,double[]> plans = new HashMap<String,double[]>();
		
		if(!new File(path).exists()) return plans;
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			boolean foundRoom = false;
			String room = "";
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				
				if(!foundRoom && sCurrentLine.startsWith("#")) {
					foundRoom = true;
					room = sCurrentLine.substring(1,sCurrentLine.length()).trim();
				} else if(foundRoom) {
					String[] temps = sCurrentLine.trim().split(",");
					double[] plan = new double[24];
					for(int i=0; i<temps.length; i++) {
						plan[i] = Double.parseDouble(temps[i]);
					}
					plans.put(room, plan);
					foundRoom = false;
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
		
		return plans;
	}
}

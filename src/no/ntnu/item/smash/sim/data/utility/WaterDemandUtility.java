package no.ntnu.item.smash.sim.data.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import no.ntnu.item.smash.sim.data.Constants;

public class WaterDemandUtility {

	private static final double[] hourlyDemand_weekend = {410,320,247.5,247.5,235,222.5,237.5,246,273.5,375,575,770,787.5,642.5,512.5,462.5,475,547.5,562.5,585,670,725,735,577.5};
	private static final double[] hourlyDemand_weekday = {652.5,520,395,320,270,247.5,397.5,630,655,575,510,487.5,502.5,487.5,462.5,440,412.5,447.5,512.5,525,537.5,602.5,635,660};
	private static final double totalDemand_weekend = 10864.5;
	private static final double totalDemand_weekday = 11225;
	
	
	public static double[][] readWaterDemand(String fileName, int day) {
		double[][] wd = new double[2][24];
		
		String path = "data/plan/" + fileName;
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(path));
 
			int line = 0;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				line++;
				
				if(line==day) {
					String[] hourly = sCurrentLine.split(";");
					for(int s=0; s<hourly.length; s++) { 
						hourly[s] = hourly[s].replace("[", "");
						hourly[s] = hourly[s].replace("]", "");
						String[] litreDuration = hourly[s].split(",");
						
						wd[0][s] = Double.parseDouble(litreDuration[0]);
						wd[1][s] = Double.parseDouble(litreDuration[1]);
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
		
		return wd;
	}
	
	public static void main(String[] args) {
		if(args.length<4) System.out.println("Needs 4 arguments: month (integer), year (integer), number of family members, and number of houses");
		
		try {
			int month = Integer.parseInt(args[0]);
			int year = Integer.parseInt(args[1]);
			int familyMembers = Integer.parseInt(args[2]);
			int numHouses = Integer.parseInt(args[3]);
			
			for(int i=0; i<numHouses; i++) {
				generateMonthlyWaterDemand("wd"+i+".txt", month, year, familyMembers);
			}
		}	catch(Exception e) {
			System.out.println("Failed to parse arguments. Check the formats.");
		}
	}
	
	public static void generateMonthlyWaterDemand(String fileName, int month, int year, int noFamilyMember) {
		int noDaysInMonth = Constants.getNumDaysInMonth(month, year);
		for(int day=1; day<=noDaysInMonth; day++) {		
			int dayType = Constants.getDayType(day, month, year);
			double[] baseData = isWeekday(dayType)?hourlyDemand_weekday:hourlyDemand_weekend;
			double baseTotal = isWeekday(dayType)?totalDemand_weekday:totalDemand_weekend;
			
			double litre = MathDistributionUtility.sampleFromUniform(20, noFamilyMember*50, noFamilyMember*100);
			double[][] dailySchedule = new double[2][24];
			
			for(int i=0; i<24; i++) {
				dailySchedule[0][i] = baseData[i]*(litre/baseTotal);
				dailySchedule[1][i] = MathDistributionUtility.sampleFromUniform(20, 10, 30);
			}
			
			writeDailyDataToFile(day==1?true:false, fileName, dailySchedule);
		}
	}
	
	/**
	 * The methods below are old methods that are no longer used.
	 */
	
	public static void generateMonthlyData(String fileName, int month, int year, int noFamilyMember) {
		int noDaysInMonth = Constants.getNumDaysInMonth(month, year);
		int daysBetweenWash = 0;
		
		for(int day=1; day<=noDaysInMonth; day++) {			
			int dailyLitre = 0, dailyDuration = 0;
			
			int dayType = Constants.getDayType(day, month, year);

			boolean isWeekday = isWeekday(dayType); // determines whether family members use water during work hours
			boolean isColdMonth = isColdMonth(month); // shower less when it's cold
			
			// random the number of family members that shower
			int noMemberShower = randomNumMemberShower(isColdMonth);
			
			// random maximum number of litre used in total
			double totalLitre = randomLitreUsed(noFamilyMember, isWeekday);
			
			// 60% used for showering, 25% for cooking, 15% others
			double washingLitre = 0; // NOTE: IN NORWAY, WASHING MACHINES ARE CONNECTED TO COLD WATER INLET
			double showerLitre = 0.4*(totalLitre-washingLitre);
			double cookingLitre = 0.29*(totalLitre-washingLitre);
			double othersLitre = 0.18*(totalLitre-washingLitre);
			
//			HashMap<Integer,double[]> washingSchedule = randomWashingHours(washingLitre, isWeekday, daysBetweenWash);
//			if(washingSchedule.isEmpty()) daysBetweenWash++;
//			else daysBetweenWash = 0;
			
			HashMap<Integer,double[]> showerSchedule = randomShowerHours(showerLitre/noFamilyMember*noMemberShower, isWeekday, noMemberShower);
			HashMap<Integer,double[]> cookingSchedule = randomCookingHours(cookingLitre, isWeekday, dayType);
			HashMap<Integer,double[]> othersSchedule = randomOthersHours(othersLitre, isWeekday);
			
			double[][] dailySchedule = new double[2][24];
			System.out.println("DAY " + day);
			for(int hour=0; hour<24; hour++) {
				double litre = 0;
				int duration = 0;
				
//				if(washingSchedule.containsKey(hour)) {
//					litre += washingSchedule.get(hour)[0];
//					duration += washingSchedule.get(hour)[1]; System.out.println("Hour: " + hour + " " + "Washing: " + washingSchedule.get(hour)[0] + "/" + washingSchedule.get(hour)[1]);
//				}
				
				if(showerSchedule.containsKey(hour)) {
					litre += showerSchedule.get(hour)[0];
					duration += showerSchedule.get(hour)[1]; System.out.println("Hour: " + hour + " " + "Shower: " + showerSchedule.get(hour)[0] + "/" + showerSchedule.get(hour)[1]);
				}
				
				if(cookingSchedule.containsKey(hour)) {
					litre += cookingSchedule.get(hour)[0];
					duration += cookingSchedule.get(hour)[1]; System.out.println("Hour: " + hour + " " + "Cooking: " + cookingSchedule.get(hour)[0] + "/" + cookingSchedule.get(hour)[1]);
				}
				
				if(othersSchedule.containsKey(hour)) {
					litre += othersSchedule.get(hour)[0];
					duration += othersSchedule.get(hour)[1]; System.out.println("Hour: " + hour + " " + "Others: " + othersSchedule.get(hour)[0] + "/" + othersSchedule.get(hour)[1]);
				}
				
				dailySchedule[0][hour] = litre;
				dailySchedule[1][hour] = duration==0?60:duration;
				
				dailyLitre+=litre;
				dailyDuration+=duration;
			}
			
			System.out.println("\nTotal: " + dailyLitre + "/" + dailyDuration);
			System.out.println("\n");
			
			writeDailyDataToFile(day==1?true:false, fileName, dailySchedule);
		}
	}
	
	public static void writeDailyDataToFile(boolean wipe, String fileName, double[][] dailySchedule) {
		String path = "data/plan/" + fileName;
		File file = new File(path);
		
		//if file doesnt exists, then create it
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(wipe){
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
	
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		String toWrite = "";
		for(int i=0; i<24; i++) {
			double litre = dailySchedule[0][i];
			double duration = dailySchedule[1][i];
			
			if(i>0)
				toWrite += ";[" + df.format(litre) + "," + duration + "]";
			else
				toWrite += "[" + df.format(litre) + "," + duration + "]";
		}
		
		pw.println(toWrite);
		pw.close();
	}
	
	private static boolean isWeekday(int day) {
		if(day>1 && day <7) return true;
		
		return false;
	}
	
	private static boolean isColdMonth(int month) {
		if(month>3 && month<10) return false;
		
		return true;
	}
	
	/*
	 * This method is fixed for 4 family members.
	 */
	private static int randomNumMemberShower(boolean isColdMonth) {
		int noMemberShower = 4;
		
		double ref1 = 0.3;
		double ref2 = 0.67;
		double ref3 = 0.93;
		double ref4 = 0.99;
		
		if(isColdMonth) {
			ref1 = 0.15;
			ref2 = 0.35;
			ref3 = 0.85;
			ref4 = 0.95;
		}
		
		double rand1 = Math.random();
		if(rand1>ref1 && rand1<ref2) noMemberShower = 3;
		else if(rand1>ref2 && rand1<ref3) noMemberShower = 2;
		else if(rand1>ref3 && rand1<ref4) noMemberShower = 1;
		else if(rand1>ref4) noMemberShower = 0;
		
		return noMemberShower;
	}
	
	private static double randomLitreUsed(int noFamilyMember, boolean isWeekday) {
		double total = 0;
		
		int ref1 = 7, ref2 = 6;
		if(isWeekday) {
			ref1 = 8;
			ref2 = 5;
		}
		
		for(int m=1; m<=noFamilyMember; m++) {
			total += 110+(ref1*Math.random())-(ref2*Math.random());
		}
		
		return total;
	}
	
	/*
	 * Using a washing machine at high temperature
	 */
	private static HashMap<Integer,double[]> randomWashingHours(double litre, boolean isWeekday, int daysBetweenWash) {
		HashMap<Integer,double[]> data = new HashMap<Integer,double[]>();
		
		// may decide not to wash at all, in which case the empty schedule is returned
		if(!isWeekday && daysBetweenWash == 0) {
			if(Math.random()>0.2) return data;
		} else if(isWeekday && daysBetweenWash<2) {
			return data;
		} else if(isWeekday && daysBetweenWash<4) {
			if(Math.random()>0.6) return data;
		}
		
		// if we decide to wash it
		int startingHour = 10, rand1 = 0;
		
		if(isWeekday) {
			startingHour = 18;
			rand1 = (int)Math.rint(Math.random() * 3);
		} else {
			rand1 = (int)Math.rint(Math.random() * 10);
		}
		
		int washHour = startingHour + rand1;
		
		double[] hourlyUse = new double[2];
		hourlyUse[0] = litre;
		hourlyUse[1] = 60;
		data.put(washHour, hourlyUse);
		
		return data;
	}
	
	/*
	 * Bath not included
	 */
	private static HashMap<Integer,double[]> randomShowerHours(double litre, boolean isWeekday, int noMemberShower) {
		HashMap<Integer,double[]> data = new HashMap<Integer,double[]>();
		
		int totalDuration = 0;
		int[] duration = new int[noMemberShower];
		for(int p=0;p<noMemberShower;p++) {
			int maxAddTime = 25; //woman-hairwash
			if(Math.random()<0.5) { // man
				maxAddTime = 10;
			} else { // woman
				if(Math.random()>0.4) maxAddTime = 15; // woman-no hairwash 
			}
			
			duration[p] = (int)(5 + (Math.random()*maxAddTime));	
			totalDuration+=duration[p];
		}
		
		boolean[] occupiedSlots;
		if(isWeekday) occupiedSlots = new boolean[10];
		else occupiedSlots = new boolean[13];
		
		for(int p=0;p<noMemberShower;p++) {
			boolean assigned = false;
			double pLitre = ((double)duration[p]/(double)totalDuration)*litre;
			
			while(!assigned) {
				int rhour = Math.max((int)Math.rint(Math.random() * occupiedSlots.length) - 1, 0);
				int hour = 0;
				
				if((isWeekday && rhour < 3) || (!isWeekday && rhour < 6)) hour = 6 + rhour;
				else if(isWeekday) hour = 17 + rhour - 3;
				else hour = 17 + rhour - 6;
				
				if((!occupiedSlots[rhour] && rhour==0) || (!occupiedSlots[rhour] && rhour>0 && !occupiedSlots[rhour-1])) {
					occupiedSlots[rhour] = true;
					assigned = true;
					
					double[] hourlyUse = new double[2];
					hourlyUse[0] = pLitre;
					hourlyUse[1] = duration[p];
					data.put(hour, hourlyUse);
				}	
			}
		}
		
		return data;
	}
	
	/*
	 * Preparing food, washing dishes, ...
	 */
	private static HashMap<Integer,double[]> randomCookingHours(double maxLitre, boolean isWeekday, int dayType) {
		HashMap<Integer,double[]> data = new HashMap<Integer,double[]>();
		
		int timesCooking = 1;
		boolean warmLunch = false;
		if(isWeekday) {
			if(dayType==Calendar.FRIDAY) timesCooking = Math.random()>0.9?0:1;
		} else {
			warmLunch = Math.random()>0.15?false:true;
			
			timesCooking = warmLunch?1:0 + Math.random()>0.9?0:1;
		}
		if (timesCooking==0) return data;
		
		double[] litre = new double[2]; // index 0: litre used for lunch, 1: litre used for dinner
		litre[0] = 0.25*maxLitre;
		litre[1] = 0.75*maxLitre;
		
		// assume food preparation=1 hour, eating=1 hour (no water usage while eating)
		// assume dish washing is done after dinner using a dish washer (110 minutes)
		if(warmLunch) {
			int cookingStart = 11;
			double rand1 = Math.random();
			if(rand1>0.55 && rand1<0.95) cookingStart = 12;
			else if(rand1>0.95) cookingStart = 13;
					
			double[] hourlyUse = new double[2];
			hourlyUse[0] = litre[0];
			hourlyUse[1] = 20;
			data.put(cookingStart, hourlyUse);
		}
		
		if(timesCooking==2 || (timesCooking==1 && !warmLunch)) {
			int cookingStart = 17;
			double rand1 = Math.random();
			if(rand1>0.3 && rand1<0.9) cookingStart = 18;
			else if(rand1>0.9) cookingStart = 19;
			
			for (int h=0; h<4; h++) {
				double litreUsed = 0, minUsed = 0;
				
				switch(h) {
				case 0:
					litreUsed = 0.6*litre[1];
					minUsed = 60;
					break;
				case 2:
				case 3:
					litreUsed = 0.25*litre[1];
					minUsed = h==2?60:50;
					break;
				}
				
				if(h!=1) {
					double[] hourlyUse = new double[2];
					hourlyUse[0] = litreUsed;
					hourlyUse[1] = minUsed;
					data.put(cookingStart+h, hourlyUse);
				}
			}
		}
		
		return data;
	}
	
	/*
	 * Housework, hand/face wash, ...
	 */
	private static HashMap<Integer,double[]> randomOthersHours(double maxLitre, boolean isWeekday) {
		HashMap<Integer,double[]> data = new HashMap<Integer,double[]>();
		
		double houseworkLitre = 0.7*maxLitre;
		double othersLitre = 0.3*maxLitre;
		
		// tend to do housework on weekends
		if(isWeekday) {
			houseworkLitre = Math.random()<0.15?houseworkLitre:(int)0.3*maxLitre;
		}
		
		double totalLitre = houseworkLitre + othersLitre;
		
		int done = 0;
		int startingHour = 10, endingHour = 22, randHours = 5;
		if(isWeekday) {
			startingHour = 18;
			randHours = 3;
		}
		
		// may not use all slots, since it's the max litre we're talking about
		for(int h=startingHour; h<=endingHour; h++) {
			boolean yes = Math.random()>0.5?false:true;
			
			if(yes) {
				double[] hourlyUse = new double[2];
				hourlyUse[0] = totalLitre/randHours;
				hourlyUse[1] = 5+(Math.random()*7);
				data.put(h, hourlyUse);
				done++;
			}
			if(done==randHours) break;
		}
		
//		if(done<randHours) {
//			int remain = randHours - done;
//			for(int h=startingHour; h<endingHour && remain>0; h++) {
//				if(data.containsKey(h)) continue;
//				
//				double[] hourlyUse = new double[2];
//				hourlyUse[0] = totalLitre/randHours;
//				hourlyUse[1] = 5+(Math.random()*7);
//				data.put(h, hourlyUse);
//				
//				remain--;
//			}
//		}
		
		return data;
	}
}

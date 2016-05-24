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

public class Temp {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
//		writeFeederSchedule("EV_01_UNCO");
//		writeFeederSchedule("EV_01_UNCO_P");
//		writeFeederSchedule("EV_01_CO");
//		writeFeederSchedule("EV_01_CO1_P");
//		writeFeederSchedule("EV_01_CO2_P");
//		writeFeederSchedule("TEMP");
//		writeFeederSchedule("EV2_01_UNCO");
//		writeFeederSchedule("EV2_01_UNCO_P");
//		writeFeederSchedule("EV2_01_CO");
//		writeFeederSchedule("EV2_01_CO1_P");
//		writeFeederSchedule("EV2_01_CO2_P");
//		writeFeederSchedule("Final_Base10");
//		writeFeederSchedule("Final_Base30");
//		writeFeederSchedule("Final_Base50");
//		writeFeederSchedule("Final_DLC20");
//		writeFeederSchedule("Final_DLC40");
//		writeFeederSchedule("Final_DLC60");
//		writeFeederSchedule("Final_CLC20");
//		writeFeederSchedule("Final_CLC40");
//		writeFeederSchedule("Final_CLC60");
/*		
		double base = findMinOrMaxValueInFile("EV_Base", "full_peakload.txt", true)*0.001;
		
		double unco01 = findMinOrMaxValueInFile("EV_01_UNCO", "full_peakload.txt", true)*0.001;
		double unco_p01 = findMinOrMaxValueInFile("EV_01_UNCO_P", "full_peakload.txt", true)*0.001;
		double co01 = findMinOrMaxValueInFile("EV_01_CO", "full_peakload.txt", true)*0.001;
		double co1_p01 = findMinOrMaxValueInFile("EV_01_CO1_P", "full_peakload.txt", true)*0.001;
		double co2_p01 = findMinOrMaxValueInFile("EV_01_CO2_P", "full_peakload.txt", true)*0.001;
		
		double unco28 = findMinOrMaxValueInFile("EV_28_UNCO", "full_peakload.txt", true)*0.001;
		double unco_p28 = findMinOrMaxValueInFile("EV_28_UNCO_P", "full_peakload.txt", true)*0.001;
		double co28 = findMinOrMaxValueInFile("EV_28_CO", "full_peakload.txt", true)*0.001;
		double co1_p28 = findMinOrMaxValueInFile("EV_28_CO1_P", "full_peakload.txt", true)*0.001;
		double co2_p28 = findMinOrMaxValueInFile("EV_28_CO2_P", "full_peakload.txt", true)*0.001;
		
		double unco46 = findMinOrMaxValueInFile("EV_46_UNCO", "full_peakload.txt", true)*0.001;
		double unco_p46 = findMinOrMaxValueInFile("EV_46_UNCO_P", "full_peakload.txt", true)*0.001;
		double co46 = findMinOrMaxValueInFile("EV_46_CO", "full_peakload.txt", true)*0.001;
		double co1_p46 = findMinOrMaxValueInFile("EV_46_CO1_P", "full_peakload.txt", true)*0.001;
		double co2_p46 = findMinOrMaxValueInFile("EV_46_CO2_P", "full_peakload.txt", true)*0.001;
		
		double unco64 = findMinOrMaxValueInFile("EV_64_UNCO", "full_peakload.txt", true)*0.001;
		double unco_p64 = findMinOrMaxValueInFile("EV_64_UNCO_P", "full_peakload.txt", true)*0.001;
		double co64 = findMinOrMaxValueInFile("EV_64_CO", "full_peakload.txt", true)*0.001;
		double co1_p64 = findMinOrMaxValueInFile("EV_64_CO1_P", "full_peakload.txt", true)*0.001;
		double co2_p64 = findMinOrMaxValueInFile("EV_64_CO2_P", "full_peakload.txt", true)*0.001;
		
		double l_base = findMinOrMaxValueInFile("EV_Base", "loadflow.txt", false);
		
		double l_unco01 = findMinOrMaxValueInFile("EV_01_UNCO", "loadflow.txt", false);
		double l_unco_p01 = findMinOrMaxValueInFile("EV_01_UNCO_P", "loadflow.txt", false);
		double l_co01 = findMinOrMaxValueInFile("EV_01_CO", "loadflow.txt", false);
		double l_co1_p01 = findMinOrMaxValueInFile("EV_01_CO1_P", "loadflow.txt", false);
		double l_co2_p01 = findMinOrMaxValueInFile("EV_01_CO2_P", "loadflow.txt", false);
		
		double l_unco28 = findMinOrMaxValueInFile("EV_28_UNCO", "loadflow.txt", false);
		double l_unco_p28 = findMinOrMaxValueInFile("EV_28_UNCO_P", "loadflow.txt", false);
		double l_co28 = findMinOrMaxValueInFile("EV_28_CO", "loadflow.txt", false);
		double l_co1_p28 = findMinOrMaxValueInFile("EV_28_CO1_P", "loadflow.txt", false);
		double l_co2_p28 = findMinOrMaxValueInFile("EV_28_CO2_P", "loadflow.txt", false);
		
		double l_unco46 = findMinOrMaxValueInFile("EV_46_UNCO", "loadflow.txt", false);
		double l_unco_p46 = findMinOrMaxValueInFile("EV_46_UNCO_P", "loadflow.txt", false);
		double l_co46 = findMinOrMaxValueInFile("EV_46_CO", "loadflow.txt", false);
		double l_co1_p46 = findMinOrMaxValueInFile("EV_46_CO1_P", "loadflow.txt", false);
		double l_co2_p46 = findMinOrMaxValueInFile("EV_46_CO2_P", "loadflow.txt", false);
		
		double l_unco64 = findMinOrMaxValueInFile("EV_64_UNCO", "loadflow.txt", false);
		double l_unco_p64 = findMinOrMaxValueInFile("EV_64_UNCO_P", "loadflow.txt", false);
		double l_co64 = findMinOrMaxValueInFile("EV_64_CO", "loadflow.txt", false);
		double l_co1_p64 = findMinOrMaxValueInFile("EV_64_CO1_P", "loadflow.txt", false);
		double l_co2_p64 = findMinOrMaxValueInFile("EV_64_CO2_P", "loadflow.txt", false);*/
		
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.0");
		
//		System.out.println("Peak load data (Case 1)");
//		System.out.println("BASE: " + df.format(base));
//		System.out.println("UNC : " + df.format(unco64) + " " + df.format(unco46) + " " + df.format(unco28) + " " + df.format(unco01));
//		System.out.println("UNCP: " + df.format(unco_p64) + " " + df.format(unco_p46) + " " + df.format(unco_p28) + " " + df.format(unco_p01));
//		System.out.println("CO  : " + df.format(co64) + " " + df.format(co46) + " " + df.format(co28) + " " + df.format(co01));
//		System.out.println("CO1 : " + df.format(co1_p64) + " " + df.format(co1_p46) + " " + df.format(co1_p28) + " " + df.format(co1_p01));
//		System.out.println("CO2 : " + df.format(co2_p64) + " " + df.format(co2_p46) + " " + df.format(co2_p28) + " " + df.format(co2_p01));
//		System.out.println();
//		System.out.println("Load flow data (Case 1)");
//		System.out.println("BASE: " + df.format(l_base));
//		System.out.println("UNC : " + df.format(l_unco64) + " " + df.format(l_unco46) + " " + df.format(l_unco28) + " " + df.format(l_unco01));
//		System.out.println("UNCP: " + df.format(l_unco_p64) + " " + df.format(l_unco_p46) + " " + df.format(l_unco_p28) + " " + df.format(l_unco_p01));
//		System.out.println("CO  : " + df.format(l_co64) + " " + df.format(l_co46) + " " + df.format(l_co28) + " " + df.format(l_co01));
//		System.out.println("CO1 : " + df.format(l_co1_p64) + " " + df.format(l_co1_p46) + " " + df.format(l_co1_p28) + " " + df.format(l_co1_p01));
//		System.out.println("CO2 : " + df.format(l_co2_p64) + " " + df.format(l_co2_p46) + " " + df.format(l_co2_p28) + " " + df.format(l_co2_p01));
		
//		double l2_base = findMinOrMaxValueInFile("EV2_Base", "loadflow.txt", false);
//		double l2_unco01 = findMinOrMaxValueInFile("EV2_01_UNCO", "loadflow.txt", false);
//		double l2_unco_p01 = findMinOrMaxValueInFile("EV2_01_UNCO_P", "loadflow.txt", false);
//		double l2_co01 = findMinOrMaxValueInFile("EV2_01_CO", "loadflow.txt", false);
//		double l2_co1_p01 = findMinOrMaxValueInFile("EV2_01_CO1_P", "loadflow.txt", false);
//		double l2_co2_p01 = findMinOrMaxValueInFile("EV2_01_CO2_P", "loadflow.txt", false);
		
//		System.out.println();
//		System.out.println("Load flow data (Case 2)");
//		System.out.println("BASE: " + df.format(l2_base));
//		System.out.println("UNC : " + df.format(l2_unco01));
//		System.out.println("UNCP: " + df.format(l2_unco_p01));
//		System.out.println("CO  : " + df.format(l2_co01));
//		System.out.println("CO1 : " + df.format(l2_co1_p01));
//		System.out.println("CO2 : " + df.format(l2_co2_p01));
		
		double l3_base10 = findMinOrMaxValueInFile("Final_Base10", "loadflow.txt", false);
		double l3_base30 = findMinOrMaxValueInFile("Final_Base30", "loadflow.txt", false);
		double l3_base50 = findMinOrMaxValueInFile("Final_Base50", "loadflow.txt", false);
		double l3_base100 = findMinOrMaxValueInFile("Final_Base100", "loadflow.txt", false);
		double l3_dlc20 = findMinOrMaxValueInFile("Final_DLC20", "loadflow.txt", false);
		double l3_dlc40 = findMinOrMaxValueInFile("Final_DLC40", "loadflow.txt", false);
		double l3_dlc60 = findMinOrMaxValueInFile("Final_DLC60", "loadflow.txt", false);
		double l3_clc20 = findMinOrMaxValueInFile("Final_CLC20", "loadflow.txt", false);
		double l3_clc40 = findMinOrMaxValueInFile("Final_CLC40", "loadflow.txt", false);
		double l3_clc60 = findMinOrMaxValueInFile("Final_CLC60", "loadflow.txt", false);
		double l3_dlc20_100 = findMinOrMaxValueInFile("Final_DLC20_100", "loadflow.txt", false);
		double l3_dlc40_100 = findMinOrMaxValueInFile("Final_DLC40_100", "loadflow.txt", false);
		double l3_dlc60_100 = findMinOrMaxValueInFile("Final_DLC60_100", "loadflow.txt", false);
		double l3_clc20_100 = findMinOrMaxValueInFile("Final_CLC20_100", "loadflow.txt", false);
		double l3_clc40_100 = findMinOrMaxValueInFile("Final_CLC40_100", "loadflow.txt", false);
		double l3_clc60_100 = findMinOrMaxValueInFile("Final_CLC60_100", "loadflow.txt", false);
		
		System.out.println();
		System.out.println("Load flow data (Last paper)");
		System.out.println("BASE10: " + df.format(l3_base10));
		System.out.println("BASE30: " + df.format(l3_base30));
		System.out.println("BASE50: " + df.format(l3_base50));
		System.out.println("BASE100: " + df.format(l3_base100));
		System.out.println("DLC20 : " + df.format(l3_dlc20));
		System.out.println("DLC40 : " + df.format(l3_dlc40));
		System.out.println("DLC60 : " + df.format(l3_dlc60));
		System.out.println("CLC20 : " + df.format(l3_clc20));
		System.out.println("CLC40 : " + df.format(l3_clc40));		
		System.out.println("CLC60 : " + df.format(l3_clc60));	
		System.out.println("DLC20_100 : " + df.format(l3_dlc20_100));
		System.out.println("DLC40_100 : " + df.format(l3_dlc40_100));
		System.out.println("DLC60_100 : " + df.format(l3_dlc60_100));
		System.out.println("CLC20_100 : " + df.format(l3_clc20_100));
		System.out.println("CLC40_100 : " + df.format(l3_clc40_100));		
		System.out.println("CLC60_100 : " + df.format(l3_clc60_100));	
		System.out.println();
	
		System.out.println("EV SoCs");
		String[] cases = {"Final_DLC20", "Final_DLC40", "Final_DLC60", "Final_CLC20", "Final_CLC40", "Final_CLC60",
				"Final_DLC20_100", "Final_DLC40_100", "Final_DLC60_100", "Final_CLC20_100", "Final_CLC40_100", "Final_CLC60_100"};
		for(int s=0; s<cases.length; s++) {
			System.out.println(cases[s]);
			for(int h=0; h<50; h++) {
				double min = 90;
				for(int d=1; d<=31; d++) {
					double soc = writeSoC(cases[s], h, d);
					if(soc<min) {
						min = soc;
					}
				}
				if(min!=-1) System.out.println(h + "," + min);
			}
			System.out.println();
		}
		
		System.out.println("ACCUMULATED COST OVER 1 MONTH FOR 30 HOUSES");
		System.out.println("Base50"); System.out.println(accumulateCost("Final_Base50", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC20"); System.out.println(accumulateCost("Final_DLC20", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC20"); System.out.println(accumulateCost("Final_CLC20", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC40"); System.out.println(accumulateCost("Final_DLC40", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC40"); System.out.println(accumulateCost("Final_CLC40", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC60"); System.out.println(accumulateCost("Final_DLC60", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC60"); System.out.println(accumulateCost("Final_CLC60", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		
		System.out.println("Base100"); System.out.println(accumulateCost("Final_Base100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC20_100"); System.out.println(accumulateCost("Final_DLC20_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC20_100"); System.out.println(accumulateCost("Final_CLC20_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC40_100"); System.out.println(accumulateCost("Final_DLC40_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC40_100"); System.out.println(accumulateCost("Final_CLC40_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("DLC60_100"); System.out.println(accumulateCost("Final_DLC60_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println("CLC60_100"); System.out.println(accumulateCost("Final_CLC60_100", new int[]{1,4,10,11,18,20,24,31,35,42,43,13,41,15,5,37,25,17,28,33,47,30,7,21,48,38,26,39,20,14}));
		System.out.println();
		
		System.out.println("NUMBER OF TIMES DSO CONTROL SIGNALS ARE SENT");
		System.out.println("DLC20"); System.out.println(countDSOLoadControlTimes("Final_DLC20", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("DLC40"); System.out.println(countDSOLoadControlTimes("Final_DLC40", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("DLC60"); System.out.println(countDSOLoadControlTimes("Final_DLC60", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC20"); System.out.println(countDSOLoadControlTimes("Final_CLC20", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC40"); System.out.println(countDSOLoadControlTimes("Final_CLc40", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC60"); System.out.println(countDSOLoadControlTimes("Final_CLC60", new int[]{1,4,10,11,18,20,24,31,35,42}));
		
		System.out.println("DLC20_100"); System.out.println(countDSOLoadControlTimes("Final_DLC20_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("DLC40_100"); System.out.println(countDSOLoadControlTimes("Final_DLC40_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("DLC60_100"); System.out.println(countDSOLoadControlTimes("Final_DLC60_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC20_100"); System.out.println(countDSOLoadControlTimes("Final_CLC20_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC40_100"); System.out.println(countDSOLoadControlTimes("Final_CLC40_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
		System.out.println("CLC60_100"); System.out.println(countDSOLoadControlTimes("Final_CLC60_100", new int[]{1,4,10,11,18,20,24,31,35,42}));
	}

	/*
	 * This one is used to aggregate some stats related to the ev schedule which
	 * is included in the EV paper submitted to SmartGridComm
	 */
/*	public static void writeFeederSchedule(String folder) throws IOException {
		System.out.println(folder);

		BufferedReader br = null;
		double[] totalVal = new double[27];

		for (int i = 0; i < 10; i++) {
			double[] values = new double[27];

			String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H"
					+ (i + 1) + "/EV_schedule.txt";
			br = new BufferedReader(new FileReader(path));

			int line = 1;
			br.readLine();
			br.readLine();

			String sCurrentLine;
			int valIndex = 0;
			while ((sCurrentLine = br.readLine()) != null
					&& !sCurrentLine.isEmpty()) {
				if (line == 3) {
					String[] split = sCurrentLine.split(", ");
					for (int j = 264; j < split.length; j = j + 4) {
						double agg = 0;
						for (int k = 0; k < 4; k++) {
							double value = Math.abs(Double.parseDouble(split[j
									+ k]));
							if (value > agg)
								agg = value;
						}
						values[valIndex] = agg;
						valIndex++;
					}
				} else if (line == 4) {
					String[] split = sCurrentLine.split(", ");
					for (int j = 0; j < 84; j = j + 4) {
						double agg = 0;
						for (int k = 0; k < 4; k++) {
							double value = Math.abs(Double.parseDouble(split[j
									+ k]));
							if (value > agg)
								agg = value;
						}
						values[valIndex] = agg;
						valIndex++;
					}
				}

				line++;
				if (line > 4)
					break;
			}

			for (int t = 0; t < values.length; t++) {
				totalVal[t] += values[t];
			}
		}

		for (int s = 0; s < totalVal.length; s++) {
			System.out.print(((int) totalVal[s]) + " ");
		}
		System.out.println();
	}*/

	public static void writeFeederSchedule(String folder) throws IOException {
		System.out.println(folder);

		BufferedReader br = null;

		double[] numHousesInFeeders = new double[]{7,7,6,6,6,6,6,6};
		//double[] numHousesInFeeders = new double[]{8};
		
		int house = 0;
		for (int feeder = 0; feeder < numHousesInFeeders.length; feeder++) {
			System.out.println("Feeder " + feeder);
			
			double[][] allVal = new double[31][288];
			for(int h=0; h<numHousesInFeeders[feeder]; h++) {
				int lv2 = -1;
				String f = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H" + (house + 1) + "/EV_peakload.txt";
				
				if(!new File(f).exists()) {
					house++;
					continue;
				}
				
				BufferedReader br_f = new BufferedReader(new FileReader(f));
				String l;
				br_f.readLine(); br_f.readLine();
				while((l = br_f.readLine())!=null) {
					String[] split = l.split(", ");
					for(int i=0;i<split.length;i++) {
						if(Double.parseDouble(split[i])==1100) {
							lv2 = 0;
							break;
						} else if(Double.parseDouble(split[i])>=6600){ 
							lv2 = 1;
							break;
						}
					}
					if(lv2!=-1) break;
				}
				
				if(lv2==0) {
					house++;
					continue;
				}
				
				String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H"
						+ (house++ + 1) + "/EV_schedule.txt";
				br = new BufferedReader(new FileReader(path));
	
				br.readLine();
				br.readLine();
	
				String sCurrentLine;
				int valIndex = 0;
				int line = 0;
				
				while ((sCurrentLine = br.readLine()) != null
						&& !sCurrentLine.isEmpty()) {
						String[] split = sCurrentLine.split(", ");
						for (int j = 0; j < split.length; j++) {
							double value = Math.abs(Double.parseDouble(split[j]));
							allVal[line][valIndex] += value;
							valIndex++;
						}
						line++;
						valIndex=0;
				}
			}
			
			double max = 0;
			for(int day=0; day<31; day++) {
				for(int i=0; i<288;i=i+4) {
					for(int j=0; j<4; j++) {
						double v = allVal[day][i+j];
						if(v>max) max = v;
					}
					System.out.print(((int)max) + " ");
					max = 0;
				}
				System.out.println();
			}
		}
	}
	
	/*
	 * Used this one to estimate the relative heating demand by the hour so that we can use this to subtract heating demand
	 * from the total load in order to obtain the base load
	 */
	public static void writeRelativeHeatingDemand() {
		String path = "TEMP/H1/Household_peakload.txt";
		String target = "data/load/RD.txt";
		BufferedReader br = null;
		PrintWriter pw = null;
		DecimalFormat df = (DecimalFormat)
		        NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		try {
			File fileTarget = new File(target);
			if (fileTarget.exists())
				fileTarget.delete();
			fileTarget.createNewFile();

			br = new BufferedReader(new FileReader(path));
			FileWriter fw = new FileWriter(fileTarget, true);
			pw = new PrintWriter(fw);
			String sCurrentLine;
			br.readLine();
			br.readLine();
			while ((sCurrentLine = br.readLine()) != null
					&& !sCurrentLine.isEmpty()) {
				String toWrite = "1,1,1,1,1,1,1,1,1,1,1,1";
				String[] wattStr = sCurrentLine.split(",");
				double firstDemand = Double.parseDouble(wattStr[0]);
				for (int i = 1; i < wattStr.length; i++) {
					String nxtHour = df.format(Double.parseDouble(wattStr[i])
							/ firstDemand);
					for(int j=0; j<12; j++)
						toWrite += "," + nxtHour;
				}

				pw.println(toWrite);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (pw != null)
					pw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
	
	public static double findMinOrMaxValueInFile(String folder, String file, boolean max) throws IOException{
		String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/" + file;
		BufferedReader br = new BufferedReader(new FileReader(path));	
		
		String sCurrentLine;
		double minMax = (max?0:9999);
		while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
			String[] values = sCurrentLine.split(",");
			
			if(values.length>0) {
				for(int i=0; i<values.length; i++) {
					double value = Double.parseDouble(values[i]);
					if(max && value>minMax) minMax = value;
					else if(!max && value<minMax) minMax = value;
					
				}
			} else {
				double value = Double.parseDouble(sCurrentLine);
				if(max && value>minMax) minMax = value;
				else if(!max && value<minMax) minMax = value;
			}
		}
		
		br.close();
		
		return minMax;
	}
	
	public static double writeSoC(String folder, int houseID, int day) throws IOException{
		double value = 90;
		
		String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H" + (houseID+1) + "/EV_soc.txt";
		
		if(!new File(path).exists()) return -1;
		
		BufferedReader br = new BufferedReader(new FileReader(path));	
		
		String sCurrentLine;
		br.readLine();
		br.readLine();
		
		int startSec = 86400 * (day-2) + (86400/2);
		int endSec = 86400 * (day-1) + (86400/2);
		
		while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
			String[] values = sCurrentLine.split(" ");
			
			if(Double.parseDouble(values[0]) >= startSec && Double.parseDouble(values[0])<endSec) {
				//System.out.println("House#" + houseID + " on day " + day + ": " + values[1]);
				value = Double.parseDouble(values[1]);
				break;
			}
		}
		br.close();		
		return value;
	}
	
	public static double accumulateCost(String folder, int[] houseIDs) throws IOException{
		double cost = 0;
		BufferedReader br = null;
		
		for(int i=0; i<houseIDs.length; i++) {
			String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H" + (houseIDs[i] + 1) + "/Household_cost.txt";
			br = new BufferedReader(new FileReader(path));	
			
			String sCurrentLine;
			br.readLine();
			br.readLine();
			
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				String[] values = sCurrentLine.split(", ");
				
				for(int j=0; j<values.length; j++) {
					cost += Double.parseDouble(values[j]);
				}
			}
			br.close();
		}
		
		return cost;
	}
	
	public static int countDSOLoadControlTimes(String folder, int[] houseIDs) throws IOException {
		int times = 0;
		BufferedReader br = null;
		
		for(int i=0; i<houseIDs.length; i++) {
			String path = "Y:/Workspaces/SMASH/SMASH/" + folder + "/H" + (houseIDs[i] + 1) + "/DSOControl.log";
			br = new BufferedReader(new FileReader(path));	
			
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null && !sCurrentLine.isEmpty()) {
				times++;
			}
			br.close();
		}
		
		return times;
	}
}

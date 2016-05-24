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

public class LoadDataExtractor {

	public static final String SRC_PATH = "rawdata/load/";
	public static final String DEST_PATH = "data/load/";
	
	public static void main(String[] args) {
		LoadDataExtractor.parseAndWriteStrict("Buskerudd", 7, 2006, 5);
	}
	
	public static void parseAndWriteStrict(String city, int month, int year, int numCustomer) {
		String fileName = city + "" + year + ".txt";
		
		DecimalFormat df = (DecimalFormat)
		NumberFormat.getNumberInstance(new Locale("en", "US"));
		df.applyPattern("0.00");
		
		BufferedReader br = null;
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(SRC_PATH + fileName));
			String customerID = "";
			int houseID = 1;
			
			while ((sCurrentLine = br.readLine()) != null) {
				String[] data = sCurrentLine.trim().split("\\s");
				if(!data[0].equals(customerID) && data[1].equals(year + "-" + (month<10?"0"+month:month) + "-01") && Double.parseDouble(data[3])>0.8){
					if(houseID>numCustomer) break;
					
					// found the correct starting point
					// start writing new customer's data
					customerID = data[0];
					
					File file = new File(DEST_PATH + "H" + houseID + "/L" + (month<10?"0"+month:month) + year + ".txt");
					if(!file.getParentFile().exists()) file.getParentFile().mkdir();
					if(file.exists()) file.delete();
					file.createNewFile();
					
					try {
						fw = new FileWriter(file,true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(pw!=null) pw.close();
					pw = new PrintWriter(fw);
					String toPrint = "";
					
					for(int i=3; i<data.length; i++) {
						double load = Double.parseDouble(data[i]);
						for(int j=0; j<12; j++) {
							toPrint+=df.format(load) + (j<11?",":"");
						}
						toPrint+=(i<data.length-1?",":"");
					}
					pw.println(toPrint);
					houseID++;
				} else if(data[0].equals(customerID) && data[1].startsWith(year + "-" + (month<10?"0"+month:month))) {
					String toPrint = "";
					for(int i=3; i<data.length; i++) {
						double load = Double.parseDouble(data[i]);
						for(int j=0; j<12; j++) {
							toPrint+=df.format(load) + (j<11?",":"");
						}
						toPrint+=(i<data.length-1?",":"");
					}
					pw.println(toPrint);
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
	}
	
	// resolution is locked to 5 min
	public static void parseAndWrite(String city, int month, int year, int numCustomer) {
		String fileName = city + "" + year + ".txt";
		
		DecimalFormat df = (DecimalFormat)
		NumberFormat.getNumberInstance(new Locale("en", "US"));
		
		BufferedReader br = null;
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(SRC_PATH + fileName));
			String customerID = "";
			int houseID = 1;
			
			while ((sCurrentLine = br.readLine()) != null) {
				String[] data = sCurrentLine.trim().split("\\s");
				if(!data[0].equals(customerID) && data[1].equals(year + "-" + (month<10?"0"+month:month) + "-01") && Double.parseDouble(data[3])>10 && Double.parseDouble(data[3])<15){
					if(houseID>numCustomer) break;
					
					// found the correct starting point
					// start writing new customer's data
					customerID = data[0];
					
					File file = new File(DEST_PATH + "H" + houseID + "/L" + (month<10?"0"+month:month) + year + ".txt");
					if(!file.getParentFile().exists()) file.getParentFile().mkdir();
					if(file.exists()) file.delete();
					file.createNewFile();
					
					try {
						fw = new FileWriter(file,true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(pw!=null) pw.close();
					pw = new PrintWriter(fw);
					String toPrint = "";
					
					for(int i=3; i<data.length; i++) {
						double load = Double.parseDouble(data[i]);
						double next = i<data.length-1?Double.parseDouble(data[i+1]):load;
						
						// 0
						double rand = Math.random();
						double index0 = load; double index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3){ index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 1
						toPrint += df.format(index1) + ",";
						// 2
						toPrint += df.format((load - 1)) + ",";
						// 3
						toPrint += df.format((load - 1)) + ",";
						// 4
						rand = Math.random();
						index0 = load - ((next-load)/2); index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 5
						toPrint += df.format(index1) + ",";
						// 6
						toPrint += df.format(load) + ",";
						// 7
						toPrint += df.format(load) + ",";
						// 8
						rand = Math.random();
						index0 = load + 1; index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 9
						toPrint += df.format(index1) + ",";
						// 10
						rand = Math.random();
						index0 = load + ((next-load)/2); index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 11
						toPrint += df.format(index1) + (i<data.length-1?",":"");
					}
					pw.println(toPrint);
					houseID++;
				} else if(data[0].equals(customerID) && data[1].startsWith(year + "-" + (month<10?"0"+month:month))) {
					String toPrint = "";
					for(int i=3; i<data.length; i++) {
						double load = Double.parseDouble(data[i]);
						double next = i<data.length-1?Double.parseDouble(data[i+1]):load;
						
						// 0
						double rand = Math.random();
						double index0 = load; double index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 1
						toPrint += df.format(index1) + ",";
						// 2
						toPrint += df.format((load - 1)) + ",";
						// 3
						toPrint += df.format((load - 1)) + ",";
						// 4
						rand = Math.random();
						index0 = load - ((next-load)/2); index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 5
						toPrint += df.format(index1) + ",";
						// 6
						toPrint += df.format(load) + ",";
						// 7
						toPrint += df.format(load) + ",";
						// 8
						rand = Math.random();
						index0 = load + 1; index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 9
						toPrint += df.format(index1) + ",";
						// 10
						rand = Math.random();
						index0 = load + ((next-load)/2); index1 = index0;
						if(load>3 && Math.random()<0.5) { index0-=rand; index1+=rand; }
						else if(load>3) { index0+=rand; index1-=rand; }
						toPrint += df.format(index0) + ",";
						// 11
						toPrint += df.format(index1) + (i<data.length-1?",":"");
					}
					pw.println(toPrint);
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
		
	}
	
}

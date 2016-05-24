package no.ntnu.item.smash.sim.pss.loadflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.util.EList;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.aclf.adpter.AclfLine;
import com.interpss.core.aclf.adpter.AclfSwingBus;
import com.interpss.core.algo.LoadflowAlgorithm;

public class LVNetwork {

	private AclfNetwork network;
	private ArrayList<AclfBus> busses = new ArrayList<AclfBus>();
	private int[] housesInFeeder;

//	public static double[] km = {0.03,0.03,0.035,0.02,0.035,0.035,0.03,0.025,0.04,0.035,0.025,0.03,0.04,0.035,0.025,0.03};
	public static double kmMid = 0.08;
	public static double[] km = {0.031,0.032,0.025,0.021,0.04,0.025,0.03, // Feeder #1
		0.04,0.027,0.021,0.03,0.021,0.042,0.026, // Feeder #2
		0.029,0.03,0.029,0.027,0.042,0.027, // Feeder #3
		0.027,0.032,0.025,0.032,0.027,0.024, // Feeder #4
		0.036,0.025,0.03,0.025,0.029,0.02, // Feeder #5
		0.037,0.03,0.023,0.018,0.023,0.031, // Feeder #6
		0.041,0.03,0.018,0.026,0.022,0.025, // Feeder #7
		0.034,0.027,0.028,0.031,0.025,0.24}; // Feeder #8
	public static double resistancePerKm = 0.125; //PFSP 3x240 Al / 70
	public static double reactancePerKm = 0.072; //PFSP 3x240 Al / 70
	
//	public static double[] km = {0.03,0.03,0.035,0.02,0.035,0.035,0.03,0.025,0.04,0.035,0.025,0.03,0.04,0.035,0.025,0.03};
//	public static double resistancePerKm = 0.153; //PFSP 3x120 Cu / 70
//	public static double reactancePerKm = 0.074; //PFSP 3x120 Cu / 70
	
	// Config parameters
	private double baseVoltage = 230.0;

	public static void main(String args[]) {
		IpssCorePlugin.init();
		LVNetwork net = new LVNetwork(500, 230);
		net.createBuses(8, 50);
		LVNetwork.runLoadFlow(net.getAclfNetwork());
	}
	
	public LVNetwork(int baseKva, int baseVoltage) {
		network = CoreObjectFactory.createAclfNetwork();
		this.baseVoltage = baseVoltage;
		
		// set system basekva for loadflow calculation
		network.setBaseKva(baseKva);
	}
	
	public void createBuses(int numFeeder, int numHouse) {
		// Transformer
		try {
			// create a AclfBus object
			AclfBus transformer = CoreObjectFactory.createAclfBus("Bus0", network);
			// set bus base voltage 
			transformer.setBaseVoltage(baseVoltage);
			// set bus to be a swing bus
			transformer.setGenCode(AclfGenCode.SWING);
			// adapt the bus object to a swing bus object
			AclfSwingBus swingBus = transformer.toSwingBus();
			// set swing bus attributes
			swingBus.setDesiredVoltMag(1.0, UnitType.PU);
			swingBus.setDesiredVoltAng(0.0, UnitType.Deg);
			
			busses.add(transformer);
		} catch (InterpssException e1) {
			e1.printStackTrace();
		}
		
		// create the bus in-between the transformer and other feeders
		try {
			AclfBus busMid = CoreObjectFactory.createAclfBus("BusMid", network);
			busMid.setBaseVoltage(baseVoltage);
			busMid.setGenCode(AclfGenCode.NON_GEN);
			busMid.setLoadCode(AclfLoadCode.CONST_P);
	  		busses.add(busMid);
	  		
	  		AclfBranch branch = CoreObjectFactory.createAclfBranch();
  	  		network.addBranch(branch, "Bus0", "BusMid");
  	  		// set branch name, description and circuit number
  	  		branch.setAttributes("Branch 0_Mid", "", "0");
  	  		
  	  		// set branch to a Line branch
  	  		branch.setBranchCode(AclfBranchCode.LINE);
  	  		
  	  		// adapt the branch object to a line branch object
  			AclfLine lineBranch = branch.toLine();
  			
  			// set branch parameters
  	  		lineBranch.setZ(new Complex(resistancePerKm*kmMid, reactancePerKm*kmMid), UnitType.PU, baseVoltage);
		} catch (InterpssException e) {
			e.printStackTrace();
		}
  		
		// determine the number of houses in each feeder and create buses
		housesInFeeder = new int[numFeeder];
		int inEachFeeder = numHouse/numFeeder;
		int remain = numHouse - (inEachFeeder*numFeeder);
		while(remain>0) {
			for(int i=0; i<numFeeder; i++) {
				if(remain>0) {
					housesInFeeder[i] += 1;
					remain--;
				} else break;
			}
		}
		
		int houseIndex = 1;
		for(int i=0; i<numFeeder; i++) {
			housesInFeeder[i] += inEachFeeder;
			System.out.println("Feeder #" + (i+1) + ": " + housesInFeeder[i] + " houses");
			
			// create buses along this feeder
			for(int h=1; h<=housesInFeeder[i]; h++) {
				try {
					// BUS
					AclfBus bus = CoreObjectFactory.createAclfBus("Bus" + houseIndex, network);
					
					// set bus base voltage 
			  		bus.setBaseVoltage(baseVoltage);
			  		
			  		// set the bus to a non-generator bus
			  		bus.setGenCode(AclfGenCode.NON_GEN);
			  		
			  		// set bus load
			  		bus.setLoadCode(AclfLoadCode.CONST_P);
			  		
			  		busses.add(bus);
			  		
			  		// BRANCH
			  		// connect this bus with the previous bus (with a cable)
		  			// create an AclfBranch object
		  			AclfBranch branch = CoreObjectFactory.createAclfBranch();
		  	  		network.addBranch(branch, "Bus" + (h==1?"Mid":houseIndex-1), "Bus" + houseIndex);
		  	  		
		  	  		// set branch name, description and circuit number
		  	  		branch.setAttributes("Branch " + (h==1?"Mid":houseIndex-1) + "_" + (houseIndex), "", ""+houseIndex);
		  	  		
		  	  		// set branch to a Line branch
		  	  		branch.setBranchCode(AclfBranchCode.LINE);
		  	  		
		  	  		// adapt the branch object to a line branch object
		  			AclfLine lineBranch = branch.toLine();
		  			
		  			// set branch parameters
		  	  		lineBranch.setZ(new Complex(resistancePerKm*km[houseIndex-1], reactancePerKm*km[houseIndex-1]), UnitType.PU, baseVoltage);
		  	  				  	  		
		  	  		//System.out.println("[CREATED] Bus" + houseIndex + " connected via Branch" + (h==1?"Mid":houseIndex-1) + "_" + (houseIndex) + " with Bus" + (h==1?"Mid":houseIndex-1));
		  	  		houseIndex++;
				} catch (InterpssException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public AclfNetwork getAclfNetwork() {
		return network;
	}
	
	public AclfBus getAclfBus(int id) {
		return busses.get(id);
	}
	
	public int[] getNumHousesInFeeders() {
		return housesInFeeder;
	}
	
	public static void runLoadFlow(AclfNetwork net) {
		// create the default loadflow algorithm
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	try {
			algo.loadflow();	
		} catch (InterpssException e) {
			e.printStackTrace();
		} 
	  	//System.out.println(AclfOutFunc.loadFlowSummary(net));
	}
	
	public static void writeLoadFlowSummaryToFile(AclfNetwork net, String path) {
		EList<AclfBus> buses = net.getBusList();
		String intervalResult = "";
		int baseVoltage = (int)buses.get(0).getBaseVoltage();
		for(int i=0; i<buses.size(); i++) {
			intervalResult += "" + (buses.get(i).getVoltage().getReal()*baseVoltage);
			if(i<buses.size()-1) intervalResult += ",";
		}
		intervalResult += "\n";
		
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(path);
			// if file doesnt exists, then create it
			if (!f.exists()) { 
				f.createNewFile();
			}
			
			fop = new FileOutputStream(f, true);			

			// get the content in bytes
			fop.write(intervalResult.getBytes());
			
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeLoadsToFile(double[] load, String path) {
		String loadString = "";
		for(int i=0; i<load.length; i++) {
			loadString += "" + load[i];
			if(i<load.length-1) loadString += ",";
		}
		loadString += "\n";
		
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(path);
			// if file doesnt exists, then create it
			if (!f.exists()) { 
				f.createNewFile();
			}
			
			fop = new FileOutputStream(f, true);			

			// get the content in bytes
			fop.write(loadString.getBytes());
			
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeGridPeakLoadToFile(double[] load, String path) {
		String loadString = "";
		double sum = 0;
		for(int i=0; i<load.length; i++) {
			sum += load[i];
		}
		loadString = "" + sum + "\n";
		
		FileOutputStream fop = null;
		File f;

		try {
			f = new File(path);
			// if file doesnt exists, then create it
			if (!f.exists()) { 
				f.createNewFile();
			}
			
			fop = new FileOutputStream(f, true);			

			// get the content in bytes
			fop.write(loadString.getBytes());
			
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

package no.ntnu.item.smash.sim.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.ntnu.item.smash.css.structure.Trigger;
import no.ntnu.item.smash.sim.data.stats.HouseholdStatistics;
import no.ntnu.item.smash.sim.data.utility.CSSConfigUtility;
import no.ntnu.item.smash.sim.data.utility.PeakloadDataUtility;
import no.ntnu.item.smash.sim.data.utility.SimConfigUtility;
import no.ntnu.item.smash.sim.data.utility.SimEnvironmentGenerator;
import no.ntnu.item.smash.sim.externalsp.CEMS;
import no.ntnu.item.smash.sim.externalsp.DSO;
import no.ntnu.item.smash.sim.model.DeviceModel;
import no.ntnu.item.smash.sim.pss.loadflow.LVNetwork;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.EVLiionBattery;

import org.interpss.IpssCorePlugin;

public class ComplexSimulation {

	// simulation
	private int simulationTime;
	private int startDay;
	private int startMonth;
	private int startYear;
	private int numHouse;

	private EventManager eventMan;
	private ExecutorService executorService;
	
	public ComplexSimulation(int simTime, int startDay, int startMonth,
			int startYear, int numHouse, int concurrentHouses, int baseKva, int nominalVoltage,
			int numFeeders, double[] patternProb, String resultDir, boolean useCEMS, int cemsScheme,
			String patternFolder, String policyPath, String histLoadFlowPath, String histPeakLoadPath,
			String subscriptionPath, HashMap<SimConfigUtility.UseDefault, Boolean> useDefault) {
		try {
			File policyLog = new File("./log/log.log");
			if(policyLog.exists()) policyLog.delete(); 
			policyLog.createNewFile();
			HouseholdStatistics.emptyDirectory(resultDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.simulationTime = simTime*86400;
		this.startDay = startDay;
		this.startMonth = startMonth;
		this.startYear = startYear;
		this.numHouse = numHouse;

		executorService = Executors.newFixedThreadPool(1000);
		
		// create LV network
		IpssCorePlugin.init();
		LVNetwork network = new LVNetwork(baseKva, nominalVoltage);
		network.createBuses(numFeeders, numHouse);
		
		// create CEMS (Feeder Controller) if needed
		CEMS cems = null;
		if (useCEMS) {
			cems = new CEMS(numHouse, network.getNumHousesInFeeders(), histLoadFlowPath);
			cems.setEVChargeAlgorithm(cemsScheme);
		}
		
		// create simulation environment (create devices and register/activate policies)
		ArrayList<SimulationModel> houseModels = setupEnvironment(cems, resultDir, patternFolder, patternProb, policyPath, subscriptionPath, useDefault);
		if(cems!=null) cems.setCustomers(houseModels);

		// create DSO
		DSO dso = new DSO(houseModels);
		
		// create EventManager for keeping track of time and events
		eventMan = new EventManager(houseModels, simTime / 300, concurrentHouses, network, dso,
				resultDir, histPeakLoadPath.isEmpty()?null:PeakloadDataUtility.readHistoricPeakLoad(startMonth, startYear, histPeakLoadPath));		
		
		
		int[] housesInFeeders = network.getNumHousesInFeeders();
		int house = 0, houseID = 0;
		for(int i=0; i<housesInFeeders.length; i++) {
			if(house<housesInFeeders[i]) {
				houseModels.get(houseID++).setFeederID(i);
				i--;
				house++;
			} else {
				house = 0;
			}
		}
	}

	public ArrayList<SimulationModel> setupEnvironment(CEMS cems,
			String resultDir, String patternFolder, double[] patternProb, String policyPath, String subscriptionPath, HashMap<SimConfigUtility.UseDefault, Boolean> useDefault) {
		ArrayList<SimulationModel> houseModels = new ArrayList<SimulationModel>();
		
		// any stochastic variables go here
		// 1- house type (size, devices, etc.)
		int[] assignedPatterns = SimEnvironmentGenerator
				.generateHouseTypeAssignments(patternProb, numHouse);
		
		// 2- read subscriptions
		int[] contracts = SimEnvironmentGenerator.parseSubscribedContracts(numHouse, subscriptionPath);
		
		// 3-generate houses and models
		for (int i = 0; i < numHouse; i++) {
			SimulationModel houseModel = new SimulationModel(
					SimEnvironmentGenerator.generate(i, simulationTime,
							startDay, startMonth, startYear,
							assignedPatterns[i], patternFolder, policyPath, useDefault), simulationTime, 300,
					startDay, startMonth, startYear, resultDir + "/H" + (i + 1));
			houseModel.setModelID(i);
			
			HashMap<Device,DeviceModel> dms = houseModel.getEnvironment().getDeviceModelMap();
			for(Device dv:dms.keySet()) {
				dms.get(dv).setSimParent(houseModel);
				if(dv.getType()==Device.DEVTYPE_EVCHARGER) {
					((EVLiionBattery)dv).setCanChargeLV2(true);
				}
			}
			
			houseModel.setCEMS(cems);
			houseModel.setExecutorService(executorService);
			houseModel.setContract(contracts[i]);
			
			// read css.cfg for the house
			ArrayList<Object[]> config = CSSConfigUtility.readCSSConfigForHouse(policyPath, i);
			if(config.size()==0) {
				houseModel.setCSSSuspended(true);
			} else {
				houseModel.setCSSSuspended(false);
				// create some reasoning machines
				no.ntnu.item.smash.css.core.SystemContext context = new no.ntnu.item.smash.css.core.SystemContext(
						houseModel);
				
				for(Object[] cfg:config) {
					context.getPgc().registerPolicy(policyPath +
							"/policies" + (i + 1)
									+ "/" + cfg[0] + ".xet.xml", (String)cfg[1], (String)cfg[0]);
		
					context.getPgc().registerPolicyTrigger((String)cfg[0], (Trigger)cfg[2]);
					if(((Integer)cfg[3])==1) context.getPgc().activatePolicy((String)cfg[0]);
				}
				houseModel.setCSS(context);
			}
			houseModel.setCSSSuspended(true);
			houseModels.add(houseModel);
		}
		
		return houseModels;
	}
	
	public synchronized void runHouse() {
		eventMan.start();
	}

}

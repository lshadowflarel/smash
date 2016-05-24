package no.ntnu.item.smash.sim.main;

import java.util.HashMap;

import no.ntnu.item.smash.sim.core.ComplexSimulation;
import no.ntnu.item.smash.sim.data.utility.SimConfigUtility;

public class Main {

	public static void main(String[] args) {
		if(args.length>0) new Main(args[0]);
		else new Main("");
	}
	
	public Main(String configFilePath) {
		// read configuration from conf/simulation.conf
		SimConfigUtility configuration = new SimConfigUtility(configFilePath);
		try {
			int startDay = configuration.getIntegerParam(SimConfigUtility.CONFIG_SIM_START_DAY);
			int startMonth = configuration.getIntegerParam(SimConfigUtility.CONFIG_SIM_START_MONTH);
			int startYear = configuration.getIntegerParam(SimConfigUtility.CONFIG_SIM_START_YEAR);
			int durationDays = configuration.getIntegerParam(SimConfigUtility.CONFIG_SIM_DURATION_DAYS);
			int numHouses = configuration.getIntegerParam(SimConfigUtility.CONFIG_NUM_HOUSES);
			int numConcurrentHouses = configuration.getIntegerParam(SimConfigUtility.CONFIG_CONCURRENT_HOUSES);
			int numFeeders = configuration.getIntegerParam(SimConfigUtility.CONFIG_NUM_FEEDERS);
			int nominalVoltage = configuration.getIntegerParam(SimConfigUtility.CONFIG_NOMINAL_VOLTAGE);
			int baseKva = configuration.getIntegerParam(SimConfigUtility.CONFIG_BASE_KVA);
			boolean useCEMS = configuration.getBooleanParam(SimConfigUtility.CONFIG_USE_CEMS);
			int cemsScheme = configuration.getIntegerParam(SimConfigUtility.CONFIG_CEMS_CHARGE_SCHEME);
			double[] patternProb = configuration.getDoubleSetParam(SimConfigUtility.CONFIG_PATTERN_PROB);
			String resultDir = configuration.getStringParam(SimConfigUtility.CONFIG_RESULTS_DIR);
			String subscriptionPath = configuration.getStringParam(SimConfigUtility.CONFIG_SUBSCRIPTION_CONFIG);
			String patternFolder = configuration.getStringParam(SimConfigUtility.CONFIG_PATTERN_FOLDER);
			String policyPath = configuration.getStringParam(SimConfigUtility.CONFIG_POLICY_PATH);
			String histLoadFlowPath = configuration.getStringParam(SimConfigUtility.CONFIG_HIST_LOAD_FLOW_PATH);
			String histPeakLoadPath = configuration.getStringParam(SimConfigUtility.CONFIG_HIST_PEAK_LOAD_PATH);
			String insulation = configuration.getStringParam(SimConfigUtility.CONFIG_INSULATION);
			String entities = configuration.getStringParam(SimConfigUtility.CONFIG_ENTITIES);
			String planLaundry = configuration.getStringParam(SimConfigUtility.CONFIG_PLAN_LAUNDRY);
			String planEV = configuration.getStringParam(SimConfigUtility.CONFIG_PLAN_ROOMTEMP);
			String planRoomTemp = configuration.getStringParam(SimConfigUtility.CONFIG_PLAN_ROOMTEMP);
			String planWaterDemand = configuration.getStringParam(SimConfigUtility.CONFIG_PLAN_WATERDEMAND);
			
			HashMap<SimConfigUtility.UseDefault, Boolean> useDefault = new HashMap<SimConfigUtility.UseDefault, Boolean>();
			useDefault.put(SimConfigUtility.UseDefault.ENUM_INSULATION, insulation.equals("DEFAULT")?true:false);
			useDefault.put(SimConfigUtility.UseDefault.ENUM_ENTITIES, entities.equals("DEFAULT")?true:false);
			useDefault.put(SimConfigUtility.UseDefault.ENUM_LAUNDRY, planLaundry.equals("DEFAULT")?true:false);
			useDefault.put(SimConfigUtility.UseDefault.ENUM_EV, planEV.equals("DEFAULT")?true:false);
			useDefault.put(SimConfigUtility.UseDefault.ENUM_ROOMTEMP, planRoomTemp.equals("DEFAULT")?true:false);
			useDefault.put(SimConfigUtility.UseDefault.ENUM_WATERDEMAND, planWaterDemand.equals("DEFAULT")?true:false);
			
			// create simulation environment with read parameters
			ComplexSimulation simulation = new ComplexSimulation(durationDays, startDay, startMonth, startYear,
					numHouses, numConcurrentHouses, baseKva, nominalVoltage, numFeeders, patternProb, resultDir, 
					useCEMS, cemsScheme, patternFolder, policyPath, histLoadFlowPath, histPeakLoadPath, subscriptionPath, useDefault);
			
			// start the simulation study
			simulation.runHouse();			
		} catch (Exception e) {
			System.out.println("Failed to parse arguments. Check the formats.");
			e.printStackTrace();
		}
	}
	
}

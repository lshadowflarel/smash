package no.ntnu.item.smash.sim.data.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimConfigUtility {

	// properties object
	private Properties configSet = new Properties();
	
	// config file path
	private final String CONFIG_FILE = "./conf/simulation.conf";
	
	// parameter names
	public static final String CONFIG_SIM_START_DAY = "SIM_START_DAY";
	public static final String CONFIG_SIM_START_MONTH = "SIM_START_MONTH";
	public static final String CONFIG_SIM_START_YEAR = "SIM_START_YEAR";
	public static final String CONFIG_SIM_DURATION_DAYS = "SIM_DURATION_DAYS";
	public static final String CONFIG_NUM_HOUSES = "NUM_HOUSES";
	public static final String CONFIG_CONCURRENT_HOUSES = "CONCURRENT_HOUSES";
	public static final String CONFIG_USE_CEMS = "USE_CEMS";
	public static final String CONFIG_CEMS_CHARGE_SCHEME = "CEMS_CHARGE_SCHEME";
	public static final String CONFIG_RESULTS_DIR = "RESULTS_DIR";
	public static final String CONFIG_BASE_KVA = "BASE_KVA";
	public static final String CONFIG_NOMINAL_VOLTAGE = "NOMINAL_VOLTAGE";
	public static final String CONFIG_NUM_FEEDERS = "NUM_FEEDERS";
	public static final String CONFIG_PATTERN_PROB = "PATTERN_PROB";
	public static final String CONFIG_PATTERN_FOLDER = "PATTERN_FOLDER";
	public static final String CONFIG_ENTITIES = "ENTITIES";
	public static final String CONFIG_INSULATION = "INSULATION";
	public static final String CONFIG_POLICY_PATH = "POLICY_PATH";
	public static final String CONFIG_HIST_LOAD_FLOW_PATH = "HIST_LOAD_FLOW_PATH";
	public static final String CONFIG_HIST_PEAK_LOAD_PATH = "HIST_PEAK_LOAD_PATH";
	public static final String CONFIG_SUBSCRIPTION_CONFIG = "SUBSCRIPTION_CONFIG";
	public static final String CONFIG_PLAN_LAUNDRY = "PLAN_LAUNDRY";
	public static final String CONFIG_PLAN_EV = "PLAN_EV";
	public static final String CONFIG_PLAN_ROOMTEMP = "PLAN_ROOMTEMP";
	public static final String CONFIG_PLAN_WATERDEMAND = "PLAN_WATERDEMAND";
	
	// enumerator for parameters that can either be DEFAULT or FILES
	public enum UseDefault {ENUM_INSULATION, ENUM_ENTITIES, ENUM_LAUNDRY, ENUM_EV, ENUM_ROOMTEMP, ENUM_WATERDEMAND};
	
	public SimConfigUtility() {
		loadConfigSet(CONFIG_FILE);
	}
	
	public SimConfigUtility(String filePath) {
		if(!filePath.isEmpty()) loadConfigSet(filePath);
		else loadConfigSet(CONFIG_FILE);
	}
	
	public void loadConfigSet(String filePath) {	
		File configFile = new File(filePath);
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(configFile);
			System.out.println("Loading configurations...");
			configSet.load(inputStream);
			System.out.println("Configurations loaded.");
		} catch (FileNotFoundException e) {
			System.out.println("Configuration file loading failed. Check that the configuration file exists.");
		} catch (IOException e) {
			System.out.println("Configuration file loading failed. Check that the configuration file exists and is in the right format.");
		}
	}
	
	public Properties getConfigSet() {
		return configSet;
	}
	
	public int getIntegerParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName))
			return Integer.parseInt(configSet.getProperty(paramName).trim());
		else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public double getDoubleParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName))
			return Double.parseDouble(configSet.getProperty(paramName).trim());
		else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public String getStringParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName))
			return configSet.getProperty(paramName).trim();
		else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public boolean getBooleanParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName))
			return configSet.getProperty(paramName).trim().equals("YES")?true:false;
		else
			throw new Exception("Parameter not defined. Check conf/simulation.conf");	
	}
	
	public String[] getStringSetParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName)) {			
			return configSet.getProperty(paramName).trim().split(",");
		} else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public int[] getIntegerSetParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName)) {
			String[] stringSet = configSet.getProperty(paramName).trim().split(",");
			int[] integerSet = new int[stringSet.length];
			
			for(int i=0; i<stringSet.length; i++) {
				integerSet[i] = Integer.parseInt(stringSet[i]);
			}
			
			return integerSet;
		} else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public double[] getDoubleSetParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName)) {
			String[] stringSet = configSet.getProperty(paramName).trim().split(",");
			double[] doubleSet = new double[stringSet.length];
			
			for(int i=0; i<stringSet.length; i++) {
				doubleSet[i] = Double.parseDouble(stringSet[i]);
			}
			
			return doubleSet;
		} else 
			throw new Exception("Parameter not defined. Check conf/simulation.conf");
	}
	
	public boolean[] getBooleanSetParam(String paramName) throws Exception{
		if(configSet.containsKey(paramName)) {
			String[] stringSet = configSet.getProperty(paramName).trim().split(",");
			boolean[] booleanSet = new boolean[stringSet.length];
			
			for(int i=0; i<stringSet.length; i++) {
				booleanSet[i] = stringSet[i].equals("YES")?true:false;
			}
			
			return booleanSet;
		} else
			throw new Exception("Parameter not defined. Check conf/simulation.conf");	
	}
}

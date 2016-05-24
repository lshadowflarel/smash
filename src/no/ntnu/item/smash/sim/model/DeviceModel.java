package no.ntnu.item.smash.sim.model;

import java.util.ArrayList;
import java.util.HashMap;

import no.ntnu.item.smash.sim.core.SimulationModel;
import no.ntnu.item.smash.sim.data.Constants;
import no.ntnu.item.smash.sim.data.stats.ComfortStatistics;
import no.ntnu.item.smash.sim.data.stats.EntityStatistics;
import no.ntnu.item.smash.sim.structure.ComfortMeasure;
import no.ntnu.item.smash.sim.structure.ComfortPreference;
import no.ntnu.item.smash.sim.structure.Device;
import no.ntnu.item.smash.sim.structure.PowerReductionRequest;
import no.ntnu.item.smash.sim.structure.Room;

public abstract class DeviceModel implements Runnable{

	protected SimulationModel model;
	protected Device device;
	protected HashMap<ComfortMeasure,ComfortPreference> comfortPrefs = new HashMap<ComfortMeasure,ComfortPreference>();
	protected int currentDay = 1;
	protected int currentMonth = 1;
	protected int currentYear = 2006;
	
	protected Room room;
	protected int syncInterval = 5*60;
	protected int simTime = 24 * 60 * 60; // 1 hour in seconds - 1 time step is 1
										  // second
	
	// statistics
	protected EntityStatistics stat;
	protected ComfortStatistics comfortStat;
	
	protected double[] hourlyEnergy = new double[24*12];
	protected double[] hourlyOTemp = new double[24*12];
	protected double[] hourlyPrice = new double[24*2];
	protected double[] hourlyCost = new double[24*2];
	protected double[] hourlyRTemp = new double[24*12];
	protected double[] hourlyPeakLoad = new double[24*12];
	
	protected double[] dailyEnergy;
	protected double[] dailyRTemp;
	
	protected double[] schedule;
	protected double[] plan;
	protected double[] powerSchedule;
	
	protected ArrayList<Object> comfortMeasureVals = new ArrayList<Object>();
	
	protected boolean isDLCDevice = false;
	protected ArrayList<int[]> controlPeriods = new ArrayList<int[]>();
	protected PowerReductionRequest currentControlReq;
	
	// simulation
	protected boolean run = false;
	
	public DeviceModel(Device device, Room room) {
		this.device = device;
		this.room = room;
	}
	
	public void startSim(int simTime, int syncInterval, int priceChangeInterval) {
		this.simTime = simTime;
		this.syncInterval = syncInterval;
		
		hourlyEnergy = new double[24*3600/syncInterval];
		hourlyOTemp = new double[24*3600/syncInterval];
		hourlyPrice = new double[24*(60/priceChangeInterval)];
		hourlyCost = new double[24*(60/priceChangeInterval)];
		hourlyRTemp = new double[24*3600/syncInterval];
		hourlyPeakLoad = new double[24*3600/syncInterval];
		
		run = true;
	}
	public void stopSim() {
		run = false;
	}
	
	public void setSimParent(SimulationModel model) {
		this.model = model;
	}
	
	public SimulationModel getSimParent() {
		return model;
	}
	
	public abstract void updateSchedule(double[] schedule);
	
	@Override
	public void run() {
		
	}
	
	public EntityStatistics getStatistics() {
		return stat;
	}
	
	public void setStatistics(String path, String name) {
		stat = new EntityStatistics(path, name);
		comfortStat = new ComfortStatistics(path, name);
	}
	
	public Device getDevice() {
		return device;
	}
	
	public Room getRoom() {
		return room;
	}
	
	public void addComfortPreference(ComfortMeasure measure, ComfortPreference pref) {
		comfortPrefs.put(measure, pref);
	}
	
	public ComfortPreference getComfortPreference(ComfortMeasure measure) {
		return comfortPrefs.get(measure);
	}
	
	public HashMap<ComfortMeasure,ComfortPreference> getComfortPreferences() {
		return comfortPrefs;
	}
	
	public int getCurrentDay() {
		return currentDay;
	}
	
	public void setCurrentDay(int currentDay) {
		this.currentDay = currentDay;
	}
	
	public int getCurrentMonth() {
		return currentMonth;
	}
	
	public void setCurrentMonth(int currentMonth) {
		this.currentMonth = currentMonth;
	}
	
	public void setCurrentDate(int currentDay, int currentMonth, int currentYear) {
		this.currentDay = currentDay;
		this.currentMonth = currentMonth;
		this.currentYear = currentYear;
		int daysInMonth = Constants.getNumDaysInMonth(currentMonth, currentYear);
		dailyEnergy = new double[daysInMonth];
		dailyRTemp = new double[daysInMonth];
	}
	
	public int getCurrentYear() {
		return currentYear;
	}
	
	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}
	
	public double[] getEnergyInfo() {
		return hourlyEnergy;
	}
	
	public double[] getPriceInfo() {
		return hourlyPrice;
	}
	
	public double[] getCostInfo() {
		return hourlyCost;
	}
	
	public double[] getOTempInfo() {
		return hourlyOTemp;
	}
	
	public double[] getRTempInfo() {
		return hourlyRTemp;
	}
	
	public double[] getPeakLoadInfo() {
		return hourlyPeakLoad;
	}
	
	public double[] getDailyEnergy() {
		return dailyEnergy;
	}
	
	public double[] getDailyRTemp() {
		return dailyRTemp;
	}
	
	public PowerReductionRequest getCurrentControlRequest() {
		return currentControlReq;
	}
	
	public void setPowerReductionRequest(PowerReductionRequest req) {
		currentControlReq = req;
		isDLCDevice = true;
			
		int startSecond = req.getStartInterval() * syncInterval;
		int endSecond = req.getEndInterval() * syncInterval;
		
		int[] period = {startSecond, endSecond};
		controlPeriods.add(period);
	}
	
	protected boolean isControlPeriod(int second) { 
		if(!isDLCDevice) return false;
		else {
			for(int[] period:controlPeriods) {
				if(second>=period[0] && second<=period[1]) { 
					return true;
				}
			}
		}
		
		return false;
	}
	
	public double sumAllHours(double[] data) {
		double sum = 0;
		for(double d:data) {
			sum+=d;
		}
			
		return sum;
	}
	
	public double[] getPlan() {
		return plan;
	}
	
	public double[] getSchedule() {
		return schedule;
	}
	
	public double[] getPowerSchedule() {
		return powerSchedule;
	}
	
	public ComfortStatistics getComfortStats() {
		return comfortStat;
	}
	
	protected ComfortPreference getComfortPreferenceByMeasureId(String m) {
		for(ComfortMeasure me:comfortPrefs.keySet()) {
			if(me.getId().equals(m)) return comfortPrefs.get(me);
		}
		return null;
	}
	
	public Object[] calculateDoD(ArrayList<Object> actual, String comfortMeasure) { 
		Object[] toReturn = new Object[2];
		double[] discomfortFactors = {0,1,2};
		ComfortPreference pref = getComfortPreferenceByMeasureId(comfortMeasure); 
		if(pref == null) return null;
		Object[] dodCal = pref.getDoDCalculation();

		double dod = 0;
		
		double inherent = (Double)actual.get(0); 
		if(pref.checkNormal(actual)) { // has only the normal comfort part
			double[] bounds = (double[])dodCal[0];
			if(inherent<bounds[0]) dod = (bounds[0]-inherent)*discomfortFactors[0]*(syncInterval/3600);
			else dod = (inherent-bounds[1])*discomfortFactors[0]*(syncInterval/3600);
			toReturn[1] = "N";
		} else if(pref.checkReduced(actual)) { // has the reduced part and the normal part
			double[] bounds = (double[])dodCal[1];
			if(inherent<bounds[0]) {
				ArrayList<Object> refs = new ArrayList<Object>();
				for(Object ob:actual) {
					refs.add(ob);
				}
				refs.set(0, bounds[0]);
				dod = ((bounds[0]-inherent)*discomfortFactors[1]*((double)syncInterval/3600)) + (Double)calculateDoD(refs, comfortMeasure)[0];
			} else {
				ArrayList<Object> refs = new ArrayList<Object>();
				for(Object ob:actual) {
					refs.add(ob);
				}
				refs.set(0, bounds[1]);
				dod = ((inherent-bounds[1])*discomfortFactors[1]*((double)syncInterval/3600)) + (Double)calculateDoD(refs, comfortMeasure)[0];
			}
			toReturn[1] = "R"; 
		} else { // has all three parts
			double[] bounds = (double[])dodCal[2];
			if(inherent<bounds[0]) {
				ArrayList<Object> refs = new ArrayList<Object>();
				for(Object ob:actual) {
					refs.add(ob);
				}
				refs.set(0, bounds[0]);
				dod = ((bounds[0]-inherent)*discomfortFactors[2]*((double)syncInterval/3600)) + (Double)calculateDoD(refs, comfortMeasure)[0];
			} else {
				ArrayList<Object> refs = new ArrayList<Object>();
				for(Object ob:actual) {
					refs.add(ob);
				}
				refs.set(0, bounds[1]);
				dod = ((inherent-bounds[1])*discomfortFactors[2]*((double)syncInterval/3600)) + (Double)calculateDoD(refs, comfortMeasure)[0];
			}
			toReturn[1] = "U";
		}
		
		toReturn[0] = dod;
		return toReturn;
	}
	
	protected void createPowerScheduleFromSchedule() {
		powerSchedule = new double[schedule.length];
		for(int i=0; i<powerSchedule.length; i++) {
			if(schedule[i]!=0 && schedule[i]!=-9999) {
				powerSchedule[i] = device.getWatt();
			}
		}
	}
	
	//TODO: does not work with variable/program energy type devices
	public double[] getExpectedPeakLoad(int atInterval, int updateInterval, int syncInterval) {
		int deviceType = getDevice().getType();
		double[] halfHourPeak = new double[updateInterval*60/syncInterval];
		for(int i=0; i<halfHourPeak.length; i++) {
			double sch = schedule[atInterval % (24*3600/syncInterval) + i];
			if(sch!=0 && sch!=-9999) {
				switch(deviceType) {
				case Device.DEVTYPE_HEATER:
					HeaterModel h = (HeaterModel) this;
					halfHourPeak[i] = sch-h.getCurrentTemperature()>0.5?device.getWatt():0;
					break;
				case Device.DEVTYPE_EWATERHEATER:
					ElectricWaterHeaterModel wh = (ElectricWaterHeaterModel) this;
					halfHourPeak[i] = sch-wh.getTankTemperature()>1?device.getWatt():0;
					break;
				case Device.DEVTYPE_EVCHARGER:
					EVChargerModel ev = (EVChargerModel) this;
					halfHourPeak[i] = ev.lv2Charging()?device.getWatt():1100;
					break;
				default:
					halfHourPeak[i] = device.getWatt();
				}
			}
		}		
		
		return halfHourPeak;
	}
}
